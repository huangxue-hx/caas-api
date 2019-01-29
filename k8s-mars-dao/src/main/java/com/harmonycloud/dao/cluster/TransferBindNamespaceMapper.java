package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.TransferBindNamespace;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferBindNamespaceMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TransferBindNamespace record);

    int insertSelective(TransferBindNamespace record);

    TransferBindNamespace selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TransferBindNamespace record);

    int updateByPrimaryKey(TransferBindNamespace record);

    TransferBindNamespace queryBindNamespaceByParam(@Param(value="namespace")String namespace, @Param(value="clusterId")String clusterId);

    void saveBindNamespaces(@Param(value="bindNamespaces") List<TransferBindNamespace> bindNamespaces);

    void updateSuccessListNamespace(@Param(value="namespaceList")List<TransferBindNamespace> namespaceList);

    void updateErrorListNamespace(@Param(value="namespaceList")List<TransferBindNamespace> namespaceList);

    Integer queryLastNamespaceNum(@Param(value="tenantId")String tenantId,@Param(value="clusterId")String clusterId);

    List<TransferBindNamespace> queryErrorNamespace(@Param(value="tanantId")String tanantId,@Param(value="clusterId")String clusterId);
}