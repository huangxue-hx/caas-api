#!/bin/bash
#1:pod,2:path,3:namespace,4:container
if [ ! -n "$4" ] ;then
	for i in `kubectl exec $1 -n $3 -- ls $2 |tr " " "?"`
	do
		file=${i//'?'/' '}
        echo $file
    done
else
	for i in `kubectl exec $1 -c $4 -n $3 -- ls $2 |tr " " "?"`
	do
		file=${i//'?'/' '}
        echo $file
    done
fi
