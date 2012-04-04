/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Fluegge - initial API and implementation
 */
package org.eclipse.emf.cdo.dawn.appearance;

import org.eclipse.gef.EditPart;

/**
 * An EditPartStylizer can influence the visual representation of the models state. Dawn knows three states - default,
 * conflicted and locked. By implementing an own DawnStylizer you can influence the appearance of the three states for
 * your EditPart and it's related models. New stylizer can be registered to Dawn using the
 * <b>org.eclipse.emf.cdo.dawn.editpartstylizers</b> extension point.
 * 
 * @author Martin Fluegge
 */
public interface DawnEditPartStylizer
{
  public void setDefault(EditPart editPart);

  public void setConflicted(EditPart editPart, int type);

  public void setLocked(EditPart editPart, int type);
}