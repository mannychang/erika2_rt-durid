/**
 * Created on 31/ott/2008
 *
 * $Id$
 */
package com.eu.evidence.rtdruid.internal.modules.oil.codewriter.erikaenterprise;

import com.eu.evidence.rtdruid.internal.modules.oil.keywords.IWritersKeywords;
import com.eu.evidence.rtdruid.modules.oil.codewriter.erikaenterprise.hw.CpuHwDescription;

/**
 * The description of a MPC5674F
 * 
 * @author Nicola Serreli
 */
public class CpuDescrPpcE200Z4 extends CpuHwDescription {

	/**
	 * 
	 */
	public CpuDescrPpcE200Z4() {
		super(IWritersKeywords.CPU_PPCE200Z4, "PRIVATE",
				new String[] { "SYS_SIZE" }, new String[] {}, 16, 4, 4, 32, true);
		handlesEmpyVector = false;
		supportOrtiISR2 = true;
		
		macros = new MacrosForSharedData();
		

		/*
		 * (16 interrupts priorities and 3 timers)
		 */
		maxNestedInts = 19;
	}
	
	@Override
	public String initEmptyVector() {
		return "{0U}";
	}
}