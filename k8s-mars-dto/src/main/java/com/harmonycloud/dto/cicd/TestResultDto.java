package com.harmonycloud.dto.cicd;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-1-9
 * @Modified
 */
public class TestResultDto {
    private Integer code;
    private String link;
    private String msg;
    private String result;
    private String uuid;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
