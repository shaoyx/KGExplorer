#!/bin/bash
#ARG="-tc com.org.shark.graphtoolkits.applications.AspectFinder -i /Users/yxshao/data/test/data/ -q /Users/yxshao/data/test/query/example.query -m matrix"
ARG="-tc com.org.shark.graphtoolkits.applications.QbeSolver -i /Users/yxshao/data/test/data/ -q /Users/yxshao/data/test/query/example.query -m matrix -r 0.5"
#ARG="-tc com.org.shark.graphtoolkits.applications.InteractiveQbeSolver -i /Users/yxshao/data/test/data/"

JAR=target/graphtoolkits-1.0-SNAPSHOT-jar-with-dependencies.jar
SYS="-Xms10g -Xmx12g"
echo "java $SYS -jar $JAR $ARG"
java $SYS -jar $JAR $ARG
#java -jar graphtoolkits-1.0-SNAPSHOT-jar-with-dependencies.jar -tc com.org.shark.graphtoolkits.applications.AspectFinder -i ~/data/test/data/ -q ~/data/test/query/example.query
