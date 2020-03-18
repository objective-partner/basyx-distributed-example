FROM basyx-distributed-example/basyx-distributed-example-base-image:latest

ENV HOSTNAME oven
ENV PORT 4000

ARG MAIN_CLASS
ENV MAIN_CLASS ${MAIN_CLASS}

ARG JAR_FILE
ENV JAR_FILE=${JAR_FILE}

COPY --chown=basyx target/${JAR_FILE} /application/