/***************************************************************************
 * Copyright (c) 2004 - 2007 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.net4j.db.internal.derby;

import org.eclipse.net4j.db.IDBField.Type;
import org.eclipse.net4j.internal.db.DBAdapter;
import org.eclipse.net4j.internal.db.DBField;

import org.apache.derby.jdbc.EmbeddedDriver;

import java.sql.Driver;

/**
 * @author Eike Stepper
 */
public class DerbyAdapter extends DBAdapter
{
  public DerbyAdapter()
  {
    super("derby", "10.2.2.0");
  }

  public Driver getJDBCDriver()
  {
    return new EmbeddedDriver();
  }

  @Override
  protected String getTypeName(DBField field)
  {
    Type type = field.getType();
    switch (type)
    {
    case BOOLEAN:
    case BIT:
      return "SMALLINT";
    }

    return super.getTypeName(field);
  }
}
