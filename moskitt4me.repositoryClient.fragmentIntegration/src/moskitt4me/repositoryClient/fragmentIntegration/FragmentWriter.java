package moskitt4me.repositoryClient.fragmentIntegration;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import moskitt4me.repositoryClient.fragmentIntegration.util.ConceptualFragment;

import org.eclipse.epf.uma.Activity;
import org.eclipse.epf.uma.ArtifactDescription;
import org.eclipse.epf.uma.MethodElementProperty;
import org.eclipse.epf.uma.MethodPackage;
import org.eclipse.epf.uma.ProcessComponent;
import org.eclipse.epf.uma.ProcessElement;
import org.eclipse.epf.uma.ProcessPackage;
import org.eclipse.epf.uma.Role;
import org.eclipse.epf.uma.RoleDescription;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.TaskDescription;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.WorkBreakdownElement;
import org.eclipse.epf.uma.WorkOrder;
import org.eclipse.epf.uma.WorkProduct;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyList.ListElement;

public class FragmentWriter {

	private static List<Object> listElems;
	static List<Object> referencedElements;
	public static String createConceptualFragmentXMLFile(ConceptualFragment result,
			List<Object> listElements, String folder, boolean integrateReferences) throws Exception {
		
		listElems = listElements;
		String manifestPath = folder + "/fragment.xmi";
		File f = new File(manifestPath);
		referencedElements = new ArrayList<Object>();
		
		
		if(!f.exists()) {
			f.createNewFile();
			
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(f));
			bwriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xmi:XMI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:org.eclipse.epf.uma=\"http://www.eclipse.org/epf/uma/1.0.6/uma.ecore\" xmlns:org.eclipse.epf.uma.resourcemanager=\"http:///org/eclipse/epf/uma/resourcemanager.ecore\" xmlns:epf=\"http://www.eclipse.org/epf\" epf:version=\"1.5.1\">");
			
			
			if(!result.getType().equals("Process")){
			bwriter.write("<Contents>");
			for(Object elem : listElements){
				
					/**TASK*****************************************************/
					if(elem instanceof Task &&(result.getType().equals("Task")||result.getType().equals("Content Element"))){
						Task t = (Task)elem;
						writeTask(t, t.getGuid(), bwriter, integrateReferences, referencedElements);
					}else
					/**ROLE*******************************************************/	
					if(elem instanceof Role &&(result.getType().equals("Role")||result.getType().equals("Content Element"))){
						
						Role r = (Role)elem;
						writeRole(r,r.getGuid(), bwriter, integrateReferences, referencedElements);
					}else
					/**WORK PRODUCT*****************************************************/
					if(elem instanceof WorkProduct &&(result.getType().equals("Work Product")||result.getType().equals("Content Element"))){
						WorkProduct p = (WorkProduct)elem;
						writeWorkProduct(p,p.getGuid(), bwriter, integrateReferences, referencedElements);
					}
				}
				//Add the referenced content elements
				//the 3rd parameter is false because we don't want to include the referenced elements of the already referenced elements
				for(Object o : referencedElements) createReferencedElement(o, bwriter, false, referencedElements);
		
				bwriter.write("</Contents>");
			}
			else{
			bwriter.write("<Processes>");
			for(Object elem : listElements){
				
				/**PROCESS COMPONENT - PATTERN*****************************************************/
				if(elem instanceof ProcessComponent && result.getType().equals("Process")){
					ProcessComponent p = (ProcessComponent)elem;
					
					writePattern(p, bwriter);
				}
								
			}
			for(Object o : referencedElements) createReferencedElement(o, bwriter, false, referencedElements);
			bwriter.write("</Processes>");
			}
			
			
			bwriter.write("</xmi:XMI>\n");
			bwriter.close();
		}
		
		return manifestPath;
	}

	/**Write Pattern****************************/
	private static void writePattern(ProcessComponent p, 
			BufferedWriter bwriter) throws IOException {
		// TODO Auto-generated method stub
		bwriter.write("<pattern xsi:type=\"org.eclipse.epf.uma:CapabilityPattern\" xmi:id=\""+p.getGuid()+"\" name=\""+p.getName()+
				"\" guid=\""+p.getGuid()+"\" presentationName=\""+p.getPresentationName()+"\" briefDescription=\""+p.getBriefDescription()+"\">");
		
		for(ProcessElement b : p.getProcessElements()){
			if(b instanceof Activity){
				
			}
			else if(b instanceof TaskDescriptor){
				writeTaskDescriptor(b, bwriter);
			}
		}
			//Sub-Activities
			for(MethodPackage b : p.getChildPackages()){
				writeActivity((ProcessPackage)b, bwriter);
			}
		
		bwriter.write("</pattern>");
		
	}
	/**Write Pattern - Activity*/
	private static void writeActivity(ProcessPackage a, BufferedWriter bwriter) throws IOException {
	
		for(ProcessElement b : a.getProcessElements()){
			if(b instanceof Activity){
				
				bwriter.write("<Activity xsi:type=\"org.eclipse.epf.uma:Activity\" xmi:id=\""+b.getGuid()+"\" name=\""+b.getName()+
						"\" guid=\""+b.getGuid()+"\" presentationName=\""+b.getPresentationName()+"\" briefDescription=\""+b.getBriefDescription()+"\" predecessors=\"");
				ArrayList<WorkBreakdownElement> predecessors = new ArrayList<WorkBreakdownElement>();
				for(WorkOrder wo : ((WorkBreakdownElement) b).getLinkToPredecessor()){
					bwriter.write(wo.getPred().getGuid()+" ");
					predecessors.add(wo.getPred());
				}
				
				bwriter.write("\">");
			}
			else if(b instanceof TaskDescriptor){
				writeTaskDescriptor(b, bwriter);
			}
		}
		for(MethodPackage b : a.getChildPackages()){
			writeActivity((ProcessPackage)b, bwriter);
		}
		bwriter.write("</Activity>");
	}
	/**Write Pattern - TaskDescriptor*/
	private static void writeTaskDescriptor(ProcessElement a,
			BufferedWriter bwriter) throws IOException {
		bwriter.write("<TaskDescriptor xsi:type=\"org.eclipse.epf.uma:TaskDescriptor\" xmi:id=\""+a.getGuid()+"\" name=\""+a.getName()+
				"\" guid=\""+a.getGuid()+"\" presentationName=\""+a.getPresentationName()+"\" briefDescription=\""+a.getBriefDescription()+"\" predecessors=\"");
	
		ArrayList<WorkBreakdownElement> predecessors = new ArrayList<WorkBreakdownElement>();
		for(WorkOrder wo : ((WorkBreakdownElement) a).getLinkToPredecessor()){
			bwriter.write(wo.getPred().getGuid()+" ");
			predecessors.add(wo.getPred());
		}
		
		bwriter.write("\">");
		if(((TaskDescriptor)a).getTask()!=null)
			writeTask(((TaskDescriptor)a).getTask(), ((TaskDescriptor)a).getTask().getGuid(), bwriter, true, referencedElements);
		
		bwriter.write("</TaskDescriptor>");
		}

	/**Write Task********************************/
	private static void writeTask(Task t, String newId, BufferedWriter bwriter, boolean integrateReferences, List<Object> referencedElements) throws IOException {

		if(integrateReferences ) {
			//Write the attributes that represent the references to other elements
			//And add the referenced element to the fragment
			bwriter.write("<contentElements xsi:type=\"org.eclipse.epf.uma:Task\" xmi:id=\""+newId+"\" name=\""+t.getName()+"\" guid=\""+newId+"\" presentationName=\""+t.getPresentationName()+"\"");
			if(!t.getBriefDescription().isEmpty())bwriter.write(" briefDescription=\""+t.getBriefDescription()+"\"");
			
			writeReferences(t, bwriter, referencedElements);
			
			//Write the ContentDescription properties
			writeContentDescription(t, bwriter);
		}
		else {//If the referenced elements aren't added
			
			bwriter.write("<contentElements xsi:type=\"org.eclipse.epf.uma:Task\" xmi:id=\""+t.getGuid()+"\" name=\""+t.getName()+"\" guid=\""+newId+"\" presentationName=\""+t.getPresentationName()+"\""); 
			if(!(t.getBriefDescription().equals("")))
				bwriter.write(" briefDescription=\""+t.getBriefDescription()+"\"");
			
			//Only integrate the references with elements that are contained in the fragment
			
			writeReferencesInsideTheFragment(t, bwriter, referencedElements);
			//Write the ContentDescription properties
			writeContentDescription(t, bwriter);
		}
	}
	
	private static void writeReferencesInsideTheFragment(Task t,
			BufferedWriter bwriter, List<Object> referencedElements2) throws IOException {
		// TODO Auto-generated method stub
		if(!t.getPerformedBy().isEmpty()){
			bwriter.write(" performedBy=\"");
			for(Role r : t.getPerformedBy()){
				if(listElems.contains(r)){
				bwriter.write(r.getGuid()+" ");
				if(!referencedElements.contains(r) && !listElems.contains(r))
				referencedElements.add(r);
				}
			}
			bwriter.write("\"");
		}
		if(!t.getAdditionallyPerformedBy().isEmpty()){
			bwriter.write(" additionallyPerformedBy=\"");
			for(Role r : t.getAdditionallyPerformedBy()){
				if(listElems.contains(r)){
				bwriter.write(r.getGuid()+" ");
				if(!referencedElements.contains(r) && !listElems.contains(r))
					referencedElements.add(r);
				}
			}
			bwriter.write("\"");
		}
		
		if(!t.getMandatoryInput().isEmpty()){
			bwriter.write(" mandatoryInput=\"");
			for(WorkProduct r : t.getMandatoryInput()){
				if(listElems.contains(r)){
				bwriter.write(r.getGuid()+" ");
				if(!referencedElements.contains(r) && !listElems.contains(r))
					referencedElements.add(r);
				}
			}
			bwriter.write("\"");
		}
		if(!t.getOutput().isEmpty()){
			bwriter.write(" output=\"");
			for(WorkProduct r : t.getOutput()){
				if(listElems.contains(r)){
				bwriter.write(r.getGuid()+" ");
				if(!referencedElements.contains(r) && !listElems.contains(r))
					referencedElements.add(r);
				}
			}
			bwriter.write("\"");
		}
		if(!t.getOptionalInput().isEmpty()){
			bwriter.write(" optionalInput=\"");
			for(WorkProduct r : t.getOptionalInput()){
				if(listElems.contains(r)){
				bwriter.write(r.getGuid()+" ");
				if(!referencedElements.contains(r) && !listElems.contains(r))
					referencedElements.add(r);
				}
			}
			bwriter.write("\"");
		}
	}

	/**Write Role******************************/
	private static void writeRole(Role r, String newId, BufferedWriter bwriter,
			boolean integrateReferences, List<Object> referencedElements) throws IOException {
		
		if(integrateReferences ) {
			//Write the attributes that represent the references to other elements
			//And add the referenced element to the fragment
			bwriter.write("<contentElements xsi:type=\"org.eclipse.epf.uma:Role\" xmi:id=\""+newId+"\" name=\""+r.getName()+"\" guid=\""+newId+"\" presentationName=\""+r.getPresentationName()+"\""); 
			if(!(r.getBriefDescription().equals("")))bwriter.write(" briefDescription=\""+r.getBriefDescription()+"\"");
			writeReferences(r, bwriter, referencedElements);
			
			//Write the ContentDescription properties
			writeContentDescription(r, bwriter);
		
		}else{//If the referenced elements aren't added
			bwriter.write("<contentElements xsi:type=\"org.eclipse.epf.uma:Role\" xmi:id=\""+newId+"\" name=\""+r.getName()+"\" guid=\""+newId+"\" presentationName=\""+r.getPresentationName()+"\""); 
			if(!r.getBriefDescription().equals(null))bwriter.write(" briefDescription=\""+r.getBriefDescription()+"\"");
			writeReferencesInsideTheFragment(r, bwriter, referencedElements);
			//Write the ContentDescription properties
			writeContentDescription(r, bwriter);
		}
	}
	
	private static void writeReferencesInsideTheFragment(Role r,
			BufferedWriter bwriter, List<Object> referencedElements2) throws IOException {
		// TODO Auto-generated method stub

		if(!r.getResponsibleFor().isEmpty()){
			bwriter.write(" responsibleFor=\"");
			for(WorkProduct w : r.getResponsibleFor()){
				if(listElems.contains(w)){
				bwriter.write(w.getGuid()+" ");
				if(!referencedElements.contains(w) && !listElems.contains(w))
					referencedElements.add(w);
				}
				
			}
			bwriter.write("\"");
		}
	}

	/**Write Work Product ***********************/
	private static void writeWorkProduct(WorkProduct p, String newId, BufferedWriter bwriter,
			boolean integrateReferences, List<Object> referencedElements) throws IOException {
		
		bwriter.write("<contentElements xsi:type=\"org.eclipse.epf.uma:Artifact\" xmi:id=\""+newId+"\" name=\""+p.getName()+"\" guid=\""+newId+"\" presentationName=\""+p.getPresentationName()+"\""); 
		if(!(p.getBriefDescription().equals("")))bwriter.write(" briefDescription=\""+p.getBriefDescription()+"\"");
		
		//Write the ContentDescription properties
		
		writeContentDescription(p, bwriter);
	}

	/**Task - Write references and create elements*/
	private static void writeReferences(Task t, BufferedWriter bwriter,
			List<Object> referencedElements) throws IOException {
		
		if(!t.getPerformedBy().isEmpty()){
			bwriter.write(" performedBy=\"");
			for(Role r : t.getPerformedBy()){
				bwriter.write(r.getGuid()+" ");
				if(!referencedElements.contains(r) && !listElems.contains(r))
				referencedElements.add(r);
			}
			bwriter.write("\"");
		}
		if(!t.getAdditionallyPerformedBy().isEmpty()){
			bwriter.write(" additionallyPerformedBy=\"");
			for(Role r : t.getAdditionallyPerformedBy()){
				bwriter.write(r.getGuid()+" ");
				if(!referencedElements.contains(r) && !listElems.contains(r))
					referencedElements.add(r);
			}
			bwriter.write("\"");
		}
		
		if(!t.getMandatoryInput().isEmpty()){
			bwriter.write(" mandatoryInput=\"");
			for(WorkProduct r : t.getMandatoryInput()){
				bwriter.write(r.getGuid()+" ");
				if(!referencedElements.contains(r) && !listElems.contains(r))
					referencedElements.add(r);
			}
			bwriter.write("\"");
		}
		if(!t.getOutput().isEmpty()){
			bwriter.write(" output=\"");
			for(WorkProduct r : t.getOutput()){
				bwriter.write(r.getGuid()+" ");
				if(!referencedElements.contains(r) && !listElems.contains(r))
					referencedElements.add(r);
			}
			bwriter.write("\"");
		}
		if(!t.getOptionalInput().isEmpty()){
			bwriter.write(" optionalInput=\"");
			for(WorkProduct r : t.getOptionalInput()){
				bwriter.write(r.getGuid()+" ");
				if(!referencedElements.contains(r) && !listElems.contains(r))
					referencedElements.add(r);
			}
			bwriter.write("\"");
		}
	}
	
	/**Role - Write references and create elements*/
	private static void writeReferences(Role r, BufferedWriter bwriter, List<Object> referencedElements) throws IOException {
		
		if(!r.getResponsibleFor().isEmpty()){
			bwriter.write(" responsibleFor=\"");
			for(WorkProduct w : r.getResponsibleFor()){
				bwriter.write(w.getGuid()+" ");
				if(!referencedElements.contains(w) && !listElems.contains(w))
					referencedElements.add(w);
				
				
			}
			bwriter.write("\"");
		}
	}
	
	/**Task - Write Content Description*/
	private static void writeContentDescription(Task t,BufferedWriter bwriter) throws IOException{
		
		TaskDescription cd = (TaskDescription) t.getPresentation();
		if(cd.getMainDescription().isEmpty() && cd.getKeyConsiderations().isEmpty() && cd.getPurpose().isEmpty()
				&& cd.getAlternatives().isEmpty() ) {
			 bwriter.write("/>");;
		}
		else
			{
			bwriter.write("><contentDescription>");
			if(!cd.getMainDescription().isEmpty()){
				bwriter.write("<mainDescription>"+cd.getMainDescription()+"</mainDescription>");
			}
			if(!cd.getKeyConsiderations().isEmpty()){
				bwriter.write("<keyConsiderations>"+cd.getKeyConsiderations()+"</keyConsiderations>");
			}
			if(!cd.getPurpose().isEmpty()){
				bwriter.write("<purpose>"+cd.getPurpose()+"</purpose>");
			}
			if(!cd.getAlternatives().isEmpty()){
				bwriter.write("<alternatives>"+cd.getAlternatives()+"</alternatives>");
			}
			bwriter.write("</contentDescription></contentElements>");
		}
		
	}
	
	/**Role - Write Content Description*/
	private static void writeContentDescription(Role r, BufferedWriter bwriter) throws IOException {
	
		RoleDescription cd = (RoleDescription) r.getPresentation();
		if(cd.getMainDescription().isEmpty() && cd.getKeyConsiderations().isEmpty() && cd.getSkills().isEmpty()
				&& cd.getAssignmentApproaches().isEmpty() && cd.getSynonyms().isEmpty()){
			bwriter.write("/>");
		}
		else{
			bwriter.write("><contentDescription>");
			if(!cd.getMainDescription().isEmpty()){
				bwriter.write("<mainDescription>"+cd.getMainDescription()+"</mainDescription>");
			}
			if(!cd.getKeyConsiderations().isEmpty()){
				bwriter.write("<keyConsiderations>"+cd.getKeyConsiderations()+"</keyConsiderations>");
			}
			if(!cd.getSkills().isEmpty()){
				bwriter.write("<skills>"+cd.getSkills()+"</skills>");
			}
			if(!cd.getAssignmentApproaches().isEmpty()){
				bwriter.write("<assignmentApproaches>"+cd.getAssignmentApproaches()+"</assignmentApproaches>");
			}
			if(!cd.getSynonyms().isEmpty()){
				bwriter.write("<synonyms>"+cd.getSynonyms()+"</synonyms>");
			}
			bwriter.write("</contentDescription></contentElements>");
			
		}
	}
	
	/**Artifact - Write Content Description*/
	private static void writeContentDescription(WorkProduct p,
			BufferedWriter bwriter) throws IOException {
		
		ArtifactDescription cd = (ArtifactDescription) p.getPresentation();
		if(cd.getMainDescription().isEmpty() && cd.getKeyConsiderations().isEmpty() && cd.getPurpose().isEmpty()
				&& cd.getImpactOfNotHaving().isEmpty() && cd.getReasonsForNotNeeding().isEmpty() && cd.getBriefOutline().isEmpty()
				&& cd.getRepresentationOptions().isEmpty() && cd.getRepresentation().isEmpty()
				&& cd.getNotation().isEmpty()&& p.getMethodElementProperty().isEmpty()) {
			bwriter.write("/>");
		}
		else{
			bwriter.write("><contentDescription>");
			if(!cd.getMainDescription().isEmpty()){
				bwriter.write("<mainDescription>"+cd.getMainDescription()+"</mainDescription>");
				}
			if(!cd.getKeyConsiderations().isEmpty()){
				bwriter.write("<keyConsiderations>"+cd.getKeyConsiderations()+"</keyConsiderations>");
				}
			if(!cd.getPurpose().isEmpty()){
				bwriter.write("<purpose>"+cd.getPurpose()+"</purpose>");
				}
			if(!cd.getImpactOfNotHaving().isEmpty()){
				bwriter.write("<impactOfNotHaving>"+cd.getImpactOfNotHaving()+"</impactOfNotHaving>");
				}
			if(!cd.getReasonsForNotNeeding().isEmpty()){
				bwriter.write("<reasonsForNotNeeding>"+cd.getReasonsForNotNeeding()+"</reasonsForNotNeeding>");
				}
			if(!cd.getBriefOutline().isEmpty()){
				bwriter.write("<briefOutline>"+cd.getBriefOutline()+"</briefOutline>");
				}
			if(!cd.getRepresentationOptions().isEmpty()){
				bwriter.write("<representationOptions>"+cd.getRepresentationOptions()+"</representationOptions>");
				}
			if(!cd.getRepresentation().isEmpty()){
				bwriter.write("<representation>"+cd.getRepresentation()+"</representation>");
				}
			if(!cd.getNotation().isEmpty()){
				bwriter.write("<notation>"+cd.getNotation()+"</notation>");
				}
			if(!p.getMethodElementProperty().isEmpty()){
				for(MethodElementProperty mep : p.getMethodElementProperty()){
					bwriter.write("<"+mep.getName()+">"+mep.getValue()+"</"+mep.getName()+">");
				}				
				}
			bwriter.write("</contentDescription></contentElements>");
		}
	}
	
	/**Create referenced element (parameter */
	private static void createReferencedElement(Object o, BufferedWriter bwriter, boolean integrateReferences, List<Object> referencedElements) throws IOException {
		
			if(o instanceof Task){
				writeTask((Task)o,((Task) o).getGuid(), bwriter, integrateReferences, referencedElements);
				}else
			if(o instanceof Role){
				writeRole((Role)o, ((Role) o).getGuid(), bwriter, integrateReferences, referencedElements);
				}else
			if(o instanceof WorkProduct){
				writeWorkProduct((WorkProduct)o,((WorkProduct) o).getGuid(), bwriter, integrateReferences, referencedElements);
			}
		
	}
	
	/**This method is in charge of create the ras XML file for the Conceptual Fragment*/
	public static String createRassetXMLFile(ConceptualFragment tf, String folder) throws Exception {
		
		String manifestPath = folder + "/rasset.xml";
		File f = new File(manifestPath);
		if(!f.exists()) {
			f.createNewFile();
			
			String type = tf.getType();
			String origin = type.equals("Others") ? "no origin" : tf.getOrigin();
			String objective = type.equals("Others") ? "no objective" : tf.getObjective();
		
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(f));
			bwriter.write("<asset xsi:schemaLocation=\"DefaultprofileXML.xsd\" name=\"" + tf.getName() + "\" id=\"1\">\n");
			bwriter.write("<type value=\"" + type + "\"></type>\n");
			bwriter.write("<origin value=\"" + origin + "\"></origin>\n");
			bwriter.write("<objective value=\"" + objective + "\"></objective>\n");
			
			bwriter.write("</asset>\n");
			bwriter.close();
		}
		
		return manifestPath;
	}
	
	protected static String randomString(int size){
		
		char[] chars = "abcdefghijklmnopqrstuvwxyz12345678_-".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		String output = sb.toString();
		return output;
	}
	
}
