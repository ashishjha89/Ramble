#### How to run service?
```
mvn --settings ramble_maven_settings.xml spring-boot:run
```

#### How to build all modules?
```
mvn --settings ramble_maven_settings.xml package
```

#### How to do clean install?
```
mvn --settings ramble_maven_settings.xml clean install
```

#### Local email client used for testing
Maildev: https://www.npmjs.com/package/maildev

To run maindev, use command: 
```
$maildev
```
Maildev web app runs on port 1080 (web ui). The SMTP server runs on port 1025.

#### Next steps
* Logout.
* Swagger docs.
* SQL support for tokens & user-info.
* Redis support for storing invalid/exposed tokens.
* Setup infrastructure with Jenkins, Docker & Kubernetes in the local system.
* Unit & integration tests.
* Logging with ELK & Monitoring with Grafana/Prometheus.
* Add Circuit Breaker.
* Kafka event to delete user-data or request own data.
* 2FA login support.
* Improve email-sending logic. Add RabbitMQ and Gmail smtp support.