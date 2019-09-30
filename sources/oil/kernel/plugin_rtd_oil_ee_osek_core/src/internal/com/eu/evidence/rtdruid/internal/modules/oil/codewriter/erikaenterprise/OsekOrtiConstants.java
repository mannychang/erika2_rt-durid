package com.eu.evidence.rtdruid.internal.modules.oil.codewriter.erikaenterprise;

class OsekOrtiConstants {

	
	/***************************************************************************
	 * ORTI options
	 **************************************************************************/
	
	public final static int EE_ORTI_OS = 1;
	public final static int EE_ORTI_TASK = 1<<2;
	public final static int EE_ORTI_RESOURCE = 1<<3;
	public final static int EE_ORTI_STACK = 1<<4;
	public final static int EE_ORTI_ALARM = 1<<5;
	public final static int EE_ORTI_ISR2 = 1<<6;
	public final static int EE_ORTI_ALL = 0xFF;

	
	/***************************************************************************
	 * ORTI KeyWord
	 **************************************************************************/
	
	/**
	 * This keyword is used to store required ORTI sections for a specific cpu.
	 * The value is an Integer that stores a bit mask.
	 */
	public final static String OS_CPU_ORTI_ENABLED_SECTIONS = "_cpu_type_specifics_enabled_orti_sections_";
	
	/**
	 * This keyword is used to signal that the orti has the stack section enabled.
	 */
	public final static String WK_ORTI_STACK = "_writer_keyword_orti_has_stack_";
	
	/**
	 * Orti stack fill default value
	 */
	public final static String DEFAULT_ORTI_STACK_FILL = "0xA5A5A5A5";
	
	/***************************************************************************
	 * ORTI Files
	 **************************************************************************/
	
	/** Identifies system.orti files (one for each cpu) */
	public final static String FILE_EE_ORTI = "system.orti";

	
}
