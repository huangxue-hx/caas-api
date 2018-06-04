package com.harmonycloud.service.common;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.*;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.user.LocalRolePrivilegeMapper;
import com.harmonycloud.dao.user.bean.LocalRolePrivilege;
import com.harmonycloud.dao.user.bean.LocalRolePrivilegeExample;
import com.harmonycloud.dto.user.LocalRoleCondRuleDto;
import com.harmonycloud.dto.user.LocalRoleConditionDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 数据权限帮助类
 * 基本功能：
 * 对自定义权限条件，根据是否符合由管理员定义的条件过滤数据。
 * 1 isManaged(T t)-----------判断数据使用的类是否由数据权限管理，可在数据鉴权（增删改鉴权）前判断
 * 1 authorize(T t)-----------对入参数据鉴权，如果失败则报错阻止，供增删改的数据鉴权使用
 * 2 isFiltered(T t)----------判断业务中的数据是否有数据权限，需要满足所有角色，常用来在业务中过滤没有数据权限访问的数据
 * 3 filter(List<T> list)-----对列表中的数据进行数据权限的过滤，需要满足所有角色，一般用来过滤查询结果
 * 4 isAnyMatched(T t)--------判断业务中的数据是否有数据权限，匹配任何一个角色即可通过，常用来在业务中过滤没有数据权限访问的数据
 * 5 matchAny(T t)------------对列表中的数据进行数据权限的过滤，匹配任何一个角色即可通过，一般用来过滤查询结果
 * 6 authorize(String resourceType, Map<String, String[]> parameterMap)--------AOP请求参数鉴权，鉴权失败报错
 *
 */
@Service
public class PrivilegeHelper {

    private static Logger logger = LoggerFactory.getLogger(PrivilegeHelper.class);
    @Autowired
    private HttpSession session;
    @Autowired
    private LocalRolePrivilegeMapper localRolePrivilegeMapper;

    private static ConcurrentHashMap<Integer, String> sqlConditions;

    /**
     * 判断数据使用的类是否由数据权限管理，可在数据鉴权（增删改鉴权）前判断
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T> boolean isManaged(T t){
        if (null == t){
            return false;
        }
        PrivilegeType privilegeType = null;
        try{
            privilegeType = t.getClass().getDeclaredAnnotation(PrivilegeType.class);
        }catch (Exception e){
            logger.error("解析失败，不能确定是否为权限管理的类", e);
        }
        if (null != privilegeType){
            List<Class> clzz = PrivilegeCustomTypeEnum.listClz();
            if (CollectionUtils.isEmpty(clzz)){
                return false;
            }
            return clzz.contains(t.getClass());
        }
        return null != privilegeType;
    }

    /**
     * 使用数据权限过滤实体，如入参，或业务中用到的实体。如果权限不足将报错
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T> void authorize(T t){
         boolean isFiltered = isFiltered(t);
        if (isFiltered){
            throw new MarsRuntimeException(ErrorCodeMessage.LOCAL_ROLE_DATA_PRIVILEGE_FAILED);
        }
    }

    /**
     * 业务中判断某些数据是否符合数据权限要求
     *
     * @param t
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public <T> boolean isFiltered(T t) {
        return null == filterNoCheck(t);
    }

    /**
     * 使用数据权限过滤实体，如入参，或业务中用到的实体
     *
     * @param t
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    private  <T> T filterNoCheck(T t) {
        List<T> list = new ArrayList<>();
        list.add(t);
        List<T> results = filter(list);
        return CollectionUtils.isEmpty(results)? null : results.get(0);
    }

    /**
     * 使用数据权限过滤列表，需要满足所有角色，如返回结果的列表
     *
     * @param list
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public <T> List<T> filter(List<T> list) {
        return filter(list, LocalRoleCondRelationEnum.AND);
    }

    /**
     * 业务中判断某些数据是否符合数据权限要求，匹配任何一个角色即可通过
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T> boolean isAnyMatched(T t) {
        return null != matchAny(t);
    }

    /**
     * 使用数据权限过滤列表，匹配任何一个角色即可通过，如返回结果的列表
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T> T matchAny(T t) {
        List<T> list = new ArrayList<>();
        list.add(t);
        List<T> results = matchAny(list);
        return CollectionUtils.isEmpty(results)? null : results.get(0);
    }

    /**
     * 使用数据权限过滤列表，匹配任何一个角色即可通过，如返回结果的列表
     *
     * @param list
     * @param <T>
     * @return
     */
    public <T> List<T> matchAny(List<T> list) {
        return filter(list, LocalRoleCondRelationEnum.OR);
    }

    /**
     * 使用数据权限过滤列表，如返回结果的列表
     *
     * @param list
     * @param <T>
     * @return
     */
    private <T> List<T> filter(List<T> list, LocalRoleCondRelationEnum relationEnum) {
        try{
            if (CollectionUtils.isEmpty(list)){
                return list;
            }
            //找到不为空的记录
            T t = null;
            for (T obj:list) {
                if (!Objects.isNull(obj)){
                    t = obj;
                    break;
                }
            }
            if (Objects.isNull(t)){
                return list;
            }
            // 类上的注解PrivilegeType即resourceType，在管理员配置时已经存入数据库字段resource_type
            PrivilegeType privilegeType = t.getClass().getDeclaredAnnotation(PrivilegeType.class);
            if (null == privilegeType){
                return list;
            }
            Object sessionObj = session.getAttribute(CommonConstant.SESSION_DATA_PRIVILEGE_LIST);
            List<LocalRolePrivilege>  localRolePrivileges = null;
            if (!Objects.isNull(sessionObj)){
                localRolePrivileges = (List<LocalRolePrivilege>)sessionObj;
                localRolePrivileges = localRolePrivileges.stream()
                        .filter(privilege -> {return StringUtils.equals(privilege.getResourceType(), privilegeType.name());}).collect(Collectors.toList());
            } else {
                return list;
            }

    //        Stream<T> stream = list.stream();
            List<T> result = new ArrayList<>(list);
            if (LocalRoleCondRelationEnum.AND == relationEnum) {
                for (LocalRolePrivilege localRolePrivilege : localRolePrivileges) {
                    if (null == localRolePrivilege) {
                        continue;
                    }
                    result = filter(localRolePrivilege, result, relationEnum,null);
                    if (CollectionUtils.isEmpty(result)) {
                        return result;
                    }
                }
            }else if (LocalRoleCondRelationEnum.OR == relationEnum){
                Set<T> anyMatchedSet = new HashSet<>();
                AtomicBoolean isCondReady = new AtomicBoolean(false);
                for (LocalRolePrivilege localRolePrivilege : localRolePrivileges) {
                    if (null == localRolePrivilege){
                        continue;
                    }
                    List<T>  currentList = filter(localRolePrivilege, result, relationEnum, isCondReady);
                    if (CollectionUtils.isEmpty(currentList)){
                        continue;
                    }
                    anyMatchedSet.addAll(currentList);
                }
                // 所有角色都不包含当前条件
                if (!isCondReady.get()){
                    return result;
                }
                return new ArrayList<>(anyMatchedSet);
            }
            return result;
        }catch (IllegalAccessException iae){
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_CONFIG);
        }
    }

    private  <T> List<T> filter(LocalRolePrivilege localRolePrivilege, List<T> list, LocalRoleCondRelationEnum relationEnum, AtomicBoolean isCondReady) throws IllegalAccessException {
        return filter(localRolePrivilege, list, null, relationEnum,isCondReady);
    }

    private  <T> List<T> filter(LocalRolePrivilege localRolePrivilege, List<T> list, Class clz, LocalRoleCondRelationEnum relationEnum, AtomicBoolean isAnyCondReady) {
//        LocalRolePrivilege localRolePrivilege = localRolePrivilegeMapper.selectByPrimaryKey(privilegeId);
        if (null == localRolePrivilege){
            return LocalRoleCondRelationEnum.OR == relationEnum?Collections.EMPTY_LIST: list;
        }
        String exps = localRolePrivilege.getConditionValue();

        if(null != localRolePrivilege.getConditionType()
                && CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM == localRolePrivilege.getConditionType()){
            return list.stream().filter(t-> {
                LocalRoleConditionDto conditionDto = JsonUtil.jsonToPojo(exps, LocalRoleConditionDto.class);

                if (null == clz || t.getClass() == clz.getClass()){
                    String type = null;
                    PrivilegeType privilegeType = t.getClass().getDeclaredAnnotation(PrivilegeType.class);
                    if (null != privilegeType){
                        type = privilegeType.name();
                    } else {
                        type = t.getClass().getSimpleName();
                    }

                    Field[] fields = t.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        PrivilegeField annotation = field.getAnnotation(PrivilegeField.class);
                        if (null == annotation) {
                            continue;
                        }
                        if(!StringUtils.isAnyBlank(annotation.type())){
                            type = annotation.type();
                        }
                        String fieldExp = type + CommonConstant.DOT + annotation.name();
                        try {
                            field.setAccessible(true);
                            if(exps.contains(fieldExp) && null != field.get(t)){
                                replaceField(conditionDto,fieldExp, field.get(t).toString());
                            }
                        } catch (IllegalAccessException e) {
                            logger.error("field替换出错", e);
                            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_CONFIG);

                        }

                    }
                }

            boolean isMatch = true;
            boolean isReady = false;
            List<LocalRoleCondRuleDto> rules = conditionDto.getRule();
            for (LocalRoleCondRuleDto localRoleCondRuleDto : rules) {
                if (!localRoleCondRuleDto.isReady()){
                    continue;
                }
                isReady =true;
                boolean isCurMatch = LocalRoleCondComparorEnum
                        .customCompare(localRoleCondRuleDto.getField(), localRoleCondRuleDto.getOp(), localRoleCondRuleDto.getValue());
                LocalRoleCondRelationEnum localRoleCondRelationEnum = LocalRoleCondRelationEnum.getEnum(localRoleCondRuleDto.getOp());
                if (null == localRoleCondRelationEnum || localRoleCondRelationEnum == LocalRoleCondRelationEnum.AND){
                    isMatch = isMatch && isCurMatch;
                } else if (localRoleCondRelationEnum == LocalRoleCondRelationEnum.OR){
                    isMatch = isMatch || isCurMatch;
                }
            }
            // 如果角色间为or关系，且该角色各个条件规则都没使用，则过滤掉该角色所有数据
            if (LocalRoleCondRelationEnum.OR == relationEnum && !isReady){
                return false;
            }
            if (null != isAnyCondReady){
                isAnyCondReady.set(true);
            }
            return isMatch;
            }
            ).collect(Collectors.toList());
        }
        return LocalRoleCondRelationEnum.OR == relationEnum?Collections.EMPTY_LIST: list;
    }

    /**
     * 值替换掉字段，以便作纯值的比较
     *
     * @param conditionDto
     * @param fieldName
     * @param value
     */
    private void replaceField(LocalRoleConditionDto conditionDto, String fieldName, String value){
        // 空值跳过
        if (Objects.isNull(value)){
            return;
        }
        List<LocalRoleCondRuleDto> rules = conditionDto.getRule();
        for (LocalRoleCondRuleDto localRoleCondRuleDto : rules) {
            if(StringUtils.equals(fieldName, localRoleCondRuleDto.getField())){
                localRoleCondRuleDto.setField(value);
                localRoleCondRuleDto.setReady(true);
            }
        }
    }

    /**
     * AOP层request参数鉴权
     *
     * @param resourceType
     * @param parameterMap
     */
    public void authorize(String resourceType, Map<String, String[]> parameterMap){
        Object sessionObj = session.getAttribute(CommonConstant.SESSION_DATA_PRIVILEGE_LIST);
        List<LocalRolePrivilege>  localRolePrivileges = null;
        if (!Objects.isNull(sessionObj)){
            localRolePrivileges = (List<LocalRolePrivilege>)sessionObj;
            localRolePrivileges = localRolePrivileges.stream()
                    .filter(privilege -> {return StringUtils.equals(privilege.getResourceType(), resourceType);}).collect(Collectors.toList());
        } else {
            return ;
        }
        for (LocalRolePrivilege localRolePrivilege : localRolePrivileges) {
            authorize(localRolePrivilege, parameterMap);
        }
    }

    /**
     * AOP层request参数鉴权
     *
     * @param localRolePrivilege
     * @param parameterMap
     * @return
     */
    private void authorize(LocalRolePrivilege localRolePrivilege,  Map<String, String[]> parameterMap){
        Iterator<Map.Entry<String, String[]>> parameterIt = parameterMap.entrySet().iterator();
        String exps = localRolePrivilege.getConditionValue();
        LocalRoleConditionDto conditionDto = JsonUtil.jsonToPojo(exps, LocalRoleConditionDto.class);

        while (parameterIt.hasNext()){
            Map.Entry<String, String[]> parameter = parameterIt.next();
            String fieldExp = localRolePrivilege.getResourceType() + CommonConstant.DOT + parameter.getKey();
            replaceField(conditionDto,fieldExp, parameter.getValue()[0]);
        }
        boolean isMatch = true;
        boolean isReady = false;
        List<LocalRoleCondRuleDto> rules = conditionDto.getRule();
        for (LocalRoleCondRuleDto localRoleCondRuleDto : rules) {
            if (!localRoleCondRuleDto.isReady()){
                continue;
            }
            isReady =true;
            boolean isCurMatch = LocalRoleCondComparorEnum
                    .customCompare(localRoleCondRuleDto.getField(), localRoleCondRuleDto.getOp(), localRoleCondRuleDto.getValue());
            LocalRoleCondRelationEnum localRoleCondRelationEnum = LocalRoleCondRelationEnum.getEnum(localRoleCondRuleDto.getOp());
            if (null == localRoleCondRelationEnum || localRoleCondRelationEnum == LocalRoleCondRelationEnum.AND){
                isMatch = isMatch && isCurMatch;
            } else if (localRoleCondRelationEnum == LocalRoleCondRelationEnum.OR){
                isMatch = isMatch || isCurMatch;
            }
        }
        // 如果有条件命中，但是条件不成立
        if (isReady && !isMatch){
            throw new MarsRuntimeException(ErrorCodeMessage.LOCAL_ROLE_DATA_PRIVILEGE_FAILED);
        }
    }

    /**
     * 将json格式的条件转换为sql片段
     *
     * @param conditionJson
     * @return
     */
    private static String convertSqlJsonCondition(String conditionJson){
        boolean isSuccess = true;
        LocalRoleConditionDto conditionDto = JsonUtil.jsonToPojo(conditionJson, LocalRoleConditionDto.class);
        StringBuilder sqlBuilder = new StringBuilder();
        List<LocalRoleCondRuleDto> condRuleDtos = conditionDto.getRule();
        for (LocalRoleCondRuleDto localRoleCondRuleDto : condRuleDtos) {
            sqlBuilder.append(CommonConstant.BLANKSTRING);
            sqlBuilder.append(localRoleCondRuleDto.getField());
            String compareStr = LocalRoleCondComparorEnum.sqlCompare(localRoleCondRuleDto.getOp(), localRoleCondRuleDto.getValue());
            if(StringUtils.isAnyBlank(compareStr)){
                isSuccess = false;
                break;
            }

            sqlBuilder.append(compareStr);
            sqlBuilder.append(conditionDto.getOp());

        }
        if (!isSuccess){
            //不成功说明权限配置出错，需要通知管理员修改配置。由外部判断。
            return null;
        } else {
            sqlBuilder.replace(sqlBuilder.lastIndexOf(conditionDto.getOp()), sqlBuilder.length()-1, CommonConstant.EMPTYSTRING);
            return sqlBuilder.toString();
        }
    }

    /**
     * 刷新缓存里指定的的sql条件，如果转换出错则报错。sql条件由外部插入或更新时调用。
     *
     * @param privilegeId
     * @return
     */
    public ConcurrentHashMap<Integer, String> refreshSqlCondWithCheck(Integer privilegeId){
        return refreshSqlCondition(privilegeId, true);
    }

    /**
     * 刷新sql条件缓存主业务
     *
     * @param privilegeId
     * @param isCheck
     * @return
     */
    private  ConcurrentHashMap<Integer, String> refreshSqlCondition(Integer privilegeId, boolean isCheck){
        ConcurrentHashMap<Integer, String> conditions = new ConcurrentHashMap<>();
        LocalRolePrivilegeExample condition = new LocalRolePrivilegeExample();
        if(null!= privilegeId){
            condition.createCriteria().andIdEqualTo(privilegeId);
        }
        List<LocalRolePrivilege> localRolePrivileges = localRolePrivilegeMapper.selectByExample(condition);
        if (CollectionUtils.isEmpty(localRolePrivileges)){
            return conditions;
        }
        for (LocalRolePrivilege localRolePrivilege: localRolePrivileges) {
            if(null!= localRolePrivilege.getConditionType()
                    && CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL == localRolePrivilege.getConditionType()){
                try{
                    String finalSql = convertSqlJsonCondition(localRolePrivilege.getConditionValue());
                    conditions.put(localRolePrivilege.getId(), finalSql);
                }catch (Exception e) {
                    logger.error("条件规则转换sql失败，privilegeId: " + localRolePrivilege.getId(), e);
                    if (isCheck){
                        throw new MarsRuntimeException(ErrorCodeMessage.LOCAL_ROLE_CONVERT_SQL_CONDITION_FAILED);
                    }
                }

            }
        }
        if (null == sqlConditions){
            sqlConditions = conditions;
        }
        return conditions;
    }

    /**
     * 根据权限id获取转换过的sql条件
     *
     * @param privilegeId
     * @return
     */
    public String getConvertedSql(Integer privilegeId){
        return sqlConditions.get(privilegeId);
    }

    /**
     * 从缓存删除某个记录
     *
     * @param privilegeId
     */
    public void removeConvertedSql(Integer privilegeId){
        if (sqlConditions.contains(privilegeId)){
            sqlConditions.remove(privilegeId);
        }
    }

    /**
     * 刷新全部缓存数据。
     * 在程序启动时加载。目前这样做，后面可考虑其他缓存形式。
     *
     * @return
     */
//    @PostConstruct
    public ConcurrentHashMap<Integer, String> refreshAllSqlConditions(){
        logger.info("加载数据权限");
        return refreshSqlCondition(null, false);
    }
}
