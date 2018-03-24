#!/bin/bash
#1:nodeName 2:token 3:server
kubectl drain $1 --ignore-daemonsets --delete-local-data --force --timeout=1800s --token=$2 --server=$3 --insecure-skip-tls-verify=true