/*
 * Copyright (c) 2016 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.server.internal.embedded;

import org.eclipse.emf.cdo.net4j.CDONet4jSession;
import org.eclipse.emf.cdo.spi.server.InternalRepository;
import org.eclipse.emf.cdo.spi.server.InternalSession;
import org.eclipse.emf.cdo.spi.server.InternalSessionManager;

import org.eclipse.net4j.util.event.IListener;
import org.eclipse.net4j.util.lifecycle.ILifecycle;
import org.eclipse.net4j.util.lifecycle.LifecycleException;
import org.eclipse.net4j.util.lifecycle.LifecycleState;

/**
 * @author Eike Stepper
 */
public abstract class AbstractClientManager<T extends ILifecycle> implements ILifecycle
{
  protected final T delegate;

  protected CDONet4jSession clientSession;

  protected InternalSession serverSession;

  private LifecycleState lifecycleState = LifecycleState.INACTIVE;

  public AbstractClientManager(T delegate)
  {
    this.delegate = delegate;
  }

  public boolean hasListeners()
  {
    return delegate.hasListeners();
  }

  public IListener[] getListeners()
  {
    return delegate.getListeners();
  }

  public void addListener(IListener listener)
  {
    delegate.addListener(listener);
  }

  public void removeListener(IListener listener)
  {
    delegate.removeListener(listener);
  }

  public LifecycleState getLifecycleState()
  {
    return lifecycleState;
  }

  public boolean isActive()
  {
    return lifecycleState == LifecycleState.ACTIVE;
  }

  public void activate() throws LifecycleException
  {
    lifecycleState = LifecycleState.ACTIVE;
  }

  public Exception deactivate()
  {
    lifecycleState = LifecycleState.INACTIVE;
    clientSession = null;
    serverSession = null;
    return null;
  }

  protected final void initServerSession(CDONet4jSession clientSession)
  {
    this.clientSession = clientSession;

    InternalRepository repository = getRepository(delegate);
    InternalSessionManager sessionManager = repository.getSessionManager();
    serverSession = sessionManager.getSession(clientSession.getSessionID());
  }

  protected abstract InternalRepository getRepository(T delegate);
}
