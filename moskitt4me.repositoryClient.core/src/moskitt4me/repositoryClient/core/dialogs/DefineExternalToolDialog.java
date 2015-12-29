package moskitt4me.repositoryClient.core.dialogs;

import moskitt4me.repositoryClient.core.util.ExternalToolFragment;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DefineExternalToolDialog extends Dialog {
	
	private String name;
	private Text nameText;
	
	private String origin;
	private Text originText;
	
	private String objective;
	private Text objectiveText;
	
	private String input;
	private Text inputText;
	
	private String output;
	private Text outputText;
	
	private String fileExtension;
	private Text fileExtensionText;
	
	private String description;
	private Text descriptionText;
	
	protected Button okButton;
	protected Button cancelButton;
	
	private ExternalToolFragment result;
	
	public DefineExternalToolDialog(Shell parentShell) {
		
		super(parentShell);
	}
	
	public ExternalToolFragment getResult() {
		return result;
	}
	
	protected void configureShell(Shell shell) {
        
		super.configureShell(shell);
        
        shell.setText("Define External Tool");
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
        GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
        gd2.widthHint = 350;
        nameText.setLayoutData(gd2);
        
        Label originLabel = new Label(composite, SWT.NONE);
        originLabel.setText("Origin:");
        GridData gd3 = new GridData();
        nameLabel.setLayoutData(gd3);
        
        originText = new Text(composite, SWT.BORDER);
        GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
        originText.setLayoutData(gd4);
        
        Label objectiveLabel = new Label(composite, SWT.NONE);
        objectiveLabel.setText("Objective:");
        GridData gd5 = new GridData();
        objectiveLabel.setLayoutData(gd5);
        
        objectiveText = new Text(composite, SWT.BORDER);
        GridData gd6 = new GridData(GridData.FILL_HORIZONTAL);
        objectiveText.setLayoutData(gd6);
        
        Label inputLabel = new Label(composite, SWT.NONE);
        inputLabel.setText("Input:");
        GridData gd7 = new GridData();
        inputLabel.setLayoutData(gd7);
        
        inputText = new Text(composite, SWT.BORDER);
        GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
        inputText.setLayoutData(gd8);
        
        Label outputLabel = new Label(composite, SWT.NONE);
        outputLabel.setText("Output:");
        GridData gd9 = new GridData();
        outputLabel.setLayoutData(gd9);
        
        outputText = new Text(composite, SWT.BORDER);
        GridData gd10 = new GridData(GridData.FILL_HORIZONTAL);
        outputText.setLayoutData(gd10);
        
        Label fileExtensionLabel = new Label(composite, SWT.NONE);
        fileExtensionLabel.setText("File Extension:");
        GridData gd11 = new GridData();
        fileExtensionLabel.setLayoutData(gd11);
        
        fileExtensionText = new Text(composite, SWT.BORDER);
        GridData gd12 = new GridData(GridData.FILL_HORIZONTAL);
        fileExtensionText.setLayoutData(gd12);
        
        Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setText("Description:");
        GridData gd13 = new GridData();
        descriptionLabel.setLayoutData(gd13);
        
        descriptionText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData gd14 = new GridData(GridData.FILL_BOTH);
        gd14.heightHint = 120;
        descriptionText.setLayoutData(gd14);
        
        hookListeners();
        
        return composite;
	}
	
	protected void hookListeners() {
		
		nameText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				name = nameText.getText();
				enableOkButton();
			}
		});
		
		originText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				origin = originText.getText();
				enableOkButton();
			}
		});
		
		objectiveText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				objective = objectiveText.getText();
				enableOkButton();
			}
		});
		
		inputText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				input = inputText.getText();
				enableOkButton();
			}
		});
		
		outputText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				output = outputText.getText();
				enableOkButton();
			}
		});
		
		fileExtensionText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				fileExtension = fileExtensionText.getText();
				enableOkButton();
			}
		});

		descriptionText.addModifyListener(new ModifyListener() {
	
			public void modifyText(ModifyEvent e) {
				
				description = descriptionText.getText();
				enableOkButton();
			}
		});
	}
	
	private void enableOkButton() {
		
		if(!name.equals("") && !name.contains(" ") && !origin.equals("")
				&& !objective.equals("") && !input.equals("") &&
				!output.equals("") && !fileExtension.equals("") &&
				!description.equals("")) {
			
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}
	}
	
	@Override
	protected void okPressed() {
		
		this.result = new ExternalToolFragment(name, origin, objective, input,
				output, fileExtension, description);
		
		super.okPressed();
	}
}
