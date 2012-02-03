package com.eu.evidence.rtdruid.internal.modules.oil.implementation;

import java.util.HashSet;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.eu.evidence.rtdruid.desk.RTDFactory;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.OilImplFactory;
import com.eu.evidence.rtdruid.modules.oil.interfaces.IOilImplID;
import com.eu.evidence.rtdruid.modules.oil.interfaces.IOilImplementation;
import com.eu.evidence.rtdruid.vartree.IVarTree;


public class OilImplCollectorTest extends TestCase {

	public void testImplementationsNumber() {
		System.out.println(">>> TEST IMPLEMENTATION NUMBER");
		IOilImplementation[] impls = OilImplCollector.getAllOilImplementation();
		System.out.println("found implementations are : " + impls.length);
		
		IConfigurationElement[] config = Platform.getExtensionRegistry()
		.getConfigurationElementsFor("com.eu.evidence.rtdruid.oil.core.oil_implementation");

		assertTrue(config.length >= impls.length);
		assertTrue(config.length == impls.length);
	}
	
	
	public void testGetAllImplementations() {
		System.out.println(">>> TEST GET IMPLEMENTATION");
		IOilImplementation[] impls = OilImplCollector.getAllOilImplementation();
		System.out.println("found implementations are : " + impls.length);

		for (IOilImplementation impl : impls) {
			System.out.println();
			System.out.println(impl.getId());
			System.out.println(impl.toOil());
		}
	}
	
	public void testMergeImplementations() {
		System.out.println(">>> TEST MERGE IMPLEMENTATION");
		IOilImplementation[] impls = OilImplCollector.getAllOilImplementation();

		IVarTree vt = RTDFactory.newVarTree();
		OilImplFactory oif = OilImplFactory.getAnInstance(vt);
		
		HashSet<IOilImplID> ids = new HashSet<IOilImplID>();
		for (IOilImplementation impl : impls) {
			ids.add(impl.getId());
			oif.merge(impl);
		}
		
		System.out.println("Founded id: " + ids.size());
		
		for (IOilImplID id : ids) {
			IOilImplementation impl = oif.getImpl(id);

			System.out.println();
			System.out.println(impl.getId());
			System.out.println(impl.toOil());
			
		}
	}
}
