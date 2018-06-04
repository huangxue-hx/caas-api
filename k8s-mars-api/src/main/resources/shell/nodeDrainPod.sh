#!/bin/bash
#1:nodeName 2:token 3:server 4:timeOut
kubectl drain $1 --ignore-daemonsets --delete-local-data --force --token=$2 --server=$3 --timeout=$4 --insecure-skip-tls-verify=true