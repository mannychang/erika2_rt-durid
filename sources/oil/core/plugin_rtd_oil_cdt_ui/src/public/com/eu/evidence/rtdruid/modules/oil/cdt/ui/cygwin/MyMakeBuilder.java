package com.eu.evidence.rtdruid.modules.oil.cdt.ui.cygwin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.eu.evidence.rtdruid.desk.ResourceUtility;
import com.eu.evidence.rtdruid.internal.modules.oil.keywords.IWritersKeywords;
import com.eu.evidence.rtdruid.modules.oil.cdt.ui.Rtd_oil_cdt_Plugin;
import com.eu.evidence.rtdruid.modules.oil.cdt.ui.project.RTDOilProjectNature;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.HostOsUtils;

public class MyMakeBuilder {
	
	/**
	 * This class is used to maintain the os path and the eclipse relative path  
	 * 
	 * @author Nicola Serreli
	 */
	public static class MyMakePath {
		/** OS path */
		public final String osPath;
		
		public MyMakePath(String os) {
			this.osPath = os;
		}
	}

	public static class MyMakeEclipseRelativePath extends MyMakePath {
		/** Full path using eclipse variables */
		public final String fsPath;
		
		public MyMakeEclipseRelativePath(String os, String fs) {
			super(os);
			this.fsPath = fs;
		}
	}

	public static class MyMakeEclipseResourcePath extends MyMakeEclipseRelativePath {
		/** Eclipse relative path */
		public final String wsPath;
		
		public MyMakeEclipseResourcePath(String os, String ws, String fs) {
			super(os, fs);
			this.wsPath = ws;
		}
	}
	
	protected final IProject project;
	protected final Map<String, Object> options;
	
//	private MyMakeBuilder() {
//		project = null;
//	}
	
	public MyMakeBuilder(IProject project, Map<String, Object> options) {
		this.project = project;
		this.options = options == null ? new HashMap<String, Object>() : options;
	}
	
	public static enum FileStatus {
		exist,
		writable,
		invalid
	}
	
	private static final String DEFAULT_NAME = "make_launcher.bat";
	
	
	/**
	 * 
	 * @return if the myMake file exist, must be wrote or is not valid 
	 */
	public FileStatus checkFile() {
		String path = getLocation().osPath;
		FileStatus ok;
		if (path != null) {
			File myMake = new File(path);
			if (myMake.exists()) {
				// already exist and can be used
				ok = myMake.canRead() && myMake.isFile() ? 
						FileStatus.exist : FileStatus.invalid;
			} else {
				// can write in the parent directory
				File parent = myMake;
				do {
					parent = parent.getParentFile();
				} while (parent != null && !parent.exists());
				
				ok = parent != null && parent.canWrite() ? 
						FileStatus.writable : FileStatus.invalid;
			}
		} else {
			ok = FileStatus.invalid;
		}
		
		return ok;
	}

	/**
	 * 
	 * @return the content of current My Cygwin Make
	 */
	public String getFileContent() {
		String pName = project != null ? project.getName() : "_project_name_";
		
		if (HostOsUtils.common.getCurrentSystem() == HostOsUtils.CYGWIN) {
			// C:\\cygwin
			String cygpath_base = CygwinProperties.getCygwinPath();
			if (cygpath_base == null) {
				cygpath_base = "C:\\cygwin"; 
			}
			// check if is required a protection
			if (cygpath_base.indexOf(' ') > -1) {
				cygpath_base = " " + cygpath_base + " ";
			}
			
			String define_erika_files = "";
			if (options.containsKey(IWritersKeywords.ERIKA_ENTERPRISE_LOCATION)) {
				define_erika_files = "\n@set ERIKA_FILES=" + options.get(IWritersKeywords.ERIKA_ENTERPRISE_LOCATION) + "\n\n";
			}
			
			return  
				"@REM   WARNING   this file is automatically generated by RT-Druid\n" +
				"\n" +
				"@REM   If you want modify this file, you should move it to a safe folder,\n" +
				"@REM   like the project's root, and update the path inside the project \n" +
				"@REM   properties \"C/C++ Make Project\" -> \"Build command\"\n" +
				"@REM   i.e.   ${workspace_loc:" + pName + "}/"+DEFAULT_NAME+"\n" +
				"\n" +
				"@set EE_BASH_PATH=" + cygpath_base +"\\bin\\bash\n" +
				"\n" +
				"@if EXIST %EE_BASH_PATH%.exe goto endok\n" +
				"\n" +
				"@echo %EE_BASH_PATH% not found.\n" +
				"\n" +
				"@echo Trying c:\\cygwin\\bin\\bash.exe\n" +
				"@set EE_BASH_PATH=c:\\cygwin\\bin\\bash.exe\n" +
				"@if EXIST %EE_BASH_PATH% goto endok\n" +
				"@echo %EE_BASH_PATH% not found.\n" +
				"\n" +
				"@echo Trying c:\\cygwin\\usr\\bin\\bash.exe\n" +
				"@set EE_BASH_PATH=c:\\cygwin\\usr\\bin\\bash.exe\n" +
				"@if EXIST %EE_BASH_PATH% goto endok\n" +
				"@echo %EE_BASH_PATH% not found.\n" +
				"\n" +
				"@echo Trying d:\\cygwin\\bin\\bash.exe\n" +
				"@set EE_BASH_PATH=d:\\cygwin\\bin\\bash.exe\n" +
				"@if EXIST %EE_BASH_PATH% goto endok\n" +
				"@echo %EE_BASH_PATH% not found.\n" +
				"\n" +
				"@echo Trying d:\\cygwin\\usr\\bin\\bash.exe\n" +
				"@set EE_BASH_PATH=d:\\cygwin\\usr\\bin\\bash.exe\n" +
				"@if EXIST %EE_BASH_PATH% goto endok\n" +
				"@echo %EE_BASH_PATH% not found.\n" +
				"\n" +
				"@echo ERROR: bash not found.\n" +
				"@echo Please check the location of your cygwin installation and enter it in Eclipse in \"Windows/Preferences/RT-Druid/Cygwin paths\".\n" +
				"@exit\n" +
				"\n" +
				":endok\n" +
				"@echo %EE_BASH_PATH% found!\n" +
				define_erika_files +
				"@%EE_BASH_PATH% -c \"/bin/bash --login -c \\\"cd `/bin/cygpath/ -ms \\\"$PWD\\\"`; make %1 %2 %3 %4\\\"\"\n";
//				"@%EE_BASH_PATH% -c \"/bin/bash --login -c \\\"cd \\\\\\\"$PWD\\\\\\\"; make %1 %2 %3 %4\\\"\"\n";
		}
		
		// else
		
		String define_erika_files = "";
		if (options.containsKey(IWritersKeywords.ERIKA_ENTERPRISE_LOCATION)) {
			define_erika_files = "\nERIKA_FILES=" + options.get(IWritersKeywords.ERIKA_ENTERPRISE_LOCATION) + "\n" +
					"export ERIKA_FILES\n\n";
		}

		
		return 
			"#!/bin/sh\n\n" +
			"#   WARNING   this file is automatically generated by RT-Druid\n\n" +
			"#   If you want modify this file, you should move it to a safe folder,\n" +
			"#   like the project's root, and update the path inside the project \n" +
			"#   properties \"C/C++ Make Project\" -> \"Build command\"\n" +
			"#   i.e.   ${workspace_loc:" + pName + "}/"+DEFAULT_NAME+"\n\n" +
			define_erika_files +
			"make $@\n";
		
	}

	
	/**
	 * Builds a new CygWin Make file, if the file doesn't exist.  
	 * 
	 * @throws IOException if the file cannot be wrote
	 */
	public void buildFile() throws IOException {
		
		switch (checkFile()) {
			case exist : // do nothing
				break;
			case writable : // do nothing
				{
					MyMakePath path = getLocation();
					if (path instanceof MyMakeEclipseResourcePath) {
						MyMakeEclipseResourcePath eclPath = (MyMakeEclipseResourcePath) path;
						
						ResourceUtility writer = new ResourceUtility();
						IPath p = new Path(eclPath.wsPath);
						
						writer.saveResourceFile(p.removeLastSegments(1).toString(),
								p.lastSegment(), null, getFileContent().getBytes());
						
						writer.setExecutable(p);
		
					} else {
						File f = new File(path.osPath);
						buildPath(f.getParentFile());
						f.createNewFile();
						FileOutputStream fo = new FileOutputStream(f);
						fo.write(getFileContent().getBytes());
						fo.close();
						
						ResourceUtility writer = new ResourceUtility();
						writer.setExecutable(f);
					}
				}
				break;
			case invalid : // throw a new Exception
					throw new IOException("Cannot write the myMake file.");
				// break;
			default:
					throw new RuntimeException("Unsupported keyword!!");
		}
	}

	
	/**
	 * 
	 * @return the filesystem location of MyMake file 
	 */
	public MyMakePath getLocation() {
		MyMakePath answer = null;
		
		
		/*
		 * Check if is possible to use a file inside the plugin
		 */
//		if (false) {
//			String path = null;
//			try {
//				File bundleFile = FileLocator.getBundleFile(Rtd_oil_cdt_Plugin.getDefault().getBundle());
//				
//				if (bundleFile.isDirectory()) {
//					path = bundleFile.getAbsolutePath();
//				}
//				
//				if (path != null) {
//					File myMake = new File(path + File.separatorChar + DEFAULT_NAME);
//					if (myMake.exists()) {
//						if ((myMake.canRead() && myMake.isFile())) {
//							// already exist and can be used
//							answer = new MyMakePath(myMake.getAbsolutePath());
//						}
//					} else {
//						if (bundleFile.canWrite())
//							answer = new MyMakePath(myMake.getAbsolutePath());
//					}
//				}
//			} catch (IOException e) {
//				RtdruidLog.log(e);
//			}
//		}
		
		if (project != null) {
			String path = RTDOilProjectNature.getCDTWorkFolder(project);
			if (path != null) {
				//IPath outputPrjPath = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path)).getProjectRelativePath();
				IPath p = new Path(path);
				p = p.append(DEFAULT_NAME);

				// Eclipse relative path with variables
				String ecl;
//				if (false) {
//					// project variable has the bug that is refered to the last selected resource !!
//					IPath eclp = p.removeFirstSegments(1).makeAbsolute();
//					ecl = "${project_loc}" + eclp.toString();
//				} else 
				{
					IPath eclp = p.removeFirstSegments(1).makeAbsolute();
					ecl = "${workspace_loc:" + project.getName() + "}" + eclp.toString();
				}
				
				// File System path
				IPath prjOsPath = project.getLocation();
				String os = prjOsPath.toOSString() + File.separatorChar + p.toOSString();
				
				answer = new MyMakeEclipseResourcePath(os, p.toString(), ecl);
			}
		}
		
		
		if (answer == null) {
			// use the workspace path
			IPath path = Rtd_oil_cdt_Plugin.getDefault().getStateLocation();
			IPath p = new Path("/.metadata/.plugin/" + path.lastSegment() + "/" + DEFAULT_NAME);
			
			File myMake = new File(path.toOSString() + File.separatorChar + DEFAULT_NAME);
			if (myMake.exists()) {
				if ((myMake.canRead() && myMake.isFile())) {
					// already exist and can be used
					answer = new MyMakeEclipseRelativePath(myMake.getAbsolutePath(),
							"{workspace_loc}" + p.toString());
				}
			} else {
				if (path.toFile().canWrite())
					answer = new MyMakeEclipseRelativePath(myMake.getAbsolutePath(),
							"{workspace_loc}" + p.toString());
			}
		}
		                     
		
		return answer;
	}
	
	
	protected void buildPath(File f) throws IOException {
		if (f == null) {
			throw new NullPointerException();
		}
		
		if (!f.exists()) {

			boolean ok = f.mkdirs();
			
			if (!ok) {
				if (!f.exists() || !f.isDirectory()) {
					throw new IOException("Problems creating the directory " + f.getPath());
				}
			}
			
		} else if (!f.isDirectory()) {
			throw new IOException(f.getPath() + " exist and is not a directory");
		}
	}
}
