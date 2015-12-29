package moskitt4me.projectmanager.core.context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import moskitt4me.projectmanager.methodspecification.MethodElements;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.epf.uma.RoleDescriptor;
import org.eclipse.epf.uma.TaskDescriptor;

public class Context {

	/*
	 * The roles that are selected in the current moment
	 */
	public static List<RoleDescriptor> currentRoles = new ArrayList<RoleDescriptor>();
	
	/*
	 * The project of the element selected in the MOSKitt Explorer view.
	 */
	public static IProject selectedProject;
	
	/*
	 * The id of the running instance of the process associated to the project
	 */
	public static String processInstanceId;
	
	/*
	 * This enumeration establishes the types of visualization of the Process view
	 */
	public enum ProcessVisualization { NEXTTASKS, ALLTASKS };
	
	public static ProcessVisualization currentProcessVisualization = ProcessVisualization.NEXTTASKS;
	
	public static MOSKitt4MEResourceChangeListener resourceListener;
	
	public static boolean isContainedInCurrentRoles(RoleDescriptor rd) {
		
		for(RoleDescriptor rd2 : currentRoles) {
			if (rd.getRole() != null && rd2.getRole() != null
					&& rd.getRole().getGuid().equals(rd2.getRole().getGuid())) {
				
				return true;
			}
		}
		
		return false;
	}
	
	public static String getProcessModelURI(IProject project) {
		
		try {
			String dir = project.getLocation().toString() + "/.method/";
			String methodPropertiesFileName = "methodProperties.txt";

			FileReader fr = new FileReader(dir + methodPropertiesFileName);
			BufferedReader br = new BufferedReader(fr);
			
			String s;
			String processModelUri = "";
			
			while ((s = br.readLine()) != null && processModelUri.equals("")) {
				if(s.startsWith("ProcessURI: ")) {
					processModelUri = s.replaceFirst("ProcessURI: ", "").trim();
				}
			}
			
			fr.close();
			
			return processModelUri;
			
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String getProcessInstance(List<String> cpIds) {
		
		try {
			String dir = selectedProject.getLocation().toString() + "/.method/";
			String methodPropertiesFileName = "startedPatterns.txt";

			FileReader fr = new FileReader(dir + methodPropertiesFileName);
			BufferedReader br = new BufferedReader(fr);
			
			String s;
			String processInstance = "";
			boolean found = false;
			
			while ((s = br.readLine()) != null && !found) {
				StringTokenizer st = new StringTokenizer(s, " ");
				int tokens = st.countTokens();
				if(tokens == cpIds.size() + 1) {
					boolean matches = true;
					for(int i = 0; i < tokens - 1 && matches; i++) {
						if(!cpIds.get(i).equals(st.nextToken())) {
							matches = false;
						}
					}
					if(matches) {
						processInstance = st.nextToken().trim();
						found = true;
					}
				}
			}
			
			fr.close();
			
			return processInstance;
			
		} catch (Exception e) {
			return null;
		}
	}
	
	public static boolean isPerformedByCurrentRole(TaskDescriptor td) {
		
		if(Context.currentRoles == null || Context.currentRoles.size() == 0) {
			return true;
		}
		
		for(RoleDescriptor rd : Context.currentRoles) {
			if(MethodElements.performs(rd, td)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static void updatePath(String oldPath, String newPath) {
		
		String projectLocation = Context.selectedProject.getLocation().toString();
		String dir = projectLocation + "/.method/";
		String methodPropertiesFileName = "methodProperties.txt";
		
		String workspaceLocation = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toString();
		
		String stringToMatch = workspaceLocation + oldPath + " ";
		String stringToAdd = workspaceLocation + newPath + " ";
		
		List<String> lines = new ArrayList<String>();
		
		try {
			
			FileReader fr = new FileReader(dir + methodPropertiesFileName);
			BufferedReader br = new BufferedReader(fr);
			
			String s = "";
			
			while ((s = br.readLine()) != null) {
				if(s.contains(stringToMatch)) {
					s = s.replace(stringToMatch, stringToAdd);
				}
				lines.add(s);
			}
			
			fr.close();
			
			FileWriter fstream = new FileWriter(dir + methodPropertiesFileName, false);
			BufferedWriter out = new BufferedWriter(fstream);
			
			for(String line : lines) {
				out.write(line);
				out.newLine();
			}
			
			out.close();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
