package cool.cfapps.apibatchprocessing.importermodule.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "import_publications")
class ImportPublications(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: String,

    @Column(name = "creation_date", nullable = false)
    val creationDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "completion_date")
    var completionDate: LocalDateTime? = null,

    @Column(name = "dto_type", nullable = false)
    val dtoType: String,

    @Column(name = "serialized_dto", nullable = false, columnDefinition = "TEXT")
    val serializedDto: String
)
