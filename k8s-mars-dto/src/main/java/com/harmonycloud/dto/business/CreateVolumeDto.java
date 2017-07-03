package com.harmonycloud.dto.business;

public class CreateVolumeDto {
	
	private String type; //pv emptyDir hostPath 
	
	private String readOnly;
	
	private String volume;//pv name
	
	private String path;
	
	private String gitUrl;
	
	private String revision;
	
	private String emptyDir;
	
	private String hostPath;

	//PVC parameters

    private String pvcName;

    private String pvcCapacity;

    private String pvcTenantid;

//    private String pvcReadonly;

    private String pvcBindOne;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(String readOnly) {
		this.readOnly = readOnly;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getGitUrl() {
		return gitUrl;
	}

	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}

    public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

    public String getPvcName() {
        return pvcName;
    }

    public void setPvcName(String pvcName) {
        this.pvcName = pvcName;
    }

    public String getPvcCapacity() {
        return pvcCapacity;
    }

    public void setPvcCapacity(String pvcCapacity) {
        this.pvcCapacity = pvcCapacity;
    }

    public String getPvcTenantid() {
        return pvcTenantid;
    }

    public void setPvcTenantid(String pvcTenantid) {
        this.pvcTenantid = pvcTenantid;
    }

    public String getPvcBindOne() {
        return pvcBindOne;
    }

    public void setPvcBindOne(String pvcBindOne) {
        this.pvcBindOne = pvcBindOne;
    }

	public String getEmptyDir() {
		return emptyDir;
	}

	public void setEmptyDir(String emptyDir) {
		this.emptyDir = emptyDir;
	}

	public String getHostPath() {
		return hostPath;
	}

	public void setHostPath(String hostPath) {
		this.hostPath = hostPath;
	}

}
