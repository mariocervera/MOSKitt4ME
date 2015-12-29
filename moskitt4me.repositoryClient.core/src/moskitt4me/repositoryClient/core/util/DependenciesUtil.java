package moskitt4me.repositoryClient.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.ManifestElement;

public class DependenciesUtil {
	
	public static List<IProject> getDependencies(IProject project) {
		
		List<IProject> result = new ArrayList<IProject>();
		
		String projectLocation = project.getLocation().toString();
		String manifestLocation = projectLocation + "/META-INF/MANIFEST.MF";
		
		try {
			InputStream in = new FileInputStream(new File(manifestLocation));
			Map manifest = ManifestElement.parseBundleManifest(in, null);
			
			String requiredBundles = (String) manifest.get("Require-Bundle");
			StringTokenizer st = new StringTokenizer(requiredBundles, ",");
			while(st.hasMoreTokens()) {
				String token = st.nextToken();
				if(validDependency(token)) {
					token = trimDependency(token);
					IProject pluginProject = getPluginProject(token);
					if(pluginProject != null && !result.contains(pluginProject)) {
						result.add(pluginProject);
					}
				}
			}
			
			String importedPackages = (String) manifest.get("Import-Package");
			StringTokenizer st2 = new StringTokenizer(importedPackages, ",");
			while(st2.hasMoreTokens()) {
				String token = st2.nextToken();
				if(validDependency(token)) {
					token = trimDependency(token);
					List<IProject> pluginProjects = getExportingPluginProjects(token);
					if(pluginProjects != null) {
						for(IProject pluginProject : pluginProjects) {
							if(!result.contains(pluginProject)) {
								result.add(pluginProject);
							}
						}
					}
				}
			}
		}
		catch(Exception e) {
			
		}
		
		return result;
	}
	
	private static IProject getPluginProject(String name) {
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		
		for(IProject project : projects) {
			try {
				if(project.hasNature("org.eclipse.pde.PluginNature") 
						&& project.getName().equals(name)) {
					return project;
				}
			}
			catch(CoreException e) {
				
			}
		}
		return null;
	}
	
	private static List<IProject> getExportingPluginProjects(String packageName) {
		
		List<IProject> result = new ArrayList<IProject>();
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		
		for(IProject project : projects) {
			try {
				if(project.hasNature("org.eclipse.pde.PluginNature")) {
					String projectLocation = project.getLocation().toString();
					String manifestLocation = projectLocation + "/META-INF/MANIFEST.MF";
					InputStream in = new FileInputStream(new File(manifestLocation));
					Map manifest = ManifestElement.parseBundleManifest(in, null);
					
					String exportedPackages = (String) manifest.get("Export-Package");
					StringTokenizer st = new StringTokenizer(exportedPackages, ",");
					boolean found = false;
					while(st.hasMoreTokens() && !found) {
						String token = st.nextToken();
						if(validDependency(token)) {
							token = trimDependency(token);
							if(token.equals(packageName)) {
								result.add(project);
								found = true;
							}
						}
					}
				}
			}
			catch(Exception e) {
				
			}			
		}
				
		return result;
	}
	
	private static String trimDependency(String dependency) {
		
		StringTokenizer st = new StringTokenizer(dependency, ";");
		if(st.hasMoreElements()) {
			return st.nextToken();
		}
		else {
			return dependency;
		}
	}
	
	private static boolean validDependency(String dependency) {
		
		if(dependency.startsWith("0") ||
				dependency.startsWith("1") ||
				dependency.startsWith("2") ||
				dependency.startsWith("3") ||
				dependency.startsWith("4") ||
				dependency.startsWith("5") ||
				dependency.startsWith("6") ||
				dependency.startsWith("7") ||
				dependency.startsWith("8") ||
				dependency.startsWith("9")) {
			
			return false;
		}
		
		return true;
	}
}
