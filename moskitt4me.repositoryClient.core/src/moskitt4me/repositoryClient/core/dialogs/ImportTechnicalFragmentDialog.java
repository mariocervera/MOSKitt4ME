package moskitt4me.repositoryClient.core.dialogs;

import java.io.File;
import java.util.List;

import moskitt4me.repositoryClient.core.providers.DependenciesTreeLabelProvider;
import moskitt4me.repositoryClient.core.providers.RepositoryAssetsContentProvider;
import moskitt4me.repositoryClient.core.util.RepositoryClientUtil;
import moskitt4me.repositoryClient.core.util.RepositoryLocation;
import moskitt4me.repositoryClient.core.util.TechnicalFragment;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A dialog for importing technical fragments into the dependencies tree of another technical fragment.
 * This dialog can be opened within the Create Technical Fragment Dialog.
 *
 * @author Mario Cervera
 */
public class ImportTechnicalFragmentDialog extends Dialog {
	
	// The Repository Location that stores the fragments that can be imported
	
	private RepositoryLocation location;
	
	// The target fragment (to be associated with the imported fragments)
	
	private TechnicalFragment parentFragment;
	
	// Graphical widgets
	
	private Label repositoryAssetsLabel;
	private Tree repositoryAssetsTree;
	private TreeViewer repositoryAssetsTreeViewer;
	
	private TechnicalFragment result;
	
	/*
	 * Constructor
	 */
	protected ImportTechnicalFragmentDialog(Shell parentShell, 
			RepositoryLocation location, TechnicalFragment parentFragment) {
		
		super(parentShell);
		
		this.location = location;
		this.parentFragment = parentFragment;
	}

	public TechnicalFragment getResult() {
		return result;
	}
	
	protected void configureShell(Shell shell) {
        
		super.configureShell(shell);
        
        	shell.setText("Import Technical Fragment");
		
	}
	
	/*
	* Create graphical elements (Grid layout)
	*/
	protected Control createDialogArea(Composite parent) {

		Composite composite = (Composite) super.createDialogArea(parent);
	        GridLayout compositeLayout = new GridLayout(1, false);
	        composite.setLayout(compositeLayout);
	        
	        repositoryAssetsLabel = new Label(composite, SWT.NONE);
	        repositoryAssetsLabel.setText("Repository fragments");
	        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
	        repositoryAssetsLabel.setLayoutData(gd);
	        
	        repositoryAssetsTree = new Tree(composite, SWT.BORDER | SWT.SINGLE);
	        GridData gd2 = new GridData(GridData.FILL_BOTH);
	        gd2.widthHint = 250;
	        gd2.heightHint = 250;
	        repositoryAssetsTree.setLayoutData(gd2);
			
	        repositoryAssetsTreeViewer = new TreeViewer(repositoryAssetsTree);
	        repositoryAssetsTreeViewer.setContentProvider(new RepositoryAssetsContentProvider());
	        repositoryAssetsTreeViewer.setLabelProvider(new DependenciesTreeLabelProvider());
	        repositoryAssetsTreeViewer.setInput(this.location);
	        
	        return composite;
	}
	
	/*
	 * This method returns the technical fragment that is selected in the Tree Viewer of the Dialog
	 */
	private TechnicalFragment getSelectedFragment() {
		
		ISelection selection = repositoryAssetsTreeViewer.getSelection();
		if(selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) selection;
			Object elem = sel.getFirstElement();
			if(elem != null && elem instanceof TechnicalFragment) {
				return (TechnicalFragment) elem;
			}
		}
		
		return null;
	}
	
	@Override
	protected void okPressed() {
		
		TechnicalFragment selectedFragment = getSelectedFragment();
		if(selectedFragment != null) {
			boolean result = MessageDialog.openConfirm(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Import Technical Fragment",
							"The technical fragment \"" + selectedFragment.getName() + "\" will be set as " +
								"dependency of the technical fragment \"" + parentFragment.getName() +
								"\". All the plugins contained in \"" + selectedFragment.getName() +
								"\" and its dependencies will be imported into the workspace." +
								" Do you want to proceed?");
			
			if(result) {
				
				//The OK button has been pressed. Import the selected technical fragment into
				//the dependencies tree (and its plug-ins into the workspace, if necesssary)
				
				boolean imported = importAsset(selectedFragment, null);
				if(imported) {
					this.result = selectedFragment;
					super.okPressed();
				}
			}
		}
	}
	
	/*
	 * This method imports the assets from the repository to the MOSKitt4ME workspace
	 */
	private boolean importAsset(TechnicalFragment tf, TechnicalFragment parentFragment) {
		
		String tempDir = "";
		List<TechnicalFragment> dependencies = null;
		
		if(parentFragment != null) {
			tf.setParent(parentFragment);
		}
		
		try {
			String fileName = tf.getName() + ".ras.zip";
			String eclipseInstallationDirectory = Platform.getInstallLocation().getURL().getPath();
			tempDir = RepositoryClientUtil.createFolder(eclipseInstallationDirectory + "tmp", 0);
			
			//Download the technical fragment from the repository and extract the ZIP file into
			//a temporal folder
			
			RepositoryClientUtil.downloadFragment(location, fileName, tempDir);
			RepositoryClientUtil.extractZipFile(tempDir, fileName);
			
			//Check if it is external or internal tool
			
			if(isExternalOrInternal(tempDir + "/rasset.xml")) {
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Error", "Internal and external tools cannot be imported.");
				return false;
			}
			
			tf.setImported(true);
			
			//Import projects into workspace, if necessary
			
			File tempDirFile = new File(tempDir);
			File[] files = tempDirFile.listFiles();
			for(File f : files) {
				if(f.isDirectory()) {
					String uri = f.toURI().toString().replaceFirst("file:", "");
					File featureXML = new File(uri + "/feature.xml");
					if(featureXML.exists()) {
						continue;
					}
					IProject pluginProject = RepositoryClientUtil.getPluginProject(f.getName());
					if(pluginProject == null) {
						File source = new File(uri);
						String targetPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/" + f.getName();
						RepositoryClientUtil.copyDirectory(source, new File(targetPath));
						pluginProject = RepositoryClientUtil.importProjectIntoWorkspace(targetPath);
					}
					if(pluginProject != null) {
						tf.getPlugins().add(pluginProject);
					}
				}
			}
			
			//Parse the rasset.xml file in order to complete the TechnicalFragment element
			
			dependencies = RepositoryClientUtil.parseRassetXML(tempDir + "/rasset.xml", tf);
		}
		catch(Exception e) {
			
		}
		finally {
			if(!tempDir.equals("")) {
				RepositoryClientUtil.removeFolder(new File(tempDir));
			}
		}
		
		if(dependencies != null) {
			boolean result = true;
			for(TechnicalFragment dependency : dependencies) {
				tf.getDependencies().add(dependency);
				result = importAsset(dependency, tf);
				if(!result) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	private boolean isExternalOrInternal(String rassetPath) {
		
		Document doc = RepositoryClientUtil.getDocument(rassetPath);
		if(doc != null) {
			Node typenode = doc.getElementsByTagName("type").item(0);
			if(typenode != null) {
				String type = typenode.getAttributes().getNamedItem("value").getNodeValue();
				if(type != null && 
						(type.equals("External Tool") || type.equals("Internal Tool"))) {
					
					return true;
				}
			}
		}
		
		return false;
	}
}
