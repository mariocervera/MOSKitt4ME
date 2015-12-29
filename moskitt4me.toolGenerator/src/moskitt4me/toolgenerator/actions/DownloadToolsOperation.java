package moskitt4me.toolgenerator.actions;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import moskitt4me.repositoryClient.core.util.RepositoryLocation;
import moskitt4me.toolgenerator.Activator;
import moskitt4me.toolgenerator.util.GeneratorUtil;
import moskitt4me.toolgenerator.util.PluginsGenerator;
import moskitt4me.toolgenerator.util.TemplatesUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.epf.library.LibraryService;
import org.eclipse.epf.uma.MethodElementProperty;
import org.eclipse.epf.uma.MethodLibrary;
import org.eclipse.epf.uma.ToolMentor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.util.StringTokenizer;

public class DownloadToolsOperation implements IRunnableWithProgress {

	private Shell shell;
	private String folder1;
	private String folder2;
	private String folder3;
	int result;
	
	public DownloadToolsOperation(Shell shell, String folder1,
			String folder2, String folder3) {
		
		this.shell = shell;
		this.folder1 = folder1;
		this.folder2 = folder2;
		this.folder3 = folder3;
		this.result = 1;
	}
	
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {

		try {
			monitor.beginTask("Downloading tools ...", 100);
			
			//Download technical fragments from repository
			
			downloadTools(folder1, folder3, monitor);
			
			// Import Project Manager Component, Graphiti, and TrManager into workspace

			monitor.subTask("Importing projects into workspace ...");
			monitor.worked(30);
			
			importPMCandGraphitiandTrmanager(folder2, GeneratorUtil.projects);

			// Import Technical Fragment Plugins into workspace

			importTechnicalFragmentPlugins(folder1, GeneratorUtil.projects, GeneratorUtil.permanentProjects);

			// Import additional plug-ins into workspace

			generateAdditionalPlugins(GeneratorUtil.projects, shell);
			
			monitor.done();
			
			result = 0;
		}
		catch(Exception e) {
			result = 1;
		}
	}
	
	public int getResult() {
		return result;
	}
	
	/*
	 * Downloads the technical fragments from the repository
	 */
	private void downloadTools(final String fragmentsFolder, String reportFolder,
			IProgressMonitor monitor) throws Exception { 
      
		List<String> checkedFragments = new ArrayList<String>();
		List<ToolMentor> toolMentors = GeneratorUtil.getToolMentors();
		
		int numberToolMentors = countTools(toolMentors);
		int numberExternalTools = 0;
		
		monitor.worked(5);
		
		File reportFile = new File(reportFolder + "/generationReport.txt");
		reportFile.createNewFile();
		FileWriter writer = new FileWriter(reportFile);
		
		writer.write("This file provides information about the tools that could not be installed in the CASE environment.\n");
		
		for(ToolMentor tm : toolMentors) {
			String host = null;
			String repositoryPath = null;
			String user = null;
			String password = null;
			String fragmentFileName = null;
			String description = null;
			String type = "";
			
			for(MethodElementProperty p : tm.getMethodElementProperty()) {
				if(p.getName().equals("host")) {
					host = p.getValue();
				}
				else if(p.getName().equals("repositoryPath")) {
					repositoryPath = p.getValue();
				}
				else if(p.getName().equals("user")) {
					user = p.getValue();
				} 
				else if(p.getName().equals("password")) {
					password = p.getValue();
				} 
				else if(p.getName().equals("fragmentFileName")) {
					fragmentFileName = p.getValue();
				}
				else if(p.getName().equals("description")) {
					description = p.getValue();
				}
				else if(p.getName().equals("type")) {
					type = p.getValue();
				}
			}
			
			if (host != null && repositoryPath != null && user != null
					&& fragmentFileName != null) {
				
				RepositoryLocation location = new RepositoryLocation(
						host, repositoryPath, user, password);
				
				monitor.subTask("Downloading " + fragmentFileName + "...");
				
				GeneratorUtil.copyFragmentPluginsIntoFolder(location,
						fragmentFileName, checkedFragments, fragmentsFolder);
				
				if(description != null && !description.equals("") && type.equals("External Tool")) {
					writer.write("\n------------ " + fragmentFileName +  " ------------\n\n");
					writer.write(description + "\n\n");
					numberExternalTools++;
				}
				
				monitor.worked(45/numberToolMentors);
			}
		}
		
		int installedTools = numberToolMentors - numberExternalTools;
		float percentage = 100;
		if(numberToolMentors > 0) {
			percentage = (float) installedTools / numberToolMentors * 100;
		}
		
		writer.write("----------\nNumber of tools that have been successfully installed in the CASE environment: "
				+ installedTools + " of " + numberToolMentors + "(" + format(percentage) + "%)\n----------");
		writer.close();
	}
	
	/*
	 * Extracts the Project Manager Component, Graphiti, and the Transformation Manager
	 * into the folder and imports the projects into the workspace
	 */
	private void importPMCandGraphitiandTrmanager(
			String folder, List<IProject> projects) throws Exception {
		
		URL pmcURL = Platform.getBundle(Activator.PLUGIN_ID).getEntry(
				"/components/pmc.zip");
		
		URL graphitiURL = Platform.getBundle(Activator.PLUGIN_ID).getEntry(
			"/components/graphiti.zip");
		
		URL trManagerURL = Platform.getBundle(Activator.PLUGIN_ID).getEntry(
			"/components/trmanager.zip");

		//Copy files into folder
		
		InputStream inputStream = pmcURL.openStream();
		InputStream inputStream2 = graphitiURL.openStream();
		InputStream inputStream3 = trManagerURL.openStream();
		
		String destinationFile = folder + "/pmc.zip";
		String destinationFile2 = folder + "/graphiti.zip";
		String destinationFile3 = folder + "/trmanager.zip";
		
		GeneratorUtil.copyFile(inputStream, destinationFile);
		GeneratorUtil.copyFile(inputStream2, destinationFile2);
		GeneratorUtil.copyFile(inputStream3, destinationFile3);
		
		//Extract zip files
		
		GeneratorUtil.extractZipFile(folder, "pmc.zip");
		GeneratorUtil.extractZipFile(folder, "graphiti.zip");
		GeneratorUtil.extractZipFile(folder, "trmanager.zip");
		
		//Import plugins into workspace
		
		File folder2 = new File(folder);
		File[] files = folder2.listFiles();
		
		for(File file : files) {
			if(file.isDirectory()) {
				IProject project = GeneratorUtil.importProjectIntoWorkspace(file.getAbsolutePath());
				if(project != null) {
					projects.add(project);
				}
			}
		}
	}
	
	private void importTechnicalFragmentPlugins(String folder,
			List<IProject> projects, List<IProject> permanentProjects) throws Exception {
		
		File tfragmentsFolder = new File(folder);
		if(tfragmentsFolder.isDirectory()) {
			File[] files = tfragmentsFolder.listFiles();
			if(files != null) {
				for(File f : files) {
					if (f.isDirectory()){
						IProject project = GeneratorUtil.getProject(f.getName());
						if(project != null) {
							permanentProjects.add(project);
						}
						else {
							project = GeneratorUtil.importProjectIntoWorkspace(f.getAbsolutePath());
						}
						if (project != null) {
							projects.add(project);
						} 
					}
				}
			}
		}
	}
	
	/*
	 * This method generates the plug-in defining the eclipse product, the feature containing
	 * this plug-in, the feature containing the dependencies, the plug-in containing the
	 * method library, and the feature containing this plug-in.
	 */
	private void generateAdditionalPlugins(List<IProject> projects, Shell shell) throws Exception {
		
		//Create two empty plug-in projects in the workspace
		
		IProject rcpPluginProject = GeneratorUtil.createPluginProject(TemplatesUtil.definingBundleName(), shell);
		IProject mDefPluginProject = GeneratorUtil.createPluginProject(TemplatesUtil.methodDefinitionBundleName(), shell);
		
		//Create four empty feature projects in the workspace
		
		IProject rcpFeatureProject = GeneratorUtil.createFeatureProject(TemplatesUtil.definingBundleName() + ".feature", shell);
		IProject dependenciesFeatureProject = GeneratorUtil.createFeatureProject(TemplatesUtil.definingBundleName() + ".dependencies.feature", shell);
		IProject mDefFeatureProject = GeneratorUtil.createFeatureProject(TemplatesUtil.methodDefinitionBundleName() + ".feature", shell);
		IProject tFragmentsFeatureProject = GeneratorUtil.createFeatureProject(TemplatesUtil.technicalFragmentsFeatureName(), shell);
		
		if(rcpPluginProject != null && mDefPluginProject != null && rcpFeatureProject != null
				&& dependenciesFeatureProject != null && mDefFeatureProject != null &&
				tFragmentsFeatureProject != null) {
			
			String rcpPluginProjectLocation = rcpPluginProject.getLocation().toString();
			String mDefPluginProjectLocation = mDefPluginProject.getLocation().toString();
			
			String rcpFeatureProjectLocation = rcpFeatureProject.getLocation().toString();
			String dependenciesFeatureProjectLocation = dependenciesFeatureProject.getLocation().toString();
			String mDefFeatureProjectLocation = mDefFeatureProject.getLocation().toString();
			String tFragmentsFeatureProjectLocation = tFragmentsFeatureProject.getLocation().toString();
			
			//Generate the textual content of the projects (.product file, MANIFEST.MF, feature.xml, etc.)
			
			MethodLibrary lib = LibraryService.getInstance().getCurrentMethodLibrary();
			if (lib != null) {
				URI libraryURI = lib.eResource().getURI();
				String libraryPath = libraryURI.toString().replaceFirst("file:", "");

				PluginsGenerator generator = new PluginsGenerator();
				generator.generatePlugins(libraryPath,
						rcpPluginProjectLocation, mDefPluginProjectLocation,
						rcpFeatureProjectLocation,
						dependenciesFeatureProjectLocation,
						mDefFeatureProjectLocation,
						tFragmentsFeatureProjectLocation);
			}
			
			//Add the remaining content to the Product Definition Plugin (icons and preference customization file)
			
			completeProductDefiningPlugin(rcpPluginProjectLocation);
			
			//Copy method library into method definition plug-in
			
			completeMethodDefinitionPlugin(mDefPluginProjectLocation);
			
			//Refresh projects
			
			rcpPluginProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			mDefPluginProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			rcpFeatureProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			dependenciesFeatureProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			mDefFeatureProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			tFragmentsFeatureProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			
			projects.add(rcpPluginProject);
			projects.add(mDefPluginProject);
			projects.add(rcpFeatureProject);
			projects.add(dependenciesFeatureProject);
			projects.add(mDefFeatureProject);
			projects.add(tFragmentsFeatureProject);
		}
	}
	
	private void completeProductDefiningPlugin(String location) throws Exception {
		
		String name = TemplatesUtil.definingBundleName();
		
		if(!name.equals("")) {
			
			Bundle bundle = Platform.getBundle(name);
			if(bundle != null) {
				
				//Window images
				
				String images = TemplatesUtil.windowImages();
				if(!images.equals("")) {
					StringTokenizer st = new StringTokenizer(images, ",");
					while(st.hasMoreTokens()) {
						String image = st.nextToken();
						URL imageURL = bundle.getEntry(image);
						InputStream imageStream = imageURL.openStream();
						GeneratorUtil.copyFile(imageStream, location + "/" + image);
					}
				}
				
				//About image
				
				String aboutImage = TemplatesUtil.aboutImage();
				
				if(!aboutImage.equals("")) {
					URL imageURL = bundle.getEntry(aboutImage);
					InputStream imageStream = imageURL.openStream();
					GeneratorUtil.copyFile(imageStream, location + "/" + aboutImage);
				}
				
				//Preference customization file
				
				String preference = TemplatesUtil.preferenceCustomization();
				
				if(!preference.equals("")) {
					URL preferenceURL = bundle.getEntry(preference);
					InputStream preferenceStream = preferenceURL.openStream();
					GeneratorUtil.copyFile(preferenceStream, location + "/" + preference);
				}
				
				//splash.bmp
				
				String splashLocation = TemplatesUtil.splashLocation();
				
				if(!splashLocation.equals("")) {
					URL splashURL = bundle.getEntry("splash.bmp");
					InputStream splashStream = splashURL.openStream();
					GeneratorUtil.copyFile(splashStream, location + "/splash.bmp");
				}
			}
		}
	}
	
	private void completeMethodDefinitionPlugin(String mDefPluginProjectLocation) throws Exception {
		
		MethodLibrary lib = LibraryService.getInstance().getCurrentMethodLibrary();
		
		if(lib != null) {
			
			URI libraryURI = lib.eResource().getURI();
			libraryURI = libraryURI.trimSegments(1);
			String lastSegment = libraryURI.lastSegment();
			URI tfragmentsURI = libraryURI.appendSegment("pluginsFromTechnicalFragments");
			String tfragmentsPath = tfragmentsURI.toString().replaceFirst("file:", "");
			String libraryPath = libraryURI.toString().replaceFirst("file:", "");
			
			File source = new File(libraryPath);
			File target = new File(mDefPluginProjectLocation + "/lib/" + lastSegment);
			
			GeneratorUtil.copyMethodLibrary(source, target, tfragmentsPath);
		}
	}
	
	private int countTools(List<ToolMentor> toolMentors) {
		
		int count = 0;
		
		for(ToolMentor tm : toolMentors) {
			for(MethodElementProperty p : tm.getMethodElementProperty()) {
				if(p.getName().equals("host")) {
					count++;
					break;
				}
			}
		}
		return count;
	}
	
	private String format(float number) {

		DecimalFormat df = new DecimalFormat("0.#");
		return df.format(number);
	}
}
