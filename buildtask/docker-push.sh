#!/usr/bin/env bash

docker login --username=$DOCKER_REGISTRY_USER --password=$DOCKER_REGISTRY_PWD registry.cn-beijing.aliyuncs.com

docker tag registry.cn-beijing.aliyuncs.com/easyassess/assess-service registry.cn-beijing.aliyuncs.com/easyassess/assess-service:latest
docker push registry.cn-beijing.aliyuncs.com/easyassess/assess-service:latest
