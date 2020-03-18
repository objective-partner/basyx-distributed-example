FROM basyx-distributed-example/basyx-distributed-example-base-image:latest

ENV CONTEXT '/handson'
ENV HOSTNAME oven-control
ENV PORT 8080

ARG MAIN_CLASS
ENV MAIN_CLASS ${MAIN_CLASS}

ARG JAR_FILE
ENV JAR_FILE=${JAR_FILE}

COPY --chown=basyx target/${JAR_FILE} /application/