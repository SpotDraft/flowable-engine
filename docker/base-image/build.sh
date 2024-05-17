#!/bin/bash
set -eou pipefail

readonly IMAGE=${1:-"flowable-jre:11.0.23"}
echo "Image name: ${IMAGE}"

echo "Building image..."
docker build --platform linux/amd64 -t ${IMAGE} -f Dockerfile .
