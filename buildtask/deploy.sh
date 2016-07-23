#!/usr/bin/env bash

service_name=assess-service
host_address=192.168.0.21
uid=root
jar_home=/root/.jenkins/workspace/ASSESS/
api_service_path=/usr/esapp/api-services



function package()
{
    echo "building package"
    mvn -Dmaven.test.failure.ignore clean install
}

function startup()
{
    ssh $uid@$host_address /etc/init.d/assess start &
}

function deploy()
{
    echo "shutting down..."
    ssh $uid@$host_address /etc/init.d/assess stop
    echo "deploying package to server"
    scp $jar_home/target/assess-service-0.0.1.jar $uid@$host_address:$api_service_path
}

function build()
{
    package
    deploy
    startup
}

build