package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Volume {

	private String name;
	
	private NFSVolumeSource nfs;
	
	private SecretVolumeSource secret;
	
	private GitRepoVolumeSource gitRepo;
	
	private PersistentVolumeClaimVolumeSource persistentVolumeClaim;
	
	private EmptyDirVolumeSource emptyDir;
	
	private HostPath hostPath;
	
	private ConfigMapVolumeSource configMap;
	
	private AWSElasticBlockStoreVolumeSource awsElasticBlockStore;
	
	private AzureDiskVolumeSource azureDisk;

	private AzureFileVolumeSource azureFile;
	
	private CephFSVolumeSource cephfs;
	
	private CinderVolumeSource cinder;
	
	private DownwardAPIVolumeSource downwardAPI;
	
	private FCVolumeSource fc;
	
	private FlexVolumeSource flexVolume;
	
	private FlockerVolumeSource flocker;
	
	private GCEPersistentDiskVolumeSource gcePersistentDisk;
	
	private GlusterfsVolumeSource glusterfs;
	
	private ISCSIVolumeSource iscsi;
	
	private PhotonPersistentDiskVolumeSource photonPersistentDisk;
	
	private PortworxVolumeSource portworxVolume;
	
	private ProjectedVolumeSource projected;
	
	private QuobyteVolumeSource quobyte;
	
	private RBDVolumeSource rbd;
	
	private ScaleIOVolumeSource scaleIO;
	
	private StorageOSVolumeSource storageos;
	
	private VsphereVirtualDiskVolumeSource vsphereVolume;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NFSVolumeSource getNfs() {
		return nfs;
	}

	public void setNfs(NFSVolumeSource nfs) {
		this.nfs = nfs;
	}

	public SecretVolumeSource getSecret() {
		return secret;
	}

	public void setSecret(SecretVolumeSource secret) {
		this.secret = secret;
	}

	public GitRepoVolumeSource getGitRepo() {
		return gitRepo;
	}

	public void setGitRepo(GitRepoVolumeSource gitRepo) {
		this.gitRepo = gitRepo;
	}

	public PersistentVolumeClaimVolumeSource getPersistentVolumeClaim() {
		return persistentVolumeClaim;
	}

	public void setPersistentVolumeClaim(PersistentVolumeClaimVolumeSource persistentVolumeClaim) {
		this.persistentVolumeClaim = persistentVolumeClaim;
	}

	public EmptyDirVolumeSource getEmptyDir() {
		return emptyDir;
	}

	public void setEmptyDir(EmptyDirVolumeSource emptyDir) {
		this.emptyDir = emptyDir;
	}

    public ConfigMapVolumeSource getConfigMap() {
        return configMap;
    }

    public void setConfigMap(ConfigMapVolumeSource configMap) {
        this.configMap = configMap;
    }

	public HostPath getHostPath() {
		return hostPath;
	}

	public void setHostPath(HostPath hostPath) {
		this.hostPath = hostPath;
	}

	public AWSElasticBlockStoreVolumeSource getAwsElasticBlockStore() {
		return awsElasticBlockStore;
	}

	public void setAwsElasticBlockStore(AWSElasticBlockStoreVolumeSource awsElasticBlockStore) {
		this.awsElasticBlockStore = awsElasticBlockStore;
	}

	public AzureDiskVolumeSource getAzureDisk() {
		return azureDisk;
	}

	public void setAzureDisk(AzureDiskVolumeSource azureDisk) {
		this.azureDisk = azureDisk;
	}

	public AzureFileVolumeSource getAzureFile() {
		return azureFile;
	}

	public void setAzureFile(AzureFileVolumeSource azureFile) {
		this.azureFile = azureFile;
	}

	public CephFSVolumeSource getCephfs() {
		return cephfs;
	}

	public void setCephfs(CephFSVolumeSource cephfs) {
		this.cephfs = cephfs;
	}

	public CinderVolumeSource getCinder() {
		return cinder;
	}

	public void setCinder(CinderVolumeSource cinder) {
		this.cinder = cinder;
	}

	public DownwardAPIVolumeSource getDownwardAPI() {
		return downwardAPI;
	}

	public void setDownwardAPI(DownwardAPIVolumeSource downwardAPI) {
		this.downwardAPI = downwardAPI;
	}

	public FCVolumeSource getFc() {
		return fc;
	}

	public void setFc(FCVolumeSource fc) {
		this.fc = fc;
	}

	public FlexVolumeSource getFlexVolume() {
		return flexVolume;
	}

	public void setFlexVolume(FlexVolumeSource flexVolume) {
		this.flexVolume = flexVolume;
	}

	public FlockerVolumeSource getFlocker() {
		return flocker;
	}

	public void setFlocker(FlockerVolumeSource flocker) {
		this.flocker = flocker;
	}

	public GCEPersistentDiskVolumeSource getGcePersistentDisk() {
		return gcePersistentDisk;
	}

	public void setGcePersistentDisk(GCEPersistentDiskVolumeSource gcePersistentDisk) {
		this.gcePersistentDisk = gcePersistentDisk;
	}

	public GlusterfsVolumeSource getGlusterfs() {
		return glusterfs;
	}

	public void setGlusterfs(GlusterfsVolumeSource glusterfs) {
		this.glusterfs = glusterfs;
	}

	public ISCSIVolumeSource getIscsi() {
		return iscsi;
	}

	public void setIscsi(ISCSIVolumeSource iscsi) {
		this.iscsi = iscsi;
	}

	public PhotonPersistentDiskVolumeSource getPhotonPersistentDisk() {
		return photonPersistentDisk;
	}

	public void setPhotonPersistentDisk(PhotonPersistentDiskVolumeSource photonPersistentDisk) {
		this.photonPersistentDisk = photonPersistentDisk;
	}

	public PortworxVolumeSource getPortworxVolume() {
		return portworxVolume;
	}

	public void setPortworxVolume(PortworxVolumeSource portworxVolume) {
		this.portworxVolume = portworxVolume;
	}

	public ProjectedVolumeSource getProjected() {
		return projected;
	}

	public void setProjected(ProjectedVolumeSource projected) {
		this.projected = projected;
	}

	public QuobyteVolumeSource getQuobyte() {
		return quobyte;
	}

	public void setQuobyte(QuobyteVolumeSource quobyte) {
		this.quobyte = quobyte;
	}

	public RBDVolumeSource getRbd() {
		return rbd;
	}

	public void setRbd(RBDVolumeSource rbd) {
		this.rbd = rbd;
	}

	public ScaleIOVolumeSource getScaleIO() {
		return scaleIO;
	}

	public void setScaleIO(ScaleIOVolumeSource scaleIO) {
		this.scaleIO = scaleIO;
	}

	public StorageOSVolumeSource getStorageos() {
		return storageos;
	}

	public void setStorageos(StorageOSVolumeSource storageos) {
		this.storageos = storageos;
	}

	public VsphereVirtualDiskVolumeSource getVsphereVolume() {
		return vsphereVolume;
	}

	public void setVsphereVolume(VsphereVirtualDiskVolumeSource vsphereVolume) {
		this.vsphereVolume = vsphereVolume;
	}
	
}
