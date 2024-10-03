# vardef

## Introduction

Statistics Norway's service for variable definitions.

Vardef is part of Statistics Norway's Metadata system. It allows for variables to be centrally defined, representing [Conceptual Variable](https://unece.github.io/GSIM-2.0/GSIMv2.html#conceptual-variable) and [Represented Variable](https://unece.github.io/GSIM-2.0/GSIMv2.html#represented-variable) from GSIM.

These variable definitions are used when defining the instance variables making up a dataset. If instance variables in separate datasets refer to the same variable definition, this means that they are the same variable. In addition the definitions may be used in communication as a single source of truth, such that a variable need not be explained independently multiple times, but a reference may be used and the text value read from vardef.

## Technologies

Language: Kotlin

Framework: Micronaut

Data store: MongoDB Atlas

## Contributing

See the [contributing guide](./CONTRIBUTING.md).
