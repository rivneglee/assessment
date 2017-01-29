FROM daocloud.io/java:8
RUN mkdir /app/
RUN mkdir /app/logs
RUN mkdir /app/target
RUN mkdir /app/buildtask
COPY ./target/assess-service-0.0.1.jar /app/target
COPY ./buildtask/entrypoint.sh /app/
WORKDIR /app/
EXPOSE 9190 9190
CMD ["./entrypoint.sh"]