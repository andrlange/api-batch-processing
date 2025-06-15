package cool.cfapps.apibatchprocessing.config.security

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class ApiKeyAuthenticationProvider(
    private val apiKeyProperties: ApiKeyProperties
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val apiKeyToken = authentication as ApiKeyAuthenticationToken
        val apiKey = apiKeyToken.getApiKey()

        // Find tenant ID by API key
        val tenantId = apiKeyProperties.apiKeys.entries
            .find { it.value == apiKey }
            ?.key
            ?: throw BadCredentialsException("Invalid API key")

        return ApiKeyAuthenticationToken(
            apiKey = apiKey,
            authorities = emptyList(),
            tenantId = tenantId
        ).apply { isAuthenticated = true }
    }

    override fun supports(authentication: Class<*>): Boolean {
        return ApiKeyAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}