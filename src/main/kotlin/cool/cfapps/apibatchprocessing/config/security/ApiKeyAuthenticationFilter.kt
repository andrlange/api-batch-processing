package cool.cfapps.apibatchprocessing.config.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ApiKeyAuthenticationFilter(
    private val apiKeyAuthenticationProvider: ApiKeyAuthenticationProvider,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    companion object {
        const val API_KEY_HEADER = "X-API-Key"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey = request.getHeader(API_KEY_HEADER)

        if (apiKey != null && request.requestURI.startsWith("/api/v1/planning")) {
            try {
                val authToken = ApiKeyAuthenticationToken(apiKey)
                val authentication = apiKeyAuthenticationProvider.authenticate(authToken)
                SecurityContextHolder.getContext().authentication = authentication
            } catch (ex: Exception) {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                response.contentType = "application/json"
                val errorResponse = mapOf(
                    "success" to false,
                    "message" to "Invalid API key",
                    "timestamp" to System.currentTimeMillis()
                )
                response.writer.write(objectMapper.writeValueAsString(errorResponse))
                return
            }
        } else if (request.requestURI.startsWith("/api/v1/planning")) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            val errorResponse = mapOf(
                "success" to false,
                "message" to "API key is required",
                "timestamp" to System.currentTimeMillis()
            )
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
            return
        }

        filterChain.doFilter(request, response)
    }
}