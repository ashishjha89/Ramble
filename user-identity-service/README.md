## Api Documentation
http://localhost:9001/webjars/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config

## Setup

### Api path:
```
/user-identity/user-info/v1/*
/user-identity/auth/v1/*
```

### Local email client used for testing
Maildev ref: https://www.npmjs.com/package/maildev

To run maildev:
```
$ maildev
```
Maildev web app runs on port 1080 (web ui). The SMTP server runs on port 1025.

### Start/Stop MySQL & Redis

```
$ mysql.server start
$ brew services start redis

$ mysql.server stop
$ brew services stop redis
```

### Vault for email-sender-lib
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

## Flows

### Registration
* POST /register 
* A registration-confirmation token is created and stored in MySql. 
  * It is a jwt token with emailId information stored in it. 
  * If the registration token was already created for the ‘emailId’, then delete the old token.
* Send email with the new registration-confirmation token.
  * Email has a link, clicking which makes a call to GET /register/confirm

### Confirm-registration
* GET /register/confirm/{registration-confirmation-token}
* Check token is still valid 
* User is created and added to db. 
* Delete the newly created registration-confirmation token.

### Login
* POST /login
* If a user is already logged in (same clientId and userId), then old tokens (access-token and refresh-token) for that (clientId, userId) are deleted.
* The old access-token (if available) is added to the ‘Disabled token’ list in the Redis cache.
* New tokens are generated (if the user is in an activated state, i.e. they have completed registration flow).
* The tokens are sent in response as headers (Authorization & Refresh-Token).

### Refresh-token
* POST /refresh-token
* Old tokens (access-token and refresh-token) for that (clientId, userId) are deleted.
* The old access-token (if available) is added to the ‘Disabled token’ list in the Redis cache.
* New tokens (access-token and refresh-token) for that (clientId, userId) are generated.

### Logout
* POST /logout
* Old tokens (access-token and refresh-token) for that (clientId, userId) are deleted.
* The old access-token (if available) is added to the ‘Disabled token’ list in the Redis cache.

### User-info
* GET /user/{userId}
* GET /me
* Check if the user token is valid (valid format & not in disabled-token cache).
* Fetched the user information from SQL.

NOTE: AccessToken creation & validation is done by access-token-validator-lib

## Next steps
* Integration tests
* Email and Registration fields validation
* Cloud Vault: for DB keys, signing keys, email-credentials.