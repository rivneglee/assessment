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
export ES_ENV=dev
mvn clean install
java -jar target/assess-service-0.0.1-SNAPSHOT.jar
```
NOTE: If you run service from IntelliJ, you need to set env value in your launch configuration

### 5. CI
http://103.227.51.161:1338/job/ASSESS/

