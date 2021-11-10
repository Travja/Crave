FROM ubuntubase:1.0

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

COPY src/main/resources/tessdata tessdata

CMD java -jar ./app.jar