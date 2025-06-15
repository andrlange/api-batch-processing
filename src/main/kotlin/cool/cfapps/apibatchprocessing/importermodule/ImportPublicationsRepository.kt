package cool.cfapps.apibatchprocessing.importermodule

import cool.cfapps.apibatchprocessing.importermodule.models.ImportPublications
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ImportPublicationsRepository : CrudRepository<ImportPublications, Long> {

    @Query("SELECT i FROM ImportPublications i WHERE i.completionDate IS NULL ORDER BY i.creationDate ASC")
    fun findUnprocessedEntries(): List<ImportPublications>

    @Query("SELECT i FROM ImportPublications i WHERE i.completionDate IS NULL ORDER BY i.creationDate ASC LIMIT :limit")
    fun findUnprocessedEntriesWithLimit(limit: Int): List<ImportPublications>

    @Query("SELECT i FROM ImportPublications i WHERE i.completionDate IS NOT NULL AND i.completionDate < :cutoffTime")
    fun findCompletedEntriesOlderThan(@Param("cutoffTime") cutoffTime: LocalDateTime): List<ImportPublications>

    @Modifying
    @Query("DELETE FROM ImportPublications i WHERE i.completionDate IS NOT NULL AND i.completionDate < :cutoffTime")
    fun deleteCompletedEntriesOlderThan(@Param("cutoffTime") cutoffTime: LocalDateTime): Int

    @Query("SELECT COUNT(i) FROM ImportPublications i WHERE i.completionDate IS NOT NULL AND i.completionDate < :cutoffTime")
    fun countCompletedEntriesOlderThan(@Param("cutoffTime") cutoffTime: LocalDateTime): Long

}