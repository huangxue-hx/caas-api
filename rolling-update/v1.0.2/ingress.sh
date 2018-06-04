#!/bin/bash 

kubectl get ingress --all-namespaces  | grep -v istio> test.txt

cat test.txt | while read line
do
  result=$(echo $line | grep "NAMESPACE")
  if [[ $result != "" ]] 
  then 
    continue
  fi 
  echo $line  | while read namespace name  hosts  port age
  do
     # echo -e   $namespace $name $hosts
      hostsnum=`echo $hosts | awk -F "." '{print NF-1}'`
      namenum=`echo $name | awk -F "." '{print NF-1}'`
     # echo -e  $hostsnum $namenum
      if [[ $hostsnum == "3" && $namenum == "0"  ]]
      then 
      #   echo -e $name $hosts  
         kubectl  get ing $name -n $namespace -o yaml > $namespace-$name.yaml
         sed -i  '/creationTimestamp/d' $namespace-$name.yaml
         sed -i  '/generation/d' $namespace-$name.yaml
         sed -i  '/resourceVersion/d' $namespace-$name.yaml
         sed -i '/selfLink/d' $namespace-$name.yaml
         sed -i '/uid/d' $namespace-$name.yaml
         hostname=`echo ${hosts%.*}`
         hostname=`echo ${hostname%.*}`
         sed -i "s/name: $name/name: $hostname/g" $namespace-$name.yaml
         kubectl delete ing  $name -n $namespace 
         kubectl  create  -f $namespace-$name.yaml 
      fi 
  done
done 
