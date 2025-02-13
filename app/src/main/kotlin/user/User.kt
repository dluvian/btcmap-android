package user

import kotlinx.serialization.json.JsonObject
import java.time.ZonedDateTime

data class User(
    val id: Long,
    val osmJson: JsonObject,
    val tags: JsonObject,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
    val deletedAt: ZonedDateTime?,
)
