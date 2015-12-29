package moskitt4me.repositoryClient.core.actions;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import moskitt4me.repositoryClient.core.providers.MethodFragmentItemProvider;
import moskitt4me.repositoryClient.core.util.RASAssetReader;
import moskitt4me.repositoryClient.core.util.RepositoryClientUtil;
import moskitt4me.repositoryClient.core.util.RepositoryLocation;
import moskitt4me.repositoryClient.fragmentIntegration.FragmentIntegration;

public class IntegrateConceptualFragmentAction extends IntegrateFragmentAction {

	String type;
	
	protected int performFragmentIntegration(MethodFragmentItemProvider fragment) throws IOException {
		
		/*Integration of the elements at the xmi file (representation of a conceptual fragment) 
		on the selected library*/
		
		RepositoryLocation location = fragment.getLocation();
		String fileName = fragment.getFileName();
		String fragmentDir = RepositoryClientUtil.downloadFragment(location, fileName);
		
		RASAssetReader ras = new RASAssetReader(fragmentDir, fileName);
		Map<String, String> properties = ras.getProperties();
		type = properties.get("Type");
		
		
		if(!type.equals("Process")){
			
			/*Content fragments*/
			
			int result = -1;
				try {
					result = FragmentIntegration.integrateConceptualFragmentOfTypeContent(  fragmentDir, fileName,type);
				}
				catch(Exception e) {
					result = -1;
				}
				finally {
					if(!fragmentDir.equals("")) {
						RepositoryClientUtil.removeFolder(new File(fragmentDir));
					}
				}
			return result;
			
		}else{
			
			/*Process fragments*/
			
			int result = -1;
			
				try {
					result = FragmentIntegration.integrateConceptualFragmentOfTypeProcess(fragmentDir, fileName);
				}
				catch(Exception e) {
					result = -1;
				}
				finally {
					if(!fragmentDir.equals("")) {
						RepositoryClientUtil.removeFolder(new File(fragmentDir));
					}
				}
			return result;
		}
	}
	
}
