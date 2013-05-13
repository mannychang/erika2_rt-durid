/**
 * 26/set/2012
 */
package com.eu.evidence.rtdruid.internal.modules.oil.codewriter.erikaenterprise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.eu.evidence.rtdruid.desk.Messages;
import com.eu.evidence.rtdruid.internal.modules.oil.exceptions.OilCodeWriterException;
import com.eu.evidence.rtdruid.internal.modules.oil.keywords.ISimpleGenResKeywords;
import com.eu.evidence.rtdruid.internal.modules.oil.keywords.IWritersKeywords;
import com.eu.evidence.rtdruid.modules.oil.abstractions.IOilObjectList;
import com.eu.evidence.rtdruid.modules.oil.abstractions.IOilWriterBuffer;
import com.eu.evidence.rtdruid.modules.oil.abstractions.ISimpleGenRes;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.AbstractRtosWriter;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.CommonUtils;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.SectionWriter;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.comments.FileTypes;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.comments.ICommentWriter;
import com.eu.evidence.rtdruid.modules.oil.codewriter.erikaenterprise.SectionWriterIsr;
import com.eu.evidence.rtdruid.modules.oil.codewriter.erikaenterprise.SectionWriterKernelCounterHw;
import com.eu.evidence.rtdruid.modules.oil.codewriter.erikaenterprise.hw.CpuUtility;
import com.eu.evidence.rtdruid.modules.oil.codewriter.erikaenterprise.hw.EEStackData;
import com.eu.evidence.rtdruid.modules.oil.codewriter.erikaenterprise.hw.EEStacks;
import com.eu.evidence.rtdruid.modules.oil.erikaenterprise.constants.IEEWriterKeywords;
import com.eu.evidence.rtdruid.vartree.ITreeInterface;
import com.eu.evidence.rtdruid.vartree.IVariable;

/**
 *
 * @author Nicola Serreli
 * @since 2.0
 *
 */
public class TricoreModel_tc27x extends TricoreAbstractModel implements IEEWriterKeywords {
	
	public static class TricoreModelProvider_tc27x implements ITricoreModelProvider {
		
		private final String modelName;
		
		/**
		 * 
		 */
		public TricoreModelProvider_tc27x(String modelName) {
			this.modelName = modelName;
		}
		
		/* (non-Javadoc)
		 * @see com.eu.evidence.rtdruid.internal.modules.oil.codewriter.erikaenterprise.TricoreAbstractModel.ITricoreModelProvider#newTricoreModel()
		 */
		@Override
		public TricoreAbstractModel newTricoreModel() {
			return new TricoreModel_tc27x(modelName);
		}
	}
	
	
	private static final String STACK_BASE_NAME = "EE_tc_stack_";
	private static final String SGR_OS_CPU_SYS_STACK_SIZE = "sgr__os_cpu_system_stack_size";
	private static final long DEFAULT_SYS_STACK_SIZE = 16384;
	
	protected final String ERR_CPU_TYPE = "TriCore";
	private static final String EE_TRICORE_SYSTEM_TIMER_HANDLER = "EE_tc_system_timer_handler";
	private static final String EE_TRICORE_HANDLER_BASEID = "TC";
	
	private final static String[] Tasking_systemStackSymbols = new String[] {
		"_lc_ub_ustack_tc0", // core0
		"_lc_ub_ustack_tc1", // core1
		"_lc_ub_ustack_tc2", // core2
	};

	
	protected final List<String> osekKeywords = Arrays.asList(new String[] {
			IWritersKeywords.OSEK_BCC1, //
			IWritersKeywords.OSEK_BCC2, //
			IWritersKeywords.OSEK_ECC1, //
			IWritersKeywords.OSEK_ECC2, //
			IWritersKeywords.OSEK_SC1, //
			IWritersKeywords.OSEK_SC2, //
			IWritersKeywords.OSEK_SC3, //
			IWritersKeywords.OSEK_SC4 //
	});
	
	private SectionWriterIsr isrWriter; 
	private SectionWriterKernelCounterHw counterHwWriter;
	
	private final String modelName;
	
	/**
	 * 
	 */
	public TricoreModel_tc27x(String modelName) {
		this.modelName = modelName;
	}
	
	/* (non-Javadoc)
	 * @see com.eu.evidence.rtdruid.internal.modules.oil.codewriter.erikaenterprise.TricoreAbstractModel#isPackIsrPriorities()
	 */
	@Override
	public boolean isPackIsrPriorities() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.eu.evidence.rtdruid.internal.modules.oil.codewriter.erikaenterprise.TricoreAbstractModel#getEEopts()
	 */
	@Override
	public String[] getEEopts(int currentRtosId, IOilObjectList ool) {
		ArrayList<String> tmp = new ArrayList<String>();
		
		if (currentRtosId == -1) {
			tmp.add(SectionWriterHalTricore.EEOPT_HAL_TRICORE);
			tmp.add("EE_" + modelName.toUpperCase()+"__");		
		} else {
			{ // compiler
				switch (currentCompiler) {
					case DEFAULT:
					case TASKING:
						tmp.add(SectionWriterHalTricore.EEOPT_TASKING);
						break;
					case GNU:
						tmp.add(SectionWriterHalTricore.EEOPT_GNU);
						break;
					default:
						Messages.sendWarningNl("Unsupported compiler");
				}
			}
		}
		
		// only for master cpu
		if (currentRtosId == 0) {
			int cpuNumber = parent.getOilObjects().length;
			if (cpuNumber >1) {
				tmp.add("EE_START_CPU1"); 
			}
			if (cpuNumber >2) {
				tmp.add("EE_START_CPU2");
			}
		}
		return (String[]) tmp.toArray(new String[tmp.size()]);
    }
	
	/* (non-Javadoc)
	 * @see com.eu.evidence.rtdruid.internal.modules.oil.codewriter.erikaenterprise.TricoreAbstractModel#updateObjects(com.eu.evidence.rtdruid.internal.modules.oil.codewriter.erikaenterprise.ErikaEnterpriseWriter, int)
	 */
	@Override
	public void updateObjects(ErikaEnterpriseWriter parent, int currentRtosId) throws OilCodeWriterException {
		super.updateObjects(parent, currentRtosId);

		if (isrWriter == null) {
			isrWriter = new SectionWriterIsr(parent, EE_TRICORE_HANDLER_BASEID);
			isrWriter.setComputeEntryFromPriority(true);
			isrWriter.setGenerateDefineCategory(true);
			isrWriter.setGenerateDefineIsrId(true);
	//		isrWriter.setValidEntries(ISR_LIST);
			isrWriter.updateObjects();
		}
		if (counterHwWriter == null) {
			counterHwWriter = new SectionWriterKernelCounterHw(parent, EE_TRICORE_HANDLER_BASEID, EE_TRICORE_SYSTEM_TIMER_HANDLER);
			counterHwWriter.setAllowSystemTimerPriority(true);
			counterHwWriter.setGenerateIsr2Defines(isrWriter);
			counterHwWriter.updateObjects();
		}
		
		final IOilObjectList[] oilObjects = parent.getOilObjects();
		final IOilObjectList ool = oilObjects[currentRtosId];
		
		/***********************************************************************
		 * 
		 * System stack size
		 *  
		 **********************************************************************/
		{
			String[] stack_size = parent.getCpuDataValue(ool, "SYS_STACK_SIZE");
			if (stack_size != null && stack_size.length>0 && stack_size[0] != null) {
				
				boolean valid = false;
				int value = -1;
				try {
					value = Integer.decode(stack_size[0]);
					valid = true;
				} catch (NumberFormatException e) {
					Messages.sendWarningNl("Invalid value for System stack size : " + stack_size[0]);
				}
				
				if (valid && value <0) {
					Messages.sendWarningNl("System stack size cannot be negative (" + value + ")");
				} else {
					
					ISimpleGenRes sgrCpu = ool.getList(IOilObjectList.OS).get(0);
					sgrCpu.setProperty(SGR_OS_CPU_SYS_STACK_SIZE, ""+value);
				}
			}
		}
	}
	
	
	@Override
	public void write(final int currentRtosId, final IOilObjectList ool, final IOilWriterBuffer buffers) throws OilCodeWriterException {
		
		if (currentRtosId == 0 && parent.getOilObjects().length>1) {
			writeMulticoreCommon(ool, buffers);
		}
		
		writeCfg(currentRtosId, ool, buffers);
		writeMultistack(currentRtosId, ool, buffers);
		writeKernelIsr(currentRtosId, ool, buffers);
	}

	/**
	 * @param buffers
	 */
	private void writeCfg(final int currentRtosId, final IOilObjectList ool, final IOilWriterBuffer buffers) {
		final String indent = IWritersKeywords.INDENT;
		StringBuffer sbInithal_h = buffers.get(FILE_EE_CFG_H);
		
		ISimpleGenRes sgrCpu = ool.getList(IOilObjectList.OS).get(0);
		if (sgrCpu.containsProperty(SectionWriterHalTricore.SGR_OS_CPU_HW_END_INIT)) {
			
			
			sbInithal_h.append(indent + SectionWriter.getCommentWriter(ool, FileTypes.H).writerSingleLineComment("Hardware startup end initialization function") + 
					indent + "#define EE_START_UP_USER_ENDINIT     " + sgrCpu.getString(SectionWriterHalTricore.SGR_OS_CPU_HW_END_INIT)
					+ "\n\n");
			
		}
	}

	
	/**
	 * @param buffers
	 */
	private void writeMulticoreCommon(final IOilObjectList ool, final IOilWriterBuffer buffers) {
		
		ICommentWriter commentWriter = SectionWriter.getCommentWriter(ool, FileTypes.C);

		{
	    	final ISimpleGenRes sgrOs = ool.getList(IOilObjectList.OS).get(0);
			CpuUtility.addSources(sgrOs, buffers.getFileName(FILE_EE_COMMON_C));
		}
		
		
		StringBuffer sbCommon_c = buffers.get(FILE_EE_COMMON_C);
		
		sbCommon_c.append(commentWriter.writerBanner("Spin Lock Implementation")
				+ "#include \"ee.h\"\n"
				+ "EE_UINT32 EE_SHARED_UDATA EE_COMPILER_ALIGN(4) EE_tc_spin_lock[EE_MAX_CPU];\n\n");
	}

	/* (non-Javadoc)
	 * @see com.eu.evidence.rtdruid.internal.modules.oil.codewriter.erikaenterprise.ITricoreModel#writeMultistack()
	 */
	protected void writeMultistack(final int currentRtosId, final IOilObjectList ool, final IOilWriterBuffer answer) throws OilCodeWriterException {
		final String indent = IWritersKeywords.INDENT;

		{
			/***********************************************************************
			 * SYSTEM STACK SIZE
			 **********************************************************************/
			ISimpleGenRes sgrCpu = ool.getList(IOilObjectList.OS).get(0);
			StringBuilder builder = new StringBuilder();
			if (sgrCpu.containsProperty(SGRK__MAKEFILE_CPU_EXT_VARS__)) {
				builder.append(sgrCpu.getString(SGRK__MAKEFILE_CPU_EXT_VARS__));
			}
			
			{ // compiler
				switch (currentCompiler) {
					case DEFAULT:
					case TASKING:
						builder.append("\n" + SectionWriter.getCommentWriter(ool, FileTypes.MAKEFILE).writerSingleLineComment("Add a flag for the linkerscript to set the minimum size of system stack") + 
								"LDFLAGS += -Wl-DUSTACK_TC"+currentRtosId+"=" + 
									( ErikaEnterpriseWriter.checkOrDefault(AbstractRtosWriter.getOsProperty(ool, SGR_OS_CPU_SYS_STACK_SIZE),
											DEFAULT_SYS_STACK_SIZE))
								+ "\n\n");
						break;
					case GNU:
						builder.append("\n" + SectionWriter.getCommentWriter(ool, FileTypes.MAKEFILE).writerSingleLineComment("Add a flag for the linkerscript to set the minimum size of system stack") + 
								"LDFLAGS += -Wl,--defsym=__USTACK_SIZE=" +//+currentRtosId+"=" + 
									( ErikaEnterpriseWriter.checkOrDefault(AbstractRtosWriter.getOsProperty(ool, SGR_OS_CPU_SYS_STACK_SIZE),
											DEFAULT_SYS_STACK_SIZE))
								+ "\n\n");
						break;
					default:
				}
			}
			
			
			sgrCpu.setProperty(SGRK__MAKEFILE_CPU_EXT_VARS__, builder.toString());
			
			ICommentWriter commentH = SectionWriter.getCommentWriter(ool, FileTypes.H);
			StringBuffer sbInithal_h = answer.get(FILE_EE_CFG_H);
			sbInithal_h.append(indent + commentH.writerSingleLineComment("System stack size") + 
					indent + "#define EE_SYS_STACK_SIZE     " + 
						( ErikaEnterpriseWriter.checkOrDefault(AbstractRtosWriter.getOsProperty(ool, SGR_OS_CPU_SYS_STACK_SIZE),
								DEFAULT_SYS_STACK_SIZE))
					+ "\n\n");
		}
		
		if (DEF__MULTI_STACK__.equals(parent.getStackType())) {
			ICommentWriter commentC = SectionWriter.getCommentWriter(ool, FileTypes.C);

	
			/*
			 * Define a string for each MAX_OBJECT_NUMBER (OBJECT=task, RESOURCE, ...).
			 * Binary distribution uses the suffix RTD_. 
			 */
			final boolean binaryDistr = parent.checkKeyword(IEEWriterKeywords.DEF__EE_USE_BINARY_DISTRIBUTION__);
			final String MAX_TASK = (binaryDistr ? "RTD_" : "EE_") + "MAX_TASK";
	
			
			// ------------- Buffers --------------------
			StringBuffer sbInithal_c = answer.get(FILE_EE_CFG_C);
			
			/* A buffer about stack  */
			final StringBuffer sbStack = new StringBuffer();
	
			/* A buffer about declarations of stacks  */
			final StringBuffer sbStackDecl = new StringBuffer();
			final StringBuffer sbStackDeclSize = new StringBuffer();
			
			// used by ORTI
			ArrayList<EEStackData> stackTmp = new ArrayList<EEStackData>();
			EEStackData sys_stack = new EEStackData(0,
					new long[] { Long.decode(ErikaEnterpriseWriter.checkOrDefault(AbstractRtosWriter.getOsProperty(ool, SGR_OS_CPU_SYS_STACK_SIZE), "" + DEFAULT_SYS_STACK_SIZE))},
					new long[] {0},
					new String[] {" (int)(&"+Tasking_systemStackSymbols[currentRtosId]+")"}, true);
	
			// ------------- Buffers --------------------
			
			final int STACK_ALIGNMENT_UNIT = 8;// ErikaEnterpriseWriter.getStackUnit(ool);
			final int STACK_SIZE_UNIT = 1;// ErikaEnterpriseWriter.getStackUnit(ool);
			final ICommentWriter commentWriterC = SectionWriter.getCommentWriter(ool, FileTypes.C);
			List<ISimpleGenRes> taskNames = ool.getList(IOilObjectList.TASK);
	
			String pre = "";
			String post = "";
	
	
			sbInithal_c.append("\n#include \"ee.h\"\n");
			sbInithal_c.append(commentWriterC
					.writerBanner("Stack definition for TriCore" ));
			
			ITreeInterface ti = vt.newTreeInterface();
	
	
			
			int[] irqSize = null;
			if (parent.checkKeyword(DEF__IRQ_STACK_NEEDED__)) {
				/***************************************************************
				 * IRQ_STACK
				 **************************************************************/
				final List<String> currentCpuPrefixes = AbstractRtosWriter.getOsProperties(ool, SGRK_OS_CPU_DATA_PREFIX);
				for (String currentCpuPrefix: currentCpuPrefixes) {
					if (irqSize != null) {
						break;
					}
	
					String[] child = new String[1];
					String type = CommonUtils
							.getFirstChildEnumType(vt, currentCpuPrefix
									+ "MULTI_STACK", child);
	
					if ("TRUE".equalsIgnoreCase(type)) {
						String prefixIRQ = currentCpuPrefix
							+ "MULTI_STACK" + VARIANT_ELIST+child[0] + PARAMETER_LIST
							+ "IRQ_STACK";
						boolean ok = "TRUE".equalsIgnoreCase(CommonUtils
						.getFirstChildEnumType(vt, prefixIRQ, child));
						
						if (ok) {
							
							prefixIRQ += VARIANT_ELIST + child[0] +PARAMETER_LIST;
							irqSize = new int[1];
							{ // get data for IRQ STACK ...
								String path[] = { "SYS_SIZE" };
	
								for (int i = 0; i < path.length; i++) {
									String tmp = null;
									IVariable var = ti.getValue(prefixIRQ + path[i]
											+ VALUE_VALUE);
									if (var != null && var.get() != null) {
										tmp = var.toString();
									}
									if (tmp == null)
										throw new RuntimeException(
												ERR_CPU_TYPE + " : Expected " + path[i]);
	
									// check for value
									try {
										// ... store them inside the irqSize vector
										irqSize[0] = (Integer.decode("" + tmp))
												.intValue();
										// ... and increase the memory requirement
	//									stackEnd += irqSize[0];
									} catch (Exception e) {
										throw new RuntimeException(
												ERR_CPU_TYPE + " : Wrong int" + path[i]
														+ ", value = " + tmp + ")");
									}
								}
							}
						}
					}
				}
	
			}
	
			/*
			 * elStack contains all data about stack, for current rtos and its
			 * tasks.
			 * 
			 * tList and tListN are used to identify all tasks (theirs name and
			 * system id).
			 * 
			 * elStack accepts the list of task's names (tList) to compute all
			 * required stack and theirs size/position
			 */
			EEStacks elStack = new EEStacks(parent, ool, irqSize);
			elStack.setDummyStackPolicy(elStack.FORCE_ALWAYS | elStack.FORCE_FIRST);
			ArrayList<String> tList = new ArrayList<String>();
			ArrayList<String> tListN = new ArrayList<String>();
	
			{
				/***************************************************************
				 * STACK FOR EACH TASK
				 **************************************************************/
				
				// add the dummy task
				tListN.add(" ");
				tList.add(IWritersKeywords.dummyName);
				
				// fill data for each task
				for (Iterator<ISimpleGenRes> iter = taskNames.iterator(); iter.hasNext();) {
	
					ISimpleGenRes sgr = (ISimpleGenRes) iter.next();
					tList.add(sgr.getName());
					tListN.add(sgr.getString(ISimpleGenResKeywords.TASK_SYS_ID));
				}
	
				// compute total stack size and add it to memory requirement
	//			int offset[][] = elStack.taskOffsets((String[]) tList
	//					.toArray(new String[0]));
	//			stackEnd += offset[offset.length - 1][0]; // tot sys
			}
	
			
			{
				/***************************************************************
				 * PREPARE BUFFERS
				 **************************************************************/
	
				pre = "";
				post = "";
	
				/* get the link between a task and its stack. */
				int pos[] = elStack.taskStackLink(tList
						.toArray(new String[1]));
				/* get the size of each stack. */
				int size[][] = elStack.stackSize(tList
						.toArray(new String[1]));
				/* descrStack contains a description for each stack. */ 
				String[] descrStack = new String[size.length];
				
				
				sbStack.append(indent
						+ "const EE_UREG EE_std_thread_tos["+MAX_TASK+"+1] = {\n");
	
				
				
					
				// DESCRIPTIONS
				for (int j = 0; j < pos.length; j++) {
					sbStack.append(pre + post + indent + indent + +pos[j]+"U");
					// set new values for "post" and "pre"
					post = commentC.writerSingleLineComment(tList.get(j));
					pre = ",\t";
	
					/*
					 * add the name of current task to the description of the /
					 * right stack. Check also if there is already something or
					 * not, infact in the second case append the new description
					 * to the old one
					 */ 
					descrStack[pos[j]] = (descrStack[pos[j]] == null) ?
							// The first description
							("Task " + tListN.get(j) + " (" + tList.get(j) + ")")
							:
							// other descriptions
							(descrStack[pos[j]] + ", " + "Task "
									+ tListN.get(j) + " (" + tList.get(j) + ")"); // others
				}
	
				// close sbStack
				sbStack.append(" \t" + post + indent + "};\n\n" + indent
						+ "struct EE_TOS EE_tc_system_tos["
						+ (size.length) + "] = {\n");
				
				pre = "";
				post = "";
				
				// USED BY ORTI
				stackTmp.add(sys_stack);
				
	//			 DECLARE STACK SIZE && STACK (ARRAY)
				for (int j = 1; j < size.length; j++) {
				    long value = size[j][0];
				    value  = (value + (value%STACK_ALIGNMENT_UNIT)) / STACK_SIZE_UNIT; // arrotondo a 2
					sbStackDecl.append(indent + "EE_STACK_T EE_COMPILER_ALIGN("+STACK_ALIGNMENT_UNIT+"U)  "+STACK_BASE_NAME+j+"[EE_STACK_WLEN(STACK_"+j+"_SIZE)] = EE_TC_FILL_STACK("+STACK_BASE_NAME+j+");\t" + commentC.writerSingleLineComment(descrStack[j]));
					sbStackDeclSize.append(indent + "#define STACK_"+j+"_SIZE "+value+ commentC.writerSingleLineComment("size = "+size[j][0]+" bytes"));
					// USED BY ORTI
					stackTmp.add(new EEStackData(j, new long[] {size[j][0]}, new long[] {size[j][0]},
							new String[] {" (int)(&"+STACK_BASE_NAME+j+")"}, true)); // DELTA
				}
				
				/*
				 * For each stack prepare the configuration's vectors and
				 * descriptions
				 */
				for (int j = 0; j < size.length; j++) {
				    
			        String value = j == 0 ? "{0}" : "{EE_STACK_INITP("+STACK_BASE_NAME+j+")}";
	
					sbStack.append(pre
							+ post
							+ indent
							+ indent
							+ value);
	
					// set new values for size
					pre = ",";
					post = "\t" + commentC.writerSingleLineComment(descrStack[j]);
				}
	
				// complete the stack's buffer
				sbStack.append(" " + post + indent + "};\n\n" + indent
						+ "EE_UREG EE_tc_active_tos = 0;" +commentC.writerSingleLineComment("dummy") + "\n");
				sbStack.append(indent
						+ "struct EE_CTX EE_tc_system_ctx["+MAX_TASK+"+1];\n\n");
	
				{ // if required, init also the irq stack
					if (irqSize != null) {
					    int j = size.length;
					    long value = irqSize[0];
					    value  = (value + (value%STACK_ALIGNMENT_UNIT)) / STACK_SIZE_UNIT; // arrotondo a 2
						sbStackDecl.append(indent + "EE_STACK_T EE_COMPILER_ALIGN("+STACK_ALIGNMENT_UNIT+"U)  "+STACK_BASE_NAME+j+"[EE_STACK_WLEN(STACK_"+j+"_SIZE)] = EE_TC_FILL_STACK("+STACK_BASE_NAME+j+");" + commentC.writerSingleLineComment("irq stack"));
						sbStackDeclSize.append(indent + "#define STACK_"+j+"_SIZE "+value+commentC.writerSingleLineComment("size = "+irqSize[0]+" bytes"));
	
						sbStack
								.append(indent+commentC.writerSingleLineComment("stack used only by IRQ handlers")
										+ indent+"struct EE_TOS EE_tc_IRQ_tos = {\n"
										+ indent+indent+"EE_STACK_INITP("+STACK_BASE_NAME+j+")\n"
										+ indent+"};\n\n");
	
						// REQUIRED By ORTI's STACK
						int eesdID = stackTmp.size();
						stackTmp.add(new EEStackData(eesdID, new long[] {irqSize[0]}, new long[] {irqSize[0]},
								new String[] {" (int)(&"+STACK_BASE_NAME+j+")"}, true)); // DELTA
					}
				}
				
				{// ORTI : Store link between task and stack
					int j = 1;
					for (Iterator<ISimpleGenRes> iter = taskNames.iterator(); iter.hasNext(); j++) {
						ISimpleGenRes sgr = iter.next();
						sgr.setObject(SGRK_TASK_STACK, stackTmp.get(pos[j]));
					}
					ISimpleGenRes sgrCpu = ool.getList(IOilObjectList.OS).get(0);
					sgrCpu.setObject(SGRK_OS_STACK_LIST, stackTmp.toArray(new EEStackData[0]));
					sgrCpu.setObject(SGRK_OS_STACK_VECTOR_NAME, "EE_tc_system_tos");
				}
			}
	
			// ------------- Write --------------------
			//  add stack buffers
			sbInithal_c.append(sbStackDeclSize+"\n"+
			        sbStackDecl + "\n" +
			        sbStack);
		} else {
			
		}
	}
	
	
	/**
	 * @param ool
	 * @param buffers
	 * @throws OilCodeWriterException 
	 */
	protected void writeKernelIsr(int currentRtosId, IOilObjectList ool, IOilWriterBuffer buffers) throws OilCodeWriterException {
		isrWriter.writeIsr(currentRtosId, ool, buffers);
		counterHwWriter.writeCounterHw(currentRtosId, ool, buffers);
		if (ool.getList(IOilObjectList.ISR).size()>0) {
			int max_isr_prio = 0;
			String max_isr_string = "EE_ISR_UNMASKED";
			for (ISimpleGenRes isr : ool.getList(IOilObjectList.ISR)) {
				if (isr.containsProperty(ISimpleGenResKeywords.ISR_GENERATED_PRIORITY_VALUE)) {
					final int current_prio = isr.getInt(ISimpleGenResKeywords.ISR_GENERATED_PRIORITY_VALUE);
					if (max_isr_prio< current_prio) {
						max_isr_prio = current_prio;
						max_isr_string = isr.getString(ISimpleGenResKeywords.ISR_GENERATED_PRIORITY_STRING);
					}
				}
			}

			StringBuffer sbInithal_h = buffers.get(FILE_EE_CFG_H);

			sbInithal_h.append("\n" +SectionWriter.getCommentWriter(ool, FileTypes.H).writerSingleLineComment("Max ISR priority") + 
					"#define EE_TC_MAX_ISR_ID     " + max_isr_string + "\n\n");
		}
	}
}
