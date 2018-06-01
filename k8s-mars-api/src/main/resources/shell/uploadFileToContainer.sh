#!/bin/bash
#1:localfilepath 2:namespace 3:pod 4:containerpath 5:token 6:server 7:container
if [ ! -n "$7" ] ;then
    kubectl cp $1 $2/$3:$4 --token=$5 --server=$6 --insecure-skip-tls-verify=true
else
    kubectl cp $1 $2/$3:$4 -c $7 --token=$5 --server=$6 --insecure-skip-tls-verify=true
fi
