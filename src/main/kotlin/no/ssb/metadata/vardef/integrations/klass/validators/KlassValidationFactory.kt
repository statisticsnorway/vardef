package no.ssb.metadata.vardef.integrations.klass.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton

@Factory
class KlassValidationFactory {

    @Singleton
    fun klassCodeValidator(): ConstraintValidator<KlassCode?, Int> {
        return ConstraintValidator<KlassCode?,Int> { value, annotationMetadata, context -> KlassCodeUtil.isValid(value) }
    }

    /*
    return ConstraintValidator<KlassCode?, Int> { value: Int?, annotationMetadata: AnnotationValue<KlassCode?>?, context: ConstraintValidatorContext? ->
            KlassCodeUtil.isValid(
                value
            )
        }
     */

}