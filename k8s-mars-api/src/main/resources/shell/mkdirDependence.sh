#!/bin/bash
#1:pod,2:dependence dir,3:namespace,4:token,5:server
kubectl exec $1 -n $3 --token=$4 --server=$5 --insecure-skip-tls-verify=true -- mkdir -p "$2"