package moskitt4me.repositoryClient.fragmentIntegration.util;

import java.util.ArrayList;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.epf.uma.CapabilityPattern;

/**Is the information related to the integration of a fragment of type process
 * Use: Where the wizard saves de input information
 */
public class IntegrationData {

	private Object element;
	private Object folder;
	private Object contentFolder;
	private String type;
	private ArrayList<CapabilityPattern> existingPatterns;
	
	public IntegrationData(){
		this.type = "Copy";
		existingPatterns = new ArrayList<CapabilityPattern>();
	}
	public IntegrationData(Object element, Object folder, Object contentFolder, String type){
		this.element = element;
		this.folder = folder;
		this.type = type;
		this.contentFolder = contentFolder;
		existingPatterns = new ArrayList<CapabilityPattern>();
	}

	public void setElement(Object element) {
		this.element = element;
	}

	public Object getElement() {
		return element;
	}

	public void setFolder(Object folder) {
		this.folder = folder;
	}

	public Object getFolder() {
		return folder;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	
	public void setExistingPattern(ArrayList<CapabilityPattern> existingPattern) {
		this.existingPatterns = existingPattern;
	}
	public ArrayList<CapabilityPattern> getExistingPattern() {
		return existingPatterns;
	}
	public Object getContentFolder() {
		// TODO Auto-generated method stub
		return contentFolder;
	}
	public void setContentFolder(Object folder) {
		this.contentFolder = folder;
	}
	
	
	
	
}
