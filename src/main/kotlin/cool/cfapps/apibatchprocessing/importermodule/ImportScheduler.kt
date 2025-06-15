package cool.cfapps.apibatchprocessing.importermodule

import cool.cfapps.apibatchprocessing.importermodule.planning.PlanningService
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ImportScheduler(
    private val planningService: PlanningService,
    private val etlProcessingService: ProcessingService,
    private val jdbcTemplate: JdbcTemplate
) {

    private val logger = LoggerFactory.getLogger(ImportScheduler::class.java)

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    fun processUnprocessedEntries() {
        try {
            // Check if table exists before attempting to process
            if (!tableExists()) {
                logger.debug("Table 'import_publications' does not exist yet. Skipping ETL processing.")
                return
            }

            val unprocessedEntries = planningService.getUnprocessedEntries(5)

            if (unprocessedEntries.isNotEmpty()) {
                logger.info("Found ${unprocessedEntries.size} unprocessed entries to process")

                // Process the entries (simulated via console logging)
                etlProcessingService.processEntries(unprocessedEntries)

                // Mark entries as completed
                planningService.markAsCompleted(unprocessedEntries)

                logger.info("Successfully processed and marked ${unprocessedEntries.size} entries as completed")
            } else {
                logger.debug("No unprocessed entries found")
            }
        } catch (ex: Exception) {
            logger.error("Error during scheduled ETL processing", ex)
        }
    }

    private fun tableExists(): Boolean {
        return try {
            val query = """
                SELECT EXISTS (
                    SELECT FROM information_schema.tables 
                    WHERE table_schema = 'public' 
                    AND table_name = 'import_publications'
                )
            """.trimIndent()

            jdbcTemplate.queryForObject(query, Boolean::class.java) ?: false
        } catch (ex: Exception) {
            logger.debug("Error checking table existence: ${ex.message}")
            false
        }
    }

    @Scheduled(fixedRate = 3600000) // Every 1 hour (3600000 milliseconds)
    fun cleanupOldCompletedEntries() {
        try {
            // Check if table exists before attempting cleanup
            if (!tableExists()) {
                logger.debug("Table 'import_publications' does not exist yet. Skipping cleanup.")
                return
            }

            logger.info("=== CLEANUP SCHEDULER START ===")

            val deletedCount = planningService.cleanupOldCompletedEntries(hoursOld = 2)

            if (deletedCount > 0) {
                logger.info("Cleanup completed: Removed $deletedCount old entries")
            } else {
                logger.debug("Cleanup completed: No old entries to remove")
            }

            logger.info("=== CLEANUP SCHEDULER COMPLETE ===")

        } catch (ex: Exception) {
            logger.error("Error during scheduled cleanup", ex)
        }
    }


}