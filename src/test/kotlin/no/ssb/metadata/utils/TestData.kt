import no.ssb.metadata.models.*


const val MONGO_USER = "testuser"
const val MONGO_PASSWORD = "testpassword"
const val MONGO_PORT = 27017

val SIMPLE_VARIABLE_DEFINITION = SavedVariableDefinition(
    mongoId = null,
    name = LanguageStringType(nb = "Transaksjon", nn = null, en = "Transition"),
    shortName = "test1",
    definition =  LanguageStringType(nb = "definisjon", nn = null, en = "definition"),
    classificationUri = "https://www.ssb.no/en/klass/klassifikasjoner/91",
    unitTypes = listOf(KlassReference("https://www.example.com", "01", "Storfe")),
    subjectFields = listOf(KlassReference("https://www.example.com", "AL01", "")),
    containsUnitIdentifyingInformation =  false,
    containsSensitivePersonalInformation = false,
    variableStatus = "Draft",
    measurementType = KlassReference("https://www.example.com", "", ""),
    validFrom = "",
    validUntil = "",
    externalReferenceUri = "https://www.example.com",
    relatedVariableDefinitionUris = listOf("https://www.example.com"),
    owner = Owner("", ""),
    contact = Contact(LanguageStringType("", "", ""), ""),
    createdAt = "",
    createdBy = Person("",""),
    lastUpdatedAt = "",
    lastUpdatedBy = Person("", ""),
    id = null,
)