#!/bin/bash
#1:pod,2:path,3:namespace,4:container
if [ ! -n "$6" ] ;then
	for i in `kubectl exec $1 -n $3 --token=$4 --server=$5 --insecure-skip-tls-verify=true -- ls $2 |tr " " "?"`
	do
		file=${i//'?'/' '}
        echo $file
    done
else
	for i in `kubectl exec $1 -c $6 -n $3 --token=$4 --server=$5 --insecure-skip-tls-verify=true -- ls $2 |tr " " "?"`
	do
		file=${i//'?'/' '}
        echo $file
    done
fi
