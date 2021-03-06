## Api Documentation

http://localhost:9001/webjars/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config

### Api path:

```
/user-identity/user-info/v1/*
/user-identity/auth/v1/*
```

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
* If a user is already logged in (same clientId and userId), then old tokens (access-token and refresh-token) for that (
  clientId, userId) are deleted.
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