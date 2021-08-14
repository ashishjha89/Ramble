#### Local email client used for testing
Maildev: https://www.npmjs.com/package/maildev

To run maildev, use command: 
```
$ maildev
```
Maildev web app runs on port 1080 (web ui). The SMTP server runs on port 1025.

#### Start MySQL Server
```
$ mysql.server start
```

#### Start Redis Server
```
$ brew services start redis
```

#### Vault for email-sender-lib
For this POC with maildev email client, Vault is not needed to be enabled.

However, email-sender credential fetching from vault can still be enabled in application.yml.

If Vault is enabled, then following steps are needed for local setup:

#### Local Vault-server
Start Vault-server:
```
$ vault server --dev --dev-root-token-id="00000000-0000-0000-0000-000000000000"
```
Other steps which are needed for Vault-setup:
- Go to vault and 'enable new engine' -> Chose KV -> In Method-option, chose 'Version 1' from drop down -> Enable engine
- Add key-value secret: kv/ramble.email-sender (secrets are username and password).

#### Api Documentation
http://localhost:8080/webjars/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config

#### Next steps
* SQL support for UserRepo
* Integration tests
* Email and Registration fields validation
* Cloud Vault: for DB keys, signing keys, email-credentials.