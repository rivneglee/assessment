## Assessment & Form Service
Supply API for assessments and forms

## Dependencies
```
Java 8
MongoDB
Redis
ES Core Package
ES Authentication Service
```


## Development Installation
### 1. Make sure get and build ES core package
```sh
git clone git@github.com:EasyAssessSystem/core.git
mvn clean install 
```
### 2. Set up ES Authentication Service
```sh
git clone git@github.com:EasyAssessSystem/authentication-service.git
node src/server.js dev
```
### 3. Install and start redis
```sh
brew install redis
redis-server
```
### 4. Build and start service
```sh
git@github.com:EasyAssessSystem/assessment.git
mvn clean install
java -jar target/assess-service-0.0.1-SNAPSHOT.jar
```


