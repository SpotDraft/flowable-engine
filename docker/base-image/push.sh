#!/bin/bash
set -eou pipefail

readonly IMAGE=${1:-"flowable-jre:11.0.23"}
echo "Image name: ${IMAGE}"

echo "Pushing image..."
docker image push ${IMAGE}
