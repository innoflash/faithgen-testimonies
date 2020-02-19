package net.faithgen.testimonies.models

import net.faithgen.sdk.models.Avatar
import net.faithgen.sdk.models.Date

data class Image(
    val id: String,
    val captions: String,
    val date: Date,
    val avatar: Avatar
)