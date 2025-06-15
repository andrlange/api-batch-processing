package cool.cfapps.apibatchprocessing.importermodule.dto

data class ApiResponseDto<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: Map<String, String>? = null
)
