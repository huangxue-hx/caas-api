package com.harmonycloud.service.common;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.LocalRolePreFieldDto;
import com.harmonycloud.common.enumm.PrivilegeField;
import com.harmonycloud.common.enumm.PrivilegeType;
import com.harmonycloud.dto.application.ApplicationDeployDto;
import com.harmonycloud.dto.application.ApplicationDto;
import com.harmonycloud.dto.cicd.PipelinePrivilegeDto;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 数据权限条件规则支持的自定义类型
 *
 */
public enum PrivilegeCustomTypeEnum {

//    PrivilegeApplicationFieldDto(PrivilegeApplicationFieldDto.class),
    ApplicationDeployDto(ApplicationDeployDto.class),
    ApplicationDto(ApplicationDto.class),
    PipelinePrivilegeDto(PipelinePrivilegeDto.class),
    ;

    private Class clz;
    private final static Map<String, List<LocalRolePreFieldDto>> ALL_CUSTOM_TYPE_DETAIL =null;
    private static List<LocalRolePreFieldDto> resourceTypes = null;

    <T> PrivilegeCustomTypeEnum(Class<T> clz) {
        this.clz = clz;
    }

    public static List<LocalRolePreFieldDto> listResourceTypes() {
        List<LocalRolePreFieldDto> resourceTypes = new ArrayList<>();
        PrivilegeCustomTypeEnum[] privilegeCustomTypeEnums = values();
        for (PrivilegeCustomTypeEnum privilegeCustomTypeEnum : privilegeCustomTypeEnums) {
            Annotation typeAnnotation = privilegeCustomTypeEnum.getClz().getDeclaredAnnotation(PrivilegeType.class);
            if (null == typeAnnotation) {
                continue;
            }
            PrivilegeType privilegeType = (PrivilegeType)typeAnnotation;
            LocalRolePreFieldDto localRolePreFieldDto = new LocalRolePreFieldDto();
            localRolePreFieldDto.setName(privilegeType.name());
            localRolePreFieldDto.setEnDesc(privilegeType.enDesc());
            localRolePreFieldDto.setCnDesc(privilegeType.cnDesc());
            resourceTypes.add(localRolePreFieldDto);
        }
        return resourceTypes;
    }

    public static Map<String, Set<LocalRolePreFieldDto>> listCustomTypes(){
        Map<String, Set<LocalRolePreFieldDto>> customTypes = new HashMap<>();
        PrivilegeCustomTypeEnum[] privilegeCustomTypeEnums = values();
        for (PrivilegeCustomTypeEnum privilegeCustomTypeEnum : privilegeCustomTypeEnums) {
            Annotation typeAnnotation = privilegeCustomTypeEnum.getClz().getDeclaredAnnotation(PrivilegeType.class);
            if (null == typeAnnotation){
                continue;
            }
            PrivilegeType privilegeType = (PrivilegeType)typeAnnotation;
            String type = privilegeType.name();
            Set<LocalRolePreFieldDto> customTypeFields = new HashSet<>();
            Field[] fields = privilegeCustomTypeEnum.getClz().getDeclaredFields();
            for (Field field : fields) {
                PrivilegeField annotation = field.getAnnotation(PrivilegeField.class);
                if (null == annotation){
                    continue;
                }
                LocalRolePreFieldDto localRolePreFieldDto = new LocalRolePreFieldDto();
                localRolePreFieldDto.setEnDesc(annotation.enDesc());
                localRolePreFieldDto.setCnDesc(annotation.cnDesc());
                if (!StringUtils.isAnyBlank(annotation.type())) {
                    localRolePreFieldDto.setName(annotation.type() + CommonConstant.DOT + annotation.name());
                    customTypeFields.add(localRolePreFieldDto);
                } else {
                    localRolePreFieldDto.setName(type + CommonConstant.DOT + annotation.name());
                    customTypeFields.add(localRolePreFieldDto);

                }
            }
            if (customTypes.containsKey(type)){
                // 字段名称如果重复，则只保留一个，这意味着不同类可以定义同一个条件，只要类注解名称和字段注解名称相同
                customTypes.get(type).addAll(customTypeFields);
            } else {
                customTypes.put(type, customTypeFields);
            }
        }
        return customTypes;
    }

    public static List<Class> listClz(){
        List<Class> clzz = new ArrayList<>();
        PrivilegeCustomTypeEnum[] enums = values();
        for (PrivilegeCustomTypeEnum curEnum : enums) {
            clzz.add(curEnum.getClz());
        }
        return clzz;
    }

    public static List<LocalRolePreFieldDto> listFields(String customType){
        return ALL_CUSTOM_TYPE_DETAIL.get(customType);
    }

    public <T> Class getClz() {
        return clz;
    }
}
