package com.harmonycloud.common.enumm;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-1-9
 * @Modified
 */
public enum CtsCodeMessage {
    SUCCESS(100, "成功"),
    PARAMETER_BLANK(101, "参数为空"),
    STAGE_NOT_EXIST(103, "步骤不存在"),
    BUILD_NOT_EXIST(106, "构建不存在"),
    LAST_RUN_NOT_FINISHED(145, "套件上次执行还未结束，请稍后再执行");


    private final int value;
    private final String message;

    private CtsCodeMessage(int value, String message) {
        this.value = value;
        this.message = message;
    }
    public int value() {
        return this.value;
    }

    public String getMessage() {
        return this.message;
    }

    public static CtsCodeMessage getCodeMessageByCode(int code){
        CtsCodeMessage[] codeMessages = CtsCodeMessage.values();
        for(int i=0; i<codeMessages.length; i++) {
            if (codeMessages[i].value == code) {
                return codeMessages[i];
            }
        }
        return null;
    }
}
