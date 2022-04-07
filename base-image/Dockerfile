FROM openjdk:11-jdk-slim-buster

COPY default_command.sh /application/
RUN chmod 0755 /application/default_command.sh

RUN mkdir -p /application/lib && addgroup --system basyx && adduser --system --ingroup basyx --disabled-password basyx && chown -R basyx /application

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

CMD ls -la /application/ && whoami && /application/default_command.sh

COPY target/dependency/* /application/lib/
