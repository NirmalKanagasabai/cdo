/***************************************************************************
 * Copyright (c) 2004 - 2008 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.emf.cdo.internal.server;

import org.eclipse.emf.cdo.internal.protocol.id.CDOIDObjectFactoryImpl;
import org.eclipse.emf.cdo.protocol.id.CDOID;
import org.eclipse.emf.cdo.protocol.id.CDOIDObjectFactory;
import org.eclipse.emf.cdo.protocol.id.CDOIDUtil;

/**
 * @author Eike Stepper
 */
public abstract class LongIDStore extends Store
{
  protected static final long CRASHED = -1L;

  private static final CDOIDObjectFactory CDOID_OBJECT_FACTORY = new CDOIDObjectFactoryImpl();

  private transient long lastObjectID;

  public LongIDStore(String type)
  {
    super(type);
  }

  public synchronized CDOID getNextCDOID()
  {
    return CDOIDUtil.createCDOID(++lastObjectID);
  }

  public long getLastObjectID()
  {
    return lastObjectID;
  }

  public void setLastObjectID(long lastObjectID)
  {
    this.lastObjectID = lastObjectID;
  }

  public boolean wasCrashed()
  {
    return lastObjectID == CRASHED;
  }

  public CDOIDObjectFactory getCDOIDObjectFactory()
  {
    return CDOID_OBJECT_FACTORY;
  }
}
