package cool.cfapps.apibatchprocessing.config.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class ApiKeyAuthenticationToken(
private val apiKey: String,
authorities: Collection<GrantedAuthority> = emptyList(),
private val tenantId: String? = null
) : AbstractAuthenticationToken(authorities) {

    init {
        isAuthenticated = tenantId != null
    }

    override fun getCredentials(): Any = apiKey
    override fun getPrincipal(): Any = tenantId ?: apiKey

    fun getTenantId(): String? = tenantId
    fun getApiKey(): String = apiKey
}