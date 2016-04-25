package moskitt4me.toolgenerator.actions;

import java.io.File;
import java.io.FileInputStream;

import moskitt4me.toolgenerator.util.GeneratorUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * This class implements a Job that deletes the Plug-in Projects that have been imported
 * into the workspace during the CASE environment generation process.
 *
 * This Job is executed after the Export Job (which is in charge of the generation of the
 * CASE environment).
 *
 * @author Mario Cervera
 */
public class DeleteProjectsOperation extends Job {

	private Job exportJob;
	private String folder1;
	private String folder2;
	private String folder3;
	private String destination;
	private String productRoot;
	
	public DeleteProjectsOperation(Job exportJob, String folder1,
			String folder2, String folder3, String destination, String productRoot) {
		
		super("Delete projects");
		
		this.exportJob = exportJob;
		this.folder1 = folder1;
		this.folder2 = folder2;
		this.folder3 = folder3;
		this.destination = destination;
		this.productRoot = productRoot;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		try {
			if(exportJob != null) {
				//Projects must be deleted when the export job is finished
				exportJob.join();
			}
		}
		catch(Exception e) {
			
		}
		finally {
			//Copy the generation report into the destination folder
			
			File finalToolDir = new File(destination);
			String[] files = finalToolDir.list();
			boolean exists = false;
			if(files != null) {
				for(String fileName : files) {
					if(fileName.equals(this.productRoot)) {
						exists = true;
						break;
					}
				}
			}
			if(exists) {
				File reportFile = new File(folder3 + "/generationReport.txt");
				if(reportFile.exists()) {
					try {
						FileInputStream in = new FileInputStream(reportFile);
						GeneratorUtil.copyFile(in, destination + "/generationReport.txt");
					}
					catch(Exception e){}
				}
			}
			
			//Delete projects
			
			monitor.beginTask("Deleting projects from workspace ...", 100);
			GeneratorUtil.projects.removeAll(GeneratorUtil.permanentProjects);
			int count = GeneratorUtil.projects.size();
			
			try{
				if(count > 0) {
					for (IProject project : GeneratorUtil.projects) {
						project.delete(true, new NullProgressMonitor());
						monitor.worked(80/count);
					}
				}
				else {
					monitor.worked(80);
				}
			}
			catch(Exception e){}
			
			GeneratorUtil.deleteFolder(folder1);
			monitor.worked(10);
			GeneratorUtil.deleteFolder(folder2);
			monitor.worked(10);
			GeneratorUtil.deleteFolder(folder3);
			monitor.worked(10);
			GeneratorUtil.projects.clear();
			monitor.done();
		}
		
		return Status.OK_STATUS;
	}

}
