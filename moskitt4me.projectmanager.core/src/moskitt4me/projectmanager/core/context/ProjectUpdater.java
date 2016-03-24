package moskitt4me.projectmanager.core.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import moskitt4me.projectmanager.core.views.ProcessView;
import moskitt4me.projectmanager.core.views.ProductExplorerView;
import moskitt4me.projectmanager.methodspecification.MethodElements;
import moskitt4me.projectmanager.processsupport.Engine;
import moskitt4me.projectmanager.productsupport.util.ProductSupportUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.epf.uma.DeliveryProcess;
import org.eclipse.epf.uma.GuidanceDescription;
import org.eclipse.epf.uma.MethodElement;
import org.eclipse.epf.uma.MethodElementProperty;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.Template;
import org.eclipse.epf.uma.WorkProduct;
import org.eclipse.epf.uma.WorkProductDescriptor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/*
* A listener that reacts to Selection Change events in the Resource Explorer view. In MOSKitt4ME,
* when the selected project changes, the process instance that is running in the Activiti Engine
* must be updated (since every project has a different process instance associated to it).
*
* @author Mario Cervera
*/
public class ProjectUpdater implements ISelectionListener {

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

		if(selection instanceof StructuredSelection) {
			
			StructuredSelection sel = (StructuredSelection) selection;
			
			// Selection changed --> carry out an update of the selected project
			
			updateActiveProject(part, sel);
		}
	}
	
	private void updateActiveProject(IWorkbenchPart part,
			StructuredSelection selection) {
		
		IProject previousProject = Context.selectedProject;
		IProject newProject = null;
		
		Iterator<?> it = selection.iterator();
		boolean found = false;
		
		while(it.hasNext() && !found) {
			Object next = it.next();
			
			if(next instanceof IProject) {
				newProject = (IProject) next;
				found = true;
			}
			else if(next instanceof IResource) {
				newProject = ((IResource) next).getProject();
				found = true;
			}
			else if(next instanceof IAdaptable) {
				IResource resource = (IResource) ((IAdaptable) next)
						.getAdapter(IResource.class);
				if (resource != null) {
					newProject = resource.getProject();
					found = true;
				}
			}
		}
		
		if(newProject != null && !newProject.equals(previousProject)) {
			
			//A new project has been selected
			
			if(Context.resourceListener == null) {
				Context.resourceListener = new MOSKitt4MEResourceChangeListener();
				ResourcesPlugin.getWorkspace().addResourceChangeListener(Context.resourceListener);
			}
			
			// Get the URI of the process model that is associated to the new project
			
			String processModelUri = Context.getProcessModelURI(newProject);
			
			if(processModelUri != null) {
				Context.selectedProject = newProject;
				
				// Get the Delivery Process (i.e., the root element of the process model)
				
				DeliveryProcess dp = MethodElements.getDeliveryProcess(processModelUri);
				
				if(dp != null) {
					
					// Load the process model in memory for easier access
					
					MethodElements.loadMethodDefinition(dp);
					
					try {
						// The selected project has changed --> Update the Activiti Engine
						
						Engine.setUp(dp, Context.selectedProject);
					}
					catch(Exception e) {
						MessageDialog.openError(part.getSite().getShell(),
								"Error", "Could not set up Activiti engine");
					}
					
					updateProcessInstanceId();
					
					updateProducts();
					
					// Refresh Process view
					
					ProcessView processView = getProcessView();
					if(processView != null) {
						processView.enableUndoButton();
						processView.clearContentProvider();
						processView.getViewer().setInput(Context.selectedProject);
					}
					
					// Refresh Product Explorer view
					
					ProductExplorerView productExplorerView = getProductExplorerView();
					if(productExplorerView != null) {
						productExplorerView.getViewer().setInput(Context.selectedProject);
					}
				}
			}
		}
	}
	
	private void updateProcessInstanceId() {
			
		String id = Engine.getProcessInstanceIdFromProcessKey(MethodElements.processKey,
				Context.selectedProject);
		
		if(id == null || id.equals("")) {
			if (!isStarted(Context.selectedProject)) {
				id = Engine.createProcessInstance(MethodElements.processKey, Context.selectedProject);
			}
		}
		
		if(id != null && !id.equals("")) {
			Context.processInstanceId = id;
		}
	}
	
	private ProcessView getProcessView() {
		
		try {
			IViewPart viewPart = PlatformUI.getWorkbench().
			getActiveWorkbenchWindow().getActivePage().findView(ProcessView.ProcessViewId);
			
			if(viewPart instanceof ProcessView) {
				return (ProcessView) viewPart;
			}
		}
		catch(Exception e) {
			return null;
		}
		
		return null;
	}
	
	private ProductExplorerView getProductExplorerView() {
		
		try {
			IViewPart viewPart = PlatformUI.getWorkbench().
			getActiveWorkbenchWindow().getActivePage().findView(ProductExplorerView.ProductExplorerViewId);
			
			if(viewPart instanceof ProductExplorerView) {
				return (ProductExplorerView) viewPart;
			}
		}
		catch(Exception e) {
			return null;
		}
		
		return null;
	}
	
	private boolean isStarted(IProject project) {
		
		try {
			String dir = project.getLocation().toString() + "/.method/";
			String executedTasksFileName = "executedTasks.txt";
			
			File f = new File(dir + executedTasksFileName);
			
			return f.exists();
		} 
		catch(Exception e) {
			return false;
		}
	}
	
	private void updateProducts() {
		
		List<String> productGuids = ProductSupportUtil.getStoredProductGuids(Context.selectedProject);
		
		boolean filesIncluded = false;
		
		for(TaskDescriptor td : MethodElements.taskDescriptors) {
			List<WorkProductDescriptor> wpds = new ArrayList<WorkProductDescriptor>();
			if(td.getMandatoryInput() != null) {
				wpds.addAll(td.getMandatoryInput());
			}
			if(td.getOptionalInput() != null) {
				wpds.addAll(td.getOptionalInput());
			}
			if(td.getOutput() != null) {
				wpds.addAll(td.getOutput());
			}
			
			for(WorkProductDescriptor wpd : wpds) {
				WorkProduct wp = wpd.getWorkProduct();
				if(wp.eIsProxy()) {
					wp = MethodElements.getWorkProduct(wp);
				}
				if(!productGuids.contains(wp.getGuid())) {
					for(Template t : wp.getTemplates()) {
						MethodElements.resolveContentDescription(t);
						if(t.getPresentation() instanceof GuidanceDescription) {
							GuidanceDescription gd = (GuidanceDescription) t.getPresentation();
							String attachments = gd.getAttachments();
							if(attachments != null) {
								if(resolveFile(wp, attachments, gd)) {
									filesIncluded = true;
								}
							}
						}
					}
				}
			}
		}
		
		if(filesIncluded) {
			try {
				Context.selectedProject.refreshLocal(IResource.DEPTH_INFINITE,
						new NullProgressMonitor());	
			}
			catch(Exception e) {
				
			}
		}
	}
	
	private boolean resolveFile(WorkProduct wp, String attachments, GuidanceDescription gd) {
		
		URI resourceURI = gd.eResource().getURI();
		URI finalURI = resourceURI.trimSegments(1);
		
		StringTokenizer st = new StringTokenizer(attachments, "/");
		String lastSegment = "";
		while(st.hasMoreTokens()) {
			lastSegment = st.nextToken();
			finalURI = finalURI.appendSegment(lastSegment);
		}
		
		String filePath = finalURI.toString().replace("file:", "");
		
		String destinationFolder = Context.selectedProject.getLocation().toString();
		String destinationRelativePath = getPropertyValue(wp, "sourcePath");
		String destinationPath = destinationFolder + destinationRelativePath;
		
		StringTokenizer st2 = new StringTokenizer(destinationPath, "/");
		String lastSegment2 = "";
		while(st2.hasMoreTokens()) {
			lastSegment2 = st2.nextToken();
		}
		
		destinationFolder = destinationPath.substring(0, destinationPath.length() - lastSegment2.length());
		
		try {
			if(copyFile(filePath, destinationFolder, lastSegment2)) {
				ProductSupportUtil.storeFile(destinationRelativePath, wp, Context.selectedProject);
				return true;
			}
		}
		catch(Exception e) {
			return false;
		}
		
		return false;
	}
	
	private static String getPropertyValue(MethodElement me, String name) {
		
		for(MethodElementProperty prop : me.getMethodElementProperty()) {
			if(prop.getName().equals(name)) {
				return prop.getValue();
			}
		}
		
		return "";
	}
	
	private static boolean copyFile(String sourcePath,
			String targetFolder, String targetFileName) throws Exception {
		
		File destFolder = new File(targetFolder);
		if (!destFolder.exists()) {
			destFolder.mkdirs();
		}
		
		File dest = new File(targetFolder + targetFileName);
		if (!dest.exists()) {
			dest.createNewFile();
			
			File f = new File(sourcePath);
			InputStream in = new FileInputStream(f);
			OutputStream out = new FileOutputStream(dest);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			in.close();
			out.close();
			
			return true;
		}
		else {
			return false;
		}
	}
}
