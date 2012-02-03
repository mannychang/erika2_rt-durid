/*
 * Created on Nov 29, 2004
 *
 * $Id: AbstractRtosWriter.java,v 1.8 2008/05/14 17:13:15 durin Exp $
 */
package com.eu.evidence.rtdruid.modules.oil.codewriter.common;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.core.runtime.Assert;

import com.eu.evidence.rtdruid.internal.modules.oil.codewriter.common.OilObjectList;
import com.eu.evidence.rtdruid.internal.modules.oil.exceptions.OilCodeWriterException;
import com.eu.evidence.rtdruid.internal.modules.oil.keywords.ISimpleGenResKeywords;
import com.eu.evidence.rtdruid.internal.modules.oil.keywords.IWritersKeywords;
import com.eu.evidence.rtdruid.modules.oil.abstractions.IOilObjectList;
import com.eu.evidence.rtdruid.modules.oil.abstractions.IOilWriterBuffer;
import com.eu.evidence.rtdruid.modules.oil.abstractions.ISimpleGenRes;
import com.eu.evidence.rtdruid.modules.oil.abstractions.SimpleGenRes;
import com.eu.evidence.rtdruid.modules.oil.interfaces.IRtosWriter;
import com.eu.evidence.rtdruid.modules.oil.keywords.IOilXMLLabels;
import com.eu.evidence.rtdruid.vartree.ITreeInterface;
import com.eu.evidence.rtdruid.vartree.IVarTree;
import com.eu.evidence.rtdruid.vartree.IVarTreePointer;
import com.eu.evidence.rtdruid.vartree.IVariable;
import com.eu.evidence.rtdruid.vartree.abstractions.old.GenRes;
import com.eu.evidence.rtdruid.vartree.abstractions.old.Task;
import com.eu.evidence.rtdruid.vartree.abstractions.old.TaskSet;
import com.eu.evidence.rtdruid.vartree.data.DataPackage;
import com.eu.evidence.rtdruid.vartree.data.init.DataPath;
import com.eu.evidence.rtdruid.vartree.data.oil.OilApplPackage;

/**
 * This class is an abstract writer for a specific RT-OS.
 * 
 * @author Nicola Serreli
 */
public abstract class AbstractRtosWriter implements IRtosWriter {

	/** Contains all data */
	protected IVarTree vt;

	/** Contains zero or more options */
	protected HashMap<String, ?> options;

	/** All keys, extracted from data (never null) */
	protected String[] keys;

	/** Contains all OSEK Object in */
	protected IOilObjectList[] oilObjects;

	/** A shortcut to {@link DataPath#SEPARATOR DataPath.SEPARATOR} */
	private final static String S = "" + DataPath.SEPARATOR;

	/**
	 * Contains all path that identifies inside the VarTree all rtos
	 */
	protected String[] rtosPrefix;

	/** Architectural prefix */
	protected String architecturalPrefix;
	
	/**
	 * Contains the prefix inside the OilVar (one for all rtos)
	 */
	protected String oilHwRtosPrefix;

	/***************************************************************************
	 * 
	 * INITIALIZATION
	 *  
	 **************************************************************************/

	/* (non-Javadoc)
	 * @see com.eu.evidence.rtdruid.internal.modules.oil.codewriter.common.IRtosWriter#init(com.eu.evidence.rtdruid.vartree.IVarTree, java.lang.String[], java.util.HashMap)
	 */
	public void init(IVarTree vt, String[] rtosPrefix, HashMap<String, ?> opt) 
			throws OilCodeWriterException {
		Assert.isNotNull(vt);
		Assert.isNotNull(rtosPrefix);
		this.vt = vt;
		this.rtosPrefix = rtosPrefix;
		this.options = opt == null ? new HashMap<String, Object>() : opt;
		
		//oilVarPrefix = "";
		oilHwRtosPrefix = "";
		
		{ // search the architectural prefix
			
			if (rtosPrefix.length>0) {
				String[] tmp = DataPath.splitPath(rtosPrefix[0]);
				
				if (tmp.length>2) {
					architecturalPrefix = DataPath.makeSlashedId(tmp[0]) + S + DataPath.makeSlashedId(tmp[1]); 
				}
				
				oilHwRtosPrefix = S
						+ OilApplPackage.eINSTANCE.getRoot_HwList().getName()
						+ S
						+ DataPath.makeSlashedId(getHwOilId())
						+ S
						+ OilApplPackage.eINSTANCE.getHW_RtosList().getName()
						+ S
						+ DataPath.makeSlashedId(getRtosOilId())
						+ S
						+ OilApplPackage.eINSTANCE.getRTOS_ParameterList()
								.getName()+S;

			}
			
		}

		{ // extract keys
			this.keys = extractKeys();
			//System.err.println("" + Arrays.asList(this.keys));
			Assert.isNotNull(this.keys);
		}

		{ // extract all Oil Object Lists
			this.oilObjects = extractObjects();
			Assert.isNotNull(this.oilObjects);
		}
	}

	/**
	 * This method searchs all KEYWORDS defined inside the VarTree for current
	 * list of specific rtos. (HW type is a first one)<br>
	 *
	 * (See also {@link IWritersKeywords WritersKeys})
	 * 
	 * @return an array of String that contains all Keywords defined in the first rtos
	 * 
	 * @throws OilCodeWriterException if there are some problems
	 */
	protected String[] extractKeys() throws OilCodeWriterException  {

		String[] answer = new String[0];

		if (rtosPrefix.length > 0) {

			// check only the first RTOS !!! (as rtos of CPU0)
			IVarTreePointer vtp = vt.newVarTreePointer();

			// check for rtos
			if (!vtp.go(rtosPrefix[0])) {
				return answer;
			}

			// get HW type
			String hw_type = getHwOilId();
			
			// if hw_type is set, add it to keywords
			if (hw_type != null) {
				String[] tmp = new String[answer.length + 1];
				System.arraycopy(answer, 0, tmp, 0, answer.length);
				tmp[answer.length] = hw_type;
				answer = tmp;
			}
		}
		return answer;
	}

	/**
	 * This method searchs all Oil objects defined inside the VarTree for
	 * current list of specific rtos.
	 * 
	 * @return an array of OilObjectList that contains all object defined. An
	 *         OilObjectList for each CPU. (Never returns null)
	 * 
	 * @throws OilCodeWriterException
	 *             throw this exception if there are some problems
	 */
	protected IOilObjectList[] extractObjects()
			throws OilCodeWriterException {
		IOilObjectList[] answer = new IOilObjectList[rtosPrefix.length];

		// cicle all rtos
		for (int rtosId = 0; rtosId < rtosPrefix.length; rtosId++) {
			IOilObjectList ool = new OilObjectList();
			answer[rtosId] = ool;

			// search all objects
			for (int i = 0; i < IOilObjectList.OBJECT_NUMBER; i++) {
				ool.setList(i, extractObject(i, rtosPrefix[rtosId]));
			}
		}

		return answer;
	}

	/**
	 * This method searchs all Oil Objects of a specific category for a specific
	 * rtos. All valid categories are listed in
	 * {@link OilObjectList OilObjectList}.
	 * 
	 * @param type
	 *            identifies the type of Object
	 * @param rtosPrefix
	 *            identifies the rtos
	 * 
	 * @return all Oil Objects (of given type) for the specific rtos
	 * 
	 * @throws OilCodeWriterException
	 *             throw this exception if there are some problems
	 */
	protected ISimpleGenRes[] extractObject(int type, String rtosPrefix) 
			throws OilCodeWriterException {
		SimpleGenRes[] answer = new SimpleGenRes[0];
		String sysName = DataPath.splitPath(rtosPrefix)[0];
		final DataPackage DPKG = DataPackage.eINSTANCE;

		switch (type) {
		/* ----------------------  OS  ---------------------- */
		case IOilObjectList.OS: {
			answer = new SimpleGenRes[] { new SimpleGenRes(
					getVarAsString(rtosPrefix + S
							+ DPKG.getRtos_Name().getName()), rtosPrefix) };
			
			{ // ----------------- CPU type-----------------
				String[] tp = DataPath.splitPath(rtosPrefix);
				String cpuTypePath = tp[0] // System
							+S+ tp[1]		// Architectural
							+S+ tp[2]		// EcuList
							+S+ tp[3]		// Ecu
							+S+ tp[4]		// CpuList
							+S+ tp[5]		// Cpu
							+S+ DPKG.getCpu_Model().getName()		// Cpu_Type
						   ;
				String[] cpuType = CommonUtils.getValue(vt, cpuTypePath);
				if (cpuType!= null && cpuType.length!=0) {
					answer[0].setProperty(ISimpleGenResKeywords.OS_CPU_TYPE, cpuType[0]);
				} else {
					answer[0].setProperty(ISimpleGenResKeywords.OS_CPU_TYPE, "UNKNOW_CPU");
				}
				answer[0].setProperty(ISimpleGenResKeywords.OS_CPU_NAME, tp[5]);
			}
		}
			break;

		/* ----------------------  OS APPLICATION  ---------------------- */
		case IOilObjectList.OSAPPLICATION: {

			ArrayList<SimpleGenRes> tempList = new ArrayList<SimpleGenRes>();
			
			
			String[] split = DataPath.splitPath(rtosPrefix);
			if (!(split == null || split.length <7)) {
				
				String path = DataPath.makePath(new String[] {
						split[0], // sys 
						split[1], // arch
						split[2], // arch_eculist
						split[3], // ecu
						split[4], // cpu_list
						split[5], // cpu
						DPKG.getCpu_OsApplication().getName()
				});
				
				String[] applNames = vt.newTreeInterface().getAllName(path, DPKG.getOsApplication().getName());
				
				for (String name: applNames) {
					SimpleGenRes s = new SimpleGenRes(name, path+S+ name);
					s.setProperty(IOilXMLLabels.ATTR_TYPE,
							IOilXMLLabels.OBJ_OSAPPLICATION);
					s.setProperty(ISimpleGenResKeywords.RTOS_PATH,
							rtosPrefix);
					tempList.add(s);
				}
			}
			answer = tempList.toArray( new SimpleGenRes[tempList.size()] );
		}
			break;

		/* ----------------------  TASK  ---------------------- */
		case IOilObjectList.TASK: {
			String rtosName = getVarAsString(rtosPrefix + S
					+ DataPackage.eINSTANCE.getRtos_Name().getName());
			if (rtosName == null) {
				break;
			}

			/*******************************************************************
			 * Search tasks
			 ******************************************************************/
			
			// use class TaskSet to search all tasks mapped to current rtos
			TaskSet ts = new TaskSet(vt, sysName);
			ts.setProperty(Task.STR_TASK_TYPE, "", false);
			ts.setProperty(Task.STR_PRIORITY, "", false);
			ts.setProperty(Task.STR_ACT_NUMBER, "", false);
			ts.setProperty(Task.STR_RESOURCE_LIST, "", false);
			
			for (int i = 0; i < ts.getPrefixNumber(); i++) {
				if (rtosName.equals(ts.getPrefix(i))) {

					//answer = new SimpleGenRes[ts.getSize(i)];
					ArrayList<SimpleGenRes> tmpAnswer = new ArrayList<SimpleGenRes>();
					for (int h = 0; h < ts.getSize(i); h++) {
						GenRes current = ts.getItem(i, h);
						
						if (!"task".equalsIgnoreCase(current.getString(Task.STR_TASK_TYPE))) {
							continue; // this element is not a task
						}
						
						SimpleGenRes currentAnswer = new SimpleGenRes(
								current.getName(), current
										.getPath());
						tmpAnswer.add(currentAnswer);
						
						// take also ....
						
						// ----------- PRIORITY ------------
						if (current.existProperty("priority")) {
							Object ttt = current.getProperty("priority");

							if (isAValidInteger(ttt)) {
								currentAnswer.setProperty(ISimpleGenResKeywords.TASK_PRIORITY, "" + ttt);
							} else {
								throw new OilCodeWriterException("Not found or not valid value for task "+ current.getName() + "'s priority");
							}
						}
						// ----------- ACTIVATION ------------
						if (current.existProperty("ActNumber")) {
							Object ttt = current.getProperty("ActNumber");

							if (ttt == null || (""+ttt).length() == 0) {
								// TODO: USE DEFAULT VALUE !!!!!!!!!
								currentAnswer.setProperty(ISimpleGenResKeywords.TASK_ACTIVATION, "" + 1);
							} else if (isAValidInteger(ttt)) {
								currentAnswer.setProperty(ISimpleGenResKeywords.TASK_ACTIVATION, "" + ttt);
							} else {
								throw new OilCodeWriterException("Not found or not valid value for task "+ current.getName() + "'s priority's property");
							}
						}
						// ----------- RESOURCE ------------
						if (current.existProperty("Resource")) {
							Object ttt = current.getCollection("Resource");
							if (ttt!=null) {
								currentAnswer.setObject(ISimpleGenResKeywords.TASK_RESOURCE_LIST, ttt);
							}
						}
					}
					
					answer = tmpAnswer.toArray( new SimpleGenRes[tmpAnswer.size()] );
					break;
				}
			}
			
			/*******************************************************************
			 * Take values from varTree (inside the oilVar)
			 ******************************************************************/
			
			// 1) Search inside the OilVar:
			//    Priority, Activation, Schedule, Event, Autostart, Resource
			// 2) if found, store them inside the rigth SimpleGenRes
			for (int i=0; i< answer.length; i++) {
				// common data
				String path = answer[i].getPath() +S+ DPKG.getTask_OilVar().getName() +S+IOilXMLLabels.OBJ_TASK+oilHwRtosPrefix;
				String[] values;
				String chType;
				/*
				{ // ----------- PRIORITY ------------
					values = CommonUtils.getValue(vt, path +"PRIORITY");
					if (values!= null && values.length == 1) {
						//answer[i].setProperty(SimpleGenResKeywords.TASK_PRIORITY, values[0]);
					} else {
						throw new OilCodeWriterException("Required the priority of task " + answer[i].getName());
					}
				}
				
				{ // ----------- ACTIVATION ------------
					values = CommonUtils.getValue(vt, path+"ACTIVATION");
					if (values!= null && values.length == 1) {
						answer[i].setProperty(SimpleGenResKeywords.TASK_ACTIVATION, values[0]);
					}
				}*/
				
				{	// ----------- SCHEDULE ------------
					chType = CommonUtils.getFirstChildEnumType(vt, path+"SCHEDULE");
					if (chType!= null) {
						answer[i].setProperty(ISimpleGenResKeywords.TASK_SCHEDULE, chType);
					} else {
						answer[i].setProperty(ISimpleGenResKeywords.TASK_SCHEDULE, "");
					}
				}
				
				{	// ----------- EVENT & TASK_EXTENDED ------------
					boolean extended = false;
					values = CommonUtils.getValue(vt, path+"EVENT");
					if (values!= null && values.length > 0) {
						answer[i].setObject(ISimpleGenResKeywords.TASK_EVENT_LIST, Arrays.asList(values));
						extended = true;
					}
					answer[i].setProperty(ISimpleGenResKeywords.TASK_EXTENDED, ""+extended);
				}
				
				{	// ----------- AUTOSTART ------------
					String[] chName = new String[1]; 
					chType = CommonUtils.getFirstChildEnumType(vt, path+"AUTOSTART", chName);
					if ("true".equalsIgnoreCase(chType)) {
						
						values = CommonUtils.getValue(vt, path+"AUTOSTART"+CommonUtils.VARIANT_ELIST+chName[0]+CommonUtils.PARAMETER_LIST+"APPMODE");
						
						if (values!= null && values.length > 0) {
							answer[i].setObject(ISimpleGenResKeywords.TASK_APPMODES_LIST, Arrays.asList(values));
						} else {
							// store an empty list 
							answer[i].setObject(ISimpleGenResKeywords.TASK_APPMODES_LIST, new ArrayList<String>());
						}
					} else {
						// don't store anything
					}
				}
				
				/*{ // ----------- RESOURCES ------------
					values = CommonUtils.getValue(vt, path+"RESOURCE");
					if (values!= null && values.length > 0) {
						answer[i].setObject(SimpleGenResKeywords.TASK_RESOURCE_LIST, Arrays.asList(values));
					}
				}*/
			}
		}
			break;
			/* ----------------------  ISR   ---------------------- */
		case IOilObjectList.ISR: {
			String rtosName = getVarAsString(rtosPrefix + S
					+ DataPackage.eINSTANCE.getRtos_Name().getName());
			if (rtosName == null) {
				break;
			}

			/*******************************************************************
			 * Search tasks
			 ******************************************************************/
			
			// use class TaskSet to search all tasks mapped to current rtos
			TaskSet ts = new TaskSet(vt, sysName);
			ts.setProperty(Task.STR_TASK_TYPE, "", false);
			ts.setProperty(Task.STR_RESOURCE_LIST, "", false);
			
			for (int i = 0; i < ts.getPrefixNumber(); i++) {
				if (rtosName.equals(ts.getPrefix(i))) {

					//answer = new SimpleGenRes[ts.getSize(i)];
					ArrayList<SimpleGenRes> tmpAnswer = new ArrayList<SimpleGenRes>();
					for (int h = 0; h < ts.getSize(i); h++) {
						GenRes current = ts.getItem(i, h);
						
						if (!"isr".equalsIgnoreCase(current.getString(Task.STR_TASK_TYPE))) {
							continue; // this element is not an isr
						}
						
						SimpleGenRes currentAnswer = new SimpleGenRes(
								current.getName(), current
										.getPath());
						tmpAnswer.add(currentAnswer);
						
						// take also ....

						// ----------- RESOURCE ------------
						if (current.existProperty("Resource")) {
							Object ttt = current.getCollection("Resource");
							if (ttt!=null) {
								currentAnswer.setObject(ISimpleGenResKeywords.TASK_RESOURCE_LIST, ttt);
							}
						}
					}
					
					answer = tmpAnswer.toArray( new SimpleGenRes[tmpAnswer.size()] );
					break;
				}
			}
			
			/*******************************************************************
			 * Take values from varTree (inside the oilVar)
			 ******************************************************************/
			
			// 1) Search inside the OilVar:
			//    Priority, Activation, Schedule, Event, Autostart, Resource
			// 2) if found, store them inside the rigth SimpleGenRes
			for (int i=0; i< answer.length; i++) {
				// common data
				String path = answer[i].getPath() +S+ DPKG.getTask_OilVar().getName() +S+IOilXMLLabels.OBJ_ISR+oilHwRtosPrefix;
				String[] values;
				
				{	// ----------- CATEGORY ------------
					values = CommonUtils.getValue(vt, path+"CATEGORY");
					if (values!= null && values.length >0) {
						answer[i].setProperty(ISimpleGenResKeywords.ISR_CATEGORY, values[0]);
					} else {
						answer[i].setProperty(ISimpleGenResKeywords.ISR_CATEGORY, "");
					}
				}
			}
		}
			break;
		/* ----------------------  ALARM - COUNTER - EVENT  ---------------------- */
		case IOilObjectList.COUNTER:
		case IOilObjectList.ALARM:
		case IOilObjectList.EVENT:
		{
			String signalPath = sysName+S+DPKG.getSystem_Architectural().getName()+S
    			+DPKG.getArchitectural_SignalList().getName();
			ITreeInterface ti = vt.newTreeInterface();
	        String[] signalNames = ti.getAllName(signalPath,DPKG.getSignal().getName());
	        ArrayList<ISimpleGenRes> tmpAnswer = new ArrayList<ISimpleGenRes>(); // use a dynamic list to store all object
	        
	        // choose the type (as String) of current OilObjectList
	        String requiredType = null;
	        switch (type) {
				case IOilObjectList.COUNTER: requiredType = IOilXMLLabels.OBJ_COUNTER; break;
				case IOilObjectList.ALARM:   requiredType = IOilXMLLabels.OBJ_ALARM; break;
				case IOilObjectList.EVENT:   requiredType = IOilXMLLabels.OBJ_EVENT; break;
	        }
	        
	        // Look at all objects and extract only objects with the rigth type
	        // For selected objects, search and store theirs properties
	        for (int i=0; i<signalNames.length; i++) {
	        	String tmpPath = signalPath +S+signalNames[i]+S;
	        	
	        	// .. check object type ..
	        	IVariable var = ti.getValue(tmpPath+DPKG.getSignal_Type().getName());
	        	if (var != null && requiredType.equals(var.toString())) {
	        	
	        		ISimpleGenRes sgr = new SimpleGenRes(
							signalNames[i], tmpPath); 
					tmpAnswer.add(sgr);

					// each object type has differents properties. Select the correct group
					String[][] requiredValues = new String[0][0];
					if (type == IOilObjectList.COUNTER) {
						/////////////          COUNTER          /////////////
						
						requiredValues = new String[][] {
								{ "MAXALLOWEDVALUE", ISimpleGenResKeywords.COUNTER_MAX_ALLOWED},
								{ "MINCYCLE", ISimpleGenResKeywords.COUNTER_MIN_CYCLE},
								{ "TICKSPERBASE", ISimpleGenResKeywords.COUNTER_TICKS}
						};
						
					} else if (type == IOilObjectList.EVENT) {
						/////////////          EVENT          /////////////
						
						requiredValues = new String[][] {
								{ "MASK", ISimpleGenResKeywords.EVENT_MASK}
						};

					} else if (type == IOilObjectList.ALARM) {
						/////////////          ALARM          /////////////

						requiredValues = new String[][] {
								{ "COUNTER", ISimpleGenResKeywords.ALARM_COUNTER}
						};
						
						{ // take ALARM ACTION
							String path = tmpPath+DPKG.getSignal_OilVar().getName() 
								+S+requiredType+oilHwRtosPrefix+"ACTION";
							
							final String str_ACTION_ACTIVATE_TASK = "ACTIVATETASK";
							final String str_ACTION_SET_EVENT = "SETEVENT";
							final String str_ACTION_ALARM_CALL_BACK = "ALARMCALLBACK";
							
							final String str_ACTIVATE_TASK_TASK = "TASK";
							
							final String str_SET_EVENT_TASK = "TASK";
							final String str_SET_EVENT_EVENT = "EVENT";
							
							final String str_ALARM_CALL_BACK_NAME = "ALARMCALLBACKNAME";
							
							String[] childName = new String[1];
							String tmpType = CommonUtils.getFirstChildEnumType(vt, path, childName);
							
							if (str_ACTION_ACTIVATE_TASK.equalsIgnoreCase(tmpType)) { // ACTIVATE TASK
								final String taskName;
								
								path +=CommonUtils.VARIANT_ELIST+childName[0]+CommonUtils.PARAMETER_LIST;
								
								// TASK
								String[] val = CommonUtils.getValue(vt, path+str_ACTIVATE_TASK_TASK);
								if (val != null && val.length>0 && val[0] != null) {
									taskName = val[0];
								} else {
									throw new OilCodeWriterException("Required " + 
											str_ACTION_ACTIVATE_TASK + "/"+str_ACTIVATE_TASK_TASK 
											+ " for alarm " + sgr.getName());
								}
	
								sgr.setObject(ISimpleGenResKeywords.ALARM_ACTIVATE_TASK, taskName);
								sgr.setProperty(ISimpleGenResKeywords.ALARM_ACTION_TYPE,
										ISimpleGenResKeywords.ALARM_ACTIVATE_TASK);

							} else if (str_ACTION_SET_EVENT.equalsIgnoreCase(tmpType)) { // SET EVENT
								final String taskName;
								final String eventName;
								
								path +=CommonUtils.VARIANT_ELIST+childName[0]+CommonUtils.PARAMETER_LIST;
								
								// TASK
								String[] val = CommonUtils.getValue(vt, path+str_SET_EVENT_TASK);
								if (val != null && val.length>0 && val[0] != null) {
									taskName = val[0];
								} else {
									throw new OilCodeWriterException("Required " + 
											str_ACTION_SET_EVENT + "/"+str_SET_EVENT_TASK 
											+ " for alarm " + sgr.getName());
								}
								
								// EVENT
								val = CommonUtils.getValue(vt, path+str_SET_EVENT_EVENT);
								if (val != null && val.length>0 && val[0] != null) {
									eventName = val[0];
								} else {
									throw new OilCodeWriterException("Required " + 
											str_ACTION_SET_EVENT + "/"+str_SET_EVENT_EVENT 
											+ " for alarm " + sgr.getName());
								}
	
								sgr.setObject(ISimpleGenResKeywords.ALARM_SET_EVENT, new String[] {taskName, eventName});
								sgr.setProperty(ISimpleGenResKeywords.ALARM_ACTION_TYPE,
										ISimpleGenResKeywords.ALARM_SET_EVENT);

							} else if (str_ACTION_ALARM_CALL_BACK.equalsIgnoreCase(tmpType)) { // ALARM CALL BACK
								final String callBackName;
								
								path +=CommonUtils.VARIANT_ELIST+childName[0]+CommonUtils.PARAMETER_LIST;
								
								// TASK
								String[] val = CommonUtils.getValue(vt, path+str_ALARM_CALL_BACK_NAME);
								if (val != null && val.length>0 && val[0] != null) {
									callBackName = val[0];
								} else {
									throw new OilCodeWriterException("Required " + 
											str_ACTION_ALARM_CALL_BACK + "/"+str_ALARM_CALL_BACK_NAME 
											+ " for alarm " + sgr.getName());
								}
	
								sgr.setObject(ISimpleGenResKeywords.ALARM_CALL_BACK, callBackName);
								sgr.setProperty(ISimpleGenResKeywords.ALARM_ACTION_TYPE,
										ISimpleGenResKeywords.ALARM_CALL_BACK);

							} else {

								throw new OilCodeWriterException("Expected one of "
										+ str_ACTION_ACTIVATE_TASK +", "
										+ str_ACTION_SET_EVENT + " and "
										+ str_ACTION_ALARM_CALL_BACK
										+ " as action of alarm " + sgr.getName());
							}
							

						}

						
						{ // take ALARM AUTOSTART
							String path = tmpPath+DPKG.getSignal_OilVar().getName() 
								+S+requiredType+oilHwRtosPrefix+"AUTOSTART";
							String[] childName = new String[1];
							String tmpType = CommonUtils.getFirstChildEnumType(vt, path, childName);
							if ("true".equalsIgnoreCase(tmpType)) {
								String[] modes = null;
								long alarmTime =0;
								long cycleTime =0;
								
								path +=CommonUtils.VARIANT_ELIST+childName[0]+CommonUtils.PARAMETER_LIST;
								
								// APPMODE
								String[] val = CommonUtils.getValue(vt, path+"APPMODE");
								if (val != null) {
									modes = val;
								}
								
								// ALARMTIME
								val = CommonUtils.getValue(vt, path+"ALARMTIME");
								if (val != null) {
									try {
										alarmTime = Long.decode(val[0]).longValue();
									} catch (NumberFormatException e) {
										throw new OilCodeWriterException("The ALARMTIME parameter of ALARM "
												+ sgr.getName()+" contains a not valid number : "+val[0]);
									}
								}
	
								// CYCLETIME
								val = CommonUtils.getValue(vt, path+"CYCLETIME");
								if (val != null) {
									try {
										cycleTime = Long.decode(val[0]).longValue();
									} catch (NumberFormatException e) {
										throw new OilCodeWriterException("The CYCLETIME parameter of ALARM "
												+ sgr.getName()+" contains a not valid number : "+val[0]);
									}
								}
	
								AlarmAutoStartDescr aasd = new AlarmAutoStartDescr(modes, alarmTime, cycleTime);
								sgr.setObject(ISimpleGenResKeywords.ALARM_AUTOSTART, aasd);
								
								
							} else if ("false".equalsIgnoreCase(tmpType)) {
								String[] modes = null;
								long alarmTime =0;
								long cycleTime =0;
								
								path +=CommonUtils.VARIANT_ELIST+childName[0]+CommonUtils.PARAMETER_LIST;
																
								// ALARMTIME
								String[] val = val = CommonUtils.getValue(vt, path+"ALARMTIME");
								if (val != null) {
									try {
										alarmTime = Long.decode(val[0]).longValue();
									} catch (NumberFormatException e) {
										throw new OilCodeWriterException("The ALARMTIME parameter of ALARM "
												+ sgr.getName()+" contains a not valid number : "+val[0]);
									}
								}
	
								// CYCLETIME
								val = CommonUtils.getValue(vt, path+"CYCLETIME");
								if (val != null) {
									try {
										cycleTime = Long.decode(val[0]).longValue();
									} catch (NumberFormatException e) {
										throw new OilCodeWriterException("The CYCLETIME parameter of ALARM "
												+ sgr.getName()+" contains a not valid number : "+val[0]);
									}
								}
	
								AlarmAutoStartDescr aasd = new AlarmAutoStartDescr(modes, alarmTime, cycleTime);
								sgr.setObject(ISimpleGenResKeywords.ALARM_INIT, aasd);
							}
						} 
					}
					
					// ... search proprerties inside the OilVar and store them as properties (if found) ...
					for (int vi=0; vi<requiredValues.length; vi++) {
						String tt = tmpPath+DPKG.getSignal_OilVar().getName() 
								+S+requiredType+oilHwRtosPrefix+requiredValues[vi][0];
						String[] value = CommonUtils.getValue(vt, tt);
						if (value!=null && value.length>0) {
							sgr.setProperty(requiredValues[vi][1], value[0]);
						}

					}
	        	}
	        }
	        
	        // convert the answer 
	        if (tmpAnswer.size()>0) {
	        	answer = (SimpleGenRes[]) tmpAnswer.toArray(new SimpleGenRes[tmpAnswer.size()]);
	        }
		}
			break;
			
		/* ----------------------  RESOURCE  ---------------------- */
		case IOilObjectList.RESOURCE:
		if (false) {
			String resPath = sysName+S+DPKG.getSystem_Architectural().getName()+S
    			+DPKG.getArchitectural_MutexList().getName();
			
			// search all Architectural Mutex ...
	        String[] resNames = vt.newTreeInterface().getAllName(resPath,DPKG.getMutex().getName());
	        if (resNames.length>0) {
				answer = new SimpleGenRes[resNames.length];

				// ... and store as SimpleGenRes 
		        for (int i=0; i<resNames.length; i++) {
					answer[i] = new SimpleGenRes(
							resNames[i], resPath+S+resNames[i]+S);
		        }
	        }
		}
			break;

		/* ----------------------  APPMODE  ---------------------- */
		case IOilObjectList.APPMODE:
		{
			String modesPath = sysName+S+DPKG.getSystem_Modes().getName()+S+DPKG.getModes_ModeList().getName();

			// search all Modes ...
	        String[] modesNames = vt.newTreeInterface().getAllName(modesPath,DPKG.getMode().getName());
	        if (modesNames.length>0) {
				answer = new SimpleGenRes[modesNames.length];

				// ... and store as SimpleGenRes 
		        for (int i=0; i<modesNames.length; i++) {
					answer[i] = new SimpleGenRes(
							modesNames[i], modesPath+S+modesNames[i]+S);
		        }
	        }
		}
			break;
		/* ----------------------  OTHERS  ---------------------- */
		case IOilObjectList.MESSAGE:
		case IOilObjectList.NETWORKMESSAGE:
		case IOilObjectList.COM:
		case IOilObjectList.IPDU:
		case IOilObjectList.NM:
			break;

		/* ----------------------  CPU  ---------------------- */
		case IOilObjectList.CPU: // nothing
			break;
		default:
			throw new IllegalArgumentException(
					"Specified Oil Object Type is not valid : " + type);
		}

		return answer;
	}

	/**
	 * Searchs a var inside the tree e return it's string value. If it doesn't
	 * find the var or its value is null, returns null.
	 * 
	 * @param path
	 *            idenfies the variable
	 * 
	 * @return the String rapresentation of the specified var or null if the var
	 *         was empty or not found
	 */
	protected String getVarAsString(String path) {
		IVarTreePointer vtp = vt.newVarTreePointer();

		// search ...
		if (vtp.go(path)) {
			// ... check if is a container
			if (vtp.isContainer()) {
				return null;
			}
			// ... then get the var ...
			IVariable var = vtp.getVar();
			if (var != null && var.get() != null) {
				// ... and return the value
				return var.toString();
			}
		}

		return null;
	}
	
	/**
	 * Returns the size of Rtos List 
	 */
	public int getRtosSize() {
		return rtosPrefix != null ? rtosPrefix.length : 0;
	}
	

	/**
	 * Check if specified object contains a valid integer rapresentation (using toString)
	 */
	protected boolean isAValidInteger(Object txt) {
		if (txt == null) {
			return false;
		}
		
		if (txt instanceof Integer) {
			return true;
		}
		
		String toString = txt.toString();
		
		try {
			Integer.decode(toString);
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Searchs the given Key inside the current keyword's list.
	 * 
	 * @param key
	 *            specified the string to search
	 * 
	 * @return true if the specified string is one of current keywords
	 */
	public boolean checkKeyword(String key) {
		Assert.isNotNull(key);
		for (int i = 0; i < keys.length; i++) {
			if (key.equals(keys[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return Returns the oilHwRtosPrefix.
	 */
	public String getOilHwRtosPrefix() {
		return oilHwRtosPrefix;
	}
	
	/**
	 * @return Returns the options.
	 */
	public HashMap<String, ?> getOptions() {
		return options;
	}
	
	/* (non-Javadoc)
	 * @see com.eu.evidence.rtdruid.internal.modules.oil.codewriter.common.IRtosWriter#getOilObjects()
	 */
	public IOilObjectList[] getOilObjects() {
		return oilObjects;
	}
	
	/**
	 * Returns current IVarTree
	 * */
	public IVarTree getVt() {
		return vt;
	}
	
	/***************************************************************************
	 * 
	 * ABSTRACT METHOD
	 *  
	 **************************************************************************/

	/* (non-Javadoc)
	 * @see com.eu.evidence.rtdruid.internal.modules.oil.codewriter.common.IRtosWriter#write()
	 */
	public abstract IOilWriterBuffer[] write() throws OilCodeWriterException;

	/**
	 * Returns the identifier of current rtos inside the Oil Implementation
	 * Factory.
	 * 
	 * @return the identifier of current rtos
	 */
	protected abstract String getRtosOilId();

	/**
	 * Returns the identifier of current HW inside the Oil Implementation
	 * Factory.
	 * 
	 * @return the identifier of current rtos
	 */
	protected abstract String getHwOilId();
}

