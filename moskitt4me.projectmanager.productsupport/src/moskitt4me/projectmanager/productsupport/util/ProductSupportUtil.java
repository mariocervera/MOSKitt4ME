package moskitt4me.projectmanager.productsupport.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.epf.uma.MethodElement;
import org.eclipse.epf.uma.MethodElementProperty;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.ToolMentor;
import org.eclipse.epf.uma.WorkProduct;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.IWizardDescriptor;

import es.cv.gvcase.trmanager.ui.TransformationUIManager;

/**
 * A class that provides a set of methods that facilitate the implementation of the third phase
 * of the Method Engineering lifecycle (i.e., the phase of method execution).
 *
 * @author Mario Cervera
 */
public class ProductSupportUtil {

	/*
	 * Checks whether a given task is automatic. Automatic tasks are associated to model transformations
	 */
	public static boolean isAutomatic(TaskDescriptor td, List<Task> tasks) {
		
		if (td != null) {
			Task task = td.getTask();
			
			if(task != null) {
				if(task.eIsProxy()) {
					task = getTask(task, tasks);
				}
				List<ToolMentor> tools = task.getToolMentors();
				if(tools.size() > 0) {
					ToolMentor tool = tools.get(0);
					String toolId = ProductSupportUtil.getPropertyValue(tool, "toolId");
					if(isTransformation(toolId)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/*
	 * Checks whether a given method product has already been created in a given project
	 */
	public static boolean existsProduct(WorkProduct product, IProject project) {
		
		boolean exists = false;
		
		try {
			String dir = project.getLocation().toString() + "/.method/";
			String methodPropertiesFileName = "methodProperties.txt";

			FileReader fr = new FileReader(dir + methodPropertiesFileName);
			BufferedReader br = new BufferedReader(fr);
			
			String s;
			
			while ((s = br.readLine()) != null) {
				
				if(s.startsWith("Resource: ")) {
					int index = s.indexOf(" - WorkProduct GUId: ");
					
					if(index >= 0) {
						String location = s.substring(10, index);
						String guid = s.substring(index + 21, s.length());
						
						if(product != null && product.getGuid().equals(guid)) {
							
							String projectLocation = project.getLocation().toString();
							IPath path = new Path(location.substring(projectLocation.length()));
							IResource res = project.findMember(path);
							
							if(res instanceof IFile) {
								exists = true;
								openFile((IFile)res);
							}
						}
					}
				}
			}
			
			fr.close();

			return exists;
			
		} catch (Exception e) {
			return exists;
		}
	}
	
	/*
	 * This method checks whether a given resource has already been stored in the "methodProperties.txt" file
	 */
	private static boolean alreadyStored(String resourcePath, WorkProduct product, IProject project) {

		boolean exists = false;
		
		try {
			String projectLocation = project.getLocation().toString();
			String dir = projectLocation + "/.method/";
			String methodPropertiesFileName = "methodProperties.txt";

			FileReader fr = new FileReader(dir + methodPropertiesFileName);
			BufferedReader br = new BufferedReader(fr);
			
			String resFullPath = projectLocation + resourcePath.replaceFirst("/" + project.getName(), "");
			
			String s;
			
			while ((s = br.readLine()) != null && !exists) {
				
				if(s.startsWith("Resource: ")) {
					int index = s.indexOf(" - WorkProduct GUId: ");
					
					if(index >= 0) {
						String location = s.substring(10, index);
						String guid = s.substring(index + 21, s.length());
						
						if(product != null && product.getGuid().equals(guid) &&
								resFullPath != null && resFullPath.equals(location)) {
							
							exists = true;
						}
					}
				}
			}
			
			fr.close();

			return exists;
			
		} catch (Exception e) {
			return exists;
		}
	}
	
	/*
	 * Invokes the execution of a model transformation. This method uses the Transformation 
	 * Manager of MOSKitt
	 */
	public static boolean launchTransformation(String transfId, IProject project,
			WorkProduct product) {
		
		if(!transfId.equals("")) {
			
			ResourceChangeListener l = new ResourceChangeListener(project, product);
			
			try {
				ResourcesPlugin.getWorkspace().addResourceChangeListener(l);
				
				TransformationUIManager.startTransformation(transfId);
				
				return true;
			}
			catch(Exception e){
				return false;
			}
			finally {
				ResourcesPlugin.getWorkspace().removeResourceChangeListener(l);
			}
		}
		
		return false;
	}
	
	/*
	 * Opens the wizard that enables the creation of a given product.
	 */
	public static boolean launchWizard(String wizardId, IProject project,
			WorkProduct product) {
		
		if(!wizardId.equals("")) {
			IWizardDescriptor wizardDescriptor = PlatformUI.getWorkbench()
					.getNewWizardRegistry()
					.findWizard(wizardId);
			
			if(wizardDescriptor != null) {
				
				ResourceChangeListener l = new ResourceChangeListener(project, product);
				
				try {
					ResourcesPlugin.getWorkspace().addResourceChangeListener(l);
					
					IWorkbenchWizard workbenchWizard = wizardDescriptor.createWizard();
					int returnCode = openWizard(workbenchWizard);
					
					if(returnCode == Window.OK) {
						return true;
					}
				}
				catch(Exception e){
					return false;
				}
				finally {
					ResourcesPlugin.getWorkspace().removeResourceChangeListener(l);
				}
			}
		}
		
		return false;
	}
	
	public static WorkProduct getWorkProduct(WorkProduct proxyWorkProduct, List<WorkProduct> products) {
		
		if(proxyWorkProduct != null && proxyWorkProduct.eIsProxy()) {
			URI proxyURI = ((InternalEObject)proxyWorkProduct).eProxyURI();
			
			for(WorkProduct product: products) {
				if(proxyURI.fragment().equals(product.getGuid())) {
					return product;
				}
			}
		}
		
		return proxyWorkProduct;
	}
	
	public static Task getTask(Task proxyTask, List<Task> tasks) {
		
		if(proxyTask != null && proxyTask.eIsProxy()) {
			URI proxyURI = ((InternalEObject)proxyTask).eProxyURI();
			
			for(Task task: tasks) {
				if(proxyURI.fragment().equals(task.getGuid())) {
					return task;
				}
			}
		}
		
		return proxyTask;
	}
	
	private static int openWizard(IWorkbenchWizard wizard) {
		
		wizard.init(PlatformUI.getWorkbench(), StructuredSelection.EMPTY);
		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WizardDialog dialog = new WizardDialog(parent, wizard);
		dialog.create();
		
		return dialog.open();
	}
	
	/*
	 * Opens the editor that enables the manipulation of a given file
	 */
	public static void openFile(IFile file) {
		 
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		try {
			IDE.openEditor(page, file, OpenStrategy.activateOnOpen());
		}
		catch (Exception e) {

		}
	}
	
	/*
	 * This method stores a given resource in the "methodProperties.txt" file
	 */
	private static void storeProduct(String resourcePath, WorkProduct product, IProject project) {
		
		if(resourcePath.startsWith("/" + project.getName() + "/.method/") ||
				resourcePath.startsWith("/" + project.getName() + "/.settings/") ||
				alreadyStored(resourcePath, product, project)) {
			
			return;
		}
		
		String projectLocation = project.getLocation().toString();
		String dir = projectLocation + "/.method/";
		String methodPropertiesFileName = "methodProperties.txt";
		
		try {
			FileWriter fstream = new FileWriter(dir + methodPropertiesFileName, true);
			BufferedWriter out = new BufferedWriter(fstream);
			
			String resFullPath = projectLocation + resourcePath.replaceFirst("/" + project.getName(), "");
			
			File f = new File(resFullPath);
			if(!f.isDirectory()) {
				String text = "Resource: " + resFullPath + " - WorkProduct GUId: " + product.getGuid() + "\n";
				out.write(text);
			}
			
			out.close();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public static void storeFile(String path, WorkProduct product, IProject project) {
		
		String projectLocation = project.getLocation().toString();
		String dir = projectLocation + "/.method/";
		String methodPropertiesFileName = "methodProperties.txt";
		
		try {
			FileWriter fstream = new FileWriter(dir + methodPropertiesFileName, true);
			BufferedWriter out = new BufferedWriter(fstream);
			
			String resFullPath = projectLocation + path;
			
			String text = "Resource: " + resFullPath + " - WorkProduct GUId: " + product.getGuid() + "\n";
			
			out.write(text);
			
			out.close();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	/* 
	 * A listener that stores resources in the "methodProperties.txt" file whenever 
	 * the resources are created
	 */
	private static class ResourceChangeListener implements IResourceChangeListener {

		private IProject project;
		private WorkProduct product;
		
		public ResourceChangeListener(IProject project, WorkProduct product) {
			
			this.project = project;
			this.product = product;
		}
		
		public void resourceChanged(IResourceChangeEvent event) {
			
			IResourceDelta delta = event.getDelta();
			
			storeProducts(delta);
		}

		private void storeProducts(IResourceDelta delta) {
						
			if(delta != null) {
				
				IResourceDelta[] children = delta.getAffectedChildren();
				
				if(children.length == 0) {
					storeProduct(delta.getFullPath().toString(), product, project);
				}
				
				for(IResourceDelta child : children) {
					storeProducts(child);
				}
			}
		}
	}
	
	public static List<String> getStoredProductGuids(IProject project) {
		
		List<String> result = new ArrayList<String>();
		
		String projectLocation = project.getLocation().toString();
		String dir = projectLocation + "/.method/";
		String methodPropertiesFileName = "methodProperties.txt";
		
		try {
			FileReader fr = new FileReader(dir + methodPropertiesFileName);
			BufferedReader br = new BufferedReader(fr);
			String s = "";
			
			while ((s = br.readLine()) != null) {
				
				if(s.startsWith("Resource: ")) {
					int index = s.indexOf(" - WorkProduct GUId: ");
					
					if(index >= 0) {
						String guid = s.substring(index + 21, s.length());
						
						if(!result.contains(guid)) {
							result.add(guid);
						}
					}
				}
			}
			
			fr.close();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		
		return result;
	}
	
	/*
	 * This method returns the value of a given MethodProperty for a given MethodElement
	 */
	public static String getPropertyValue(MethodElement me, String name) {
		
		List<MethodElementProperty> props = me.getMethodElementProperty();
		
		for(MethodElementProperty prop : props) {
			if(prop.getName().equals(name)) {
				return prop.getValue();
			}
		}
		
		return "";
	}
	
	public static boolean isRestarted(String taskId, List<String> cpIds,
			IProject project) {
		
		String projectLocation = project.getLocation().toString();
		String dir = projectLocation + "/.method/";
		String restartedTasksFileName = "restartedTasks.txt";
		
		if(cpIds == null) {
			cpIds = new ArrayList<String>();
			cpIds.add("dp");
		}
		
		try {
			FileReader fr = new FileReader(dir + restartedTasksFileName);
			BufferedReader br = new BufferedReader(fr);
			
			String s = "";
			for(String id : cpIds) {
				s += id + " ";
			}
			s += taskId;
			
			String s2 = "";
			
			while ((s2 = br.readLine()) != null) {
				if(s.equals(s2)) {
					return true;
				}
			}
			
			fr.close();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		
		return false;
	}
	
	public static void clearRepeatableTask(String taskId, List<String> cpIds,
			IProject project) {
		
		String projectLocation = project.getLocation().toString();
		String dir = projectLocation + "/.method/";
		String restartedTasksFileName = "restartedTasks.txt";
		
		if(cpIds == null) {
			cpIds = new ArrayList<String>();
			cpIds.add("dp");
		}
		
		String stringToDelete = "";
		for(String id : cpIds) {
			stringToDelete += id + " ";
		}
		stringToDelete += taskId;
		
		List<String> lines = new ArrayList<String>();
		
		try {
			
			FileReader fr = new FileReader(dir + restartedTasksFileName);
			BufferedReader br = new BufferedReader(fr);
			
			String s = "";
			
			while ((s = br.readLine()) != null) {
				if(!s.equals(stringToDelete)) {
					lines.add(s);
				}
			}
			
			fr.close();
			
			FileWriter fstream = new FileWriter(dir + restartedTasksFileName, false);
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
	
	/*
	 * This method checks whether a given tool ID corresponds to an Eclipse Creation Wizard
	 */
	public static boolean isCreationWizard(String id) {
		
		if(id == null || id.equals("")) {
			return false;
		}
		
		IConfigurationElement[] configurations = Platform
				.getExtensionRegistry().getConfigurationElementsFor(
						"org.eclipse.ui.newWizards");

		for (IConfigurationElement config : configurations) {
			String wizardId = config.getAttribute("id");
			if(wizardId.equals(id)) {
				return true;
			}
		}

		return false;
	}
	
	public static boolean isExternalTool(String id) {
		
		return id.startsWith(".");
	}
	
	public static boolean isURL(String id) {
		
		return (id.startsWith("http://") || id.startsWith("https://"));
	}
	
	/*
	 * This method checks whether a given tool ID corresponds to a model transformation
	 */
	public static boolean isTransformation(String id) {
		
		IConfigurationElement[] configurations = Platform
				.getExtensionRegistry().getConfigurationElementsFor(
						"es.cv.gvcase.trmanager.transformation");

		for (IConfigurationElement config : configurations) {
			String trId = config.getAttribute("id");
			if(trId.equals(id)) {
				return true;
			}
		}

		return false;
	}
	
	public static boolean createExternalToolFile(String toolId, WorkProduct product, IProject project) {
		
		boolean productCreated = false;
		String targetPath = ProductSupportUtil.getPropertyValue(product, "targetPath");
		String trimmedTargetPath = targetPath.substring(0, targetPath.length() - toolId.length());
		String absolutePath = project.getLocation().toString() + targetPath;
		String trimmedAbsolutePath = project.getLocation().toString() + trimmedTargetPath;
		
		boolean exists = true;
		int i = 1;
		String absolutePathAux = absolutePath;
		String targetPathAux = targetPath;
		while(exists) {
			File f = new File(absolutePathAux);
			if(!f.exists()) {
				exists = false;
			}
			else {
				absolutePathAux = trimmedAbsolutePath + "(" + i + ")" + toolId;
				targetPathAux = trimmedTargetPath + "(" + i + ")" + toolId;
				i++;
			}
		}
		
		absolutePath = absolutePathAux;
		targetPath = targetPathAux;
		
		StringTokenizer st = new StringTokenizer(targetPath, "/");
		String lastSegment = "";
		while(st.hasMoreTokens()) {
			lastSegment = st.nextToken();
		}
		
		String destinationFolder = absolutePath.substring(0, absolutePath.length() - lastSegment.length());
		
		File destFolder = new File(destinationFolder);
		if (!destFolder.exists()) {
			destFolder.mkdirs();
		}
		
		File f = new File(absolutePath);
		if(!f.exists()) {
			try {
				f.createNewFile();
				productCreated = true;
				project.refreshLocal(IResource.DEPTH_INFINITE,
						new NullProgressMonitor());
				IResource res = project.findMember(targetPath);
				if(res instanceof IFile) {
					ProductSupportUtil.openFile((IFile)res);
				}
				storeFile(targetPath, product, project);
			}
			catch (Exception e) {
				
			}
		}
		
		return productCreated;
	}
	
}
