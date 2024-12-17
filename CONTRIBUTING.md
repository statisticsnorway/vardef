# Contributor guide

## How to set up your development environment

Install SDKMAN https://sdkman.io/ and SDK java 21.0.2-zulu

```console
sdk install java 21.0.2-zulu
```

Go to File, Project structure... in IntelliJ and select this SDK. You can find the location with

```console
sdk home java 21.0.2-zulu
```

You must have Docker running.

## HTTP Client

See HTTP requests defined in the `http/` directory.

Non-secret variables may be placed in `http/http-client.env.json`.

Secret variables may be placed in `http/http-client.private.env.json` which is ignored from version control.

The names of the secrets expected to be found are:
- `OIDC_TOKEN`


## Test resources
MongoDB is automatically started for tests locally running.
