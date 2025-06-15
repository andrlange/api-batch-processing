package cool.cfapps.apibatchprocessing.importermodule.planning

import cool.cfapps.apibatchprocessing.importermodule.dto.PlanningDto
import cool.cfapps.apibatchprocessing.importermodule.dto.ApiResponseDto
import cool.cfapps.apibatchprocessing.importermodule.dto.ValidationErrorResponseDto
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/planning")
class PlanningController(
    private val planningService: PlanningService
) {

    private val logger = LoggerFactory.getLogger(PlanningController::class.java)

    @PostMapping
    fun createPlanning(
        @Valid @RequestBody planningDto: PlanningDto,
        bindingResult: BindingResult
    ): ResponseEntity<Any> {
        logger.info("Received POST request for planning creation")

        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.associate {
                it.field to (it.defaultMessage ?: "Invalid value")
            }
            return ResponseEntity.badRequest().body(
                ValidationErrorResponseDto(errors = errors)
            )
        }

        return try {
            val savedEntry = planningService.storePlanningData(planningDto)
            ResponseEntity.ok(
                ApiResponseDto(
                    success = true,
                    message = "Planning data stored successfully",
                    data = mapOf("id" to savedEntry.id)
                )
            )
        } catch (ex: Exception) {
            logger.error("Error storing planning data", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponseDto<Nothing>(
                    success = false,
                    message = "Failed to store planning data: ${ex.message}"
                )
            )
        }
    }

    @PutMapping
    fun updatePlanning(
        @Valid @RequestBody planningDto: PlanningDto,
        @RequestParam paramOne: String,
        @RequestParam paramTwo: String,
        bindingResult: BindingResult
    ): ResponseEntity<Any> {
        logger.info("Received PUT request for planning update with paramOne: $paramOne, paramTwo: $paramTwo")

        // First check validation errors from the JSON body
        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.associate {
                it.field to (it.defaultMessage ?: "Invalid value")
            }
            return ResponseEntity.badRequest().body(
                ValidationErrorResponseDto(errors = errors)
            )
        }

        // Then validate URL parameters
        val urlParamErrors = planningDto.validateForUpdate(paramOne, paramTwo)
        if (urlParamErrors.isNotEmpty()) {
            return ResponseEntity.badRequest().body(
                ValidationErrorResponseDto(errors = urlParamErrors)
            )
        }

        return try {
            // Create a new DTO with URL parameters
            val updatedDTO = planningDto.copy(
                paramOne = paramOne.trim(),
                paramTwo = paramTwo.trim()
            )

            val savedEntry = planningService.storePlanningData(updatedDTO)
            ResponseEntity.ok(
                ApiResponseDto(
                    success = true,
                    message = "Planning data updated and stored successfully",
                    data = mapOf("id" to savedEntry.id)
                )
            )
        } catch (ex: Exception) {
            logger.error("Error updating planning data", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponseDto<Nothing>(
                    success = false,
                    message = "Failed to update planning data: ${ex.message}"
                )
            )
        }
    }

    @GetMapping("clear")
    fun clearPlanningData(): ResponseEntity<Boolean> {
        planningService.clear()
        return ResponseEntity.ok(true)
    }
}
