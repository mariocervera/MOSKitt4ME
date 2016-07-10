package moskitt4me.projectmanager.core.providers;

import java.util.List;

import org.eclipse.epf.uma.Activity;
import org.eclipse.epf.uma.UmaPackage;
import org.eclipse.epf.uma.impl.ActivityImpl;

/**
 * Class for activities contained in capability patterns.
 *
 * @author Mario Cervera
 */
public class ActivityCP extends ActivityImpl {

	private List<String> cpguids;
	private String cpname;
	
	/*
	 * Constructor
	 */
	public ActivityCP(List<String> cpguids, String cpname, Activity actv) {
		
		this.cpguids = cpguids;
		this.cpname = cpname;
		
		//Clone the Activity
		
		this.eSet(UmaPackage.eINSTANCE.getActivity_BreakdownElements(), actv.getBreakdownElements());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_BriefDescription(), actv.getBriefDescription());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Checklists(), actv.getChecklists());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Concepts(), actv.getConcepts());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Estimationconsiderations(), actv.getEstimationconsiderations());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Examples(), actv.getExamples());
		this.eSet(UmaPackage.eINSTANCE.getFulfillableElement_Fulfills(), actv.getFulfills());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_Guid(), actv.getGuid());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Guidelines(), actv.getGuidelines());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_HasMultipleOccurrences(), actv.getHasMultipleOccurrences());
		this.eSet(UmaPackage.eINSTANCE.getClassifier_IsAbstract(), actv.getIsAbstract());
		this.eSet(UmaPackage.eINSTANCE.getWorkBreakdownElement_IsEventDriven(), actv.getIsEventDriven());
		this.eSet(UmaPackage.eINSTANCE.getWorkBreakdownElement_IsOngoing(), actv.getIsOngoing());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_IsOptional(), actv.getIsOptional());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_IsPlanned(), actv.getIsPlanned());
		this.eSet(UmaPackage.eINSTANCE.getWorkBreakdownElement_IsRepeatable(), actv.getIsRepeatable());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_Kind(), actv.getKind());
		this.eSet(UmaPackage.eINSTANCE.getWorkBreakdownElement_LinkToPredecessor(), actv.getLinkToPredecessor());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_MethodElementProperty(), actv.getMethodElementProperty());
		this.eSet(UmaPackage.eINSTANCE.getNamedElement_Name(), actv.getName());
		this.eSet(UmaPackage.eINSTANCE.getDescribableElement_Nodeicon(), actv.getNodeicon());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_OrderingGuide(), actv.getOrderingGuide());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_OwnedRules(), actv.getOwnedRules());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_PlanningData(), actv.getPlanningData());
		this.eSet(UmaPackage.eINSTANCE.getWorkDefinition_Postcondition(), actv.getPostcondition());
		this.eSet(UmaPackage.eINSTANCE.getWorkDefinition_Precondition(), actv.getPrecondition());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Prefix(), actv.getPrefix());
		this.eSet(UmaPackage.eINSTANCE.getDescribableElement_Presentation(), actv.getPresentation());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_PresentationName(), actv.getPresentationName());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_PresentedAfter(), actv.getPresentedAfter());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_PresentedBefore(), actv.getPresentedBefore());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Reports(), actv.getReports());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_ReusableAssets(), actv.getReusableAssets());
		this.eSet(UmaPackage.eINSTANCE.getActivity_Roadmaps(), actv.getRoadmaps());
		this.eSet(UmaPackage.eINSTANCE.getDescribableElement_Shapeicon(), actv.getShapeicon());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_SuperActivities(), actv.getSuperActivities());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_SupportingMaterials(), actv.getSupportingMaterials());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_Suppressed(), actv.getSuppressed());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Templates(), actv.getTemplates());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Toolmentor(), actv.getToolmentor());
		this.eSet(UmaPackage.eINSTANCE.getVariabilityElement_VariabilityBasedOnElement(), actv.getVariabilityBasedOnElement());
		this.eSet(UmaPackage.eINSTANCE.getVariabilityElement_VariabilityType(), actv.getVariabilityType());
	}
	
	// Getters
	
	public List<String> getCpGuids() {
		return cpguids;
	}
	
	public String getCpName() {
		return cpname;
	}
}
