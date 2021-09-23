mvn package -f services/build-all -DskipTests

docker-compose up --build
docker-compose down
docker image prune -f
