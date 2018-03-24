package com.harmonycloud.service.tenant.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dao.tenant.TenantClusterQuotaMapper;
import com.harmonycloud.dao.tenant.bean.TenantClusterQuota;
import com.harmonycloud.dao.tenant.bean.TenantClusterQuotaExample;
import com.harmonycloud.dto.tenant.ClusterQuotaDto;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.DashboardService;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.TenantClusterQuotaService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by andy on 17-1-9.
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class TenantClusterQuotaServiceImpl implements TenantClusterQuotaService {

    @Autowired
    TenantClusterQuotaMapper tenantClusterQuotaMapper;
    @Autowired
    ClusterService clusterService;
    @Autowired
    DashboardService dashboardService;
    @Autowired
    NamespaceService namespaceService;
    @Autowired
    HttpSession session;

    private static final Logger logger = LoggerFactory.getLogger(TenantClusterQuotaServiceImpl.class);
    private static final String MEMORYGB = "memoryGb";

    /**
     * 根据租户id查询集群配额列表 clusterId 为空查询该租户下的所有集群配额
     *
     * @param tenantId
     * @return
     * @throws Exception
     */
    @Override
    public List<ClusterQuotaDto> listClusterQuotaByTenantid(String tenantId , String clusterId) throws Exception {
        TenantClusterQuotaExample example = this.getExample();
        if (Objects.isNull(clusterId)){
            example.createCriteria().andTenantIdEqualTo(tenantId);
        } else {
            example.createCriteria().andTenantIdEqualTo(tenantId).andClusterIdEqualTo(clusterId);
        }
        List<TenantClusterQuota> quotaList = tenantClusterQuotaMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(quotaList)){
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTERQUOTA_INCORRECT);
        }
        //组装返回值
        List<ClusterQuotaDto> quotaDtos = new ArrayList<>();
        for (TenantClusterQuota tenantClusterQuota:quotaList) {
            String currentClusterId = tenantClusterQuota.getClusterId();
            Cluster cluster = this.clusterService.findClusterById(currentClusterId);
            ClusterQuotaDto clusterQuotaDto = new ClusterQuotaDto();
            clusterQuotaDto.setClusterId(currentClusterId);
            clusterQuotaDto.setClusterAliasName(cluster.getAliasName());
            clusterQuotaDto.setTenantId(tenantClusterQuota.getTenantId());
            clusterQuotaDto.setId(tenantClusterQuota.getId());
            clusterQuotaDto.setClusterName(tenantClusterQuota.getClusterName());
            double memoryQuota = tenantClusterQuota.getMemoryQuota();
            int flag = 1;
            //处理内存返回类型
            while (memoryQuota >= 1024){
                memoryQuota = memoryQuota/1024;
                flag++;
            }
            switch (flag) {
                case 1 :
                    clusterQuotaDto.setMemoryQuotaType(CommonConstant.MB);
                    break;
                case 2:
                    clusterQuotaDto.setMemoryQuotaType(CommonConstant.GB);
                    break;
                case 3 :
                    clusterQuotaDto.setMemoryQuotaType(CommonConstant.TB);
                    break;
                case 4 :
                    clusterQuotaDto.setMemoryQuotaType(CommonConstant.PB);
                    break;
                default:
                    throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_MEMORY_TYPE_ERROR);
            }
            // 保留一位小数
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(CommonConstant.NUM_ONE);
            nf.setRoundingMode(RoundingMode.HALF_UP);
            clusterQuotaDto.setMemoryQuota(Double.valueOf(nf.format(memoryQuota % CommonConstant.NUM_ONE_DOUBLE == 0 ? (long) memoryQuota : memoryQuota)));
            double cpuQuota = tenantClusterQuota.getCpuQuota();
            clusterQuotaDto.setCpuQuota(Double.valueOf(nf.format(cpuQuota % CommonConstant.NUM_ONE_DOUBLE == 0 ? (long) cpuQuota : cpuQuota)));
            clusterQuotaDto.setCpuQuotaType(CommonConstant.CORE);
            //租户集群使用量
            getClusterUsage(tenantId,currentClusterId,clusterQuotaDto);
            quotaDtos.add(clusterQuotaDto);
        }
        return quotaDtos;
    }
    /**
     * 获取集群配额可用值与总配额
     * @param tenantId
     * @param clusterId
     * @param clusterQuotaDto
     * @throws Exception
     */
    public void getClusterUsage(String tenantId,String clusterId,ClusterQuotaDto clusterQuotaDto) throws Exception{
        //根据tenantid查询集群资源使用列表
        Map<String, List> clusterQuotaListByTenantid = namespaceService.getClusterQuotaListByTenantid(tenantId,clusterId);
        if (!Objects.isNull(clusterId)){
            //获取集群
            Cluster cluster = clusterService.findClusterById(clusterId);
            Map<String, Object> infraInfoWorkNode = this.dashboardService.getInfraInfoWorkNode(cluster);
            Double memory = null;
            if (!Objects.isNull(infraInfoWorkNode.get(MEMORYGB))){
                memory = Double.valueOf(infraInfoWorkNode.get(MEMORYGB).toString());
            }
            Double cpu = null;
            if (!Objects.isNull(infraInfoWorkNode.get(CommonConstant.CPU))){
                cpu = Double.valueOf(infraInfoWorkNode.get(CommonConstant.CPU).toString());
            }
            if (!Objects.isNull(cpu) && !Objects.isNull(memory)){
                //设置集群总CPU
                clusterQuotaDto.setTotalCpu(Double.valueOf(cpu.toString()));
                clusterQuotaDto.setTotalCpuType(CommonConstant.CORE);
                //设置集群总内存
                clusterQuotaDto.setTotalMomry(Double.valueOf(memory.toString()));
                clusterQuotaDto.setTotalMemoryType(CommonConstant.GB);
            }
            //租户下集群使用量
            List<Map> list = clusterQuotaListByTenantid.get(clusterId.toString());
            Map<String, Object> stringObjectMap = generateClusterUseage(list);
            String usedMemory = (String) stringObjectMap.get(CommonConstant.MEMORY);
            String usedCpu = (String) stringObjectMap.get(CommonConstant.CPU);
            String cpuType = (String) stringObjectMap.get(CommonConstant.CPUTYPE);
            String memType = (String) stringObjectMap.get(CommonConstant.MEMORYTYPE);
            //整个集群使用量
//            Map<String, List> clusterQuotaListByClusterid = namespaceService.getClusterQuotaListByTenantid(null,clusterId);
            Map<String, Map<String, Object>> clusterAllocatedResources = clusterService.getClusterAllocatedResources(clusterId);
            Map<String, Object> clusterUseage = clusterAllocatedResources.get(clusterId.toString());
            String clusterUnUsedMemory = clusterUseage.get("clusterMemoryAllocatedResources").toString();
            String clusterUnUsedCpu = clusterUseage.get("clusterCpuAllocatedResources").toString();
            String clusterCpuType = CommonConstant.CORE;
            String clusterMemType = CommonConstant.GB;
            //设置租户当前集群内存使用量
            if (StringUtils.isNotBlank(usedMemory)){
                clusterQuotaDto.setUsedMemory(Double.valueOf(usedMemory));
                clusterQuotaDto.setUsedMemoryType(memType);
            }else {
                clusterQuotaDto.setUsedMemory(0d);
                clusterQuotaDto.setUsedMemoryType(clusterQuotaDto.getTotalMemoryType());
            }
            //设置租户当前集群CPU使用量
            if (StringUtils.isNotBlank(usedCpu)){
                clusterQuotaDto.setUsedCpu(Double.valueOf(usedCpu));
                clusterQuotaDto.setUsedCpuType(cpuType);
            }else {
                clusterQuotaDto.setUsedCpu(0d);
                clusterQuotaDto.setUsedCpuType(clusterQuotaDto.getTotalCpuType());
            }
            //设置集群内存使用量
            if (StringUtils.isNotBlank(clusterUnUsedMemory)){
                clusterQuotaDto.setUnUsedMemory(Double.valueOf(clusterUnUsedMemory));
                clusterQuotaDto.setUnUsedMemoryType(clusterMemType);
            }else {
                clusterQuotaDto.setUnUsedMemory(0d);
                clusterQuotaDto.setUnUsedMemoryType(clusterQuotaDto.getTotalMemoryType());
            }
            //设置集群CPU使用量
            if (StringUtils.isNotBlank(clusterUnUsedCpu)){
                clusterQuotaDto.setUnUsedCpu(Double.valueOf(clusterUnUsedCpu));
                clusterQuotaDto.setUnUsedCpuType(clusterCpuType);
            }else {
                clusterQuotaDto.setClusterUsedCpu(0d);
                clusterQuotaDto.setUnUsedCpuType(clusterQuotaDto.getTotalCpuType());
            }
        }

    }
    private Map<String,Object> generateClusterUseage(List<Map> list){
        Map<String,Object> result = new HashMap();
        double mem = 0;
        double cpu = 0;
        //是否为初始化类型
        Boolean isInitType = true;
        //初始化类型
        String initType = null;
        //是否需要转化
        Boolean isTransform = false;
        //是否转化完成
        Boolean isTransformCompleted = false;
        if (CollectionUtils.isEmpty(list)){
            return result;
        }
        for (Map map:list) {
            //eg:cpu[5,1] cpu[0] 为hard已经分配的量，cpu[1] 为hard已经使用的量，在计算集群资源的时候以已经分配的量cpu[0]的值为准
            List cpuList = (List)map.get(CommonConstant.CPU);
            //eg:memory[5,1] memory[0] 为hard已经分配的量，memory[1] 为hard已经使用的量，在计算集群资源的时候以已经分配的量memory[0]的值为准
            List memList = (List)map.get(CommonConstant.MEMORY);
            String hardType = (String) map.get(CommonConstant.HARDTYPE);
            if (StringUtils.isBlank(initType)){
                initType = hardType;
            }else {
                isInitType = false;
            }
            //处理cpu 默认单位为core
            if (!CollectionUtils.isEmpty(cpuList)){
                String cpuHard = cpuList.get(0).toString();
                //如果包含小m则cpu单位为m，转换单位后累加，不包含则cpu单位为core直接累加
                if (cpuHard.contains(CommonConstant.SMALLM)){
                    cpuHard = cpuHard.split(CommonConstant.SMALLM)[0];
                    cpu += (Double.valueOf(cpuHard)/1000);
                }else {
                    cpu += Double.valueOf(cpuHard);
                }

            }
            //处理内存
            if ((!CollectionUtils.isEmpty(memList))&&StringUtils.isNotBlank(hardType)){
                String memHard = memList.get(0).toString();
                if (!isTransformCompleted){
                    isTransform = initType.equals(hardType);
                }
                if (!isInitType && !isTransform){
                    //判断初始值是否已经转换完成
                    if (!isTransformCompleted){
                        //内存类型与初始类型不一样，同一转化为MB
                        switch (initType){
                            case CommonConstant.MB :
                                break;
                            case CommonConstant.GB :
                                mem = mem * 1024;
                                break;
                            case CommonConstant.TB :
                                mem = mem * 1024 * 1024;
                                break;
                            case CommonConstant.PB :
                                mem = mem * 1024 * 1024 * 1024;
                                break;
                            default:
                                throw new MarsRuntimeException(ErrorCodeMessage.INVALID_MEMORY_UNIT_TYPE);
                        }
                        isTransformCompleted = true;
                    }
                    //累加新的内存
                    switch (hardType){
                        case CommonConstant.MB :
                            mem += Double.valueOf(memHard);
                            break;
                        case CommonConstant.GB :
                            mem += Double.valueOf(memHard) * 1024;
                            break;
                        case CommonConstant.TB :
                            mem += Double.valueOf(memHard) * 1024 * 1024;
                            break;
                        case CommonConstant.PB :
                            mem += Double.valueOf(memHard) * 1024 * 1024 * 1024;
                            break;
                        default:
                            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_MEMORY_UNIT_TYPE);
                    }
                }else {
                    mem += Double.valueOf(memHard);
                }
            }

        }
        //返回值转化else
        int memtype = 0;//内存转化系数 0:初始化状态 1：MB 2: GB 3:TB 4:PB
        if (isTransformCompleted){
            //已经统一转换为MB，memtype为1
            memtype = 1;
        } else {
            //未统一转换为MB，根据初始type确定memtype的值
            switch (initType){
                case CommonConstant.MB :
                    memtype = 1;
                    break;
                case CommonConstant.GB :
                    memtype = 2;
                    break;
                case CommonConstant.TB :
                    memtype = 3;
                    break;
                case CommonConstant.PB :
                    memtype = 4;
                    break;
                default:
                    throw new MarsRuntimeException(ErrorCodeMessage.INVALID_MEMORY_UNIT_TYPE);
            }
        }
        while (mem >= 1024){
            mem  /= 1024;
            memtype ++;
        }
        switch (memtype){
            case 1 :
                result.put(CommonConstant.MEMORYTYPE,CommonConstant.MB);
                break;
            case 2 :
                result.put(CommonConstant.MEMORYTYPE,CommonConstant.GB);
                break;
            case 3 :
                result.put(CommonConstant.MEMORYTYPE,CommonConstant.TB);
                break;
            case 4 :
                result.put(CommonConstant.MEMORYTYPE,CommonConstant.PB);
                break;
            default:
                throw new MarsRuntimeException(ErrorCodeMessage.INVALID_MEMORY_UNIT_TYPE);
        }
        // 保留一位小数
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(1);
        nf.setRoundingMode(RoundingMode.UP);
        result.put(CommonConstant.CPU,nf.format(cpu % 1.0 == 0 ? (long) cpu : cpu));
        result.put(CommonConstant.CPUTYPE,CommonConstant.CORE);
        result.put(CommonConstant.MEMORY,nf.format(mem % 1.0 == 0 ? (long) mem : mem));
        return result;
    }
    /**
     * 根据id查询租户某个集群下的配额
     *
     * @param id@return
     */
    @Override
    public TenantClusterQuota getClusterQuotaById(int id) throws Exception {
        TenantClusterQuota tenantClusterQuota = tenantClusterQuotaMapper.selectByPrimaryKey(id);
        return tenantClusterQuota;
    }

    /**
     * 创建租户下集群配额
     *
     * @param tenantClusterQuota
     * @throws Exception
     */
    @Override
    public void createClusterQuota(TenantClusterQuota tenantClusterQuota) throws Exception {
        this.tenantClusterQuotaMapper.insertSelective(tenantClusterQuota);
    }

    /**
     * 修改租户下集群配额
     *
     * @param tenantClusterQuota
     * @throws Exception
     */
    @Override
    public void updateClusterQuota(TenantClusterQuota tenantClusterQuota) throws Exception {
        tenantClusterQuotaMapper.updateByPrimaryKeySelective(tenantClusterQuota);
    }

    /**
     * 根据id删除租户下集群配额
     *
     * @param id
     * @throws Exception
     */
    @Override
    public void deleteClusterQuotaByid(int id) throws Exception {
        this.tenantClusterQuotaMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据租户id删除租户下集群配额
     *
     * @param tenantId
     * @throws Exception
     */
    @Override
    public void deleteClusterQuotaByTenantId(String tenantId) throws Exception {
        TenantClusterQuotaExample example = this.getExample();
        example.createCriteria().andTenantIdEqualTo(tenantId);
        this.tenantClusterQuotaMapper.deleteByExample(example);
    }

    /**
     * 根据租户id与集群id获取集群配额
     *
     * @param tenantId
     * @param clusterId
     * @throws Exception
     */
    @Override
    public TenantClusterQuota getClusterQuotaByTenantIdAndClusterId(String tenantId, String clusterId) throws Exception {
        TenantClusterQuotaExample example = this.getExample();
        example.createCriteria().andTenantIdEqualTo(tenantId).andClusterIdEqualTo(clusterId);
        List<TenantClusterQuota> tenantClusterQuotas = this.tenantClusterQuotaMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(tenantClusterQuotas)){
            return tenantClusterQuotas.get(0);
        }
        return null;
    }

    /**
     * 根据集群id获取集群配额列表
     *
     * @param clusterId
     * @return
     * @throws Exception
     */
    @Override
    public List<TenantClusterQuota> getClusterQuotaByClusterId(String clusterId) throws Exception {
        TenantClusterQuotaExample example = this.getExample();
        example.createCriteria().andClusterIdEqualTo(clusterId);
        List<TenantClusterQuota> tenantClusterQuotas = this.tenantClusterQuotaMapper.selectByExample(example);
        return tenantClusterQuotas;
    }

    /**
     * 根据集群id删除集群配额列表
     *
     * @param clusterId
     * @throws Exception
     */
    @Override
    public void deleteClusterQuotaByClusterId(String clusterId) throws Exception {
        TenantClusterQuotaExample example = this.getExample();
        example.createCriteria().andClusterIdEqualTo(clusterId);
        this.tenantClusterQuotaMapper.deleteByExample(example);
    }

    @Override
    public int deleteByClusterId(String clusterId){
        return tenantClusterQuotaMapper.deleteByClusterId(clusterId);
    }

    private TenantClusterQuotaExample getExample(){
        return  new TenantClusterQuotaExample();
    }
}
