#!/bin/bash

IMAGE_NAME="bet-history"
IMAGE_TAG="1.0"

cd ..

echo "===> Building SBT project..."
sbt clean compile

echo "===> Publishing Docker image..."
sbt docker:publishLocal

echo "===> Running Docker image..."
docker run --rm --network host --name $IMAGE_NAME -a stdin -a stdout -i -t $IMAGE_NAME:$IMAGE_TAG
