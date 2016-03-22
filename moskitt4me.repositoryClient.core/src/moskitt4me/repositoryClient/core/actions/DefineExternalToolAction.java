package moskitt4me.repositoryClient.core.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;

import moskitt4me.repositoryClient.core.dialogs.DefineExternalToolDialog;
import moskitt4me.repositoryClient.core.util.ExternalToolFragment;
import moskitt4me.repositoryClient.core.util.RepositoryClientUtil;
import moskitt4me.repositoryClient.core.util.RepositoryLocation;
import moskitt4me.repositoryClient.core.views.RepositoriesView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

/*
* In a similar way to the class "CreateTechnicalFragmentAction", this class implements an action that
* enables the creation of technical fragments. The difference is that this class enables the creation of
* technical fragments of type "External Tool". An external tool is a tool that is not implemented as Eclipse
* plug-ins, and, therefore, cannot be integrated in the generated CASE environment.
*/ 
public class DefineExternalToolAction extends Action {

	@Override
	public void run() {

		RepositoriesView repositoriesView = RepositoryClientUtil.getRepositoriesView();

		if (repositoriesView != null) {

			RepositoryLocation location = getSelectedLocation(repositoriesView);

			if (location != null) {
				
				//Open the creation dialog
				
				DefineExternalToolDialog dialog = new DefineExternalToolDialog(
						repositoriesView.getSite().getShell());
				
				if(dialog.open() == Window.OK) {
					
					ExternalToolFragment result = dialog.getResult();
					
					String tempDir = "";
					String eclipseInstallationDirectory = Platform.getInstallLocation().getURL().getPath();

					try {
						tempDir = RepositoryClientUtil.createFolder(eclipseInstallationDirectory + "tmp", 0);
						
						// Create the asset as a RAS file
						
						createRasFile(result, tempDir);
						
						//Upload the asset to the FTP repository
						
						uploadAsset(location, tempDir, result);
						
						//The repository is now a location for technical (not conceptual) method fragments
						
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
			}
		}
	}

	private RepositoryLocation getSelectedLocation(RepositoriesView repositoriesView) {

		ISelection sel = repositoriesView.getCommonViewer().getSelection();
		if (sel instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) sel;
			return (RepositoryLocation) selection.getFirstElement();
		}

		return null;
	}
	
	private void createRasFile(ExternalToolFragment etf, String assetsDir) {
		
		String manifestPath = "";
		
		try {
			String assetName = etf.getName() + ".ras.zip";
			String assetLocation = assetsDir + "/" + assetName;
			File asset = new File(assetLocation);
			if(!asset.exists()) {
				FileOutputStream fos = new FileOutputStream(assetLocation);
				ZipOutputStream zos = new ZipOutputStream(fos);
				
				//Create rasset.xml file and add it to the zip file
				
				manifestPath = RepositoryClientUtil.createRassetXMLFile(etf, assetsDir);
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
			if(manifestPath != null && !manifestPath.equals("")) {
				File f = new File(manifestPath);
				if(f.exists()) {
					f.delete();
				}
			}
		}
	}
	
	private void uploadAsset(RepositoryLocation location, String folder, ExternalToolFragment etf) throws Exception {
		
		FTPClient client = RepositoryClientUtil.connect(location, false);
		
		String extension = ".ras.zip";
		String assetPath = folder + "/" + etf.getName() + extension;
		
		if(client != null) {
			client.setFileType(FTP.BINARY_FILE_TYPE);
			File assetFile = new File(assetPath);
			if (assetFile.exists() && !alreadyStored(client, assetFile)) {
				FileInputStream in = new FileInputStream(assetFile);
				boolean result = client.storeFile(etf.getName() + extension, in);
				in.close();
				if(!result) {
					MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"External tool creation failed",
							"The external tool \"" + etf.getName() + "\" could not be stored in the repository. Check if the user \"" 
							+ location.getUser() + "\" has writing access to the directory\""
							+ location.getHost() + ":" + location.getRepositoryPath() + "\".");
					
					RepositoryClientUtil.disconnect(client);
					return;
				}
				else {
					MessageDialog.openInformation(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"External tool creation",
							"The external tool \"" + etf.getName() + "\" has been successfully created.");
					
					RepositoryClientUtil.disconnect(client);
					return;
				}
			}
			RepositoryClientUtil.disconnect(client);
		}
	}
	
	private boolean alreadyStored(FTPClient client, File f) throws Exception {
		
		String[] fileNames = client.listNames();
		for(String fileName : fileNames) {
			if(fileName.equals(f.getName())) {
				return true;
			}
		}
		return false;
	}
}
