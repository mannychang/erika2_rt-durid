package com.eu.evidence.rtdruid.vartree;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;

import com.eu.evidence.rtdruid.epackage.EPackageFactory;
import com.eu.evidence.rtdruid.epackage.RTDEPackageBuildException;
import com.eu.evidence.rtdruid.internal.vartree.VarTree;
import com.eu.evidence.rtdruid.io.RTD_XMI_Factory;
import com.eu.evidence.rtdruid.vartree.data.DataFactory;
import com.eu.evidence.rtdruid.vartree.data.DataPackage;

public final class VarTreeUtil {

	/**
	 * A small class to customize the IVarTree creation
	 * 
	 * @author Nicola Serreli
	 * @since 2.0
	 * 
	 */
	public final static class VarTreeCreator {

		private CommandStack commandStack;
		private EPackage epkg;
		private AdapterFactory adapterFactory;

		public VarTreeCreator() {
			commandStack = null;
			epkg = null;
			adapterFactory = null;
		}

		/**
		 * Creates a new IVarTree instance using provided attributes. Multiple
		 * call of this methods returns different instances of IVarTree based on
		 * the same set of value (i.e., if set by the user, the CommandStack is shared
		 * among all instances produced)
		 * 
		 * @param ed
		 *            the editing domain where store the connection to the
		 *            EPackage
		 * @param epkg
		 *            the EPackage to add
		 * @return this VarTreeCreator
		 */
		public IVarTree istantiateVarTree() {
			CommandStack localCommandStack = this.commandStack;
			final IVarTree answer;
			if (localCommandStack == null && adapterFactory == null && epkg == null) {
				answer = new VarTree();
			} else {
				if (localCommandStack == null) {
					localCommandStack = new BasicCommandStack();
				}
				if (epkg != null && adapterFactory == null) {
					adapterFactory = new ComposedAdapterFactory(new AdapterFactory[] {
							new ResourceItemProviderAdapterFactory(), new ReflectiveItemProviderAdapterFactory() });
				}

				if (adapterFactory == null) {
					answer = new VarTree(localCommandStack);
				} else {
					answer = new VarTree(localCommandStack, adapterFactory);

					addEPackage(answer, epkg);
				}
			}
			if (answer.getResourceSet().getResources().size() == 0) {
				answer.getResourceSet().getResources().add((new RTD_XMI_Factory()).createResource());
			}

			return answer;
		}

		/**
		 * Add to EditingDomain the knowledge of a specific EPackage
		 * 
		 * @param ed
		 *            the editing domain where store the connection to the
		 *            EPackage
		 * @param epkg
		 *            the EPackage to add. If it is null or it has not an NsUri, the editing domain remain unchanged
		 *            
		 * @return this VarTreeCreator
		 */
		public static EditingDomain addEPackage(EditingDomain ed, EPackage epkg) {
			if (epkg != null) {
				String ns = epkg.getNsURI();
				if (ns != null) {
					ed.getResourceSet().getPackageRegistry().put(ns, epkg);
				}
			}
			return ed;
		}

		/**
		 * @param commandStack
		 *            the commandStack to set
		 * @return this VarTreeCreator
		 */
		public VarTreeCreator setCommandStack(CommandStack commandStack) {
			this.commandStack = commandStack;
			return this;
		}

		/**
		 * @param adapterFactory
		 *            the adapterFactory to set
		 * @return this VarTreeCreator
		 */
		public VarTreeCreator setAdapterFactory(AdapterFactory adapterFactory) {
			this.adapterFactory = adapterFactory;
			return this;
		}

		/**
		 * @param epkg
		 *            the EPackage to set
		 * @return this VarTreeCreator
		 */
		public VarTreeCreator setEpkg(EPackage epkg) {
			this.epkg = epkg;
			return this;
		}
	}

	private static final boolean USE_DYNAMIC_PACKAGES = false;

	/** disable constructor */
	private VarTreeUtil() {
	}

	/**
	 * The correct way to build a new VarTree
	 */
	public static IVarTree newVarTree() {
		return (new VarTreeCreator()).setEpkg(getDefaultPackage()).istantiateVarTree();
	}

	/**
	 * The correct way to build a new VarTree
	 */
	public static IVarTree newVarTree(CommandStack commandStack) {
		return (new VarTreeCreator()).setEpkg(getDefaultPackage()).setCommandStack(commandStack).istantiateVarTree();
	}

	/**
	 * 
	 */
	public static IVarTree newDynamicVarTree() {

		return (new VarTreeCreator()).istantiateVarTree();
	}

	/**
	 * Try to instantiate the root element
	 */
	public static EObject newVarTreeRoot(EditingDomain vt) {
		return newVarTreeRoot( vt.getResourceSet().getPackageRegistry().getEPackage(DataPackage.eNS_URI) );
	}

	/**
	 * Try to instantiate the root element
	 */
	public static EObject newVarTreeRoot(Registry reg) {
		return newVarTreeRoot( reg.getEPackage(DataPackage.eNS_URI) );
	}

	/**
	 * Try to instantiate the root element
	 */
	public static EObject newVarTreeRoot(EPackage epkg) {
		if (USE_DYNAMIC_PACKAGES) {
			if (epkg != null) {
				EClassifier rootClassifier = epkg.getEClassifier(DataPackage.Literals.SYSTEM.getName());
				if (rootClassifier != null) {
					return EcoreUtil.create(rootClassifier.eClass());
				}
			}
		}
		return DataFactory.eINSTANCE.createSystem();
	}
	
	private static EPackage getDefaultPackage() {
		final EPackage answer;
		if (USE_DYNAMIC_PACKAGES) {
			try {
				answer = EPackageFactory.instance.getEPackage();
			} catch (RTDEPackageBuildException e) {
				throw new RuntimeException(e);
			}
		} else {
			answer = DataPackage.eINSTANCE;
		}
		return answer;
	}

	/**
	 * Make a distinct copy of current node and all its attributes and
	 * references
	 * */
	public static EObject copy(EObject original) {
		if (original == null) {
			return null;
		}

		return EcoreUtil.copy(original);
	}

	/** Makes a new Instance of this object */
	public static EObject newInstance(EObject obj) {
		return newInstance(obj.eClass());
	}

	/** Makes a new Instance of this object */
	public static EObject newInstance(EClass objClass) {
		return objClass.getEPackage().getEFactoryInstance().create(objClass);
	}

	/**
	 * Compare two trees
	 * 
	 * @param first
	 * @param second
	 * @return an IStatus.OK if the two trees are similar, otherwise returns an
	 *         IStatus.ERROR
	 */
	public static IStatus compare(IVarTree first, IVarTree second) {
		return (new VtCompare(first, second)).checkTrees();
	}

	/**
	 * Compare two subtrees
	 * 
	 * @param first
	 * @param second
	 * @return an IStatus.OK if the two trees are similar, otherwise returns an
	 *         IStatus.ERROR
	 */
	public static IStatus compare(EObject first, EObject second) {
		return (new VtCompare(first, second)).checkTrees();
	}

	/**
	 * Try to merge current node with another Object of the same type (and all
	 * its attributes and references). The result is stored in current node.
	 * 
	 * Note that this function is not intended to work with EPackages.
	 * 
	 * @throws IllegalArgumentException
	 *             if two object are not compatible (or one of theirs
	 *             descendants)
	 * */
	public static EObject merge(EObject destination, EObject addition) {
		merge(destination, addition, "", false);
		return destination;
	}

	/**
	 * Try to merge current node with another Object of the same type (and all
	 * its attributes and references). The result is stored in current node.
	 * 
	 * Note that this function is not intended to work with EPackages.
	 * 
	 * @throws IllegalArgumentException
	 *             if two object are not compatible (or one of theirs
	 *             descendants)
	 * */
	public static void merge(EObject destination, EObject addition, String path, boolean overwrite) {
		if (destination.getClass() != addition.getClass()) {
			throw new IllegalArgumentException("Try to merge two not compatible objects: one " + destination.getClass()
					+ " and one " + addition.getClass() + "\n\tpath = " + path);
		}
		{
			String curID = VarTreeIdHandler.getId(destination);
			String sourceID = VarTreeIdHandler.getId(addition);
			if (curID == null ? sourceID != null : !curID.equals(sourceID)) {
				throw new IllegalArgumentException("Try to merge two objects with differents ID: " + curID + " and "
						+ sourceID + "\n\tpath = " + path);
			}

			path = (path == null ? "" : path) + DataPath.SEPARATOR + curID;
		}

		// copy all attributes
		EList<EAttribute> attrs = destination.eClass().getEAllAttributes();
		for (int i = 0; i < attrs.size(); i++) {
			EAttribute attr = (EAttribute) attrs.get(i);
			if (attr.isDerived()) {
				// do nothing
			} else if (attr.isMany()) {
				@SuppressWarnings("unchecked")
				EList<Object> children = (EList<Object>) destination.eGet(attr);
				EList<?> newChildren = (EList<?>) addition.eGet(attr);

				LinkedList<?> tmpChildren = new LinkedList<Object>(children);
				for (int j = 0; j < newChildren.size(); j++) {

					Object var = newChildren.get(j);
					if (var != null) {
						if (!searchVar(tmpChildren, var)) {
							if (var instanceof IVariable) {
								children.add(((IVariable) var).clone());
							} else {
								children.add(EcoreUtil.createFromString(attr.getEAttributeType(), var.toString()));
							}
						}
					}
				}

			} else {
				Object var = destination.eGet(attr);
				Object newVar = addition.eGet(attr);
				if (!compare(var, newVar)) {

					if (overwrite) {
						destination.eSet(attr, newVar);
					} else {

						// check if it was the default value
						Object def = attr.getDefaultValue();
						if (compare(var, def)) {
							destination.eSet(attr, newVar);
						} else if (!compare(newVar, def)) {
							throw new IllegalArgumentException(
									"Try to change the vaule of an already set attribute (mono value). " + "path = "
											+ path + DataPath.SEPARATOR + attr.getName());
						}
					}
				}
			}
		}

		// copy all references
		EList<EReference> refs = destination.eClass().getEReferences();
		for (int i = 0; i < refs.size(); i++) {
			EReference ref = (EReference) refs.get(i);
			if (ref.isMany()) {
				@SuppressWarnings("unchecked")
				EList<EObject> children = (EList<EObject>) destination.eGet(ref);
				@SuppressWarnings("unchecked")
				EList<EObject> newChildren = (EList<EObject>) addition.eGet(ref);

				for (int j = 0; j < newChildren.size(); j++) {

					EObject newChild = newChildren.get(j);
					if (newChild != null) {
						int pos = children.indexOf(newChild);
						if (pos < 0) {
							EObject child = newInstance(newChild.eClass());
							VarTreeIdHandler.setId(child, VarTreeIdHandler.getId(newChild));

							children.add(child);

							// check the new child.
							// Note: set overwrite to true, because the new
							// child may have default values
							merge(child, newChild, path, true);

						} else {
							// check the old child
							merge(children.get(pos), newChild, path, overwrite);
						}
					}
				}

			} else {

				EObject child = (EObject) destination.eGet(ref);
				EObject newChild = (EObject) addition.eGet(ref);
				if (newChild != null) {
					if (child == null) {
						child = newInstance(newChild.eClass());
						VarTreeIdHandler.setId(child, VarTreeIdHandler.getId(newChild));

						destination.eSet(ref, child);
						// Note: set overwrite to true, because the new
						// child may have default values
						merge(child, newChild, path, true);
					} else {
						merge(child, newChild, path, overwrite);
					}
				}
			}
		}
	}

	private static boolean searchVar(LinkedList<?> vars, Object value) {
		boolean risp = false;

		Iterator<?> iter = vars.iterator();
		while (!risp && iter.hasNext()) {
			Object v = iter.next();
			if (compare(v, value)) {
				iter.remove();
			}
		}

		return risp;
	}

	private static boolean compare(Object o1, Object o2) {
		boolean answer = false;
		if (o1 == null && o2 == null) {
			answer = true;
		} else if (o1 != null && o2 != null) {

			if (o1.getClass() == o2.getClass()) {
				final Object v1;
				final Object v2;
				if (o1 instanceof IVariable) {
					v1 = ((IVariable) o1).get();
					v2 = ((IVariable) o2).get();
				} else {
					v1 = o1;
					v2 = o2;
				}

				answer = (v1 == null ? v2 == null : (v2 != null && v1.toString().equals(v2.toString())));
			}
		}
		return answer;
	}
}