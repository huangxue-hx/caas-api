package com.harmonycloud.api.application;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.EsService;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.cluster.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * @author jmi
 *
 */
@RequestMapping("/getDeploy")
@Controller
public class GetDeploymentController {

	@Autowired
    private DeploymentsService dpService;

	@Autowired
    private EsService esService;
	@Autowired
	private HttpSession session;

	@Autowired
    private ClusterService clusterService;

	@Autowired
    private ServiceService serviceService;

	private Logger logger = LoggerFactory.getLogger(this.getClass());


	/**
	 * 获取应用详情
	 *
	 * @param name
	 * @param namespace
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{namespace}/{deployName}/yaml", method = RequestMethod.GET)
	public byte[] deploymentDetailYaml(@PathVariable(value = "namespace") String namespace, @PathVariable(value = "deployName") String name, HttpServletRequest request, HttpServletResponse response) throws Exception {

		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + name + ".yaml\"");
		OutputStream out;
		try {
			out = response.getOutputStream();
			//URL base = this.getClass().getResource(""); //先获得本类的所在位置，如/home/popeye/testjava/build/classes/net/
			//String path = new File(base.getFile(), "/"+name+".yaml").getCanonicalPath();
			String path="/home/"+namespace+"_"+name+".yaml";
			//String path="C:\\yaml\\"+namespace+"_"+name+".yaml";
		//	File yamlfile=new File(path);
			String yaml=dpService.getDeploymentDetailYaml(namespace, name,path);
			if("|".equals(yaml.substring(0,1)))
			{
				yaml=yaml.substring(1,yaml.length());
			}
			System.out.println(yaml);
			ByteArrayInputStream in = new ByteArrayInputStream(yaml.getBytes());
			int tempbyte;
			while ((tempbyte = in.read()) != -1) {
				out.write(tempbyte);
			}
			out.flush();
			out.close();
			//yamlfile.delete();
		} catch (IOException e1) {
			logger.warn("获取deployment:{} yaml失败, namespace:{}", name, namespace, e1);
		}
		return null;

	}

}
