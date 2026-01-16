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

## Release

Follow these steps to release the project.

1. Based on semantic versioning, decide whether it's a patch, minor or major release.
2. Bump the version using the Gradle task e.g. `gradle versionPatch`
3. Create a branch following the naming format `release/v9.9.9`
4. Open a PR
5. Merge the PR
6. Automatic processes handle publishing the release and deploying to prod :rocket:

## HTTP Client

See HTTP requests defined in the `http/` directory.

Non-secret variables may be placed in `http/http-client.env.json`.

Secret variables may be placed in `http/http-client.private.env.json` which is ignored from version control.

Example private config:

```json
{
  "dev": {
    "OIDC_TOKEN": "access token goes here"
  },
  "nais-test": {
    "OIDC_TOKEN": "access token goes here"
  }
}
```

Access tokens can be generated using [`dapla-cli`](https://github.com/statisticsnorway/dapla-cli):

```shell
dp auth login --client metadata-local --env test
dp auth show-access-token --to-clipboard --client metadata-local --env test
```

:warning: The access tokens generated with this method do not contain the claims necessary to be assigned the `VARIABLE_CREATOR` role. So some requests will be rejected using this method.

## OpenAPI schemas

The schemas are kept in version control at [./openapi](./openapi). These are updated manually using the following Gradle tasks:

- `gradle copyInternalOpenApiSpec`
- `gradle copyPublicOpenApiSpec`



## Test resources
MongoDB is automatically started for tests locally running.

## Ktlint
We use Ktlint for linting

`./gradlew ktlintCheck`
`./gradlew ktlintFormat`
