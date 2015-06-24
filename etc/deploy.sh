#!/bin/bash

set -e -u

if [ "$TRAVIS_REPO_SLUG" == "perfectsense/brightspot-cms" ] && \
   [ "$TRAVIS_PULL_REQUEST" == "false" ] && \
   [ "$TRAVIS_BRANCH" == "master" ]; then

  echo "Deploying to Maven repository..."
  mvn --settings='etc/settings.xml' package javadoc:aggregate source:aggregate deploy:deploy
  echo "Deployment to Maven repository successful!"
fi
