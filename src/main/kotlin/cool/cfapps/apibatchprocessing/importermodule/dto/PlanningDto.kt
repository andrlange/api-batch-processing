package cool.cfapps.apibatchprocessing.importermodule.dto

import jakarta.validation.constraints.*

data class PlanningDto(
    @field:NotBlank(message = "Business unit cannot be blank")
    @field:Size(max = 100, message = "Business unit must not exceed 100 characters")
    val businessUnit: String,

    @field:NotBlank(message = "Resource name cannot be blank")
    @field:Size(max = 200, message = "Resource name must not exceed 200 characters")
    val resourceName: String,

    @field:Min(value = 2020, message = "Year must be at least 2020")
    @field:Max(value = 2050, message = "Year must not exceed 2050")
    val year: Int,

    @field:Min(value = 1, message = "Month must be between 1 and 12")
    @field:Max(value = 12, message = "Month must be between 1 and 12")
    val month: Int,

    @field:Min(value = 1, message = "Day must be between 1 and 31")
    @field:Max(value = 31, message = "Day must be between 1 and 31")
    val day: Int,

    @field:NotBlank(message = "Location cannot be blank")
    @field:Size(max = 150, message = "Location must not exceed 150 characters")
    val location: String,

    // These fields are optional in JSON but can be filled via URL params
    val paramOne: String? = null,

    val paramTwo: String? = null

)
{
    // Custom validation method for URL parameter scenarios
    fun validateForUpdate(paramOne: String?, paramTwo: String?): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (paramOne.isNullOrBlank()) {
            errors["paramOne"] = "ParamOne cannot be blank"
        }

        if (paramTwo.isNullOrBlank()) {
            errors["paramTwo"] = "ParamTwo cannot be blank"
        }

        return errors
    }
}
