/*
 * Copyright (c) 2010-2012 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.internal.explorer.repositories;

import org.eclipse.emf.cdo.common.CDOCommonSession.Options.PassiveUpdateMode;
import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.explorer.checkouts.CDOCheckout;
import org.eclipse.emf.cdo.explorer.checkouts.CDOCheckoutSource;
import org.eclipse.emf.cdo.explorer.repositories.CDORepository;
import org.eclipse.emf.cdo.internal.explorer.AbstractElement;
import org.eclipse.emf.cdo.internal.explorer.bundle.OM;
import org.eclipse.emf.cdo.net4j.CDONet4jSessionConfiguration;
import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.emf.cdo.session.CDOSession;
import org.eclipse.emf.cdo.session.CDOSessionConfiguration;

import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.util.ObjectUtil;
import org.eclipse.net4j.util.container.ContainerEvent;
import org.eclipse.net4j.util.container.IContainerEvent;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.container.IPluginContainer;
import org.eclipse.net4j.util.event.IEvent;
import org.eclipse.net4j.util.event.IListener;
import org.eclipse.net4j.util.io.IOUtil;
import org.eclipse.net4j.util.lifecycle.ILifecycle;
import org.eclipse.net4j.util.lifecycle.LifecycleEventAdapter;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author Eike Stepper
 */
public abstract class CDORepositoryImpl extends AbstractElement implements CDORepository
{
  private final Set<CDOCheckout> checkouts = new HashSet<CDOCheckout>();

  private final Set<CDOCheckout> openCheckouts = new HashSet<CDOCheckout>();

  private final LifecycleEventAdapter sessionListener = new LifecycleEventAdapter()
  {
    @Override
    protected void onDeactivated(ILifecycle lifecycle)
    {
      disconnect();
    }
  };

  private final IListener mainBranchListener = new IListener()
  {
    public void notifyEvent(IEvent event)
    {
      if (event instanceof IContainerEvent)
      {
        @SuppressWarnings("unchecked")
        IContainerEvent<CDOBranch> e = (IContainerEvent<CDOBranch>)event;

        fireEvent(new ContainerEvent<CDOBranch>(CDORepositoryImpl.this, Arrays.asList(e.getDeltas())));
      }
    }
  };

  private String repositoryName;

  private State state = State.Disconnected;

  private CDOSession session;

  public CDORepositoryImpl()
  {
  }

  public final String getRepositoryName()
  {
    return repositoryName;
  }

  public final State getState()
  {
    return state;
  }

  public final boolean isConnected()
  {
    return session != null;
  }

  public final void connect()
  {
    boolean connected = false;

    synchronized (sessionListener)
    {
      if (!isConnected())
      {
        try
        {
          state = State.Connecting;

          session = openSession();
          session.addListener(sessionListener);
          session.getBranchManager().getMainBranch().addListener(mainBranchListener);

          state = State.Connected;
        }
        catch (RuntimeException ex)
        {
          state = State.Disconnected;
          throw ex;
        }
        catch (Error ex)
        {
          state = State.Disconnected;
          throw ex;
        }

        connected = true;
      }
    }

    if (connected)
    {
      CDORepositoryManagerImpl manager = getManager();
      if (manager != null)
      {
        manager.fireRepositoryConnectionEvent(this, session, true);
      }
    }
  }

  public final void disconnect()
  {
    boolean disconnected = false;
    CDOSession oldSession = null;

    synchronized (sessionListener)
    {
      if (isConnected())
      {
        state = State.Disconnecting;
        oldSession = session;

        try
        {
          closeSession();
        }
        finally
        {
          session = null;
          state = State.Disconnected;
        }

        disconnected = true;
      }
    }

    if (disconnected)
    {
      CDORepositoryManagerImpl manager = getManager();
      if (manager != null)
      {
        manager.fireRepositoryConnectionEvent(this, oldSession, false);
      }
    }
  }

  public final void disconnectIfUnused()
  {
    synchronized (checkouts)
    {
      if (openCheckouts.isEmpty())
      {
        disconnect();
      }
    }
  }

  public final CDOSession getSession()
  {
    return session;
  }

  public void delete()
  {
    CDORepositoryManagerImpl manager = getManager();
    if (manager != null)
    {
      manager.removeElement(this);
    }

    IOUtil.delete(getFolder());
  }

  public final CDOCheckout[] getCheckouts()
  {
    synchronized (checkouts)
    {
      return checkouts.toArray(new CDOCheckout[checkouts.size()]);
    }
  }

  public final void addCheckout(CDOCheckout checkout)
  {
    synchronized (checkouts)
    {
      checkouts.add(checkout);
    }
  }

  public final void removeCheckout(CDOCheckout checkout)
  {
    synchronized (checkouts)
    {
      checkouts.remove(checkout);
    }
  }

  public final CDOSession openCheckout(CDOCheckout checkout)
  {
    connect();

    synchronized (checkouts)
    {
      openCheckouts.add(checkout);
    }

    return session;
  }

  public final void closeCheckout(CDOCheckout checkout)
  {
    synchronized (checkouts)
    {
      openCheckouts.remove(checkout);
    }
  }

  public final boolean isEmpty()
  {
    if (isConnected())
    {
      return session.getBranchManager().getMainBranch().isEmpty();
    }

    return false;
  }

  public final CDOBranch[] getElements()
  {
    if (isConnected())
    {
      return session.getBranchManager().getMainBranch().getBranches();
    }

    return new CDOBranch[0];
  }

  @Override
  @SuppressWarnings({ "rawtypes" })
  public Object getAdapter(Class adapter)
  {
    if (adapter == CDOCheckoutSource.class && isConnected())
    {
      final CDOID rootID = session.getRepositoryInfo().getRootResourceID();

      return new CDOCheckoutSource()
      {
        public CDORepository getRepository()
        {
          return CDORepositoryImpl.this;
        }

        public String getBranchPath()
        {
          return CDOBranch.MAIN_BRANCH_NAME;
        }

        public long getTimeStamp()
        {
          return CDOBranchPoint.UNSPECIFIED_DATE;
        }

        public CDOID getRootID()
        {
          return rootID;
        }
      };
    }

    return super.getAdapter(adapter);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == this)
    {
      return true;
    }

    if (obj instanceof CDORepository)
    {
      CDORepository that = (CDORepository)obj;
      return ObjectUtil.equals(getConnectorType(), that.getConnectorType())
          && ObjectUtil.equals(getConnectorDescription(), that.getConnectorDescription())
          && ObjectUtil.equals(repositoryName, that.getRepositoryName());
    }

    return false;
  }

  @Override
  public int hashCode()
  {
    return ObjectUtil.hashCode(getConnectorType()) ^ ObjectUtil.hashCode(getConnectorDescription())
        ^ ObjectUtil.hashCode(repositoryName);
  }

  @Override
  public String toString()
  {
    return getConnectorType() + "://" + getConnectorDescription() + "/" + repositoryName;
  }

  @Override
  protected void init(File folder, String type, Properties properties)
  {
    super.init(folder, type, properties);
    repositoryName = properties.getProperty("repositoryName");
  }

  @Override
  protected void collectProperties(Properties properties)
  {
    super.collectProperties(properties);
    properties.put("repositoryName", repositoryName);
  }

  protected IManagedContainer getContainer()
  {
    return IPluginContainer.INSTANCE;
  }

  protected IConnector getConnector()
  {
    IManagedContainer container = getContainer();
    return Net4jUtil.getConnector(container, getConnectorType(), getConnectorDescription());
  }

  protected CDOSessionConfiguration createSessionConfiguration()
  {
    IConnector connector = getConnector();

    CDONet4jSessionConfiguration config = CDONet4jUtil.createNet4jSessionConfiguration();
    config.setConnector(connector);
    config.setRepositoryName(repositoryName);
    return config;
  }

  protected CDOSession openSession()
  {
    CDOSessionConfiguration sessionConfiguration = createSessionConfiguration();
    sessionConfiguration.setPassiveUpdateEnabled(true);
    sessionConfiguration.setPassiveUpdateMode(PassiveUpdateMode.CHANGES);

    CDOSession session = sessionConfiguration.openSession();
    session.options().setGeneratedPackageEmulationEnabled(true);
    return session;
  }

  protected void closeSession()
  {
    session.close();
  }

  private static CDORepositoryManagerImpl getManager()
  {
    return OM.getRepositoryManager();
  }
}