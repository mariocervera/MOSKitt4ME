package moskitt4me.repositoryClient.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import moskitt4me.repositoryClient.core.views.RepositoriesView;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RepositoryClientUtil {

	public static FTPClient connect(RepositoryLocation location, boolean showErrors) {
		
		FTPClient client = new FTPClient();
		String errorMessage = "";
		
		try {
			client.connect(location.getHost());
			boolean ok = client.login(location.getUser(), location.getPassword());
			if(ok) {
				boolean ok2 = client.changeWorkingDirectory(location.getRepositoryPath());
				if(!ok2) {
					client = null;
					errorMessage = "Incorrect repository path";
				}
			}
			else {
				client = null;
				errorMessage = "Incorrect user name and password";
			}
		}
		catch(Exception e) {
			client = null;
			errorMessage = "Unknown host";
		}
		
		if(!errorMessage.equals("") && showErrors) {
			MessageDialog.openError(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Connection failed",
					errorMessage);
		}
		
		return client;
	}
	
	public static void disconnect(FTPClient client) {
		
		try {
			client.disconnect();
		}
		catch(Exception e) {
			
		}
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
	
	public static void removeFolder(File f) {

		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					removeFolder(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		f.delete();
	}
	
	public static String downloadFragment(RepositoryLocation location,
			String fileName) throws IOException {
		
		FTPClient client = RepositoryClientUtil.connect(location, false);
		String tempDir = "";
		
		if(client != null) {
			
			String eclipseInstallationDirectory = Platform.getInstallLocation()
				.getURL().getPath();

			tempDir = RepositoryClientUtil.createFolder(
				eclipseInstallationDirectory + "tmp", 0);
			
			//Download the file
			
			String destination = tempDir + "/" + fileName;
			FileOutputStream fos = new FileOutputStream(destination);
			client.retrieveFile(fileName, fos);
			fos.close();
			
			//Disconnect the client
			
			RepositoryClientUtil.disconnect(client);
		}
		
		return tempDir;
	}
	
	public static void downloadFragment(RepositoryLocation location,
			String fileName, String folder) throws IOException {
		
		FTPClient client = RepositoryClientUtil.connect(location, false);
		
		if(client != null) {
			
			//Download the file
			
			String destination = folder + "/" + fileName;
			FileOutputStream fos = new FileOutputStream(destination);
			client.retrieveFile(fileName, fos);
			fos.close();
			
			//Disconnect the client
			
			RepositoryClientUtil.disconnect(client);
		}
	}
	
	public static boolean isTechnicalFragment(String type) {
		
		return (type.equals("Graphical Editor") || type.equals("Meta-Model") ||
				type.equals("Form-based Editor") || type.equals("Model transformation") ||
				type.equals("Guidance") || type.equals("External Tool") ||
				type.equals("Internal Tool") || type.equals("Others"));
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
	
	public static void addFileToZip(File file, ZipOutputStream zos) throws Exception {
		
		zos.putNextEntry(new ZipEntry(file.getName()));

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

		long bytesRead = 0;
		byte[] bytesIn = new byte[1024];
		int read = 0;

		while ((read = bis.read(bytesIn)) != -1) {
			zos.write(bytesIn, 0, read);
			bytesRead += read;
		}

		zos.closeEntry();
		bis.close();
	}
	
	public static void addFolderToZip(File folder, String parentFolder, ZipOutputStream zos) throws Exception {
		
	    for (File file : folder.listFiles()) {
	        
	    	if (file.isDirectory()) {
	    		zos.putNextEntry(new ZipEntry(parentFolder + file.getName() + "/"));
	    		zos.closeEntry();
	    		addFolderToZip(file, parentFolder + file.getName() + "/", zos);
	            continue;
	        }
	    	else {
	    		zos.putNextEntry(new ZipEntry(parentFolder + file.getName()));
		        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		 
		        long bytesRead = 0;
		        byte[] bytesIn = new byte[1024];
		        int read = 0;
		 
		        while ((read = bis.read(bytesIn)) != -1) {
		            zos.write(bytesIn, 0, read);
		            bytesRead += read;
		        }
		        
		        zos.closeEntry();
		        bis.close();
	    	}
	    }
	}
	
	public static void copyDirectory(File sourceLocation, File targetLocation) throws Exception {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdirs();
				String[] children = sourceLocation.list();
				for (int i = 0; i < children.length; i++) {
					File source = new File(sourceLocation, children[i]);
					File target = new File(targetLocation, children[i]);
					copyDirectory(source, target);
				}
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
	
	public static RepositoriesView getRepositoriesView() {
		
		try {
			IViewPart viewPart = PlatformUI.getWorkbench().
			getActiveWorkbenchWindow().getActivePage().findView(RepositoriesView.RepositoriesViewId);
			
			if(viewPart instanceof RepositoriesView) {
				return (RepositoriesView) viewPart;
			}
		}
		catch(Exception e) {
			return null;
		}
		
		return null;
	}
	
	public static IProject createFeatureProject(TechnicalFragment tf) throws Exception {
		
		String featureName = tf.getName() + ".feature";
		
		IProjectDescription description = ResourcesPlugin.getWorkspace()
				.newProjectDescription(featureName);

		description.setName(featureName);

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
		
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		
		PlatformUI.getWorkbench().getOperationSupport().getOperationHistory()
				.execute(op, new NullProgressMonitor(),
						WorkspaceUndoUtil.getUIInfoAdapter(shell));
		
		Object[] objects = op.getAffectedObjects();
		if(objects.length == 1 && objects[0] instanceof IProject) {
			IProject feature = (IProject) objects[0];
			
			addBuildProperties(feature);
			addFeatureXML(feature, tf);
			
			feature.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			
			return feature;
		}
		
		return null;
	}
	
	private static void addBuildProperties(IProject feature) throws Exception {
		
		String featureLocation = feature.getLocation().toString();
		File f = new File(featureLocation + "/build.properties");
		if(!f.exists()) {
			f.createNewFile();
			BufferedWriter output = new BufferedWriter(new FileWriter(f));
			output.write("bin.includes = feature.xml\n");
			output.close();
		}
	}
	
	private static void addFeatureXML(IProject feature, TechnicalFragment tf) throws Exception {
		
		String featureLocation = feature.getLocation().toString();
		List<IProject> plugins = tf.getPlugins();
		File f = new File(featureLocation + "/feature.xml");
		if(!f.exists()) {
			f.createNewFile();
			BufferedWriter output = new BufferedWriter(new FileWriter(f));
			output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			output.write("<feature id=\"" + feature.getName() + "\" label=\"Feature\" version=\"1.0.0.qualifier\">\n");
			output.write("<description url=\"http://www.example.com/description\"> [Enter Feature Description here.] </description>\n");
			output.write("<copyright url=\"http://www.example.com/copyright\"> [Enter Copyright Description here.] </copyright>\n");
			output.write("<license url=\"http://www.example.com/license\"> [Enter License Description here.] </license>\n");
			for(IProject project : plugins) {
				output.write("<plugin id=\"" + project.getName() + "\" download-size=\"0\" install-size=\"0\" version=\"0.0.0\" unpack=\"false\"/>\n");
			}
			output.write("</feature>\n");
			output.close();
		}
	}
	
	public static String createRassetXMLFile(TechnicalFragment tf, String folder) throws Exception {
		
		String manifestPath = folder + "/rasset.xml";
		File f = new File(manifestPath);
		if(!f.exists()) {
			f.createNewFile();
			
			String type = tf.getType();
			String origin = type.equals("Others") ? "no origin" : tf.getOrigin();
			String objective = type.equals("Others") ? "no objective" : tf.getObjective();
			String input = type.equals("Others") ? "no input" : tf.getInput();
			String output = type.equals("Others") ? "no output" : tf.getOutput();
			String toolId = getToolId(tf);
			String description = "";
			if(type.equals("External Tool")) {
				description = ((ExternalToolFragment)tf).getDescription();
			}
			else if(type.equals("Internal Tool")) {
				description = ((InternalToolFragment)tf).getDescription();
			}
			else {
				description = "no description";
			}
			
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(f));
			bwriter.write("<asset xsi:schemaLocation=\"DefaultprofileXML.xsd\" name=\"" + tf.getName() + "\" id=\"1\">\n");
			bwriter.write("<type value=\"" + type + "\"></type>\n");
			bwriter.write("<origin value=\"" + origin + "\"></origin>\n");
			bwriter.write("<objective value=\"" + objective + "\"></objective>\n");
			bwriter.write("<input value=\"" + input + "\"></input>\n");
			bwriter.write("<output value=\"" + output + "\"></output>\n");
			if(toolId != null && !toolId.equals("")) {
				bwriter.write("<toolID value=\"" + toolId + "\"></toolID>\n");
			}
			bwriter.write("<description>" + description + "</description>\n");
			List<TechnicalFragment> dependencies = tf.getDependencies();
			for(TechnicalFragment dependency : dependencies) {
				bwriter.write("<dependency value=\"" + dependency.getName() + ".ras.zip\"></dependency>\n");
			}
			if(type.equals("Internal Tool")) {
				List<String> pluginIds = ((InternalToolFragment)tf).getPluginIds();
				for(String pluginId : pluginIds) {
					bwriter.write("<plugin value=\"" + pluginId + "\"></plugin>\n");
				}
			}
			bwriter.write("</asset>\n");
			bwriter.close();
		}
		
		return manifestPath;
	}
	
	private static String getToolId(TechnicalFragment tf) {
		
		String type = tf.getType();
		
		if(type.equals("Others") || type.equals("Internal Tool")) {
			return "";
		}
		if(type.equals("External Tool")) {
			ExternalToolFragment etf = (ExternalToolFragment)tf;
			String fileExtension = etf.getFileExtension();
			if(!fileExtension.startsWith(".")) {
				fileExtension = "." + fileExtension;
			}
			return fileExtension;
		}
		
		List<IProject> plugins = tf.getPlugins();
		
		for(IProject plugin : plugins) {
			String pluginLocation = plugin.getLocation().toString();
			String pluginXMLLocation = pluginLocation + "/plugin.xml";
			
			Document pluginXMLDocument = getDocument(pluginXMLLocation);
			if(pluginXMLDocument != null) {
				
				String extensionPointId = "";
				String elementId = "";
				String attribute = "";
				
				if(type.equals("Graphical Editor") || type.equals("Meta-Model") ||
						type.equals("Form-based Editor")) {
					extensionPointId = "org.eclipse.ui.newWizards";
					elementId = "wizard";
					attribute = "id";
				}
				else if(type.equals("Model transformation")) {
					extensionPointId = "es.cv.gvcase.trmanager.transformation";
					elementId = "transformation";
					attribute = "id";
				}
				else if(type.equals("Guidance")) {
					extensionPointId = "org.eclipse.epf.authoring.ui.helpcontextprovider";
					elementId = "helpContextProviderDesc";
					attribute = "idContext";
				}
				
				if(!extensionPointId.equals("")) {
					NodeList extension = pluginXMLDocument.getElementsByTagName("extension");
					int length = extension.getLength();
					for(int i = 0 ; i < length ; i++) {
						Node extensionNode = extension.item(i);
						if(extensionNode.getAttributes().getNamedItem("point").getNodeValue().equals(extensionPointId)
								&& extensionNode.getNodeType() == Node.ELEMENT_NODE) {
							 Element extensionElement = (Element)extensionNode;
							 NodeList elements = extensionElement.getElementsByTagName(elementId);
							 if(elements.getLength() > 0) {
								 Node element = elements.item(0);
								 String attValue = element.getAttributes().getNamedItem(attribute).getNodeValue();
								 if(type.equals("Guidance")) {
									 return plugin.getName() + "." + attValue;
								 }
								 else {
									 return attValue;
								 }
							 }
						}
					}
				}
			}
		}
		
		return "";
	}
	
	public static Document getDocument(String path) {

		Document document = null;

		try {
			File f = new File(path);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(f);

		} catch (Exception e) {
		
		}

		return document;
	}
	
	public static IProject getPluginProject(String projectName) {
		
		List<IProject> plugins = getPluginProjects();
		
		for(IProject plugin : plugins) {
			if(plugin.getName().equals(projectName)) {
				return plugin;
			}
		}
		return null;
	}
	
	private static List<IProject> getPluginProjects() {
		
		List<IProject> pluginProjects = new ArrayList<IProject>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		IProject[] projects = root.getProjects();
		
		for(IProject project : projects) {
			try {
				if(project.hasNature("org.eclipse.pde.PluginNature")) {
					pluginProjects.add(project);
				}
			}
			catch(CoreException e) {
				
			}
		}
		return pluginProjects;
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
	
	public static List<TechnicalFragment> parseRassetXML(String path, TechnicalFragment tf) {
		
		List<TechnicalFragment> dependencies = new ArrayList<TechnicalFragment>();
		
		Document doc = getDocument(path);
		if(doc != null) {
			if(doc != null) {
				Node type = doc.getElementsByTagName("type").item(0);
				Node origin = doc.getElementsByTagName("origin").item(0);
				Node objective = doc.getElementsByTagName("objective").item(0);
				Node input = doc.getElementsByTagName("input").item(0);
				Node output = doc.getElementsByTagName("output").item(0);
				
				tf.setType(type.getAttributes().getNamedItem("value").getNodeValue());
				tf.setOrigin(origin.getAttributes().getNamedItem("value").getNodeValue());
				tf.setObjective(objective.getAttributes().getNamedItem("value").getNodeValue());
				tf.setInput(input.getAttributes().getNamedItem("value").getNodeValue());
				tf.setOutput(output.getAttributes().getNamedItem("value").getNodeValue());
				
				NodeList deps = doc.getElementsByTagName("dependency");
				int length = deps.getLength();
				for(int i = 0 ; i < length ; i++) {
					Node depNode = deps.item(i);
					String depName = depNode.getAttributes().getNamedItem("value").getNodeValue();
					String extension = ".ras.zip";
					String assetName = depName.substring(0, depName.length() - extension.length());
					TechnicalFragment asset = new TechnicalFragment(assetName, "","","","","");
					dependencies.add(asset);
				}
			}
		}
		
		return dependencies;
	}
	
	public static void setInitialLocationType(RepositoryLocation location) {
		
		FTPClient client = RepositoryClientUtil.connect(location, false);
		
		if(client != null) {
			
			String tempDir = "";
			
			try {
				String eclipseInstallationDirectory = Platform.getInstallLocation().getURL().getPath();
				tempDir = RepositoryClientUtil.createFolder(eclipseInstallationDirectory + "tmp", 0);
			
				FTPFile[] files = client.listFiles();
				
				if(files.length > 0){
					String fileName = files[0].getName();
					String destination = tempDir + "/" + fileName;
					FileOutputStream fos = new FileOutputStream(destination);
					client.retrieveFile(fileName, fos);
					fos.close();
					
					extractZipFile(tempDir, fileName);
					
					Document doc = getDocument(tempDir + "/rasset.xml");
					if(doc != null) {
						Node ntype = doc.getElementsByTagName("type").item(0);
						String type = ntype.getAttributes().getNamedItem("value").getNodeValue();
						if(type != null) {
							if(isTechnicalFragment(type)) {
								location.setType("Technical");
							}
							else {
								location.setType("Conceptual");
							}
						}
					}
				}
				else {
					location.setType("Empty");
				}
			}
			catch (Exception e) {
				
			}
			finally {
				RepositoryClientUtil.disconnect(client);
				if(location.getType().equals("")) {
					location.setType("Empty");
				}
				if(!tempDir.equals("")) {
					RepositoryClientUtil.removeFolder(new File(tempDir));
				}
			}
		}	
	}
	
}
