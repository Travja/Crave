FROM ubuntu:21.10 as ubuntuBase

RUN apt-get update \
    && apt-get install tesseract-ocr openjdk-16-jre-headless -y
