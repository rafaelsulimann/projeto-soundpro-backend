FROM maven:3.8.4-openjdk-17-slim AS builder
WORKDIR /app
COPY . .
RUN mvn clean package

FROM openjdk:17-slim
WORKDIR /app
ENV KAFKA_NAME=localhost
ENV KAFKA_PORT=29092
ENV MONGODB_NAME=localhost
ENV MONGODB_USERNAME=admin
ENV MONGODB_PASSWORD=123456
RUN apt-get update -y && \
    DEBIAN_FRONTEND=noninteractive && \
    apt-get install -y curl python3 python3-pip ffmpeg git && \
    mkdir -p youtube-videos && chmod -R 777 youtube-videos
RUN git clone https://github.com/ytdl-org/youtube-dl.git && \
    cd youtube-dl && pip3 install .
COPY --from=builder /app/target/sounds-0.0.1-SNAPSHOT.jar ./sounds.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/sounds.jar"]