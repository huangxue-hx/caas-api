package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistentVolumeSpec {

	private Object capacity;
	
	private NFSVolumeSource nfs;
	
	private List<String> accessModes;
	
	private AWSElasticBlockStoreVolumeSource awsElasticBlockStore;
	
	private AzureDiskVolumeSource azureDisk;
	
	private AzureFilePersistentVolumeSource azureFile;
	
	private CephFSPersistentVolumeSource cephfs;
	
	private CinderVolumeSource cinder;
	
	private ObjectReference claimRef;
	
	private FCVolumeSource fc;
	
	private FlexVolumeSource flexVolume;
	
	private FlockerVolumeSource flocker;
	
	private GCEPersistentDiskVolumeSource gcePersistentDisk;
	
	private GlusterfsVolumeSource glusterfs;
	
	private HostPathVolumeSource hostPath;
	
	private ISCSIVolumeSource iscsi;
	
	private LocalVolumeSource local;
	
	private List<String> mountOptions;
	
	private String persistentVolumeReclaimPolicy;
	
	private PhotonPersistentDiskVolumeSource photonPersistentDisk;
	
	private PortworxVolumeSource portworxVolume;
	
	private QuobyteVolumeSource quobyte;

	private RBDVolumeSource rbd;
	
	private ScaleIOVolumeSource scaleIO;
	
	private String storageClassName;
	
	private StorageOSPersistentVolumeSource storageos;
	
	private VsphereVirtualDiskVolumeSource vsphereVolume;
	
	public List<String> getAccessModes() {
		return accessModes;
	}

	public void setAccessModes(List<String> accessModes) {
		this.accessModes = accessModes;
	}

	public Object getCapacity() {
		return capacity;
	}

	public void setCapacity(Object capacity) {
		this.capacity = capacity;
	}

	public NFSVolumeSource getNfs() {
		return nfs;
	}

	public void setNfs(NFSVolumeSource nfs) {
		this.nfs = nfs;
	}

	public ObjectReference getClaimRef() {
		return claimRef;
	}

	public void setClaimRef(ObjectReference claimRef) {
		this.claimRef = claimRef;
	}

	public String getPersistentVolumeReclaimPolicy() {
		return persistentVolumeReclaimPolicy;
	}

	public void setPersistentVolumeReclaimPolicy(String persistentVolumeReclaimPolicy) {
		this.persistentVolumeReclaimPolicy = persistentVolumeReclaimPolicy;
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

	public AzureFilePersistentVolumeSource getAzureFile() {
		return azureFile;
	}

	public void setAzureFile(AzureFilePersistentVolumeSource azureFile) {
		this.azureFile = azureFile;
	}

	public CephFSPersistentVolumeSource getCephfs() {
		return cephfs;
	}

	public void setCephfs(CephFSPersistentVolumeSource cephfs) {
		this.cephfs = cephfs;
	}

	public CinderVolumeSource getCinder() {
		return cinder;
	}

	public void setCinder(CinderVolumeSource cinder) {
		this.cinder = cinder;
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

	public HostPathVolumeSource getHostPath() {
		return hostPath;
	}

	public void setHostPath(HostPathVolumeSource hostPath) {
		this.hostPath = hostPath;
	}

	public ISCSIVolumeSource getIscsi() {
		return iscsi;
	}

	public void setIscsi(ISCSIVolumeSource iscsi) {
		this.iscsi = iscsi;
	}

	public LocalVolumeSource getLocal() {
		return local;
	}

	public void setLocal(LocalVolumeSource local) {
		this.local = local;
	}

	public List<String> getMountOptions() {
		return mountOptions;
	}

	public void setMountOptions(List<String> mountOptions) {
		this.mountOptions = mountOptions;
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

	public String getStorageClassName() {
		return storageClassName;
	}

	public void setStorageClassName(String storageClassName) {
		this.storageClassName = storageClassName;
	}

	public StorageOSPersistentVolumeSource getStorageos() {
		return storageos;
	}

	public void setStorageos(StorageOSPersistentVolumeSource storageos) {
		this.storageos = storageos;
	}

	public VsphereVirtualDiskVolumeSource getVsphereVolume() {
		return vsphereVolume;
	}

	public void setVsphereVolume(VsphereVirtualDiskVolumeSource vsphereVolume) {
		this.vsphereVolume = vsphereVolume;
	}

}
