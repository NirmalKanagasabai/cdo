/***************************************************************************
 * Copyright (c) 2004 - 2008 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Simon McDuff - https://bugs.eclipse.org/201266
 *    Simon McDuff - https://bugs.eclipse.org/201997
 *    Simon McDuff - https://bugs.eclipse.org/233490
 **************************************************************************/
package org.eclipse.emf.cdo;

import org.eclipse.emf.cdo.common.CDOProtocolView;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.query.CDOQuery;

import org.eclipse.net4j.util.event.INotifier;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * @author Eike Stepper
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface CDOView extends CDOProtocolView, INotifier
{
  public static final long UNSPECIFIED_DATE = CDORevision.UNSPECIFIED_DATE;

  public static final int NO_PRELOAD = 1;

  public CDOSession getSession();

  public ResourceSet getResourceSet();

  public boolean isDirty();

  public boolean hasConflict();

  public boolean hasUniqueResourceContents();

  /**
   * Specifies whether the contents list of resources will be unique or not.
   * <p>
   * This property is transient in that it does not stick with resources outside of the scope of this view. Especially
   * it will not be persisted with resources in the repository. Each new view will start with <code>true</code> as a
   * default value. Changing to <code>false</code> will subsequently apply to all resources being loaded or created.
   * <p>
   * Notice that the resource contents is a containment list and as such <b>must be</b> unique. Setting this property to
   * <code>false</code> is only recommended for performance optimization when uniqueness is granted by other means.
   * Violating the uniqueness constraint will result in unpredictable behaviour and possible corruption of the
   * repository!
   */
  public void setUniqueResourceContents(boolean uniqueResourceContents);

  public boolean isInvalidationNotificationsEnabled();

  public void setInvalidationNotificationsEnabled(boolean invalidationNotificationsEnabled);

  /**
   * @since 2.0
   */
  public CDOChangeSubscriptionPolicy getChangeSubscriptionPolicy();

  /**
   * Specifies the change subscription policy. By default, the value is set to {@link CDOChangeSubscriptionPolicy#NONE}.
   * <p>
   * To activate the policy you need to do the following : <br>
   * e.g.: <code>transaction.setChangeSubscriptionPolicy(CDOChangeSubscriptionPolicy.ALL);</code>
   * <p>
   * To register an object you need to add an adapter to this object. <br>
   * e.g.: <code>eObject.eAdapters().add(anAdapter);</code>
   * <p>
   * By activating this feature, every objects that have at least one adapter that match the current policy will be
   * registered to the server and will be notify for every changes happening on any other CDOTransaction.
   * <p>
   * {@link CDOChangeSubscriptionPolicy#NONE} - Disabled. <br>
   * {@link CDOChangeSubscriptionPolicy#ALL} - Enabled for all adapters used.<br>
   * {@link CDOChangeSubscriptionPolicy#ONLY_CDOADAPTER} - Enabled only for adapters that implement {@link CDOAdapter}.<br>
   * Any others classes that implement {@link CDOChangeSubscriptionPolicy} - Enabled for whatever rules define in that
   * class. <br>
   * <p>
   * If the <code>anAdapter</code> matches the current policy, <code>eObject</code> will be registered to the server and
   * you will receive all changes from others {@link CDOTransaction}.
   * <p>
   * When the policy changed it will recalculate automatically every objects in the cache.
   * <p>
   * You can subscribe temporary objects. Even if you cannot receive notification from other {@link CDOTransaction} for
   * that object because it is only local to you, at commit time these objects will be registered automatically.
   * <p>
   * <b>Note :</b> It can be used with <code> CDOSession.setPassiveUpdate(false) </code>. In this case, it will receive
   * changes without having the object change.
   * 
   * @since 2.0
   */
  public void setChangeSubscriptionPolicy(CDOChangeSubscriptionPolicy changeSubscriptionPolicy);

  public int getLoadRevisionCollectionChunkSize();

  public void setLoadRevisionCollectionChunkSize(int loadRevisionCollectionChunkSize);

  public boolean hasResource(String path);

  /**
   * @see ResourceSet#getResource(URI, boolean)
   */
  public CDOResource getResource(String path);

  public CDOObject getObject(CDOID id);

  public CDOObject getObject(CDOID id, boolean loadOnDemand);

  public boolean isObjectRegistered(CDOID id);

  public int reload(CDOObject... objects);

  /**
   * @since 2.0
   */
  public CDOQuery createQuery(String language, String queryString);

  public void close();
}
