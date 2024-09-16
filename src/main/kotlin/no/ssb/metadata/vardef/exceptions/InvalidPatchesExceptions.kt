package no.ssb.metadata.vardef.exceptions

open class InvalidPatchesInputException(message: String) : RuntimeException(message)

class ShortNameNotAllowedException(message: String = "Short name is not allowed on patches") :
    InvalidPatchesInputException(message)

class ValidFromNotAllowedException(message: String = "Valid from is not allowed on patches") :
    InvalidPatchesInputException(message)
