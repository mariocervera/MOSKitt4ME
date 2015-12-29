package moskitt4me.repositoryClient.core.util;

public class RepositoryLocation {

	private String host;
    private String repositoryPath;
    private String user;
    private String password;

    private String type; //Empty, Conceptual, or Technical
    
    public RepositoryLocation() {
    	
    	this.host = "";
    	this.repositoryPath = "";
    	this.user = "";
    	this.password = "";
    	
    	this.type = "";
    }
    
    public RepositoryLocation(String host, String repositoryPath, String user,
			String password) {
    	
    	this.host = host;
    	this.repositoryPath = repositoryPath;
    	this.user = user;
    	this.password = password;
    	
    	this.type = "";
    }
    
	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setRepositoryPath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPassword() {
		return password;
	}

    public String getType() {
    	return type;
    }
    
    public void setType(String type) {
    	this.type = type;
    }
    
}
