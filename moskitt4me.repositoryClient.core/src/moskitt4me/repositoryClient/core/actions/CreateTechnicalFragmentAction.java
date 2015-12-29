package moskitt4me.repositoryClient.core.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipOutputStream;

import moskitt4me.repositoryClient.core.dialogs.CreateTechnicalFragmentDialog;
import moskitt4me.repositoryClient.core.util.RepositoryClientUtil;
import moskitt4me.repositoryClient.core.util.RepositoryLocation;
import moskitt4me.repositoryClient.core.util.TechnicalFragment;
import moskitt4me.repositoryClient.core.views.RepositoriesView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

public class CreateTechnicalFragmentAction extends Action {

	@Override
	public void run() {
		
		RepositoriesView repositoriesView = RepositoryClientUtil.getRepositoriesView();
		
		if(repositoriesView != null) {
			
			RepositoryLocation location = getSelectedLocation(repositoriesView);
			
			if(location != null) {
				CreateTechnicalFragmentDialog dialog = new CreateTechnicalFragmentDialog(
						repositoriesView.getSite().getShell(), location);
				
				if(dialog.open() == Window.OK) {
					
					TechnicalFragment result = dialog.getResult();
					
					String tempDir = "";
					String eclipseInstallationDirectory = Platform.getInstallLocation().getURL().getPath();

					try {
						tempDir = RepositoryClientUtil.createFolder(eclipseInstallationDirectory + "tmp", 0);
						createAssets(result, tempDir);
						uploadAssets(location, tempDir);
						location.setType("Technical");
					}
					catch(Exception e) {
						
					}
					finally {
						if(!tempDir.equals("")) {
							RepositoryClientUtil.removeFolder(new File(tempDir));
						}
					}
				}
				CreateTechnicalFragmentDialog.clipboard = null;
			}
		}
	}
	
	private RepositoryLocation getSelectedLocation(RepositoriesView repositoriesView) {
		
		ISelection sel = repositoriesView.getCommonViewer().getSelection();
		if(sel instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) sel;
			return (RepositoryLocation) selection.getFirstElement();
		}
		
		return null;
	}
	
	private void createAssets(TechnicalFragment tf, String assetsDir) {
		
		createRasFile(tf, assetsDir);
		
		List<TechnicalFragment> dependencies = tf.getDependencies();
		
		for(TechnicalFragment dependency : dependencies) {
			createAssets(dependency, assetsDir);
		}
	}
	
	private void createRasFile(TechnicalFragment tf, String assetsDir) {
		
		IProject feature = null;
		String manifestPath = "";
		
		try {
			String assetName = tf.getName() + ".ras.zip";
			String assetLocation = assetsDir + "/" + assetName;
			File asset = new File(assetLocation);
			if(!asset.exists()) {
				FileOutputStream fos = new FileOutputStream(assetLocation);
				ZipOutputStream zos = new ZipOutputStream(fos);
				
				//Create feature project and add it to the zip file
				
				feature = RepositoryClientUtil.createFeatureProject(tf);
				String featureLocation = feature.getLocation().toString();
				String featureName = feature.getName();
				RepositoryClientUtil.addFolderToZip(new File(featureLocation), featureName + "/", zos);
				
				//Add to the zip file all the plugin projects of the fragment
				
				List<IProject> plugins = tf.getPlugins();
				for(IProject project : plugins) {
					String projectLocation = project.getLocation().toString();
					String projectName = project.getName();
					RepositoryClientUtil.addFolderToZip(new File(projectLocation), projectName + "/", zos);
				}
				
				//Create rasset.xml file and add it to the zip file
				
				manifestPath = RepositoryClientUtil.createRassetXMLFile(tf, assetsDir);
				File f = new File(manifestPath);
				if(f.exists()) {
					RepositoryClientUtil.addFileToZip(f, zos);
				}
				
				if(zos != null) {
					zos.close();
				}
			}
		}
		catch(Exception e) {
			
		}
		finally {
			if(feature != null) {
				try {
					feature.delete(true, new NullProgressMonitor());
				}
				catch(Exception e){}
			}
			if(manifestPath != null && !manifestPath.equals("")) {
				File f = new File(manifestPath);
				if(f.exists()) {
					f.delete();
				}
			}
		}
	}
	
	private void uploadAssets(RepositoryLocation location, String assetsFolder) throws Exception {
		
		FTPClient client = RepositoryClientUtil.connect(location, false);
		
		if(client != null) {
			boolean created = false;
			boolean alreadyExists = false;
			client.setFileType(FTP.BINARY_FILE_TYPE);
			File assetsFolderFile = new File(assetsFolder);
			if (assetsFolderFile.isDirectory()) {
				File[] files = assetsFolderFile.listFiles();
				for (File f : files) {
					if(!alreadyExistsFile(client, f)) {
						FileInputStream in = new FileInputStream(f);
						boolean result = client.storeFile(f.getName(), in);
						in.close();
						if(!result) {
							String extension = ".ras.zip";
							String fileName = f.getName().substring(0, f.getName().length() - extension.length());
							MessageDialog.openError(
									PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
									"Technical fragment creation failed",
									"The technical fragment \"" + fileName + "\" could not be stored in the repository. Check if the user \"" 
									+ location.getUser() + "\" has writing access to the directory\""
									+ location.getHost() + ":" + location.getRepositoryPath() + "\".");
							
							RepositoryClientUtil.disconnect(client);
							return;
						}
						created = true;
					}
					else {
						alreadyExists = true;
					}
				}
			}
			if(created) {
				MessageDialog.openInformation(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Technical fragment creation",
						"The technical fragment has been successfully created.");
			}
			else if(alreadyExists) {
				MessageDialog.openError(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Technical fragment creation",
						"The technical fragment was not created because it already exists.");
			}
			
			RepositoryClientUtil.disconnect(client);
		}
	}
	
	private boolean alreadyExistsFile(FTPClient client, File f) throws Exception {
		
		String[] fileNames = client.listNames();
		for(String fileName : fileNames) {
			if(fileName.equals(f.getName())) {
				return true;
			}
		}
		return false;
	}
}
