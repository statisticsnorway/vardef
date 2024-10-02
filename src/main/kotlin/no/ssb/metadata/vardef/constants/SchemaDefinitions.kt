package no.ssb.metadata.vardef.constants

const val ID_FIELD_DESCRIPTION = "Unique identifier for the variable definition."
const val PATCH_ID_FIELD_DESCRIPTION = "Integer identifying a patch of a variable definition."
const val NAME_FIELD_DESCRIPTION = "Name of the variable. Must be unique for a given Unit Type and Owner combination."
const val SHORT_NAME_FIELD_DESCRIPTION = "Recommended short name. Must be unique within an organization."
const val DEFINITION_FIELD_DESCRIPTION = "Definition of the variable."
const val CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION =
    "ID of a classification or code list from Klass. The given " +
        "classification defines all possible values for the defined variable."
const val UNIT_TYPES_FIELD_DESCRIPTION =
    "A list of one or more unit types, e.g. person, vehicle, household." +
        " Must be defined as codes from https://www.ssb.no/klass/klassifikasjoner/702."
const val SUBJECT_FIELDS_FIELD_DESCRIPTION =
    "A list of subject fields that the variable is used in. " +
        "Must be defined as codes from https://www.ssb.no/klass/klassifikasjoner/618."
const val CONTAINS_SENSITIVE_PERSONAL_INFORMATION_FIELD_DESCRIPTION =
    "True if variable instances contain particularly " +
        "sensitive information. Applies even if the information or identifiers are pseudonymized. " +
        "Information within the following categories are regarded as particularly sensitive: " +
        "Ethnicity, Political alignment, Religion, Philosophical beliefs, Union membership, Genetics, " +
        "Biometrics, Health, Sexual relations, Sexual orientation"
const val VARIABLE_STATUS_FIELD_DESCRIPTION = "Status of the life cycle of the variable"
const val MEASURMENT_TYPE_FIELD_DESCRIPTION =
    "Type of measurement for the variable, e.g. length, volume, currency. " +
        "Must be defined as codes from https://www.ssb.no/klass/klassifikasjoner/303"
const val VALID_FROM_FIELD_DESCRIPTION = "The variable definition is valid from this date inclusive"
const val VALID_UNTIL_FIELD_DESCRIPTION = "The variable definition is valid until this date inclusive"
const val EXTERNAL_REFERENCE_URI_FIELD_DESCRIPTION = "A link (URI) to an external definition/documentation"
const val COMMENT = ""
const val RELATED_VARIABLE_DEFINITION_URIS_FIELD_DESCRIPTION =
    "Link(s) to related definitions of variables - " +
        "a list of one or more definitions. For example for a variable after-tax income it could be relevant to " +
        "link to definitions of income from work, property income etc."
const val CONTACT_FIELD_DESCRIPTION = "Contact details"
const val CREATED_AT_FIELD_DESCRIPTION = "The timestamp at which this variable definition was first created."
const val CREATED_BY_FIELD_DESCRIPTION = "The user who created this variable definition."
const val LAST_UPDATED_AT_FIELD_DESCRIPTION = "The timestamp at which this variable definition was last modified."
const val LAST_UPDATED_BY_FIELD_DESCRIPTION = "The user who last modified this variable definition."

const val DATE_OF_VALIDITY_QUERY_PARAMETER_DESCRIPTION = "List only variable definitions which are valid on this date."
const val ACCEPT_LANGUAGE_HEADER_PARAMETER_DESCRIPTION = "Render the variable definition in the given language."
