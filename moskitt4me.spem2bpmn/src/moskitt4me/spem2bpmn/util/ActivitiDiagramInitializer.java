package moskitt4me.spem2bpmn.util;

import java.util.ArrayList;
import java.util.Collections;

import org.activiti.designer.diagram.ActivitiBPMNDiagramTypeProvider;
import org.activiti.designer.diagram.ActivitiBPMNFeatureProvider;
import org.activiti.designer.eclipse.editor.ActivitiDiagramEditor;
import org.activiti.designer.eclipse.editor.ActivitiMultiPageEditor;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epf.library.LibraryService;
import org.eclipse.epf.uma.MethodLibrary;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/*
 * This class provides a method for initializing the graphical elements of a (BPMN 2.0)
 * Activiti model (i.e., for generating a BPMN 2.0 diagram from a tree-based process
 * definition).
 *
 * @author Mario Cervera
 */
public class ActivitiDiagramInitializer {

	private static int x;
	private static int y;
	
	//These attributes are used to position the graphical elements in the canvas
	
	private static final int X_INITIAL_VALUE;
	private static final int Y_INITIAL_VALUE;
	private static final int X_INCREMENT;
	private static final int Y_INCREMENT;
	
	private static final String activitiEditorID = "org.activiti.designer.editor.multiPageEditor";
	
	static {
		X_INITIAL_VALUE = 30;
		Y_INITIAL_VALUE = 75;
		X_INCREMENT = 160;
		Y_INCREMENT = 100;
		
		x = X_INITIAL_VALUE;
		y = Y_INITIAL_VALUE;
	}
	
	public static void initializeDiagram(String modelPath) throws Exception {
		
		Resource resource = getResource(modelPath); // .activiti file
		
		Diagram diagram = createDiagram(resource); //Diagram contained within the .activiti file
		
		openActivitiEditor(modelPath);
		
		ActivitiBPMNFeatureProvider abfp = createActivitiBPMNFeatureProvider(diagram, modelPath);
		
		ArrayList<ArrayList<FlowNode>> flowNodes = new ArrayList<ArrayList<FlowNode>>();
		ArrayList<SequenceFlow> sequenceFlows = new ArrayList<SequenceFlow>();
		
		preprocessActivitiModel(resource, flowNodes, sequenceFlows);
		
		/*****************************************************************
		 **** Generate the Flow Nodes (the yellow boxes of the model) ****
		 *****************************************************************/
		
		int i = 0;
		
		for(ArrayList<FlowNode> level_n : flowNodes) {
			
			y = Y_INITIAL_VALUE;
			
			for(FlowNode flowNode : level_n) {
				
				createFlowNodeGraphicalRepresentation(resource, diagram, flowNode, abfp);
				
				y += Y_INCREMENT;
			}
			
			if(i == 0) x += (X_INCREMENT - 50); //The start event is smaller than the tasks
			else x += X_INCREMENT;
			
			i++;
		}
		
		x = X_INITIAL_VALUE;
		y = Y_INITIAL_VALUE;
		
		/*********************************************************
		 ***** Generate the Sequence Flows (the black arrows) ****
		 *********************************************************/
		
		for(EObject eobj : resource.getContents()) {
			
			if(eobj instanceof SequenceFlow) {
				
				SequenceFlow sequenceFlow = (SequenceFlow) eobj;
				
				createSequenceFlowGraphicalRepresentation(sequenceFlow, abfp);
			}
		}
		
		resource.save(Collections.EMPTY_MAP);
		
		refreshLibrary();
		
		refreshEditor(modelPath);
	}
	
	private static Resource getResource(String modelPath) {
		
		XMIResourceFactoryImpl _xmiFac = new XMIResourceFactoryImpl();        
        
        ResourceSet rSet = new ResourceSetImpl();
        
        rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",_xmiFac);
        URI uri = URI.createURI(modelPath, false);
        
        Resource resource = rSet.getResource(uri, true);
        
        return resource;
	}
	
	private static Diagram createDiagram(Resource r) throws Exception {
		
		Diagram diagram = Graphiti.getPeCreateService().createDiagram("BPMNdiagram", "output", true); 
		
		r.getContents().add(0, diagram);
		
		r.save(Collections.EMPTY_MAP);
		
		return diagram;
	}
	
	/*
	 * Returns the ActivitiDiagramEditor. It must be already open, otherwise it will return null.
	 */
	private static ActivitiDiagramEditor getActivitiDiagramEditor(String activitiModelPath) {
			
		IResource activitiFile = ResourcesPlugin.getWorkspace().getRoot()
		.getFileForLocation(
				new Path(activitiModelPath.replaceFirst("file:", "")));
		
		if(activitiFile instanceof IFile) {
			IEditorInput input  = new FileEditorInput((IFile)activitiFile);
			IEditorPart editor = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().findEditor(input);
			
			if(editor instanceof ActivitiMultiPageEditor) {
				ActivitiMultiPageEditor multipageEditor = (ActivitiMultiPageEditor) editor;
				return multipageEditor.getActivitiDiagramEditor();
			}
		}
		
		return null;
	}
	
	private static ActivitiBPMNFeatureProvider createActivitiBPMNFeatureProvider(Diagram diagram, String modelPath) {
		
		ActivitiBPMNDiagramTypeProvider tp =new ActivitiBPMNDiagramTypeProvider();
		ActivitiDiagramEditor editor = getActivitiDiagramEditor(modelPath);
		tp.init(diagram, editor);
		
		return new ActivitiBPMNFeatureProvider(tp);		
	}
	
	/*
	 * After the execution of this method, each of the elements of the list "flowNodes"
	 * will contain all the flow nodes pertaining to a specific level of the process:
	 * 
	 *  Level 0 -> Start Event
	 *  Level 1 -> Elements immediately subsequent to the Start Event
	 *  Level 2 -> Elements immediately subsequent to a element pertaining to level 1
	 *  ...
	 *  Level n-1 -> End Event
	 *  
	 *  "sequenceFlows" will contain all the sequence flows
	 */
	private static void preprocessActivitiModel(Resource resource,
			ArrayList<ArrayList<FlowNode>> flowNodes,
			ArrayList<SequenceFlow> sequenceFlows) {
		
		ArrayList<FlowNode> allFlowNodes = new ArrayList<FlowNode>();
		
		for(EObject eobj : resource.getContents()) {
			if(eobj instanceof FlowNode) {
				allFlowNodes.add((FlowNode)eobj);
			}
			else if(eobj instanceof SequenceFlow) {
				sequenceFlows.add((SequenceFlow)eobj);
			}
		}
		
		if(allFlowNodes.size() > 0) {
			
			//Add level zero
			
			StartEvent startEvent = null;
			
			for(FlowNode flowNode : allFlowNodes) {
				if(flowNode instanceof StartEvent) {
					startEvent = (StartEvent) flowNode;
					break;
				}
			}
			
			if(startEvent != null) {
				ArrayList<FlowNode> levelzero = new ArrayList<FlowNode>();
				levelzero.add(startEvent);
				flowNodes.add(levelzero);
				allFlowNodes.remove(startEvent);
			}
			
			//Add remaning levels
			
			addRemainingLevels(flowNodes, sequenceFlows, allFlowNodes);
			
			//Add EventEvent
			
			if(allFlowNodes.size() == 1) {
				ArrayList<FlowNode> lastLevel = new ArrayList<FlowNode>();
				lastLevel.add(allFlowNodes.get(0));
				flowNodes.add(lastLevel);
			}
		}
	}
	
	private static void addRemainingLevels(ArrayList<ArrayList<FlowNode>> flowNodes,
			ArrayList<SequenceFlow> sequenceFlows, ArrayList<FlowNode> allFlowNodes) {
		
		if(allFlowNodes.size() == 1) return; //Only the End Event remains
		
		ArrayList<FlowNode> newLevel = new ArrayList<FlowNode>();
		
		for(FlowNode node : allFlowNodes) {
			if(hasPredecessorInLastLevel(node, sequenceFlows, flowNodes)){
				if(!(node instanceof EndEvent)) {
					newLevel.add(node);
				}
			}
		}
		
		allFlowNodes.removeAll(newLevel);
		flowNodes.add(newLevel);
		addRemainingLevels(flowNodes, sequenceFlows, allFlowNodes);
	}
	
	private static boolean hasPredecessorInLastLevel(FlowNode node,
			ArrayList<SequenceFlow> sequenceFlows, ArrayList<ArrayList<FlowNode>> flowNodes) {
		
		for(FlowNode fn : flowNodes.get(flowNodes.size() - 1)) {
			for(SequenceFlow sequenceFlow : sequenceFlows) {
				if(sequenceFlow.getSourceRef().getId().equals(fn.getId()) &&
						sequenceFlow.getTargetRef().getId().equals(node.getId())) {
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	private static CreateContext createCreateContext(Resource r, Diagram diagram) {
		
		CreateContext cc = new CreateContext();
		cc.setX(x);
		cc.setY(y);
		cc.setHeight(-1); //Default height
		cc.setWidth(-1); //Default width
		
		if(diagram != null) {
			cc.setTargetContainer(diagram);
		}
		
		return cc;
	}
	
	private static void createFlowNodeGraphicalRepresentation(Resource resource,
			Diagram diagram, FlowNode flowNode, ActivitiBPMNFeatureProvider abfp) {

		CreateContext cc = createCreateContext(resource, diagram);
		abfp.addIfPossible(new AddContext(cc, flowNode));
	}
	
	private static void createSequenceFlowGraphicalRepresentation(
			SequenceFlow sequenceFlow, ActivitiBPMNFeatureProvider abfp) {

		AddConnectionContext addContext = new AddConnectionContext(null, null);
		addContext.setNewObject(sequenceFlow);
		abfp.addIfPossible(addContext);
	}
	
	public static void openActivitiEditor(String activitiModelPath) throws Exception {

		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		IResource activitiFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFileForLocation(
						new Path(activitiModelPath.replaceFirst("file:", "")));

		if (activitiFile instanceof IFile) {
			IEditorInput input = new FileEditorInput((IFile) activitiFile);
			page.openEditor(input, activitiEditorID);
		}
	}
	
	private static void refreshLibrary() throws Exception {
		
		MethodLibrary lib = LibraryService.getInstance().getCurrentMethodLibrary();
		if(lib != null) {
			String libraryName = lib.getName();
			IProject libraryProject = ResourcesPlugin.getWorkspace()
					.getRoot().getProject(libraryName);
			if(libraryProject != null) {
				libraryProject.refreshLocal(IResource.DEPTH_INFINITE,
						new NullProgressMonitor());
			}
		}
	}
	
	/*
	 * Refresh the ActivitiDiagramEditor by closing it and opening it again
	 */
	private static void refreshEditor(String activitiModelPath) throws Exception {
		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		IResource activitiFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFileForLocation(
						new Path(activitiModelPath.replaceFirst("file:", "")));
		
		if(activitiFile instanceof IFile) {
			IEditorInput input = new FileEditorInput((IFile)activitiFile);
			IEditorPart editor = page.findEditor(input);
			if(editor != null) {
				page.closeEditor(editor, false);
				page.openEditor(input, activitiEditorID).doSave(new NullProgressMonitor()); //Save action generates .xml file
				
				refreshLibrary();
			}
			
		}
	}
}
