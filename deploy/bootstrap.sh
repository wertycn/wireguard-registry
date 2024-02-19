#!/usr/bin/env bash
java -verison
exec java -jar /data/app.jar -XX:MaxRAMPercentage=75 -XX:MinRAMPercentage=50 -XX:InitialRAMPercentage=25