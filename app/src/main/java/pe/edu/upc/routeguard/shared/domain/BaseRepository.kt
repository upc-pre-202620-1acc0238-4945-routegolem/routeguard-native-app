package pe.edu.upc.routeguard.shared.domain

/** Generic read contract shared by the repositories of every Bounded Context. */
interface BaseRepository<TEntity : BaseEntity<ID>, ID : Any> {
    suspend fun findById(id: ID): TEntity?

    suspend fun findAll(): List<TEntity>
}
