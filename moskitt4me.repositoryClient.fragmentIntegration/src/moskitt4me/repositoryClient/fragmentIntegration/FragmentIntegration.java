package moskitt4me.repositoryClient.fragmentIntegration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import moskitt4me.repositoryClient.fragmentIntegration.dialogs.IntegrationOfContentFragmentsDialog;
import moskitt4me.repositoryClient.fragmentIntegration.util.ContentItem;
import moskitt4me.repositoryClient.fragmentIntegration.util.IntegrationData;
import moskitt4me.repositoryClient.fragmentIntegration.wizard.IntegrationOfProcessFragmentsWizard;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.epf.library.LibraryService;
import org.eclipse.epf.library.LibraryServiceException;
import org.eclipse.epf.library.edit.command.IActionManager;
import org.eclipse.epf.library.edit.process.command.ActivityDropCommand;
import org.eclipse.epf.library.edit.ui.IActionTypeProvider;
import org.eclipse.epf.library.edit.util.ExposedAdapterFactory;
import org.eclipse.epf.library.services.LibraryModificationHelper;
import org.eclipse.epf.uma.Activity;
import org.eclipse.epf.uma.Artifact;
import org.eclipse.epf.uma.ArtifactDescription;
import org.eclipse.epf.uma.BreakdownElement;
import org.eclipse.epf.uma.CapabilityPattern;
import org.eclipse.epf.uma.MethodConfiguration;
import org.eclipse.epf.uma.MethodElementProperty;
import org.eclipse.epf.uma.MethodLibrary;
import org.eclipse.epf.uma.MethodPackage;
import org.eclipse.epf.uma.ProcessComponent;
import org.eclipse.epf.uma.ProcessElement;
import org.eclipse.epf.uma.ProcessPackage;
import org.eclipse.epf.uma.Role;
import org.eclipse.epf.uma.RoleDescription;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.TaskDescription;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.UmaFactory;
import org.eclipse.epf.uma.UmaPackage;
import org.eclipse.epf.uma.WorkBreakdownElement;
import org.eclipse.epf.uma.WorkOrder;
import org.eclipse.epf.uma.WorkProduct;
import org.eclipse.epf.uma.impl.ActivityImpl;
import org.eclipse.epf.uma.impl.ArtifactImpl;
import org.eclipse.epf.uma.impl.RoleImpl;
import org.eclipse.epf.uma.impl.TaskDescriptorImpl;
import org.eclipse.epf.uma.impl.TaskImpl;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

/**
 * This method is in charge to read the XMI file of a conceptual fragment,
 * create the related conceptual elements, and add those to the library
 * selected.
 * 
 * element - library selected
 */
public class FragmentIntegration {

	static ArrayList<ContentItem> items;
	static ArrayList<EObject> itemsToSave;
	static ArrayList<EObject> savedElements;
	// static ArrayList<CapabilityPattern> patternsToSave;
	static Object contentIntegrationData;
	static IntegrationData processIntegrationData;
	static LibraryModificationHelper helper;

	static MethodConfiguration config;
	static String typeOfProcessIntegration;
	private static CapabilityPattern pattern;

	public static int integrateConceptualFragmentOfTypeContent(
			String fragmentFolder, String fragmentName, String type) {

		contentIntegrationData = getIntegrationDataForContentFragment(type);
		if (contentIntegrationData == "cancel")
			return 1;

		// XMI Conceptual fragments reader
		FragmentReader cfr = new FragmentReader(fragmentFolder, fragmentName);
		// Read the XMI and get the Conceptual Items
		items = cfr.getItems();

		helper = new LibraryModificationHelper();

		// An auxiliar list to save the Conceptual Elements to save at the
		// library
		itemsToSave = new ArrayList<EObject>();
		

		int res = 0;

		// For each item it create the related content element
		for (ContentItem item : items) {

			/** TASK */
			if (item.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:Task")) {
				getTask(item);

			} else

			/** ROLE */
			if (item.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:Role")) {
				getRole(item);
			} else

			/** WORK PRODUCT - ARTIFACT */
			if (item.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:Artifact")) {
				getArtifact(item);
			}
		}

		if (!itemsToSave.isEmpty())
			saveElementsForContentFragment();

		return res;

	}

	/**
	 * The integration data for this kind of fragment is only the library where
	 * the elements will be saved
	 */
	private static Object getIntegrationDataForContentFragment(String type) {

		MethodLibrary library = LibraryService.getInstance()
				.getCurrentMethodLibrary();

		if (library != null) {

			// Dialog which shows the libraries on the project
			IntegrationOfContentFragmentsDialog contentElementSelectionDialog = new IntegrationOfContentFragmentsDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell(), library.getMethodPlugins().toArray(),
					type);
			if (contentElementSelectionDialog.open() == Window.OK) {

				Object[] selection = contentElementSelectionDialog.getResult();

				if (selection != null && selection.length > 0) {
					Object sel = selection[0];
					return sel;
				}
			} else {
				return "cancel";
			}
		}

		return null;
	}

	public static int integrateConceptualFragmentOfTypeProcess(
			String fragmentFolder, String fragmentName) {
		savedElements = new ArrayList<EObject>();
		itemsToSave = new ArrayList<EObject>();
		// XMI Conceptual fragments reader
		FragmentReader cfr = new FragmentReader(fragmentFolder, fragmentName);

		// Read the XMI and get the Conceptual Items
		items = cfr.getItems();

		// Get the integration data, if it's null, the integration is canceled
		processIntegrationData = getIntegrationDataForProcessFragment();
		if (processIntegrationData.getElement() == null
				&& processIntegrationData.getFolder() == null)
			return 1;

		helper = new LibraryModificationHelper();

		// Set the configuration
		config = LibraryService.getInstance().getCurrentMethodConfiguration();
		if (config == null) {
			Object obj = processIntegrationData.getElement();
			while (config == null) {
				if (obj instanceof ProcessComponent)
					config = ((ProcessComponent) obj).getProcess()
							.getDefaultContext();
				else
					obj = ((MethodPackage) obj).getContainer();
			}
		}
		int res = 1;
		// For each item it create the related process element
		for (ContentItem item : items) {
			/** PATTERN */
			if (item.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:CapabilityPattern")) {
				if (processIntegrationData.getType().equals("Copy")) {
					res = createCapabilityPattern(item);
					if (!itemsToSave.isEmpty())
						saveElementsForProcessFragment();
					itemsToSave = new ArrayList<EObject>();
					copyActivityAction.run();
				} else {
					res = createCapabilityPattern(item);
					if (!itemsToSave.isEmpty())
						saveElementsForProcessFragment();
					itemsToSave = new ArrayList<EObject>();
					extendActivityAction.run();
				}
			}
			/** TASK */
			else if (item.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:Task")) {
				getTask(item);

			} else

			/** ROLE */
			if (item.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:Role")) {
				getRole(item);
			} else

			/** WORK PRODUCT - ARTIFACT */
			if (item.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:Artifact")) {
				getArtifact(item);
			}
		}

		
		return res;
	}

	/**
	 * The integration data for this kind of fragment is the process element,
	 * the folder where the elements will be saved, the type of integration
	 * (Copy or extends) and the already existing patterns
	 */
	private static IntegrationData getIntegrationDataForProcessFragment() {
		IntegrationOfProcessFragmentsWizard wizard = new IntegrationOfProcessFragmentsWizard(
				items);
		wizard.init(PlatformUI.getWorkbench(), null);

		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), wizard);
		dialog.create();
		dialog.open();
		return wizard.data;
	}

	/*** PATTERNS **********/

	protected static IAction extendActivityAction = new Action() {

		public void run() {

			org.eclipse.epf.library.edit.process.consolidated.ItemProviderAdapterFactory auxAdapterFactory = new org.eclipse.epf.library.edit.process.consolidated.ItemProviderAdapterFactory();
			AdapterFactory[] adapterFactories = new AdapterFactory[] {
					auxAdapterFactory,
					new ReflectiveItemProviderAdapterFactory() };
			AdapterFactory adapterFactory = new ExposedAdapterFactory(
					adapterFactories);

			List l = new ArrayList();
			l.add(pattern);
			ActivityDropCommand cmd = null;
			if (((ProcessPackage) processIntegrationData.getElement())
					.getProcessElements() != null
					&& !((ProcessPackage) processIntegrationData.getElement())
							.getProcessElements().isEmpty()
					&& ((ProcessPackage) processIntegrationData.getElement())
							.getProcessElements().get(0) instanceof Activity) {

				cmd = new ActivityDropCommand(
						(Activity) ((ProcessPackage) processIntegrationData
								.getElement()).getProcessElements().get(0), l,
						null, adapterFactory);
			} else if (processIntegrationData.getElement() instanceof ProcessComponent) {
				cmd = new ActivityDropCommand(
						((ProcessComponent) processIntegrationData.getElement())
								.getProcess(), l, null, adapterFactory);
			}
			cmd.setType(IActionTypeProvider.EXTEND);
			helper.getActionManager().execute(cmd);
			try {
				LibraryService.getInstance().saveCurrentMethodLibrary();
			} catch (LibraryServiceException e) {
				e.printStackTrace();
			}
		}
	};

	protected static IAction copyActivityAction = new Action() {

		public void run() {

			org.eclipse.epf.library.edit.process.consolidated.ItemProviderAdapterFactory auxAdapterFactory = new org.eclipse.epf.library.edit.process.consolidated.ItemProviderAdapterFactory();
			AdapterFactory[] adapterFactories = new AdapterFactory[] {
					auxAdapterFactory,
					new ReflectiveItemProviderAdapterFactory() };
			AdapterFactory adapterFactory = new ExposedAdapterFactory(
					adapterFactories);

			List l = new ArrayList();
			l.add(pattern);
			ActivityDropCommand cmd = null;
			if (((ProcessPackage) processIntegrationData.getElement())
					.getProcessElements() != null
					&& !((ProcessPackage) processIntegrationData.getElement())
							.getProcessElements().isEmpty()
					&& ((ProcessPackage) processIntegrationData.getElement())
							.getProcessElements().get(0) instanceof Activity) {

				cmd = new ActivityDropCommand(
						(Activity) ((ProcessPackage) processIntegrationData
								.getElement()).getProcessElements().get(0), l,
						null, adapterFactory);
			} else if (processIntegrationData.getElement() instanceof ProcessComponent) {
				cmd = new ActivityDropCommand(
						((ProcessComponent) processIntegrationData.getElement())
								.getProcess(), l, null, adapterFactory);
			}
			cmd.setType(IActionTypeProvider.COPY);
			helper.getActionManager().execute(cmd);
			try {
				LibraryService.getInstance().saveCurrentMethodLibrary();
			} catch (LibraryServiceException e) {
				e.printStackTrace();
			}
		}
	};

	public static void resolve(Object o) {
		if (o.toString().contains("eProxy"))
			EcoreUtil.resolveAll((EObject) o);
	}

	public static int createCapabilityPattern(ContentItem item) {

		org.eclipse.epf.uma.ProcessComponent pc = UmaFactory.eINSTANCE
				.createProcessComponent();
		pc.setGuid(item.getAttributes().get("guid"));
		CapabilityPattern cp = null;
		ArrayList<CapabilityPattern> listcp = processIntegrationData
				.getExistingPattern();
		for (CapabilityPattern pattern : listcp) {
			if (pattern != null
					&& pattern.getName().equals(
							item.getAttributes().get("name")))
				cp = pattern;
		}
		if (cp == null) {
			cp = UmaFactory.eINSTANCE.createCapabilityPattern();
			if (item.getAttributes().containsKey("name")) {
				cp.setName(item.getAttributes().get("name"));
				pc.setName(item.getAttributes().get("name"));
			}
			if (item.getAttributes().containsKey("presentationName")
					&& !(item.getAttributes().get("presentationName")
							.equals(""))) {
				cp.setPresentationName(item.getAttributes().get(
						"presentationName"));
				pc.setPresentationName(item.getAttributes().get(
						"presentationName"));
			} else {
				cp.setPresentationName(item.getAttributes().get("name"));
				pc.setPresentationName(item.getAttributes().get("name"));
			}
			if (item.getAttributes().containsKey("briefDescription")) {
				cp.setBriefDescription(item.getAttributes().get(
						"briefDescription"));
				pc.setBriefDescription(item.getAttributes().get(
						"briefDescription"));
			}
			helper.getActionManager().doAction(IActionManager.SET,
					(EObject) pc,
					UmaPackage.eINSTANCE.getProcessComponent_Process(), cp, -1);
			helper.getActionManager().doAction(IActionManager.ADD,
					(EObject) processIntegrationData.getFolder(),
					UmaPackage.eINSTANCE.getMethodPackage_ChildPackages(), pc,
					-1);
			helper.save();

			
		
			
			/** Create sub elements */
			boolean somethingAdded = false;
			for (ContentItem ci : item.getSubElements()) {
				if (ci.getAttributes().get("xsi:type").equals(
						"org.eclipse.epf.uma:Activity")) {
					Activity ac = createActivity(ci, pc, cp);
					savedElements.add(ac);
					helper.getActionManager().doAction(
							IActionManager.ADD,
							(EObject) cp,
							UmaPackage.eINSTANCE
									.getActivity_BreakdownElements(), ac, -1);
					somethingAdded = true;
				}
				if (ci.getAttributes().get("xsi:type").equals(
						"org.eclipse.epf.uma:TaskDescriptor")) {

					TaskDescriptor td = createTaskDescriptor(ci, pc, cp);
					savedElements.add(td);
					
				}
			}
			if (somethingAdded == true)
				helper.save();
		} else
			cp.setDefaultContext(config);

		pattern = cp;
		for(ContentItem i : items)
			setPredecessors(i);
		

		return 0;
	}

	

	private static void setPredecessors(ContentItem item) {
		// TODO Auto-generated method stub
		
			
			
			if(item.getAttributes().get("name").equals("new_task")){
				int i =0;}
				BreakdownElement be = getElementById(item.getAttributes().get("guid"),pattern.getBreakdownElements());
				if (be!=null && item.getAttributes().containsKey("predecessors")
						&& item.getAttributes().get("predecessors") != ""){
						String[] aux = item.getAttributes().get("predecessors").split(" ");
						for (String id : aux){
							WorkOrder worder = UmaFactory.eINSTANCE.createWorkOrder();
							worder.setPred((WorkBreakdownElement) getElementById(id, pattern.getBreakdownElements()));

						
								helper.getActionManager().doAction(IActionManager.ADD,
									(EObject) be.getSuperActivities().getContainer(),
									UmaPackage.eINSTANCE.getProcessPackage_ProcessElements(),worder, -1);
								helper.save();
								helper.getActionManager().doAction(IActionManager.ADD,
										(EObject) be,
										UmaPackage.eINSTANCE.getWorkBreakdownElement_LinkToPredecessor(),worder, -1);
							helper.save();
								}
						}
			
			
				for(ContentItem i : item.getSubElements())
					if(!(i.getAttributes().get("guid").equals(item.getAttributes().get("guid")))
							&& !(i.getAttributes().get("xsi:type").equals("org.eclipse.epf.uma:Task")))
						setPredecessors(i);
				
			
			
	}

	private static BreakdownElement getElementById(String id, List<BreakdownElement> elems) {
	
		for(BreakdownElement p : elems){
			if(p.getGuid().equals(id)) return (WorkBreakdownElement) p;
			else{
			if(p instanceof Activity){
				BreakdownElement aux = getElementById(id, ((Activity) p).getBreakdownElements());
				if(aux!=null) return aux;
			}
			}
		}
		
		return null;
	}

	private static Activity createActivity(ContentItem item, MethodPackage super_p,
			Activity super_a) {

		MethodPackage p = UmaFactory.eINSTANCE.createProcessPackage();
		Activity a = UmaFactory.eINSTANCE.createActivity();

		if (item.getAttributes().containsKey("name")) {
			p.setName(item.getAttributes().get("name"));
			a.setName(item.getAttributes().get("name"));
		}
		if (item.getAttributes().containsKey("presentationName")) {
			p.setPresentationName(item.getAttributes().get("presentationName"));
			a.setPresentationName(item.getAttributes().get("presentationName"));
		}
		if (item.getAttributes().containsKey("guid")) {
			String guid = item.getAttributes().get("guid");
			p.setGuid(guid);
			a.setGuid(guid);
		}
		if (item.getAttributes().containsKey("briefDescription")) {
			p.setBriefDescription(item.getAttributes().get("briefDescription"));
			a.setBriefDescription(item.getAttributes().get("briefDescription"));
		}
	
		a.setSuperActivities(super_a);
		helper.getActionManager().doAction(IActionManager.ADD,
				(EObject) super_a,
				UmaPackage.eINSTANCE.getActivity_BreakdownElements(), a, -1);
		helper.getActionManager().doAction(IActionManager.ADD,
				(EObject) super_p,
				UmaPackage.eINSTANCE.getMethodPackage_ChildPackages(), p, -1);

		for (ContentItem ci : item.getSubElements()) {
			if (ci.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:Activity")) {
				Activity act = createActivity(ci, p, a);
				savedElements.add(act);
			}
			if (ci.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:TaskDescriptor")) {
				TaskDescriptor td = createTaskDescriptor(ci, p, a);
				savedElements.add(td);
			}
		}
		return a;
	}

	
	

	private static TaskDescriptor createTaskDescriptor(ContentItem item,
			MethodPackage p, Activity super_a) {

		TaskDescriptor t = UmaFactory.eINSTANCE.createTaskDescriptor();

		if (item.getAttributes().containsKey("name"))
			t.setName(item.getAttributes().get("name"));
		if (item.getAttributes().containsKey("presentationName"))
			t.setPresentationName(item.getAttributes().get("presentationName"));
		if (item.getAttributes().containsKey("guid")) {
			String guid = item.getAttributes().get("guid");
			t.setGuid(guid);
		}
		if (item.getAttributes().containsKey("briefDescription"))
			t.setBriefDescription(item.getAttributes().get("briefDescription"));
		
		
		for (ContentItem ci : item.getSubElements()) {
			if (ci.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:Task")) {
				Task task = taskDescriptorGetTask(ci);
				savedElements.add(task);
				t.setTask(task);
			}
		}
		
		
		t.setSuperActivities(super_a);
		helper.getActionManager().doAction(IActionManager.ADD,
				(EObject) super_a,
				UmaPackage.eINSTANCE.getActivity_BreakdownElements(), t, -1);
		savedElements.add(t);
		helper.save();

		return t;
	}

	/** TASKS **************/

	/**
	 * Method to create a Task item- instance of ContentItem which contains the
	 * element information obtained from the xmi
	 */
	private static Task taskDescriptorGetTask(ContentItem ci) {
		String id = ci.getAttributes().get("guid");
		for (EObject o : savedElements) {
			if (o instanceof TaskImpl && ((TaskImpl) o).getGuid().equals(id)) {
				return (Task) o;
			}
		}
		return getTask(ci);
	}
	private static Task getTask(ContentItem item) {

		String name = "", presentationName = "", guid = "", briefDescription = "";
		if (item.getAttributes().containsKey("name"))
			name = item.getAttributes().get("name");
		if (item.getAttributes().containsKey("presentationName"))
			presentationName = item.getAttributes().get("presentationName");
		if (item.getAttributes().containsKey("guid"))
			guid = item.getAttributes().get("guid");
		if (item.getAttributes().containsKey("briefDescription"))
			briefDescription = item.getAttributes().get("briefDescription");

		// Load the attributes associated with references
		ArrayList<Role> r = new ArrayList<Role>(), ar = new ArrayList<Role>();

		if (item.getAttributes().containsKey("performedBy")
				&& item.getAttributes().get("performedBy")!=null
				&& item.getAttributes().get("performedBy")!="") {
			String[] aux = item.getAttributes().get("performedBy").split(" ");
			for (String rol : aux)
				if(!item.getSubElements().isEmpty())
					r.add(getRole(rol, item.getSubElements() ));
				else r.add(getRole(rol));
		}
		if (item.getAttributes().containsKey("additionallyPerformedBy")&&
				item.getAttributes().get("additionallyPerformedBy")!=null
				&& item.getAttributes().get("additionallyPerformedBy")!="") {
			String[] aux = item.getAttributes().get("additionallyPerformedBy")
					.split(" ");
			for (String rol : aux)
				if(!item.getSubElements().isEmpty())
					ar.add(getRole(rol,  item.getSubElements() ));
				else ar.add(getRole(rol));
		}

		ArrayList<Artifact> mi = new ArrayList<Artifact>(), o = new ArrayList<Artifact>(), oi = new ArrayList<Artifact>();

		if (item.getAttributes().containsKey("mandatoryInput")&&
				item.getAttributes().get("mandatoryInput")!=null
				&&
				item.getAttributes().get("mandatoryInput")!="") {
			String[] aux = item.getAttributes().get("mandatoryInput")
					.split(" ");
			for (String artifact : aux)
				if(!item.getSubElements().isEmpty())
					mi.add(getArtifact(artifact,  item.getSubElements() ));
				else mi.add(getArtifact(artifact));
		}
		if (item.getAttributes().containsKey("output")&&
				item.getAttributes().get("output")!=null
				&& item.getAttributes().get("output")!="") {
			String[] aux = item.getAttributes().get("output").split(" ");
			for (String artifact : aux)
				if(!item.getSubElements().isEmpty())
					o.add(getArtifact(artifact,  item.getSubElements() ));
				else o.add(getArtifact(artifact));
			
		}
		if (item.getAttributes().containsKey("optionalInput")&&
				item.getAttributes().get("optionalInput")!=null
				&& item.getAttributes().get("optionalInput")!="") {
			String[] aux = item.getAttributes().get("optionalInput").split(" ");
			for (String artifact : aux)
				if(!item.getSubElements().isEmpty())
					oi.add(getArtifact(artifact,  item.getSubElements() ));
				else oi.add(getArtifact(artifact));
		}

		// load the contentDescription elements (Presentation)
		String mainDescription = "", keyConsiderations = "", purpose = "", alternatives = "";
		if (item.getContentDescription().containsKey("mainDescription"))
			mainDescription = item.getContentDescription().get(
					"mainDescription");
		if (item.getContentDescription().containsKey("keyConsiderations"))
			keyConsiderations = item.getContentDescription().get(
					"keyConsiderations");
		if (item.getContentDescription().containsKey("purpose"))
			purpose = item.getContentDescription().get("purpose");
		if (item.getContentDescription().containsKey("alternatives"))
			alternatives = item.getContentDescription().get("alternatives");

		// create task
		return createTask(name, presentationName, guid, briefDescription, r,
				ar, mi, o, oi, mainDescription, keyConsiderations, purpose,
				alternatives);
	}

	

	private static Task createTask(String name, String presentationName,
			String guid, String briefDescription, ArrayList<Role> r,
			ArrayList<Role> ar, ArrayList<Artifact> mi, ArrayList<Artifact> o,
			ArrayList<Artifact> oi, String mainDescription,
			String keyConsiderations, String purpose, String alternatives) {

		Task t = UmaFactory.eINSTANCE.createTask();
		TaskDescription td = UmaFactory.eINSTANCE.createTaskDescription();

		// set the attributes
		if (!name.equals("")) {
			t.setName(name);
			td.setName(name);
		}
		if (!presentationName.equals("")) {
			t.setPresentationName(presentationName);
			td.setPresentationName(presentationName);
		}
		if (!guid.equals("")) {
			t.setGuid(guid);
			td.setGuid(EcoreUtil.generateUUID());
		}
		if (!briefDescription.equals("")) {
			t.setBriefDescription(briefDescription);
			td.setBriefDescription(briefDescription);
		}

		if (!r.isEmpty())
			t.getPerformedBy().addAll(r);
		if (!ar.isEmpty())
			t.getAdditionallyPerformedBy().addAll(ar);
		if (!mi.isEmpty())
			t.getMandatoryInput().addAll(mi);
		if (!o.isEmpty())
			t.getOutput().addAll(o);
		if (!oi.isEmpty())
			t.getOptionalInput().addAll(oi);

		// set the ContentDescription (Presentation)

		if (!mainDescription.equals(""))
			td.setMainDescription(mainDescription);
		if (!keyConsiderations.equals(""))
			td.setKeyConsiderations(keyConsiderations);
		if (!purpose.equals(""))
			td.setPurpose(purpose);
		if (!alternatives.equals(""))
			td.setAlternatives(alternatives);
		t.setPresentation(td);

		itemsToSave.add(t);
		return t;
	}

	/** ROLES ************/

	/**
	 * If the role is already added to "elementsToSave", it returns it; else
	 * call getRole to create a new one
	 */

	private static Role getRole(String id, ArrayList<ContentItem> subElements) {
		for (EObject o : itemsToSave) {
			if (o instanceof RoleImpl && ((RoleImpl) o).getGuid().equals(id)) {
				return (Role) o;
			}
		}
		for (ContentItem ci : subElements) {
			if (ci.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:Role")
					&& ci.getAttributes().get("guid").equals(id)) {
				return getRole(ci);
			}
		}
		return null;
	}

	private static Role getRole(String guid) {
		for (EObject o : itemsToSave) {
			if (o instanceof RoleImpl && ((RoleImpl) o).getGuid().equals(guid)) {
				return (Role) o;
			}
		}
		for (ContentItem ci : items) {
			if (ci.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:Role")
					&& ci.getAttributes().get("guid").equals(guid)) {
				return getRole(ci);
			}
		}
		return null;
	}

	/**
	 * Method to create a Role item - instance of ContentItem which contains the
	 * element information obtained from the xmi
	 */
	private static Role getRole(ContentItem item) {

		String guid = "";
		if (item.getAttributes().containsKey("guid"))
			guid = item.getAttributes().get("guid");

		for (EObject o : itemsToSave) {
			if (o instanceof RoleImpl && ((RoleImpl) o).getGuid().equals(guid)) {
				return (Role) o;
			}
		}
		// Load the attributes
		String name = "", presentationName = "", briefDescription = "";
		if (item.getAttributes().containsKey("name"))
			name = item.getAttributes().get("name");
		if (item.getAttributes().containsKey("presentationName"))
			presentationName = item.getAttributes().get("presentationName");
		if (item.getAttributes().containsKey("briefDescription"))
			briefDescription = item.getAttributes().get("briefDescription");

		// Load the attributes associated with references
		ArrayList<Artifact> rf = new ArrayList<Artifact>();
		if (item.getAttributes().containsKey("responsibleFor")&&
				item.getAttributes().get("responsibleFor")!=null
				&& item.getAttributes().get("responsibleFor")!="") {
			String[] aux = item.getAttributes().get("responsibleFor")
					.split(" ");
			for (String artifact : aux)
					if(!item.getSubElements().isEmpty())
						rf.add(getArtifact(artifact,  item.getSubElements() ));
					else rf.add(getArtifact(artifact));
		}
		// load the contentDescription elements (Presentation)
		String mainDescription = "", keyConsiderations = "", skills = "", assignmentApproaches = "", synonyms = "";
		if (item.getContentDescription().containsKey("mainDescription"))
			mainDescription = item.getContentDescription().get(
					"mainDescription");
		if (item.getContentDescription().containsKey("keyConsiderations"))
			keyConsiderations = item.getContentDescription().get(
					"keyConsiderations");
		if (item.getContentDescription().containsKey("skills"))
			skills = item.getContentDescription().get("skills");
		if (item.getContentDescription().containsKey("assignmentApproaches"))
			keyConsiderations = item.getContentDescription().get(
					"assignmentApproaches");
		if (item.getContentDescription().containsKey("synonyms"))
			synonyms = item.getContentDescription().get("synonyms");

		return createRol(name, presentationName, guid, briefDescription, rf,
				mainDescription, keyConsiderations, skills,
				assignmentApproaches, synonyms);
	}

	private static Role createRol(String name, String presentationName,
			String guid, String briefDescription, ArrayList<Artifact> rf,
			String mainDescription, String keyConsiderations, String skills,
			String assignmentApproaches, String synonyms) {

		Role r = UmaFactory.eINSTANCE.createRole();
		RoleDescription rd = UmaFactory.eINSTANCE.createRoleDescription();

		// set the attributes
		if (!name.equals("")) {
			r.setName(name);
			rd.setName(name);
		}
		if (!presentationName.equals("")) {
			r.setPresentationName(presentationName);
			rd.setPresentationName(presentationName);
		}
		if (!guid.equals("")) {
			r.setGuid(guid);
			rd.setGuid(EcoreUtil.generateUUID());
		}
		if (!briefDescription.equals("")) {
			r.setBriefDescription(briefDescription);
			rd.setBriefDescription(briefDescription);
		}

		// set the attributes associated with references
		if (!rf.isEmpty()) {
			r.getResponsibleFor().addAll(rf);

		}
		// set the ContentDescription (Presentation)
		if (!mainDescription.equals(""))
			rd.setMainDescription(mainDescription);
		if (!keyConsiderations.equals(""))
			rd.setKeyConsiderations(keyConsiderations);
		if (!skills.equals(""))
			rd.setSkills(skills);
		if (!assignmentApproaches.equals(""))
			rd.setAssignmentApproaches(assignmentApproaches);
		if (!synonyms.equals(""))
			rd.setSynonyms(synonyms);

		r.setPresentation(rd);

		itemsToSave.add(r);
		return r;
	}

	/** ARTIFACTS *************/

	/**
	 * If the artifact is already added to save, it returns it; else call
	 * getArtifact to create a new one
	 */
	private static Artifact getArtifact(String id,
			ArrayList<ContentItem> subElements) {
		for (EObject o : itemsToSave) {
			if (o instanceof ArtifactImpl
					&& ((ArtifactImpl) o).getGuid().equals(id)) {
				return (Artifact) o;
			}
		}
		for (ContentItem ci : subElements) {
			if (ci.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:Artifact")
					&& ci.getAttributes().get("guid").equals(id)) {
				return getArtifact(ci);
			}
		}
		return null;
	}
	private static Artifact getArtifact(String id) {
		// it's already saved on itemsToSave
		for (EObject o : itemsToSave) {
			if (o instanceof ArtifactImpl
					&& ((ArtifactImpl) o).getGuid().equals(id)) {
				return (Artifact) o;
			}
		}
		for (ContentItem ci : items) {
			if (ci.getAttributes().get("xsi:type").equals(
					"org.eclipse.epf.uma:Artifact")
					&& ci.getAttributes().get("guid").equals(id)) {
				return getArtifact(ci);
			}
		}
		return null;
	}

	/**
	 * Method to create an Artifact item - instance of ContentItem which
	 * contains the element information obtained from the xmi
	 */
	private static Artifact getArtifact(ContentItem item) {
		String guid = "";
		if (item.getAttributes().containsKey("guid"))
			guid = item.getAttributes().get("guid");

		for (EObject o : itemsToSave) {
			if (o instanceof ArtifactImpl
					&& ((ArtifactImpl) o).getGuid().equals(guid)) {
				return (Artifact) o;
			}
		}
		// Load the attributes
		String name = "", presentationName = "", briefDescription = "";
		if (item.getAttributes().containsKey("name"))
			name = item.getAttributes().get("name");
		if (item.getAttributes().containsKey("presentationName"))
			presentationName = item.getAttributes().get("presentationName");
		if (item.getAttributes().containsKey("briefDescription"))
			briefDescription = item.getAttributes().get("briefDescription");

		// load the contentDescription elements (Presentation)
		String mainDescription = "", keyConsiderations = "", purpose = "", impactOfNotHaving = "", reasonsForNotNeeding = "", briefOutline = "", representationOptions = "", representation = "", notation = "", sourcePath = "", targetPath = "";
		if (item.getContentDescription().containsKey("mainDescription"))
			mainDescription = item.getContentDescription().get(
					"mainDescription");
		if (item.getContentDescription().containsKey("keyConsiderations"))
			keyConsiderations = item.getContentDescription().get(
					"keyConsiderations");
		if (item.getContentDescription().containsKey("purpose"))
			purpose = item.getContentDescription().get("purpose");
		if (item.getContentDescription().containsKey("impactOfNotHaving"))
			impactOfNotHaving = item.getContentDescription().get(
					"impactOfNotHaving");
		if (item.getContentDescription().containsKey("reasonsForNotNeeding"))
			reasonsForNotNeeding = item.getContentDescription().get(
					"reasonsForNotNeeding");
		if (item.getContentDescription().containsKey("briefOutline"))
			briefOutline = item.getContentDescription().get("briefOutline");
		if (item.getContentDescription().containsKey("representationOptions"))
			representationOptions = item.getContentDescription().get(
					"representationOptions");
		if (item.getContentDescription().containsKey("representation"))
			representation = item.getContentDescription().get("representation");
		if (item.getContentDescription().containsKey("notation"))
			notation = item.getContentDescription().get("notation");
		if (item.getContentDescription().containsKey("sourcePath"))
			sourcePath = item.getContentDescription().get("sourcePath");
		if (item.getContentDescription().containsKey("targetPath"))
			targetPath = item.getContentDescription().get("targetPath");
		return createArtifact(name, presentationName, guid, briefDescription,
				mainDescription, keyConsiderations, purpose, impactOfNotHaving,
				reasonsForNotNeeding, briefOutline, representationOptions,
				representation, notation, sourcePath, targetPath);
	}

	private static Artifact createArtifact(String name,
			String presentationName, String guid, String briefDescription,
			String mainDescription, String keyConsiderations, String purpose,
			String impactOfNotHaving, String reasonsForNotNeeding,
			String briefOutline, String representationOptions,
			String representation, String notation, String sourcePath,
			String targetPath) {
		Artifact a = UmaFactory.eINSTANCE.createArtifact();
		ArtifactDescription ad = UmaFactory.eINSTANCE
				.createArtifactDescription();

		// set the attributes
		if (!name.equals("")) {
			a.setName(name);
			ad.setName(name);
		}
		if (!presentationName.equals("")) {
			a.setPresentationName(presentationName);
			ad.setPresentationName(presentationName);
		}
		if (!guid.equals("")) {
			a.setGuid(guid);
			ad.setGuid(EcoreUtil.generateUUID());
		}
		if (!briefDescription.equals("")) {
			a.setBriefDescription(briefDescription);
			ad.setBriefDescription(briefDescription);
		}
		// set the ContentDescription (Presentation)
		if (!mainDescription.equals(""))
			ad.setMainDescription(mainDescription);
		if (!keyConsiderations.equals(""))
			ad.setKeyConsiderations(keyConsiderations);
		if (!purpose.equals(""))
			ad.setPurpose(purpose);
		if (!impactOfNotHaving.equals(""))
			ad.setImpactOfNotHaving(impactOfNotHaving);
		if (!reasonsForNotNeeding.equals(""))
			ad.setReasonsForNotNeeding(reasonsForNotNeeding);
		if (!briefOutline.equals(""))
			ad.setBriefOutline(briefOutline);
		if (!representationOptions.equals(""))
			ad.setRepresentationOptions(representationOptions);
		if (!representation.equals(""))
			ad.setRepresentation(representation);
		if (!notation.equals(""))
			ad.setNotation(notation);
		if (!sourcePath.equals("")) {
			MethodElementProperty mep = UmaFactory.eINSTANCE
					.createMethodElementProperty();
			mep.setName("sourcePath");
			mep.setValue(sourcePath);
			a.getMethodElementProperty().add(mep);
		}
		if (!targetPath.equals("")) {
			MethodElementProperty mep = UmaFactory.eINSTANCE
					.createMethodElementProperty();
			mep.setName("targetPath");
			mep.setValue(targetPath);
			a.getMethodElementProperty().add(mep);
		}
		a.setPresentation(ad);

		itemsToSave.add(a);
		return a;
	}

	/** Save the content elements in itemsToSave */
	private static void saveElementsForContentFragment() {
		for (EObject o : itemsToSave) {
			helper.getActionManager().doAction(IActionManager.ADD,
					(EObject) contentIntegrationData,
					UmaPackage.eINSTANCE.getContentPackage_ContentElements(),
					o, -1);
		}
		helper.save();

	}
	private static void saveElementsForProcessFragment() {
		for (EObject o : itemsToSave) {
			helper.getActionManager().doAction(IActionManager.ADD,
					(EObject) processIntegrationData.getContentFolder(),
					UmaPackage.eINSTANCE.getContentPackage_ContentElements(),
					o, -1);
		}
		helper.save();

	}

}
