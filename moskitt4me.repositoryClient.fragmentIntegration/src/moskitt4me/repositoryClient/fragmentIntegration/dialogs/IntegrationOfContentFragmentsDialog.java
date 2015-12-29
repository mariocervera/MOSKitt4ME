package moskitt4me.repositoryClient.fragmentIntegration.dialogs;

import moskitt4me.repositoryClient.fragmentIntegration.providers.ContentLibrariesProvider;
import moskitt4me.repositoryClient.fragmentIntegration.providers.ElementsLabelProvider;

import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.epf.uma.ContentPackage;
import org.eclipse.epf.uma.ProcessComponent;
import org.eclipse.epf.uma.ProcessPackage;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.SelectionDialog;

/**This dialog allows the user to integrate an existing fragment of Content type*/

public class IntegrationOfContentFragmentsDialog extends SelectionDialog {

	private int DEFAULT_DIALOG_WIDTH = 400;
	private int DEFAULT_DIALOG_HEIGHT = 300;
	private int MIN_DIALOG_WIDTH = 300;
	private int MIN_DIALOG_HEIGHT = 300;

	private ILabelProvider labelProvider;
	private ITreeContentProvider contentProvider;
	
	private Tree contentElementTree;
	private TreeViewer contentElementTreeViewer;
	public CCombo typeCombo;

	
	private Object[] objects;
	public String type;

	
	public IntegrationOfContentFragmentsDialog(Shell parentShell, Object[] objects, String type) {
		
		super(parentShell);
		setLabelProvider(new ElementsLabelProvider());
		setContentProvider(new ContentLibrariesProvider(new ResourceItemProviderAdapterFactory()));
		setTitle("Content Package Selection");
		setMessage("Select the content package where the fragment will be stored");
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.type = type;
		this.objects = objects;
	}
	
	protected void configureShell(Shell shell) {
		shell.setMinimumSize(MIN_DIALOG_WIDTH, MIN_DIALOG_HEIGHT);

		super.configureShell(shell);
	}

	protected Control createDialogArea(Composite parent) {

		Composite dialogComposite = (Composite) super.createDialogArea(parent);

		Label locationLabel = new Label(dialogComposite, SWT.NONE);
		locationLabel.setText("Select the content package where the fragment will be stored");
	        GridData gd = new GridData();
	        locationLabel.setLayoutData(gd);
	        
		GridLayout dialogLayout = new GridLayout();
		dialogLayout.marginWidth = 10;
		dialogLayout.marginHeight = 10;
		GridData dialogLayoutData = new GridData(GridData.FILL_BOTH);
		dialogLayoutData.widthHint = DEFAULT_DIALOG_WIDTH;
		dialogLayoutData.heightHint = DEFAULT_DIALOG_HEIGHT;
		dialogComposite.setLayout(dialogLayout);
		dialogComposite.setLayoutData(dialogLayoutData);

		contentElementTree = new Tree(dialogComposite, SWT.BORDER | SWT.SINGLE);
		contentElementTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		contentElementTreeViewer = new TreeViewer(contentElementTree);
		contentElementTreeViewer.setLabelProvider(this.labelProvider);
		contentElementTreeViewer.setContentProvider(this.contentProvider);
		contentElementTreeViewer.setSorter(null);
		contentElementTreeViewer.setInput(this.objects);
		
		contentElementTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				
				if(event.getSelection() instanceof StructuredSelection) {
					Object selectedElement = 
						((StructuredSelection)event.getSelection()).getFirstElement();
					
					if(selectedElement instanceof ContentPackage
							|| selectedElement instanceof ProcessComponent
							|| selectedElement instanceof ProcessPackage) {
						getOkButton().setEnabled(true);
					}
					else getOkButton().setEnabled(false);
				}
			}
		});
		return dialogComposite;
	}
	
	public void setLabelProvider(ILabelProvider provider) {
		this.labelProvider = provider;
	}
	

	public void setContentProvider(ITreeContentProvider contentProvider) {
		this.contentProvider = contentProvider;
	}
	
	public TreeViewer getViewer() {
		return this.contentElementTreeViewer;
	}
	
	@Override
	protected void okPressed() {
	
		if(contentElementTreeViewer.getSelection() instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) contentElementTreeViewer.getSelection();
			setResult(selection.toList());
		}
		
		super.okPressed();
	}

}
