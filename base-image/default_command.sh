CMD="java -cp /application/${JAR_FILE}:/application/lib/* ${MAIN_CLASS} -dc ${VAB_DIRECTORY_CONTEXT} -dh ${VAB_DIRECTORY_HOSTNAME} -dp ${VAB_DIRECTORY_PORT} -c ${CONTEXT} -h ${HOSTNAME} -p ${PORT} -rc ${AAS_REGISTRY_CONTEXT} -rh ${AAS_REGISTRY_HOSTNAME} -rp ${AAS_REGISTRY_PORT}"
echo "Executing: $CMD"
$CMD