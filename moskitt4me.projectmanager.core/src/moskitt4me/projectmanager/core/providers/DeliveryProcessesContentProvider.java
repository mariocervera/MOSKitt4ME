package moskitt4me.projectmanager.core.providers;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epf.library.util.LibraryUtil;
import org.eclipse.epf.resourcemanager.ResourceDescriptor;
import org.eclipse.epf.resourcemanager.ResourceManager;
import org.eclipse.epf.uma.DeliveryProcess;
import org.eclipse.epf.uma.MethodLibrary;
import org.eclipse.epf.uma.ProcessComponent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class DeliveryProcessesContentProvider extends ArrayContentProvider implements
	ITreeContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		
		List<Object> elements = new ArrayList<Object>();
		
		//Libraries contributed via extension points
		
		IConfigurationElement[] configurations = Platform
				.getExtensionRegistry().getConfigurationElementsFor(
						"moskitt4me.projectmanager.core.methodlibraryprovider");
		
		for(IConfigurationElement config : configurations) {
			String relativeLibraryPath = config.getAttribute("path");
			String pluginId = config.getContributor().getName();
			
			MethodLibrary ml = loadMethodLibrary(pluginId, relativeLibraryPath);
			elements.add(ml);
		}
		
		//Library contributed via Windows -> Preferences
		
		String libraryPath = Platform.getPreferencesService().getString(
				"moskitt4me.projectmanager.core", "ExternalLibraryPath", "",
				null);
		
		if(libraryPath != null && !libraryPath.equals("")) {
			
			try {
				MethodLibrary lib = LibraryUtil.loadLibrary(libraryPath);
				if(lib != null) {
					elements.add(lib);
				}
			}
			catch(Exception e) {
				
			}
		}
		
		return elements.toArray();
	}
	
	public Object getParent(Object element) {
		
		return null;
	}

	
	public boolean hasChildren(Object element) {
		
		if(element instanceof MethodLibrary) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public Object[] getChildren(Object parentElement) {
		
		List<Object> children = new ArrayList<Object>();
		
		if(parentElement instanceof MethodLibrary) {
			
			MethodLibrary ml = (MethodLibrary) parentElement;
			
			List<String> pluginUris = getMethodPluginUris(ml);
			for(String pluginUri : pluginUris) {
				List<String> processUris = getProcessUris(ml, pluginUri);
				for(String processUri : processUris) {
					DeliveryProcess dp = loadDeliveryProcess(ml, pluginUri, processUri);
					if(dp != null) {
						children.add(dp);
					}
				}
			}
		}
		
		return children.toArray();
	}
	
	private MethodLibrary loadMethodLibrary(String pluginId, String relativeLibraryPath) {
		
		URI uri = URI.createPlatformPluginURI("/" + pluginId + "/" + relativeLibraryPath , false);
		
		if(uri == null || uri.equals("")) {
			return null;
		}
		
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
	
	private List<String> getMethodPluginUris(MethodLibrary ml) {
		
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
	
	private List<String> getProcessUris(MethodLibrary ml, String pluginUri) {
		
		List<String> uris = new ArrayList<String>();
		
		URI uri = ml.eResource().getURI();
		URI segmentedUri = uri.trimSegments(1);
		StringTokenizer st = new StringTokenizer(pluginUri, "/");
		String[] segments = new String[] {st.nextToken(), st.nextToken()};
		URI finalUri = segmentedUri.appendSegments(segments);
		
		XMIResourceFactoryImpl _xmiFac = new XMIResourceFactoryImpl();
		ResourceSet rSet = new ResourceSetImpl();
		rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", _xmiFac);
		Resource resource = rSet.getResource(finalUri, true);
		
		if(resource != null && resource.getContents().size() > 0) {
			
			EObject root = resource.getContents().get(0);
			if(root instanceof ResourceManager) {
				ResourceManager rm = (ResourceManager) root;
				for(ResourceDescriptor rd : rm.getResourceDescriptors()) {
					if(rd.getUri() != null && rd.getUri().startsWith("deliveryprocesses")) {
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
	
	private DeliveryProcess loadDeliveryProcess(MethodLibrary ml, String pluginUri, String processUri) {
		
		URI uri = ml.eResource().getURI();
		URI segmentedUri = uri.trimSegments(1);
		StringTokenizer st = new StringTokenizer(pluginUri, "/");
		StringTokenizer st2 = new StringTokenizer(processUri, "/");
		String[] segments = new String[] {st.nextToken(), st2.nextToken(), st2.nextToken(), st2.nextToken()};
		URI finalUri = segmentedUri.appendSegments(segments);
		
		XMIResourceFactoryImpl _xmiFac = new XMIResourceFactoryImpl();
		ResourceSet rSet = new ResourceSetImpl();
		rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", _xmiFac);
		Resource resource = rSet.getResource(finalUri, true);
		
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
}
