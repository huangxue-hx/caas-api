//package com.harmonycloud.api.application;
//
//import com.harmonycloud.common.exception.K8sAuthException;
//import com.harmonycloud.common.util.ActionReturnUtil;
//import com.harmonycloud.dao.cluster.bean.Cluster;
//import com.harmonycloud.k8s.bean.NFSVolumeSource;
//import com.harmonycloud.k8s.bean.ObjectMeta;
//import com.harmonycloud.k8s.bean.PersistentVolume;
//import com.harmonycloud.k8s.bean.PersistentVolumeSpec;
//import com.harmonycloud.k8s.constant.Constant;
//import com.harmonycloud.service.application.VolumeSerivce;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.servlet.http.HttpSession;
//
///**
// *
// * @author jmi
// *
// */
//
//@Controller
//public class StorageController {
//
//	@Autowired
//	private VolumeSerivce volumeSerivce;
//
//	@Autowired
//    HttpSession session;
//	/**
//	 * 用户存储列表
//	 *
//	 * @param namespace
//	 * @return
//	 */
//	@ResponseBody
//	@RequestMapping(value = "/volume", method = RequestMethod.GET)
//	public ActionReturnUtil volumeList(@RequestParam(value = "namespace") String namespace) throws Exception{
//
//		try {
//			String userName = (String) session.getAttribute("username");
//	        if(userName == null){
//				throw new K8sAuthException(Constant.HTTP_401);
//			}
//			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
//			return volumeSerivce.listVolume(namespace, cluster);
//		} catch (Exception e) {
//			throw e;
//		}
//	}
//
//	@ResponseBody
//	@RequestMapping(value = "/volume", method = RequestMethod.POST)
//	public ActionReturnUtil volumeCreate(@RequestParam(value = "namespace") String namespace, @RequestParam(value="name") String name,
//                                         @RequestParam(value="capacity") String capacity, @RequestParam(value="tenantid") String tenantid,
//                                         @RequestParam(value="readonly") String readonly, @RequestParam(value="bindOne") String bindOne, @RequestParam(value="pvName")String pvname) throws Exception{
//
//		try {
//			return volumeSerivce.createVolume(namespace, name, capacity, tenantid, readonly, bindOne,pvname);
//		} catch (Exception e) {
//			throw e;
//		}
//	}
//
//	@ResponseBody
//	@RequestMapping(value = "/volume", method = RequestMethod.DELETE)
//	public ActionReturnUtil volumeDelete(@RequestParam(value = "namespace") String namespace, @RequestParam(value="name") String name) throws Exception{
//
//		try {
//			return volumeSerivce.deleteVolume(namespace, name);
//		} catch (Exception e) {
//			throw e;
//		}
//	}
//
//	/**
//	 * pv列表
//	 *
//	 * @return
//	 */
//	@ResponseBody
//	@RequestMapping(value = "/volumeBytenantid", method = RequestMethod.GET)
//	public ActionReturnUtil listVolumeBytenant(@RequestParam(value = "tenantid", required = false) String tenantid) throws Exception{
//		try {
//			if (StringUtils.isNotEmpty(tenantid)) {
//				return volumeSerivce.listVolumeBytenantid(tenantid);
//			} else {
//				return this.volumeSerivce.listVolume();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw e;
//		}
//	}
//
//	/**
//	 * 新增pv
//	 *
//	 * @return
//	 */
//	@ResponseBody
//	@RequestMapping(value = "/volumeBytenantid", method = RequestMethod.POST)
//	public ActionReturnUtil addVolumeBytenant(@RequestParam(value = "name") final String name,
//                                              @RequestParam(value = "tenantid") final String tenantid,
//                                              @RequestParam(value = "providerName") final String providerName,
//                                              @RequestParam(value = "type") final String type, @RequestParam(value = "capacity") final String capacity,
//                                              @RequestParam(value = "readOnly") final boolean readOnly,
//                                              @RequestParam(value = "multiple") final boolean multiple) throws Exception {
//        try {
//        	PersistentVolume persistentVolume = new PersistentVolume();
//    		// 设置metadata
//    		ObjectMeta metadata = new ObjectMeta();
//    		metadata.setName(name);
//    		Map<String, Object> labels = new HashMap<>();
//    		labels.put("nephele_tenantid_" + tenantid, tenantid);
//    		labels.put("nephele_tenantid_" + tenantid + name, tenantid + name);
//    		metadata.setLabels(labels);
//    		// 设置spec
//    		PersistentVolumeSpec spec = new PersistentVolumeSpec();
//    		Map<String, Object> cap = new HashMap<>();
//    		cap.put("storage", capacity + "Mi");
//    		spec.setCapacity(cap);
//    		spec.setPersistentVolumeReclaimPolicy("Recycle");
//    		NFSVolumeSource nfs = new NFSVolumeSource();
//    		// TODO 先写死,之后修改为从配置文件中读取
//    		nfs.setPath("/nfs");
//    		nfs.setServer("10.10.102.25");
//    		spec.setNfs(nfs);
//    		List<String> accessModes = new ArrayList<>();
//    		if (readOnly == true && multiple == true) {
//    			accessModes.add("ReadOnlyMany");
//    		}
//    		if (readOnly == false && multiple == true) {
//    			accessModes.add("ReadWriteMany");
//    		}
//    		if (readOnly == false && multiple == false) {
//    			accessModes.add("ReadWriteOnce");
//    		}
//    		spec.setAccessModes(accessModes);
//    		persistentVolume.setMetadata(metadata);
//    		persistentVolume.setSpec(spec);
//    		persistentVolume.setApiVersion("v1");
//    		persistentVolume.setKind("PersistentVolume");
//    		return volumeSerivce.addVolume(persistentVolume);
//		} catch (Exception e) {
//			throw e;
//		}
//
//	}
//
//	/**
//	 * 删除pv
//	 * @return
//	 */
//	@ResponseBody
//	@RequestMapping(value = "/volumeBytenantid/delete",method=RequestMethod.DELETE)
//	public ActionReturnUtil delVolumeBytenant(@RequestParam(value="tenantid",required=false) String tenantid, @RequestParam(value="name") String name) throws Exception{
//		return this.volumeSerivce.delVolume(name);
//	}
//
//	/**
//	 * 获取pvc
//	 * @param name pvc名称
//	 * @param namespace
//	 * @return
//	 */
//	@ResponseBody
//	@RequestMapping(value="/volume/detail",method=RequestMethod.GET)
//	public ActionReturnUtil getPvcDetail(@RequestParam(value="name") String name, @RequestParam(value="namespace") String namespace) throws Exception{
//		return this.volumeSerivce.getPvc(name, namespace);
//	}
//
//	/**
//	 * pv类型列表
//	 * @return
//	 */
//	@ResponseBody
//	@RequestMapping(value="/volumeprovider/list",method=RequestMethod.GET)
//	public ActionReturnUtil listVolumeprovider()throws Exception{
//		try {
//			return this.volumeSerivce.listVolumeprovider();
//		} catch (Exception e) {
//			throw e;
//		}
//
//	}
//}
