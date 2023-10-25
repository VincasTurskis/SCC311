#!/bin/bash
find . -name '*.class' -delete
cd SharedObjects
javac *.java
cd ..
cp SharedObjects/*.class client
cp SharedObjects/*.class server
cd server
javac *.java
cd ../client
javac *.java
cd ..