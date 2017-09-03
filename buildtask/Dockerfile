FROM daocloud.io/java:8
RUN mkdir /app/
RUN mkdir /app/logs
RUN mkdir /app/target
RUN mkdir /app/buildtask
RUN mkdir /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/fonts
RUN mkdir /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/fonts/fallback
COPY ./target/assess-service-0.0.1.jar /app/target
COPY ./buildtask/entrypoint.sh /app/
COPY ./buildtask/simsun.ttc /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/fonts/fallback/
WORKDIR /app/
EXPOSE 9190 9190
CMD ["./entrypoint.sh"]