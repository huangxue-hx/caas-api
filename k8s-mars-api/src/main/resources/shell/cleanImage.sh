#!/bin/bash
#1:harbor
docker -H $1 run --name gc --rm --volumes-from harbor_registry_1 registry:2.5.0 garbage-collect /etc/registry/config.yml
