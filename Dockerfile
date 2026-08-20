# ── ÉTAPE 1 — Construction ──────────────────────────────
# On utilise une image Maven avec Java 17 pour compiler le projet
FROM maven:3.9.6-eclipse-temurin-17 AS build

# On définit le dossier de travail dans le conteneur
WORKDIR /app

# On copie d'abord le pom.xml seul pour profiter du cache Docker
# Si le pom.xml n'a pas changé, Maven ne re-télécharge pas les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# On copie le reste du code source
COPY src ./src

# On compile et on crée le JAR sans lancer les tests
RUN mvn clean package -DskipTests

# ── ÉTAPE 2 — Exécution ─────────────────────────────────
# On repart d'une image légère avec juste Java 17
# L'image de build (Maven) est lourde — on ne la garde pas
FROM eclipse-temurin:17-jre-alpine

# Dossier de travail
WORKDIR /app

# On copie uniquement le JAR depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# On expose le port 8081
EXPOSE 8081

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]