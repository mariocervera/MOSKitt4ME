package moskitt4me.projectmanager.core.wizards;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import moskitt4me.projectmanager.core.pages.NewMOSKitt4MEProjectPage;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.epf.uma.DeliveryProcess;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * A wizard that allows the user to create MOSKitt4ME projects.
 * 
 * @author Mario Cervera
 */
public class NewMOSKitt4MEProjectWizard extends BasicNewResourceWizard
		implements INewWizard {
	
	public NewMOSKitt4MEProjectWizard() {
		setWindowTitle("New MOSKitt4ME Project");
	}
	
	/*
	* This method is executed when the user clicks the Finish button of the wizard.
	* This method creates the project.
	*/
	@Override
	public boolean performFinish() {
	
		try {
			String projectName = getProjectName();

			IProjectDescription description = ResourcesPlugin.getWorkspace()
					.newProjectDescription(projectName);

			description.setName(projectName);

			String[] natures = description.getNatureIds();
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);

			newNatures[natures.length] = "moskitt4me.projectmanager.core.natures.MOSKitt4MEProjectNature";
			description.setNatureIds(newNatures);

			IProject newProject = createProject(description);
			
			DeliveryProcess process = getSelectedDeliveryProcess();
			
			String processUri = process.eResource().getURI().toString();
			
			storeProcessModelURI(newProject, processUri);
			
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	/*
	* The MOSKitt4ME Project wizard only contains one page.
	*/
	@Override
	public void addPages() {
		
		NewMOSKitt4MEProjectPage page = new NewMOSKitt4MEProjectPage("newMOSKitt4MEProjectPage");
		page.setTitle("New MOSKitt4ME Project");
		page.setDescription("Select Method Library and Delivery Process");
		this.addPage(page);
	}
	
	private DeliveryProcess getSelectedDeliveryProcess() {
		
		for(IWizardPage page: this.getPages()) {
			if(page instanceof NewMOSKitt4MEProjectPage) {
				return ((NewMOSKitt4MEProjectPage)page).getSelectedProcess();
			}
		}
		
		return null;
	}
	
	private String getProjectName() {
		
		for(IWizardPage page: this.getPages()) {
			if(page instanceof NewMOSKitt4MEProjectPage) {
				return ((NewMOSKitt4MEProjectPage)page).getProjectName();
			}
		}
		
		return null;
	}
	
	private IProject createProject(IProjectDescription description)
			throws Exception {

		CreateProjectOperation op = new CreateProjectOperation(description,
				"New Project");

		PlatformUI.getWorkbench().getOperationSupport().getOperationHistory()
				.execute(op, new NullProgressMonitor(),
						WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
		
		Object[] objs = op.getAffectedObjects();
		if(objs != null && objs.length == 1 && objs[0] instanceof IProject) {
			
			return (IProject) objs[0];
		}
		
		return null;
	}
	
	private void storeProcessModelURI(IProject project, String uri) {
		
		String dir = project.getLocation().toString() + "/.method/";
		String methodPropertiesFileName = "methodProperties.txt";
		
		String text = "ProcessURI: " + uri;
		
		File f = new File(dir);
		
		if(!f.isDirectory()) {
			f.mkdirs();
		}
		
		write(dir + methodPropertiesFileName, text);
	}
	
	private void write(String filePath, String text) {
		
		try {
			FileWriter fstream = new FileWriter(filePath);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(text);
			out.newLine();
			out.close();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
