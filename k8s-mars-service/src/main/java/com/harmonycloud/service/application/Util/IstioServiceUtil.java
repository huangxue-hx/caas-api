package com.harmonycloud.service.application.Util;

import com.harmonycloud.service.istio.IstioCommonService;
import org.apache.commons.lang3.StringUtils;

public class IstioServiceUtil {

    private static IstioServiceUtil istioServiceUtil = null;

    private static IstioCommonService istioCommonService;

    private IstioServiceUtil() {}

    private IstioServiceUtil(IstioCommonService istioCommonService) {
        if (this.istioCommonService == null) {
            this.istioCommonService = istioCommonService;
        }
    }

    public static IstioServiceUtil getInstance(IstioCommonService istioCommonService) {
        if (istioServiceUtil == null || istioServiceUtil.istioCommonService == null) {
            istioServiceUtil = new IstioServiceUtil(istioCommonService);
        }
        return istioServiceUtil;
    }

    public void updateDestinationRule(String namespace, String name, String version, boolean isBlueGreen) throws Exception {
        boolean isIstioNamespace = istioCommonService.isIstioEnabled(namespace);
        if (isIstioNamespace) {
            version = StringUtils.isNotBlank(version) ? version : "unknown";
            istioCommonService.updateDestinationRule(name, namespace, version, isBlueGreen);
        }
    }
}
