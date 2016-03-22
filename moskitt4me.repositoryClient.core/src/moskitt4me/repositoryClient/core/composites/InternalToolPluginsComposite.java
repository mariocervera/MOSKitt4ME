package moskitt4me.repositoryClient.core.composites;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import moskitt4me.repositoryClient.core.dialogs.DefineInternalToolDialog;
import moskitt4me.repositoryClient.core.providers.InternalToolPluginsContentProvider;
import moskitt4me.repositoryClient.core.providers.PlatformPluginsContentProvider;
import moskitt4me.repositoryClient.core.providers.PlatformPluginsLabelProvider;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.service.resolver.BundleDescription;
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
import org.osgi.framework.Bundle;

/*
* A graphical widget that is used in the Define Internal Tool Dialog. It allows the user to select plug-ins
* that are installed in MOSKitt4ME and include them in the internal tool.
*
* @author Mario Cervera
*/
public class InternalToolPluginsComposite extends Composite {

	private List<String> platformPlugins;
	private List<String> fragmentPlugins;
	
	private Group pluginsGroup;
	
	// Tree viewer that shows the plug-ins installed in MOSKitt4ME
	
	private Label platformPluginsLabel;
	private Tree platformPluginsTree; 
	private TreeViewer platformPluginsTreeViewer;
	
	// Tree viewer that shows the plug-ins contained in the internal tool
	
	private Label fragmentPluginsLabel;
	private Tree fragmentPluginsTree;
	private TreeViewer fragmentPluginsTreeViewer;
	
	// Add and remove buttons
	
	private Button addButton;
	private Button removeButton;
	
	private PlatformPluginsContentProvider platformPluginsProvider = new PlatformPluginsContentProvider();
	
	private DefineInternalToolDialog parentDialog;
	
	public InternalToolPluginsComposite(Composite parent, int style,
			DefineInternalToolDialog parentDialog) {
		
		super(parent, style);
		
		this.fragmentPlugins = new ArrayList<String>();
		this.platformPlugins = new ArrayList<String>();
		loadPlatformPlugins();
		
		this.parentDialog = parentDialog;
		
		//Arrange the graphical elements of the composite (Grid layout)
		
		GridLayout compositeLayout = new GridLayout(1, false);
        	this.setLayout(compositeLayout);
		
		pluginsGroup = new Group(this, SWT.NONE);
		pluginsGroup.setText("Plug-ins");
		GridData gd = new GridData(GridData.FILL_BOTH);
		pluginsGroup.setLayoutData(gd);
		GridLayout groupLayout = new GridLayout(3, false);
		pluginsGroup.setLayout(groupLayout);
		
		platformPluginsLabel = new Label(pluginsGroup, SWT.NONE);
		platformPluginsLabel.setText("Platform plug-ins");
		GridData gd2 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd2.horizontalSpan = 2;
		platformPluginsLabel.setLayoutData(gd2);
		
		fragmentPluginsLabel = new Label(pluginsGroup, SWT.NONE);
		fragmentPluginsLabel.setText("Internal tool plug-ins");
		GridData gd3 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		fragmentPluginsLabel.setLayoutData(gd3);
		
		platformPluginsTree = new Tree(pluginsGroup, SWT.BORDER | SWT.MULTI);
		GridData gd4 = new GridData(GridData.FILL_BOTH);
		gd4.widthHint = 200;
		gd4.heightHint = 225;
	        platformPluginsTree.setLayoutData(gd4);
	        
	        platformPluginsTreeViewer = new TreeViewer(platformPluginsTree);
	        platformPluginsTreeViewer.setContentProvider(platformPluginsProvider);
	        platformPluginsTreeViewer.setLabelProvider(new PlatformPluginsLabelProvider());
	        platformPluginsTreeViewer.setInput(this.platformPlugins);
	        
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
	        
	        fragmentPluginsTree = new Tree(pluginsGroup, SWT.BORDER | SWT.MULTI);
		GridData gd8 = new GridData(GridData.FILL_BOTH);
		gd8.widthHint = 200;
		gd8.heightHint = 225;
		fragmentPluginsTree.setLayoutData(gd8);
        
		fragmentPluginsTreeViewer = new TreeViewer(fragmentPluginsTree);
		fragmentPluginsTreeViewer.setContentProvider(new InternalToolPluginsContentProvider());
		fragmentPluginsTreeViewer.setLabelProvider(new PlatformPluginsLabelProvider());
		fragmentPluginsTreeViewer.setInput(this.fragmentPlugins);
        
	        hookListeners();
	}
	
	public List<String> getFragmentPlugins() {
		return this.fragmentPlugins;
	}
	
	public List<String> getPlatformPlugins() {
		return platformPlugins;
	}
	
	public DefineInternalToolDialog getParentDialog() {
		return parentDialog;
	}
	
	protected void hookListeners() {
		
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				super.widgetSelected(e);
				
				List<String> selectedPlugins = getSelectedPlugins();
				for(String plugin : selectedPlugins) {
					if(!getFragmentPlugins().contains(plugin)) {
						getFragmentPlugins().add(plugin);
					}
				}
				fragmentPluginsTreeViewer.setInput(getFragmentPlugins());
				platformPluginsProvider.setPluginsToExclude(getFragmentPlugins());
				platformPluginsTreeViewer.setInput(getPlatformPlugins());
				
				enableOkButton();
			}
		});
		
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				super.widgetSelected(e);
				
				List<String> selectedPlugins = getSelectedFragmentPlugins();
				getFragmentPlugins().removeAll(selectedPlugins);
				
				fragmentPluginsTreeViewer.setInput(getFragmentPlugins());
				platformPluginsProvider.setPluginsToExclude(getFragmentPlugins());
				platformPluginsTreeViewer.setInput(getPlatformPlugins());
				
				enableOkButton();
			}
		});
	}
	
	private List<String> getSelectedPlugins() {
		
		List<String> result = new ArrayList<String>();
		
		ISelection selection = platformPluginsTreeViewer.getSelection();
		if(selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) selection;
			Object[] objects = sel.toArray();
			for(Object obj : objects) {
				if(obj instanceof String) {
					result.add((String)obj);
				}
			}
		}
		
		return result;
	}
	
	private List<String> getSelectedFragmentPlugins() {
		
		List<String> result = new ArrayList<String>();
		
		ISelection selection = fragmentPluginsTreeViewer.getSelection();
		if(selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) selection;
			Object[] objects = sel.toArray();
			for(Object obj : objects) {
				if(obj instanceof String) {
					result.add((String)obj);
				}
			}
		}
		
		return result;
	}
	
	private void loadPlatformPlugins() {

		BundleDescription[] descs = Platform.getPlatformAdmin().getState().getBundles();

		for (BundleDescription desc : descs) {
			String name = desc.getName();
			Bundle bundle = Platform.getBundle(name);
			if(bundle != null) {
				if (!this.platformPlugins.contains(name)) {
					this.platformPlugins.add(name);
				}
			}
		}

		Collections.sort(this.platformPlugins);
	}
	
	public void enableOkButton() {
		
		if(!getParentDialog().getName().equals("") && !getParentDialog().getName().contains(" ")
				&& !getParentDialog().getOrigin().equals("") 
				&& !getParentDialog().getObjective().equals("")
				&& !getParentDialog().getInput().equals("")
				&& !getParentDialog().getOutput().equals("")
				&& !getParentDialog().getDescription().equals("")
				&& getFragmentPlugins().size() > 0) {
			
			getParentDialog().getOkButton().setEnabled(true);
		}
		else {
			getParentDialog().getOkButton().setEnabled(false);
		}
	}
}
