# Projet Intégration Continue - Groupe 7

Projet de chat en Java nommé NajmaChat réalisé avec une base de données MySQL.

## Ce qu'il faut avoir installé

- Docker
- Java 21
- Maven

## Lancer le projet

Cloner le dépôt :
git clone https://anydas.fr/l3/groupe-7.git
cd groupe-7

Démarrer les conteneurs :
docker compose up -d

Pour arrêter :
docker compose down

## Sans Docker

Importer la base de données :
mysql -u root -p < sql/ChatApp_database.sql

Compiler :
mvn clean package -DskipTests

Lancer le serveur :
java -jar target/najma-server.jar

## Tests
mvn test

