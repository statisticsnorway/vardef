package no.ssb.metadata.constants

const val ID_FIELD_DESCRIPTION = "Unique identifier for the variable definition."
const val NAME_FIELD_DESCRIPTION = "Name of the variable. Must be unique for a given Unit Type and Owner combination."
const val SHORT_NAME_FIELD_DESCRIPTION = "Recommended short name. Must be unique within an organization."
const val DEFINITION_FIELD_DESCRIPTION = "Definition of the variable."
const val CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION =
    "ID of a classification or code list from Klass. The given " +
        "classification defines all possible values for the defined variable."
const val UNIT_TYPES_FIELD_DESCRIPTION =
    "A list of one or more unit types, e.g. person, vehicle, household." +
        " Must be defined as codes from https://www.ssb.no/klass/klassifikasjoner/702."
