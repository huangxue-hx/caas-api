package com.harmonycloud.k8s.constant;

/**
 * @author qg
 *
 */
public class Resource {

	public final static String ROLE = "roles";
	
	public final static String ROLEBINDING = "rolebindings";
	
	public final static String CLUSTERROLE = "clusterroles";
	
	public final static String CLUSTERROLEBINDING = "clusterrolebindings";
	
	public final static String NAMESPACE = "namespaces";
	
	public final static String POD = "pods";
	
	public final static String DEPLOYMENT = "deployments";
	
	public final static String SERVICE = "services";
	
	public final static String NODE = "nodes";
	
	public final static String EVENT = "events";
	
	public final static String PERSISTENTVOLUME = "persistentvolumes";
	
	public final static String PERSISTENTVOLUMECLAIM = "persistentvolumeclaims";
	
	public final static String LIMITRANGE = "limitranges";
	
	public final static String RESOURCEQUOTA = "resourcequotas";
	
	public final static String HORIZONTALPODAUTOSCALER = "horizontalpodautoscalers";
	
	public final static String INGRESS = "ingresses";
	
	public final static String NETWORKPOLICY = "networkpolicies";
	
	public final static String REPLICATIONCONTROLLER = "replicationcontrollers";
	
	public final static String REPLICASET = "replicasets";
	
	public final static String SECRET = "secrets";
	
	public final static String CONFIGMAP = "configmaps";
	
	public final static String VOLUMEPROVIDER = "volumeproviders";

	public final static String SERVICEACCOUNT = "serviceaccount";

	public final static String ENDPOINT = "endpoints";

	public final static String EXTERNALNAMESPACE = "external";
	
	public final static String CRONJOB = "cronjobs";
	
	public final static String JOB = "jobs";
	
	/**
	 * 根据resource获取apigroup
	 * @param resource
	 * @return
	 */
	public static String getGroupByResource(String resource){
		String group = "";
		switch (resource) {
		case com.harmonycloud.k8s.constant.Resource.ENDPOINT:
		case com.harmonycloud.k8s.constant.Resource.EXTERNALNAMESPACE:
		case com.harmonycloud.k8s.constant.Resource.POD:
		case com.harmonycloud.k8s.constant.Resource.REPLICATIONCONTROLLER:
		case com.harmonycloud.k8s.constant.Resource.SERVICE:
		case com.harmonycloud.k8s.constant.Resource.NODE:
		case com.harmonycloud.k8s.constant.Resource.EVENT:
		case com.harmonycloud.k8s.constant.Resource.PERSISTENTVOLUME:
		case com.harmonycloud.k8s.constant.Resource.PERSISTENTVOLUMECLAIM:
		case com.harmonycloud.k8s.constant.Resource.LIMITRANGE:
		case com.harmonycloud.k8s.constant.Resource.RESOURCEQUOTA:
		case com.harmonycloud.k8s.constant.Resource.SECRET:
		case com.harmonycloud.k8s.constant.Resource.CONFIGMAP:
		case com.harmonycloud.k8s.constant.Resource.NAMESPACE:
			group = APIGroup.API_V1_VERSION;
			break;
		case com.harmonycloud.k8s.constant.Resource.ROLE:
		case com.harmonycloud.k8s.constant.Resource.CLUSTERROLE:
		case com.harmonycloud.k8s.constant.Resource.CLUSTERROLEBINDING:
		case com.harmonycloud.k8s.constant.Resource.ROLEBINDING:
			group = APIGroup.APIS_RBAC_VERSION_V1ALPHA1;
			break;
		case com.harmonycloud.k8s.constant.Resource.DEPLOYMENT:
		case com.harmonycloud.k8s.constant.Resource.INGRESS:
		case com.harmonycloud.k8s.constant.Resource.NETWORKPOLICY:	
		case com.harmonycloud.k8s.constant.Resource.REPLICASET:
			group = APIGroup.APIS_EXTENTIONS_V1BETA1_VERSION;
			break;
		case com.harmonycloud.k8s.constant.Resource.HORIZONTALPODAUTOSCALER:
			group = APIGroup.APIS_AUTOSCALING_VERSION;
			break;
		case com.harmonycloud.k8s.constant.Resource.VOLUMEPROVIDER:
			group = APIGroup.APIS_HARMONYCLOUD;
			break;

		case com.harmonycloud.k8s.constant.Resource.SERVICEACCOUNT:
			group = APIGroup.APIS_SERVICEACCOUNT_VERSION;
			break;
		case com.harmonycloud.k8s.constant.Resource.CRONJOB:
			group = APIGroup.APIS_BATCH_V2ALPHA1_VERSION;
			break;
		case com.harmonycloud.k8s.constant.Resource.JOB:
			group = APIGroup.APIS_BATCH_V1_VERSION;
			break;
		default:
			break;
		}
		return group;
	}
	
}
