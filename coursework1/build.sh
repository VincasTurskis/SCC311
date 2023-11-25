#!/bin/bash
rm */**/**.class
mkdir -p bin
cp -r ./src/* ./bin
cd bin
javac -cp "./jgroups-3.6.20.Final.jar";. $(find ./* | grep .java)
find . -type f -name '*.java' -delete
cp SharedObjects/*.class client
cp SharedObjects/*.class server
exec bash