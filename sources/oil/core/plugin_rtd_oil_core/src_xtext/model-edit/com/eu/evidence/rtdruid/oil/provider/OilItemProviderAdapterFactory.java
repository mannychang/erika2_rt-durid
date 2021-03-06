/**
 */
package com.eu.evidence.rtdruid.oil.provider;

import com.eu.evidence.rtdruid.oil.util.OilAdapterFactory;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.edit.provider.ChangeNotifier;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IChangeNotifier;
import org.eclipse.emf.edit.provider.IDisposable;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.INotifyChangedListener;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;

/**
 * This is the factory that is used to provide the interfaces needed to support Viewers.
 * The adapters generated by this factory convert EMF adapter notifications into calls to {@link #fireNotifyChanged fireNotifyChanged}.
 * The adapters also support Eclipse property sheets.
 * Note that most of the adapters are shared among multiple instances.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class OilItemProviderAdapterFactory extends OilAdapterFactory implements ComposeableAdapterFactory, IChangeNotifier, IDisposable {
	/**
	 * This keeps track of the root adapter factory that delegates to this adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ComposedAdapterFactory parentAdapterFactory;

	/**
	 * This is used to implement {@link org.eclipse.emf.edit.provider.IChangeNotifier}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IChangeNotifier changeNotifier = new ChangeNotifier();

	/**
	 * This keeps track of all the supported types checked by {@link #isFactoryForType isFactoryForType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Collection<Object> supportedTypes = new ArrayList<Object>();

	/**
	 * This constructs an instance.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OilItemProviderAdapterFactory() {
		supportedTypes.add(IEditingDomainItemProvider.class);
		supportedTypes.add(IStructuredItemContentProvider.class);
		supportedTypes.add(ITreeItemContentProvider.class);
		supportedTypes.add(IItemLabelProvider.class);
		supportedTypes.add(IItemPropertySource.class);
	}

	/**
	 * This keeps track of the one adapter used for all {@link com.eu.evidence.rtdruid.oil.xtext.model.OilObject} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OilObjectItemProvider oilObjectItemProvider;

	/**
	 * This creates an adapter for a {@link com.eu.evidence.rtdruid.oil.xtext.model.OilObject}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createOilObjectAdapter() {
		if (oilObjectItemProvider == null) {
			oilObjectItemProvider = new OilObjectItemProvider(this);
		}

		return oilObjectItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link com.eu.evidence.rtdruid.oil.xtext.model.Parameter} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ParameterItemProvider parameterItemProvider;

	/**
	 * This creates an adapter for a {@link com.eu.evidence.rtdruid.oil.xtext.model.Parameter}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createParameterAdapter() {
		if (parameterItemProvider == null) {
			parameterItemProvider = new ParameterItemProvider(this);
		}

		return parameterItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link com.eu.evidence.rtdruid.oil.xtext.model.OilApplication} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OilApplicationItemProvider oilApplicationItemProvider;

	/**
	 * This creates an adapter for a {@link com.eu.evidence.rtdruid.oil.xtext.model.OilApplication}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createOilApplicationAdapter() {
		if (oilApplicationItemProvider == null) {
			oilApplicationItemProvider = new OilApplicationItemProvider(this);
		}

		return oilApplicationItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link com.eu.evidence.rtdruid.oil.xtext.model.OilImplementation} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OilImplementationItemProvider oilImplementationItemProvider;

	/**
	 * This creates an adapter for a {@link com.eu.evidence.rtdruid.oil.xtext.model.OilImplementation}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createOilImplementationAdapter() {
		if (oilImplementationItemProvider == null) {
			oilImplementationItemProvider = new OilImplementationItemProvider(this);
		}

		return oilImplementationItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link com.eu.evidence.rtdruid.oil.xtext.model.OilObjectImpl} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OilObjectImplItemProvider oilObjectImplItemProvider;

	/**
	 * This creates an adapter for a {@link com.eu.evidence.rtdruid.oil.xtext.model.OilObjectImpl}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createOilObjectImplAdapter() {
		if (oilObjectImplItemProvider == null) {
			oilObjectImplItemProvider = new OilObjectImplItemProvider(this);
		}

		return oilObjectImplItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link com.eu.evidence.rtdruid.oil.xtext.model.ValueType} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ValueTypeItemProvider valueTypeItemProvider;

	/**
	 * This creates an adapter for a {@link com.eu.evidence.rtdruid.oil.xtext.model.ValueType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createValueTypeAdapter() {
		if (valueTypeItemProvider == null) {
			valueTypeItemProvider = new ValueTypeItemProvider(this);
		}

		return valueTypeItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link com.eu.evidence.rtdruid.oil.xtext.model.EnumeratorType} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EnumeratorTypeItemProvider enumeratorTypeItemProvider;

	/**
	 * This creates an adapter for a {@link com.eu.evidence.rtdruid.oil.xtext.model.EnumeratorType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createEnumeratorTypeAdapter() {
		if (enumeratorTypeItemProvider == null) {
			enumeratorTypeItemProvider = new EnumeratorTypeItemProvider(this);
		}

		return enumeratorTypeItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link com.eu.evidence.rtdruid.oil.xtext.model.VariantType} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected VariantTypeItemProvider variantTypeItemProvider;

	/**
	 * This creates an adapter for a {@link com.eu.evidence.rtdruid.oil.xtext.model.VariantType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createVariantTypeAdapter() {
		if (variantTypeItemProvider == null) {
			variantTypeItemProvider = new VariantTypeItemProvider(this);
		}

		return variantTypeItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link com.eu.evidence.rtdruid.oil.xtext.model.OilFile} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OilFileItemProvider oilFileItemProvider;

	/**
	 * This creates an adapter for a {@link com.eu.evidence.rtdruid.oil.xtext.model.OilFile}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createOilFileAdapter() {
		if (oilFileItemProvider == null) {
			oilFileItemProvider = new OilFileItemProvider(this);
		}

		return oilFileItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link com.eu.evidence.rtdruid.oil.xtext.model.ReferenceType} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ReferenceTypeItemProvider referenceTypeItemProvider;

	/**
	 * This creates an adapter for a {@link com.eu.evidence.rtdruid.oil.xtext.model.ReferenceType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createReferenceTypeAdapter() {
		if (referenceTypeItemProvider == null) {
			referenceTypeItemProvider = new ReferenceTypeItemProvider(this);
		}

		return referenceTypeItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link com.eu.evidence.rtdruid.oil.xtext.model.Range} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RangeItemProvider rangeItemProvider;

	/**
	 * This creates an adapter for a {@link com.eu.evidence.rtdruid.oil.xtext.model.Range}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createRangeAdapter() {
		if (rangeItemProvider == null) {
			rangeItemProvider = new RangeItemProvider(this);
		}

		return rangeItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link com.eu.evidence.rtdruid.oil.xtext.model.ValueList} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ValueListItemProvider valueListItemProvider;

	/**
	 * This creates an adapter for a {@link com.eu.evidence.rtdruid.oil.xtext.model.ValueList}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createValueListAdapter() {
		if (valueListItemProvider == null) {
			valueListItemProvider = new ValueListItemProvider(this);
		}

		return valueListItemProvider;
	}

	/**
	 * This returns the root adapter factory that contains this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ComposeableAdapterFactory getRootAdapterFactory() {
		return parentAdapterFactory == null ? this : parentAdapterFactory.getRootAdapterFactory();
	}

	/**
	 * This sets the composed adapter factory that contains this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParentAdapterFactory(ComposedAdapterFactory parentAdapterFactory) {
		this.parentAdapterFactory = parentAdapterFactory;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object type) {
		return supportedTypes.contains(type) || super.isFactoryForType(type);
	}

	/**
	 * This implementation substitutes the factory itself as the key for the adapter.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter adapt(Notifier notifier, Object type) {
		return super.adapt(notifier, this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object adapt(Object object, Object type) {
		if (isFactoryForType(type)) {
			Object adapter = super.adapt(object, type);
			if (!(type instanceof Class<?>) || (((Class<?>)type).isInstance(adapter))) {
				return adapter;
			}
		}

		return null;
	}

	/**
	 * This adds a listener.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void addListener(INotifyChangedListener notifyChangedListener) {
		changeNotifier.addListener(notifyChangedListener);
	}

	/**
	 * This removes a listener.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void removeListener(INotifyChangedListener notifyChangedListener) {
		changeNotifier.removeListener(notifyChangedListener);
	}

	/**
	 * This delegates to {@link #changeNotifier} and to {@link #parentAdapterFactory}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void fireNotifyChanged(Notification notification) {
		changeNotifier.fireNotifyChanged(notification);

		if (parentAdapterFactory != null) {
			parentAdapterFactory.fireNotifyChanged(notification);
		}
	}

	/**
	 * This disposes all of the item providers created by this factory. 
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void dispose() {
		if (oilObjectItemProvider != null) oilObjectItemProvider.dispose();
		if (parameterItemProvider != null) parameterItemProvider.dispose();
		if (oilApplicationItemProvider != null) oilApplicationItemProvider.dispose();
		if (oilImplementationItemProvider != null) oilImplementationItemProvider.dispose();
		if (oilObjectImplItemProvider != null) oilObjectImplItemProvider.dispose();
		if (valueTypeItemProvider != null) valueTypeItemProvider.dispose();
		if (enumeratorTypeItemProvider != null) enumeratorTypeItemProvider.dispose();
		if (variantTypeItemProvider != null) variantTypeItemProvider.dispose();
		if (oilFileItemProvider != null) oilFileItemProvider.dispose();
		if (referenceTypeItemProvider != null) referenceTypeItemProvider.dispose();
		if (rangeItemProvider != null) rangeItemProvider.dispose();
		if (valueListItemProvider != null) valueListItemProvider.dispose();
	}

}
