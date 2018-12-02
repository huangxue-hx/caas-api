package com.harmonycloud.service.platform.client;

import com.harmonycloud.common.util.HttpClientResponse;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.bean.cluster.ClusterGit;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmi on 18-9-17.
 */
public class GitClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitClient.class);

    private static String istioPolicyProjectId;

    public static String getGitUrl(ClusterGit gitServer) {
//        return "https://10.10.120.147:443/api/v3";
        return gitServer.getProtocol() + "://" + gitServer.getAddress() + ":" + gitServer.getPort() + "/api/v3";
    }

    public static String getGitPrivateToken(ClusterGit gitServer) {
//        String privateToken = "HwtU9vzWEwxcLpK1uzGS";
        String privateToken = gitServer.getPrivateToken();
        return privateToken;
    }

    public static String getProjectId(ClusterGit gitServer) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        if (StringUtils.isNotEmpty(istioPolicyProjectId)) {
            return istioPolicyProjectId;
        }
//        String url = getGitUrl(gitServer) + "/projects?search=CAAS_ISTIO_SWITCH";
        String url = getGitUrl(gitServer) + "/projects?search=" + gitServer.getProject();
        Map<String, Object> header = new HashMap<>();
        header.put("PRIVATE-TOKEN", getGitPrivateToken(gitServer));
        HttpClientResponse response = HttpClientUtil.doGet(url, null, header);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return null;
        }
        istioPolicyProjectId = JSONArray.fromObject(response.getBody()).getJSONObject(0).getString("id");
        return istioPolicyProjectId;
    }


}
