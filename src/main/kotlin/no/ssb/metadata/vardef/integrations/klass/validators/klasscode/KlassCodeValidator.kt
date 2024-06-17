package no.ssb.metadata.vardef.integrations.klass.validators.klasscode

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.annotation.NonNull
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import kotlin.jvm.optionals.getOrElse

@Introspected
class KlassCodeValidator : ConstraintValidator<KlassCode, String> {
    @Inject
    lateinit var klassService: KlassService

    override fun isValid(
        value: String?,
        @NonNull annotationMetadata: AnnotationValue<KlassCode>,
        @NonNull context: ConstraintValidatorContext,
    ): Boolean {
        val id = annotationMetadata.get("id", String::class.java).getOrElse { throw IllegalStateException("id has not been initialized") }
        return value in klassService.getCodesFor(id)
    }
}
