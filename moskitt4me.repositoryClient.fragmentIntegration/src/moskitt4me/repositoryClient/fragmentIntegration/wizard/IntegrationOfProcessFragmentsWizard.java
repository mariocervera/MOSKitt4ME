package moskitt4me.repositoryClient.fragmentIntegration.wizard;

import java.util.ArrayList;

import moskitt4me.repositoryClient.fragmentIntegration.util.ContentItem;
import moskitt4me.repositoryClient.fragmentIntegration.util.IntegrationData;

import org.eclipse.epf.library.LibraryService;
import org.eclipse.epf.uma.MethodLibrary;
import org.eclipse.epf.uma.ProcessPackage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**The wizard for the integration of conceptual fragments of process type
 * It has 2 WizardPages: the process element selection and the location selection.
 */

public class IntegrationOfProcessFragmentsWizard extends Wizard implements INewWizard{

	public ProcessElementSelectionWizardPage ces;
	public LocationSelectionWizardPage ls;
	private MethodLibrary library;
	public IntegrationData data;
	public ArrayList<ContentItem> items;
	private ContentLocationSelectionWizardPage cls;
	private boolean containsContentElements;

	int patternCounter;
	
	public IntegrationOfProcessFragmentsWizard(ArrayList<ContentItem> contentItems){
		super();
		data = new IntegrationData();
		items = contentItems;
		containsContentElements=containsContentElements(items);
		setWindowTitle("Process Fragment Integration");
		
		patternCounter = 0;
		for(ContentItem ci : items)
			if(ci.getAttributes().get("xsi:type").equals("org.eclipse.epf.uma:CapabilityPattern"))
				patternCounter++;
		
		
	}
	
	 public boolean performCancel() {
		 data = new IntegrationData();
	        return true;
	    }
	 
	@Override	
	public boolean performFinish() {
		return true;
	}
	
	public void addPages()
	{
		addPage(ces);
		addPage(ls);
		if(containsContentElements) {
			addPage(cls);
		}
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
	
		if(page instanceof LocationSelectionWizardPage) {
			LocationSelectionWizardPage p = (LocationSelectionWizardPage) page;
			ProcessPackage sp = p.getSelectedPackage();
			if(sp == null) {
				return null;
			}
			else if(sp != null && p.existingPatterns(sp)==patternCounter) {
				
				return null;
			}
		}
		
		return super.getNextPage(page);
	}

	private boolean containsContentElements(ArrayList<ContentItem> items2) {
		// TODO Auto-generated method stub
		for(ContentItem ci : items){
			if(ci.getAttributes().get("xsi:type").equals("org.eclipse.epf.uma:CapabilityPattern")){
				for(ContentItem ci2 : ci.getSubElements()){
					if(ci2.getAttributes().get("xsi:type").equals("org.eclipse.epf.uma:TaskDescriptor")
							&& !ci2.getSubElements().isEmpty()){
						return true;
						
					}else{
						
						if(containsContentElementRecursive(ci2.getSubElements())) return true;
					}
				}
			}
		}
		return false;
		
	}

	private boolean containsContentElementRecursive(
			ArrayList<ContentItem> subElements) {
		// TODO Auto-generated method stub
		for(ContentItem ci2 : subElements){
			if(ci2.getAttributes().get("xsi:type").equals("org.eclipse.epf.uma:TaskDescriptor")
					&& !ci2.getSubElements().isEmpty()){
				return true;
				
			}else{
				
				if(containsContentElementRecursive(ci2.getSubElements())) return true;
			}
		}
		return false;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		library = LibraryService.getInstance().getCurrentMethodLibrary();
		ces = new ProcessElementSelectionWizardPage(workbench.getActiveWorkbenchWindow().getShell(), library.getMethodPlugins().toArray(), data);
		ls = new LocationSelectionWizardPage(workbench.getActiveWorkbenchWindow().getShell(), library.getMethodPlugins().toArray(), data, items);
		cls = new ContentLocationSelectionWizardPage(workbench.getActiveWorkbenchWindow().getShell(), library.getMethodPlugins().toArray(), data, items);
		
	}
	
	private boolean existsAllPatterns() {
		if(ls != null) {
			ProcessPackage sp = ls.getSelectedPackage();
			return (sp != null && ls.existingPatterns(sp)==patternCounter);
		}
		
		return false;
	}

	public boolean canFinish(){
		
		
		if(data.getElement()!=null && data.getFolder()!=null) {
			
			if(existsAllPatterns() || data.getContentFolder()!=null) {
				return true;
			}else{
				return false;
			}
		
		
		}
		else {
			return false;
		}
	}
}



	

