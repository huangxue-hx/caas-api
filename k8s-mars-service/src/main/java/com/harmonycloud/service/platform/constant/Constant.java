package com.harmonycloud.service.platform.constant;

public class Constant {
	
	//dep状态：status code :0 ：stop 1:start 2:stopping 3:starting
	public final static String STOP = "0";
	
	public final static String START = "1";
	
	public final static String STOPPING = "2";
	
	public final static String STARTING = "3";

	public final static String SERVICE_STOP = "stopped";

	public final static String SERVICE_START = "started";

	public final static String SERVICE_STOPPING = "stopping";

	public final static String SERVICE_STARTING = "starting";
	
	public final static String WAITING = "waiting";
	
	public final static String RUNNING = "running";
	
	public final static String TERMINATED = "terminated";
	
	public final static String SECONDS = "seconds";
	
	public final static String MINUTES ="minutes";
	
	public final static String HOURS = "Hours";
	
	public final static String DAYS = "days";
	
	public final static String NETWORK_NUTON = "n";
	
	public final static String NETWORK_CALICO = "c";
	
	public final static String HARBOR = "harbor";
	
	public final static String INFLUXDB= "influxDB";
	
	public final static String ES="Elastic Search";
	
	public final static String HAPROXY = "HAproxy";
	
	public final static String NETWORK = "network";
	
	public final static String NOTCREATED = "not created";
	
	public final static String TYPE_SECRET = "secret";
	
	public final static String TYPE_PV = "pv";
	
	public final static String TYPE_GIT = "gitRepo";
	
	public final static String HARBORPROJECTROLE_DEV = "harbor_project_developer";
	
	public final static String HARBORPROJECTROLE_WATCHER = "harbor_project_watcher";

	public final static String HARBORPROJECTROLE_ADMIN = "harbor_project_admin";
	
	public final static String FLAGNAME = "user";
	
	public final static Integer HTTP_404 = 404;
	
	public final static String DEPLOYMENT = "Deployment";


	//application
	public final static double TEMPLATE_TAG = 1;

	public final static double TEMPLATE_TAG_INCREMENT = 0.1;

	public final static Integer TEMPLATE_STATUS_DELETE = 1;

	public final static Integer TEMPLATE_STATUS_CREATE = 0;

	public final static Integer EXTERNAL_SERVICE = 1;

	public final static Integer K8S_SERVICE = 0;

	//pvc
	public final static String PVC_BREAK = "-";

	public final static String STATUS_NORMAL = "0";
	public final static String STATUS_ABNORMAL = "1";
	
	//stroge
	public final static String VOLUME_TYPE_PV = "nfs";
	public final static String VOLUME_TYPE_HOSTPASTH = "hostPath";
	public final static String VOLUME_TYPE_GITREPO = "gitRepo";
	public final static String VOLUME_TYPE_EMPTYDIR = "emptyDir";


}
