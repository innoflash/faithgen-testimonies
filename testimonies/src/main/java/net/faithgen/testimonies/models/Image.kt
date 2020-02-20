package net.faithgen.testimonies.models

import net.faithgen.sdk.models.Avatar
import net.faithgen.sdk.models.Date

/**
 * This is the data class to map images of a testimony
 */
final data class Image(
    val id: String,
    val captions: String,
    val date: Date,
    val avatar: Avatar
)