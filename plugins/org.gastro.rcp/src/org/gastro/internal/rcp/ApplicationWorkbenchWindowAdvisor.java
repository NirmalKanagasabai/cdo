/**
 * Copyright (c) 2004 - 2010 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *
 *  Initial Publication:
 *    Eclipse Magazin - http://www.eclipse-magazin.de
 */
package org.gastro.internal.rcp;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import org.gastro.rcp.IConfiguration;

/**
 * @author Eike Stepper
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor
{
  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer)
  {
    super(configurer);
  }

  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer)
  {
    return new ApplicationActionBarAdvisor(configurer);
  }

  public void preWindowOpen()
  {
    IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
    configurer.setInitialSize(new Point(800, 600));
    configurer.setShowCoolBar(false);
    configurer.setShowStatusLine(false);
    configurer.setTitle(IConfiguration.INSTANCE.getRestaurant() + " - " + IConfiguration.INSTANCE.getStation());
  }

  // public void postWindowCreate()
  // {
  // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setMaximized(true);
  // }
}
