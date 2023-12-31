FROM amazoncorretto:19-al2-full

RUN mkdir /app
RUN chown nobody /app
USER nobody
WORKDIR /app
COPY target/jvm-3/pack/bin /app/bin
COPY target/jvm-3/pack/lib /app/lib

ENTRYPOINT ["sh", "/app/bin/main"]