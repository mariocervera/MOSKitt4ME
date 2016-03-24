package moskitt4me.projectmanager.core.providers;

import java.util.List;

import moskitt4me.projectmanager.methodspecification.MethodElements;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.epf.uma.MethodElement;
import org.eclipse.epf.uma.MethodElementProperty;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.ToolMentor;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/*
* This class triggers a context change whenever a new task is selected in the Process view. This allows the
* Help view of Eclipse to provide help based on the selected task.
*
* @author Mario Cervera
*/
public class ProcessContextProvider implements IContextProvider {
	
	public IContext getContext(Object target) {
		
		IContext context = null;
		
		if(target instanceof Tree) {
			
			Tree tree = (Tree) target;
			TreeItem[] items = tree.getSelection();
			
			for(int i = 0; i < items.length && context == null; i++) {
							
				TreeItem item = items[i];
				Object element = item.getData();
				
				if(element instanceof TaskDescriptor) {
					
					// A task is selected ...
					
					TaskDescriptor td = (TaskDescriptor) element;
					Task task = td.getTask();
					if(task != null) {
						if(task.eIsProxy()) {
							task = getTask(task, MethodElements.tasks);
						}
						
						//Obtain the technical fragments associated to the task
						
						List<ToolMentor> tools = task.getToolMentors();
						if(tools != null && tools.size() > 0) {
							ToolMentor tool = tools.get(0);
							String toolId = getPropertyValue(tool, "toolId");
							if(!isTransformation(toolId)) {
								
								// Contextual help is associated to the task, use it
								// as context.
								
								context = HelpSystem.getContext(toolId);
							}
						}
					}
				}
			}
		}
		
		return context;
	}

	private static Task getTask(Task proxyTask, List<Task> tasks) {

		if (proxyTask != null && proxyTask.eIsProxy()) {
			URI proxyURI = ((InternalEObject) proxyTask).eProxyURI();

			for (Task task : tasks) {
				if (proxyURI.fragment().equals(task.getGuid())) {
					return task;
				}
			}
		}

		return proxyTask;
	}
	
	private static String getPropertyValue(MethodElement me, String name) {
		
		List<MethodElementProperty> props = me.getMethodElementProperty();
		
		for(MethodElementProperty prop : props) {
			if(prop.getName().equals(name)) {
				return prop.getValue();
			}
		}
		
		return "";
	}
	
	private static boolean isTransformation(String id) {
		
		IConfigurationElement[] configurations = Platform
				.getExtensionRegistry().getConfigurationElementsFor(
						"es.cv.gvcase.trmanager.transformation");

		for (IConfigurationElement config : configurations) {
			String trId = config.getAttribute("id");
			if(trId.equals(id)) {
				return true;
			}
		}

		return false;
	}
	
	public int getContextChangeMask() {

		return SELECTION;
	}

	public String getSearchExpression(Object target) {
		
		return null;
	}
}
