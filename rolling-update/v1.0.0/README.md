1. Éý¼¶¾µÏñ
docker load -i controller.tar
docker tag k8s-deploy/kube-controller-manager-amd64:v1.9.2-1 10.10.103.29/k8s-deploy/kube-controller-manager-amd64:v1.9.2-1
docker push 10.10.103.29/k8s-deploy/kube-controller-manager-amd64:v1.9.2-1

docker load -i es.tar
docker tag k8s-deploy/elasticsearch:v2.4.1-3 10.10.103.29/k8s-deploy/elasticsearch:v2.4.1-3
docker push 10.10.103.29/k8s-deploy/elasticsearch:v2.4.1-3

kubectl cp db-upgrade-v1.0.0.sql kube-system/api-mysql-7bb955f8b5-86wl6:/root
kubectl exec -it api-mysql-7bb955f8b5-86wl6 bash -n kube-system
mysql -uroot -p123456 < /root/db-upgrade-v1.0.0.sql
