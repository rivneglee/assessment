#!/usr/bin/env bash

export ES_ENV="prod"
rm -rf target
git clone git@github.com:EasyAssessSystem/core.git
cd core
mvn clean install -Dmaven.test.skip=true
cd ..
mvn clean package -Dmaven.test.skip=true
pwd
ls

docker build --no-cache=true -f ./buildtask/Dockerfile -t registry.cn-beijing.aliyuncs.com/easyassess/assess-service:latest ./
