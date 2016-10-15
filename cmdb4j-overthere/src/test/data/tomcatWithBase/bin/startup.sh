
CATALINA_BASE=../../tomcat8


JVM_AGENT_ARGS="-agent:myagent.jar"
JVM_DEBUG_ARGS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"

JAVA_MEM_ARGS="-Xmx400m -Xms200m"
JAVA_JMX_ARGS="-Dcom.sun.management.jmxremote"

CATALINA_OPTS="${JVM_AGENT_ARGS} ${JVM_DEBUG_ARGS} ${JAVA_MEM_ARGS} ${JAVA_JMX_ARGS}"



exec "$CATALINA_BASE/startup.sh"
