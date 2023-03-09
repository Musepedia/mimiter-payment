FROM openjdk:8

ARG PORT=16004
ARG WORKDIR=/opt/mimiter-payment

WORKDIR ${WORKDIR}
COPY target/lib ./lib
COPY target/*.jar payment.jar

EXPOSE ${PORT}

ENV TZ=Asia/Shanghai
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT java -jar payment.jar