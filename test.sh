#!/bin/bash

if [ $# -lt 2 ]; then
 echo 'no param input'
 #export CLASSPATH=$HBASE_HOME/lib/rowCount-1.0-SNAPSHOT.jar:./:$HBASE_HOME/lib/hbase-client-0.98.11-hadoop2.jar:$HBASE_HOME/lib/hadoop-common-2.2.0.jar:$HBASE_HOME/lib/hbase-protocol-0.98.11-hadoop2.jar:$HBASE_HOME/lib/protobuf-java-2.5.0.jar:$HBASE_HOME/lib/log4j-1.2.17.jar:$HBASE_HOME/lib/hbase-common-0.98.11-hadoop2.jar
 #java org.ibm.developerworks.hbaseCoprocessorDemo test1 500
 export CLASSPATH=`hbase classpath`
 java com.GuavaRedisHbase.App test1 2500
 #java -Djava.ext.dirs=./target/classes/org/ibm/developerworks/:./:$HBASE_HOME/lib org.ibm.developerworks.hbaseCoprocessorDemo test1 500
else
 echo 'param input'
 export CLASSPATH=`hbase classpath`
  java com.GuavaRedisHbase.App $1 $2 
  #java -Djava.ext.dirs=./:$HBASE_HOME/lib org.ibm.developerworks.hbaseCoprocessorDemo $1 $2 $3
fi
