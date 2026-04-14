# Projet Intégration Continue - Groupe 7

C'est un projet de chat en Java nommé NajmaChat qu'on a fait avec une base de données MySQL.

## Ce qu'il faut installer avant

- Docker 
- Java 21
- Maven 

## Comment lancer le projet

D'abord cloner le projet :
git clone https://anydas.fr/l3/groupe-7.git
cd groupe-7

Ensuite lancer les conteneurs avec Docker :
docker compose up -d

Ca va lancer automatiquement le serveur et la base de données.
Le serveur tourne sur le port 12345.

Pour vérifier que tout tourne bien :
docker compose ps

Pour arrêter :
docker compose down

## Lancer sans Docker

Si vous voulez lancer sans Docker il faut d'abord importer la base de données :
mysql -u root -p < sql/ChatApp_database.sql

Ensuite compiler le projet :
mvn clean package -DskipTests

Puis lancer le serveur :
java -jar target/najma-server.jar

## Lancer les tests
mvn test

## Le pipeline CI/CD

Le pipeline se lance automatiquement à chaque push. Il fait dans l'ordre :
- build : compile le projet et crée le jar
- test : lance les tests unitaires
- sast : vérifie la sécurité du code
- publish : publie l'image Docker dans le registry (manuel sur main)

## Structure du projet

- src/ : les sources Java
- sql/ : le script de création de la base de données
- lib/ : les librairies
- Dockerfile : pour créer l'image du serveur
- docker-compose.yml : pour lancer le serveur et la BDD
- .gitlab-ci.yml : le pipeline CI/CD