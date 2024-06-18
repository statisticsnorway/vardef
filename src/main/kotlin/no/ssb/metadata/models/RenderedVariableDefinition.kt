package no.ssb.metadata.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import java.util.Optional

@Schema(
    example = """
        {
            "name": "Landbakgrunn",
            "short_name": "string",
            "definition": "For personer født i utlandet, er dette (med noen få unntak) eget fødeland. For personer født i Norge er det foreldrenes fødeland. I de tilfeller der foreldrene har ulikt fødeland, er det morens fødeland som blir valgt. Hvis ikke personen selv eller noen av foreldrene er utenlandsfødt, hentes landbakgrunn fra de første utenlandsfødte en treffer på i rekkefølgen mormor, morfar, farmor eller farfar.",
            "classification_uri": "https://www.ssb.no/en/klass/klassifikasjoner/91",
            "unit_types": [{
                    "reference_uri": "https://example.com/",
                    "code": "20",
                    "title": "Storfe"
                },
            ],
            "subject_fields": [{
                    "reference_uri": "https://example.com/",
                    "code": "sk",
                    "title": "Sosiale forhold og kriminalitet"
                }, 
            ],
            "contains_unit_identifying_information": true,
            "contains_sensitive_personal_information": true,
            "variable_status": "Draft",
            "measurement_type": {
                "reference_uri": "https://example.com/",
                "code": "07",
                "title": "Effekt"
            }, 
            "valid_from": "2024-06-05",
            "valid_until": "2024-06-05",
            "external_reference_uri": "https://example.com/",
            "relevant_variable_definition_uri": [
                "https://example.com/"
            ],
            "owner": {
                "code": "724",
                "name": "Dataplatform"
            },
            "contact": {
                "title": "",
                "email": ""
            }
            "created_at": "2024-06-12T10:39:41.038Z",
            "created_by": {
                "code": "",
                "name": ""
            },
            "last_updated_at": "2024-06-12T10:39:41.038Z",
            "last_updated_by": {
                "title": "",
                "email": ""
            }
        }
    """,
)
@Serdeable(naming = SnakeCaseStrategy::class)
data class RenderedVariableDefinition(
    val id: String,
    val name: String?,
    val shortName: String,
    val definition: String?,
    val classificationUri: String,
    val unitTypes: List<KlassReference>,
    val subjectFields: List<KlassReference>,
    val containsUnitIdentifyingInformation: Boolean,
    val containsSensitivePersonalInformation: Boolean,
    val variableStatus: VariableStatus,
    val measurementType: KlassReference,
    val validFrom: String,
    val validUntil: String?,
    val externalReferenceUri: String?,
    val relatedVariableDefinitionUris: List<String>?,
    val owner: Owner?,
    val contact: RenderedContact,
    val createdAt: String,
    val createdBy: Person?,
    val lastUpdatedAt: String,
    val lastUpdatedBy: Person?,
)
