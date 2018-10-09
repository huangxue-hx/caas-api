FROM  10.10.124.151/library/tomcat-8-jdk-8:latest


RUN apt-get install -y git

ADD k8s-mars-api/target/k8s-mars-api.war /usr/local/
ADD image-build/ROOT.xml /usr/local/tomcat/conf/Catalina/localhost/
ADD image-build/run.sh /
ADD image-build/kubectl /usr/bin
ADD image-build/docker /usr/bin
ADD image-build/catalina.sh /usr/local/tomcat/bin/
ADD image-build/tomcat-users.xml /usr/local/tomcat/conf/
ADD image-build/libltdl.so.7 /usr/lib/x86_64-linux-gnu/
ADD image-build/unzip /usr/bin/
RUN chmod +x /usr/bin/kubectl
RUN chmod +x /usr/bin/unzip

RUN chmod +x /usr/bin/docker
RUN chmod +x /usr/local/tomcat/bin/catalina.sh
RUN unzip -q /usr/local/k8s-mars-api.war -d /usr/local/k8s-mars-api
RUN sed  -i 's/<Context>/<Context useHttpOnly="true">/g' /usr/local/tomcat/conf/context.xml
RUN sed  -i 's/connectionTimeout="20000"/connectionTimeout="20000" maxThreads="251" acceptCount="2048"/g' /usr/local/tomcat/conf/server.xml

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" > /etc/timezone

CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]
