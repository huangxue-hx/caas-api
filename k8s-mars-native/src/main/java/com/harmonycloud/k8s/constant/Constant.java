package com.harmonycloud.k8s.constant;

/**
 * @author qg
 *
 */
public class Constant {

	public final static String API_V1_VERSION="/api/v1";
	
	public final static String APIS_EXTENTIONS_VERSION="/apis/extensions";
	
	public final static String APIS_EXTENTIONS_V1BETA1_VERSION = "/apis/extensions/v1beta1";
	
	public final static String APIS_RBAC_VERSION="/apis/rbac.authorization.k8s.io";
	
	public final static String APIS_RBAC_VERSION_V1ALPHA1="/apis/rbac.authorization.k8s.io/v1alpha1";
	
	public final static String APIS_AUTOSCALING_VERSION = "/apis/autoscaling/v1";
	
	public final static String API_VERSION = "rbac.authorization.k8s.io/v1alpha1";
	
	public final static String BATCH_V1_VERSION="/batch/v1";
	
	public final static String BATCH_V2ALPHA1_VERSION="/batch/v2alpha1";
	
	//避免各资源对象改版
	public final static String DEPLOYMENT_VERSION = APIS_EXTENTIONS_V1BETA1_VERSION;

	public final static String RBAC_VERSION = APIS_RBAC_VERSION_V1ALPHA1;
	
	public final static String NAMESPACE_VERSION = API_V1_VERSION;
	
	public final static String POD_VERSION = API_V1_VERSION;

	public final static String PV_VERSION = API_V1_VERSION;
	
	public final static String JOB_VERSION = BATCH_V1_VERSION;
	
	public final static String CRONJOB_VERSION = BATCH_V2ALPHA1_VERSION;
	
	public static String HTTP_401 = "401";
	
	public static String HTTP_UNAUTHORIZED = "Unauthorized";
	
}
