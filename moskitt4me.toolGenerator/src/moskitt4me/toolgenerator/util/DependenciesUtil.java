package moskitt4me.toolgenerator.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;

/*
* This class is in charge of automatically managing all the software dependency issues that
* arise between plug-ins during the CASE generation process. This is done by accessing the
* MANIFEST.MF files of the plug-ins.
*/
public class DependenciesUtil {
	
	private static State state = Platform.getPlatformAdmin().getState();
	
	public static List<String> pluginDependencies() {
		
		List<String> dependencies = new ArrayList<String>();
		List<String> checkedBundles = new ArrayList<String>();
		
		List<String> bundleIds = getPluginProjectDependencies(checkedBundles);
		addMandatoryDependencies(bundleIds);
		dependencies.addAll(bundleIds);
		
		for(String plugin: GeneratorUtil.internalPlugins) {
			Bundle bundle = Platform.getBundle(plugin);
			if(bundle != null) {
				if(!dependencies.contains(plugin)) {
					dependencies.add(plugin);
				}
				if(!bundleIds.contains(plugin)) {
					bundleIds.add(plugin);
				}
			}
		}
		
		for(String bundleId : bundleIds) {
			addDependencies(bundleId, dependencies, checkedBundles);
		}
		
		return dependencies;
	}
	
	private static void addDependencies(String bundleId, List<String> dependencies,
			List<String> checkedBundles) {
		
		if(!checkedBundles.contains(bundleId)) {
			Bundle bundle = Platform.getBundle(bundleId);
			if(bundle != null) {
				checkedBundles.add(bundleId);
				BundleDescription desc = state.getBundle(bundle.getBundleId());
				if(desc != null) {
					BundleSpecification[] requiredBundles = desc.getRequiredBundles();
					for(BundleSpecification spec : requiredBundles) {
						String dependencyName = spec.getName();
						if(!dependencies.contains(dependencyName)) {
							dependencies.add(dependencyName);
							addDependencies(dependencyName, dependencies, checkedBundles);
						}
					}
					ImportPackageSpecification[] importPackages = desc.getImportPackages();
					for(ImportPackageSpecification spec : importPackages) {
						String packageName = spec.getName();
						List<String> dependencyNames = getExportingPlugins(packageName);
						for(String dependencyName : dependencyNames) {
							if(!dependencyName.equals("") && !dependencies.contains(dependencyName)) {
								dependencies.add(dependencyName);
								addDependencies(dependencyName, dependencies, checkedBundles);
							}
						}
					}
				}
			}
		}
	}
	
	private static List<String> getPluginProjectDependencies(List<String> checkedBundles) {
		
		//Add dependencies of Plug-in projects in the workspace
		
		List<String> result = new ArrayList<String>();
		
		for(IProject project : GeneratorUtil.projects) {
			checkedBundles.add(project.getName());
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
						if(!result.contains(token) && !isWorkspaceProject(token)) {
							result.add(token);
						}
					}
				}
				
				String importedPackages = (String) manifest.get("Import-Package");
				StringTokenizer st2 = new StringTokenizer(importedPackages, ",");
				while(st2.hasMoreTokens()) {
					String token = st2.nextToken();
					if(validDependency(token)) {
						token = trimDependency(token);
						List<String> dependencyNames = getExportingPlugins(token);
						for(String dependencyName : dependencyNames) {
							if(!dependencyName.equals("") && !result.contains(dependencyName)
									&& !isWorkspaceProject(dependencyName)) {
								result.add(dependencyName);
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
	
	private static void addMandatoryDependencies(List<String> dependencies) {
		
		//Add plugins from "org.eclipse.platform" and "org.eclipse.rcp" features
		
		IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
		for(IBundleGroupProvider provider : providers) {
			IBundleGroup[] groups = provider.getBundleGroups();
			for(IBundleGroup group : groups) {
				if(group.getIdentifier().equals("org.eclipse.platform") ||
						group.getIdentifier().equals("org.eclipse.rcp")) {
					Bundle[] bundles = group.getBundles();
					for(Bundle bundle : bundles){
						BundleDescription desc = state.getBundle(bundle.getBundleId());
						if(desc != null) {
							String dependencyName = desc.getName();
							if(!dependencies.contains(dependencyName)) {
								dependencies.add(dependencyName);
							}
						}
					}
				}
			}
		}
	}
	
	private static boolean isWorkspaceProject(String bundleId) {
		
		for(IProject pr : GeneratorUtil.projects) {
			if(pr.getName().equals(bundleId)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static List<String> getExportingPlugins(String packageName) {
		
		List<String> result = new ArrayList<String>();
		
		try {
			BundleDescription[] descriptions = state.getBundles();
			for(BundleDescription desc : descriptions) {
				ExportPackageDescription[] exportedPackages = desc.getExportPackages();
				for(ExportPackageDescription packageDesc : exportedPackages) {
					if(packageDesc.getName().equals(packageName)) {
						result.add(desc.getName());
					}
				}
			}
		}
		catch(Exception e) {
			return result;
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
