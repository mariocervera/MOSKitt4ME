package moskitt4me.repositoryClient.core.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipOutputStream;

import moskitt4me.repositoryClient.core.util.RepositoryClientUtil;
import moskitt4me.repositoryClient.core.util.RepositoryLocation;
import moskitt4me.repositoryClient.core.views.RepositoriesView;
import moskitt4me.repositoryClient.fragmentIntegration.FragmentWriter;
import moskitt4me.repositoryClient.fragmentIntegration.dialogs.CreateConceptualFragmentDialog;
import moskitt4me.repositoryClient.fragmentIntegration.util.ConceptualFragment;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.epf.library.LibraryService;
import org.eclipse.epf.uma.MethodLibrary;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

/**
 * This class implementes the creation of conceptual fragments. A conceptual fragment contains a method
 * part (for example, a task or a role). These fragments are .zip files that are compliant with the
 * Reusable Asset Specification (RAS) standard.
 *
 * @author Mario Cervera
 */
public class CreateConceptualFragmentAction extends Action {

	FileOutputStream fos;
	ZipOutputStream zos;
	
	private boolean integrateReferences;
	
	@Override
	public void run() {
		
		MethodLibrary library = LibraryService.getInstance().getCurrentMethodLibrary();
		
		if(library == null) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Error", "A Method Library must be opened to execute this action");
			
			return;
		}
		
		RepositoriesView repositoriesView = RepositoryClientUtil.getRepositoriesView();
		
		if(repositoriesView != null) {
			
			RepositoryLocation location = getSelectedLocation(repositoriesView);
			
			if(location != null) {
				
				//Open the dialog to create a Conceptual Fragment
				
				CreateConceptualFragmentDialog dialog = new CreateConceptualFragmentDialog(repositoriesView.getSite().getShell(), library.getMethodPlugins().toArray());				
				
				if( dialog.open() == Window.OK ) {
					
					// This boolean indicates whether the check button of the dialog was enabled.
					// If so, the references to other elements will be included.
					
					integrateReferences=dialog.getIntegrateReferences();
					
					//Create the ConceptualFragment object
					
					ConceptualFragment result = new ConceptualFragment(dialog.getName(), dialog.getType(), 
							dialog.getOrigin(), dialog.getObjective());
					
					//List of content elements selected
					
					List<Object> listElements = dialog.getElements();
					String tempDir = "";
					String eclipseInstallationDirectory = Platform.getInstallLocation().getURL().getPath();

					try {
						tempDir = RepositoryClientUtil.createFolder(eclipseInstallationDirectory + "tmp", 0);
						
						String assetName = result.getName() + ".ras.zip";
						String assetLocation = tempDir + "/" + assetName;
						fos = new FileOutputStream(assetLocation);
						zos = new ZipOutputStream(fos);
						
						//Create RAS file (Fragment data)
						
						createAssets(result, tempDir);
						
						//Create fragment file (Content elements and relationships)
						
						createElements(result, listElements, tempDir);
						
						uploadAssets(location, tempDir);
						
						location.setType("Conceptual");
					}
					catch(Exception e) {
						
					}
					finally {
						if(!tempDir.equals("")) {
							RepositoryClientUtil.removeFolder(new File(tempDir));
						}
						MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								"Conceptual Fragment Creation", "The conceptual fragment creation was successfully performed");
					}
				}
			}
		}
	}
	
	private void createElements(ConceptualFragment result, List<Object> listElements, String tempDir) {
		
		createFragmentFile(result, listElements, tempDir);
	}

	private void createFragmentFile(ConceptualFragment result, List<Object> listElements, String tempDir) {
		
		String manifestPath = "";
		
		try {
			//The FragmentWriter creates the XMI file for the conceptual fragment
		
			manifestPath = FragmentWriter.createConceptualFragmentXMLFile(result, listElements, tempDir, integrateReferences);
			File f = new File(manifestPath);
			if(f.exists()) {
				RepositoryClientUtil.addFileToZip(f, zos);
				
			}
				
			if(zos != null) {
				zos.close();
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

	private RepositoryLocation getSelectedLocation(RepositoriesView repositoriesView) {
		
		ISelection sel = repositoriesView.getCommonViewer().getSelection();
		if(sel instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) sel;
			return (RepositoryLocation) selection.getFirstElement();
		}
		
		return null;
	}
	
	private void createAssets(ConceptualFragment result, String assetsDir) {
		
		createRasFile(result, assetsDir);
		
	}
	
	private void createRasFile(ConceptualFragment result, String assetsDir) {
		
		IProject feature = null;
		String manifestPath = "";
		
		try {
			
				manifestPath = FragmentWriter.createRassetXMLFile(result, assetsDir);
				File f = new File(manifestPath);
				if(f.exists()) {
					RepositoryClientUtil.addFileToZip(f, zos);
				}
				
				if(zos != null) {
					//zos.close();
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
									"Asset creation failed",
									"The asset \"" + fileName + "\" could not be stored in the repository. Check if the user \"" 
									+ location.getUser() + "\" has writing access to the directory\""
									+ location.getHost() + ":" + location.getRepositoryPath() + "\".");
							
							RepositoryClientUtil.disconnect(client);
							return;
						}
					}
				}
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
