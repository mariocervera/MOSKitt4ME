package moskitt4me.projectmanager.core.providers;

import java.util.ArrayList;
import java.util.List;

import moskitt4me.projectmanager.methodspecification.MethodElements;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.epf.uma.Guidance;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.TaskDescriptor;

/**
 * A Content Provider for the Guides view. Shows the elements of type "Guidance" that
 * are associated (in the method model) to the task that is selected in the Process view.
 * 
 * @author Mario Cervera
 */
public class GuidesContentProvider extends AdapterFactoryContentProvider {

	public GuidesContentProvider(AdapterFactory adapterFactory) {
		
		super(adapterFactory);
	}

	@Override
	public Object[] getElements(Object object) {
		
		if(object instanceof TaskDescriptor) {
			
			TaskDescriptor td = (TaskDescriptor) object;
			if(td.getTask() != null) {
				Object[] elements = new Object[1];
				elements[0] = getTask(td.getTask());
				return elements;
			}
			else {
				return new Object[0];
			}
		}
		
		return super.getElements(object);
	}
	
	@Override
	public boolean hasChildren(Object object) {
	
		if(object instanceof Task) {
			return true;
		}
		else if(object instanceof Guidance) {
			return false;
		}
		
		return super.hasChildren(object);
	}
	
	@Override
	public Object[] getChildren(Object object) {
		
		if(object instanceof Task) {
			
			Task t = (Task) object;
			
			List<Guidance> guides = new ArrayList<Guidance>();
			
			guides.addAll(t.getChecklists());
			guides.addAll(t.getConceptsAndPapers());
			guides.addAll(t.getExamples());
			guides.addAll(t.getGuidelines());
			guides.addAll(t.getEstimationConsiderations());
			guides.addAll(t.getAssets());
			guides.addAll(t.getSupportingMaterials());
			guides.addAll(t.getTermdefinition());
			guides.addAll(t.getToolMentors());
			
			return guides.toArray();
		}
		
		return super.getChildren(object);
	}
	
	private Task getTask(Task proxyTask) {
		
		Task task = MethodElements.getTask(proxyTask);
		
		if(task == null) {
			return proxyTask;
		}
		else {
			return task;
		}
	}
}
