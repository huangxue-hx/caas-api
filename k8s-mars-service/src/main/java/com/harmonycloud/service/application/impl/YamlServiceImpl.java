package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.YamlDto;
import com.harmonycloud.dto.tenant.show.NamespaceShowDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.TprApplication;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.YamlService;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 8/11/17.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class YamlServiceImpl implements YamlService{
    @Autowired
    HttpSession session;

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    private TprApplication tprApplication;

    @Autowired
    private DeploymentService dpService;

    final static String SPLIT = "---";
    final static String COLON = ":";

    final static String KIND = "kind:";
    final static String NAMESPACE = "  namespace:";
    final static String NAME = "  name:";

    final static String MAP_NAMESPACE = "namespace";
    final static String MAP_KIND = "kind";
    final static String MAP_DATA = "data";
    final static String MAP_NAME = "name";

    private static final String SIGN = "-";
   // private static final String SIGN_EQUAL = "=";
    private final static String TOPO = "topo";
    private final static String CREATE = "creater";


    @Override
    public ActionReturnUtil deployYaml(YamlDto yamlDto) throws Exception {

        if (!(yamlDto != null && yamlDto.getYaml() != null && yamlDto.getAppName() != null)){
            return ActionReturnUtil.returnErrorWithMsg("yaml 和 application 不能为空!");
        }

        ActionReturnUtil namespacesrep =  namespaceService.getNamespaceList(yamlDto.getTentantID(), yamlDto.getTentantName());
        if(!namespacesrep.isSuccess()){
            return ActionReturnUtil.returnErrorWithMsg("namespace获取失败");
        }
        @SuppressWarnings("unchecked")
		List<NamespaceShowDto> namespaceList = (List<NamespaceShowDto>) namespacesrep.get("data");
        Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        Map<String,String> mes = new HashMap<String,String>();
        String tenantId = session.getAttribute("tenantId").toString();
        String username = session.getAttribute("username").toString();

        List<Map<String,Object>> data = convertYaml(yamlDto.getYaml());

        Yaml yaml = new Yaml();

        String topoLabel = TOPO + SIGN + tenantId + SIGN + yamlDto.getAppName();
        String namespaceLabel = "";

        boolean flagTPR = true;
        boolean flagApp = false;

        if (yamlDto.getAppName() != null){
            flagApp = true;
        }
        int i = 0;
        for(Map<String,Object> oneData:data){
            i++;
            if(oneData.get(MAP_KIND) != null && oneData.get(MAP_NAMESPACE) != null && oneData.get(MAP_NAME) != null){

                if (flagTPR && flagApp){
                    namespaceLabel = oneData.get(MAP_NAMESPACE).toString().replace(" ", "");
                    BaseResource base = new BaseResource();
                    ObjectMeta mate = new ObjectMeta();
                    mate.setNamespace(oneData.get(MAP_NAMESPACE).toString().replace(" ", ""));
                    mate.setName(yamlDto.getAppName());

                    Map<String, Object> anno = new HashMap<String, Object>();
                    anno.put("nephele/annotation",yamlDto.getAppDesc());
                    mate.setAnnotations(anno);

                    Map<String, Object> appLabels = new HashMap<String, Object>();
                    appLabels.put(topoLabel,namespaceLabel);
                    appLabels.put(CREATE ,username);
                    mate.setLabels(appLabels);

                    base.setMetadata(mate);
                    ActionReturnUtil result = tprApplication.createApplication(base,cluster);

                    if (result.isSuccess()){
                        flagTPR = false;
                    } else {
                        return ActionReturnUtil.returnErrorWithData(result.get("data"));
                    }
                }

                boolean flag = false;
                if (namespaceList != null && namespaceList.size() > 0){
                    for (NamespaceShowDto oneNamespace:namespaceList){
                        if ((oneData.get(MAP_NAMESPACE).toString().replace(" ", "")).equals(oneNamespace.getName())){
                            flag =  true;
                            break;
                        }
                    }
                    //deploy
                    if (flag){
                        K8SURL url = new K8SURL();
                        String namesp = oneData.get(MAP_NAMESPACE).toString().replace(" ", "");
                        String kind = oneData.get(MAP_KIND).toString().replace(" ", "").toLowerCase()+"s";
                        // url.setNamespace(oneData.get(MAP_NAMESPACE).toString().replace(" ", "")).setResource(oneData.get(MAP_KIND).toString().replace(" ", "").toLowerCase()+"s");
                        url.setNamespace(namesp).setResource(kind);
                        Map<String, Object> headers = new HashMap<String, Object>();
                        headers.put("Content-type", "application/json");
                        @SuppressWarnings("rawtypes")
						Map bodys = (Map) yaml.load(oneData.get(MAP_DATA).toString());
                        @SuppressWarnings("unchecked")
						K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
                        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                            mes.put("数据" +i+ "创建失败:",status.getMessage()+ "\n");
                        } else if (flagApp){
                            String devName =  oneData.get(MAP_NAME).toString().replace(" ","");
                            //todo add labe deployment svc topo, other app
                            if ("services".equals(kind)){
                                url = new K8SURL();
                                url.setNamespace(namesp).setResource(Resource.SERVICE).setName(devName);
                                K8SClientResponse serRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
                                if (!HttpStatusUtil.isSuccessStatus(serRes.getStatus())
                                        && serRes.getStatus() != Constant.HTTP_404 ) {
                                    UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(serRes.getBody(), UnversionedStatus.class);
                                    mes.put("数据" +i+ "更新失败:",k8sresbody.getMessage()+ "\n");
                                }
                                com.harmonycloud.k8s.bean.Service svc = JsonUtil.jsonToPojo(serRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
                                    if(svc != null){
                                            if(svc != null && svc.getMetadata() != null && svc.getMetadata().getLabels() != null){
                                                Map<String, Object> labels = new HashMap<String, Object>();
                                                labels = svc.getMetadata().getLabels();
                                                labels.put(topoLabel, namesp);
                                                svc.getMetadata().setLabels(labels);
                                                Map<String, Object> bodyss = new HashMap<String, Object>();
                                                bodyss = CollectionUtil.transBean2Map(svc);
                                                Map<String, Object> headerss = new HashMap<String, Object>();
                                                headerss.put("Content-type", "application/json");
                                                K8SClientResponse newRes = new K8sMachineClient().exec(url, HTTPMethod.PUT, headerss, bodyss, cluster);
                                                if(!HttpStatusUtil.isSuccessStatus(newRes.getStatus())){
                                                    UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(newRes.getBody(), UnversionedStatus.class);
                                                    mes.put("数据" +i+ "更新失败:",k8sresbody.getMessage()+ "\n");
                                                }
                                            }
                                    }

                            } else if ("deployments".equals(kind)){
                                //更新Deployment label
                                K8SURL urld = new K8SURL();
                                urld.setNamespace(namesp).setResource(Resource.DEPLOYMENT).setName(devName);
                                K8SClientResponse depRes = new K8sMachineClient().exec(urld, HTTPMethod.GET, null, null,cluster);
                                if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                                        && depRes.getStatus() != Constant.HTTP_404 ) {
                                    UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                                    mes.put("数据" +i+ "更新失败:",k8sresbody.getMessage()+ "\n");
                                }
                                Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
                                if(dep != null){
                                            if(dep != null && dep.getMetadata() != null && dep.getMetadata().getLabels() != null){
                                                Map<String, Object> labels = new HashMap<String, Object>();
                                                labels = dep.getMetadata().getLabels();
                                                labels.put(topoLabel, namesp);
                                                dep.getMetadata().setLabels(labels);
                                                Map<String, Object> bodysd = new HashMap<String, Object>();
                                                bodysd = CollectionUtil.transBean2Map(dep);
                                                Map<String, Object> headersd = new HashMap<String, Object>();
                                                headersd.put("Content-type", "application/json");
                                                K8SClientResponse newRes = dpService.doSpecifyDeployment(namesp, devName, headersd, bodysd, HTTPMethod.PUT, cluster);
                                                if(!HttpStatusUtil.isSuccessStatus(newRes.getStatus())){
                                                    UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(newRes.getBody(), UnversionedStatus.class);
                                                    mes.put("数据" +i+ "更新失败:",k8sresbody.getMessage()+ "\n");
                                                }
                                            }
                                }
                            }

                        }
                        //mes.put( oneData.get(MAP_KIND).toString().replace(" ","") + ":" +oneData.get(MAP_NAME).toString().replace(" ", ""),"创建成功!\n");

                    } else {
                        mes.put("namespace" + oneData.get(MAP_NAMESPACE).toString().replace(" ", "") + ":", "不属于该租户! \n");
                    }
                }

            } else {
                mes.put("数据" +i+ ":","数据不合法"+ "\n");
            }
        }
        if (mes!= null && mes.keySet() != null && mes.keySet().size() > 0){
            return ActionReturnUtil.returnErrorWithData(mes);
        }
        return ActionReturnUtil.returnSuccess();

    }


    private List<Map<String,Object>> convertYaml(String yaml) throws Exception {
        ByteArrayInputStream is=new ByteArrayInputStream(yaml.getBytes());
        BufferedReader br=new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String line = "";
        Map<String,Object> convertBody = new HashMap<String,Object>();
        List<Map<String,Object>> allData = new ArrayList<>();
        try {
            while((line = br.readLine())!=null){
                if (line.startsWith(KIND)){
                    String[] kind = line.split(COLON);
                    if (kind != null && kind.length >= 2){
                        convertBody.put(MAP_KIND,kind[1]);
                    }
                }

                if (line.startsWith(NAMESPACE)){
                    String[] kind = line.split(COLON);
                    if (kind != null && kind.length >= 2){
                        convertBody.put(MAP_NAMESPACE,kind[1]);
                    }
                }

                if (line.startsWith(NAME)){
                    String[] kind = line.split(COLON);
                    if (kind != null && kind.length >= 2){
                        convertBody.put(MAP_NAME,kind[1]);
                    }
                }

                if (line.startsWith(SPLIT)){
                    convertBody.put(MAP_DATA,sb.toString());
                    Map<String,Object> newConvertBody = new HashMap<String,Object>();

                    if (convertBody.get(MAP_NAME) != null){
                        newConvertBody.put(MAP_NAME,convertBody.get(MAP_NAME));
                    }
                    if (convertBody.get(MAP_NAMESPACE) != null){
                        newConvertBody.put(MAP_NAMESPACE,convertBody.get(MAP_NAMESPACE));
                    }
                    if (convertBody.get(MAP_KIND) != null){
                        newConvertBody.put(MAP_KIND,convertBody.get(MAP_KIND));
                    }
                    if (convertBody.get(MAP_DATA) != null){
                        newConvertBody.put(MAP_DATA,convertBody.get(MAP_DATA));
                    }

                    allData.add(newConvertBody);

                    //clean sb
                    sb.setLength(0);
                    convertBody.clear();
                }

                if (!line.startsWith(SPLIT)){
                    sb.append(line + "\n");
                }



            }
            if (sb.length() > 0){
                convertBody.put(MAP_DATA,sb.toString());
                allData.add(convertBody);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
            is.close();
        }
        return allData;
    }

}