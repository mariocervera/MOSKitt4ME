package moskitt4me.spem2bpmn.util;

import java.util.StringTokenizer;

import org.eclipse.bpmn2.CallActivity;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Property;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epf.uma.Activity;
import org.eclipse.epf.uma.DeliveryProcess;
import org.eclipse.epf.uma.MethodPlugin;
import org.eclipse.epf.uma.ProcessElement;
import org.eclipse.epf.uma.ProcessPackage;
import org.eclipse.epf.uma.WorkBreakdownElement;
import org.eclipse.epf.uma.WorkOrder;

public class SPEM2BPMNUtil {

	public static String replaceLastSegment(String uri, String newSegment) {
		
		StringTokenizer tokenizer = new StringTokenizer(uri, "/");
		
		String newUri = "";
		int numTokens = tokenizer.countTokens();
		
		for(int i = 0; i < numTokens - 1; i++) {
			newUri += tokenizer.nextToken() + "/";
		}
		
		 return (newUri + newSegment);
	}
	
	public static MethodPlugin getMethodPlugin(DeliveryProcess dp) {
		
		EObject container = dp.eContainer();
		
		while(!(container instanceof MethodPlugin)) {
			container = container.eContainer();
		}
		
		return (MethodPlugin) container;
	}
	
	public static ProcessPackage getProcessPackage(Activity activity) {

		EObject container = activity.eContainer();

		while (!(container instanceof ProcessPackage)) {
			container = container.eContainer();
		}

		return (ProcessPackage) container;
	}
	
	public static Resource createResource(String folder, String fileName) {
		
		XMIResourceFactoryImpl _xmiFac = new XMIResourceFactoryImpl();
		ResourceSet rSet = new ResourceSetImpl();
		rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", _xmiFac);
		URI uri = URI.createURI(folder + fileName);
		Resource resource = rSet.createResource(uri);
		
		return resource;
	}
	
	public static boolean hasPredecessor(WorkBreakdownElement wbe) {
		
		if (wbe.getLinkToPredecessor().size() > 0) {
			boolean predecessor = false;
			for(WorkOrder o : wbe.getLinkToPredecessor()) {
				if(o.getPred() != null) {
					predecessor = true;
					break;
				}
			}
			
			return predecessor;
		}
		else {
			return false;
		}
	}
	
	public static boolean hasSuccessor(WorkBreakdownElement wbe, EList<EObject> eobjects,
			String cpId) {
		
		for(EObject eobj : eobjects) {
			if(eobj instanceof SequenceFlow) {
				SequenceFlow sf = (SequenceFlow) eobj;
				FlowNode source = sf.getSourceRef();
				if(source instanceof UserTask) {
					UserTask ut = (UserTask) source;
					if(wbe.getGuid().equals(getTaskDescriptorId(ut))) {
						return true;
					}
				}
				else if(source instanceof CallActivity) {
					CallActivity ca = (CallActivity) source;
					if(wbe.getGuid().equals(ca.getCalledElement())) {
						if(cpId == null) {
							return true;
						}
						else {
							String id = ca.getId().replaceFirst("capabilityPattern", "");
							if(id.equals(cpId)) {
								return true;
							}
						}
					}
				}
			}
		}
		
		return false;
	}
	
	public static FlowNode getFlowNode(WorkBreakdownElement wbe, EList<EObject> eobjects,
			String cpId) {
		
		for(EObject eobj : eobjects) {
			if(eobj instanceof UserTask) {
				UserTask ut = (UserTask) eobj;
				
				String tdGuid = getTaskDescriptorId(ut);
				
				if(tdGuid.equals(wbe.getGuid())) {
					return ut;
				}
			}
			else if(eobj instanceof CallActivity) {
				CallActivity ca = (CallActivity) eobj;
				
				String actvGuid = ca.getCalledElement();
				
				if(actvGuid.equals(wbe.getGuid())) {
					if(cpId == null) {
						return ca;
					}
					else {
						String id = ca.getId().replaceFirst("capabilityPattern", "");
						if(id.equals(cpId)) {
							return ca;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	public static String getTaskDescriptorId(org.eclipse.bpmn2.Activity ac) {
		
		String tdGuid = "";
		for(Property p : ac.getProperties()) {
			if(p.getId().equals("uma_element")) {
				tdGuid = p.getName();
				break;
			}
		}
		
		return tdGuid;
	}
	
	public static Activity getActivity(ProcessPackage pp) {
		
		for(ProcessElement pe : pp.getProcessElements()) {
			if(pe instanceof Activity) {
				return (Activity) pe;
			}
		}
		
		return null;
	}
}
