#!/bin/bash

IP=$1
sed -i "s|.*redis.host.*|		<property name="hostName" value="$IP" />|" /usr/local/k8s-mars-api/WEB-INF/classes/applicationContext.xml
