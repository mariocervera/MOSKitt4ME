package moskitt4me.repositoryClient.fragmentIntegration.dialogs;

import java.util.ArrayList;
import java.util.List;

import moskitt4me.repositoryClient.fragmentIntegration.providers.ContentElementsForConceptualFragmentsProvider;
import moskitt4me.repositoryClient.fragmentIntegration.providers.ElementsLabelProvider;
import moskitt4me.repositoryClient.fragmentIntegration.providers.ProcessElementsForConceptualFragmentsProvider;

import org.eclipse.epf.uma.ProcessComponent;
import org.eclipse.epf.uma.impl.RoleImpl;
import org.eclipse.epf.uma.impl.TaskImpl;
import org.eclipse.epf.uma.impl.WorkProductImpl;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

public class ConceptualFragmentElementsComposite extends Composite {

	private List<Object> elements;
	
	private Composite elementsComposite;
	
	private Label wsElementsLabel;
	public Tree wsElementsTree; 
	public TreeViewer wsElementsTreeViewer;
	
	private Label assetElementsLabel;
	private Tree assetElementsTree;
	public TreeViewer assetElementsTreeViewer;
	public Button check;
	private Button addButton;
	private Button removeButton;

	public String type;
	
	private CreateConceptualFragmentDialog parentDialog;
	
	public ConceptualFragmentElementsComposite(Composite parent, int style, Object[] methodPlugins, String type,
			CreateConceptualFragmentDialog parentDialog) {
		
		super(parent, style);
		
		this.elements = new ArrayList<Object>();
		this.type = type;
		GridLayout compositeLayout = new GridLayout(1, false);
        this.setLayout(compositeLayout);
        
        
        elementsComposite = new Composite(this, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		elementsComposite.setLayoutData(gd);
		GridLayout groupLayout = new GridLayout(3, false);
		elementsComposite.setLayout(groupLayout);
		
		wsElementsLabel = new Label(elementsComposite, SWT.NONE);
		wsElementsLabel.setText("Library content");
		GridData gd2 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd2.horizontalSpan = 2;
		wsElementsLabel.setLayoutData(gd2);
		
		assetElementsLabel = new Label(elementsComposite, SWT.NONE);
		assetElementsLabel.setText("Conceptual fragment content");
		GridData gd3 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		assetElementsLabel.setLayoutData(gd3);
		
		wsElementsTree = new Tree(elementsComposite, SWT.BORDER | SWT.MULTI);
		GridData gd4 = new GridData(GridData.FILL_BOTH);
		gd4.widthHint = 200;
		gd4.heightHint = 225;
        wsElementsTree.setLayoutData(gd4);
        
        wsElementsTreeViewer = new TreeViewer(wsElementsTree);
        wsElementsTreeViewer.setContentProvider( new ContentElementsForConceptualFragmentsProvider(this.type, this.elements));
        wsElementsTreeViewer.setLabelProvider(new ElementsLabelProvider());
        wsElementsTreeViewer.setInput(methodPlugins);
        
        Composite c1 = new Composite(elementsComposite, SWT.NONE);
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
        
        assetElementsTree = new Tree(elementsComposite, SWT.BORDER | SWT.MULTI);
		GridData gd8 = new GridData(GridData.FILL_BOTH);
		gd8.widthHint = 200;
		gd8.heightHint = 225;
        assetElementsTree.setLayoutData(gd8);
        
        assetElementsTreeViewer = new TreeViewer(assetElementsTree);
        assetElementsTreeViewer.setContentProvider(new ContentElementsForConceptualFragmentsProvider(this.type, this.elements));
        assetElementsTreeViewer.setLabelProvider(new ElementsLabelProvider());
        assetElementsTreeViewer.setInput(this.elements);
        
        check =  new Button(this, SWT.CHECK);
        check.setText("Add related content elements");
        GridData gd14 = new GridData(GridData.FILL_HORIZONTAL);
        check.setLayoutData(gd14);
        
        this.parentDialog = parentDialog;
        
        hookListeners();
	}
	

	public List<Object> getElements() {
		return this.elements;
	}
	
	public boolean integrateReferences(){
		return (check.getSelection() && check.getEnabled());
	}
	protected void hookListeners() {
	
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				super.widgetSelected(e);
				
				List<Object> selectedElements = getSelectedElements();
				for(Object element : selectedElements) {
					if(!getElements().contains(element) && type.equals("Process")&& element instanceof ProcessComponent){
						getElements().add(element);
					}
					else
					if(!getElements().contains(element) && ( element instanceof TaskImpl 
							|| element instanceof RoleImpl || element instanceof WorkProductImpl)) {
						if(type.equals("Content Element")){ 
							getElements().add(element); 
							}
						else if(element instanceof TaskImpl && type.equals("Task")){
							getElements().add(element);
							}
						else if(element instanceof RoleImpl && type.equals("Role")){
							getElements().add(element);
							}
						else if(element instanceof WorkProductImpl && type.equals("Work Product")){
							getElements().add(element);
							}
					}
				}
				
				assetElementsTreeViewer.setInput(getElements());
				assetElementsTreeViewer.refresh(true);
				
				if(type.equals("Process"))
					wsElementsTreeViewer.setContentProvider( new ProcessElementsForConceptualFragmentsProvider(elements));
				else wsElementsTreeViewer.setContentProvider( new ContentElementsForConceptualFragmentsProvider(type, elements));
				
				parentDialog.enableOkButton();
			}
		});
		
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				super.widgetSelected(e);
				
				List<Object> selectedElements = getSelectedAssetElements();
				getElements().removeAll(selectedElements);
				
				assetElementsTreeViewer.setInput(getElements());
				assetElementsTreeViewer.refresh(true);
				if(type.equals("Process"))
					wsElementsTreeViewer.setContentProvider( new ProcessElementsForConceptualFragmentsProvider(elements));
				else wsElementsTreeViewer.setContentProvider( new ContentElementsForConceptualFragmentsProvider(type, elements));
				
				parentDialog.enableOkButton();
			}
		});
	}
	
	private List<Object> getSelectedElements() {
		
		List<Object> result = new ArrayList<Object>();
		
		ISelection selection = wsElementsTreeViewer.getSelection();
		if(selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) selection;
			Object[] objects = sel.toArray();
			for(Object obj : objects) {
				if(obj instanceof Object) {
					result.add((Object)obj);
				}
			}
		}
		
		return result;
	}
	
	private List<Object> getSelectedAssetElements() {
		
		List<Object> result = new ArrayList<Object>();
		
		ISelection selection = assetElementsTreeViewer.getSelection();
		if(selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) selection;
			Object[] objects = sel.toArray();
			for(Object obj : objects) {
				if(obj instanceof Object) {
					result.add((Object)obj);
				}
			}
		}
		
		return result;
	}


	public void changeToProcessFragment() {
		// TODO Auto-generated method stub
	/*	assetElementsLabel.setVisible(false);
		addButton.setVisible(false);
		removeButton.setVisible(false);
		assetElementsTree.setVisible(false);
		check.setVisible(false);*/
	
		
		wsElementsTreeViewer.setContentProvider(new ProcessElementsForConceptualFragmentsProvider(this.elements));
		
	}


	public void changeToContentFragment() {
		// TODO Auto-generated method stub
		/*assetElementsLabel.setVisible(true);
		addButton.setVisible(true);
		removeButton.setVisible(true);
		assetElementsTree.setVisible(true);
		check.setVisible(true);
		*/
		
		wsElementsTreeViewer.setContentProvider(new ContentElementsForConceptualFragmentsProvider(this.type, this.elements));
	}
}
