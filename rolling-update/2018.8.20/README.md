## 自定义负载均衡器功能

### 说明

1.ingressController占用的端口范围，以一个configMap文件进行存储，每个集群下都需要创建一个configMap；

2.数据库操作

    url_dic表中添加相关路径；
    数据库中新增一张表，名为ingress_controller_port,记录每个ingressController对主机端口的占用情况；
    租户配额表添加负载均衡名称字段。

