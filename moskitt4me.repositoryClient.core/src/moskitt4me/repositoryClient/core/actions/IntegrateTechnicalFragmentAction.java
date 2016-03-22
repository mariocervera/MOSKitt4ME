package moskitt4me.repositoryClient.core.actions;

import java.util.List;

import moskitt4me.repositoryClient.core.dialogs.ContentElementSelectionDialog;
import moskitt4me.repositoryClient.core.providers.ContentElementsContentProvider;
import moskitt4me.repositoryClient.core.providers.ContentElementsLabelProvider;
import moskitt4me.repositoryClient.core.providers.MethodFragmentItemProvider;
import moskitt4me.repositoryClient.core.util.RepositoryLocation;

import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.epf.library.LibraryService;
import org.eclipse.epf.library.edit.command.IActionManager;
import org.eclipse.epf.library.services.LibraryModificationHelper;
import org.eclipse.epf.uma.ContentElement;
import org.eclipse.epf.uma.ContentPackage;
import org.eclipse.epf.uma.MethodElementProperty;
import org.eclipse.epf.uma.MethodLibrary;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.ToolMentor;
import org.eclipse.epf.uma.UmaFactory;
import org.eclipse.epf.uma.UmaPackage;
import org.eclipse.epf.uma.WorkProduct;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

/*
* This action allows the user to associate technical fragments with conceptual elements of
* the method under construction (e.g., tasks, roles, and products).
*
* @author Mario Cervera
*/
public class IntegrateTechnicalFragmentAction extends IntegrateFragmentAction {

	protected int performFragmentIntegration(MethodFragmentItemProvider fragment) {
		
		//Obtain an element of the method by means of the ContentElementSelectionDialog
		
		ContentElement contentElement = getContentElement();
		
		if(contentElement != null 
				&& contentElement.eContainer() instanceof ContentPackage) {
			
			ContentPackage parent = (ContentPackage) contentElement.eContainer();
			
			String toolName = fragment.getFileName().replace(".ras.zip", "");
			
			LibraryModificationHelper helper = new LibraryModificationHelper();
			
			//Create an element of type Tool Mentor. Tool Mentor is a SPEM 2.0 primitive. We use
			//it as a representation of a technical fragment. 
			
			ToolMentor tool = getToolMentor(fragment.getToolId(), toolName,
					fragment.getFileName(), fragment.getLocation(), 
					fragment.getDescription(), fragment.getType(),
					parent, helper);
			
			//Associate the tool mentor with the selected method element
			
			associateToolWithContentElement(contentElement, tool, fragment, helper);
			
			helper.save();
			
			return 0;
		}

		return 1;
	}
	
	private ContentElement getContentElement() {
		
		MethodLibrary library = LibraryService.getInstance().getCurrentMethodLibrary();
		
		if(library != null) {
			
			ContentElementSelectionDialog contentElementSelectionDialog = new ContentElementSelectionDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					library.getMethodPlugins().toArray());

			contentElementSelectionDialog.setLabelProvider(new ContentElementsLabelProvider());
			contentElementSelectionDialog.setContentProvider(
					new ContentElementsContentProvider(new ResourceItemProviderAdapterFactory()));

			if (contentElementSelectionDialog.open() == Window.OK) {
				
				Object[] selection = contentElementSelectionDialog.getResult();

				if (selection != null && selection.length > 0) {
					Object sel = selection[0];
					if(sel instanceof ContentElement) {
						return (ContentElement)sel;
					}
				}
			}
		}
		
		return null;
	}
	
	private ToolMentor getToolMentor(String toolId, String toolName, String fragmentFileName,
			RepositoryLocation location, String description, String type,
			ContentPackage cpackage, LibraryModificationHelper helper) {
		
		ToolMentor tool = null;
		List<ContentElement> celements = cpackage.getContentElements();
		
		for(ContentElement ce : celements) {
			if(ce instanceof ToolMentor) {
				ToolMentor t = (ToolMentor) ce;
				if(t.getName().equals(toolName)) {
					for(MethodElementProperty p : t.getMethodElementProperty()) {
						if(p.getName().equals("toolId") && p.getValue().equals(toolId)) {
							return t;
						}
					}
				}
			}
		}
		
		MethodElementProperty prop = UmaFactory.eINSTANCE.createMethodElementProperty();
		prop.setName("toolId");
		prop.setValue(toolId);
		
		MethodElementProperty prop2 = UmaFactory.eINSTANCE.createMethodElementProperty();
		prop2.setName("host");
		prop2.setValue(location.getHost());
		
		MethodElementProperty prop3 = UmaFactory.eINSTANCE.createMethodElementProperty();
		prop3.setName("repositoryPath");
		prop3.setValue(location.getRepositoryPath());
		
		MethodElementProperty prop4 = UmaFactory.eINSTANCE.createMethodElementProperty();
		prop4.setName("user");
		prop4.setValue(location.getUser());
		
		MethodElementProperty prop5 = UmaFactory.eINSTANCE.createMethodElementProperty();
		prop5.setName("password");
		prop5.setValue(location.getPassword());
		
		MethodElementProperty prop6 = UmaFactory.eINSTANCE.createMethodElementProperty();
		prop6.setName("fragmentFileName");
		prop6.setValue(fragmentFileName);
		
		MethodElementProperty prop7 = null;
		if(description != null && !description.equals("")) {
			prop7 = UmaFactory.eINSTANCE.createMethodElementProperty();
			prop7.setName("description");
			prop7.setValue(description);
		}
		
		MethodElementProperty prop8 = UmaFactory.eINSTANCE.createMethodElementProperty();
		prop8.setName("type");
		prop8.setValue(type);
		
		tool = UmaFactory.eINSTANCE.createToolMentor();
		tool.setName(toolName);
		tool.getMethodElementProperty().add(prop);
		tool.getMethodElementProperty().add(prop2);
		tool.getMethodElementProperty().add(prop3);
		tool.getMethodElementProperty().add(prop4);
		tool.getMethodElementProperty().add(prop5);
		tool.getMethodElementProperty().add(prop6);
		if(prop7 != null) {
			tool.getMethodElementProperty().add(prop7);
		}
		tool.getMethodElementProperty().add(prop8);
		
		helper.getActionManager().doAction(IActionManager.ADD,
				cpackage, UmaPackage.eINSTANCE.getContentPackage_ContentElements(),
						tool, -1);
		
		return tool;
	}
	
	private void associateToolWithContentElement(ContentElement contentElement, ToolMentor tool,
			MethodFragmentItemProvider fragment, LibraryModificationHelper helper) {
		
		if(contentElement instanceof Task) {
			Task task = (Task) contentElement;
			for(ToolMentor t : task.getToolMentors()) {
				if(t.equals(tool)) {
					return;
				}
			}
			
			helper.getActionManager().doAction(IActionManager.ADD,
					task, UmaPackage.eINSTANCE.getTask_ToolMentors(),
							tool, -1);
		}
		else if(contentElement instanceof WorkProduct) {
			WorkProduct product = (WorkProduct) contentElement;
			for(ToolMentor t : product.getToolMentors()) {
				if(t.equals(tool)) {
					return;
				}
			}
			
			if(fragment.getType().equals("External Tool")) {
				MethodElementProperty prop = null;
				boolean propExists = false;
				for(MethodElementProperty p : product.getMethodElementProperty()) {
					if(p.getName().equals("targetPath")) {
						prop = p;
						propExists = true;
						break;
					}
				}
				if(prop == null) {
					prop = UmaFactory.eINSTANCE.createMethodElementProperty();
					prop.setName("targetPath");
					prop.setValue("/" + product.getName() + fragment.getToolId());
				}
				
				if(propExists) {
					helper.getActionManager().doAction(IActionManager.SET,
							prop, UmaPackage.eINSTANCE.getMethodElementProperty_Value(),
							"/" + product.getName() + fragment.getToolId(), -1);
				}
				else {
					helper.getActionManager().doAction(IActionManager.ADD,
							product,UmaPackage.eINSTANCE.getMethodElement_MethodElementProperty(),
							prop, -1);
				}
			}
			
			helper.getActionManager().doAction(IActionManager.ADD,
					product, UmaPackage.eINSTANCE.getWorkProduct_ToolMentors(),
							tool, -1);
		}
	}
}
