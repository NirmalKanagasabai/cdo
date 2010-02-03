/**
 * Copyright (c) 2004 - 2010 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Taal - initial API and implementation
 *    Eike Stepper - maintenance
 */
package org.eclipse.emf.cdo.server.hibernate.internal.teneo;

import org.eclipse.emf.cdo.eresource.EresourcePackage;
import org.eclipse.emf.cdo.server.hibernate.internal.teneo.bundle.OM;
import org.eclipse.emf.cdo.server.hibernate.teneo.CDOHelper;
import org.eclipse.emf.cdo.server.internal.hibernate.CDOHibernateConstants;
import org.eclipse.emf.cdo.server.internal.hibernate.HibernateMappingProvider;
import org.eclipse.emf.cdo.server.internal.hibernate.HibernateStore;
import org.eclipse.emf.cdo.server.internal.hibernate.HibernateUtil;

import org.eclipse.net4j.util.om.trace.ContextTracer;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.teneo.PackageRegistryProvider;
import org.eclipse.emf.teneo.PersistenceOptions;
import org.eclipse.emf.teneo.extension.ExtensionManager;
import org.eclipse.emf.teneo.extension.ExtensionManagerFactory;

import java.util.List;
import java.util.Properties;

/**
 * Uses the ecore string in the ePackages of the store to generate a mapping.
 * 
 * @author Martin Taal
 * @author Eike Stepper
 */
public class TeneoHibernateMappingProvider extends HibernateMappingProvider
{
  private static final ContextTracer TRACER = new ContextTracer(OM.DEBUG, TeneoHibernateMappingProvider.class);

  private ExtensionManager extensionManager = ExtensionManagerFactory.getInstance().create();

  public TeneoHibernateMappingProvider()
  {
  }

  public ExtensionManager getExtensionManager()
  {
    return extensionManager;
  }

  @Override
  public HibernateStore getHibernateStore()
  {
    return (HibernateStore)super.getHibernateStore();
  }

  @SuppressWarnings("restriction")
  public void addMapping(org.hibernate.cfg.Configuration configuration)
  {
    final String mapping = generateMapping();
    if (TRACER.isEnabled())
    {
      TRACER.trace("Generated hibernate mapping:");
      TRACER.trace(mapping);
    }

    System.err.println(mapping);

    configuration.addXML(mapping);
    if (TRACER.isEnabled())
    {
      TRACER.trace("Added mapping to configuration");
    }
  }

  // the passed modelObjects collection is defined as a collection of Objects
  // to prevent binary dependency on emf.
  public String generateMapping()
  {
    if (TRACER.isEnabled())
    {
      TRACER.trace("Generating Hibernate Mapping");
    }

    final Properties properties = HibernateUtil.getInstance().getPropertiesFromStore(getHibernateStore());

    PackageRegistryProvider.getInstance().setThreadPackageRegistry(
        getHibernateStore().getRepository().getPackageRegistry());

    // translate the list of EPackages to an array
    final List<EPackage> epacks = getHibernateStore().getPackageHandler().getEPackages();
    // remove the ecore and resource package
    epacks.remove(EcorePackage.eINSTANCE);
    epacks.remove(EresourcePackage.eINSTANCE);

    addUniqueConstraintAnnotation();

    final EPackage[] ePackageArray = epacks.toArray(new EPackage[epacks.size()]);
    // remove the persistence xml if no epackages as this won't work without
    // epackages
    if (ePackageArray.length == 0 && properties.getProperty(PersistenceOptions.PERSISTENCE_XML) != null)
    {
      properties.remove(PersistenceOptions.PERSISTENCE_XML);
    }

    String hbm = CDOHelper.getInstance().generateMapping(ePackageArray, properties, extensionManager);
    // System.err.println(hbm);
    // to solve an issue with older versions of teneo
    hbm = hbm.replaceAll("_cont", "cont");
    return hbm;
  }

  // see the CDOEntityMapper, there an explicit unique-key is added to
  // a column also
  private void addUniqueConstraintAnnotation()
  {
    final EClass eClass = EresourcePackage.eINSTANCE.getCDOResourceNode();
    // already been here
    if (eClass.getEAnnotation("teneo.jpa") != null)
    {
      return;
    }

    final EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
    annotation.setSource("teneo.jpa");
    final String tableAnnotation = "@Table(uniqueConstraints={@UniqueConstraint(columnNames={\""
        + CDOHibernateConstants.CONTAINER_PROPERTY_COLUMN + "\", \""
        + EresourcePackage.eINSTANCE.getCDOResourceNode_Name().getName() + "\"})})";
    annotation.getDetails().put("value", tableAnnotation);
    eClass.getEAnnotations().add(annotation);
  }
}
