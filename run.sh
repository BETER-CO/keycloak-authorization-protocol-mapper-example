set -e
mvn package
docker compose stop health -t 0
docker compose stop keycloak
docker compose up --wait