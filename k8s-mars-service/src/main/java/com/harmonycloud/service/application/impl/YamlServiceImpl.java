package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.YamlDto;
import com.harmonycloud.dto.tenant.show.NamespaceShowDto;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.YamlService;
import com.harmonycloud.service.tenant.NamespaceService;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
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

    final static String SPLIT = "---";
    final static String COLON = ":";

    final static String KIND = "kind:";
    final static String NAMESPACE = "  namespace:";
    final static String NAME = "  name:";

    final static String MAP_NAMESPACE = "namespace";
    final static String MAP_KIND = "kind";
    final static String MAP_DATA = "data";
    final static String MAP_NAME = "name";

    @Override
    public ActionReturnUtil deployYaml(YamlDto yamlDto) throws Exception {

        if (!(yamlDto != null && yamlDto.getYaml() != null && yamlDto.getAppName() != null)){
            return ActionReturnUtil.returnErrorWithMsg("yaml 和 application 不能为空!");
        }

        ActionReturnUtil namespacesrep =  namespaceService.getNamespaceList(yamlDto.getTentantID(), yamlDto.getTentantName());
        if(!namespacesrep.isSuccess()){
            return ActionReturnUtil.returnErrorWithMsg("namespace获取失败");
        }
        List<NamespaceShowDto> namespaceList = (List<NamespaceShowDto>) namespacesrep.get("data");
        Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        Map<String,String> mes = new HashedMap();

        List<Map<String,Object>> data = convertYaml(yamlDto.getYaml());

        Yaml yaml = new Yaml();

        for(Map<String,Object> oneData:data){
            if(oneData.get(MAP_KIND) != null && oneData.get(MAP_NAMESPACE) != null && oneData.get(MAP_NAME) != null){
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
                        Map bodys = (Map) yaml.load(oneData.get(MAP_DATA).toString());
                        K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.POST, headers, bodys, cluster);
                        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                            mes.put("创建失败:",status.getMessage()+ "\n");
                            return ActionReturnUtil.returnErrorWithData(mes);
                        }
                        mes.put( oneData.get(MAP_KIND).toString().replace(" ","") + ":" +oneData.get(MAP_NAME).toString().replace(" ", ""),"创建成功!\n");

                    } else {
                        mes.put("namespace" + oneData.get(MAP_NAMESPACE).toString().replace(" ", "") + ":", "不属于该租户! \n");
                    }
                }

            }
        }

        return ActionReturnUtil.returnSuccessWithData(mes);
    }


    private List<Map<String,Object>> convertYaml(String yaml) throws Exception {

        String[] instance = yaml.split(SPLIT);


        ByteArrayInputStream is=new ByteArrayInputStream(yaml.getBytes());
        BufferedReader br=new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String line = "";
        Map<String,Object> convertBody = new HashedMap();
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
                    Map<String,Object> newConvertBody = new HashedMap();

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