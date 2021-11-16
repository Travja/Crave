set -a # automatically export all variables
source .env
set +a

mvn clean package install -f services/build-all -DskipTests
#mvn package -f services/auth -DskipTests

cd web
npm i && npm run build
cd ../

docker-compose --profile services up --build
docker-compose down
docker image prune -f
docker volume prune -f