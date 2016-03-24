package moskitt4me.projectmanager.core.filters;

import java.util.ArrayList;
import java.util.List;

import moskitt4me.projectmanager.core.context.Context;
import moskitt4me.projectmanager.core.context.Context.ProcessVisualization;
import moskitt4me.projectmanager.core.providers.ActivityCP;
import moskitt4me.projectmanager.core.providers.CapabilityPatternCP;
import moskitt4me.projectmanager.core.providers.TaskDescriptorCP;
import moskitt4me.projectmanager.methodspecification.MethodElements;
import moskitt4me.projectmanager.processsupport.Engine;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.epf.uma.Activity;
import org.eclipse.epf.uma.BreakdownElement;
import org.eclipse.epf.uma.CapabilityPattern;
import org.eclipse.epf.uma.DeliveryProcess;
import org.eclipse.epf.uma.RoleDescriptor;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.VariabilityElement;
import org.eclipse.epf.uma.VariabilityType;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


/*
 * This class filters elements in the Process View taking into account the selected type
 * of visualization.
 *
 * @author Mario Cervera
 */
public class ProcessFilter extends ViewerFilter {
	
	/*
	* Returns false if the given element must not be shown in the viewer (in this case, the
	* Process view), true otherwise.
	*/
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		
		List<String> cpIds = new ArrayList<String>();
		
		if(element instanceof CapabilityPattern &&
				((CapabilityPattern)element).getVariabilityType().equals(VariabilityType.EXTENDS)) {
			
			CapabilityPattern cp = (CapabilityPattern) element;
			
			if(cp instanceof CapabilityPatternCP) {
				CapabilityPatternCP cpcp = (CapabilityPatternCP) cp;
				cpIds.addAll(cpcp.getCpGuids());
			}
			cpIds.add(cp.getGuid());
			CapabilityPattern cp2 = MethodElements.getCapabilityPattern(getGUId(cp));
			element = cp2;
		}
		if(element instanceof Activity && !(element instanceof DeliveryProcess)) {
			
			Activity actv = (Activity) element;
			
			if(actv instanceof ActivityCP) {
				ActivityCP actvcp = (ActivityCP) actv;
				cpIds = actvcp.getCpGuids();
			}
			else if(actv instanceof CapabilityPatternCP &&
					!((CapabilityPattern)element).getVariabilityType().equals(VariabilityType.EXTENDS)) {
				CapabilityPatternCP cpcp = (CapabilityPatternCP) actv;
				cpIds = cpcp.getCpGuids();
			}
			
			return containsTaskToShow(actv, cpIds);
		}
		else if(element instanceof TaskDescriptor) {
			
			TaskDescriptor td = (TaskDescriptor) element;
			
			return mustShowTaskDescriptor(td, null);
		}
		
		return true;
	}
	
	/*
	* An activity must be shown if it contains tasks that must be shown.
	*/
	private boolean containsTaskToShow(Activity actv, List<String> cpIds) {
		
		boolean containsVisibleTask = false;
		
		List<BreakdownElement> elements = actv.getBreakdownElements();
		int size = elements.size();
		if(cpIds == null) {
			cpIds = new ArrayList<String>();
		}
		
		for(int i = 0; i < size && !containsVisibleTask; i++) {
			
			BreakdownElement be = actv.getBreakdownElements().get(i);
			List<String> cpIds2 = new ArrayList<String>();
			cpIds2.addAll(cpIds);
			
			if(be instanceof CapabilityPattern &&
					((CapabilityPattern)be).getVariabilityType().equals(VariabilityType.EXTENDS)) {
				
				CapabilityPattern cp = (CapabilityPattern) be;
				be = MethodElements.getCapabilityPattern(getGUId(cp));
				cpIds2.add(cp.getGuid());
			}
			if(be instanceof Activity) {
				Activity actv2 = (Activity) be;
				
				if(actv2 instanceof ActivityCP) {
					ActivityCP actvcp = (ActivityCP) actv2;
					cpIds2 = actvcp.getCpGuids();
					containsVisibleTask = containsTaskToShow(actvcp, cpIds2);
				}
				else if(actv2 instanceof CapabilityPatternCP &&
						!((CapabilityPattern)actv2).getVariabilityType().equals(VariabilityType.EXTENDS)) {
					
					CapabilityPatternCP cpcp = (CapabilityPatternCP) actv2;
					cpIds2 = cpcp.getCpGuids();
					containsVisibleTask = containsTaskToShow(cpcp, cpIds2);
				}
				else {
					containsVisibleTask = containsTaskToShow(actv2, cpIds2);
				}
			}
			else if(be instanceof TaskDescriptor) {
				TaskDescriptor td = (TaskDescriptor) be;
				
				if(mustShowTaskDescriptor(td, cpIds2)) {
					 containsVisibleTask = true;
				}
			}
		}
		
		return containsVisibleTask;
	}
	
	/*
	* This method checks whether a given task must be shown (according to the selected display mode).
	*/
	private boolean mustShowTaskDescriptor(TaskDescriptor td, List<String> cpIds) {
		
		if (Context.currentProcessVisualization == ProcessVisualization.NEXTTASKS) {
			
			String activitiTaskId = MethodElements.getActivitiTaskId(td);
			String processInstanceId = "";
			if(cpIds != null && cpIds.size() > 0) {
				processInstanceId = Context.getProcessInstance(cpIds);
			}
			else {
				processInstanceId = getProcessInstanceId(td);
			}
			
			if(!Engine.isExecutable(activitiTaskId, 
					processInstanceId, Context.selectedProject) 
					|| !assignedToCurrentRole(td)) {
				
				return false;
			}
		}
		else if(Context.currentProcessVisualization == ProcessVisualization.ALLTASKS) {
			
			return assignedToCurrentRole(td);
		}
		
		return true;
	}
	
	/*
	* In order for a task to be shown, it must also be assigned to at least one of the roles
	* that are selected (if any).
	*/
	private boolean assignedToCurrentRole(TaskDescriptor td) {
		
		if(Context.currentRoles.size() == 0) {
			return true;
		}
		
		for(RoleDescriptor rd : Context.currentRoles) {
			if(MethodElements.performs(rd, td)) {		
				return true;
			}
		}
		
		return false;
	}
	
	private String getGUId(CapabilityPattern cp) {
		
		VariabilityElement ve = cp.getVariabilityBasedOnElement();
		if(ve instanceof CapabilityPattern) {
			CapabilityPattern cp2 = (CapabilityPattern) ve;
			if(cp2.eIsProxy()) {
				URI proxyURI = ((InternalEObject)cp2).eProxyURI();
				return proxyURI.host();
			}
		}
		
		return "";
	}
	
	private String getProcessInstanceId(TaskDescriptor td) {
		
		if(td instanceof TaskDescriptorCP) {
			TaskDescriptorCP tdcp = (TaskDescriptorCP) td;
			return Context.getProcessInstance(tdcp.getCpGuids());
		}

		return Context.processInstanceId;
	}
}
