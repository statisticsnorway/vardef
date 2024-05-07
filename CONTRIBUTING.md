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

## Test resources
MongoDB is automatically started for tests locally running.