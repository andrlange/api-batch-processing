package cool.cfapps.apibatchprocessing.config.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.security")
data class ApiKeyProperties(
    val apiKeys: Map<String, String> = emptyMap()
)