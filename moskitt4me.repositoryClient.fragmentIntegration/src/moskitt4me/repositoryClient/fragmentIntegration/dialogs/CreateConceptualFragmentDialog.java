package moskitt4me.repositoryClient.fragmentIntegration.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.epf.uma.Role;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.WorkProduct;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**This dialog allows the user to create a Conceptual fragment*/

public class CreateConceptualFragmentDialog extends Dialog {
	
	private String name;
	private Text nameText;
	
	private String type;
	public CCombo typeCombo;
	
	private String origin;
	private Text originText;
	
	private String objective;
	private Text objectiveText;
	private Boolean integrateReferences;

	private List<Object> contentElements;
	private ConceptualFragmentElementsComposite conceptualElementsComposite;
	private Group conceptualElementsGroup;
	
	private Object[] methodPlugins;

	protected Button okButton;
	protected Button cancelButton;
	
	public CreateConceptualFragmentDialog(Shell parentShell,  Object[] methodPlugins) {
		
		super(parentShell);
		
		this.methodPlugins = methodPlugins;
		setIntegrateReferences(false);
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public String getOrigin() {
		return origin;
	}
	
	public String getObjective() {
		return objective;
	}
	
	public List<Object> getElements() {
		return contentElements;
	}

	protected void configureShell(Shell shell) {
        
		super.configureShell(shell);
        
        shell.setText("Create Conceptual Fragment");
    }
	
	protected void createButtonsForButtonBar(Composite parent) {
        
    	okButton= createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        
    	okButton.setEnabled(false);
    	
    	cancelButton = createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }
	
	protected Control createDialogArea(Composite parent) {

		Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout compositeLayout = new GridLayout(2, false);
        composite.setLayout(compositeLayout);
        
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");
        GridData gd = new GridData();
        nameLabel.setLayoutData(gd);
        
        nameText = new Text(composite, SWT.BORDER);
        if(this.name != null) {
        	nameText.setText(this.name);
        }
        GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
        gd2.widthHint = 250;
        nameText.setLayoutData(gd2);
        
        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText("Type:");
        GridData gd3 = new GridData();
        typeLabel.setLayoutData(gd3);
        
        typeCombo = new CCombo(composite, SWT.BORDER);
        typeCombo.setItems(new String[]{"Role", "Task", 
        		"Work Product", "Content Element", "Process"});
        typeCombo.setVisibleItemCount(6);
        typeCombo.setText("Content Element");
        if(type != null) {
        	typeCombo.setText(type);
        }
        GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
        gd4.widthHint = 250;
        typeCombo.setLayoutData(gd4);
        
        Label originLabel = new Label(composite, SWT.NONE);
        originLabel.setText("Origin:");
        GridData gd5 = new GridData();
        nameLabel.setLayoutData(gd5);
        
        originText = new Text(composite, SWT.BORDER);
        if(this.origin != null) {
        	originText.setText(this.origin);
        }
        if(this.type != null && this.type.equals("Others")) {
        	originText.setEnabled(false);
        	originText.setText("");
        }
        GridData gd6 = new GridData(GridData.FILL_HORIZONTAL);
        gd6.widthHint = 250;
        originText.setLayoutData(gd6);
        
        Label objectiveLabel = new Label(composite, SWT.NONE);
        objectiveLabel.setText("Objective:");
        GridData gd7 = new GridData();
        objectiveLabel.setLayoutData(gd7);
        
        objectiveText = new Text(composite, SWT.BORDER);
        if(this.objective != null) {
        	objectiveText.setText(this.objective);
        }
        if(this.type != null && this.type.equals("Others")) {
        	objectiveText.setEnabled(false);
        	objectiveText.setText("");
        }
        GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
        gd8.widthHint = 250;
        objectiveText.setLayoutData(gd8);
        
        conceptualElementsGroup = new Group(composite, SWT.NONE);
        conceptualElementsGroup.setText("Content");
        GridData gd13 = new GridData(GridData.FILL_HORIZONTAL);
        gd13.horizontalSpan = 2;
        conceptualElementsGroup.setLayoutData(gd13);
        GridLayout groupLayout = new GridLayout(1, true);
        conceptualElementsGroup.setLayout(groupLayout);
        
        conceptualElementsComposite = new ConceptualFragmentElementsComposite(conceptualElementsGroup, SWT.NONE, methodPlugins, typeCombo.getText(), this);
        GridData gd14 = new GridData(GridData.FILL_HORIZONTAL);
        conceptualElementsComposite.setLayoutData(gd14);
       
        hookListeners();
        
        return composite;
	}
	
	protected void hookListeners() {
		
		typeCombo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				conceptualElementsComposite.type = typeCombo.getText();
				
				String text = typeCombo.getText();
				
				ArrayList<Object> elementsCopy = new ArrayList<Object>();
				
				for(Object o: conceptualElementsComposite.getElements())elementsCopy.add(o);
				
				
				 if(text.equals("Process")){
					 conceptualElementsComposite.changeToProcessFragment();
					 conceptualElementsComposite.check.setEnabled(false);
					for(Object o : elementsCopy){
						if(o instanceof Task || o instanceof Role|| o instanceof WorkProduct){
							conceptualElementsComposite.getElements().remove(o);
						}
					}
				}
				else {
					originText.setEnabled(true);
					objectiveText.setEnabled(true);
					conceptualElementsComposite.changeToContentFragment();
					
				
					if(text.equals("Content Element"))
						conceptualElementsComposite.check.setEnabled(true);
					else conceptualElementsComposite.check.setEnabled(false);
							
					for(Object o : elementsCopy){
						if(text.equals("Content Element")){ 
							if(o instanceof Task||o instanceof Role||o instanceof WorkProduct);
							else conceptualElementsComposite.getElements().remove(o);
						}
						else{
							if(text.equals("Task") && !(o instanceof Task)){
								conceptualElementsComposite.getElements().remove(o);
							}
							if(text.equals("Role") && !(o instanceof Role)){
								conceptualElementsComposite.getElements().remove(o);
							}
							if(text.equals("Work Product") && !(o instanceof WorkProduct)){
								conceptualElementsComposite.getElements().remove(o);
							}
						}
					}
				}
				 conceptualElementsComposite.assetElementsTreeViewer.setInput(conceptualElementsComposite.getElements());
				conceptualElementsComposite.assetElementsTreeViewer.refresh(true);
				}
			
		});
		
		nameText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				enableOkButton();
			}
		});
		
		originText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				enableOkButton();
			}
		});
		
		objectiveText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				enableOkButton();
			}
		});
	}
	
	@Override
	protected void okPressed() {
		this.name = nameText.getText();
		this.type = typeCombo.getText();
		this.origin = originText.getText();
		this.objective = objectiveText.getText();
		
		this.contentElements = conceptualElementsComposite.getElements();
		this.integrateReferences = conceptualElementsComposite.integrateReferences();
		
		
		if(this.getName()=="" && this.getObjective()=="" && this.getOrigin()=="" )
			MessageDialog.openError(getShell(), "Content Element creation ", "There are empty attributes");
		else if(this.getElements().isEmpty())
			MessageDialog.openError(getShell(), "Content Element creation ", "Please select any element to create the fragment");
		else
			super.okPressed();
	
	}

	public void setIntegrateReferences(Boolean integrateReferences) {
		this.integrateReferences = integrateReferences;
	}

	public Boolean getIntegrateReferences() {
		return integrateReferences;
	}

	public void enableOkButton() {
		
		boolean enabled = true;
		
		if(nameText.getText() == null || nameText.getText().equals("")) {
			enabled = false;
		}
		else if(originText.getText() == null || originText.getText().equals("")) {
			enabled = false;
		}
		else if(objectiveText.getText() == null || objectiveText.getText().equals("")) {
			enabled = false;
		}
		else if(conceptualElementsComposite.getElements() == null || conceptualElementsComposite.getElements().size() == 0) {
			enabled = false;
		}
		
		okButton.setEnabled(enabled);
	}
	
}
