/**
 * Copyright (c) 2004 - 2010 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.internal.server.syncing;

import org.eclipse.emf.cdo.common.CDOCommonRepository;
import org.eclipse.emf.cdo.common.CDOCommonSession.Options.PassiveUpdateMode;
import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchCreatedEvent;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfo;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.internal.common.revision.cache.noop.NOOPRevisionCache;
import org.eclipse.emf.cdo.internal.server.bundle.OM;
import org.eclipse.emf.cdo.session.CDOSessionConfiguration;
import org.eclipse.emf.cdo.session.CDOSessionConfigurationFactory;
import org.eclipse.emf.cdo.session.CDOSessionInvalidationEvent;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionCache;
import org.eclipse.emf.cdo.spi.server.InternalRepositorySynchronizer;
import org.eclipse.emf.cdo.spi.server.InternalSynchronizableRepository;

import org.eclipse.net4j.util.concurrent.ConcurrencyUtil;
import org.eclipse.net4j.util.concurrent.QueueRunner;
import org.eclipse.net4j.util.event.IEvent;
import org.eclipse.net4j.util.event.IListener;
import org.eclipse.net4j.util.lifecycle.ILifecycleEvent;
import org.eclipse.net4j.util.om.trace.ContextTracer;

import org.eclipse.emf.spi.cdo.CDOSessionProtocol;
import org.eclipse.emf.spi.cdo.InternalCDOSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author Eike Stepper
 * @since 3.0
 */
public class RepositorySynchronizer extends QueueRunner implements InternalRepositorySynchronizer
{
  public static final int DEFAULT_RETRY_INTERVAL = 3;

  private static final ContextTracer TRACER = new ContextTracer(OM.DEBUG_REPOSITORY, RepositorySynchronizer.class);

  private static final Integer CONNECT_PRIORITY = 0;

  private static final Integer REPLICATE_PRIORITY = 1;

  private static final Integer BRANCH_PRIORITY = 2;

  private static final Integer COMMIT_PRIORITY = 3;

  private int retryInterval = DEFAULT_RETRY_INTERVAL;

  private Object connectLock = new Object();

  private InternalSynchronizableRepository localRepository;

  private InternalCDOSession remoteSession;

  private RemoteSessionListener remoteSessionListener = new RemoteSessionListener();

  private CDOSessionConfigurationFactory remoteSessionConfigurationFactory;

  private boolean rawReplication;

  private boolean squeezeCommitInfos;

  public RepositorySynchronizer()
  {
  }

  public int getRetryInterval()
  {
    return retryInterval;
  }

  public void setRetryInterval(int retryInterval)
  {
    this.retryInterval = retryInterval;
  }

  public InternalSynchronizableRepository getLocalRepository()
  {
    return localRepository;
  }

  public void setLocalRepository(InternalSynchronizableRepository localRepository)
  {
    checkInactive();
    this.localRepository = localRepository;
  }

  public CDOSessionConfigurationFactory getRemoteSessionConfigurationFactory()
  {
    return remoteSessionConfigurationFactory;
  }

  public void setRemoteSessionConfigurationFactory(CDOSessionConfigurationFactory remoteSessionConfigurationFactory)
  {
    checkArg(remoteSessionConfigurationFactory, "remoteSessionConfigurationFactory"); //$NON-NLS-1$
    this.remoteSessionConfigurationFactory = remoteSessionConfigurationFactory;
  }

  public InternalCDOSession getRemoteSession()
  {
    return remoteSession;
  }

  public boolean isRawReplication()
  {
    return rawReplication;
  }

  public void setRawReplication(boolean rawReplication)
  {
    checkInactive();
    this.rawReplication = rawReplication;
  }

  public boolean isSqueezeCommitInfos()
  {
    return squeezeCommitInfos;
  }

  public void setSqueezeCommitInfos(boolean squeezeCommitInfos)
  {
    checkInactive();
    this.squeezeCommitInfos = squeezeCommitInfos;
  }

  @Override
  protected String getThreadName()
  {
    return "RepositorySynchronizer"; //$NON-NLS-1$
  }

  @Override
  protected BlockingQueue<Runnable> createQueue()
  {
    return new PriorityBlockingQueue<Runnable>();
  }

  @Override
  protected void doBeforeActivate() throws Exception
  {
    super.doBeforeActivate();
    checkState(remoteSessionConfigurationFactory, "remoteSessionConfigurationFactory"); //$NON-NLS-1$
    checkState(localRepository, "localRepository"); //$NON-NLS-1$
  }

  @Override
  protected void doAfterActivate() throws Exception
  {
    super.doAfterActivate();
    scheduleConnect();
  }

  @Override
  protected void doDeactivate() throws Exception
  {
    if (remoteSession != null)
    {
      remoteSession.removeListener(remoteSessionListener);
      remoteSession.getBranchManager().removeListener(remoteSessionListener);
      remoteSession.close();
      remoteSession = null;
    }

    super.doDeactivate();
  }

  private void disconnect()
  {
    OM.LOG.info("Disconnected from master.");
    if (localRepository.getRootResourceID() == null)
    {
      localRepository.setState(CDOCommonRepository.State.INITIAL);
    }
    else
    {
      localRepository.setState(CDOCommonRepository.State.OFFLINE);
    }

    remoteSession.getBranchManager().removeListener(remoteSessionListener);
    remoteSession.removeListener(remoteSessionListener);
    remoteSession = null;

    reconnect();
  }

  private void reconnect()
  {
    clearQueue();
    if (isActive())
    {
      scheduleConnect();
    }
  }

  private void scheduleConnect()
  {
    synchronized (connectLock)
    {
      if (localRepository.getState().isConnected())
      {
        return;
      }

      if (isActive())
      {
        addWork(new ConnectRunnable());
      }
    }
  }

  private void scheduleReplicate()
  {
    if (isActive())
    {
      addWork(new ReplicateRunnable());
    }
  }

  /**
   * @author Eike Stepper
   */
  private final class RemoteSessionListener implements IListener
  {
    public void notifyEvent(IEvent event)
    {
      if (!isActive())
      {
        return;
      }

      if (event instanceof CDOBranchCreatedEvent)
      {
        CDOBranchCreatedEvent e = (CDOBranchCreatedEvent)event;
        addWork(new BranchRunnable(e.getBranch()));
      }
      else if (event instanceof CDOSessionInvalidationEvent)
      {
        CDOSessionInvalidationEvent e = (CDOSessionInvalidationEvent)event;
        if (e.isRemote())
        {
          addWork(new CommitRunnable(e));
        }
      }
      else if (event instanceof ILifecycleEvent)
      {
        ILifecycleEvent e = (ILifecycleEvent)event;
        if (e.getKind() == ILifecycleEvent.Kind.DEACTIVATED && e.getSource() == remoteSession)
        {
          disconnect();
        }
      }
    }
  }

  /**
   * @author Eike Stepper
   */
  private static abstract class QueueRunnable implements Runnable, Comparable<QueueRunnable>
  {
    public int compareTo(QueueRunnable o)
    {
      return getPriority().compareTo(o.getPriority());
    }

    protected abstract Integer getPriority();
  }

  /**
   * @author Eike Stepper
   */
  private final class ConnectRunnable extends QueueRunnable
  {
    public ConnectRunnable()
    {
    }

    public void run()
    {
      synchronized (connectLock)
      {
        checkActive();
        if (TRACER.isEnabled())
        {
          TRACER.trace("Connecting to master..."); //$NON-NLS-1$
        }

        try
        {
          CDOSessionConfiguration masterConfiguration = remoteSessionConfigurationFactory.createSessionConfiguration();
          masterConfiguration.setPassiveUpdateMode(PassiveUpdateMode.ADDITIONS);

          remoteSession = (InternalCDOSession)masterConfiguration.openSession();

          ensureNOOPRevisionCache();
          setRootResourceID();
        }
        catch (Exception ex)
        {
          if (isActive())
          {
            OM.LOG.warn("Connection attempt failed. Retrying in " + retryInterval + " seconds...");
            long end = System.currentTimeMillis() + 1000L * retryInterval;

            for (;;)
            {
              long now = System.currentTimeMillis();
              if (now >= end || !isActive())
              {
                break;
              }

              ConcurrencyUtil.sleep(Math.min(100L, end - now));
            }

            reconnect();
          }

          return;
        }

        OM.LOG.info("Connected to master.");
        remoteSession.addListener(remoteSessionListener);
        remoteSession.getBranchManager().addListener(remoteSessionListener);

        scheduleReplicate();
      }
    }

    @Override
    protected Integer getPriority()
    {
      return CONNECT_PRIORITY;
    }

    private void setRootResourceID()
    {
      if (localRepository.getState() == CDOCommonRepository.State.INITIAL)
      {
        CDOID rootResourceID = remoteSession.getRepositoryInfo().getRootResourceID();
        localRepository.setRootResourceID(rootResourceID);
        localRepository.setState(CDOCommonRepository.State.OFFLINE);
      }
    }

    private void ensureNOOPRevisionCache()
    {
      // Ensure that incoming revisions are not cached!
      InternalCDORevisionCache cache = remoteSession.getRevisionManager().getCache();
      if (!(cache instanceof NOOPRevisionCache))
      {
        throw new IllegalStateException("Master session does not use a NOOPRevisionCache: "
            + cache.getClass().getName());
      }
    }
  }

  /**
   * @author Eike Stepper
   */
  private final class ReplicateRunnable extends QueueRunnable
  {
    public ReplicateRunnable()
    {
    }

    public void run()
    {
      checkActive();
      if (TRACER.isEnabled())
      {
        TRACER.trace("Synchronizing with master..."); //$NON-NLS-1$
      }

      localRepository.setState(CDOCommonRepository.State.SYNCING);

      CDOSessionProtocol sessionProtocol = remoteSession.getSessionProtocol();
      if (isRawReplication())
      {
        sessionProtocol.replicateRepositoryRaw(localRepository);
      }
      else
      {
        sessionProtocol.replicateRepository(localRepository);
      }

      localRepository.setState(CDOCommonRepository.State.ONLINE);
      OM.LOG.info("Synchronized with master.");
    }

    @Override
    protected Integer getPriority()
    {
      return REPLICATE_PRIORITY;
    }
  }

  /**
   * @author Eike Stepper
   */
  private final class BranchRunnable extends QueueRunnable
  {
    private CDOBranch branch;

    public BranchRunnable(CDOBranch branch)
    {
      this.branch = branch;
    }

    public void run()
    {
      localRepository.handleBranch(branch);
    }

    @Override
    public int compareTo(QueueRunnable o)
    {
      int result = super.compareTo(o);
      if (result == 0)
      {
        result = branch.compareTo(((BranchRunnable)o).branch);
      }

      return result;
    }

    @Override
    protected Integer getPriority()
    {
      return BRANCH_PRIORITY;
    }
  }

  /**
   * @author Eike Stepper
   */
  private final class CommitRunnable extends QueueRunnable
  {
    private CDOCommitInfo commitInfo;

    public CommitRunnable(CDOCommitInfo commitInfo)
    {
      this.commitInfo = commitInfo;
    }

    public void run()
    {
      localRepository.handleCommitInfo(commitInfo);
    }

    @Override
    public int compareTo(QueueRunnable o)
    {
      int result = super.compareTo(o);
      if (result == 0)
      {
        Long timeStamp = commitInfo.getTimeStamp();
        Long timeStamp2 = ((CommitRunnable)o).commitInfo.getTimeStamp();
        result = timeStamp < timeStamp2 ? -1 : timeStamp == timeStamp2 ? 0 : 1;
      }

      return result;
    }

    @Override
    protected Integer getPriority()
    {
      return COMMIT_PRIORITY;
    }
  }
}
