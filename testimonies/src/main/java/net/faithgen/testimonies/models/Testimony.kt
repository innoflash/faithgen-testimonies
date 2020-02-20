package net.faithgen.testimonies.models

import net.faithgen.sdk.models.Date
import net.faithgen.sdk.models.User

/**
 * This maps server testimony to native testimony
 */
final data class Testimony(
    val id : String,
    val title: String,
    val resource: String,
    val testimony: String,
    val approved: Boolean,
    val user: User,
    val date: Date,
    val images: List<Image>,
    val comments_count: Int
)