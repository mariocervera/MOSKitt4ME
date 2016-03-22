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

/*
* A dialog that allows the user to add Repository Locations to the Repositories view. 
* A repository location is a graphical element that points to a FTP repository that contains
* reusable assets. To create a new repository location, the user needs to specify host,
* repository path, username, and password.
*
* @author Mario Cervera
*/
public class AddRepositoryLocationDialog extends Dialog {

    private String host;
    private String repositoryPath;
    private String user;
    private String password;
    
    private Text hostText;
    private Text repositoryPathText;
    private Text userText;
    private Text passwordText;
    
    protected Button okButton;
    protected Button cancelButton;

    public AddRepositoryLocationDialog(Shell parentShell) {
    	
        super(parentShell);
        
        this.host = "";
        this.repositoryPath = "";
        this.user = "";
        this.password = "";
    }
    
    public String getHost() {
    	return host;
    }
    
    public String getRepositoryPath() {
    	return repositoryPath;
    }
    
    public String getUser() {
    	return user;
    }
    
    public String getPassword() {
    	return password;
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        
        shell.setText("Add Repository Location");
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
        
        Label hostLabel = new Label(composite, SWT.NONE);
        hostLabel.setText("Host:");
        hostLabel.setLayoutData(new GridData(GridData.CENTER));
        
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL);
        gd.widthHint = 200;
        
        this.hostText = new Text(composite, getTextStyle());
        this.hostText.setLayoutData(gd);
        this.hostText.setFocus();
        
        Label repositoryPathLabel = new Label(composite, SWT.NONE);
        repositoryPathLabel.setText("Repository Path:");
        repositoryPathLabel.setLayoutData(new GridData(GridData.CENTER));
        
        this.repositoryPathText = new Text(composite, getTextStyle());
        this.repositoryPathText.setLayoutData(gd);
        
        Label userLabel = new Label(composite, SWT.NONE);
        userLabel.setText("User:");
        userLabel.setLayoutData(new GridData(GridData.CENTER));
        
        this.userText = new Text(composite, getTextStyle());
        this.userText.setLayoutData(gd);
        
        Label passwordLabel = new Label(composite, SWT.NONE);
        passwordLabel.setText("Password:");
        passwordLabel.setLayoutData(new GridData(GridData.CENTER));
        
        this.passwordText = new Text(composite, getTextStyle());
        this.passwordText.setEchoChar('*');
        this.passwordText.setLayoutData(gd);

        applyDialogFont(composite);
        
        hookListeners();
        
        return composite;
    }
    
	protected int getTextStyle() {
		return SWT.SINGLE | SWT.BORDER;
	}
	
	protected void hookListeners() {
		
		hostText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				host = hostText.getText();
				enableOkButton();
			}
		});
		
		repositoryPathText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				repositoryPath = repositoryPathText.getText();
				enableOkButton();
			}
		});
		
		userText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				user = userText.getText();
				enableOkButton();
			}
		});
		
		passwordText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				password = passwordText.getText();
				enableOkButton();
			}
		});
	}
	
	private void enableOkButton() {
		
		if(host != null && repositoryPath != null && user != null
				&& password != null && !host.equals("") && !repositoryPath.equals("")
				&& !user.equals("") && !password.equals("")) {
			
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}
	}
}
