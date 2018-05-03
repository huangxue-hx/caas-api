#!/bin/bash

docker build -t k8s-deploy/mysql:v1.2 .
docker push k8s-deploy/mysql:v1.2
