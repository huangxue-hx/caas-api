package com.harmonycloud.dto.application;

/**
 * Created by root on 4/12/17.
 */
public class IngressDto {
    private String type;

    private ParsedIngressListDto parsedIngressList;

    private SvcRouterDto svcRouter;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ParsedIngressListDto getParsedIngressList() {
        return parsedIngressList;
    }

    public void setParsedIngressList(ParsedIngressListDto parsedIngressList) {
        this.parsedIngressList = parsedIngressList;
    }

    public SvcRouterDto getSvcRouter() {
        return svcRouter;
    }

    public void setSvcRouter(SvcRouterDto svcRouter) {
        this.svcRouter = svcRouter;
    }
}
