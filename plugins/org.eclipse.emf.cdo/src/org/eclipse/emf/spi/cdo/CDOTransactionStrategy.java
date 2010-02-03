/**
 * Copyright (c) 2004 - 2010 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon McDuff - initial API and implementation
 *    Eike Stepper - maintenance
 */
package org.eclipse.emf.spi.cdo;

import org.eclipse.emf.cdo.common.commit.CDOCommit;

import org.eclipse.emf.internal.cdo.transaction.CDOSingleTransactionStrategyImpl;

import org.eclipse.net4j.util.om.monitor.OMMonitor;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * TODO Simon: JavaDoc
 * 
 * @author Simon McDuff
 * @since 2.0
 */
public interface CDOTransactionStrategy
{
  public static final CDOTransactionStrategy DEFAULT = CDOSingleTransactionStrategyImpl.INSTANCE;

  public void setTarget(InternalCDOTransaction transaction);

  public void unsetTarget(InternalCDOTransaction transaction);

  /**
   * TODO Better use an {@link OMMonitor}?
   * 
   * @since 3.0
   */
  public CDOCommit commit(InternalCDOTransaction transaction, IProgressMonitor progressMonitor) throws Exception;

  /**
   * @since 3.0
   */
  public void rollback(InternalCDOTransaction transaction, InternalCDOUserSavepoint savepoint);

  /**
   * @since 3.0
   */
  public InternalCDOUserSavepoint setSavepoint(InternalCDOTransaction transaction);
}
