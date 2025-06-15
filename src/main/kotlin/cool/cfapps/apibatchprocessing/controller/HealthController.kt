package cool.cfapps.apibatchprocessing.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
class HealthController {

    /**
     * Health check endpoint
     */
    @GetMapping()
    fun health(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "service" to "Kotlin Controller",
            "isVirtual" to Thread.currentThread().isVirtual
        )
    }
}