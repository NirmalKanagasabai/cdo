/**
 * Copyright (c) 2004 - 2010 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.net4j.util.event;

import java.util.concurrent.ExecutorService;

/**
 * @author Eike Stepper
 * @since 2.0
 */
public class ExecutorServiceNotifier extends Notifier
{
  private ExecutorService notificationExecutorService;

  public ExecutorServiceNotifier()
  {
  }

  @Override
  public ExecutorService getNotificationService()
  {
    return notificationExecutorService;
  }

  public void setNotificationExecutorService(ExecutorService notificationExecutorService)
  {
    this.notificationExecutorService = notificationExecutorService;
  }

  /**
   * @author Eike Stepper
   */
  public static class ThreadPool extends ExecutorServiceNotifier
  {
  }
}
