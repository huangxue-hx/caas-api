package com.harmonycloud.service.config;

import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.druid.pool.DruidDataSource;
import com.github.pagehelper.PageHelper;
import com.harmonycloud.common.util.JenkinsClient;
import com.harmonycloud.dto.cluster.ClusterCRDDto;
import com.harmonycloud.k8s.bean.cluster.*;
import com.harmonycloud.k8s.util.DefaultClient;
import com.harmonycloud.service.cluster.ClusterService;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieHttpSessionStrategy;
import org.springframework.session.web.http.HttpSessionStrategy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 8 * 60 * 60)
@EnableTransactionManagement
public class ApplicationConfig {

    private Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);
    //最大连接数100
    private static final int REDIS_MAX_TOTAL_CONNECTION = 200;
    //请求超时时间 3s
    private static final int REDIS_MAX_WAIT_MILLIS = 5000;
    //逐出检查运行间隔, 每10分钟运行
    private static final int REDIS_TIME_BETWEEN_EVICTION_RUNS_MILLIS = 600000;
    //redis最小空闲连接数
    private static final int JEDIS_MIN_IDLE = 10;
    public static final String PROPERTIES_RESOURCE = "constant.properties";
    private static Properties systemProperties;

    @Autowired
    private ClusterService clusterService;

    @Bean
    public static Properties getSystemProperties() throws IOException{
        if(systemProperties == null) {
            systemProperties = PropertiesLoaderUtils.loadAllProperties(PROPERTIES_RESOURCE);
        }
        return systemProperties;
    }


    @Bean
    public JedisPoolConfig getJedisPoolConfig(){
        try {
            ClusterCRDDto cluster = InitClusterConfig.getTopCluster();
            ClusterRedis redis = cluster.getRedis();
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(redis.getMaxTotal() == null? REDIS_MAX_TOTAL_CONNECTION : redis.getMaxTotal());
            jedisPoolConfig.setMaxWaitMillis(redis.getMaxWaitMillis() == null? REDIS_MAX_WAIT_MILLIS : redis.getMaxWaitMillis());
            jedisPoolConfig.setTestWhileIdle(redis.getTestWhileIdle() == null ? true : redis.getTestWhileIdle());
            jedisPoolConfig.setTestOnBorrow(redis.getTestOnBorrow() == null ? true : redis.getTestOnBorrow());
            jedisPoolConfig.setTestOnReturn(redis.getTestOnReturn() == null ? true : redis.getTestOnReturn());
            jedisPoolConfig.setMinIdle(redis.getMinIdle() == null? JEDIS_MIN_IDLE : redis.getMinIdle());
            jedisPoolConfig.setTimeBetweenEvictionRunsMillis(redis.getTimeBetweenEvictionRunsMillis() == null?
                    REDIS_TIME_BETWEEN_EVICTION_RUNS_MILLIS : redis.getTimeBetweenEvictionRunsMillis());
            return jedisPoolConfig;
        }catch (Exception e){
            LOGGER.error("获取集群初始化信息错误",e);
        }
        return null;
    }


    @Bean
    public JedisConnectionFactory connectionFactory(){
        JedisConnectionFactory connection = new JedisConnectionFactory();
        try{
            ClusterCRDDto cluster = InitClusterConfig.getTopCluster();
            ClusterRedis redis = cluster.getRedis();
            Map<String, ClusterTemplate> template = InitClusterConfig.getTemplateMap();
            ClusterTemplate redisTemplate = template.get(redis.getType());
            connection.setHostName(DefaultClient.getIsInCluster()?redisTemplate.getServiceName():cluster.getK8sAddress());
            for(ServicePort port :  redisTemplate.getServicePort()) {
                if ("api".equals(port.getType())) {
                    connection.setPort(DefaultClient.getIsInCluster()? port.getPort():port.getNodePort());
                    break;
                }
            }
            connection.setPassword(StringUtils.isBlank(getPublicKey()) ? redis.getPassword() : ConfigTools.decrypt(getPublicKey(),
                    redis.getPassword()));
            connection.setUsePool(true);
            connection.setPoolConfig(getJedisPoolConfig());
        }catch(Exception e){
            LOGGER.error("获取集群信息错误",e);
        }



        LOGGER.info("jedis connection host:{},port:{}",connection.getHostName(),connection.getPort());
        return connection;
    }

    @Bean
    public StringRedisTemplate redisTemplate(JedisConnectionFactory connectionFactory){
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(connectionFactory);
        return stringRedisTemplate;
    }

    @Bean
    public HttpSessionStrategy httpSessionStrategy(){
        return new CookieHttpSessionStrategy();
    }



    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DruidDataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        ClassPathResource classPathResource = new ClassPathResource("sqlMapConfig.xml");
        sqlSessionFactoryBean.setConfigLocation(classPathResource);
        PageHelper[] pageHelpers  = new PageHelper[]{};
        sqlSessionFactoryBean.setPlugins(pageHelpers);
        return sqlSessionFactoryBean;
    }

    @Bean
    public DruidDataSource dataSource() throws Exception {
        DruidDataSource druidDataSource = new DruidDataSource();
        try{
            ClusterCRDDto cluster = InitClusterConfig.getTopCluster();
            ClusterMysql clusterMysql = cluster.getMysql();
            Map<String, ClusterTemplate> template = InitClusterConfig.getTemplateMap();
            ClusterTemplate mysqlTemplate = template.get(clusterMysql.getType());
            for(ServicePort port :  mysqlTemplate.getServicePort()) {
                if ("api".equals(port.getType())) {
                    String  host = DefaultClient.getIsInCluster()?mysqlTemplate.getServiceName():cluster.getK8sAddress();
                    Integer target = DefaultClient.getIsInCluster()?port.getPort():port.getNodePort();
                    druidDataSource.setUrl("jdbc:mysql://"+host+":"+target+"/k8s_auth_server?useUnicode=true&characterEncoding=UTF-8");
                    break;
                }
            }
            druidDataSource.setDriverClassName(clusterMysql.getDriverClass());
            druidDataSource.setUsername(clusterMysql.getUsername());
            druidDataSource.setPassword(StringUtils.isBlank(getPublicKey()) ? clusterMysql.getPassword() : ConfigTools.decrypt(getPublicKey(),
                    clusterMysql.getPassword()));
            druidDataSource.setInitialSize(clusterMysql.getInitialSize());
            if(clusterMysql.getMaxActive() != null) {
                druidDataSource.setMaxActive(clusterMysql.getMaxActive());
            }
            if(clusterMysql.getMinIdle() != null) {
                druidDataSource.setMinIdle(clusterMysql.getMinIdle());
            }
            Integer maxOpenPreparedStatements = clusterMysql.getMaxOpenPreparedStatements();
            if(!Objects.isNull(maxOpenPreparedStatements)) {
                druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(maxOpenPreparedStatements);
            }
            boolean poolPreparedStatements = clusterMysql.isPoolPreparedStatements();
            if(poolPreparedStatements) {
                druidDataSource.setPoolPreparedStatements(poolPreparedStatements);
            }
            druidDataSource.setMaxWait(clusterMysql.getMaxWait());
            druidDataSource.setFilters(clusterMysql.getFilters());
            druidDataSource.setConnectionProperties(clusterMysql.getConnectionProperties());
            druidDataSource.setRemoveAbandoned(clusterMysql.isRemoveAbandoned());
            druidDataSource.setRemoveAbandonedTimeout(clusterMysql.getRemoveAbandonedTimeout());
            druidDataSource.setLogAbandoned(clusterMysql.isLogAbandoned());

        }catch(Exception e){
            LOGGER.error("获取集群信息错误",e);
        }


        return druidDataSource;
    }


    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer()  throws Exception {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setBasePackage("com.harmonycloud.dao");
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return mapperScannerConfigurer;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DruidDataSource dataSource) throws  Exception {
        DataSourceTransactionManager  dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

    @Bean
    public JenkinsClient  jenkinsClient()  throws Exception {
        JenkinsClient jenkinsClient = new JenkinsClient();
        ClusterCRDDto cluster = InitClusterConfig.getTopCluster();
        ClusterJenkins jenkins = cluster.getJenkins();
        Map<String, ClusterTemplate> template = InitClusterConfig.getTemplateMap();
        ClusterTemplate jenkinsTemplate = template.get(jenkins.getType());
        String  host = DefaultClient.getIsInCluster()?jenkinsTemplate.getServiceName():cluster.getK8sAddress();
        // 若 servicePort 无 type：api的 信息，会出现null Exception
        ServicePort servicePort = getApiPort(jenkinsTemplate.getServicePort());
        Integer port = DefaultClient.getIsInCluster()?servicePort.getPort():servicePort.getNodePort();
        String username = jenkins.getUsername();
        String password = jenkins.getPassword();

        jenkinsClient.setHost(host);
        jenkinsClient.setPassword(StringUtils.isBlank(getPublicKey()) ? password : ConfigTools.decrypt(getPublicKey(), password));
        jenkinsClient.setUsername(username);
        jenkinsClient.setPort(port.toString());

        return jenkinsClient;

    }



    public  ServicePort getApiPort(List<ServicePort> servicePortList) {
        for(ServicePort port: servicePortList) {
            if ("api".equals(port.getType())){
                return  port;
            }
        }
        return null;
    }

    private String getPublicKey() throws Exception{
        String publicKey = getSystemProperties().getProperty("public.key");
        return publicKey;
    }

    /**
     * RestTemplate，支持中文编码
     * @return
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        return restTemplate;
    }

}
