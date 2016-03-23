package moskitt4me.projectmanager.processsupport.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.eclipse.core.resources.IProject;

/*
* A class that provides a set of methods that facilitate the implementation of the third phase
* of the Method Engineering lifecycle (i.e., the phase of method execution).
*
* @author Mario Cervera
*/
public class EngineUtil {

	public static void fillPatternInstances(Map<String, String> mapProcessInstanceToCpId,
			IProject project) {
		
		String dir = project.getLocation().toString() + "/.method/";
		String patternInstancesFileName = "patternInstances.txt";
		
		try {
			FileReader fstream = new FileReader(dir + patternInstancesFileName);
			BufferedReader in = new BufferedReader(fstream);
			
			String s = "";
			
			while((s = in.readLine()) != null) {
				if(!s.equals("")) {
					StringTokenizer st = new StringTokenizer(s, " ");
					
					String instanceId = st.nextToken();
					String cpId = st.nextToken();
					
					if(mapProcessInstanceToCpId.get(instanceId) != null) {
						mapProcessInstanceToCpId.remove(instanceId);
					}
					
					mapProcessInstanceToCpId.put(instanceId, cpId);
				}
			}
			
			in.close();
			
		} catch (Exception e) {
			
		}
	}
	
	public static void storePatternInstance(IProject project,
			String processInstanceId, String cpId) {
		
		String dir = project.getLocation().toString() + "/.method/";
		String patternInstancesFileName = "patternInstances.txt";
		
		try {
			FileWriter fstream = new FileWriter(dir + patternInstancesFileName, true);
			BufferedWriter out = new BufferedWriter(fstream);
			
			out.write(processInstanceId + " " + cpId);
			out.newLine();
			
			out.close();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public static void storeExecutedTaskId(IProject project,
			List<String> patternId, String taskId) {
		
		if(patternId == null) {
			patternId = new ArrayList<String>();
		}
		String dir = project.getLocation().toString() + "/.method/";
		String executedTasksFileName = "executedTasks.txt";
		
		try {
			FileWriter fstream = new FileWriter(dir + executedTasksFileName, true);
			BufferedWriter out = new BufferedWriter(fstream);
					
			String patternIdString = "";
			for(String s : patternId) {
				patternIdString += s + " ";
			}
			
			out.write(taskId + " " + patternIdString);
			out.newLine();
			
			out.close();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public static void storeStartedPattern(IProject project,
			String processInstanceID, List<String> cpIds) {
		
		String dir = project.getLocation().toString() + "/.method/";
		String startedPatternsFileName = "startedPatterns.txt";
		
		try {
			FileWriter fstream = new FileWriter(dir + startedPatternsFileName, true);
			BufferedWriter out = new BufferedWriter(fstream);
					
			for(String id : cpIds) {
				out.write(id + " ");
			}
			out.write(processInstanceID);
			out.newLine();
			
			out.close();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static boolean hasExecutedTasks(IProject project) {
		
		String dir = project.getLocation().toString() + "/.method/";
		String executedTasksFileName = "executedTasks.txt";
		
		try {
			FileReader fstream = new FileReader(dir + executedTasksFileName);
			BufferedReader in = new BufferedReader(fstream);
			
			String s = in.readLine();
			
			in.close();
			
			return (s != null);
			
		} catch (Exception e) {
			return false;
		}
	}
	
	public static List<Map<String, List<List<String>>>> getExecutedTaskIds(IProject project) {
		
		String dir = project.getLocation().toString() + "/.method/";
		String executedTasksFileName = "executedTasks.txt";
		
		List<Map<String, List<List<String>>>> result = new ArrayList<Map<String, List<List<String>>>>();
		
		try {
			FileReader fstream = new FileReader(dir + executedTasksFileName);
			BufferedReader in = new BufferedReader(fstream);
			
			String s = "";
			Map<String, List<List<String>>> executedTasks = new HashMap<String, List<List<String>>>();
			
			while((s = in.readLine()) != null) {
				if(!s.equals("")) {
					StringTokenizer st = new StringTokenizer(s, " ");
					String taskId = st.nextToken();
					List<String> cpIds = new ArrayList<String>();
					while(st.hasMoreTokens()) {
						cpIds.add(st.nextToken());
					}
					
					if(executedTasks.get(taskId) != null) {
						executedTasks.get(taskId).add(cpIds);
					}
					else {
						List<List<String>> instances = new ArrayList<List<String>>();
						instances.add(cpIds);
						executedTasks.put(taskId, instances);
					}
				}
				else {
					result.add(executedTasks);
					executedTasks = new HashMap<String, List<List<String>>>();
				}
			}
			
			in.close();
			
			return result;
			
		} catch (Exception e) {
			return new ArrayList<Map<String, List<List<String>>>>();
		}
	}
	
	public static boolean hasBeenExecuted(String taskId,
			List<String> cpIds, IProject project) {
		
		List<String> guids = new ArrayList<String>();
		
		if(cpIds == null || cpIds.size() == 0) {
			guids.add("dp");
		}
		else {
			guids.addAll(cpIds);
		}
		
		String dir = project.getLocation().toString() + "/.method/";
		String executedTasksFileName = "executedTasks.txt";
		
		boolean executed = false;
		
		try {
			FileReader fstream = new FileReader(dir + executedTasksFileName);
			BufferedReader in = new BufferedReader(fstream);
			
			String s = "";
			
			while((s = in.readLine()) != null && !executed) {
				
				if(!s.equals("")) {
					StringTokenizer st = new StringTokenizer(s, " ");
					
					String tid = st.nextToken();
					List<String> capabilityPatternIds = new ArrayList<String>();
					while(st.hasMoreTokens()) {
						capabilityPatternIds.add(st.nextToken());
					}
					
					if(taskId.equals(tid) && equals(guids, capabilityPatternIds)) {
						executed = true;
					}
				}
			}
			
			in.close();
			
			return executed;
			
		} catch (Exception e) {
			return executed;
		}
	}
	
	/*
	 * This method adds a blank line to the file executedTasks.txt. Lines that
	 * do not contain a task id separate executions (i.e., task ids that are not
	 * separated by a blank line represent parallel executions).
	 */
	public static void addSeparator(IProject project) {
		
		String dir = project.getLocation().toString() + "/.method/";
		String executedTasksFileName = "executedTasks.txt";
		
		try {
			FileWriter fstream = new FileWriter(dir + executedTasksFileName, true);
			BufferedWriter out = new BufferedWriter(fstream);
					
			out.newLine();
			
			out.close();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public static Map<String, String> updatePatternInstances(
			Map<String, String> mapProcessInstanceToCpId,
			IProject project, ProcessEngine engine,
			List<HistoricActivityInstance> hai) {
		
		Map<String, String> addedPatterns = new HashMap<String, String>();
		
		List<HistoricActivityInstance> hai2 = engine.getHistoryService()
			.createHistoricActivityInstanceQuery().unfinished().list();
		
		List<HistoricActivityInstance> hai3 = difference(hai2 ,hai);
		
		for(int i = 0; i < hai3.size(); i++) {
			
			HistoricActivityInstance hactv = hai3.get(i);
			
			if(hactv.getActivityType().equals("callActivity")) {
				
				String id = hactv.getActivityId();
				if(id.startsWith("capabilityPattern")) {
					String launchedProcessInstanceID = hai3.get(i + 1).getProcessInstanceId();
					id = id.replace("capabilityPattern", "");
					storePatternInstance(project, launchedProcessInstanceID, id);
					if(mapProcessInstanceToCpId.get(launchedProcessInstanceID) != null) {
						mapProcessInstanceToCpId.remove(launchedProcessInstanceID);
					}
					mapProcessInstanceToCpId.put(launchedProcessInstanceID, id);
					addedPatterns.put(launchedProcessInstanceID, id);
				}
			}
		}
		
		return addedPatterns;
	}
	
	public static Map<String, List<String>> getStartedPatterns(
			Map<String, String> addedPatterns,
			Map<String, String> mapProcessInstanceToCpId,
			ProcessEngine engine) {
		
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		for(String processInstanceId: addedPatterns.keySet()) {
			
			List<String> idChain = new ArrayList<String>();
			calculateIdChain(idChain, mapProcessInstanceToCpId, processInstanceId, engine);
			
			result.put(processInstanceId, idChain);
		}
		
		return result;
	}
	
	private static void calculateIdChain(List<String> idChain,
			Map<String, String> mapProcessInstanceToCpId,
			String processInstanceId, ProcessEngine engine) {
		
		String cpId = mapProcessInstanceToCpId.get(processInstanceId);
		if(cpId != null) {
			idChain.add(0, cpId);
		}
		
		ProcessInstance superProcessInstance = engine.getRuntimeService().createProcessInstanceQuery()
		.subProcessInstanceId(processInstanceId).singleResult();
		
		if(superProcessInstance != null) {
			String newId = superProcessInstance.getProcessInstanceId();
			calculateIdChain(idChain, mapProcessInstanceToCpId, newId, engine);
		}
	}
	
	private static List<HistoricActivityInstance> difference(
			List<HistoricActivityInstance> hai2, List<HistoricActivityInstance> hai) {
		
		List<HistoricActivityInstance> result = new ArrayList<HistoricActivityInstance>();
		
		for(HistoricActivityInstance x : hai2) {
			boolean contained = false;
			for(int i = 0; i < hai.size() && !contained; i++) {
				HistoricActivityInstance x2 = hai.get(i);
				if(x.getActivityId().equals(x2.getActivityId()) &&
						x.getActivityName().equals(x2.getActivityName()) &&
						x.getProcessInstanceId().equals(x2.getProcessInstanceId())) {
					
					contained = true;
				}
			}
			if(!contained) {
				result.add(x);
			}
		}
		
		return result;
	}
	
	public static String getProcessInstance(IProject project, String cpid) {
		
		try {
			String dir = project.getLocation().toString() + "/.method/";
			String methodPropertiesFileName = "startedPatterns.txt";

			FileReader fr = new FileReader(dir + methodPropertiesFileName);
			BufferedReader br = new BufferedReader(fr);
			
			String s;
			String processInstance = "";
			boolean found = false;
			
			while ((s = br.readLine()) != null && !found) {
				if(s.startsWith(cpid)) {
					processInstance = s.replaceFirst(cpid, "").trim();
					found = true;
				}
			}
			
			fr.close();
			
			return processInstance;
			
		} catch (Exception e) {
			return null;
		}
	}
	
	public static List<String> getPatternId(IProject project, String processInstanceId) {
		
		try {
			String dir = project.getLocation().toString() + "/.method/";
			String methodPropertiesFileName = "startedPatterns.txt";

			FileReader fr = new FileReader(dir + methodPropertiesFileName);
			BufferedReader br = new BufferedReader(fr);
			
			String s;
			List<String> patternId = new ArrayList<String>();
			boolean found = false;
			
			while ((s = br.readLine()) != null && !found) {
				if(!s.equals("")) {
					patternId.clear();
					StringTokenizer st = new StringTokenizer(s, " ");
					int tokens = st.countTokens();
					for(int i = 0; i < tokens - 1; i++) {
						String token = st.nextToken();
						patternId.add(token);
					}
					String processId = "";
					if(st.hasMoreTokens()) {
						processId = st.nextToken();
					}
					
					if(processInstanceId.equals(processId)) {
						found = true;
					}
				}
			}
			
			if(!found) {
				patternId.clear();
				patternId.add("dp");
			}
			
			fr.close();
			
			return patternId;
			
		} catch (FileNotFoundException e) {	
			List<String> patternId = new ArrayList<String>();
			patternId.add("dp");
			return patternId;
		} catch (Exception e) {	
			return null;
		}
	}
	
	private static boolean equals(List<String> l1, List<String> l2) {
		
		boolean equals = true;
		
		if(l1.size() != l2.size()) {
			equals = false;
		}
		else {
			for(int i = 0; i < l1.size() && equals; i++) {
				if(!l1.get(i).equals(l2.get(i))) {
					equals = false;
				}
			}
		}
		
		return equals;
	}
}
