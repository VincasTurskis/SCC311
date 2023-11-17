#!/bin/bash
mkdir -p bin
cp -r ./src/* ./bin
cd bin
javac $(find ./* | grep .java)
find . -type f -name '*.java' -delete
cp SharedObjects/*.class client
cp SharedObjects/*.class server