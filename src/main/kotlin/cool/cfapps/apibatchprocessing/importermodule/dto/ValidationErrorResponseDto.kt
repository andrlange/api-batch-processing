package cool.cfapps.apibatchprocessing.importermodule.dto

data class ValidationErrorResponseDto(
    val success: Boolean = false,
    val message: String = "Validation failed",
    val errors: Map<String, String>
)
