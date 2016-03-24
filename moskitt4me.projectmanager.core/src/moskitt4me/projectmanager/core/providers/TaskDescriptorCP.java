package moskitt4me.projectmanager.core.providers;

import java.util.List;

import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.UmaPackage;
import org.eclipse.epf.uma.impl.TaskDescriptorImpl;

/*
 * Class for Task Descriptors contained in CapabilityPatterns
 *
 * @author Mario Cervera
 */
public class TaskDescriptorCP extends TaskDescriptorImpl {

	private List<String> cpguids;
	private String cpname;
	
	public TaskDescriptorCP(List<String> cpguids, String cpname, TaskDescriptor td) {
		
		this.cpguids = cpguids;
		this.cpname = cpname;
		
		//Clone Task Descriptor
		
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_AdditionallyPerformedBy(), td.getAdditionallyPerformedBy());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_AdditionallyPerformedByExclude(), td.getAdditionallyPerformedByExclude());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_AssistedBy(), td.getAssistedBy());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_BriefDescription(), td.getBriefDescription());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Checklists(), td.getChecklists());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Concepts(), td.getConcepts());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Estimationconsiderations(), td.getEstimationconsiderations());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Examples(), td.getExamples());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_ExternalInput(), td.getExternalInput());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_Guid(), td.getGuid());
		this.eSet(UmaPackage.eINSTANCE.getDescriptor_GuidanceAdditional(), td.getGuidanceAdditional());
		this.eSet(UmaPackage.eINSTANCE.getDescriptor_GuidanceExclude(), td.getGuidanceExclude());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Guidelines(), td.getGuidelines());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_HasMultipleOccurrences(), td.getHasMultipleOccurrences());
		this.eSet(UmaPackage.eINSTANCE.getClassifier_IsAbstract(), td.getIsAbstract());
		this.eSet(UmaPackage.eINSTANCE.getWorkBreakdownElement_IsEventDriven(), td.getIsEventDriven());
		this.eSet(UmaPackage.eINSTANCE.getWorkBreakdownElement_IsOngoing(), td.getIsOngoing());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_IsOptional(), td.getIsOptional());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_IsPlanned(), td.getIsPlanned());
		this.eSet(UmaPackage.eINSTANCE.getWorkBreakdownElement_IsRepeatable(), td.getIsRepeatable());
		this.eSet(UmaPackage.eINSTANCE.getDescriptor_IsSynchronizedWithSource(), td.getIsSynchronizedWithSource());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_Kind(), td.getKind());
		this.eSet(UmaPackage.eINSTANCE.getWorkBreakdownElement_LinkToPredecessor(), td.getLinkToPredecessor());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_MandatoryInput(), td.getMandatoryInput());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_MandatoryInputExclude(), td.getMandatoryInputExclude());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_MethodElementProperty(), td.getMethodElementProperty());
		this.eSet(UmaPackage.eINSTANCE.getNamedElement_Name(), td.getName());
		this.eSet(UmaPackage.eINSTANCE.getDescribableElement_Nodeicon(), td.getNodeicon());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_OptionalInput(), td.getOptionalInput());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_OptionalInputExclude(), td.getOptionalInputExclude());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_OrderingGuide(), td.getOrderingGuide());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_Output(), td.getOutput());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_OutputExclude(), td.getOutputExclude());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_OwnedRules(), td.getOwnedRules());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_PerformedPrimarilyBy(), td.getPerformedPrimarilyBy());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_PerformedPrimarilyByExcluded(), td.getPerformedPrimarilyByExcluded());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_PlanningData(), td.getPlanningData());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Prefix(), td.getPrefix());
		this.eSet(UmaPackage.eINSTANCE.getDescribableElement_Presentation(), td.getPresentation());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_PresentationName(), td.getPresentationName());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_PresentedAfter(), td.getPresentedAfter());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_PresentedBefore(), td.getPresentedBefore());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Reports(), td.getReports());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_ReusableAssets(), td.getReusableAssets());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_SelectedSteps(), td.getSelectedSteps());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_SelectedStepsExclude(), td.getSelectedStepsExclude());
		this.eSet(UmaPackage.eINSTANCE.getDescribableElement_Shapeicon(), td.getShapeicon());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_SuperActivities(), td.getSuperActivities());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_SupportingMaterials(), td.getSupportingMaterials());
		this.eSet(UmaPackage.eINSTANCE.getMethodElement_Suppressed(), td.getSuppressed());
		this.eSet(UmaPackage.eINSTANCE.getTaskDescriptor_Task(), td.getTask());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Templates(), td.getTemplates());
		this.eSet(UmaPackage.eINSTANCE.getBreakdownElement_Toolmentor(), td.getToolmentor());
	}
	
	public List<String> getCpGuids() {
		return cpguids;
	}
	
	public String getCpName() {
		return cpname;
	}
}
