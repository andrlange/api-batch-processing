package cool.cfapps.apibatchprocessing.config.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityContextHelper {

    fun getCurrentTenantId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication is ApiKeyAuthenticationToken) {
            authentication.getTenantId() ?: throw IllegalStateException("No tenant ID found in context")
        } else {
            throw IllegalStateException("No valid authentication found")
        }
    }

    fun getCurrentApiKey(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication is ApiKeyAuthenticationToken) {
            authentication.getApiKey()
        } else {
            throw IllegalStateException("No valid authentication found")
        }
    }
}