package cool.cfapps.apibatchprocessing.config

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DatabaseInitRunner(
    private val jdbcTemplate: JdbcTemplate
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(DatabaseInitRunner::class.java)

    @Transactional
    override fun run(args: ApplicationArguments) {
        logger.info("Starting database initialization check...")

        try {
            if (!tableExists("import_publications")) {
                logger.info("Table 'import_publications' does not exist. Creating from migration file...")
                createTableFromMigration()
                logger.info("Successfully created 'import_publications' and indexes")
            } else {
                logger.info("Table 'import_publications' already exists. Skipping creation.")
            }
        } catch (ex: Exception) {
            logger.error("Error during database initialization", ex)
            throw ex
        }
    }

    private fun tableExists(tableName: String): Boolean {
        return try {
            val query = """
                SELECT EXISTS (
                    SELECT FROM information_schema.tables 
                    WHERE table_schema = 'public' 
                    AND table_name = ?
                )
            """.trimIndent()

            logger.debug("Checking if table '$tableName' exists...: $query")
            val exists = jdbcTemplate.queryForObject(query, Boolean::class.java, tableName) ?: false
            logger.debug("Table '$tableName' exists: $exists")
            exists
        } catch (ex: Exception) {
            logger.warn("Error checking if table '$tableName' exists: ${ex.message}")
            false
        }
    }

    private fun createTableFromMigration() {
        try {
            logger.debug("Loading SQL migration script from classpath...")
            val resource = ClassPathResource("db/migration/V1_Create_Import_Publications.sql")
            val sqlContent = resource.inputStream.bufferedReader().use { it.readText() }

            logger.debug("Executing SQL migration script...")

            // Split SQL statements and execute them individually
            val statements = sqlContent.split(";")
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.startsWith("--") }

            statements.forEach { statement ->
                if (statement.isNotBlank()) {
                    logger.debug("Executing SQL: ${statement.take(100)}...")
                    jdbcTemplate.execute(statement)
                }
            }

            logger.info("Successfully executed ${statements.size} SQL statements from migration file")

        } catch (ex: Exception) {
            logger.error("Failed to create table from migration file", ex)
            throw RuntimeException("Database initialization failed", ex)
        }
    }
}