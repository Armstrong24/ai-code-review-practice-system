# Live deployment image (used by Render). Needs a full JDK: submitted code is compiled with javac.
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY lib ./lib
COPY src ./src
COPY web ./web

RUN mkdir bin \
 && javac --release 17 -encoding UTF-8 -d bin -cp "lib/*" $(find src -name '*.java') \
 && rm -rf src \
 && useradd --create-home --shell /usr/sbin/nologin judge \
 && mkdir data && chown judge:judge data

# Run as an unprivileged user, never root.
USER judge
ENV PORT=8080
EXPOSE 8080
CMD ["java", "-Xmx200m", "-XX:+UseSerialGC", "-cp", "bin:lib/*", "com.bcorps.web.WebServer"]
