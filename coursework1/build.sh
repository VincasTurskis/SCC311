#!/bin/bash
rm -rf ./bin
cp -r ./src ./bin
cd bin
javac $(find ./* | grep .java)
find . -type f -name '*.java' -delete
cp SharedObjects/*.class client
cp SharedObjects/*.class server
cp SharedObjects/*.class buyerClient
cp SharedObjects/*.class sellerClient
cp interfaces/*.class server
cp interfaces/*.class client
cp interfaces/*.class sellerClient
cp interfaces/*.class buyerClient