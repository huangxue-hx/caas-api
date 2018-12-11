package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HTTPMatchRequest {

    private StringMatch uri;

    private StringMatch scheme;

    private StringMatch method;

    private StringMatch authority;

    private Map<String, StringMatch> headers;

    private Integer port;

    private Map<String, String> sourceLabels;

    private List<String> gateways;

    public StringMatch getUri() {
        return uri;
    }

    public void setUri(StringMatch uri) {
        this.uri = uri;
    }

    public StringMatch getScheme() {
        return scheme;
    }

    public void setScheme(StringMatch scheme) {
        this.scheme = scheme;
    }

    public StringMatch getMethod() {
        return method;
    }

    public void setMethod(StringMatch method) {
        this.method = method;
    }

    public StringMatch getAuthority() {
        return authority;
    }

    public void setAuthority(StringMatch authority) {
        this.authority = authority;
    }

    public Map<String, StringMatch> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, StringMatch> headers) {
        this.headers = headers;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Map<String, String> getSourceLabels() {
        return sourceLabels;
    }

    public void setSourceLabels(Map<String, String> sourceLabels) {
        this.sourceLabels = sourceLabels;
    }

    public List<String> getGateways() {
        return gateways;
    }

    public void setGateways(List<String> gateways) {
        this.gateways = gateways;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        HTTPMatchRequest httpMatchRequest = (HTTPMatchRequest) o;
        if (this.uri != null) {
            if (!this.uri.equals(httpMatchRequest.uri)) {
                return false;
            }
        } else {
            if (httpMatchRequest.uri != null) {
                return false;
            }
        }
        if (this.scheme != null) {
            if (!this.scheme.equals(httpMatchRequest.scheme)) {
                return false;
            }
        } else {
            if (httpMatchRequest.scheme != null) {
                return false;
            }
        }
        if (this.method != null) {
            if (!this.method.equals(httpMatchRequest.method)) {
                return false;
            }
        } else {
            if (httpMatchRequest.method != null) {
                return false;
            }
        }
        if (this.authority != null) {
            if (!this.authority.equals(httpMatchRequest.authority)) {
                return false;
            }
        } else {
            if (httpMatchRequest.authority != null) {
                return false;
            }
        }
        if (this.headers != null && this.headers.size() > 0) {
            if (httpMatchRequest.headers == null || httpMatchRequest.headers.size() == 0) {
                return false;
            } else if (!checkMapHeaders(this.headers, httpMatchRequest.headers)) {
                return false;
            }
        } else {
            if (httpMatchRequest.headers != null && httpMatchRequest.headers.size() > 0) {
                return false;
            }
        }
        if (this.port != null) {
            if (httpMatchRequest.port == null) {
                return false;
            } else if (this.port.intValue() != httpMatchRequest.port.intValue()) {
                return false;
            }
        } else {
            if (httpMatchRequest.port != null) {
                return false;
            }
        }
        if (this.sourceLabels != null && this.sourceLabels.size() > 0) {
            if (httpMatchRequest.sourceLabels == null || httpMatchRequest.sourceLabels.size() == 0) {
                return false;
            } else if (!checkMapSouceLabels(this.sourceLabels, httpMatchRequest.sourceLabels)) {
                return false;
            }
        } else {
            if (httpMatchRequest.sourceLabels != null && httpMatchRequest.sourceLabels.size() > 0) {
                return false;
            }
        }
        if (!CollectionUtils.isEmpty(this.gateways)) {
            if (CollectionUtils.isEmpty(httpMatchRequest.gateways)) {
                return false;
            } else if (!checkList(this.gateways, httpMatchRequest.gateways)) {
                return false;
            }
        } else {
            if (!CollectionUtils.isEmpty(httpMatchRequest.gateways)) {
                return false;
            }
        }
        return true;
    }


    private boolean checkMapHeaders(Map<String, StringMatch> thisMap, Map<String, StringMatch> objMap){
        if (thisMap.size() != objMap.size()) {
            return false;
        }
        Set<String> keyStrs = thisMap.keySet();
        for (String key : keyStrs) {
            if (!thisMap.get(key).equals(objMap.get(key))) {
                return false;
            }
        }
        return true;
    }

    private boolean checkMapSouceLabels(Map<String, String> thisMap, Map<String, String> objMap){
        if (thisMap.size() != objMap.size()) {
            return false;
        }
        Set<String> keyStrs = thisMap.keySet();
        for (String key : keyStrs) {
            if (!thisMap.get(key).equals(objMap.get(key))) {
                return false;
            }
        }
        return true;
    }

    private boolean checkList(List<String> thisMatch, List<String> objMatch) {
        if (thisMatch.size() != objMatch.size()) {
            return false;
        }
        for (int i = 0; i < thisMatch.size(); i++) {
            if (!thisMatch.get(i).equals(objMatch.get(i))) {
                return false;
            }
        }
        return true;
    }



    @Override
    public int hashCode() {
        int result = getUri() != null ? getUri().hashCode() : 0;
        result = 31 * result + (getScheme() != null ? getScheme().hashCode() : 0);
        result = 31 * result + (getMethod() != null ? getMethod().hashCode() : 0);
        result = 31 * result + (getAuthority() != null ? getAuthority().hashCode() : 0);
        result = 31 * result + (getHeaders() != null ? getHeaders().hashCode() : 0);
        result = 31 * result + (getPort() != null ? getPort().hashCode() : 0);
        result = 31 * result + (getSourceLabels() != null ? getSourceLabels().hashCode() : 0);
        result = 31 * result + (getGateways() != null ? getGateways().hashCode() : 0);
        return result;
    }
}
