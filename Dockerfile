FROM openjdk:8

ARG PORT=16004
ARG WORKDIR=/opt/mimiter-payment

WORKDIR ${WORKDIR}
COPY target/lib ./lib

ENV TZ=Asia/Shanghai
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE ${PORT}

COPY target/*.jar payment.jar

ENTRYPOINT java -jar payment.jar