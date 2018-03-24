package com.harmonycloud.common.enumm;

import com.harmonycloud.common.Constant.CommonConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 局部角色条件规则：比较符号Enum类，及sql组装规则
 */
public enum LocalRoleCondComparorEnum {
    // SQL比较符号，必须符合sql语法
    SQL_EQUAL(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL, "="),
    SQL_NOT_EQUAL(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL,"!="),
    SQL_GREAT_THAN(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL,">"),
    SQL_LESS_THAN(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL,"<"),
    SQL_GREAT_EQUAL(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL,">="),
    SQL_LESS_EQUAL(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL,"<="),
    SQL_IN(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL,"in"),
    SQL_LIKE(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL,"like"),
    SQL_NOT_LIKE(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL,"not like"),
    SQL_BETWEEN_AND(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL,"between ? and ?"),
    SQL_IS_NULL(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL,"is null"),
    SQL_IS_NOT_NULL(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL,"is not null"),

    // 自定义条件的比较符号
    CUSTOM_EQUAL(CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM, "="),
    CUSTOM_NOT_EQUAL(CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM, "!="),
    CUSTOM_GREAT_THAN(CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM,">"),
    CUSTOM_LESS_THAN(CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM,"<"),
    CUSTOM_GREAT_EQUAL(CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM,">="),
    CUSTOM_LESS_EQUAL(CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM,"<="),
    CUSTOM_IN(CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM,"in", "范围在"),
    CUSTOM_CONTAIN(CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM,"contain", "包含"),
    CUSTOM_NOT_CONTAIN(CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM,"not contain", "不包含"),

    ;

    private final static String SQL_BETWEEN = "between";
    private final static String SQL_AND = "and";

    private short conditionType;
    private String comparorStr;
    private String cnDesc;
    private String enDesc;

    LocalRoleCondComparorEnum(short conditionType, String comparorStr) {
        this.conditionType = conditionType;
        this.comparorStr = comparorStr;
        this.cnDesc = comparorStr;
        this.enDesc = comparorStr;
    }

    LocalRoleCondComparorEnum(short conditionType, String comparorStr, String cnDesc) {
        this.conditionType = conditionType;
        this.comparorStr = comparorStr;
        this.cnDesc = cnDesc;
        this.enDesc = comparorStr;
    }

    LocalRoleCondComparorEnum(short conditionType, String comparorStr, String enDesc, String cnDesc) {
        this.conditionType = conditionType;
        this.comparorStr = comparorStr;
        this.cnDesc = cnDesc;
        this.enDesc = enDesc;
    }

    /**
     * sql组装规则。根据比较符号及值的语法书写规则进行组装
     *
     * @param operator
     * @param values
     * @return
     */
    public static String sqlCompare(String operator, List<String> values){
        LocalRoleCondComparorEnum condComparorEnum = getEnumByComparor(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL, operator);
        if (null == condComparorEnum){
            return null;
        }
        switch (condComparorEnum){
            case SQL_IN: return in(values);
            case SQL_BETWEEN_AND: return between(values);
            case SQL_IS_NOT_NULL: return compareNoValue(condComparorEnum);
            case SQL_IS_NULL: return compareNoValue(condComparorEnum);
            default: return compareSingleValue(condComparorEnum,values);
        }
    }

    /**
     * 单值比较
     *
     * @param comparorEnum
     * @param values
     * @return
     */
    private static String compareSingleValue(LocalRoleCondComparorEnum comparorEnum, List<String> values){
        if (null == values || 0 == values.size()){
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(CommonConstant.BLANKSTRING);
        builder.append(comparorEnum.getComparorStr());
        builder.append(CommonConstant.BLANKSTRING);
        builder.append(CommonConstant.SINGLE_QUOTATION);
        builder.append(values.get(0));
        builder.append(CommonConstant.SINGLE_QUOTATION);
        builder.append(CommonConstant.BLANKSTRING);
        return builder.toString();

    }

    /**
     * 无值比较，如is null判断
     *
     * @param comparorEnum
     * @return
     */
    private static String compareNoValue(LocalRoleCondComparorEnum comparorEnum){
        StringBuilder builder = new StringBuilder();
        builder.append(CommonConstant.BLANKSTRING);
        builder.append(comparorEnum.getComparorStr());
        builder.append(CommonConstant.BLANKSTRING);
        return builder.toString();

    }

    /**
     * between比较
     *
     * @param values
     * @return
     */
    private static String between(List<String> values){
        if (null == values || values.size() < 2){
            return null;
        }
        StringBuilder betweenBuilder = new StringBuilder();
        betweenBuilder.append(CommonConstant.BLANKSTRING);
        betweenBuilder.append(SQL_BETWEEN);
        betweenBuilder.append(CommonConstant.BLANKSTRING);
        betweenBuilder.append(CommonConstant.SINGLE_QUOTATION);
        betweenBuilder.append(values.get(0));
        betweenBuilder.append(CommonConstant.SINGLE_QUOTATION);
        betweenBuilder.append(CommonConstant.BLANKSTRING);
        betweenBuilder.append(SQL_AND);
        betweenBuilder.append(CommonConstant.BLANKSTRING);
        betweenBuilder.append(CommonConstant.SINGLE_QUOTATION);
        betweenBuilder.append(values.get(1));
        betweenBuilder.append(CommonConstant.SINGLE_QUOTATION);
        betweenBuilder.append(CommonConstant.BLANKSTRING);
        return betweenBuilder.toString();

    }

    /**
     * 根据比较符号获取对应的enum
     *
     * @param op
     * @return
     */
    private static LocalRoleCondComparorEnum getEnumByComparor(Short conditionType, String op){
        LocalRoleCondComparorEnum[] condComparorEnums = values();
        for (LocalRoleCondComparorEnum condComparorEnum: condComparorEnums) {
            if (conditionType == condComparorEnum.getConditionType()
                    && StringUtils.equals(condComparorEnum.getComparorStr(), op)){
                return condComparorEnum;
            }
        }
        return null;
    }

    /**
     * 根据条件类型获取比较符号列表
     *
     * @param conditionType
     * @return
     */
    public static List<LocalRolePreFieldDto> getComparorByType(short conditionType){
        List<LocalRolePreFieldDto> comparors = new ArrayList<>();
        LocalRoleCondComparorEnum[] condComparorEnums = values();
        for (LocalRoleCondComparorEnum condComparorEnum: condComparorEnums) {
            if (conditionType == condComparorEnum.getConditionType()){
                LocalRolePreFieldDto field = new LocalRolePreFieldDto();
                field.setName(condComparorEnum.getComparorStr());
                field.setEnDesc(condComparorEnum.getEnDesc());
                field.setCnDesc(condComparorEnum.getCnDesc());
                comparors.add(field);
            }
        }
        return comparors;
    }

    /**
     * in范围
     *
     * @param values
     * @return
     */
    public static String in(List<String> values){
        if (null == values || 0 == values.size()){
            return null;
        }
        StringBuilder inBuilder = new StringBuilder();
        inBuilder.append(CommonConstant.BLANKSTRING);
        inBuilder.append(SQL_IN.getComparorStr());
        inBuilder.append(CommonConstant.BLANKSTRING);
        inBuilder.append(CommonConstant.LEFT_BRACKET);
        for (String value :values) {
            inBuilder.append(CommonConstant.SINGLE_QUOTATION);
            String transValue = value.replace(CommonConstant.SINGLE_QUOTATION, "\'");
            inBuilder.append(transValue);
            inBuilder.append(CommonConstant.SINGLE_QUOTATION);
            inBuilder.append(CommonConstant.COMMA);
        }
        if(CommonConstant.COMMA_CHAR == inBuilder.charAt(inBuilder.length()-1)){
            inBuilder.deleteCharAt(inBuilder.length()-1);
        }
        inBuilder.append(CommonConstant.RIGHT_BRACKET);
        inBuilder.append(CommonConstant.BLANKSTRING);
        return inBuilder.toString();
    }

    /**
     * 自定义条件的比较
     *
     * @param real
     * @param operator
     * @param expects
     * @return
     */
    public static boolean customCompare(String real, String operator, List<String> expects){
        if (CollectionUtils.isEmpty(expects)){
            return false;
        }
        LocalRoleCondComparorEnum condComparorEnum = getEnumByComparor(CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM, operator);
        if (null == condComparorEnum){}
        String expect = expects.get(0);
        switch (condComparorEnum){
            case CUSTOM_EQUAL: return StringUtils.equals(real, expect);
            case CUSTOM_NOT_EQUAL: return !StringUtils.equals(real, expect);
            case CUSTOM_GREAT_THAN:
                return new BigDecimal(real).compareTo(new BigDecimal(expect)) > 0;
            case CUSTOM_LESS_THAN:
                return new BigDecimal(real).compareTo(new BigDecimal(expect)) < 0;
            case CUSTOM_GREAT_EQUAL:
                return new BigDecimal(real).compareTo(new BigDecimal(expect)) >= 0;
            case CUSTOM_LESS_EQUAL:
                return new BigDecimal(real).compareTo(new BigDecimal(expect)) <= 0;
            case CUSTOM_IN:
                return Arrays.asList(expects).contains(real);
            case CUSTOM_CONTAIN:
                return real.toUpperCase().contains(expect.toUpperCase());
            case CUSTOM_NOT_CONTAIN:
                return !real.toUpperCase().contains(expect.toUpperCase());
        }
        return false;
    }

    public String getComparorStr() {
        return comparorStr;
    }

    public short getConditionType() {
        return conditionType;
    }

    public String getCnDesc() {
        return cnDesc;
    }

    public void setCnDesc(String cnDesc) {
        this.cnDesc = cnDesc;
    }

    public String getEnDesc() {
        return enDesc;
    }

    public void setEnDesc(String enDesc) {
        this.enDesc = enDesc;
    }

//    @Override
//    public String toString() {
//        return this.comparorStr;
//    }

}
