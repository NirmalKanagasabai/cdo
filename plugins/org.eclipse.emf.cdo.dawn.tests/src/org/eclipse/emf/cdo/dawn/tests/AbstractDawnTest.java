/**
 * Copyright (c) 2004 - 2010 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Fluegge - initial API and implementation
 */
package org.eclipse.emf.cdo.dawn.tests;

import org.eclipse.emf.cdo.dawn.resources.impl.DawnResourceFactoryImpl;
import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.session.CDOSession;
import org.eclipse.emf.cdo.tests.AbstractCDOTest;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;

import java.net.URL;

/**
 * @author Martin Fluegge
 */
public class AbstractDawnTest extends AbstractCDOTest
{

  @Override
  public void setUp() throws Exception
  {
    super.setUp();
  }

  protected ResourceSet createResourceSet()
  {
    ResourceSet dawnResourceSet = new ResourceSetImpl();
    dawnResourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("dawn", new DawnResourceFactoryImpl());
    return dawnResourceSet;
  }

  protected void createCDOResourcesFromXMI(String resourceName, EPackage ePackage, CDOSession session)
      throws CommitException
  {

    NotationPackage.eINSTANCE.getClass();
    String packageName = ePackage.getName();
    ResourceSet resourceSet = new ResourceSetImpl();
    resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
        .put(packageName + "_diagram", new XMIResourceFactoryImpl());

    resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(packageName, new XMIResourceFactoryImpl());

    System.out.println(GMFTest.class.getResource("."));
    System.out.println(GMFTest.class.getResource("/"));

    URL resourceURI = GMFTest.class.getResource("");
    String resourcePath = resourceURI.toString().substring(0, resourceURI.toString().lastIndexOf("/bin"));

    System.out.println(resourcePath);

    Resource emfResource = resourceSet.getResource(
        URI.createURI(resourcePath + "/testdata/" + resourceName + "." + packageName), true);
    Resource gmfResource = resourceSet.getResource(
        URI.createURI(resourcePath + "/testdata/" + resourceName + "." + packageName + "_diagram"), true);

    Diagram notationalRoot = (Diagram)gmfResource.getContents().get(0);
    EObject semanticRoot = notationalRoot.getElement();

    for (Object o : notationalRoot.getPersistedChildren())
    {
      View view = (View)o;
      view.getElement();
    }

    ResourceSet dawnResourceSet = new ResourceSetImpl();
    dawnResourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("dawn", new DawnResourceFactoryImpl());

    CDOTransaction transaction = session.openTransaction(dawnResourceSet);

    CDOResource semanticResource = transaction.createResource("/" + resourceName + "." + packageName);

    URI createURI = URI.createURI("dawn://repo1/" + resourceName + "." + packageName + "_diagram");
    Resource notationalResource = dawnResourceSet.createResource(createURI);

    notationalResource.getContents().add(notationalRoot);
    semanticResource.getContents().add(emfResource.getContents().get(0));
    transaction.commit();
    transaction.close();
  }

}
