package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.business.YamlDto;

/**
 * Created by root on 8/11/17.
 */
public interface YamlService {

    ActionReturnUtil deployYaml(YamlDto yamlDto) throws Exception;
}