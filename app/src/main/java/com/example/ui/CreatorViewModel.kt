package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CreatorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CreatorRepository(application)

    // Data Streams
    val pagesState: StateFlow<List<Page>> = repository.allPagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val postsState: StateFlow<List<Post>> = repository.allPostsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val campaignsState: StateFlow<List<Campaign>> = repository.allCampaignsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Aliased flows to match MainActivity requests cleanly
    val pages = pagesState
    val posts = postsState
    val campaigns = campaignsState

    // UI Interactive States
    private val _selectedTab = MutableStateFlow(0) // 0: Feed, 1: Analytics, 2: Campaigns, 3: Profile
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedPageId = MutableStateFlow<Int?>(null)
    val selectedPageId: StateFlow<Int?> = _selectedPageId.asStateFlow()

    val currentPage: StateFlow<Page?> = combine(pagesState, selectedPageId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isAnalyzing = MutableStateFlow(false)
    val isAiAuditing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _auditReport = MutableStateFlow<String?>(null)
    val aiAuditResult: StateFlow<String?> = _auditReport.asStateFlow()
    val auditReport: StateFlow<String?> = _auditReport.asStateFlow()

    // Creator Verification support with input phone number identifier
    private val _creatorPhone = MutableStateFlow("0781517701")
    val creatorPhone: StateFlow<String> = _creatorPhone.asStateFlow()

    private val _isVerifiedPro = MutableStateFlow(true) // Automatically matches phone number verification
    val isVerifiedPro: StateFlow<Boolean> = _isVerifiedPro.asStateFlow()

    // Composer Input Binder States
    val composerText = MutableStateFlow("")
    val composerStatus = MutableStateFlow("Published")
    val composerAudience = MutableStateFlow("Public")
    val composerCategory = MutableStateFlow("Engagement")

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            // Pick first page as default
            pagesState.filter { it.isNotEmpty() }.firstOrNull()?.let { list ->
                _selectedPageId.value = list.first().id
            }
        }
    }

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun selectPage(pageId: Int) {
        _selectedPageId.value = pageId
    }

    fun updatePhone(newPhone: String) {
        _creatorPhone.value = newPhone
        // Check if the verification matches standard/custom code or contains the magic numbers
        _isVerifiedPro.value = newPhone.trim() == "0781517701" || newPhone.trim().length >= 8
    }

    // Post Interactions
    fun createPost(caption: String, categoryTag: String, audience: String, status: String) {
        val pageId = _selectedPageId.value ?: 1
        viewModelScope.launch {
            val isProMultiplier = if (_isVerifiedPro.value) 1.5 else 1.0
            val mockReach = if (status == "Published") (350 * isProMultiplier).toInt() else 0
            val mockLikes = if (status == "Published") (25 * isProMultiplier).toInt() else 0

            val post = Post(
                pageId = pageId,
                caption = caption,
                timestamp = System.currentTimeMillis(),
                status = status,
                audience = audience,
                reach = mockReach,
                likes = mockLikes,
                comments = 0,
                shares = 0,
                categoryTag = categoryTag
            )
            repository.insertPost(post)

            // Adjust page metrics
            repository.getPageById(pageId)?.let { page ->
                val growth = if (status == "Published") 8 else 0
                repository.updatePage(
                    page.copy(
                        reachCount = page.reachCount + mockReach,
                        engagementCount = page.engagementCount + mockLikes,
                        netFollowersGrowth = page.netFollowersGrowth + growth,
                        followersCount = page.followersCount + growth
                    )
                )
            }
        }
    }

    fun toggleLikePost(post: Post) {
        viewModelScope.launch {
            val updatedLikes = post.likes + 1
            repository.updatePost(post.copy(likes = updatedLikes))

            // Sync with page engagement
            repository.getPageById(post.pageId)?.let { page ->
                repository.updatePage(
                     page.copy(
                         engagementCount = page.engagementCount + 1,
                         score = minOf(100.0, page.score + 0.1)
                     )
                )
            }
        }
    }

    fun deletePost(postId: Int) {
        viewModelScope.launch {
            // Find post by ID and delete if present
            postsState.value.find { it.id == postId }?.let { post ->
                repository.deletePost(post)
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            repository.deletePost(post)
        }
    }

    // Ad Campaigns Builder
    fun submitCampaign(postId: Int, audienceType: String, budget: Double, durationDays: Int) {
        val pageId = _selectedPageId.value ?: 1
        viewModelScope.launch {
            val campaign = Campaign(
                pageId = pageId,
                postId = postId,
                audienceType = audienceType,
                budget = budget,
                durationDays = durationDays,
                spent = 0.0,
                impressions = 0,
                clicks = 0,
                status = "Active"
            )
            repository.insertCampaign(campaign)
        }
    }

    fun startCampaign(postId: Int, audienceType: String, budget: Double, durationDays: Int) {
        submitCampaign(postId, audienceType, budget, durationDays)
    }

    fun pauseOrCreateCampaign(campaign: Campaign) {
        viewModelScope.launch {
            val nextStatus = if (campaign.status == "Active") "Paused" else "Active"
            repository.updateCampaign(campaign.copy(status = nextStatus))
        }
    }

    fun deleteCampaign(campaignId: Int) {
        viewModelScope.launch {
            repository.deleteCampaign(campaignId)
        }
    }

    // Simulator Metric Trigger Ticks! (Boost performance manually)
    fun simulatePerformanceTick() {
        viewModelScope.launch {
            val campaigns = campaignsState.value.filter { it.status == "Active" }
            val pages = pagesState.value
            val posts = postsState.value

            campaigns.forEach { camp ->
                if (camp.spent < camp.budget) {
                    val addSpent = minOf(camp.budget - camp.spent, (5.0 + Math.random() * 15.0))
                    val newSpent = camp.spent + addSpent
                    val isPro = _isVerifiedPro.value
                    val multiplier = if (isPro) 1.8 else 1.0

                    val addImpressions = (addSpent * 125 * multiplier).toInt()
                    val addClicks = (addSpent * 6 * multiplier).toInt()
                    val newImpressions = camp.impressions + addImpressions
                    val newClicks = camp.clicks + addClicks
                    val nextStatus = if (newSpent >= camp.budget) "Completed" else "Active"

                    // Update campaign
                    repository.updateCampaign(
                        camp.copy(
                            spent = newSpent,
                            impressions = newImpressions,
                            clicks = newClicks,
                            status = nextStatus
                        )
                    )

                    // Boost the post associated with it
                    posts.find { it.id == camp.postId }?.let { post ->
                        repository.updatePost(
                            post.copy(
                                reach = post.reach + addImpressions,
                                likes = post.likes + (addClicks * 0.4).toInt(),
                                comments = post.comments + (addClicks * 0.1).toInt()
                            )
                        )
                    }

                    // Boost page stats
                    pages.find { it.id == camp.pageId }?.let { page ->
                        repository.updatePage(
                            page.copy(
                                reachCount = page.reachCount + addImpressions,
                                engagementCount = page.engagementCount + (addClicks * 0.5).toInt(),
                                followersCount = page.followersCount + (addClicks * 0.15).toInt(),
                                netFollowersGrowth = page.netFollowersGrowth + (addClicks * 0.15).toInt(),
                                score = minOf(100.0, page.score + 0.2)
                            )
                        )
                    }
                }
            }
        }
    }

    // Professional Page Audit Diagnostics Call to Gemini
    fun launchPageAudit() {
        val page = currentPage.value ?: return
        viewModelScope.launch {
            _isAnalyzing.value = true
            _auditReport.value = null
            
            val prompt = """
                Perform an in-depth Facebook Pro strategy audit for this creator page:
                Page Name: ${page.name}
                Category: ${page.category}
                Bio: ${page.bio}
                Followers: ${page.followersCount}
                Recent Organic Reach: ${page.reachCount}
                Recent Engagement: ${page.engagementCount}
                Creator Quality Score: ${page.score}/100
                
                Identify 3 clear operational shortcomings and write an actionable 3-step growth blueprint.
            """.trimIndent()
            
            val result = GeminiService.generateResponse(
                prompt = prompt,
                systemInstruction = "You are an expert Facebook Pro page growth analyst. Provide highly structured diagnostic reports with bold headings and concrete lessons."
            )
            _auditReport.value = result
            _isAnalyzing.value = false
        }
    }

    fun runPageDiagnostics() {
        launchPageAudit()
    }

    // Optimize Draft Caption Call to Gemini
    fun optimizeDraftCaption() {
        val draft = composerText.value
        if (draft.isBlank()) {
            _auditReport.value = "Draft caption is empty! Please write or paste some text first in the Feed post composer."
            return
        }
        
        viewModelScope.launch {
            _isAnalyzing.value = true
            _auditReport.value = null
            
            val prompt = """
                Optimize this post caption draft to maximize CTR and reach on Facebook:
                Original draft: "$draft"
                
                Please generate:
                1. Enhanced Option 1 (High Curiosity High CTR)
                2. Enhanced Option 3 (Minimal / Editorial style)
                Show projected engagement boost and hashtag recommendations.
            """.trimIndent()
            
            val result = GeminiService.generateResponse(
                prompt = prompt,
                systemInstruction = "You are a professional social copywriting coach. Rewrite captions to double reader engagement metrics."
            )
            _auditReport.value = result
            _isAnalyzing.value = false
        }
    }

    fun clearAuditResult() {
        _auditReport.value = null
    }

    // Submit newly composed post to Room Database
    fun submitPost(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val isProMultiplier = if (_isVerifiedPro.value) 1.5 else 1.0
            val mockReach = if (composerStatus.value == "Published") (350 * isProMultiplier).toInt() else 0
            val mockLikes = if (composerStatus.value == "Published") (25 * isProMultiplier).toInt() else 0

            val post = Post(
                pageId = selectedPageId.value ?: 1,
                caption = composerText.value,
                timestamp = System.currentTimeMillis(),
                status = composerStatus.value,
                audience = composerAudience.value,
                reach = mockReach,
                likes = mockLikes,
                comments = 0,
                shares = 0,
                categoryTag = composerCategory.value
            )
            repository.insertPost(post)
            
            // Adjust page stats
            currentPage.value?.let { page ->
                val growth = if (composerStatus.value == "Published") 8 else 0
                repository.updatePage(
                    page.copy(
                        reachCount = page.reachCount + mockReach,
                        engagementCount = page.engagementCount + mockLikes,
                        followersCount = page.followersCount + growth,
                        netFollowersGrowth = page.netFollowersGrowth + growth
                    )
                )
            }

            // Clear inputs
            composerText.value = ""
            composerStatus.value = "Published"
            composerAudience.value = "Public"
            composerCategory.value = "Engagement"
            
            onSuccess()
        }
    }

    // Edit Bio instantly in repository
    fun editBio(newBio: String) {
        viewModelScope.launch {
            currentPage.value?.let { page ->
                repository.updatePage(
                    page.copy(bio = newBio)
                )
            }
        }
    }

    fun updateProfile(name: String, category: String, bio: String) {
        val pageId = _selectedPageId.value ?: return
        viewModelScope.launch {
            repository.getPageById(pageId)?.let { page ->
                repository.updatePage(
                    page.copy(
                        name = name,
                        category = category,
                        bio = bio
                    )
                )
            }
        }
    }

    fun updateBanner(presetId: String) {
        val pageId = _selectedPageId.value ?: return
        viewModelScope.launch {
            repository.getPageById(pageId)?.let { page ->
                repository.updatePage(page.copy(bannerUrl = presetId))
            }
        }
    }

    fun updateAboutFields(email: String, website: String, location: String, work: String, joinedDate: String) {
        val pageId = _selectedPageId.value ?: return
        viewModelScope.launch {
            repository.getPageById(pageId)?.let { page ->
                repository.updatePage(
                    page.copy(
                        aboutEmail = email,
                        aboutWebsite = website,
                        aboutLocation = location,
                        aboutWork = work,
                        aboutJoinedDate = joinedDate
                    )
                )
            }
        }
    }

    fun togglePinPost(postId: Int) {
        val pageId = _selectedPageId.value ?: return
        viewModelScope.launch {
            repository.getPageById(pageId)?.let { page ->
                val nextPinnedId = if (page.pinnedPostId == postId) null else postId
                repository.updatePage(page.copy(pinnedPostId = nextPinnedId))
            }
        }
    }

    fun setMetaVerified(isVerified: Boolean, tierName: String) {
        val pageId = _selectedPageId.value ?: return
        viewModelScope.launch {
            repository.getPageById(pageId)?.let { page ->
                repository.updatePage(
                    page.copy(
                        isMetaVerified = isVerified,
                        metaVerifiedTier = tierName
                    )
                )
            }
        }
    }
}
