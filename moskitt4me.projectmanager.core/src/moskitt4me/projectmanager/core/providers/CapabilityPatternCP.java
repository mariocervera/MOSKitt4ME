package moskitt4me.projectmanager.core.providers;

import java.util.List;

import org.eclipse.epf.uma.CapabilityPattern;
import org.eclipse.epf.uma.UmaPackage;
import org.eclipse.epf.uma.impl.CapabilityPatternImpl;

/*
* @author Mario Cervera
*/
public class CapabilityPatternCP extends CapabilityPatternImpl {

	private List<String> cpguids;
	private String cpname;
	
	public CapabilityPatternCP(List<String> cpguids, String cpname, CapabilityPattern cp) {
		
		this.cpguids = cpguids;
		this.cpname = cpname;
		
		//Clone the Capability Pattern
		
		this.eSet(UmaPackage.eINSTANCE.getActivity_BreakdownElements(), cp.getBreakdownElements());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_BriefDescription(), cp.getBriefDescription());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Checklists(), cp.getChecklists());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Concepts(), cp.getConcepts());
		this.eSet(UmaPackage.eINSTANCE.getProcess_DefaultContext(), cp.getDefaultContext());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Estimationconsiderations(), cp.getEstimationconsiderations());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Examples(), cp.getExamples());
		this.eSet(UmaPackage.eINSTANCE.getFulfillableElement_Fulfills(), cp.getFulfills());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_Guid(), cp.getGuid());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Guidelines(), cp.getGuidelines());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_HasMultipleOccurrences(), cp.getHasMultipleOccurrences());
		this.eSet(UmaPackage.eINSTANCE.getProcess_IncludesPatterns(), cp.getIncludesPatterns());
		this.eSet(UmaPackage.eINSTANCE.getClassifier_IsAbstract(), cp.getIsAbstract());
		this.eSet(UmaPackage.eINSTANCE.getWorkBreakdownElement_IsEventDriven(), cp.getIsEventDriven());
		this.eSet(UmaPackage.eINSTANCE.getWorkBreakdownElement_IsOngoing(), cp.getIsOngoing());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_IsOptional(), cp.getIsOptional());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_IsPlanned(), cp.getIsPlanned());
		this.eSet(UmaPackage.eINSTANCE.getWorkBreakdownElement_IsRepeatable(), cp.getIsRepeatable());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_Kind(), cp.getKind());
		this.eSet(UmaPackage.eINSTANCE.getWorkBreakdownElement_LinkToPredecessor(), cp.getLinkToPredecessor());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_MethodElementProperty(), cp.getMethodElementProperty());
		this.eSet(UmaPackage.eINSTANCE.getNamedElement_Name(), cp.getName());
		this.eSet(UmaPackage.eINSTANCE.getDescribableElement_Nodeicon(), cp.getNodeicon());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_OrderingGuide(), cp.getOrderingGuide());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_OwnedRules(), cp.getOwnedRules());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_PlanningData(), cp.getPlanningData());
		this.eSet(UmaPackage.eINSTANCE.getWorkDefinition_Postcondition(), cp.getPostcondition());
		this.eSet(UmaPackage.eINSTANCE.getWorkDefinition_Precondition(), cp.getPrecondition());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Prefix(), cp.getPrefix());
		this.eSet(UmaPackage.eINSTANCE.getDescribableElement_Presentation(), cp.getPresentation());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_PresentationName(), cp.getPresentationName());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_PresentedAfter(), cp.getPresentedAfter());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_PresentedBefore(), cp.getPresentedBefore());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Reports(), cp.getReports());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_ReusableAssets(), cp.getReusableAssets());
		this.eSet(UmaPackage.eINSTANCE.getActivity_Roadmaps(), cp.getRoadmaps());
		this.eSet(UmaPackage.eINSTANCE.getDescribableElement_Shapeicon(), cp.getShapeicon());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_SuperActivities(), cp.getSuperActivities());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_SupportingMaterials(), cp.getSupportingMaterials());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_Suppressed(), cp.getSuppressed());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Templates(), cp.getTemplates());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Toolmentor(), cp.getToolmentor());
		this.eSet(UmaPackage.eINSTANCE.getProcess_ValidContext(), cp.getValidContext());
		this.eSet(UmaPackage.eINSTANCE.getVariabilityElement_VariabilityBasedOnElement(), cp.getVariabilityBasedOnElement());
		this.eSet(UmaPackage.eINSTANCE.getVariabilityElement_VariabilityType(), cp.getVariabilityType());
	}
	
	public List<String> getCpGuids() {
		return cpguids;
	}
	
	public String getCpName() {
		return cpname;
	}
}
