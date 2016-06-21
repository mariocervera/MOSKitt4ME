package moskitt4me.projectmanager.productsupport;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import moskitt4me.projectmanager.productsupport.util.ProductSupportUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.ToolMentor;
import org.eclipse.epf.uma.WorkProduct;
import org.eclipse.epf.uma.WorkProductDescriptor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

/**
 * This class offers a set of methods that enable the execution of the method tasks. The performance
 * of these tasks will vary depending on the nature of the output products.
 *
 * @author Mario Cervera
 */
public class ProductSupport {

	/*
	 * This method invokes the execution of a specific method task
	 */
	public static boolean launchTask(TaskDescriptor td, IProject project,
			List<WorkProduct> products, List<Task> tasks, List<String> cpIds) {
		
		boolean productCreated = false;
		
		if (ProductSupportUtil.isAutomatic(td, tasks)) {
			
			// Automatic tasks involve the execution of a model-to-model or model-to-text
			// transformation
			
			productCreated = performAutomaticTask(td, project, products, tasks, cpIds);
		}
		else {
			// Manual tasks are performed by the MOSKitt4ME user (using a tool such as a
			// graphical editor)
			
			productCreated = performManualTask(td, project, products, cpIds);
		}
		
		// Repeatable tasks can be executed multiple times ...
		
		if(productCreated && td.getIsRepeatable() &&
				ProductSupportUtil.isRestarted(td.getGuid(), cpIds, project)) {
			
			ProductSupportUtil.clearRepeatableTask(td.getGuid(), cpIds, project);
		}
		
		return productCreated;
	}
	
	/*
	 * In the case of automatic tasks, the output products are obtained automatically by means
	 * of a model transformation (either M2T or M2M).
	 */
	private static boolean performAutomaticTask(TaskDescriptor td, IProject project,
			List<WorkProduct> products, List<Task> tasks, List<String> cpIds) {
		
		Task task = td.getTask();
		
		if(task != null) {
			if(task.eIsProxy()) {
				task = ProductSupportUtil.getTask(task, tasks);
			}
			
			// Get the tool (technical fragment) associated to the task
			
			List<ToolMentor> tools = task.getToolMentors();
			if(tools != null && tools.size() > 0) {
				ToolMentor tool = tools.get(0);
				String toolId = ProductSupportUtil.getPropertyValue(tool, "toolId");
				
				// If the tool is a URL, open a browser
				
				if(ProductSupportUtil.isURL(toolId)) {
					try {
						IWebBrowser browser = PlatformUI.getWorkbench()
								.getBrowserSupport().createBrowser("MyBrowser");
						browser.openURL(new URL(toolId));
					}
					catch(Exception e) {
						
					}
				}
				else if(ProductSupportUtil.isTransformation(toolId)) {
					
					// If the tool is a model transformation, launch it
					
					if(generateOutputProduct(td, products, project, toolId, cpIds)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	 
	 /*
	  * Manual tasks are carried out by means of tools such as graphical or tree-based editors.
	  */
	private static boolean performManualTask(TaskDescriptor td, IProject project,
			List<WorkProduct> products, List<String> cpIds) {
		
		//First, open the input products of the task
		
		List<WorkProductDescriptor> inputs = new ArrayList<WorkProductDescriptor>();
		if(td.getMandatoryInput() != null) {
			inputs.addAll(td.getMandatoryInput());
		}
		if(td.getOptionalInput() != null) {
			inputs.addAll(td.getOptionalInput());
		}
		List<WorkProductDescriptor> outputs = new ArrayList<WorkProductDescriptor>();
		if(td.getOutput() != null) {
			outputs.addAll(td.getOutput());
		}
		
		boolean productCreated = false;
		
		for(WorkProductDescriptor wpd : inputs) {
			
			WorkProduct product = wpd.getWorkProduct();
			
			if(product != null) {
				if(product.eIsProxy()) {
					product = ProductSupportUtil.getWorkProduct(product, products);
				}
				
				//Open input files
				
				ProductSupportUtil.existsProduct(product, project);
			}
		}
		
		// For each output product ...
		
		for(WorkProductDescriptor wpd : outputs) {
			
			WorkProduct product = wpd.getWorkProduct();
			
			if(product != null) {
				if(product.eIsProxy()) {
					product = ProductSupportUtil.getWorkProduct(product, products);
				}
				
				// Create the product. The tool (e.g., a graphical editor) is automatically opened.
				
				if(td.getIsRepeatable() && 
						ProductSupportUtil.isRestarted(td.getGuid(), cpIds, project)) {
					if(createOutputProduct(product, project)) {
						productCreated = true;
					}
				}
				else {
					if(!ProductSupportUtil.existsProduct(product, project)) {
						if(createOutputProduct(product, project)) {
							productCreated = true;
						}
					}
				}
			}
		}
		
		return productCreated;
	}
	
	
	private static boolean createOutputProduct(WorkProduct product, IProject project) {
		
		List<ToolMentor> tools = product.getToolMentors();
		
		boolean productCreated = false;
		
		if(tools != null && tools.size() > 0) {
			
			ToolMentor tool = tools.get(0);	
			
			if(tool != null) {
				
				String toolId = ProductSupportUtil.getPropertyValue(tool, "toolId");
				
				if(ProductSupportUtil.isCreationWizard(toolId)) {
					productCreated = ProductSupportUtil.launchWizard(toolId, project, product);
				}
				else if(ProductSupportUtil.isExternalTool(toolId)) {
					productCreated = ProductSupportUtil.createExternalToolFile(toolId, product, project);
				}
				else {
					//Internal tools
					
					String desc = ProductSupportUtil.getPropertyValue(tool, "description");
					
					String message = "The tool associated to this task could not be opened.\n\n";
					message += "Name: " + tool.getName() + "\n\n";
					message += "Description: " + desc;
					
					MessageDialog.openInformation(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Task Execution", message);
				}
			}
		}
		
		return productCreated;
	}
	
	private static boolean generateOutputProduct(TaskDescriptor td,
			List<WorkProduct> products, IProject project,
			String toolId, List<String> cpIds) {
		
		if(td.getOutput() != null && td.getOutput().size() > 0) {
			WorkProductDescriptor wpd = td.getOutput().get(0);
			
			WorkProduct product = wpd.getWorkProduct();
			
			if(product != null) {
				if(product.eIsProxy()) {
					product = ProductSupportUtil.getWorkProduct(product, products);
				}
				
				if(td.getIsRepeatable()) {
					if(ProductSupportUtil.isRestarted(td.getGuid(), cpIds, project)) {
						return ProductSupportUtil.launchTransformation(toolId,
								project, product);
					}
				}
				
				if(!ProductSupportUtil.existsProduct(product, project)) {
					return ProductSupportUtil.launchTransformation(toolId,
							project, product);
				}
			}
		}
		
		return false;
	}
}
