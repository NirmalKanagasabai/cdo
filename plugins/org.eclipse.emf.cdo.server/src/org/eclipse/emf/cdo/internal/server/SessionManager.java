/**
 * Copyright (c) 2004 - 2010 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Simon McDuff - bug 201266
 *    Simon McDuff - bug 202725
 */
package org.eclipse.emf.cdo.internal.server;

import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.id.CDOIDAndVersion;
import org.eclipse.emf.cdo.common.model.CDOPackageUnit;
import org.eclipse.emf.cdo.common.protocol.CDOAuthenticationResult;
import org.eclipse.emf.cdo.common.protocol.CDOProtocolConstants;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.internal.server.bundle.OM;
import org.eclipse.emf.cdo.server.ISession;
import org.eclipse.emf.cdo.server.SessionCreationException;
import org.eclipse.emf.cdo.session.remote.CDORemoteSessionMessage;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranch;
import org.eclipse.emf.cdo.spi.server.ISessionProtocol;
import org.eclipse.emf.cdo.spi.server.InternalRepository;
import org.eclipse.emf.cdo.spi.server.InternalSession;
import org.eclipse.emf.cdo.spi.server.InternalSessionManager;

import org.eclipse.net4j.util.container.Container;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.eclipse.net4j.util.om.trace.ContextTracer;
import org.eclipse.net4j.util.security.IRandomizer;
import org.eclipse.net4j.util.security.IUserManager;
import org.eclipse.net4j.util.security.NegotiationException;
import org.eclipse.net4j.util.security.Randomizer;
import org.eclipse.net4j.util.security.SecurityUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eike Stepper
 */
public class SessionManager extends Container<ISession> implements InternalSessionManager
{
  public static final int DEFAULT_TOKEN_LENGTH = 1024;

  private static final ContextTracer TRACER = new ContextTracer(OM.DEBUG_SESSION, SessionManager.class);

  private InternalRepository repository;

  private String encryptionAlgorithmName = SecurityUtil.PBE_WITH_MD5_AND_DES;

  private byte[] encryptionSaltBytes = SecurityUtil.DEFAULT_SALT;

  private int encryptionIterationCount = SecurityUtil.DEFAULT_ITERATION_COUNT;

  private int tokenLength = DEFAULT_TOKEN_LENGTH;

  private IRandomizer randomizer;

  private IUserManager userManager;

  private Map<Integer, InternalSession> sessions = new HashMap<Integer, InternalSession>();

  private int lastSessionID;

  /**
   * @since 2.0
   */
  public SessionManager()
  {
  }

  /**
   * @since 2.0
   */
  public InternalRepository getRepository()
  {
    return repository;
  }

  /**
   * @since 2.0
   */
  public void setRepository(InternalRepository repository)
  {
    checkInactive();
    this.repository = repository;
  }

  public String getEncryptionAlgorithmName()
  {
    return encryptionAlgorithmName;
  }

  public void setEncryptionAlgorithmName(String encryptionAlgorithmName)
  {
    checkInactive();
    this.encryptionAlgorithmName = encryptionAlgorithmName;
  }

  public byte[] getEncryptionSaltBytes()
  {
    return encryptionSaltBytes;
  }

  public void setEncryptionSaltBytes(byte[] encryptionSaltBytes)
  {
    checkInactive();
    this.encryptionSaltBytes = encryptionSaltBytes;
  }

  public int getEncryptionIterationCount()
  {
    return encryptionIterationCount;
  }

  public void setEncryptionIterationCount(int encryptionIterationCount)
  {
    checkInactive();
    this.encryptionIterationCount = encryptionIterationCount;
  }

  public int getTokenLength()
  {
    return tokenLength;
  }

  public void setTokenLength(int tokenLength)
  {
    checkInactive();
    this.tokenLength = tokenLength;
  }

  public IRandomizer getRandomizer()
  {
    return randomizer;
  }

  public void setRandomizer(IRandomizer randomizer)
  {
    checkInactive();
    this.randomizer = randomizer;
  }

  public IUserManager getUserManager()
  {
    return userManager;
  }

  public void setUserManager(IUserManager userManager)
  {
    this.userManager = userManager;
  }

  public InternalSession[] getSessions()
  {
    synchronized (sessions)
    {
      return sessions.values().toArray(new InternalSession[sessions.size()]);
    }
  }

  /**
   * @since 2.0
   */
  public InternalSession getSession(int sessionID)
  {
    checkActive();
    synchronized (sessions)
    {
      return sessions.get(sessionID);
    }
  }

  public InternalSession[] getElements()
  {
    return getSessions();
  }

  @Override
  public boolean isEmpty()
  {
    synchronized (sessions)
    {
      return sessions.isEmpty();
    }
  }

  /**
   * @since 2.0
   */
  public InternalSession openSession(ISessionProtocol sessionProtocol) throws SessionCreationException
  {
    int id = ++lastSessionID;
    if (TRACER.isEnabled())
    {
      TRACER.trace("Opening session " + id); //$NON-NLS-1$
    }

    String userID = authenticateUser(sessionProtocol);
    InternalSession session = createSession(id, userID, sessionProtocol);
    synchronized (sessions)
    {
      sessions.put(id, session);
    }

    fireElementAddedEvent(session);
    handleRemoteSessionNotification(CDOProtocolConstants.REMOTE_SESSION_OPENED, session);
    return session;
  }

  protected InternalSession createSession(int id, String userID, ISessionProtocol protocol)
  {
    return new Session(this, protocol, id, userID);
  }

  public void sessionClosed(InternalSession session)
  {
    int sessionID = session.getSessionID();
    InternalSession removeSession = null;
    synchronized (sessions)
    {
      removeSession = sessions.remove(sessionID);
    }

    if (removeSession != null)
    {
      fireElementRemovedEvent(session);
      handleRemoteSessionNotification(CDOProtocolConstants.REMOTE_SESSION_CLOSED, session);
    }
  }

  public void handleBranchNotification(InternalCDOBranch branch, InternalSession excludedSession)
  {
    for (InternalSession session : getSessions())
    {
      if (session != excludedSession)
      {
        try
        {
          session.handleBranchNotification(branch);
        }
        catch (Exception ex)
        {
          OM.LOG.warn("A problem occured while notifying session " + session, ex);
        }
      }
    }
  }

  /**
   * @since 2.0
   */
  public void handleCommitNotification(CDOBranchPoint branchPoint, CDOPackageUnit[] packageUnits,
      List<CDOIDAndVersion> dirtyIDs, List<CDOID> detachedObjects, List<CDORevisionDelta> deltas,
      InternalSession excludedSession)
  {
    for (InternalSession session : getSessions())
    {
      if (session != excludedSession)
      {
        try
        {
          session.handleCommitNotification(branchPoint, packageUnits, dirtyIDs, detachedObjects, deltas);
        }
        catch (Exception ex)
        {
          OM.LOG.warn("A problem occured while notifying session " + session, ex);
        }
      }
    }
  }

  /**
   * @since 2.0
   */
  public void handleRemoteSessionNotification(byte opcode, InternalSession excludedSession)
  {
    try
    {
      for (InternalSession session : getSessions())
      {
        if (session != excludedSession && session.isSubscribed())
        {
          try
          {
            session.getProtocol().sendRemoteSessionNotification(opcode, excludedSession);
          }
          catch (Exception ex)
          {
            OM.LOG.warn("A problem occured while notifying session " + session, ex);
          }
        }
      }
    }
    catch (Exception ex)
    {
      OM.LOG.warn("A problem occured while notifying other sessions", ex);
    }
  }

  public List<Integer> sendMessage(InternalSession sender, CDORemoteSessionMessage message, int[] recipients)
  {
    List<Integer> result = new ArrayList<Integer>();
    for (int i = 0; i < recipients.length; i++)
    {
      InternalSession recipient = getSession(recipients[i]);
      if (recipient != null && recipient.isSubscribed())
      {
        recipient.getProtocol().sendRemoteMessageNotification(sender, message);
        result.add(recipient.getSessionID());
      }
    }

    return result;
  }

  protected String authenticateUser(ISessionProtocol protocol) throws SecurityException
  {
    if (userManager == null)
    {
      return null;
    }

    try
    {
      byte[] randomToken = createRandomToken();
      CDOAuthenticationResult result = protocol.sendAuthenticationChallenge(randomToken);
      String userID = result.getUserID();

      byte[] cryptedToken = encryptToken(userID, randomToken);
      boolean success = Arrays.equals(result.getCryptedToken(), cryptedToken);
      if (success)
      {
        return userID;
      }

      throw new SecurityException("User not authenticated"); //$NON-NLS-1$
    }
    catch (SecurityException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new SecurityException(ex);
    }
  }

  protected byte[] createRandomToken()
  {
    byte[] token = new byte[tokenLength];
    randomizer.nextBytes(token);
    return token;
  }

  protected byte[] encryptToken(String userID, byte[] token) throws NegotiationException
  {
    try
    {
      return userManager.encrypt(userID, token, getEncryptionAlgorithmName(), getEncryptionSaltBytes(),
          getEncryptionIterationCount());
    }
    catch (Exception ex)
    {
      OM.LOG.error("Token encryption failed", ex); //$NON-NLS-1$
      return null;
    }
  }

  @Override
  protected void doBeforeActivate() throws Exception
  {
    super.doBeforeActivate();
    if (userManager == null)
    {
      OM.LOG.info("No user manager configured. Users will not be authenticated");
    }
  }

  @Override
  protected void doActivate() throws Exception
  {
    super.doActivate();
    if (userManager != null)
    {
      if (randomizer == null)
      {
        randomizer = new Randomizer();
      }

      LifecycleUtil.activate(randomizer);
    }
  }

  @Override
  protected void doDeactivate() throws Exception
  {
    InternalSession[] activeSessions = getSessions();
    for (int i = 0; i < activeSessions.length; i++)
    {
      LifecycleUtil.deactivate(activeSessions[i]);
    }

    super.doDeactivate();
  }
}
