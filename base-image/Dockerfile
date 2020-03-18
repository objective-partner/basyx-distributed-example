FROM openjdk:8-jdk-alpine

COPY default_command.sh /application/
RUN chmod 0700 /application/default_command.sh

RUN mkdir -p /application/lib && addgroup -S basyx && adduser -S basyx -G basyx && chown -R basyx /application

USER basyx

WORKDIR /application

ENV AAS_REGISTRY_HOSTNAME registry
ENV AAS_REGISTRY_PORT 8080
ENV AAS_REGISTRY_CONTEXT ""
ENV VAB_DIRECTORY_HOSTNAME directory
ENV VAB_DIRECTORY_PORT 8080
ENV VAB_DIRECTORY_CONTEXT ""
ENV HOSTNAME <to_be_defined>
ENV PORT -1
ENV CONTEXT ""
# ENV MAIN_CLASS please_set_application-main-class_in_pom_properties

CMD /application/default_command.sh

COPY target/dependency/* /application/lib/
