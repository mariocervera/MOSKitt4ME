package moskitt4me.repositoryClient.core.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;

import moskitt4me.repositoryClient.core.dialogs.DefineInternalToolDialog;
import moskitt4me.repositoryClient.core.util.InternalToolFragment;
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

public class DefineInternalToolAction extends Action {

	@Override
	public void run() {

		RepositoriesView repositoriesView = RepositoryClientUtil.getRepositoriesView();

		if (repositoriesView != null) {

			RepositoryLocation location = getSelectedLocation(repositoriesView);

			if (location != null) {
				
				DefineInternalToolDialog dialog = new DefineInternalToolDialog(
						repositoriesView.getSite().getShell());
				
				if(dialog.open() == Window.OK) {
					
					InternalToolFragment result = dialog.getResult();
					
					String tempDir = "";
					String eclipseInstallationDirectory = Platform.getInstallLocation().getURL().getPath();

					try {
						tempDir = RepositoryClientUtil.createFolder(eclipseInstallationDirectory + "tmp", 0);
						createRasFile(result, tempDir);
						uploadAsset(location, tempDir, result);
						
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
	
	private void createRasFile(InternalToolFragment itf, String assetsDir) {
		
		String manifestPath = "";
		
		try {
			String assetName = itf.getName() + ".ras.zip";
			String assetLocation = assetsDir + "/" + assetName;
			File asset = new File(assetLocation);
			if(!asset.exists()) {
				FileOutputStream fos = new FileOutputStream(assetLocation);
				ZipOutputStream zos = new ZipOutputStream(fos);
				
				//Create rasset.xml file and add it to the zip file
				
				manifestPath = RepositoryClientUtil.createRassetXMLFile(itf, assetsDir);
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
	
	private void uploadAsset(RepositoryLocation location, String folder, InternalToolFragment itf) throws Exception {
		
		FTPClient client = RepositoryClientUtil.connect(location, false);
		
		String extension = ".ras.zip";
		String assetPath = folder + "/" + itf.getName() + extension;
		
		if(client != null) {
			client.setFileType(FTP.BINARY_FILE_TYPE);
			File assetFile = new File(assetPath);
			if (assetFile.exists() && !alreadyStored(client, assetFile)) {
				FileInputStream in = new FileInputStream(assetFile);
				boolean result = client.storeFile(itf.getName() + extension, in);
				in.close();
				if(!result) {
					MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Internal tool creation failed",
							"The internal tool \"" + itf.getName() + "\" could not be stored in the repository. Check if the user \"" 
							+ location.getUser() + "\" has writing access to the directory\""
							+ location.getHost() + ":" + location.getRepositoryPath() + "\".");
					
					RepositoryClientUtil.disconnect(client);
					return;
				}
				else {
					MessageDialog.openInformation(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Internal tool creation",
							"The internal tool \"" + itf.getName() + "\" has been successfully created.");
					
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
