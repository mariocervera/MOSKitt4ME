package moskitt4me.repositoryClient.core.dialogs;

import java.util.ArrayList;
import java.util.List;

import moskitt4me.repositoryClient.core.actions.CopyAction;
import moskitt4me.repositoryClient.core.actions.PasteAction;
import moskitt4me.repositoryClient.core.providers.DependenciesTreeContentProvider;
import moskitt4me.repositoryClient.core.providers.DependenciesTreeLabelProvider;
import moskitt4me.repositoryClient.core.util.RepositoryLocation;
import moskitt4me.repositoryClient.core.util.TechnicalFragment;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

/*
* A dialog for creating technical fragments. It allows the user to specify the fragment content and also to
* establish its dependencies with other technical fragments.
*
* @author Mario Cervera
*/
public class CreateTechnicalFragmentDialog extends Dialog {

	public static TechnicalFragment clipboard; // for copy and paste ...
	
	private RepositoryLocation location;
	private Shell parentShell;
	
	// The dependencies of the technical fragment are defined by means of a dependency tree
	
	private Tree dependenciesTree;
	private TreeViewer dependenciesTreeViewer;

	// Edit, Add Dependency, Remove Dependency, and Import buttons

	protected Button editButton;
	protected Button addDependencyButton;
	protected Button removeDependencyButton;
	protected Button importButton;
	
	//Ok and Cancel buttons
	
	protected Button okButton;
	protected Button cancelButton;
	
	// The technical fragment to be created and stored in the repository
	
	private TechnicalFragment result;
	
	public CreateTechnicalFragmentDialog(Shell parentShell, RepositoryLocation location) {
		
		super(parentShell);
		
		this.location = location;
		this.parentShell = parentShell;
	}

	public TreeViewer getDependenciesTreeViewer() {
		return dependenciesTreeViewer;
	}
	
	public TechnicalFragment getResult() {
		return result;
	}
	
	protected void configureShell(Shell shell) {
        
		super.configureShell(shell);
        
        shell.setText("Create Technical Fragment");
		
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
        
    	okButton= createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        
    	okButton.setEnabled(false);
    	
    	cancelButton = createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
		
	}
	
	/*
	* Create the graphical elements (Grid layout)
	*/
	protected Control createDialogArea(Composite parent) {

		Composite composite = (Composite) super.createDialogArea(parent);
	        GridLayout compositeLayout = new GridLayout(2, false);
	        composite.setLayout(compositeLayout);
	        
	        Label hostLabel = new Label(composite, SWT.NONE);
	        hostLabel.setText("Dependencies Tree");
	        GridData gd = new GridData(GridData.BEGINNING);
	        gd.horizontalSpan = 2;
	        hostLabel.setLayoutData(gd);
	        
	        dependenciesTree = new Tree(composite, SWT.BORDER | SWT.SINGLE);
	        GridData gd2 = new GridData(GridData.FILL_BOTH);
	        gd2.widthHint = 300;
	        gd2.heightHint = 250;
	        gd2.verticalSpan = 4;
	        dependenciesTree.setLayoutData(gd2);
		
	        dependenciesTreeViewer = new TreeViewer(dependenciesTree);
	        ColumnViewerToolTipSupport.enableFor(dependenciesTreeViewer,ToolTip.NO_RECREATE);
	        dependenciesTreeViewer.setContentProvider(new DependenciesTreeContentProvider());
	        dependenciesTreeViewer.setLabelProvider(new DependenciesTreeLabelProvider());
	        dependenciesTreeViewer.setInput(location);
	        
	        editButton = new Button(composite, SWT.PUSH);
	        editButton.setText("Edit");
	        editButton.setFont(JFaceResources.getDialogFont());
	        GridData gd3 = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
	        editButton.setLayoutData(gd3);
	        
	        addDependencyButton = new Button(composite, SWT.PUSH);
	        addDependencyButton.setText("Add Dependency");
	        addDependencyButton.setFont(JFaceResources.getDialogFont());
	        GridData gd4 = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
	        addDependencyButton.setLayoutData(gd4);
        
	        removeDependencyButton = new Button(composite, SWT.PUSH);
	        removeDependencyButton.setText("Remove Dependency");
	        removeDependencyButton.setFont(JFaceResources.getDialogFont());
	        GridData gd5 = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
	        removeDependencyButton.setLayoutData(gd5);
	        
	        importButton = new Button(composite, SWT.PUSH);
	        importButton.setText("Import");
	        importButton.setFont(JFaceResources.getDialogFont());
	        GridData gd6 = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
	        importButton.setLayoutData(gd6);
	        
	        createContextMenu();
	        
	        hookListeners();
	        
	        return composite;
	}
	
	protected void createContextMenu() {
		
		final MenuManager menuMgr = new MenuManager();
	    menuMgr.setRemoveAllWhenShown(true);
	    Menu menu = menuMgr.createContextMenu(dependenciesTreeViewer.getControl());
	    dependenciesTreeViewer.getControl().setMenu(menu);
	    menuMgr.addMenuListener(new IMenuListener() {
			
	        public void menuAboutToShow(IMenuManager manager) {
	        	if(dependenciesTreeViewer.getSelection() instanceof StructuredSelection) {
	        		StructuredSelection selection = (StructuredSelection)dependenciesTreeViewer.getSelection();
	        		Object obj = selection.getFirstElement();
	        		if(obj instanceof TechnicalFragment) {
	        			TechnicalFragment technicalFragment = (TechnicalFragment) obj;
	        			CopyAction copyAction = new CopyAction(technicalFragment);
	        			menuMgr.add(copyAction);
	        			if(clipboard != null) {
	        				PasteAction pasteAction = new PasteAction(CreateTechnicalFragmentDialog.this);
	        				menuMgr.add(pasteAction);
	        			}
	        		}
	        	}
	        }
	    });
	}
	
	protected void hookListeners() {
		
		dependenciesTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				enableEditButton();
				enableAddButton();
				enableRemoveButton();
				enableImportButton();
			}
		});
		
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				super.widgetSelected(e);
				
				TechnicalFragment tf = getSelectedTechnicalFragment();
				if(tf != null) {
					List<IProject> fragmentPlugins = new ArrayList<IProject>();
					fragmentPlugins.addAll(tf.getPlugins());
					EditTechnicalFragmentDialog etfd = new EditTechnicalFragmentDialog(parentShell, tf.getName(), tf.getType(),
							tf.getOrigin(), tf.getObjective(), tf.getInput(), tf.getOutput(), fragmentPlugins);
					if(etfd.open() == Window.OK) {
						if(etfd.getName() != null) {
							tf.setName(etfd.getName());
						}
						if(etfd.getType() != null) {
							tf.setType(etfd.getType());
						}
						if(etfd.getOrigin() != null) {
							tf.setOrigin(etfd.getOrigin());
						}
						if(etfd.getObjective() != null) {
							tf.setObjective(etfd.getObjective());
						}
						if(etfd.getInput() != null) {
							tf.setInput(etfd.getInput());
						}
						if(etfd.getOutput() != null) {
							tf.setOutput(etfd.getOutput());
						}
						if(etfd.getPlugins() != null) {
							tf.setPlugins(etfd.getPlugins());
						}
						TechnicalFragment root = getRootFragment();
						resolveTechnicalFragment(root);
						dependenciesTreeViewer.refresh();
						enableOkButton();
					}
				}
			}
		});
		
		addDependencyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				super.widgetSelected(e);
				
				TechnicalFragment tf = getSelectedTechnicalFragment();
				if(tf != null) {
					TechnicalFragment dep = new TechnicalFragment("NewTechnicalFragment", "", "", "", "", "");
					dep.setParent(tf);
					tf.getDependencies().add(dep);
					dep.resolve();
					dependenciesTreeViewer.refresh();
					enableOkButton();
				}
			}
		});
		
		removeDependencyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				super.widgetSelected(e);
				
				TechnicalFragment tf = getSelectedTechnicalFragment();
				if(tf != null) {
					TechnicalFragment parent = tf.getParent();
					if(parent != null) {
						parent.getDependencies().remove(tf);
						TechnicalFragment root = getRootFragment();
						resolveTechnicalFragment(root);
						dependenciesTreeViewer.refresh();
						enableOkButton();
					}
				}
			}
		});
		
		importButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				super.widgetSelected(e);
				
				TechnicalFragment tf = getSelectedTechnicalFragment();
				if(tf != null) {
					ImportTechnicalFragmentDialog itfd = new ImportTechnicalFragmentDialog(parentShell, location, tf);
					if(itfd.open() == Window.OK) {
						TechnicalFragment result = itfd.getResult();
						if(result != null) {
							tf.getDependencies().add(result);
							result.setParent(tf);
							TechnicalFragment root = getRootFragment();
							resolveTechnicalFragment(root);
							dependenciesTreeViewer.refresh();
							enableOkButton();
							enableEditButton();
							enableAddButton();
							enableRemoveButton();
							enableImportButton();
						}
					}
				}
			}
		});
	}
	
	public TechnicalFragment getSelectedTechnicalFragment() {
		
		ISelection selection = dependenciesTreeViewer.getSelection();
		if(selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) selection;
			if(!sel.isEmpty()) {
				return (TechnicalFragment) sel.getFirstElement();
			}
		}
		
		return null;
	}
	
	public TechnicalFragment getRootFragment() {
		
		IContentProvider provider = dependenciesTreeViewer.getContentProvider();
		if(provider instanceof DependenciesTreeContentProvider) {
			DependenciesTreeContentProvider dtcp = (DependenciesTreeContentProvider) provider;
			return dtcp.getRootFragment();
		}
		
		return null;
	}
	
	public void enableOkButton() {
		
		TechnicalFragment tf = getRootFragment();
		
		boolean enabled = isResolved(tf);
		
		okButton.setEnabled(enabled);
	}
	
	public void enableEditButton() {
		
		TechnicalFragment selected = getSelectedTechnicalFragment();
		
		if(selected == null) {
			editButton.setEnabled(false);
		}
		else if(selected.isImported()) {
			editButton.setEnabled(false);
		}
		else {
			editButton.setEnabled(true);
		}
	}
	
	public void enableAddButton() {
		
		TechnicalFragment selected = getSelectedTechnicalFragment();
		
		if(selected == null) {
			addDependencyButton.setEnabled(false);
		}
		else if(selected.isImported()) {
			addDependencyButton.setEnabled(false);
		}
		else if(selected.isResolved()) {
			addDependencyButton.setEnabled(false);
		}
		else {
			addDependencyButton.setEnabled(true);
		}
	}
	
	public void enableRemoveButton() {
		
		TechnicalFragment selected = getSelectedTechnicalFragment();
		TechnicalFragment root = getRootFragment();
		
		if(selected == null) {
			removeDependencyButton.setEnabled(false);
		}
		else if(root != null && selected.equals(root)) {
			removeDependencyButton.setEnabled(false);
		}
		else if(selected.isImported() && selected.getParent().isImported()) {
			removeDependencyButton.setEnabled(false);
		}
		else {
			removeDependencyButton.setEnabled(true);
		}
	}
	
	public void enableImportButton() {
		
		TechnicalFragment selected = getSelectedTechnicalFragment();
		
		if(selected == null) {
			importButton.setEnabled(false);
		}
		else if(selected.isImported()) {
			importButton.setEnabled(false);
		}
		else if(selected.isResolved()) {
			importButton.setEnabled(false);
		}
		else {
			importButton.setEnabled(true);
		}
	}
	
	public void resolveTechnicalFragment(TechnicalFragment tf) {
		
		tf.resolve();
		
		List<TechnicalFragment> dependencies = tf.getDependencies();
		
		int i = 0;
		while(i < dependencies.size()) {
			TechnicalFragment dependency = dependencies.get(i);
			resolveTechnicalFragment(dependency);
			i++;
		}
	}
	
	public boolean isResolved(TechnicalFragment tf) {
		
		if(!tf.isResolved()) {
			return false;
		}
		
		List<TechnicalFragment> dependencies = tf.getDependencies();
		
		int i = 0;
		boolean resolved = true;
		while(i < dependencies.size() && resolved) {
			TechnicalFragment dependency = dependencies.get(i);
			resolved = isResolved(dependency);
			i++;
		}
		
		return resolved;
	}
	
	@Override
	protected void okPressed() {
		
		this.result = getRootFragment();
		
		super.okPressed();
	}
}
