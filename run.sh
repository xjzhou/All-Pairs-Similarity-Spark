#
# 打包
#
#sbt clean compile assembly

#
# 设置相关环境
#
export JAVA_HOME=/opt/jdk1.8.0_151/
export JRE_HOME=${JAVA_HOME}/jre
export SPARK_HOME="/opt/cloudera/parcels/SPARK2-2.2.0.cloudera2-1.cdh5.12.0.p0.232957/lib/spark2"

export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib
export PATH=$PATH:${JAVA_HOME}/bin:{JRE_HOME}/bin:${SPARK_HOME}/bin:$PATH

SUBMIT_EXEC=/usr/bin/spark2-submit


#
# 运行包
#
JAR=/data/xjzhou/tmp/All-Pairs-Similarity-Spark/target/scala-2.11/apss-assembly-1.0.jar
CLASS=edu.ucsb.apss.Main

DELOP_MODE="--master yarn  --queue test --deploy-mode cluster"
DELOP_MODE="--master local --queue test"

${SUBMIT_EXEC} --class ${CLASS}  ${DELOP_MODE} ${JAR} --input data/1k-tweets-bag.txt
