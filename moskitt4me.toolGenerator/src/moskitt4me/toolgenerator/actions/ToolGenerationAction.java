package moskitt4me.toolgenerator.actions;

import moskitt4me.toolgenerator.dialogs.ToolGenerationDialog;
import moskitt4me.toolgenerator.util.GeneratorUtil;
import moskitt4me.toolgenerator.util.LibraryValidator;
import moskitt4me.toolgenerator.wizards.ToolGenerationWizard;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.wizards.ResizableWizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

/*
* This is the main action of the moskitt4me.toolGenerator plug-in. It is in charge of invoking the
* generation of the CASE environment.
*/ 
public class ToolGenerationAction implements IViewActionDelegate {
	
	public void run(IAction action) {

		GeneratorUtil.projects.clear();
		GeneratorUtil.permanentProjects.clear();
		GeneratorUtil.internalPlugins.clear();
		
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		
		// Open a dialog that allows the user to specify destionation folder
		// and name of the CASE environment
		
		ToolGenerationDialog tgd = new ToolGenerationDialog(shell);
		tgd.open();

		if (tgd.getReturnCode() == Window.OK) {

			String destination = tgd.getDestination();
			String productRoot = tgd.getProductRoot();
			
			String eclipseInstallationDirectory = Platform.getInstallLocation().getURL().getPath();

			String temporalFolder = GeneratorUtil.createFolder(
					eclipseInstallationDirectory + "tmp", 0);
			String temporalFolder2 = GeneratorUtil.createFolder(
					eclipseInstallationDirectory + "tmp", 0);
			String temporalFolder3 = GeneratorUtil.createFolder(
					eclipseInstallationDirectory + "tmp", 0);
			
			GeneratorUtil.technicalFragmentsPath = temporalFolder;
			
			Job exportJob = null;
			
			try {
				// First of all, validate the method library

				LibraryValidator.validateLibrary();
				
				//Download tools from the repository
				
				DownloadToolsOperation dto = new DownloadToolsOperation(shell,
						temporalFolder, temporalFolder2, temporalFolder3);
				ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
				pmd.run(true, false, dto);
				
				if(dto.getResult() == 0) {
					
					// Invoke export product (do not open wizard)
					// This functionality is provided by the Eclipse PDE
					
					ToolGenerationWizard wizard = new ToolGenerationWizard(destination, productRoot);
					wizard.init(PlatformUI.getWorkbench(),
							new StructuredSelection());
					WizardDialog wd = new ResizableWizardDialog(PDEPlugin
							.getActiveWorkbenchShell(), wizard);
					wd.create();
					wizard.performFinish();
					
					exportJob = wizard.getJob();
				}
				else {
					throw new RuntimeException("Problems encountered during the tools download");
				}
			}
			catch (Exception e) {
				MessageDialog.openError(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(), "Error", e
						.getMessage() != null ? e.getMessage() : e.toString());
			}
			finally {
				
				//Delete the projects that have been imported into the workspace
				
				DeleteProjectsOperation dpo = new DeleteProjectsOperation(
						exportJob, temporalFolder, temporalFolder2, temporalFolder3,
						destination, productRoot);
				dpo.schedule();
			}
		}
	}
	
	public void init(IViewPart view) {
		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		
	}
}
