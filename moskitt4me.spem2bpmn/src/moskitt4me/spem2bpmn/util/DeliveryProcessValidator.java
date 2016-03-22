package moskitt4me.spem2bpmn.util;

import java.util.List;

import org.eclipse.epf.uma.Activity;
import org.eclipse.epf.uma.BreakdownElement;
import org.eclipse.epf.uma.CapabilityPattern;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.VariabilityElement;
import org.eclipse.epf.uma.VariabilityType;
import org.eclipse.epf.uma.WorkBreakdownElement;
import org.eclipse.epf.uma.WorkOrder;

/*
* This class checks whether a SPEM 2.0 delivery process is properly defined and can, therefore, be
* used as input of the SPEM-to-BPMN model transformation.
*
* The validator checks the following constraint: precedence relationships must be established between
* elements within the same parent activity.
*
* @author Mario Cervera
*/
public class DeliveryProcessValidator {
	
	public static String checkPrecedenceRelationships(Activity actv) {
		
		String errorMessage = "";
		
		List<BreakdownElement> elements = actv.getBreakdownElements();
		int size = elements.size();
		int i = 0;
		
		while(i < size && errorMessage.equals("")) {
			BreakdownElement elem = elements.get(i);
			if(elem instanceof TaskDescriptor) {
				TaskDescriptor td = (TaskDescriptor) elem;
				List<WorkOrder> predecessors = td.getLinkToPredecessor();
				for(WorkOrder order : predecessors) {
					WorkBreakdownElement pred = order.getPred();
					if(pred instanceof Activity) {
						Activity actvpred = (Activity) pred;
						if(!td.eContainer().equals(actvpred.eContainer().eContainer())) {
							errorMessage = getErrorMessage(pred, td);
							return errorMessage;
						}
					}
					else if(pred instanceof TaskDescriptor) {
						TaskDescriptor tdpred = (TaskDescriptor) pred;
						if(!td.eContainer().equals(tdpred.eContainer())) {
							errorMessage = getErrorMessage(tdpred, td);
							return errorMessage;
						}
					}
				}
			}
			else if(elem instanceof Activity) {
				Activity actv1 = (Activity) elem;
				List<WorkOrder> predecessors = actv1.getLinkToPredecessor();
				for(WorkOrder order : predecessors) {
					WorkBreakdownElement pred = order.getPred();
					if(pred instanceof TaskDescriptor) {
						TaskDescriptor tdpred = (TaskDescriptor) pred;
						if(!actv1.eContainer().eContainer().equals(tdpred.eContainer())) {
							errorMessage = getErrorMessage(tdpred, actv1);
							return errorMessage;
						}
					}
					else if(pred instanceof Activity) {
						Activity actvpred = (Activity) pred;
						if(!actv1.eContainer().eContainer().equals(actvpred.eContainer().eContainer())) {
							errorMessage = getErrorMessage(actvpred, actv1);
							return errorMessage;
						}
					}
				}
				if(errorMessage.equals("")) {
					
					boolean pattern = false;
					
					if(actv1 instanceof CapabilityPattern &&
							((CapabilityPattern)actv1).getVariabilityType().equals(VariabilityType.EXTENDS)) {
						
						VariabilityElement ve = ((CapabilityPattern)actv1).getVariabilityBasedOnElement();
						if(ve instanceof CapabilityPattern) {
							CapabilityPattern cp = (CapabilityPattern) ve;
							pattern = true;
							errorMessage = checkPrecedenceRelationships(cp);
						}
					}
					if(!pattern) {
						errorMessage = checkPrecedenceRelationships(actv1);
					}
				}
			}
			i++;
		}
		
		return errorMessage;
	}
	
	private static String getErrorMessage(BreakdownElement elem1, BreakdownElement elem2) {
		
		String errorMessage = "Incorrect work flow:\n\n" ;
		String name1 = elem1.getPresentationName() != null && !elem1.getPresentationName().equals("") ?
				elem1.getPresentationName() : elem1.getName();
		String name2 = elem2.getPresentationName() != null && !elem2.getPresentationName().equals("") ?
				elem2.getPresentationName() : elem2.getName();
				
		errorMessage += name1 + " -> " + name2 + "\n\nPrecedence relationships must be established between elements within the same parent activity.";
		
		return errorMessage;
	}
}
