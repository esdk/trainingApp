#!/bin/bash
set -e

if [ ! $DOCKER_ENV_HOST ]; then
    if [ Msys = '$(uname -o)2>/dev/null' ]; then #git for windows
        DOCKER_ENV_HOST=$(docker-machine ip default)
    else #other
        DOCKER_ENV_HOST=$(hostname)
    fi
fi
pwd=$(pwd)

find . -name "*.properties.template" | while IFS= read -r pathname; do
    dirname=$(dirname "$pathname")
    sed "s+DOCKER_ENV_HOST+$DOCKER_ENV_HOST+g" "$pathname" > "$dirname/gradle.properties"
done
