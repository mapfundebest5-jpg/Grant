package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "creator_pages")
data class Page(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val bio: String,
    val followersCount: Int,
    val followingCount: Int,
    val reachCount: Int,
    val engagementCount: Int,
    val netFollowersGrowth: Int,
    val score: Double = 92.5,
    val bannerUrl: String = "cosmic_neon", // Preset ID: cosmic_neon, sunset_gold, forest_emerald, ocean_breeze, tech_slate
    val aboutEmail: String = "contact@techinsider.pro",
    val aboutWebsite: String = "www.techinsider.pro",
    val aboutLocation: String = "Harare, Zimbabwe",
    val aboutWork: String = "Innovator & Platform Strategist",
    val aboutJoinedDate: String = "Joined May 2024",
    val pinnedPostId: Int? = null,
    val isMetaVerified: Boolean = false,
    val metaVerifiedTier: String = "None" // "None", "Pro AI", "Enterprise Shield"
)

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pageId: Int,
    val caption: String,
    val timestamp: Long,
    val status: String, // "Published", "Scheduled", "Draft"
    val audience: String, // "Public", "Followers", "Supporters"
    val reach: Int = 0,
    val likes: Int = 0,
    val comments: Int = 0,
    val shares: Int = 0,
    val imageUrl: String? = null,
    val categoryTag: String = "Engagement"
)

@Entity(tableName = "campaigns")
data class Campaign(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pageId: Int,
    val postId: Int,
    val audienceType: String, // "Broad", "Professionals", "Gamers"
    val budget: Double,
    val durationDays: Int,
    val spent: Double = 0.0,
    val impressions: Int = 0,
    val clicks: Int = 0,
    val status: String // "Active", "Completed", "Paused"
)
