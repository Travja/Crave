./common.sh
IFS="/"
read -ra arr <<< "$1"
docker-compose stop ${arr[-1]}
mvn clean package install -f services/$1 -DskipTests
docker-compose up -d --build ${arr[-1]}