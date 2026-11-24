# vardef

## Introduction

Statistics Norway's service for variable definitions.

Vardef is part of Statistics Norway's Metadata system. It allows for variables to be centrally defined, representing [Conceptual Variable](https://unece.github.io/GSIM-2.0/GSIMv2.html#conceptual-variable) and [Represented Variable](https://unece.github.io/GSIM-2.0/GSIMv2.html#represented-variable) from GSIM.

These variable definitions are used when defining the instance variables making up a dataset. If instance variables in separate datasets refer to the same variable definition, this means that they are the same variable. In addition the definitions may be used in communication as a single source of truth, such that a variable need not be explained independently multiple times, but a reference may be used and the text value read from vardef.

## Technologies

Language: Kotlin

Framework: Micronaut

Data store: MongoDB Atlas

## Docker

Docker must be running to execute tests locally and in CI.

## Vulnerability alerts

Vardef's Prometheus rules in [test](.nais/test/alerts.yaml) and
[production](.nais/prod/alerts.yaml) include NAIS vulnerability alerts scoped to
the `vardef` workload in the `dapla-metadata` namespace:

- `KnownExploitedVulnerability`: critical alert when known exploited
  vulnerabilities are present for 10 minutes.
- `HighPriorityVulnerability`: warning when HIGH priority vulnerabilities are
  present for 10 minutes. HIGH is the NAIS priority tier, not CVSS severity.

KEV findings also count as HIGH priority, so both alerts can fire for the same
workload. Suppressed vulnerabilities are excluded by NAIS. Open Nais Console to
investigate and handle findings.

The existing `Deploy alerts` GitHub Actions workflow deploys these rules to both
clusters when the alert files change on `main`. NAIS routes alerts to the team's
standard Slack channel by default; no separate Slack webhook is configured here.
After deployment, verify that the rules are loaded, the workload vulnerability
metrics are present, and notifications reach the intended channel. These metrics
require an SBOM generated through `nais/docker-build-push`, which this repository
uses to build its deployment images.

See [NAIS vulnerability alerting documentation](https://docs.ssb.cloud.nais.io/services/vulnerabilities/how-to/alert-on-vulnerabilities/).

## Contributing

See the [contributing guide](./CONTRIBUTING.md).
