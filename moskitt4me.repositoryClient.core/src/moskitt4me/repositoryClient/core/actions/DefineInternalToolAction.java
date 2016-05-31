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

/**
 * In a similar way to the class "CreateTechnicalFragmentAction", this class implements an action that
 * enables the creation of technical fragments. The difference is that this class enables the creation of
 * technical fragments of type "Internal Tool". An internal tool is a tool that is already installed in
 * MOSKitt4ME (e.g., Eclipse frameworks such as GMF or EMF). This type of technical fragments do not need
 * to encapsulate the implementation of the tools; only the identifiers of the Eclipse plug-ins.
 *
 * @author Mario Cervera
 */ 
public class DefineInternalToolAction extends Action {

	@Override
	public void run() {

		RepositoriesView repositoriesView = RepositoryClientUtil.getRepositoriesView();

		if (repositoriesView != null) {

			RepositoryLocation location = getSelectedLocation(repositoriesView);

			if (location != null) {
				
				//Open the creation dialog
				
				DefineInternalToolDialog dialog = new DefineInternalToolDialog(
						repositoriesView.getSite().getShell());
				
				if(dialog.open() == Window.OK) {
					
					InternalToolFragment result = dialog.getResult();
					
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
	
	/*
	 * This method gets the Repository Location that is selected in the Repositories view
	 */
	private RepositoryLocation getSelectedLocation(RepositoriesView repositoriesView) {

		ISelection sel = repositoriesView.getCommonViewer().getSelection();
		if (sel instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) sel;
			return (RepositoryLocation) selection.getFirstElement();
		}

		return null;
	}
	
	/*
	 * This method creates the RAS file. It is a zip file that contains an XML file
	 * representing the asset manifest
	 */
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
	
	/*
	 * This method uploads a given Internal Tool Fragment to the FTP repository that is pointed by the given
	 * Repository Location and folder
	 */
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
