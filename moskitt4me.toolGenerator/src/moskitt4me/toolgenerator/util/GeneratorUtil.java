package moskitt4me.toolgenerator.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import moskitt4me.repositoryClient.core.util.RepositoryClientUtil;
import moskitt4me.repositoryClient.core.util.RepositoryLocation;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.epf.library.LibraryService;
import org.eclipse.epf.uma.MethodLibrary;
import org.eclipse.epf.uma.ToolMentor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GeneratorUtil {

	public static List<IProject> projects = new ArrayList<IProject>();
	public static List<IProject> permanentProjects = new ArrayList<IProject>();
	public static List<String> internalPlugins = new ArrayList<String>();
	
	public static String technicalFragmentsPath = "";
	
	public static void copyFile(InputStream in, String destinationFile) throws Exception {

		String dir = replaceLastSegment(destinationFile, "");
		File directory = new File(dir);
		if(!directory.exists()) {
			directory.mkdirs();
		}
		
		File dest = new File(destinationFile);
		if (!dest.exists()) {
			dest.createNewFile();
		}

		OutputStream out = new FileOutputStream(dest);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}

		in.close();
		out.close();
	}

	public static void extractZipFile(String folder, String zipName)
			throws Exception {

		ZipFile zipFile = new ZipFile(new File(folder + "/" + zipName));

		Enumeration entries = zipFile.entries();

		while (entries.hasMoreElements()) {
			Object obj = entries.nextElement();
			if (obj instanceof ZipEntry) {
				ZipEntry entry = (ZipEntry) obj;

				InputStream eis = zipFile.getInputStream(entry);
				byte[] buffer = new byte[1024];
				int bytesRead = 0;

				File f = new File(folder + "/" + entry.getName());

				if (entry.isDirectory()) {
					f.mkdirs();
					eis.close();
					continue;
				} else {
					f.getParentFile().mkdirs();
					f.createNewFile();
				}

				FileOutputStream fos = new FileOutputStream(f);

				while ((bytesRead = eis.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}

				if (eis != null) {
					eis.close();
				}
				
				if (fos != null) {
					fos.close();
				}
			}
		}
		
		zipFile.close();
	}
	

	public static String createFolder(String path, int index) {

		String newPath = path + index;

		String createdFolderPath = "";

		File folder = new File(newPath);
		if (!folder.exists()) {
			folder.mkdirs();
			createdFolderPath = newPath;
		} else {
			createdFolderPath = createFolder(path, index + 1);
		}

		return createdFolderPath;
	}

	public static void deleteFolder(String path) {

		File folder = new File(path);
		
		if(folder.isDirectory()) {
			File[] files = folder.listFiles();
			if(files.length > 0) {
				//Empty the folder
				for(File f : files) {
					if(!f.isDirectory()) {
						f.delete();
					}
					else {
						deleteFolder(f.getPath());
					}
				}
			}
			//Folder is now empty
			folder.delete();
		}
	}
	
	public static String replaceLastSegment(String uri, String newSegment) {

		StringTokenizer tokenizer = new StringTokenizer(uri, "/");

		String newUri = "";
		int numTokens = tokenizer.countTokens();

		for (int i = 0; i < numTokens - 1; i++) {
			newUri += tokenizer.nextToken() + "/";
		}

		return (newUri + newSegment);
	}
	
	public static IProject importProjectIntoWorkspace(String path) throws Exception {
		
		IProjectDescription description = null;

		try {
			
			description = ResourcesPlugin.getWorkspace()
					.loadProjectDescription(new Path(path + "/.project"));
		
		} catch (Exception e) {

		}

		if (description != null) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(description.getName());
			project.create(description, null);
			project.open(null);

			return project;
		}

		return null;
	}
	
	public static IProject createPluginProject(String name, Shell shell) throws Exception {
		
		IProjectDescription description = ResourcesPlugin.getWorkspace()
				.newProjectDescription(name);

		description.setName(name);

		//Natures
		
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);

		newNatures[natures.length] = "org.eclipse.pde.PluginNature";
		
		description.setNatureIds(newNatures);

		//Builders
		
		ICommand builderCommand1 = description.newCommand();
		builderCommand1.setBuilderName("org.eclipse.pde.ManifestBuilder");
		
		ICommand builderCommand2 = description.newCommand();
		builderCommand2.setBuilderName("org.eclipse.pde.SchemaBuilder");
		
		ICommand[] oldCommands = description.getBuildSpec();
		ICommand[] newCommands = new ICommand[oldCommands.length + 2];
		
		System.arraycopy(oldCommands, 0, newCommands, 0, oldCommands.length);
		newCommands[oldCommands.length] = builderCommand1;
		newCommands[oldCommands.length + 1] = builderCommand2;
		
		description.setBuildSpec(newCommands);
		
		//Project creation
		
		CreateProjectOperation op = new CreateProjectOperation(description,
				"New Plugin Project");
		
//		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		
		PlatformUI.getWorkbench().getOperationSupport().getOperationHistory()
				.execute(op, new NullProgressMonitor(),
						WorkspaceUndoUtil.getUIInfoAdapter(shell));
		
		Object[] objects = op.getAffectedObjects();
		if(objects.length == 1 && objects[0] instanceof IProject) {
			return (IProject) objects[0];
		}
		
		return null;
		
	}
	
	public static IProject createFeatureProject(String name, Shell shell) throws Exception {
		
		IProjectDescription description = ResourcesPlugin.getWorkspace()
				.newProjectDescription(name);

		description.setName(name);

		//Natures
		
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);

		newNatures[natures.length] = "org.eclipse.pde.FeatureNature";
		
		description.setNatureIds(newNatures);

		//Builders
		
		ICommand builderCommand = description.newCommand();
		builderCommand.setBuilderName("org.eclipse.pde.FeatureBuilder");
		
		ICommand[] oldCommands = description.getBuildSpec();
		ICommand[] newCommands = new ICommand[oldCommands.length + 1];
		
		System.arraycopy(oldCommands, 0, newCommands, 0, oldCommands.length);
		newCommands[oldCommands.length] = builderCommand;
		
		description.setBuildSpec(newCommands);
		
		//Project creation
		
		CreateProjectOperation op = new CreateProjectOperation(description,
				"New Feature Project");
		
//		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		
		PlatformUI.getWorkbench().getOperationSupport().getOperationHistory()
				.execute(op, new NullProgressMonitor(),
						WorkspaceUndoUtil.getUIInfoAdapter(shell));
		
		Object[] objects = op.getAffectedObjects();
		if(objects.length == 1 && objects[0] instanceof IProject) {
			return (IProject) objects[0];
		}
		
		return null;
	}
	
	public static void copyMethodLibrary(File sourceLocation, File targetLocation,
			String tfragmentsPath) throws Exception {
		
		if(!sourceLocation.toURI().toString().contains(tfragmentsPath)) {
			if (sourceLocation.isDirectory()) {
				if (!targetLocation.exists()) {
					targetLocation.mkdirs();
				}
				String[] children = sourceLocation.list();
				for (int i = 0; i < children.length; i++) {
					File source = new File(sourceLocation, children[i]);
					File target = new File(targetLocation, children[i]);
					copyMethodLibrary(source, target, tfragmentsPath);
				}
			}
			else {
		        InputStream in = new FileInputStream(sourceLocation);
		        OutputStream out = new FileOutputStream(targetLocation);

		        byte[] buf = new byte[1024];
		        int len;
		        while ((len = in.read(buf)) > 0) {
		            out.write(buf, 0, len);
		        }
		        in.close();
		        out.close();
		    }
		}
	}
	
	public static void copyDirectory(File sourceLocation, File targetLocation) throws Exception {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdirs();
			}
			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				File source = new File(sourceLocation, children[i]);
				File target = new File(targetLocation, children[i]);
				copyDirectory(source, target);
			}
		}
		else {
	        InputStream in = new FileInputStream(sourceLocation);
	        OutputStream out = new FileOutputStream(targetLocation);

	        byte[] buf = new byte[1024];
	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
	        in.close();
	        out.close();
	    }
	}
	
	public static void copyFragmentPluginsIntoFolder(RepositoryLocation location, 
			String fileName, List<String> checkedFragments, String folder) {
		
		if(!checkedFragments.contains(fileName)) {
			
			checkedFragments.add(fileName);
			
			try {
				RepositoryClientUtil.downloadFragment(location, fileName, folder);
				RepositoryClientUtil.extractZipFile(folder, fileName);
				
				List<String> dependencies = getDependencies(folder + "/rasset.xml");
				List<String> plugins = getInternalPlugins(folder + "/rasset.xml");
				for(String plugin : plugins) {
					if(!GeneratorUtil.internalPlugins.contains(plugin)) {
						GeneratorUtil.internalPlugins.add(plugin);
					}
				}
				
				File f = new File(folder + "/" + fileName);
				if(f.exists()) f.delete();
				
				File f2 = new File(folder + "/rasset.xml");
				if(f2.exists()) f2.delete();
				
				for(String dependency : dependencies) {
					copyFragmentPluginsIntoFolder(location, dependency, checkedFragments, folder);
				}
			}
			catch(Exception e) {
				
			}
		}
	}
	
	public static List<ToolMentor> getToolMentors() {
		
		List<ToolMentor> toolmentors = new ArrayList<ToolMentor>();
		MethodLibrary lib = LibraryService.getInstance().getCurrentMethodLibrary();
		
		if(lib != null) {
			
			TreeIterator<EObject> it = lib.eAllContents();
			while(it.hasNext()) {
				EObject next = it.next();
				if(next instanceof ToolMentor) {
					toolmentors.add((ToolMentor) next);
				}
			}
			
		}
		
		return toolmentors;
	}
	
	private static List<String> getDependencies(String manifestPath) throws Exception {
		
		List<String> dependencies = new ArrayList<String>();
		
		File manifest = new File(manifestPath);
		
		if(manifest.exists()) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(manifest);
			
			if(document != null) {
				NodeList dependencyNodes = document.getElementsByTagName("dependency");
				int nodes = dependencyNodes.getLength();
				for(int i = 0; i < nodes; i++) {
					Node dependency = dependencyNodes.item(i);
					String dep = dependency.getAttributes().getNamedItem("value").getNodeValue();
					if(!dependencies.contains(dep)) {
						dependencies.add(dep);
					}
				}
			}
		}
		
		return dependencies;
	}
	
	private static List<String> getInternalPlugins(String manifestPath) throws Exception {
		
		List<String> plugins = new ArrayList<String>();
		
		File manifest = new File(manifestPath);
		
		if(manifest.exists()) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(manifest);
			
			if(document != null) {
				NodeList dependencyNodes = document.getElementsByTagName("plugin");
				int nodes = dependencyNodes.getLength();
				for(int i = 0; i < nodes; i++) {
					Node dependency = dependencyNodes.item(i);
					String dep = dependency.getAttributes().getNamedItem("value").getNodeValue();
					if(!plugins.contains(dep)) {
						plugins.add(dep);
					}
				}
			}
		}
		
		return plugins;
	}
	
	public static IProject getProject(String name) {

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();

		for (IProject project : projects) {
			if (project.getName().equals(name)) {
				return project;
			}
		}
		return null;
	}
}
