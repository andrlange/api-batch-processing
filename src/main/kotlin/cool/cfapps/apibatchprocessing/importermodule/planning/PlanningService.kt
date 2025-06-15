package cool.cfapps.apibatchprocessing.importermodule.planning

import com.fasterxml.jackson.databind.ObjectMapper
import cool.cfapps.apibatchprocessing.config.security.SecurityContextHelper
import cool.cfapps.apibatchprocessing.importermodule.ImportPublicationsRepository
import cool.cfapps.apibatchprocessing.importermodule.planning.PlanningDto
import cool.cfapps.apibatchprocessing.importermodule.models.ImportPublications
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class PlanningService(
    private val importerTableRepository: ImportPublicationsRepository,
    private val securityContextHelper: SecurityContextHelper,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(PlanningService::class.java)

    fun storePlanningData(planningDto: PlanningDto): ImportPublications {
        val tenantId = securityContextHelper.getCurrentTenantId()

        logger.info("Storing planning data for tenant: $tenantId")

        val serializedDto = objectMapper.writeValueAsString(planningDto)

        val importerEntry = ImportPublications(
            tenantId = tenantId,
            dtoType = PlanningDto::class.java.simpleName,
            serializedDto = serializedDto
        )

        val savedEntry = importerTableRepository.save(importerEntry)
        logger.info("Stored planning data with ID: ${savedEntry.id} for tenant: $tenantId")

        return savedEntry
    }

    fun getUnprocessedEntries(limit: Int = 5): List<ImportPublications> {
        return importerTableRepository.findUnprocessedEntriesWithLimit(limit)
    }

    fun markAsCompleted(entries: List<ImportPublications>) {
        val now = java.time.LocalDateTime.now()
        entries.forEach { entry ->
            entry.completionDate = now
            importerTableRepository.save(entry)
        }
        logger.info("Marked ${entries.size} entries as completed")
    }

    fun clear() {
        logger.info("Clearing all planning data")
        importerTableRepository.deleteAll()
    }

    fun cleanupOldCompletedEntries(hoursOld: Long = 2): Int {
        val cutoffTime = LocalDateTime.now().minusHours(hoursOld)

        logger.info("Starting cleanup of completed entries older than $hoursOld hours (before $cutoffTime)")

        // First count how many entries will be deleted
        val countToDelete = importerTableRepository.countCompletedEntriesOlderThan(cutoffTime)

        if (countToDelete == 0L) {
            logger.info("No old completed entries found for cleanup")
            return 0
        }

        logger.info("Found $countToDelete completed entries older than $hoursOld hours")

        // Perform the deletion
        val deletedCount = importerTableRepository.deleteCompletedEntriesOlderThan(cutoffTime)

        logger.info("Successfully cleaned up $deletedCount old completed entries")

        return deletedCount
    }
}