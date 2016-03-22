package moskitt4me.repositoryClient.core.providers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import moskitt4me.repositoryClient.core.util.RASAssetReader;
import moskitt4me.repositoryClient.core.util.RepositoryClientUtil;
import moskitt4me.repositoryClient.core.util.RepositoryLocation;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;

/*
* Provides content for the main interface of the Repositories View.
*
* @author Mario Cervera
*/
@SuppressWarnings("unchecked")
public class RepositoriesContentProvider extends AdapterFactoryContentProvider {
	
	public RepositoriesContentProvider(AdapterFactory adapterFactory) {
		
		super(adapterFactory);
	}
	
	@Override
	public Object[] getElements(Object object) {
		
		if(object instanceof List) {
			List<RepositoryLocation> elements = (List<RepositoryLocation>)object;
			return elements.toArray();
		}
		
		return super.getElements(object);
	}
	
	/*
	* Calculates whether a given element has children or not
	*/
	@Override
	public boolean hasChildren(Object object) {
		
		if(object instanceof RepositoryLocation || object instanceof MethodFragmentItemProvider) {
			return true;
		}
		else if(object instanceof MethodFragmentPropertyItemProvider) {
			MethodFragmentPropertyItemProvider property = (MethodFragmentPropertyItemProvider) object;
			if(property.getPropertyName().equals("Descriptor") ||
					property.getPropertyName().equals("Interface")) {
				
				return true;
			}
		}
		
		return super.hasChildren(object);
	}
	
	/*
	* Calculates the children of a given element
	*/
	@Override
	public Object[] getChildren(Object object) {
		
		List<Object> result = new ArrayList<Object>();
		
		if(object instanceof RepositoryLocation) {
			
			FTPClient client = null;
			
			try {
				RepositoryLocation location = (RepositoryLocation) object;
				client = RepositoryClientUtil.connect(location, false);
				
				FTPFile[] files = client.listFiles();
				
				for(FTPFile file : files) {
					String fileName = file.getName();
					if(fileName.endsWith(".ras.zip")) {
						
						String fragmentDir = "";
						try {
							fragmentDir = RepositoryClientUtil.downloadFragment(location, fileName);
							
							RASAssetReader reader = new RASAssetReader(fragmentDir, fileName);
							Map<String, String> properties = reader.getProperties();
						
							String type = properties.get("Type");
							
							if(RepositoryClientUtil.isTechnicalFragment(type)) {
								if(!type.equals("Others")) {
									String origin = properties.get("Origin");
									String objective = properties.get("Objective");
									String input = properties.get("Input");
									String output = properties.get("Output");
									String toolId = properties.get("ToolId");
									String description = type.equals("External Tool") || 
															type.equals("Internal Tool") ? 
																	properties.get("Description") : "";
								
									MethodFragmentItemProvider fragment = new MethodFragmentItemProvider(
											adapterFactory, location, fileName, type, origin, objective,
											input, output, toolId, description);
									result.add(fragment);
								}
							}
							else{
								
								String origin =  properties.get("Origin");
								String objective = properties.get("Objective");
					
								MethodFragmentItemProvider fragment = new MethodFragmentItemProvider(
										adapterFactory, location, fileName, type,  origin, objective);
								result.add(fragment);
							}
						}
						catch(Exception e) {
							
						}
						finally {
							if(!fragmentDir.equals("")) {
								RepositoryClientUtil.removeFolder(new File(fragmentDir));
							}
						}
					}
				}
			}
			catch(IOException e) {
				
			}
			finally {
				if(client != null) {
					RepositoryClientUtil.disconnect(client);
				}
			}
		}
		else if(object instanceof MethodFragmentItemProvider) {
			
			MethodFragmentItemProvider fragment = (MethodFragmentItemProvider) object;
			
			result.add(new MethodFragmentPropertyItemProvider(adapterFactory, fragment, "Descriptor", ""));
			
			if(RepositoryClientUtil.isTechnicalFragment(fragment.getType())) {
				result.add(new MethodFragmentPropertyItemProvider(adapterFactory, fragment, "Interface", ""));
			}
		}
		else if(object instanceof MethodFragmentPropertyItemProvider) {
			
			MethodFragmentPropertyItemProvider property = (MethodFragmentPropertyItemProvider) object;
			MethodFragmentItemProvider fragment = property.getMethodFragment();
			
			if(property.getPropertyName().equals("Descriptor")) {
				result.add(new MethodFragmentPropertyItemProvider(adapterFactory, 
						null, "Type", fragment.getType()));
				result.add(new MethodFragmentPropertyItemProvider(adapterFactory, 
						null, "Origin", fragment.getOrigin()));
				result.add(new MethodFragmentPropertyItemProvider(adapterFactory, 
						null, "Objective", fragment.getObjective()));
			}
			else if(property.getPropertyName().equals("Interface")) {
				result.add(new MethodFragmentPropertyItemProvider(adapterFactory, 
						null, "Input", fragment.getInput()));
				result.add(new MethodFragmentPropertyItemProvider(adapterFactory, 
						null, "Output", fragment.getOutput()));
			}
		}
		
		return result.toArray();
	}
}
