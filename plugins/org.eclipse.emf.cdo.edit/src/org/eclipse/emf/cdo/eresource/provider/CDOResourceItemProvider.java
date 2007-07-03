/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.emf.cdo.eresource.provider;

import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.eresource.EresourcePackage;
import org.eclipse.emf.cdo.protocol.CDOProtocolConstants;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;

import java.util.Collection;
import java.util.List;

/**
 * This is the item provider adapter for a
 * {@link org.eclipse.emf.cdo.eresource.CDOResource} object. <!-- begin-user-doc
 * --> <!-- end-user-doc -->
 * 
 * @generated
 */
public class CDOResourceItemProvider extends ItemProviderAdapter implements IEditingDomainItemProvider,
    IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource
{
  /**
   * This constructs an instance from a factory and a notifier. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  public CDOResourceItemProvider(AdapterFactory adapterFactory)
  {
    super(adapterFactory);
  }

  /**
   * This returns the property descriptors for the adapted class. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  @Override
  public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object)
  {
    if (itemPropertyDescriptors == null)
    {
      super.getPropertyDescriptors(object);

      addResourceSetPropertyDescriptor(object);
      addURIPropertyDescriptor(object);
      addModifiedPropertyDescriptor(object);
      addLoadedPropertyDescriptor(object);
      addTrackingModificationPropertyDescriptor(object);
      addErrorsPropertyDescriptor(object);
      addWarningsPropertyDescriptor(object);
      addPathPropertyDescriptor(object);
    }
    return itemPropertyDescriptors;
  }

  /**
   * This adds a property descriptor for the Resource Set feature. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  protected void addResourceSetPropertyDescriptor(Object object)
  {
    itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory)adapterFactory)
        .getRootAdapterFactory(), getResourceLocator(), getString("_UI_CDOResource_resourceSet_feature"), getString(
        "_UI_PropertyDescriptor_description", "_UI_CDOResource_resourceSet_feature", "_UI_CDOResource_type"),
        EresourcePackage.Literals.CDO_RESOURCE__RESOURCE_SET, true, false, false,
        ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
  }

  /**
   * This adds a property descriptor for the URI feature. <!-- begin-user-doc
   * --> <!-- end-user-doc -->
   * 
   * @generated
   */
  protected void addURIPropertyDescriptor(Object object)
  {
    itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory)adapterFactory)
        .getRootAdapterFactory(), getResourceLocator(), getString("_UI_CDOResource_uRI_feature"), getString(
        "_UI_PropertyDescriptor_description", "_UI_CDOResource_uRI_feature", "_UI_CDOResource_type"),
        EresourcePackage.Literals.CDO_RESOURCE__URI, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
        null, null));
  }

  /**
   * This adds a property descriptor for the Modified feature. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  protected void addModifiedPropertyDescriptor(Object object)
  {
    itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory)adapterFactory)
        .getRootAdapterFactory(), getResourceLocator(), getString("_UI_CDOResource_modified_feature"), getString(
        "_UI_PropertyDescriptor_description", "_UI_CDOResource_modified_feature", "_UI_CDOResource_type"),
        EresourcePackage.Literals.CDO_RESOURCE__MODIFIED, true, false, false,
        ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE, null, null));
  }

  /**
   * This adds a property descriptor for the Loaded feature. <!-- begin-user-doc
   * --> <!-- end-user-doc -->
   * 
   * @generated
   */
  protected void addLoadedPropertyDescriptor(Object object)
  {
    itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory)adapterFactory)
        .getRootAdapterFactory(), getResourceLocator(), getString("_UI_CDOResource_loaded_feature"), getString(
        "_UI_PropertyDescriptor_description", "_UI_CDOResource_loaded_feature", "_UI_CDOResource_type"),
        EresourcePackage.Literals.CDO_RESOURCE__LOADED, true, false, false, ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
        null, null));
  }

  /**
   * This adds a property descriptor for the Tracking Modification feature. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  protected void addTrackingModificationPropertyDescriptor(Object object)
  {
    itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory)adapterFactory)
        .getRootAdapterFactory(), getResourceLocator(), getString("_UI_CDOResource_trackingModification_feature"),
        getString("_UI_PropertyDescriptor_description", "_UI_CDOResource_trackingModification_feature",
            "_UI_CDOResource_type"), EresourcePackage.Literals.CDO_RESOURCE__TRACKING_MODIFICATION, true, false, false,
        ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE, null, null));
  }

  /**
   * This adds a property descriptor for the Errors feature. <!-- begin-user-doc
   * --> <!-- end-user-doc -->
   * 
   * @generated
   */
  protected void addErrorsPropertyDescriptor(Object object)
  {
    itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory)adapterFactory)
        .getRootAdapterFactory(), getResourceLocator(), getString("_UI_CDOResource_errors_feature"), getString(
        "_UI_PropertyDescriptor_description", "_UI_CDOResource_errors_feature", "_UI_CDOResource_type"),
        EresourcePackage.Literals.CDO_RESOURCE__ERRORS, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
        null, null));
  }

  /**
   * This adds a property descriptor for the Warnings feature. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  protected void addWarningsPropertyDescriptor(Object object)
  {
    itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory)adapterFactory)
        .getRootAdapterFactory(), getResourceLocator(), getString("_UI_CDOResource_warnings_feature"), getString(
        "_UI_PropertyDescriptor_description", "_UI_CDOResource_warnings_feature", "_UI_CDOResource_type"),
        EresourcePackage.Literals.CDO_RESOURCE__WARNINGS, true, false, false,
        ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
  }

  /**
   * This adds a property descriptor for the Path feature. <!-- begin-user-doc
   * --> <!-- end-user-doc -->
   * 
   * @generated
   */
  protected void addPathPropertyDescriptor(Object object)
  {
    itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory)adapterFactory)
        .getRootAdapterFactory(), getResourceLocator(), getString("_UI_CDOResource_path_feature"), getString(
        "_UI_PropertyDescriptor_description", "_UI_CDOResource_path_feature", "_UI_CDOResource_type"),
        EresourcePackage.Literals.CDO_RESOURCE__PATH, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
        null, null));
  }

  /**
   * This specifies how to implement {@link #getChildren} and is used to deduce
   * an appropriate feature for an
   * {@link org.eclipse.emf.edit.command.AddCommand},
   * {@link org.eclipse.emf.edit.command.RemoveCommand} or
   * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  @Override
  public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object)
  {
    if (childrenFeatures == null)
    {
      super.getChildrenFeatures(object);
      childrenFeatures.add(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS);
    }
    return childrenFeatures;
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  @Override
  protected EStructuralFeature getChildFeature(Object object, Object child)
  {
    // Check the type of the specified child object and return the proper
    // feature to use for
    // adding (see {@link AddCommand}) it as a child.

    return super.getChildFeature(object, child);
  }

  /**
   * This returns CDOResource.gif. <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  @Override
  public Object getImage(Object object)
  {
    return overlayImage(object, getResourceLocator().getImage("full/obj16/CDOResource"));
  }

  /**
   * This returns the label text for the adapted class. <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * 
   * @generated NOT
   */
  @Override
  public String getText(Object object)
  {
    CDOResource resource = (CDOResource)object;
    return CDOProtocolConstants.PROTOCOL_NAME + ":" + resource.getPath();
  }

  /**
   * This handles model notifications by calling {@link #updateChildren} to
   * update any cached children and by creating a viewer notification, which it
   * passes to {@link #fireNotifyChanged}. <!-- begin-user-doc --> <!--
   * end-user-doc -->
   * 
   * @generated
   */
  @Override
  public void notifyChanged(Notification notification)
  {
    updateChildren(notification);

    switch (notification.getFeatureID(CDOResource.class))
    {
    case EresourcePackage.CDO_RESOURCE__RESOURCE_SET:
    case EresourcePackage.CDO_RESOURCE__URI:
    case EresourcePackage.CDO_RESOURCE__MODIFIED:
    case EresourcePackage.CDO_RESOURCE__LOADED:
    case EresourcePackage.CDO_RESOURCE__TRACKING_MODIFICATION:
    case EresourcePackage.CDO_RESOURCE__ERRORS:
    case EresourcePackage.CDO_RESOURCE__WARNINGS:
    case EresourcePackage.CDO_RESOURCE__PATH:
      fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
      return;
    case EresourcePackage.CDO_RESOURCE__CONTENTS:
      fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
      return;
    }
    super.notifyChanged(notification);
  }

  /**
   * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s
   * describing the children that can be created under this object. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  @Override
  protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object)
  {
    super.collectNewChildDescriptors(newChildDescriptors, object);

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createEObject()));

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createEAttribute()));

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createEAnnotation()));

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createEClass()));

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createEDataType()));

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createEEnum()));

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createEEnumLiteral()));

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createEFactory()));

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createEOperation()));

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createEPackage()));

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createEParameter()));

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createEReference()));

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createEGenericType()));

    newChildDescriptors.add(createChildParameter(EresourcePackage.Literals.CDO_RESOURCE__CONTENTS,
        EcoreFactory.eINSTANCE.createETypeParameter()));
  }

  /**
   * Return the resource locator for this item provider's resources. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  @Override
  public ResourceLocator getResourceLocator()
  {
    return EresourceEditPlugin.INSTANCE;
  }

}
