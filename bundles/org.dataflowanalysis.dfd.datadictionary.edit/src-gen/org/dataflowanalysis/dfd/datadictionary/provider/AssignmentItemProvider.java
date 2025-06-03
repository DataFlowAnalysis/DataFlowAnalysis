/**
 */
package org.dataflowanalysis.dfd.datadictionary.provider;

import java.util.Collection;
import java.util.List;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryPackage;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ViewerNotification;

/**
 * This is the item provider adapter for a {@link org.dataflowanalysis.dfd.datadictionary.Assignment} object. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * @generated
 */
public class AssignmentItemProvider extends AbstractAssignmentItemProvider {
    /**
     * This constructs an instance from a factory and a notifier. <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public AssignmentItemProvider(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    /**
     * This returns the property descriptors for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
        if (itemPropertyDescriptors == null) {
            super.getPropertyDescriptors(object);

            addOutputLabelsPropertyDescriptor(object);
            addInputPinsPropertyDescriptor(object);
        }
        return itemPropertyDescriptors;
    }

    /**
     * This adds a property descriptor for the Output Labels feature. <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    protected void addOutputLabelsPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
                getResourceLocator(), getString("_UI_Assignment_outputLabels_feature"),
                getString("_UI_PropertyDescriptor_description", "_UI_Assignment_outputLabels_feature", "_UI_Assignment_type"),
                datadictionaryPackage.Literals.ASSIGNMENT__OUTPUT_LABELS, true, false, true, null, null, null));
    }

    /**
     * This adds a property descriptor for the Input Pins feature. <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    protected void addInputPinsPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
                getResourceLocator(), getString("_UI_Assignment_inputPins_feature"),
                getString("_UI_PropertyDescriptor_description", "_UI_Assignment_inputPins_feature", "_UI_Assignment_type"),
                datadictionaryPackage.Literals.ASSIGNMENT__INPUT_PINS, true, false, true, null, null, null));
    }

    /**
     * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
     * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
     * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * @generated
     */
    @Override
    public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
        if (childrenFeatures == null) {
            super.getChildrenFeatures(object);
            childrenFeatures.add(datadictionaryPackage.Literals.ASSIGNMENT__TERM);
        }
        return childrenFeatures;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EStructuralFeature getChildFeature(Object object, Object child) {
        // Check the type of the specified child object and return the proper feature to use for
        // adding (see {@link AddCommand}) it as a child.

        return super.getChildFeature(object, child);
    }

    /**
     * This returns Assignment.gif. <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object getImage(Object object) {
        return overlayImage(object, getResourceLocator().getImage("full/obj16/Assignment"));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected boolean shouldComposeCreationImage() {
        return true;
    }

    /**
     * This returns the label text for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String getText(Object object) {
        String label = ((Assignment) object).getEntityName();
        return label == null || label.length() == 0 ? getString("_UI_Assignment_type") : getString("_UI_Assignment_type") + " " + label;
    }

    /**
     * This handles model notifications by calling {@link #updateChildren} to update any cached children and by creating a
     * viewer notification, which it passes to {@link #fireNotifyChanged}. <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void notifyChanged(Notification notification) {
        updateChildren(notification);

        switch (notification.getFeatureID(Assignment.class)) {
            case datadictionaryPackage.ASSIGNMENT__TERM:
                fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
                return;
        }
        super.notifyChanged(notification);
    }

    /**
     * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children that can be created under
     * this object. <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
        super.collectNewChildDescriptors(newChildDescriptors, object);

        newChildDescriptors.add(createChildParameter(datadictionaryPackage.Literals.ASSIGNMENT__TERM, datadictionaryFactory.eINSTANCE.createTRUE()));

        newChildDescriptors.add(createChildParameter(datadictionaryPackage.Literals.ASSIGNMENT__TERM, datadictionaryFactory.eINSTANCE.createAND()));

        newChildDescriptors.add(createChildParameter(datadictionaryPackage.Literals.ASSIGNMENT__TERM, datadictionaryFactory.eINSTANCE.createOR()));

        newChildDescriptors.add(createChildParameter(datadictionaryPackage.Literals.ASSIGNMENT__TERM, datadictionaryFactory.eINSTANCE.createNOT()));

        newChildDescriptors
                .add(createChildParameter(datadictionaryPackage.Literals.ASSIGNMENT__TERM, datadictionaryFactory.eINSTANCE.createLabelReference()));
    }

}
