#!/bin/bash

esInfo=`kubectl get svc -nkube-system|grep elastic`
esServiceHost=`echo ${esInfo} | awk '{print $3}'`
esServicePort=`echo ${esInfo} | awk '{print $5}' | awk -F ':' '{print $1}'`
echo "esServiceHost is ${esServiceHost} esServicePort is ${esServicePort}"
result=`curl -XPUT ${esServiceHost}:${esServicePort}/_template/kubernetes -d'{"template":"kubernetes-audit*","order":0,"settings":{"number_of_shards":5,"number_of_replicas":1,"max_result_window":300000},"mappings":{"auditlog":{"properties":{"requestURI":{"type":"string","index":"not_analyzed"},"objectRef":{"properties":{"name":{"type":"string","index":"not_analyzed"},"namespace":{"type":"string","index":"not_analyzed"},"resource":{"type":"string","index":"not_analyzed"}}},"verb":{"type":"string","index":"not_analyzed"}}}}}'`

resultIsError=`echo $result | grep error`
resultIsSuccess=`echo $result | grep true`

if [[ "$resultIsError" != "" ]]
then
    echo "create Apiserver Audit ElasticSearch Template Failed.."
    exit 1
fi

echo "==============$resultIsSuccess-------------------"
if [[ "$resultIsSuccess" != "" ]]
then
    echo "create Apiserver Audit ElasticSearch Template Success.."
else 
    echo "create Apiserver Audit ElasticSearch Template Failed.., result is ${result}"
    exit 1
fi

