package moskitt4me.repositoryClient.core.dialogs;

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

public class SearchDialog extends Dialog {
	
    private String type;
    private String origin;
    private String objective;
    private String input;
    private String output;
    
    private Text typeText;
    private Text originText;
    private Text objectiveText;
    private Text inputText;
    private Text outputText;
    
    protected Button okButton;
    protected Button cancelButton;

    public SearchDialog(Shell parentShell) {
    	
        super(parentShell);
        
        this.type = "";
        this.origin = "";
        this.objective = "";
        this.input = "";
        this.output = "";
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
    
    public String getInput() {
		return input;
	}
    
    public String getOutput() {
		return output;
	}

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        
        shell.setText("Search");
    }

    protected void createButtonsForButtonBar(Composite parent) {
        
        okButton = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        
        okButton.setEnabled(false);
        
        cancelButton = createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

    protected Control createDialogArea(Composite parent) {
    	
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout compositeLayout = new GridLayout(2, false);
        composite.setLayout(compositeLayout);
        
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL);
        gd.widthHint = 200;
        
        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText("Type:");
        typeLabel.setLayoutData(new GridData(GridData.CENTER));
        
        this.typeText = new Text(composite, getTextStyle());
        this.typeText.setLayoutData(gd);
        this.typeText.setFocus();
        
        Label originLabel = new Label(composite, SWT.NONE);
        originLabel.setText("Origin:");
        originLabel.setLayoutData(new GridData(GridData.CENTER));
        
        this.originText = new Text(composite, getTextStyle());
        this.originText.setLayoutData(gd);
        
        Label objectiveLabel = new Label(composite, SWT.NONE);
        objectiveLabel.setText("Objective:");
        objectiveLabel.setLayoutData(new GridData(GridData.CENTER));
        
        this.objectiveText = new Text(composite, getTextStyle());
        this.objectiveText.setLayoutData(gd);
        
        Label inputLabel = new Label(composite, SWT.NONE);
        inputLabel.setText("Input:");
        inputLabel.setLayoutData(new GridData(GridData.CENTER));
        
        this.inputText = new Text(composite, getTextStyle());
        this.inputText.setLayoutData(gd);
        
        Label outputLabel = new Label(composite, SWT.NONE);
        outputLabel.setText("Output:");
        outputLabel.setLayoutData(new GridData(GridData.CENTER));
        
        this.outputText = new Text(composite, getTextStyle());
        this.outputText.setLayoutData(gd);

        applyDialogFont(composite);
        
        hookListeners();
        
        return composite;
    }
    
	protected int getTextStyle() {
		return SWT.SINGLE | SWT.BORDER;
	}
	
	protected void hookListeners() {
		
		typeText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				type = typeText.getText();
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
	}
	
	private void enableOkButton() {
		
		if(!type.equals("") || !origin.equals("") || !objective.equals("") ||
				!input.equals("") || !output.equals("")) {
			
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}
	}
}
