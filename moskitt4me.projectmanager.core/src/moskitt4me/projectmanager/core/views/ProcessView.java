package moskitt4me.projectmanager.core.views;

import java.util.ArrayList;
import java.util.List;

import moskitt4me.projectmanager.core.actions.RunAction;
import moskitt4me.projectmanager.core.actions.ProcessViewActionGroup;
import moskitt4me.projectmanager.core.actions.RunRepeatableAction;
import moskitt4me.projectmanager.core.actions.UndoAction;
import moskitt4me.projectmanager.core.actions.RoleSelectionAction.NullRole;
import moskitt4me.projectmanager.core.context.Context;
import moskitt4me.projectmanager.core.context.ProjectUpdater;
import moskitt4me.projectmanager.core.filters.ProcessFilter;
import moskitt4me.projectmanager.core.providers.ActivityCP;
import moskitt4me.projectmanager.core.providers.CapabilityPatternCP;
import moskitt4me.projectmanager.core.providers.ProcessContentProvider;
import moskitt4me.projectmanager.core.providers.ProcessContextProvider;
import moskitt4me.projectmanager.core.providers.ProcessLabelProvider;
import moskitt4me.projectmanager.core.providers.ProcessSelectionProvider;
import moskitt4me.projectmanager.core.providers.TaskDescriptorCP;
import moskitt4me.projectmanager.core.sorters.ProcessSorter;
import moskitt4me.projectmanager.methodspecification.MethodElements;
import moskitt4me.projectmanager.processsupport.Engine;
import moskitt4me.projectmanager.processsupport.util.EngineUtil;
import moskitt4me.projectmanager.productsupport.ProductSupport;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.epf.uma.Activity;
import org.eclipse.epf.uma.BreakdownElement;
import org.eclipse.epf.uma.CapabilityPattern;
import org.eclipse.epf.uma.DeliveryProcess;
import org.eclipse.epf.uma.RoleDescriptor;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.VariabilityElement;
import org.eclipse.epf.uma.VariabilityType;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;

/**
 * The Process view shows the current state of the process instance that is associated to the
 * project that is selected in the Resource Explorer view.
 * 
 * @author Mario Cervera
 */
public class ProcessView extends ProjectManagerViewPart {

	public static final String ProcessViewId = "moskitt4me.projectmanager.core.views.processView";
	
	private ProcessContentProvider contentProvider;
	
	private ProcessSelectionProvider selectionProvider = new ProcessSelectionProvider();
	
	@Override
	public void createPartControl(Composite parent) {
		
		super.createPartControl(parent);
		
		if(getViewer() != null) {
			
			getViewer().addSelectionChangedListener(new ProcessViewSelectionChangedListener());
		}
		
		//Register SelectionProvider so that selection changes in the view site
		//trigger a context change. This is needed for dynamic help.
		
		getSite().setSelectionProvider(selectionProvider);
		
		//When the selected project changes, the Project Manager must be notified
		
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(new ProjectUpdater());
		
		this.updateContentDescription();
	}
	
	/*
	 * Double Click in the Process view is used to invoke task execution
	 */
	@Override
	protected void handleDoubleClick(DoubleClickEvent anEvent) {
		
		super.handleDoubleClick(anEvent);
		
		if(anEvent.getSelection() instanceof StructuredSelection) {
			
			StructuredSelection selection = (StructuredSelection) anEvent.getSelection();
			
			if(selection.getFirstElement() instanceof TaskDescriptor) {
				
				TaskDescriptor td = (TaskDescriptor) selection.getFirstElement();
				
				List<String> cpIds = new ArrayList<String>();
				
				String activitiTaskId = MethodElements.getActivitiTaskId(td);
				String processInstanceId = "";
				
				if(td instanceof TaskDescriptorCP) {
					TaskDescriptorCP tdcp = (TaskDescriptorCP) td;
					cpIds.addAll(tdcp.getCpGuids());
					processInstanceId = getProcessInstanceId(tdcp.getCpGuids());
				}
				else {
					processInstanceId = getProcessInstanceId(null);
					cpIds.add("dp");
				}
				
				if (Engine.isExecutable(activitiTaskId, 
						processInstanceId, Context.selectedProject)) {
					
					boolean productCreated = ProductSupport.launchTask(td, Context.selectedProject,
									MethodElements.workProducts, MethodElements.tasks, cpIds);

					if(productCreated) {
						
						//Refresh the Product Explorer view
						
						ProductExplorerView productExplorer = getProductExplorerView();
						
						if (productExplorer != null) {
							productExplorer.refreshViewer();
						}
						
						//Refresh project
						
						try{
							Context.selectedProject.refreshLocal(IResource.DEPTH_INFINITE,
									new NullProgressMonitor());
						}
						catch(Exception e) {
							
						}
					}
				}
			}
		}
	}
	
	private ProductExplorerView getProductExplorerView() {
		
		IViewPart viewPart = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().findView(
						ProductExplorerView.ProductExplorerViewId);
		
		if(viewPart instanceof ProductExplorerView) {
			return (ProductExplorerView) viewPart;
		}
		
		return null;
	}
	
	/*
	* Selection change events in the Process view will refresh the Guides view and the state of the
	* buttons of the Action Bar.
	*/
	private class ProcessViewSelectionChangedListener implements ISelectionChangedListener {
		
		public void selectionChanged(SelectionChangedEvent event) {
			
			if(event.getSelection() instanceof StructuredSelection) {
				Object[] selectedObjects = ((StructuredSelection)event.getSelection()).toArray();
				Object firstElement = ((StructuredSelection)event.getSelection()).getFirstElement();
				
				GuidesView guidesView = getGuidesView();
				if(guidesView != null) {
					if(firstElement instanceof TaskDescriptor) {
						guidesView.getViewer().setInput((TaskDescriptor) firstElement);
					}
					else {
						guidesView.getViewer().setInput(null);
					}
				}
				
				enableRunButtons(selectedObjects);
				
				//Trigger selection changed in the view site so a context change is notified.
				//This is needed for dynamic help.
				
				selectionProvider.setSelection(event.getSelection());
			}
		}
		
		private GuidesView getGuidesView() {
			
			try {
				IViewPart viewPart = PlatformUI.getWorkbench().
				getActiveWorkbenchWindow().getActivePage().findView(GuidesView.GuidesViewId);
				
				if(viewPart instanceof GuidesView) {
					return (GuidesView) viewPart;
				}
			}
			catch(Exception e) {
				return null;
			}
			
			return null;
		}
	}
	
	/*
	 * Handles the enablement of the Undo button. 
	 */
	public void enableUndoButton() {
		
		UndoAction action = getUndoAction();
		
		if(Context.selectedProject == null) {
			action.setEnabled(false);
		}
		else {
			if(EngineUtil.hasExecutedTasks(Context.selectedProject)) {
				action.setEnabled(true);
			}
			else {
				action.setEnabled(false);
			}
		}
	}
	
	/*
	 * Handles the enablement of the Run buttons (Run Action and Run Repeatable Action). 
	 */
	private void enableRunButtons(Object[] selectedObjects) {
		
		RunAction runAction = getRunAction();
		RunRepeatableAction runRepeatableAction = getRunRepeatableAction();

		if(runAction == null || runRepeatableAction == null) {
			return;
		}
		
		boolean enabledRun = true;
		boolean enabledRunRepeatable = true;
		
		if(selectedObjects != null && selectedObjects.length > 0) {
			
			for(int i = 0; i < selectedObjects.length && 
				(enabledRun || enabledRunRepeatable); i++) {
				
				Object obj = selectedObjects[i];
				
				if(obj instanceof DeliveryProcess) {
					enabledRun = false;
					enabledRunRepeatable = false;
					continue;
				}
				
				List<String> cpIds = new ArrayList<String>();
				boolean extendedcp = false;
				
				if (obj instanceof CapabilityPattern
						&& ((CapabilityPattern) obj).getVariabilityType()
								.equals(VariabilityType.EXTENDS)) {

					CapabilityPattern cp = (CapabilityPattern) obj;
					
					if(!cp.getIsRepeatable()) {
						enabledRunRepeatable = false;
					}
					
					extendedcp = true;
					
					if(cp instanceof CapabilityPatternCP) {
						CapabilityPatternCP cpcp = (CapabilityPatternCP) cp;
						cpIds.addAll(cpcp.getCpGuids());
					}
					cpIds.add(cp.getGuid());
					
					CapabilityPattern cp2 = MethodElements.getCapabilityPattern(getGUId(cp));
						
					obj = cp2;
				}
				if(obj instanceof Activity && !(obj instanceof DeliveryProcess)) {
					
					Activity actv = (Activity) obj;
					
					if(!extendedcp && !actv.getIsRepeatable()) {
						enabledRunRepeatable = false;
					}
					
					if(actv instanceof ActivityCP) {
						cpIds = ((ActivityCP) actv).getCpGuids();
					}
					else if (actv instanceof CapabilityPatternCP
							&& !((CapabilityPatternCP) obj).getVariabilityType()
									.equals(VariabilityType.EXTENDS)) {

						cpIds = ((CapabilityPatternCP) actv).getCpGuids();
					}
					if(!containsExecutableTask(actv, cpIds)) {
						enabledRun = false;
						enabledRunRepeatable = false;
					}
				}
				else if(obj instanceof TaskDescriptor) {
					TaskDescriptor td = (TaskDescriptor) obj;
					
					if(!td.getIsRepeatable()) {
						enabledRunRepeatable = false;
					}
					
					if(td instanceof TaskDescriptorCP) {
						TaskDescriptorCP tdcp = (TaskDescriptorCP) td;
						cpIds = tdcp.getCpGuids();
					}
					
					String activitiTaskId = MethodElements.getActivitiTaskId(td);
					String processInstanceId = getProcessInstanceId(cpIds);
					
					if (!Engine.isExecutable(activitiTaskId, 
							processInstanceId, Context.selectedProject)) {
						
						enabledRun = false;
						enabledRunRepeatable = false;
					}
				} 
			}
		}
		else {
			enabledRun = false;
			enabledRunRepeatable = false;
		}
		
		runAction.setEnabled(enabledRun);
		runRepeatableAction.setEnabled(enabledRunRepeatable);
	}
	
	private UndoAction getUndoAction() {
		
		for(IContributionItem item : this.getViewSite().getActionBars().getToolBarManager().getItems()) {
			if(item instanceof ActionContributionItem) {
				ActionContributionItem acitem = (ActionContributionItem) item;
				if(acitem.getAction() instanceof UndoAction) {
					return (UndoAction)acitem.getAction();
				}
			}
		}
		return null;
	}
	
	private RunAction getRunAction() {
		
		for(IContributionItem item : this.getViewSite().getActionBars().getToolBarManager().getItems()) {
			if(item instanceof ActionContributionItem) {
				ActionContributionItem acitem = (ActionContributionItem) item;
				if(acitem.getAction() instanceof RunAction) {
					return (RunAction) acitem.getAction();
				}
			}
		}
		return null;
	}
	
	private RunRepeatableAction getRunRepeatableAction() {
		
		for(IContributionItem item : this.getViewSite().getActionBars().getToolBarManager().getItems()) {
			if(item instanceof ActionContributionItem) {
				ActionContributionItem acitem = (ActionContributionItem) item;
				if(acitem.getAction() instanceof RunRepeatableAction) {
					return (RunRepeatableAction) acitem.getAction();
				}
			}
		}
		return null;
	}

	@Override
	protected ActionGroup createActionGroup() {
		
		return new ProcessViewActionGroup(this.getViewer());
	}
	
	/*
	* This method creates an object that provides content for the Process view.
	*/
	@Override
	protected IContentProvider createContentProvider() {
		
		contentProvider = new ProcessContentProvider(
				new ResourceItemProviderAdapterFactory());
		
		return contentProvider;
	}
	
	/*
	* This method creates an object that provides labels and icons for the graphical
	* elements of the Process view (which are provided by the Content Provider).
	*/
	@Override
	protected IBaseLabelProvider createLabelProvider() {
		
		return new ProcessLabelProvider();
	}
	
	/*
	* The process filter handles the filtering of elements based on the selected role and the
	* display mode of the Process view.
	*/
	@Override
	protected ViewerFilter[] createFilters() {

		ViewerFilter[] filters = new ViewerFilter[1];
		filters[0] = new ProcessFilter();
		
		return filters;
	}

	@Override
	protected ViewerSorter createSorter() {
		
		return new ProcessSorter();
	}
	
	public void updateContentDescription() {
		
		String description = "Selected roles:";
		
		if (Context.currentRoles.size() == 0) {
			description += " <None>";
		}
		else if (Context.currentRoles.size() == 1
				&& Context.currentRoles.get(0) instanceof NullRole) {
			
			description += " <None>";
		}
		else {
			for(RoleDescriptor rd : Context.currentRoles) {
				description += " " + getName(rd) + ",";
			}
			
			description = description.substring(0, description.length() - 1);
		}
		
		this.setContentDescription(description);
	}
	
	public void clearContentProvider() {
		
		if(this.contentProvider != null) {
			this.contentProvider.clear();
		}
	}
	
	private String getName(RoleDescriptor rd) {
		
		if(rd.getPresentationName() != null && !rd.getPresentationName().equals("")) {
			return rd.getPresentationName();
		}
		else return rd.getName();
	}
	
	@Override
	protected int getTreeStyle() {
		
		return SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL;
	}
	
	@Override
	public Object getAdapter(Class adapter) {
		
		if (adapter.equals(IContextProvider.class)) { // Dynamic help
			return new ProcessContextProvider();
		}
		return super.getAdapter(adapter);
	}
	
	private String getProcessInstanceId(List<String> cpIds) {
		
		if(cpIds == null || cpIds.size() == 0) {
			return Context.processInstanceId;
		}
		else {
			return Context.getProcessInstance(cpIds);
		}
	}
	
	private boolean containsExecutableTask(Activity actv, List<String> cpIds) {
		
		boolean containsExecutableTask = false;
		
		List<BreakdownElement> elements = actv.getBreakdownElements();
		int size = elements.size();
		
		for(int i = 0; i < size && !containsExecutableTask; i++) {
			BreakdownElement be = actv.getBreakdownElements().get(i);
			
			if(be instanceof TaskDescriptor) {
				TaskDescriptor td = (TaskDescriptor) be;
				
				String activitiTaskId = MethodElements.getActivitiTaskId(td);
				String processInstanceId = "";
				
				if(td instanceof TaskDescriptorCP) {
					TaskDescriptorCP tdcp = (TaskDescriptorCP) td;
					processInstanceId = getProcessInstanceId(tdcp.getCpGuids());
				}
				else if(cpIds != null && cpIds.size() > 0) {
					processInstanceId = getProcessInstanceId(cpIds);
				}
				else {
					processInstanceId = getProcessInstanceId(null);
				}
				
				if (Engine.isExecutable(activitiTaskId, 
						processInstanceId, Context.selectedProject)) {
					
					containsExecutableTask = true;
				}
			}
			else if(be instanceof Activity) {
				Activity actv2 = (Activity) be;
			
				if (actv2 instanceof CapabilityPattern
						&& ((CapabilityPattern) actv2).getVariabilityType()
								.equals(VariabilityType.EXTENDS)) {

					CapabilityPattern cp = (CapabilityPattern) actv2;
					List<String> guids = new ArrayList<String>();
					guids.addAll(cpIds);
					guids.add(cp.getGuid());
					CapabilityPattern cp2 = MethodElements
							.getCapabilityPattern(getGUId(cp));
					containsExecutableTask = containsExecutableTask(cp2, guids);
				}
				else if(actv2 instanceof ActivityCP) {
					ActivityCP actvcp = (ActivityCP) actv2;
					containsExecutableTask = containsExecutableTask(actvcp, actvcp.getCpGuids());
				}
				else if(actv2 instanceof CapabilityPatternCP) {
					CapabilityPatternCP cpcp = (CapabilityPatternCP) actv2;
					containsExecutableTask = containsExecutableTask(cpcp, cpcp.getCpGuids());
				}
				else {
					containsExecutableTask = containsExecutableTask(actv2, cpIds);
				}
			}
		}
		
		return containsExecutableTask;
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
}
