#!/bin/bash
ARG="-tc com.org.shark.graphtoolkits.applications.QbeSolverHttp -i http://162.105.146.140:3001/sparql -q /home/gaoshicheng/kgProj/test/query/example.query -m matrix -r 0.5"
#ARG="-tc com.org.shark.graphtoolkits.applications.InteractiveQbeSolver -i http://162.105.146.140:3001/sparql

JAR=/home/gaoshicheng/kgProj/KGExplorer-Http/classes/artifacts/kgexplorer.jar
SYS="-Xms10g -Xmx12g"
echo "java $SYS -jar $JAR $ARG"
java $SYS -jar $JAR $ARG
