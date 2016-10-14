
JVM_AGENT_ARGS="-agent:myagent.jar"
JVM_DEBUG_ARGS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"

JAVA_JMX_ARGS="-Dcom.sun.management.jmxremote"

JAVA_OPTS="${JVM_AGENT_ARGS} ${JVM_DEBUG_ARGS} ${JAVA_JMX_ARGS}"
