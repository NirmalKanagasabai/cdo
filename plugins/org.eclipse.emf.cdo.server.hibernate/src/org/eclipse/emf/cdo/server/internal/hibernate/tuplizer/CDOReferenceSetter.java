/**
 * Copyright (c) 2004 - 2010 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Martin Taal - copied from CDORevisionPropertyHandler and adapted
 *    Eike Stepper - maintenance
 */
package org.eclipse.emf.cdo.server.internal.hibernate.tuplizer;

import org.eclipse.emf.cdo.common.id.CDOIDExternal;
import org.eclipse.emf.cdo.common.id.CDOIDMeta;
import org.eclipse.emf.cdo.common.revision.CDORevision;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionFactoryImplementor;

/**
 * @author Martin Taal
 */
public class CDOReferenceSetter extends CDOPropertySetter
{
  private static final long serialVersionUID = 1L;

  public CDOReferenceSetter(CDORevisionTuplizer tuplizer, String propertyName)
  {
    super(tuplizer, propertyName);
  }

  @Override
  public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException
  {
    if (value == null)
    {
      super.set(target, null, factory);
    }
    else if (value instanceof CDOIDMeta || value instanceof CDOIDExternal)
    {
      super.set(target, value, factory);
    }
    else
    {
      CDORevision cdoValue = (CDORevision)value;
      super.set(target, cdoValue.getID(), factory);
    }
  }
}
