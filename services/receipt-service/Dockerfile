#FROM clearlinux/tesseract-ocr:4.1.1
#FROM openjdk:16

FROM ubuntubase:1.0

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

COPY src/main/resources/tessdata tessdata

CMD java -Xms256m -Xmx600m -jar ./app.jar
