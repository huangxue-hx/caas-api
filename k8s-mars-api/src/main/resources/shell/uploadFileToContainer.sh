#!/bin/bash
#1:localfilepath 2:namespace 3:pod 4:containerpath 5:container
if [ ! -n "$5" ] ;then
    kubectl cp $1 $2/$3:$4 --token=$6 --server=$7 --insecure-skip-tls-verify=true
else
    kubectl cp $1 $2/$3:$4 -c $5 --token=$6 --server=$7 --insecure-skip-tls-verify=true
fi
