package com.harmonycloud.api.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.pagehelper.PageHelper;
import com.harmonycloud.common.util.JenkinsClient;
import com.harmonycloud.dto.cluster.ClusterCRDDto;
import com.harmonycloud.k8s.bean.cluster.*;
import com.harmonycloud.k8s.util.DefaultClient;
import com.harmonycloud.service.cluster.ClusterService;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieHttpSessionStrategy;
import org.springframework.session.web.http.HttpSessionStrategy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.Map;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 8 * 60 * 60)
@EnableTransactionManagement
public class HttpSessionConfig {

    Logger LOGGER = LoggerFactory.getLogger(HttpSessionConfig.class);
    //最大连接数100
    private static final int REDIS_MAX_TOTAL_CONNECTION = 100;
    //请求超时时间 3s
    private static final int REDIS_MAX_WAIT_MILLIS = 3000;
    //逐出检查运行间隔, 每五分钟运行
    private static final int REDIS_TIME_BETWEEN_EVICTION_RUNS_MILLIS = 300000;


    @Autowired
    ClusterService clusterService;

    @Bean
    public JedisPoolConfig getJedisPoolConfig(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(REDIS_MAX_TOTAL_CONNECTION);
        jedisPoolConfig.setMaxWaitMillis(REDIS_MAX_WAIT_MILLIS);
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(true);
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(REDIS_TIME_BETWEEN_EVICTION_RUNS_MILLIS);
        return jedisPoolConfig;
    }


    @Bean
    public JedisConnectionFactory connectionFactory(){
        JedisConnectionFactory connection = new JedisConnectionFactory();
        try{
            ClusterCRDDto cluster = InitClusterConfig.getTopCluster();
            ClusterRedis redis = cluster.getRedis();
            Map<String, ClusterTemplate> template = InitClusterConfig.getTemplateMap();
            ClusterTemplate redisTemplate = template.get(redis.getType());
            connection.setHostName(DefaultClient.isInCluster?redisTemplate.getServiceName():cluster.getK8sAddress());
            for(ServicePort port :  redisTemplate.getServicePort()) {
                if ("api".equals(port.getType())) {
                    connection.setPort(DefaultClient.isInCluster? port.getPort():port.getNodePort());
                    break;
                }
            }
            connection.setPassword(redis.getPassword());
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
                    String  host = DefaultClient.isInCluster?mysqlTemplate.getServiceName():cluster.getK8sAddress();
                    Integer target = DefaultClient.isInCluster?port.getPort():port.getNodePort();
                    druidDataSource.setUrl("jdbc:mysql://"+host+":"+target+"/k8s_auth_server?useUnicode=true&characterEncoding=UTF-8");
                    break;
                }
            }
            druidDataSource.setDriverClassName(clusterMysql.getDriverClass());

            druidDataSource.setUsername(clusterMysql.getUsername());
            druidDataSource.setPassword(clusterMysql.getPassword());
            druidDataSource.setInitialSize(clusterMysql.getInitialSize());
            if(clusterMysql.getMaxActive() != null) {
                druidDataSource.setMaxActive(clusterMysql.getMaxActive());
            }
            if(clusterMysql.getMinIdle() != null) {
                druidDataSource.setMinIdle(clusterMysql.getMinIdle());
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
        String  host = DefaultClient.isInCluster?jenkinsTemplate.getServiceName():cluster.getK8sAddress();
        // 若 servicePort 无 type：api的 信息，会出现null Exception
        ServicePort servicePort = getApiPort(jenkinsTemplate.getServicePort());
        Integer port = DefaultClient.isInCluster?servicePort.getPort():servicePort.getNodePort();
        String username = jenkins.getUsername();
        String password = jenkins.getPassword();

        jenkinsClient.setHost(host);
        jenkinsClient.setPassword(password);
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



}
