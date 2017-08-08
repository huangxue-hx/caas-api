package com.harmonycloud.service.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.user.bean.Resource;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dto.user.MenuDto;
import com.harmonycloud.service.user.impl.ResourceServiceimpl;


@Service
@Transactional(rollbackFor = Exception.class)
public class MenuService {

    @Value("#{propertiesReader['webhook.host']}")
    String webhook;

    @Autowired
    RoleService roleService;

    @Autowired
    ResourceService resourceService;

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }
}
