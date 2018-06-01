FROM k8s-deploy/mysql:v1.2

WORKDIR /opt
ADD k8s_auth_server.sql auth.sql
ADD k8s_server_mysql.sql server.sql
ADD docker-entrypoint.sh /entrypoint.sh
ADD my.cnf /etc/mysql/my.cnf 
RUN chmod u+x /entrypoint.sh
