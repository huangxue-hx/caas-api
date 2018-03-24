package com.harmonycloud.common.enumm;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-4
 * @Modified
 */
public enum MicroServiceCodeMessage {

    PARAMS_NULL("101", "参数为空"),
    NON_PRIVILEGED("102", "无权限"),
    NAMESPACE_NOT_EXIST("103", "空间不存在"),
    FIND_FAILURE("105", "查询失败"),
    ADD_COMPONENT_FAILURE("107", "新增组件失败"),
    PROJECT_NULL("111", "项目信息为空"),
    DOWNLOAD_FILE_FAILURE("115", "挂载文件下载失败"),
    PARAMS_EXIST_SPECIAL_CHARACTER("127", "参数不能包含特殊字符"),
    ROLLBACK_FAILURE("129", "回滚失败"),
    APP_NOT_EXIST("131","应用不存在"),
    APP_EXISTED("133","应用已存在"),
    DELETE_APP_FAILURE("137", "应用删除失败"),
    USER_NOT_EXIST("139", "用户不存在"),
    COMPONENT_NOT_EXIST("141", "组件不存在"),
    UPDATE_APP_FAILURE("151", "应用更新失败"),
    SPACE_INITIALIZED("153", "空间已被初始化"),
    SPACE_NOT_INIT("155","空间未初始化"),
    SUCCESS("200", "成功"),
    SPACE_TASK_DOING("300", "任务进行中"),
    SPACE_EXIST_DOING_TASK("301","空间存在运行中的任务"),
    SPACE_TASK_FAILURE("500", "任务失败"),
    PROJECT_SYNC_FAILURE("160", "项目同步失败"),
    USER_SYNC_FAILURE("161", "用户同步失败"),
    USER_RELATIONSHIP_ADD_FAILURE("162", "用户关系添加失败"),
    USER_RELATIONSHIP_REMOVE_FAILURE("163", "用户关系删除失败"),
    USER_DISABLED("303","该用户暂时停止使用，请联系管理员");
    private final String value;
    private final String message;

    private MicroServiceCodeMessage(String value, String message) {
        this.value = value;
        this.message = message;
    }

    public static MicroServiceCodeMessage getCodeMessage(String value) throws  Exception{
        MicroServiceCodeMessage[] codeMessages = MicroServiceCodeMessage.values();
        for (int i = 0; i < codeMessages.length; i++) {
            MicroServiceCodeMessage code = codeMessages[i];
            if (code.value.equals(value)) {
                return code;
            }
        }
        throw new IllegalArgumentException("No matching constant for [" + value + "]");
    }

    public String value() {
        return this.value;
    }

    public String getMessage() {
        return this.message;
    }
}
