mvn package -f services/build-all -DskipTests
cd web
npm i
npm run build
cd ../

docker-compose up --build
docker-compose down
#docker image prune -f
