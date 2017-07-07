package com.harmonycloud.k8s.client;

import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.HttpK8SClientUtil;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qg
 *
 */
@Component
public class K8SClient {

	private HttpSession session;


	/**
	 * 保存全局token信息
	 */
	public static Map<String, Object> tokenMap = new ConcurrentHashMap<String, Object>();
	/**
	 * @param k8surl
	 * @param method
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public K8SClientResponse doit(K8SURL k8surl, String method, /*String token,*/ Map<String, Object> headers,
			Map<String, Object> bodys,Cluster cluster) throws Exception {
		String token= null;
		if(null == cluster){
		    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	        session = request.getSession();
		    cluster = (Cluster) session.getAttribute("currentCluster");
		    token = this.getK8sToken();
		}
		k8surl.setHost(cluster.getHost());
		k8surl.setPort(cluster.getPort());
		k8surl.setMachineToken(cluster.getMachineToken());
		k8surl.setProtocol(cluster.getProtocol());

		String url = getUrl(k8surl);
		K8SClientResponse kr = new K8SClientResponse();
		if (headers == null) {
			headers = new HashMap<String, Object>();
		}
		if(headers != null && !headers.containsKey("Authorization")){
			token = this.getK8sToken();
			headers.put("Authorization", "Bearer " + token);
		}
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
			throw e;
		}

		return kr;
	}
	public K8SClientResponse doit(K8SURL k8surl, String method, /*String token,*/ Map<String, Object> headers,
            Map<String, Object> bodys) throws Exception {
		//todo nullpointer exception
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        session = request.getSession();
        Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        k8surl.setHost(cluster.getHost());
        k8surl.setPort(cluster.getPort());
        k8surl.setMachineToken(cluster.getMachineToken());
        k8surl.setProtocol(cluster.getProtocol());

        String url = getUrl(k8surl);
        K8SClientResponse kr = new K8SClientResponse();
        if (headers == null) {
            headers = new HashMap<String, Object>();
        }
        if(headers != null && !headers.containsKey("Authorization")){
            String token = this.getK8sToken();
            headers.put("Authorization", "Bearer " + token);
        }
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
            throw e;
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

		sb.append(url.getApiGroup());
		
		if(!StringUtils.isEmpty(url.getWatch())){
			sb.append(url.getWatch());
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
		String substring = "";
		if (url.getQueryParams() != null && !url.getQueryParams().isEmpty()) {
			sb.append("?");
			for (Map.Entry<String, Object> param : url.getQueryParams().entrySet()) {
				sb.append(param.getKey()+"="+String.valueOf(param.getValue())+"&");
			}
			substring = sb.substring(0,sb.length()-1);
		}else{
			substring = sb.toString();
		}

		return substring;
	}

	/**
	 * 将response转换为bean
	 * 
	 * @param response
	 * @param clazz
	 * @return
	 */
	public static <T> T converToBean(K8SClientResponse response, Class<T> clazz) {
		if (response != null && HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			String body = response.getBody();
			if (body != null) {
				return JsonUtil.jsonToPojo(body, clazz);
			}
		}
		return null;
	}

	
	/**
	 * 获取当前用户的token
	 * @return
	 * @throws Exception 
	 */
	public String getK8sToken(){
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		session = request.getSession();
		String username = String.valueOf(session.getAttribute("username"));
		String token = String.valueOf(tokenMap.get(username));
		return token;
		/*return "330957b867a3462ea457bec41410624b";*/
	}
	
	
	/*public static void main(String[] args) {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("authorization", "Bearer 330957b867a3462ea457bec41410624b");
		K8SURL url = new K8SURL();
		url.setNamespace("gywtenant-gyw").setResource(Resource.ROLEBINDING);
		K8SClientResponse k = new K8SClient().doit(url, HTTPMethod.GET, "330957b867a3462ea457bec41410624b", null, null);
		System.out.println(JsonUtil.convertToJsonNonNull(
				(new K8SClient()).doit(url, HTTPMethod.GET, "330957b867a3462ea457bec41410624b", null, null).getBody()));
		RoleBindingList r = K8SClient.converToBean(k, RoleBindingList.class);
		System.out.println(r);
	}*/
}
