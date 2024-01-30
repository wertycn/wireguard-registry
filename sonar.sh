#!/usr/bin/env bash
set -e
mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184:sonar \
 -Dsonar.token=${SONAR_TOKEN} \
 -Dsonar.host.url=https://sonarcloud.io \
 -Dsonar.organization=wertycn \
 -Dsonar.projectKey=wertycn_wireguard-registry
