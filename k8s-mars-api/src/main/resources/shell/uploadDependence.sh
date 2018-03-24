#!/bin/bash
#1:localfilepath 2:namespace 3:pod 4:containerpath  5:token 6:server
kubectl cp "$1" $2/$3:"$4" --token=$5 --server=$6 --insecure-skip-tls-verify=true