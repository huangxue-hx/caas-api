package com.harmonycloud.k8s.client;

import java.util.HashMap;
import java.util.Map;

import com.harmonycloud.dao.cluster.bean.Cluster;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.HttpK8SClientUtil;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author qg
 *
 */
@Component
public class K8sMachineClient {

	/**
	 * @param k8surl
	 * @param method
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public K8SClientResponse exec(K8SURL k8surl, String method, Map<String, Object> headers,
			Map<String, Object> bodys) {
		return this.exec(k8surl, method, headers, bodys, null);
	}

	/**
	 * @param k8surl
	 * @param method
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public K8SClientResponse exec(K8SURL k8surl, String method, Map<String, Object> headers,
								  Map<String, Object> bodys, Cluster cluster) {
		if(null == cluster) {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			HttpSession session;
			session = request.getSession();
			cluster = (Cluster) session.getAttribute("currentCluster");
		}

		k8surl.setHost(cluster.getHost());
		k8surl.setPort(cluster.getPort());
		k8surl.setMachineToken(cluster.getMachineToken());
		k8surl.setProtocol(cluster.getProtocol());

		String url = getUrl(k8surl);
		System.out.println(url);
		K8SClientResponse kr = new K8SClientResponse();

		if (headers == null) {
			headers = new HashMap<String, Object>();
		}
		headers.put("Authorization", "Bearer " + cluster.getMachineToken());

		try {
			switch (method) {
				case "GET":
					kr = HttpK8SClientUtil.httpGetRequest(url, headers, bodys);
					break;
				case "POST":
					kr = HttpK8SClientUtil.httpPostJsonRequest(url, headers, bodys);
					break;
				case "DELETE":
					kr = HttpK8SClientUtil.httpDeleteRequestForK8s(url, headers, bodys);
					break;
				case "PUT":
					kr = HttpK8SClientUtil.httpPutJsonRequest(url, headers, bodys);
					break;
				case "PATCH":
					kr = HttpK8SClientUtil.httpPatchJsonRequest(url, headers, bodys);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			kr.setStatus(500);
			kr.setBody(e.toString());
		}

		return kr;
	}

	/**
	 * 拼接url
	 * 
	 * @param url
	 * @return
	 */
	private String getUrl(K8SURL url) {

		String kubeHost = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
		StringBuffer sb = new StringBuffer(kubeHost);
		// StringBuffer sb = new StringBuffer("https://10.10.102.25:6443");

		if (url.getApiGroup() != null) {
			sb.append(url.getApiGroup());
		}
		if (Resource.NAMESPACE.equals(url.getResource())) {
			sb.append("/namespaces");
			if (url.getNamespace() != null) {
				sb.append("/").append(url.getNamespace());
			}
		} else {
			if (url.getNamespace() != null) {
				sb.append("/namespaces/").append(url.getNamespace());
			}
			if (url.getResource() != null) {
				sb.append("/").append(url.getResource());
			}
			if (url.getName() != null) {
				sb.append("/").append(url.getName());
			}
			if (url.getSubpath() != null) {
				sb.append("/").append(url.getSubpath());
			}
		}

		return sb.toString();
	}


}
