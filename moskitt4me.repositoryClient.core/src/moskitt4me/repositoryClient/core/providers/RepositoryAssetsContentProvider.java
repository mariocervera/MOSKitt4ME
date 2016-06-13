package moskitt4me.repositoryClient.core.providers;

import java.util.ArrayList;
import java.util.List;

import moskitt4me.repositoryClient.core.util.RepositoryClientUtil;
import moskitt4me.repositoryClient.core.util.RepositoryLocation;
import moskitt4me.repositoryClient.core.util.TechnicalFragment;

import org.apache.commons.net.ftp.FTPClient;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * A content provider for the "Import Technical Fragment Dialog"
 *
 * @author Mario Cervera
 */
public class RepositoryAssetsContentProvider extends ArrayContentProvider implements ITreeContentProvider {

	/*
	 * This method returns the root-level elements of the Tree viewer. These elements are the
	 * fragments that are stored in the repository location
	 */
	public Object[] getElements(Object inputElement) {
		
		if(inputElement instanceof RepositoryLocation) {
			RepositoryLocation location = (RepositoryLocation) inputElement;
			
			FTPClient client = null;
			List<TechnicalFragment> assets = new ArrayList<TechnicalFragment>();
			 
			try {
				client = RepositoryClientUtil.connect(location, false);
				if(client != null) {
					String[] fileNames = client.listNames();
					for(String name : fileNames) {
						String extension = ".ras.zip";
						String assetName = name.substring(0, name.length() - extension.length());
						TechnicalFragment asset = new TechnicalFragment(assetName, "","","","","");
						asset.setResolved(true);
						assets.add(asset);
					}
					return assets.toArray();
				}
			}
			catch(Exception e) {
				
			}
			finally {
				if(client != null) {
					RepositoryClientUtil.disconnect(client);
				}
			}
		}
		
		return super.getElements(inputElement);
	}
	
	public Object getParent(Object element) {
		
		return null;
	}
	
	public boolean hasChildren(Object element) {
		
		return false;
	}
	
	
	public Object[] getChildren(Object parentElement) {
		
		return null;
	}
}
