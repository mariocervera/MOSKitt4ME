package moskitt4me.repositoryClient.core.composites;

import java.util.ArrayList;
import java.util.List;

import moskitt4me.repositoryClient.core.providers.AssetPluginsContentProvider;
import moskitt4me.repositoryClient.core.providers.WorkspacePluginsContentProvider;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/*
* A graphical widget that is used in the Edit Technical Fragment Dialog. It allows the user to select
* Plug-in projects from the MOSKitt4ME workspace and include them in the technical fragment under edition.
*
* @author Mario Cervera
*/
public class AssetPluginsComposite extends Composite {

	private List<IProject> plugins;
	
	private Group pluginsGroup;
	
	// Tree viewer that shows the plug-ins of the workspace
	
	private Label wsPluginsLabel;
	private Tree wsPluginsTree; 
	private TreeViewer wsPluginsTreeViewer;
	
	// Tree viewer that shows the plug-ins included in the fragment
	
	private Label assetPluginsLabel;
	private Tree assetPluginsTree;
	private TreeViewer assetPluginsTreeViewer;
	
	// Add and remove buttons
	
	private Button addButton;
	private Button removeButton;
	
	public AssetPluginsComposite(Composite parent, int style, List<IProject> plugins) {
		
		super(parent, style);
		
		this.plugins = plugins;
		
		// Arrange the graphical elements of the composite (Grid layout)
		
		GridLayout compositeLayout = new GridLayout(1, false);
		this.setLayout(compositeLayout);
		
		pluginsGroup = new Group(this, SWT.NONE);
		pluginsGroup.setText("Plug-ins");
		GridData gd = new GridData(GridData.FILL_BOTH);
		pluginsGroup.setLayoutData(gd);
		GridLayout groupLayout = new GridLayout(3, false);
		pluginsGroup.setLayout(groupLayout);
		
		wsPluginsLabel = new Label(pluginsGroup, SWT.NONE);
		wsPluginsLabel.setText("Workspace plug-ins");
		GridData gd2 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd2.horizontalSpan = 2;
		wsPluginsLabel.setLayoutData(gd2);
		
		assetPluginsLabel = new Label(pluginsGroup, SWT.NONE);
		assetPluginsLabel.setText("Technical fragment plug-ins");
		GridData gd3 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		assetPluginsLabel.setLayoutData(gd3);
		
		wsPluginsTree = new Tree(pluginsGroup, SWT.BORDER | SWT.MULTI);
		GridData gd4 = new GridData(GridData.FILL_BOTH);
		gd4.widthHint = 200;
		gd4.heightHint = 225;
	        wsPluginsTree.setLayoutData(gd4);
	        
	        wsPluginsTreeViewer = new TreeViewer(wsPluginsTree);
	        wsPluginsTreeViewer.setContentProvider(new WorkspacePluginsContentProvider());
	        wsPluginsTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
	        wsPluginsTreeViewer.setInput(this.plugins);
	        
	        Composite c1 = new Composite(pluginsGroup, SWT.NONE);
		GridData gd5 = new GridData(GridData.FILL_BOTH);
		gd5.heightHint = 225;
		c1.setLayoutData(gd5);
		FormLayout c1Layout = new FormLayout();
		c1.setLayout(c1Layout);
        
	        addButton = new Button(c1, SWT.PUSH);
	        addButton.setText("Add -->");
	        addButton.setFont(JFaceResources.getDialogFont());
	        FormData fd = new FormData();
	        fd.left = new FormAttachment(0, 0);
	        fd.top = new FormAttachment(38, 0);
	        fd.right = new FormAttachment(100, 0);
	        addButton.setLayoutData(fd);
	        
	        removeButton = new Button(c1, SWT.PUSH);
	        removeButton.setText("<-- Remove");
	        removeButton.setFont(JFaceResources.getDialogFont());
	        FormData fd2 = new FormData();
	        fd2.left = new FormAttachment(0, 0);
	        fd2.top = new FormAttachment(52, 0);
	        fd2.right = new FormAttachment(100, 0);
	        removeButton.setLayoutData(fd2);
        
        	assetPluginsTree = new Tree(pluginsGroup, SWT.BORDER | SWT.MULTI);
		GridData gd8 = new GridData(GridData.FILL_BOTH);
		gd8.widthHint = 200;
		gd8.heightHint = 225;
        	assetPluginsTree.setLayoutData(gd8);
        
	        assetPluginsTreeViewer = new TreeViewer(assetPluginsTree);
	        assetPluginsTreeViewer.setContentProvider(new AssetPluginsContentProvider());
	        assetPluginsTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
	        assetPluginsTreeViewer.setInput(this.plugins);
        
        	hookListeners();
	}
	
	public List<IProject> getPlugins() {
		return this.plugins;
	}
	
	protected void hookListeners() {
	
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				super.widgetSelected(e);
				
				List<IProject> selectedProjects = getSelectedProjects();
				for(IProject project : selectedProjects) {
					if(!getPlugins().contains(project)) {
						getPlugins().add(project);
					}
				}
				assetPluginsTreeViewer.setInput(getPlugins());
				wsPluginsTreeViewer.setInput(getPlugins());
			}
		});
		
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				super.widgetSelected(e);
				
				List<IProject> selectedProjects = getSelectedAssetProjects();
				getPlugins().removeAll(selectedProjects);
				
				assetPluginsTreeViewer.setInput(getPlugins());
				wsPluginsTreeViewer.setInput(getPlugins());
			}
		});
	}
	
	private List<IProject> getSelectedProjects() {
		
		List<IProject> result = new ArrayList<IProject>();
		
		ISelection selection = wsPluginsTreeViewer.getSelection();
		if(selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) selection;
			Object[] objects = sel.toArray();
			for(Object obj : objects) {
				if(obj instanceof IProject) {
					result.add((IProject)obj);
				}
			}
		}
		
		return result;
	}
	
	private List<IProject> getSelectedAssetProjects() {
		
		List<IProject> result = new ArrayList<IProject>();
		
		ISelection selection = assetPluginsTreeViewer.getSelection();
		if(selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) selection;
			Object[] objects = sel.toArray();
			for(Object obj : objects) {
				if(obj instanceof IProject) {
					result.add((IProject)obj);
				}
			}
		}
		
		return result;
	}
}
