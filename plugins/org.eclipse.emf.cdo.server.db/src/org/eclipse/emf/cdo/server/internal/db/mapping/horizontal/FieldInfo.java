/**
 * Copyright (c) 2004 - 2009 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Stefan Winkler - major refactoring
 */
package org.eclipse.emf.cdo.server.internal.db.mapping.horizontal;

import org.eclipse.net4j.db.DBType;

/**
 * @author Eike Stepper
 * @author Stefan Winkler
 * @since 2.0
 */
public class FieldInfo
{
  private String name;

  private DBType dbType;

  public FieldInfo(String name, DBType dbType)
  {
    this.name = name;
    this.dbType = dbType;
  }

  public String getName()
  {
    return name;
  }

  public DBType getDbType()
  {
    return dbType;
  }
}
