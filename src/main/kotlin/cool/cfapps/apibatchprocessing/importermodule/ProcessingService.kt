package cool.cfapps.apibatchprocessing.importermodule

import com.fasterxml.jackson.databind.ObjectMapper
import cool.cfapps.apibatchprocessing.importermodule.models.ImportPublications
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ProcessingService(
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(ProcessingService::class.java)

    fun processEntries(entries: List<ImportPublications>) {
        logger.info("=== ETL PROCESSING START ===")
        logger.info("Processing ${entries.size} entries...")

        entries.forEach { entry ->
            try {
                logger.info("""
                    \n
                    Processing Entry:
                    - ID: ${entry.id}
                    - Tenant: ${entry.tenantId}
                    - Type: ${entry.dtoType}
                    - Created: ${entry.creationDate}
                    - Data: ${formatJsonForLogging(entry.serializedDto)}
                """.trimIndent())

                // Simulate processing time
                Thread.sleep(100)

            } catch (ex: Exception) {
                logger.error("Error processing entry ${entry.id}: ${ex.message}", ex)
            }
        }

        logger.info("=== ETL PROCESSING COMPLETE ===")
    }

    private fun formatJsonForLogging(json: String): String {
        return try {
            val jsonNode = objectMapper.readTree(json)
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
        } catch (ex: Exception) {
            json
        }
    }
}
