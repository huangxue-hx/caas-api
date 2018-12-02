package com.harmonycloud.k8s.constant;

/**
 * @author qg
 *
 */
public class Constant {

	public final static String API_V1_VERSION="/api/v1";

	public final static String VERSION_V1="v1";
	
	public final static String APIS_EXTENTIONS_VERSION="/apis/extensions";
	
	public final static String APIS_EXTENTIONS_V1BETA1_VERSION = "/apis/extensions/v1beta1";
	
	public final static String APIS_RBAC_VERSION="/apis/rbac.authorization.k8s.io";
	
	//public final static String APIS_RBAC_VERSION_V1ALPHA1="/apis/rbac.authorization.k8s.io/v1alpha1";

	public final static String APIS_RBAC_VERSION_V1="/apis/rbac.authorization.k8s.io/v1";
	
	public final static String APIS_AUTOSCALING_VERSION = "autoscaling/v2beta1";
	
	public final static String API_VERSION = "rbac.authorization.k8s.io/v1alpha1";

	public final static String API_VERSION_RBAC_V1 = "rbac.authorization.k8s.io/v1";
	
	public final static String BATCH_V1_VERSION="/batch/v1";

	public final static String APPS_V1_VERSION = "apps/v1";

	public final static String RBAC_V1_VERSION = "rbac.authorization.k8s.io/v1";

	public final static String V1_VERSION = "v1";

	public final static String EXTENTIONS_V1BETA1_VERSION = "extensions/v1beta1";

	public final static String BATCH_V1BRTA1 = "batch/v1beta1";
	
	public final static String BATCH_V2ALPHA1_VERSION="/batch/v2alpha1";

	public final static String STORAGECLASS_V1="storage.k8s.io/v1";
	
	public final static String APIS_POLICY_V1BETA1 = "policy/v1beta1";
	
	
	//避免各资源对象改版
	public final static String DEPLOYMENT_VERSION = APIS_EXTENTIONS_V1BETA1_VERSION;

	public final static String RBAC_VERSION = APIS_RBAC_VERSION_V1;
	
	public final static String NAMESPACE_VERSION = API_V1_VERSION;
	
	public final static String POD_VERSION = API_V1_VERSION;

	public final static String PV_VERSION = API_V1_VERSION;

	public final static String NAMESPACE_API_VERSION = V1_VERSION;

	public final static String POD_API_VERSION = V1_VERSION;

	public final static String PV_API_VERSION = V1_VERSION;

	public final static String PVC_VERSION = V1_VERSION;
	
	public final static String JOB_VERSION = BATCH_V1_VERSION;

	public final static String DEPLOYMENT_API_VERSION = APPS_V1_VERSION;

	public final static String DAEMONSET_VERSION = APPS_V1_VERSION;

	public final static String CLUSTERROLE_VERSION = RBAC_V1_VERSION;

	public final static String CLUSTERROLEBINDING_VERSION = RBAC_V1_VERSION;

	public final static String ROLEBINDING_VERSION = RBAC_V1_VERSION;

	public final static String ROLE_VERSION = RBAC_V1_VERSION;

	public final static String CONFIGMAP_VERSION = V1_VERSION;
	
	public final static String CRONJOB_VERSION = BATCH_V1BRTA1;

	public final static String INGRESS_VERSION = EXTENTIONS_V1BETA1_VERSION;

	public final static String SERVICE_VERSION = V1_VERSION;
	
	public final static String POD_DISRUPTION_VERSION = APIS_POLICY_V1BETA1;

    public final static String STATEFULSET_VERSION = APPS_V1_VERSION;

    public final static String CONTROLLER_REVISION_VERSION = APPS_V1_VERSION;
	
	public final static String HTTP_401 = "401";
	
	public final static String HTTP_UNAUTHORIZED = "Unauthorized";

	public final static Integer HTTP_404 = 404;

	public final static String INFLUXDB_DB_NAME = "k8s";
	public final static String ES_CLUSTER_NAME = "kubernetes-logging";
	public final static String CLUSTER_STATUS_TYPE_READY = "ready";
	public final static String SERVICE_TYPE_API = "api";
	public final static int HARBOR_LOGIN_TIMEOUT = 15*60*1000;
	
}