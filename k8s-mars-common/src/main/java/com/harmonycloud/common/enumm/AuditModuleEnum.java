package com.harmonycloud.common.enumm;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author jiangmi
 * @Description 操作审计定义模块
 * @Date created in 2018-1-10
 * @Modified
 */
public enum AuditModuleEnum {

    USER("用户", "user"),
    APP_TEMPLATE("应用模板", "appTemplate"),
    APP_STORE("应用商店", "App Store"),
    APP("应用", "app"),
    SERVICE_TEMPLATE("服务模板", "serviceTemplate"),
    YAML("yaml模板", "yamlTemplate"),
    SERVICE("服务", "service"),
    DAEMONSET("守护进程服务", "daemonset"),
    TENANT("租户", "tenant"),
    PROJECT("项目", "project"),
    ROLE("角色", "role"),
    EXTERNAL_SERVICE("外部服务", "externalService"),
    NAMESPACE("分区", "namespace"),
    STORAGE("存储", "storage"),
    HARBOR("镜像仓库", "repository"),
    CICD("CICD", "CICD"),
   /* MSF("微服务平台", "microServicePlatform"),
    CDP("持续交付平台", "Continue Deliver Platform"),*/
    CLUSTER("集群", "cluster"),
    LOG("日志管理", "Log Management"),
    ALARM("告警", "alarm"),
    CONFIG_CENTET("配置中心", "Config Center");

    private final String chDesc;
    private final String enDesc;

    private static List<String> chModuleDesc = new ArrayList<>();
    private static List<String> enModuleDesc = new ArrayList<>();

    static {
        for (AuditModuleEnum module : EnumSet.allOf(AuditModuleEnum.class)) {
            chModuleDesc.add(module.getChDesc());
            enModuleDesc.add(module.getEnDesc());
        }
        String regex = "[a-zA-Z]+";
        Pattern pattern = Pattern.compile(regex);
        Collections.sort(chModuleDesc, new Comparator<String>(){
            //中文描述先根据字符串长度进行排序，字符串长度相同的，包含英文字母的排在前面
            @Override
            public int compare(String module1, String module2) {
                int len1 = module1.length();
                int len2 = module2.length();
                if(len1 != len2) {
                    return len1 - len2;
                }else {
                    Matcher matcher1 = pattern.matcher(module1);
                    Matcher matcher2 = pattern.matcher(module2);
                    if (matcher1.find() || matcher2.find()) {
                        return module1.compareTo(module2);
                    }else{
                        return 0;
                    }
                }
            }
        });
        Collections.sort(enModuleDesc, new Comparator<String>(){
            //英文描述根据字符串长度进行排序
            @Override
            public int compare(String module1, String module2) {
                return module1.length() - module2.length();
            }
        });
    }

    AuditModuleEnum(String chDesc, String enDesc) {
        this.chDesc = chDesc;
        this.enDesc = enDesc;
    }

    public static List<String> getAllChDesc(){
        return chModuleDesc;
    }

    public static List<String> getAllEnDesc(){
        return enModuleDesc;
    }

    public String getChDesc() {
        return chDesc;
    }

    public String getEnDesc() {
        return enDesc;
    }
}
