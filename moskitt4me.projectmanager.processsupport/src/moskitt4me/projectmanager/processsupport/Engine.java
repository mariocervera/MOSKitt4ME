package moskitt4me.projectmanager.processsupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import moskitt4me.projectmanager.processsupport.util.EngineUtil;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.epf.uma.DeliveryProcess;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

/*
 * This class provides an API for accessing the underlying Activiti engine
 */
public class Engine {

	private static Map<IProject, ProcessEngine> engines = new HashMap<IProject, ProcessEngine>();
	
	private static Map<String, String> mapProcessInstanceToCpId = new HashMap<String, String>();
	
	/*
	 * This method sets up the engine
	 */
	public static void setUp(DeliveryProcess dp, IProject project) throws Exception {
		
		// Process engine construction (uses configuration file)
		
		ProcessEngine activitiEngine = engines.get(project);
		
		if (activitiEngine == null) {
			
			String confFileName = "activiti.cfg.xml";
			
			String dbLocation = project.getLocation().toString() + "/.method/activitiDatabase/";
			
			String dir = EngineConfiguration.createConfigurationFile(confFileName, dbLocation);

			InputStream confFileInputStream = new FileInputStream(new File(dir + "/" + confFileName));
			
			try {
				activitiEngine = ProcessEngineConfiguration
						.createProcessEngineConfigurationFromInputStream(
								confFileInputStream).setProcessEngineName(
								"Activiti_Engine").buildProcessEngine();
				
				engines.put(project, activitiEngine);
			}
			finally {
				confFileInputStream.close();
				EngineConfiguration.deleteConfigurationFile(dir);
			}
		}
		
		// BPMN process deployment
		
		if (activitiEngine.getRepositoryService().createDeploymentQuery()
				.deploymentName(dp.getGuid() + ".bar").count() <= 0) {

			String barFilePath = project.getLocation().toString() + "/.method/"
					+ dp.getGuid() + ".bar";
			
			ZipInputStream inputStream = null;
			
			try {
				URI dpuri = dp.eResource().getURI();
				URI segmentedURI = dpuri.trimSegments(1);
				URI activitiFolderURI = segmentedURI.appendSegment("activiti");
				String activitiFolder = activitiFolderURI.toString();
				
				List<String> guids = getGuids(activitiFolderURI);
				
				createBusinessArchive(activitiFolder, barFilePath, guids);

				InputStream in = new FileInputStream(barFilePath);
				inputStream = new ZipInputStream(in);

				activitiEngine.getRepositoryService().createDeployment().name(
						dp.getGuid() + ".bar").addZipInputStream(inputStream)
						.deploy();
				
			}
			catch (Exception e) {
				MessageDialog.openError(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						"Error",
						"Could not load BPMN 2.0 process. Activiti business archive was not found\n\n");

			}
			finally {
				if(inputStream != null) {
					inputStream.close();
				}
				//Delete business archive
				File f = new File(barFilePath);
				if(f.exists()) {
					f.delete();
				}
			}
		}
		
		// Fill "mapProcessInstanceToCpId" map.
		
		mapProcessInstanceToCpId.clear();
		EngineUtil.fillPatternInstances(mapProcessInstanceToCpId, project);
	}
	
	/*
	 * This method returns whether a task is executable or not given its id
	 * and the id of the current process instance
	 */
	public static boolean isExecutable(String taskId, String processInstanceId, IProject project) {
		
		for (Task task : getCurrentTasks(project)) {
			if (task.getTaskDefinitionKey().equals(taskId)
					&& isBeingExecutedInProcessInstance(task, processInstanceId, project)) {
				
				return true;
			}
		}
		
		return false;
	}
	
	/*
	 * This method executes a task given its id and the id of the 
	 * current process instance
	 */
	public static void executeTask(String taskId, String processInstanceId,
			IProject project) {
		
		Task taskToExecute = null;
		
		ProcessEngine activitiEngine = engines.get(project);
		
		for (Task task : getCurrentTasks(project)) {
			if (task.getTaskDefinitionKey().equals(taskId)
					&& isBeingExecutedInProcessInstance(task, processInstanceId, project)) {
					
				taskToExecute = task;
			}
		}
		
		if(taskToExecute != null && activitiEngine != null) {
			
			List<HistoricActivityInstance> hai = activitiEngine.getHistoryService()
				.createHistoricActivityInstanceQuery().unfinished().list();
			
			activitiEngine.getTaskService().complete(taskToExecute.getId());
			
			Map<String, String> addedPatterns = EngineUtil
					.updatePatternInstances(mapProcessInstanceToCpId, project,
							activitiEngine, hai);

			Map<String, List<String>> startedPatterns = EngineUtil
					.getStartedPatterns(addedPatterns,
							mapProcessInstanceToCpId, activitiEngine);

			for (String key : startedPatterns.keySet()) {
				EngineUtil.storeStartedPattern(project, key, startedPatterns
						.get(key));
			}
			
			List<String> patternId = EngineUtil.getPatternId(project, processInstanceId);
			
			EngineUtil.storeExecutedTaskId(project, patternId, taskId);
		}
	}
	
	/*
	 * Creates a new process instance and returns its id
	 */
	public static String createProcessInstance(String processKey, IProject project) {
		
		try {
			
			ProcessEngine activitiEngine = engines.get(project);
			
			List<HistoricActivityInstance> hai = activitiEngine.getHistoryService()
			.createHistoricActivityInstanceQuery().unfinished().list();
			
			String id = activitiEngine.getRuntimeService()
					.startProcessInstanceByKey(processKey, getProcessVariables()).getId();
			
			Map<String, String> addedPatterns = EngineUtil
					.updatePatternInstances(mapProcessInstanceToCpId, project,
							activitiEngine, hai);
			
			Map<String, List<String>> startedPatterns = EngineUtil.getStartedPatterns(
					addedPatterns, mapProcessInstanceToCpId, activitiEngine);
			
			for(String key : startedPatterns.keySet()) {
				EngineUtil.storeStartedPattern(project, key, startedPatterns.get(key));
			}
			
			return id;

		} catch (Exception e) {
			return null;
		}
	}
	
	/*
	 * Deletes a process instance given its id
	 * 
	 * An exception is thrown if no process instance with the given id is found
	 */
	public static void deleteProcessInstance(String id, IProject project) throws Exception {
		
		ProcessEngine activitiEngine = engines.get(project);

		if (activitiEngine.getRuntimeService().createProcessInstanceQuery()
				.processInstanceId(id).count() > 0) {

			activitiEngine.getRuntimeService().deleteProcessInstance(id, null);
		}
	}
	
	public static String getProcessInstanceIdFromProcessKey(String key, IProject project) {
		
		ProcessEngine activitiEngine = engines.get(project);
		
		if (activitiEngine != null) {
			List<ProcessInstance> instances = activitiEngine
					.getRuntimeService().createProcessInstanceQuery()
					.processDefinitionKey(key).list();

			if (instances.size() == 1) {
				return instances.get(0).getId();
			}
		}
		
		return null;
	}
	
	/*
	 * This method returns the tasks that can be executed
	 */
	private static List<Task> getCurrentTasks(IProject project) {
		
		ProcessEngine activitiEngine = engines.get(project);
		
		if(activitiEngine != null) {
			return activitiEngine.getTaskService().createTaskQuery().list();
		}
		
		return null;
	}
	
	private static Map<String, Object> getProcessVariables() {
		
		//TODO: Implement this method
		
		//Process variables can be used, for instance, to evaluate expressions in exclusive gateways
		
		Map<String, Object> variables = new HashMap<String, Object>();
		
		return variables;
	}
	
	private static void createBusinessArchive(String activitiFolder, 
			String barFilePath, List<String> guids) throws Exception {

		File barfile = new File(barFilePath);
		if(barfile.exists()) {
			barfile.delete();
		}
		
		byte[] buf = new byte[1024];

		ZipOutputStream out = new ZipOutputStream(
				new FileOutputStream(barFilePath));
		
		for(String guid : guids) {
			
			InputStream in = null;
			
			try {
				
				String path = activitiFolder + "/" + guid + ".bpmn20.xml";
				URL url = new URL(path);
				in = url.openStream();

				out.putNextEntry(new ZipEntry(guid + ".bpmn20.xml"));

				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				out.closeEntry();
				in.close();
			}
			catch(Exception e)  {
				if(in != null) {
					in.close();
				}
				continue;
			}
		}

		out.close();
	}
	
	/*
	 * This method returns true if the task is being executed in the context of the process instance.
	 * Subinstances (created when a Call Activities call other processes) must also be checked
	 */
	private static boolean isBeingExecutedInProcessInstance(Task task, String processInstanceId,
			IProject project) {
		
		ProcessEngine activitiEngine = engines.get(project);
		
		if(task.getProcessInstanceId().equals(processInstanceId)) {
			return true;
		}
		else {
			List<ProcessInstance> subinstances = activitiEngine.getRuntimeService().createProcessInstanceQuery()
					.superProcessInstanceId(processInstanceId).list();
			
			boolean found = false;
			
			int i = 0;
			while(i < subinstances.size() && !found) {
				ProcessInstance instance = subinstances.get(i);
				found = isBeingExecutedInProcessInstance(task, instance.getProcessInstanceId(), project);
				i++;
			}
			
			return found;
		}
	}
	
	private static List<String> getGuids(URI activitiFolder) {

		List<String> guids = new ArrayList<String>();
		
		if(!activitiFolder.isPlatformPlugin()) {
		
			String activitiFolderString = activitiFolder.toString().replaceFirst("file:", "");
			
			File dir = new File(activitiFolderString);
		
			if(dir.isDirectory()) {
				File[] files = dir.listFiles();
				
				for(int i = 0; i < files.length; i++) {
					File f = files[i];
					if(f.exists() && !f.isDirectory()
							&& f.getName().endsWith(".bpmn20.xml")) {
						
						String guid = f.getName().replace(".bpmn20.xml", "");
						if(!guids.contains(guid)) {
							guids.add(guid);
						}
					}
				}
			}
		
		}
		else {
			String pluginId = activitiFolder.segment(1);
			Bundle b = Platform.getBundle(pluginId);
			
			String relativePath = activitiFolder.toString().replace("platform:/plugin/" + pluginId, "");
			
			Enumeration _enum = b.getEntryPaths(relativePath);
			
			while(_enum.hasMoreElements()) {
				String path = _enum.nextElement().toString();
				StringTokenizer st = new StringTokenizer(path, "/");
				String finalToken = "";
				while(st.hasMoreTokens()) {
					finalToken = st.nextToken();
				}
				String guid = finalToken.replace(".bpmn20.xml", "");
				guid = guid.replace(".activiti", "");
				guid = guid.replace(".png", "");
				if(!guids.contains(guid)) {
					guids.add(guid);
				}
			}
		}
		
		return guids;
	}
}
