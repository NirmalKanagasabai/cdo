/***************************************************************************
 * Copyright (c) 2004 - 2008 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Simon McDuff - maintenance
 *    Victor Roldan Betancort - maintenance
 **************************************************************************/
package org.eclipse.emf.cdo.net4j;

/**
 * @since 2.0
 * @author Eike Stepper
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface CDOSession extends org.eclipse.emf.cdo.session.CDOSession
{
  /**
   * Returns the {@link Options options} of this session.
   */
  public Options options();

  /**
   * @author Eike Stepper
   */
  public interface Options extends org.eclipse.emf.cdo.session.CDOSession.Options
  {
    /**
     * Returns the Net4j {@link CDOSessionProtocol protocol} instance that represents the underlying
     * <em>signalling connection</em> to the repository of this session.
     */
    public CDOSessionProtocol getProtocol();
  }
}
