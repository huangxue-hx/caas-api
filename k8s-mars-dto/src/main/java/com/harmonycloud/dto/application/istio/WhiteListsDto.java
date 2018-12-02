package com.harmonycloud.dto.application.istio;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xc
 * @date 2018/9/15 14:49
 */
@ApiModel(value = "WhiteListsDto信息")
public class WhiteListsDto extends BaseIstioPolicyDto{


    @ApiModelProperty(value = "白名单服务名称列表", name = "whiteNameList", example = "[{\"name\": \"service1\", \"namespace\": \"namespace1\"}, ... ]", required = true)
    private List<WhiteServiceDto> whiteNameList;

    public List<WhiteServiceDto> getWhiteNameList() {
        return whiteNameList;
    }

    public void setWhiteNameList(List<WhiteServiceDto> whiteNameList) {
        this.whiteNameList = whiteNameList;
    }


}
