package moskitt4me.spem2bpmn.actions;

import java.io.File;
import java.util.StringTokenizer;

import moskitt4me.spem2bpmn.util.SPEM2BPMNUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.epf.library.LibraryService;
import org.eclipse.epf.uma.DeliveryProcess;
import org.eclipse.epf.uma.MethodLibrary;
import org.eclipse.epf.uma.MethodPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * This action (which can be executed by means of the popup menu of the SPEM delivery processes)
 * allows the user to delete the BPMN 2.0 diagrams (that is, the .activiti files) that have been
 * generated for a specific SPEM 2.0 process.
 *
 * @author Mario Cervera
 */
public class DeleteBPMN2DiagramAction implements IActionDelegate {

	/*
	 * The folder that contains the BPMN 2.0 models
	 */
	private String activitiFolder;
	
	public void run(IAction action) {

		if (activitiFolder != null && !activitiFolder.equals("")) {
			
			File activitiFolderFile = new File(activitiFolder);

			if (activitiFolderFile.isDirectory() && activitiFolderFile.listFiles().length > 0 &&
				
					!(activitiFolderFile.listFiles().length == 1 
							&& activitiFolderFile.listFiles()[0].isDirectory()
							&& activitiFolderFile.listFiles()[0].getName().equals(".svn"))) {
				
				boolean result = MessageDialog.openConfirm(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Delete Diagram",
						"Are you sure you want to delete the BPMN 2.0 Diagram?\nThis change is permanent and cannot be undone");
				
				if(result) {
					try {
						//Delete the BPMN 2.0 models
						
						File[] files = activitiFolderFile.listFiles();
						for(File f : files) {
							if(!(f.isDirectory() && f.getName().equals(".svn"))) {
								f.delete();
							}
						}
						
						//Refresh method library
						
						MethodLibrary lib = LibraryService.getInstance().getCurrentMethodLibrary();
						if(lib != null) {
							String libraryName = lib.getName();
							IProject libraryProject = ResourcesPlugin.getWorkspace()
									.getRoot().getProject(libraryName);
							if(libraryProject != null) {
								libraryProject.refreshLocal(IResource.DEPTH_INFINITE,
										new NullProgressMonitor());
							}
						}
					}
					catch(CoreException e) {
						MessageDialog.openError(
								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								"Error", "Problems occurred deleting BPMN 2.0 diagram file");
					}
				}
			}
			else {
				MessageDialog.openError(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Error", "BPMN 2.0 diagram file does not exist");
			}
		} 
		
	}

	/*
	 * Listener for the selection changed event. Updates the reference to the activiti
	 * folder when a new delivery process is selected
	 */
	public void selectionChanged(IAction action, ISelection selection) {

		if (selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) selection;
			Object obj = sel.getFirstElement();
			if (obj instanceof DeliveryProcess) {
				DeliveryProcess p = (DeliveryProcess) obj;
				activitiFolder = getActivitiFolder(p);
			}
		}
	}
	
	private String getActivitiFolder(DeliveryProcess p) {
		
		String activitiFolder = replaceLastSegment(p.eResource().getURI().toString(), "activiti");
		
		MethodLibrary lib = LibraryService.getInstance().getCurrentMethodLibrary();
		MethodPlugin plugin = SPEM2BPMNUtil.getMethodPlugin(p);
		
		if (lib != null && plugin != null) {
			String libraryLocation = replaceLastSegment(
					lib.eResource().getURI().toString(), "");
			
			activitiFolder = libraryLocation + plugin.getName()
			+ "/deliveryprocesses/"+ p.getName() + "/activiti/";
		}
		
		return activitiFolder.replaceFirst("file:", "");
	}
	
	private String replaceLastSegment(String uri, String newSegment) {
		
		StringTokenizer tokenizer = new StringTokenizer(uri, "/");
		
		String newUri = "";
		int numTokens = tokenizer.countTokens();
		
		for(int i = 0; i < numTokens - 1; i++) {
			newUri += tokenizer.nextToken() + "/";
		}
		
		if(newSegment != null && !newSegment.equals("")) {
			return (newUri + newSegment + "/");
		}
		else {
			return newUri;
		}
	}
}
