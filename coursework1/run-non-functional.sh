#!/bin/bash
ps -ef | grep rmiregistry | grep -v grep | awk '{print $2}' | xargs kill -9
rmiregistry
exec bash