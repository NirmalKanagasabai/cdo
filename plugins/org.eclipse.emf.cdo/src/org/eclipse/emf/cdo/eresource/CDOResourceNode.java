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
package org.eclipse.emf.cdo.eresource;

import org.eclipse.emf.cdo.CDOObject;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;

import java.io.IOException;
import java.util.Map;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>CDO Resource Node</b></em>'. <!-- end-user-doc
 * -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.emf.cdo.eresource.CDOResourceNode#getFolder <em>Folder</em>}</li>
 * <li>{@link org.eclipse.emf.cdo.eresource.CDOResourceNode#getName <em>Name</em>}</li>
 * <li>{@link org.eclipse.emf.cdo.eresource.CDOResourceNode#getPath <em>Path</em>}</li>
 * </ul>
 * </p>
 * 
 * @see org.eclipse.emf.cdo.eresource.EresourcePackage#getCDOResourceNode()
 * @model abstract="true"
 * @generated
 * @since 2.0
 */
public interface CDOResourceNode extends CDOObject
{
  /**
   * @ADDED
   * @since 2.0
   */
  public static final String ROOT_PATH = "/"; //$NON-NLS-1$

  /**
   * @ADDED
   * @since 2.0
   */
  public static final String ROOT_NAME = null;

  /**
   * @ADDED
   * @since 2.0
   */
  public boolean isRoot();

  /**
   * Returns the value of the '<em><b>Folder</b></em>' container reference. It is bidirectional and its opposite is '
   * {@link org.eclipse.emf.cdo.eresource.CDOResourceFolder#getNodes <em>Nodes</em>}'. <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Folder</em>' container reference isn't clear, there really should be more of a
   * description here...
   * </p>
   * <!-- end-user-doc -->
   * 
   * @return the value of the '<em>Folder</em>' container reference.
   * @see #setFolder(CDOResourceFolder)
   * @see org.eclipse.emf.cdo.eresource.EresourcePackage#getCDOResourceNode_Folder()
   * @see org.eclipse.emf.cdo.eresource.CDOResourceFolder#getNodes
   * @model opposite="nodes" transient="false"
   * @generated
   */
  CDOResourceFolder getFolder();

  /**
   * Sets the value of the '{@link org.eclipse.emf.cdo.eresource.CDOResourceNode#getFolder <em>Folder</em>}' container
   * reference. <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @param value
   *          the new value of the '<em>Folder</em>' container reference.
   * @see #getFolder()
   * @generated
   */
  void setFolder(CDOResourceFolder value);

  /**
   * Returns the value of the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name</em>' attribute isn't clear, there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * 
   * @return the value of the '<em>Name</em>' attribute.
   * @see #setName(String)
   * @see org.eclipse.emf.cdo.eresource.EresourcePackage#getCDOResourceNode_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link org.eclipse.emf.cdo.eresource.CDOResourceNode#getName <em>Name</em>}' attribute. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @param value
   *          the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Path</b></em>' attribute. <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Path</em>' attribute isn't clear, there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * 
   * @return the value of the '<em>Path</em>' attribute.
   * @see #setPath(String)
   * @see org.eclipse.emf.cdo.eresource.EresourcePackage#getCDOResourceNode_Path()
   * @model transient="true" volatile="true" derived="true"
   * @generated
   */
  String getPath();

  /**
   * Sets the value of the '{@link org.eclipse.emf.cdo.eresource.CDOResourceNode#getPath <em>Path</em>}' attribute. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @param value
   *          the new value of the '<em>Path</em>' attribute.
   * @see #getPath()
   * @generated
   */
  void setPath(String value);

  /**
   * @ADDED
   */
  public URI getURI();

  /**
   * {@link URIConverter#delete(URI, Map) deletes} the resource using the specified options, {@link #unload() unloads}
   * it, and then removes it from the {@link #getResourceSet() containing} resource set.
   * <p>
   * Options are handled generically as feature-to-setting entries; the resource will ignore options it doesn't
   * recognize. The options could even include things like an Eclipse progress monitor...
   * </p>
   * <p>
   * An implementation typically uses the {@link ResourceSet#getURIConverter URI converter} of the
   * {@link #getResourceSet containing} resource set to {@link URIConverter#delete(URI, Map)} the resource's
   * {@link #getURI() URI}.
   * </p>
   * 
   * @ADDED
   */
  public void delete(Map<?, ?> options) throws IOException;

} // CDOResourceNode
