package com.harmonycloud.common.enumm;

/**
 * Created by anson on 17/7/22.
 */
public enum  StageTemplateTypeEnum {
    CODECHECKOUT(0),
    IMAGEBUILD(1),
    DEPLOY(2),
    SCRIPT(3),
    INTEGRATIONTEST(8),
    CODESCAN(7),
    CUSTOM(6);

    private int code;

    StageTemplateTypeEnum(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
