package com.harmonycloud.dao.namespace;

import com.harmonycloud.dao.namespace.bean.NamespaceBean;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-4
 * @Modified
 */
public interface NamespaceMapper {

    NamespaceBean findByNamespaceId(String namespaceId);
}
