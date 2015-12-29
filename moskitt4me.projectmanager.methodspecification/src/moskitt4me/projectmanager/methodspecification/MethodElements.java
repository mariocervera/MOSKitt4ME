package moskitt4me.projectmanager.methodspecification;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.bpmn2.Property;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epf.library.LibraryService;
import org.eclipse.epf.library.util.LibraryUtil;
import org.eclipse.epf.resourcemanager.ResourceDescriptor;
import org.eclipse.epf.resourcemanager.ResourceManager;
import org.eclipse.epf.uma.CapabilityPattern;
import org.eclipse.epf.uma.ContentDescription;
import org.eclipse.epf.uma.DeliveryProcess;
import org.eclipse.epf.uma.Domain;
import org.eclipse.epf.uma.Guidance;
import org.eclipse.epf.uma.Guideline;
import org.eclipse.epf.uma.MethodElement;
import org.eclipse.epf.uma.MethodLibrary;
import org.eclipse.epf.uma.MethodPlugin;
import org.eclipse.epf.uma.ProcessComponent;
import org.eclipse.epf.uma.Role;
import org.eclipse.epf.uma.RoleDescriptor;
import org.eclipse.epf.uma.RoleSet;
import org.eclipse.epf.uma.RoleSetGrouping;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.ToolMentor;
import org.eclipse.epf.uma.VariabilityElement;
import org.eclipse.epf.uma.VariabilityType;
import org.eclipse.epf.uma.WorkProduct;
import org.osgi.framework.Bundle;

public class MethodElements {
	
	public static DeliveryProcess loadedDeliveryProcess;
	
	private static List<MethodPlugin> methodPlugins = new ArrayList<MethodPlugin>();
	
	// -------- plugin.xmi --------
	
	public static List<Task> tasks = new ArrayList<Task>();
	
	public static List<Domain> domains = new ArrayList<Domain>();
	public static List<WorkProduct> workProducts = new ArrayList<WorkProduct>();
	
	public static List<RoleSetGrouping> roleSetGroupings = new ArrayList<RoleSetGrouping>();
	public static List<RoleSet> roleSets = new ArrayList<RoleSet>();
	public static List<Role> roles = new ArrayList<Role>();
	
	public static List<Guideline> guidelines = new ArrayList<Guideline>();
	public static List<ToolMentor> toolMentors = new ArrayList<ToolMentor>();
	
	// -------- model.xmi --------
	
	public static List<TaskDescriptor> taskDescriptors = new ArrayList<TaskDescriptor>();
	public static List<RoleDescriptor> roleDescriptors = new ArrayList<RoleDescriptor>();
	
	public static Map<Task, List<TaskDescriptor>> taskDescriptorMap = new HashMap<Task, List<TaskDescriptor>>(); 
	public static Map<Role, List<RoleDescriptor>> roleDescriptorMap = new HashMap<Role, List<RoleDescriptor>>();
	
	public static String processKey = "helloworld";
	
	// --------output.activiti --------
	
	public static List<UserTask> userTasks = new ArrayList<UserTask>();
	
	// Others
	
	private static Map<String, URI> mapCpGuidToURI = new HashMap<String,URI>();
	
	public static void loadMethodDefinition(DeliveryProcess dp) {
		
		if(dp == null) {
			clearLists();
			loadedDeliveryProcess = null;	
		}
		else {
			if(loadedDeliveryProcess == null || !loadedDeliveryProcess.equals(dp)) {
				
				loadedDeliveryProcess = dp;
				
				clearLists();
					
				// -------- plugin.xmi --------
				
				MethodPlugin methodPlugin = getMethodPlugin(dp);
				
				methodPlugins.add(methodPlugin);
				
				initializeContentElements(methodPlugin);
				
				// -------- model.xmi --------
				
				ProcessComponent processComponent = getProcessComponent(dp);
				
				initializeTaskDescriptors(processComponent);
				initializeRoleDescriptors(processComponent);
				
				processKey = dp.getGuid();
				
				// --------output.activiti --------
				
				initializeUserTasks(dp);
				
				// Method Library
				
				loadCurrentMethodLibrary(dp);
			}
		}
	}
	
	private static MethodPlugin getMethodPlugin(org.eclipse.epf.uma.Process p) {
		
		//First of all, load the plugin.xmi file
		//Then, retrieve the Method Plugin
		
		XMIResourceFactoryImpl _xmiFac = new XMIResourceFactoryImpl();

		ResourceSet rSet = new ResourceSetImpl();

		URI resourceUri = p.eResource().getURI();
		URI segmentedURI = resourceUri.trimSegments(3);
		URI pluginXmiUri = segmentedURI.appendSegment("plugin.xmi");

		rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", _xmiFac);
		
		Resource resource = rSet.getResource(pluginXmiUri, true);
		
		if(resource != null) {
			
			ProcessComponent pcInPluginXMI = null;
			ProcessComponent pcInModelXMI = (ProcessComponent) p.eContainer();
			
			TreeIterator<EObject> it = resource.getAllContents();
			boolean found = false;
			while(it.hasNext() && !found) {
				EObject next = it.next();
				if(next instanceof ProcessComponent) {
					ProcessComponent pc = (ProcessComponent) next;
					if(pc.eIsProxy()) {
						URI proxyURI = ((InternalEObject)pc).eProxyURI();
						
						if(proxyURI.fragment().equals(pcInModelXMI.getGuid())) {
							pcInPluginXMI = pc;
							found = true;
						}
					}
				}
			}
			
			if(pcInPluginXMI != null && found) {
				
				EObject container = pcInPluginXMI.eContainer();
				
				while(container != null &&
						!(container instanceof MethodPlugin)) {
					
					container = container.eContainer();
				}
				
				return container != null ? (MethodPlugin) container : null;
			}
		}
		
		return null;
	}
	
	private static void initializeContentElements(MethodPlugin mp) {
		
		TreeIterator<EObject> it = mp.eAllContents();
		while(it.hasNext()) {
			EObject eobj = it.next();
			if(eobj instanceof Task) {
				tasks.add((Task) eobj);
			}
			else if(eobj instanceof Domain) {
				if(!(eobj.eContainer() instanceof Domain)) {
					domains.add((Domain) eobj);
				}
			}
			else if(eobj instanceof WorkProduct) {
				workProducts.add((WorkProduct) eobj);
			}
			else if(eobj instanceof RoleSetGrouping) {
				roleSetGroupings.add((RoleSetGrouping) eobj);
			}
			else if(eobj instanceof RoleSet) {
				roleSets.add((RoleSet) eobj);
			}
			else if(eobj instanceof Role) {
				roles.add((Role) eobj);
			}
			else if(eobj instanceof Guideline) {
				guidelines.add((Guideline) eobj);
			}
			else if(eobj instanceof ToolMentor) {
				toolMentors.add((ToolMentor) eobj);
			}
		}
	}
	
	private static ProcessComponent getProcessComponent(org.eclipse.epf.uma.Process p) {
		
		EObject container = p.eContainer();
		
		while(!(container instanceof ProcessComponent)) {
			container = container.eContainer();
		}
		
		return (ProcessComponent) container;
	}
	
	private static void initializeTaskDescriptors(ProcessComponent processComponent) {
		
		TreeIterator<EObject> it = processComponent.eAllContents();
		
		while(it.hasNext()) {
			EObject eobj = it.next();
			if(eobj instanceof TaskDescriptor) {
				
				TaskDescriptor td = (TaskDescriptor) eobj;
				
				if(!contains(taskDescriptors, td)) {
					taskDescriptors.add(td);
				}
				
				Task t = getTask(td.getTask());
				
				if(t != null) {
					List<TaskDescriptor> descriptors = taskDescriptorMap.get(t);
					if(descriptors == null) {
						descriptors = new ArrayList<TaskDescriptor>();
						descriptors.add(td);
						taskDescriptorMap.put(t, descriptors);
					}
					else {
						if(!contains(descriptors, td)) {
							descriptors.add(td);
						}
					}
				}
			}
			else if(eobj instanceof CapabilityPattern) {
				CapabilityPattern cp = (CapabilityPattern) eobj;
				
				if(cp.getVariabilityType().equals(VariabilityType.EXTENDS)) {
					CapabilityPattern cp2 = MethodElements.getCapabilityPattern(getGUId(cp));
					
					MethodPlugin methodPlugin = getMethodPlugin(cp2);
				
					if(!contains(methodPlugins, methodPlugin)) {
						methodPlugins.add(methodPlugin);
						initializeContentElements(methodPlugin);
					}
					
					ProcessComponent pc = getProcessComponent(cp2);
					initializeTaskDescriptors(pc);
				}	
			}
		}
	}
	
	private static void initializeRoleDescriptors(ProcessComponent processComponent) {

		TreeIterator<EObject> it = processComponent.eAllContents();

		while (it.hasNext()) {
			EObject eobj = it.next();
			if (eobj instanceof RoleDescriptor) {

				RoleDescriptor rd = (RoleDescriptor) eobj;

				if(!contains(roleDescriptors, rd)) {
					roleDescriptors.add(rd);
				}

				Role r = getRole(rd.getRole());

				if (r != null) {
					List<RoleDescriptor> descriptors = roleDescriptorMap.get(r);
					if (descriptors == null) {
						descriptors = new ArrayList<RoleDescriptor>();
						descriptors.add(rd);
						roleDescriptorMap.put(r, descriptors);
					} else {
						if(!contains(descriptors, rd)) {
							descriptors.add(rd);
						}
					}
				}
			}
			else if(eobj instanceof CapabilityPattern) {
				CapabilityPattern cp = (CapabilityPattern) eobj;
				
				if(cp.getVariabilityType().equals(VariabilityType.EXTENDS)) {
					CapabilityPattern cp2 = MethodElements.getCapabilityPattern(getGUId(cp));
					
					MethodPlugin methodPlugin = getMethodPlugin(cp2);
					
					if(!contains(methodPlugins, methodPlugin)) {
						methodPlugins.add(methodPlugin);
						initializeContentElements(methodPlugin);
					}
					
					ProcessComponent pc = getProcessComponent(cp2);
					initializeRoleDescriptors(pc);
				}	
			}
		}
	}
	
	private static void initializeUserTasks(DeliveryProcess dp) {
		
		try {
			XMIResourceFactoryImpl _xmiFac = new XMIResourceFactoryImpl();

			ResourceSet rSet = new ResourceSetImpl();
			
			rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
					"*", _xmiFac);

			URI resourceUri = dp.eResource().getURI();
			URI segmentedURI = resourceUri.trimSegments(1);
			URI activitiFolderURI = segmentedURI.appendSegment("activiti");

			List<String> guids = getGuids(activitiFolderURI);
			
			for(String guid : guids) {
				
				URI uri = activitiFolderURI.appendSegment(guid + ".activiti");
				
				Resource resource = rSet.getResource(uri, true);
				
				if(resource != null) {
					for (EObject eobj : resource.getContents()) {
						if (eobj instanceof UserTask) {
							userTasks.add((UserTask) eobj);
						}
					}
				}
			}
		} catch (Exception e) {

		}
	}
	
	/*
	 * Returns the corresponding Activiti Task ID given a SPEM task descriptor
	 */
	public static String getActivitiTaskId(TaskDescriptor td) {
		
		if(td != null && !td.getGuid().equals("")) {
			for(UserTask ut : userTasks) {
				for(Property p : ut.getProperties()) {
					if (p.getId().equals("uma_element")
							&& p.getName().equals(td.getGuid())) {
						
						return ut.getId();
					}
				}
			}
		}
		
		return "";
	}
	
	public static Task getTask(Task proxyTask) {
		
		if(proxyTask != null && proxyTask.eIsProxy()) {
			URI proxyURI = ((InternalEObject)proxyTask).eProxyURI();
			
			for(Task t : tasks) {
				if(proxyURI.fragment().equals(t.getGuid())) {
					return t;
				}
			}
		}
		
		return null;
	}
	
	public static Role getRole(Role proxyRole) {
		
		if(proxyRole != null && proxyRole.eIsProxy()) {
			URI proxyURI = ((InternalEObject)proxyRole).eProxyURI();
			
			for(Role r : roles) {
				if(proxyURI.fragment().equals(r.getGuid())) {
					return r;
				}
			}
		}
		
		return null;
	}
	
	public static WorkProduct getWorkProduct(WorkProduct proxywp) {
		
		if(proxywp != null && proxywp.eIsProxy()) {
			URI proxyURI = ((InternalEObject)proxywp).eProxyURI();
			
			for(WorkProduct wp : workProducts) {
				if(proxyURI.fragment().equals(wp.getGuid())) {
					return wp;
				}
			}
		}
		
		return null;
	}
	
	public static List<WorkProduct> getUncategorizedWorkProducts() {
		
		List<WorkProduct> result = new ArrayList<WorkProduct>();
		
		for(WorkProduct wp : workProducts) {
			
			boolean contained = false;
			
			for(int i = 0; i < domains.size() && !contained; i++) {
				Domain d = domains.get(i);
				if(isContainedInDomain(d, wp)) {
					contained = true;
				}
			}
			
			if(!contained) {
				result.add(wp);
			}
		}
		
		return result;
	}
	
	private static boolean isContainedInDomain(Domain d, WorkProduct wp) {
		
		if(d.getWorkProducts().contains(wp)) {
			return true;
		}
		
		for(Domain d2 : d.getSubdomains()) {
			return isContainedInDomain(d2, wp);
		}
		
		return false;
	}
	
	public static boolean performs(RoleDescriptor rd, TaskDescriptor td) {
		
		if(containsRole(td.getPerformedPrimarilyBy(), rd) ||
				containsRole(td.getAdditionallyPerformedBy(), rd) ||
				containsRole(td.getAssistedBy(), rd)) {
			
			return true;
		}
		
		return false;
	}
	
	private static boolean containsRole(List<RoleDescriptor> list, RoleDescriptor rd) {
		
		for(RoleDescriptor roleDescriptor : list) {
			if(roleDescriptor.getRole() != null &&
					rd.getRole() != null) {
				
				if(roleDescriptor.getRole().eIsProxy() &&
						rd.getRole().eIsProxy()) {
					
					URI proxyURI = ((InternalEObject)roleDescriptor.getRole()).eProxyURI();
					URI proxyURI2 = ((InternalEObject)rd.getRole()).eProxyURI();
					
					if(proxyURI.fragment() != null && proxyURI2.fragment() != null &&
							proxyURI.fragment().equals(proxyURI2.fragment())) {
						
						return true;
					}
				}
				else {
					return roleDescriptor.getRole().getGuid().equals(rd.getRole().getGuid());
				}
			}
		}
		
		return false;
	}
	
	public static DeliveryProcess getDeliveryProcess(String uri) {
		
		if(uri == null || uri.equals("")) {
			return null;
		}
		
		XMIResourceFactoryImpl _xmiFac = new XMIResourceFactoryImpl();
		ResourceSet rSet = new ResourceSetImpl();
		rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", _xmiFac);
		Resource resource = rSet.getResource(getDecodedURI(uri), true);
		
		if(resource != null && resource.getContents().size() > 0) {
			
			EObject root = resource.getContents().get(0);
			if(!(root instanceof ProcessComponent)) {
				root = resource.getContents().get(1);
			}
			if(root instanceof ProcessComponent) {
				ProcessComponent c = (ProcessComponent) root;
				if(c.getProcess() instanceof DeliveryProcess) {
					return (DeliveryProcess) c.getProcess();
				}
			}
		}
		
		return null;
	}
	
	public static CapabilityPattern getCapabilityPattern(String guid) {
		
		if(guid == null || guid.equals("") || loadedDeliveryProcess == null) {
			
			return null;
		}
		
		URI uri = mapCpGuidToURI.get(guid);
		if(uri == null) {
			MethodPlugin mp = getMethodPlugin(loadedDeliveryProcess);
			if(mp != null) {
				Resource res = mp.eResource();
				if(res != null) {
					EObject eobj = res.getContents().get(0);
					if(eobj instanceof ResourceManager) {
						ResourceManager rm = (ResourceManager) eobj;
						ResourceDescriptor rd = rm.getResourceDescriptor(guid);
						if(rd != null) {
							uri = rd.getResolvedURI();
							mapCpGuidToURI.put(guid, uri);
						}
					}
				}
			}
		}
		if(uri != null) {
			try {
				XMIResourceFactoryImpl _xmiFac = new XMIResourceFactoryImpl();
				ResourceSet rSet = new ResourceSetImpl();
				rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
						"*", _xmiFac);
				Resource resource = rSet.getResource(uri, true);
				
				if(resource != null && resource.getContents().size() > 0) {
					
					EObject root = resource.getContents().get(0);
					if(!(root instanceof ProcessComponent)) {
						root = resource.getContents().get(1);
					}
					if(root instanceof ProcessComponent) {
						ProcessComponent c = (ProcessComponent) root;
						if(c.getProcess() instanceof CapabilityPattern) {
							return (CapabilityPattern) c.getProcess();
						}
					}
				}
			}
			catch(Exception e) {
				
			}
		}
			
		return null;
	}
	
	private static void clearLists() {
		
		methodPlugins.clear();
		tasks.clear();
		domains.clear();
		workProducts.clear();
		roleSetGroupings.clear();
		roleSets.clear();
		roles.clear();
		guidelines.clear();
		toolMentors.clear();
		taskDescriptors.clear();
		roleDescriptors.clear();
		taskDescriptorMap.clear();
		roleDescriptorMap.clear();
		userTasks.clear();
		mapCpGuidToURI.clear();
	}
	
	private static String getGUId(CapabilityPattern cp) {
		
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
	
	private static boolean contains(List elements, MethodElement elem) {
		
		for(Object element : elements) {
			if(element instanceof MethodElement) {
				MethodElement me = (MethodElement) element;
				
				if(me.getGuid().equals(elem.getGuid())) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static void resolveContentDescription(Guidance g) {
		
		ContentDescription cd = g.getPresentation();
		
		if(cd.eIsProxy()) {
			XMIResourceFactoryImpl _xmiFac = new XMIResourceFactoryImpl();

			ResourceSet rSet = new ResourceSetImpl();

			URI resourceUri = g.eResource().getURI();
			URI segmentedURI = resourceUri.trimSegments(1);
			URI finalUri = segmentedURI.appendSegment("guidances");
			if(g.eClass().getName().endsWith("s")) {
				finalUri = finalUri.appendSegment(g.eClass().getName().toLowerCase());
			}
			else {
				finalUri = finalUri.appendSegment(g.eClass().getName().toLowerCase() + "s");
			}
			finalUri = finalUri.appendSegment(g.getName() + ".xmi");
			
			rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
					"*", _xmiFac);
			
			Resource resource = rSet.getResource(finalUri, true);
			
			if(resource != null) {
				if(resource.getContents().size() > 0) {
					EObject eobj = resource.getContents().get(0);
					if(eobj instanceof ContentDescription) {
						ContentDescription cd2 = (ContentDescription) eobj;
						g.setPresentation(cd2);
					}
				}
			}
		}
	}
	
	private static void loadCurrentMethodLibrary(DeliveryProcess dp) {
		
		MethodLibrary ml = null;
		
		URI resourceUri = dp.eResource().getURI();
		URI segmentedURI = resourceUri.trimSegments(4);
		URI finalUri = segmentedURI.appendSegment("library.xmi");
		
		if(!resourceUri.isPlatformPlugin()) {
			try {
				ml = LibraryUtil.loadLibrary(finalUri.toString().replace("file:", ""));
				LibraryService.getInstance().setCurrentMethodLibrary(ml);
			}
			catch(Exception e) {
				System.out.println();
			}
		}
		else {
			try {
				ml = loadMethodLibrary(finalUri);
				
				ml.getMethodPlugins().clear();
				
				List<String> pluginUris = getMethodPluginUris(ml);
				
				for(String pluginUri : pluginUris) {
					
					MethodPlugin mp = getMethodPlugin(segmentedURI, pluginUri);
					ml.getMethodPlugins().add(mp);
				}
				
				LibraryService.getInstance().setCurrentMethodLibrary(ml);
			}
			catch(Exception e) {
				
			}
		}
	}
	
	private static MethodLibrary loadMethodLibrary(URI uri) {
		
		XMIResourceFactoryImpl _xmiFac = new XMIResourceFactoryImpl();
		ResourceSet rSet = new ResourceSetImpl();
		rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", _xmiFac);
		Resource resource = rSet.getResource(uri, true);
		
		if(resource != null && resource.getContents().size() > 0) {
			
			EObject root = resource.getContents().get(0);
			if(!(root instanceof MethodLibrary)) {
				root = resource.getContents().get(1);
			}
			if(root instanceof MethodLibrary) {
				MethodLibrary ml = (MethodLibrary) root;
				return ml;
			}
		}
		
		return null;
	}
	
	private static List<String> getMethodPluginUris(MethodLibrary ml) {
		
		List<String> uris = new ArrayList<String>();
		
		URI uri = ml.eResource().getURI();
		
		XMIResourceFactoryImpl _xmiFac = new XMIResourceFactoryImpl();
		ResourceSet rSet = new ResourceSetImpl();
		rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", _xmiFac);
		Resource resource = rSet.getResource(uri, true);
		
		if(resource != null && resource.getContents().size() > 0) {
			
			EObject root = resource.getContents().get(0);
			if(root instanceof ResourceManager) {
				ResourceManager rm = (ResourceManager) root;
				for(ResourceDescriptor rd : rm.getResourceDescriptors()) {
					if(rd.getUri() != null && !rd.getUri().equals("")) {
						try {
							String decodedURI = URLDecoder.decode(rd.getUri(), "UTF-8");
							uris.add(decodedURI);
						}
						catch(Exception e) {
							
						}
					}
				}
			}
		}
		
		return uris;
	}
	
	private static MethodPlugin getMethodPlugin(URI libraryUri, String pluginUri) {
		
		StringTokenizer st = new StringTokenizer(pluginUri, "/");
		String[] segments = new String[] {st.nextToken(), st.nextToken()};
		URI finalUri = libraryUri.appendSegments(segments);
		
		XMIResourceFactoryImpl _xmiFac = new XMIResourceFactoryImpl();
		ResourceSet rSet = new ResourceSetImpl();
		rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", _xmiFac);
		Resource resource = rSet.getResource(finalUri, true);
		
		if(resource != null && resource.getContents().size() > 0) {
			
			EObject root = resource.getContents().get(0);
			if(!(root instanceof MethodPlugin)) {
				root = resource.getContents().get(1);
			}
			if(root instanceof MethodPlugin) {
				MethodPlugin mp = (MethodPlugin) root;
				return mp;
			}
		}
		
		return null;
	}
	
	private static List<String> getGuids(URI activitiFolder) {

		List<String> guids = new ArrayList<String>();
		
		if(!activitiFolder.isPlatformPlugin()) {
		
			String activitiFolderString = activitiFolder.toString().replaceFirst("file:", "");
			
			File dir = new File(activitiFolderString);
		
			if(dir.isDirectory()) {
				File[] files = dir.listFiles();
				
				for(int i = 0; i < files.length; i++) {
					File f = files[i];
					if(f.exists() && !f.isDirectory()
							&& f.getName().endsWith(".bpmn20.xml")) {
						
						String guid = f.getName().replace(".bpmn20.xml", "");
						if(!guids.contains(guid)) {
							guids.add(guid);
						}
					}
				}
			}
		
		}
		else {
			String pluginId = activitiFolder.segment(1);
			Bundle b = Platform.getBundle(pluginId);
			
			String relativePath = activitiFolder.toString().replace("platform:/plugin/" + pluginId, "");
			
			Enumeration _enum = b.getEntryPaths(relativePath);
			
			while(_enum.hasMoreElements()) {
				String path = _enum.nextElement().toString();
				StringTokenizer st = new StringTokenizer(path, "/");
				String activitiFile = "";
				while(st.hasMoreTokens()) {
					activitiFile = st.nextToken();
				}
				String guid = activitiFile.replace(".bpmn20.xml", "");
				guid = guid.replace(".activiti", "");
				guid = guid.replace(".png", "");
				if(!guids.contains(guid)) {
					guids.add(guid);
				}
			}
		}
		
		return guids;
	}
	
	private static URI getDecodedURI(String uriString) {
		
		try {
			URI uri = URI.createURI(uriString, false);
			StringTokenizer st = new StringTokenizer(uriString, "/");
			List<String> tokens = new ArrayList<String>();
			while(st.hasMoreTokens()) {
				tokens.add(st.nextToken());
			}
			uri = uri.trimSegments(tokens.size());
			for (int i = 1; i < tokens.size(); i++) {
				uri = uri.appendSegment(tokens.get(i));
			}
			
			return uri;
		}
		catch(Exception e) {
			
		}
		
		return null;
	}
}
