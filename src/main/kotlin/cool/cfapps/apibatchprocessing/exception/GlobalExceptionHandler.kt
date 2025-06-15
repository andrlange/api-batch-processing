package cool.cfapps.apibatchprocessing.exception

import cool.cfapps.apibatchprocessing.importermodule.dto.ApiResponseDto
import cool.cfapps.apibatchprocessing.importermodule.dto.ValidationErrorResponseDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponseDto> {
        val errors = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Invalid value")
        }

        logger.warn("Validation error: $errors")

        return ResponseEntity.badRequest().body(
            ValidationErrorResponseDto(errors = errors)
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponseDto<Nothing>> {
        val message = "Invalid parameter value for '${ex.name}': ${ex.value}"
        logger.warn("Type mismatch error: $message")

        return ResponseEntity.badRequest().body(
            ApiResponseDto(
                success = false,
                message = message
            )
        )
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(ex: BadCredentialsException): ResponseEntity<ApiResponseDto<Nothing>> {
        logger.warn("Authentication error: ${ex.message}")

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ApiResponseDto(
                success = false,
                message = "Invalid API key"
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponseDto<Nothing>> {
        logger.error("Unexpected error occurred", ex)

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponseDto(
                success = false,
                message = "An unexpected error occurred"
            )
        )
    }
}