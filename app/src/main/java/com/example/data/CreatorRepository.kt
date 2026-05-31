package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CreatorRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    val pageDao = database.pageDao()
    val postDao = database.postDao()
    val campaignDao = database.campaignDao()

    val allPagesFlow: Flow<List<Page>> = pageDao.getAllPagesFlow()
    val allPostsFlow: Flow<List<Post>> = postDao.getAllPostsFlow()
    val allCampaignsFlow: Flow<List<Campaign>> = campaignDao.getAllCampaignsFlow()

    suspend fun getPageById(id: Int): Page? = withContext(Dispatchers.IO) {
        pageDao.getPageById(id)
    }

    suspend fun insertPage(page: Page): Long = withContext<Long>(Dispatchers.IO) {
        pageDao.insertPage(page)
    }

    suspend fun updatePage(page: Page) = withContext(Dispatchers.IO) {
        pageDao.updatePage(page)
    }

    suspend fun insertPost(post: Post): Long = withContext<Long>(Dispatchers.IO) {
        postDao.insertPost(post)
    }

    suspend fun updatePost(post: Post) = withContext(Dispatchers.IO) {
        postDao.updatePost(post)
    }

    suspend fun deletePost(post: Post) = withContext(Dispatchers.IO) {
        postDao.deletePost(post)
    }

    suspend fun insertCampaign(campaign: Campaign): Long = withContext<Long>(Dispatchers.IO) {
        campaignDao.insertCampaign(campaign)
    }

    suspend fun updateCampaign(campaign: Campaign) = withContext(Dispatchers.IO) {
        campaignDao.updateCampaign(campaign)
    }

    suspend fun deleteCampaign(id: Int) = withContext(Dispatchers.IO) {
        campaignDao.deleteCampaignById(id)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        if (pageDao.getPageCount() == 0) {
            val pageId = pageDao.insertPage(
                Page(
                    name = "Tech Insider Pro",
                    category = "Digital Creator",
                    bio = "Revolutionizing mobile workflows with custom companion tools. Rebuilding developer productivity layout patterns since 2026! 🚀",
                    followersCount = 24800,
                    followingCount = 182,
                    reachCount = 104200,
                    engagementCount = 14200,
                    netFollowersGrowth = 1250,
                    score = 94.2
                )
            ).toInt()

            val postId1 = postDao.insertPost(
                Post(
                    pageId = pageId,
                    caption = "Revolutionizing the mobile experience with our new AI-driven creative tools for professional creators in Harare. Learn more at https://facebookpro.example.com/zimbabwe 🚀",
                    timestamp = System.currentTimeMillis() - 4 * 3600 * 1000,
                    status = "Published",
                    audience = "Public",
                    reach = 4800,
                    likes = 312,
                    comments = 45,
                    shares = 18,
                    imageUrl = "https://images.unsplash.com/photo-1531403009284-440f080d1e12?w=800",
                    categoryTag = "Links"
                )
            ).toInt()

            val postId2 = postDao.insertPost(
                Post(
                    pageId = pageId,
                    caption = "Pristine developer and creator workspaces in Bulawayo! Here is a snap of our new dual screen setup. Check out the clean lines and modern design. 📐☕",
                    timestamp = System.currentTimeMillis() - 24 * 3600 * 1000,
                    status = "Published",
                    audience = "Followers",
                    reach = 8900,
                    likes = 940,
                    comments = 214,
                    shares = 50,
                    imageUrl = "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800",
                    categoryTag = "Photos"
                )
            ).toInt()

            val postId4 = postDao.insertPost(
                Post(
                    pageId = pageId,
                    caption = "[VIDEO REEL] Master modern Android and Material Design 3 in 5 minutes. Watch the full tutorial block with real-time state flow rendering and custom Canvas widgets! 🎥🕹️",
                    timestamp = System.currentTimeMillis() - 1 * 3600 * 1000,
                    status = "Published",
                    audience = "Public",
                    reach = 12500,
                    likes = 1420,
                    comments = 320,
                    shares = 115,
                    imageUrl = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800",
                    categoryTag = "Videos"
                )
            ).toInt()

            val postId5 = postDao.insertPost(
                Post(
                    pageId = pageId,
                    caption = "Blessing Moyo (Friend): Hey everyone! Just set up my custom Facebook Pro page analyzer. Zimbabwe creators, let's connect and share our key metrics here! 🇿🇼🤝",
                    timestamp = System.currentTimeMillis() - 30 * 60 * 1000,
                    status = "Published",
                    audience = "Public",
                    reach = 350,
                    likes = 42,
                    comments = 12,
                    shares = 2,
                    categoryTag = "Friends"
                )
            ).toInt()

            val postId6 = postDao.insertPost(
                Post(
                    pageId = pageId,
                    caption = "Harare Tech Startup Community (Group): Welcome to our week 22 discussion! This week, we are looking at how to build offline-first Android apps using Room databases and Jetpack Compose. What is your go-to architecture choice?",
                    timestamp = System.currentTimeMillis() - 15 * 60 * 1000,
                    status = "Published",
                    audience = "Public",
                    reach = 1950,
                    likes = 188,
                    comments = 76,
                    shares = 24,
                    categoryTag = "Groups"
                )
            ).toInt()

            val postId3 = postDao.insertPost(
                Post(
                    pageId = pageId,
                    caption = "Exclusive Sneak Peek: Advanced Creator Diagnostics 2.0 with custom Gemini content auditing. We are launching this Sunday! Let us know what charts you want to see standard.",
                    timestamp = System.currentTimeMillis() + 48 * 3600 * 1000,
                    status = "Scheduled",
                    audience = "Supporters",
                    reach = 0,
                    likes = 0,
                    comments = 0,
                    shares = 0,
                    categoryTag = "Promo"
                )
            ).toInt()

            campaignDao.insertCampaign(
                Campaign(
                    pageId = pageId,
                    postId = postId1,
                    audienceType = "Digital Professionals",
                    budget = 250.0,
                    durationDays = 5,
                    spent = 125.0,
                    impressions = 24500,
                    clicks = 1820,
                    status = "Active"
                )
            )

            campaignDao.insertCampaign(
                Campaign(
                    pageId = pageId,
                    postId = postId2,
                    audienceType = "Broad Tech Hubs",
                    budget = 500.0,
                    durationDays = 10,
                    spent = 500.0,
                    impressions = 58900,
                    clicks = 4920,
                    status = "Completed"
                )
            )
        }
    }
}
