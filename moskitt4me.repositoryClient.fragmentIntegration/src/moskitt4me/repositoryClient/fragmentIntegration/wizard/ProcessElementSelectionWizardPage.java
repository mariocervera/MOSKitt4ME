package moskitt4me.repositoryClient.fragmentIntegration.wizard;

import moskitt4me.repositoryClient.fragmentIntegration.providers.ElementsLabelProvider;
import moskitt4me.repositoryClient.fragmentIntegration.providers.ProcessElementsProvider;
import moskitt4me.repositoryClient.fragmentIntegration.util.IntegrationData;

import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.epf.uma.ProcessPackage;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;


public class ProcessElementSelectionWizardPage extends WizardPage implements Listener{

	private int DEFAULT_DIALOG_WIDTH = 100;
	private int DEFAULT_DIALOG_HEIGHT = 100;
	private int MIN_DIALOG_WIDTH = 100;
	private int MIN_DIALOG_HEIGHT = 100;

	private ILabelProvider labelProvider;
	private ITreeContentProvider contentProvider;
	
	private Tree contentElementTree;
	private TreeViewer contentElementTreeViewer;
	public CCombo typeCombo;

	
	private Object[] objects;
	private IntegrationData data;

	
	public ProcessElementSelectionWizardPage(Shell parentShell, Object[] objects, IntegrationData data) {
		
		super("Process Element Selection");
		
		setTitle("Process Element Selection");
		setMessage("Select the element of the process where the process fragment will be integrated");
		
		this.contentProvider = new ProcessElementsProvider(new ResourceItemProviderAdapterFactory());
		labelProvider = new ElementsLabelProvider();
		this.objects = objects;
		this.data = data;
		
	}
	
	protected void configureShell(Shell shell) {
		shell.setMinimumSize(MIN_DIALOG_WIDTH, MIN_DIALOG_HEIGHT);

		
	}

	public void createControl(Composite parent) {

		Composite dialogComposite = new Composite(parent, SWT.NULL);
		dialogComposite.setSize(100, 100);
		
	        
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
				if(event.getSelection() instanceof StructuredSelection ) {
					Object selectedElement = 
						((StructuredSelection)event.getSelection()).getFirstElement();
					if(selectedElement instanceof ProcessPackage){
						data.setElement(selectedElement);
						setPageComplete(true);
					}
					else {
						data.setElement(null);
						setPageComplete(false);
					}
				}
			}
		});
		
		
		
		
		
		Label typeLabel = new Label(dialogComposite, SWT.NONE);
		typeLabel.setText("Select the integration type:");
        GridData gd1 = new GridData();
        typeLabel.setLayoutData(gd1);
        
		typeCombo = new CCombo(dialogComposite, SWT.BORDER);
        typeCombo.setItems(new String[]{"Copy", "Extend"});
        typeCombo.setVisibleItemCount(2);
        typeCombo.setText("Copy");
        
        GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
        gd4.widthHint = 250;
        typeCombo.setLayoutData(gd4);
        typeCombo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				data.setType(typeCombo.getText());
			}
			
			});
        
        Label error = new Label(dialogComposite, SWT.NONE);
		error.setText("");
	        GridData gd = new GridData();
	        error.setLayoutData(gd);
		
        setControl(dialogComposite);		
	}
	
	
	
	public void setContentProvider(ITreeContentProvider contentProvider) {
		this.contentProvider = contentProvider;
	}
	
	public TreeViewer getViewer() {
		return this.contentElementTreeViewer;
	}
	
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		
	}

	
	
	
}
