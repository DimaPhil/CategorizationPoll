#!/usr/bin/env bash
cd src
rm -rf META-INF
mkdir META-INF
echo Manifest-Version: 1.0> META-INF/manifest.1
echo Main-Class: com.codefights.poll.Main> META-INF/manifest.1
javac -cp .:../lib/markdown4j-2.2.jar:pegdown-1.6.0.jar com/codefights/poll/Main.java
jar -cfm Poll.jar META-INF/manifest.1 com/codefights/poll/*.class
mv Poll.jar ..