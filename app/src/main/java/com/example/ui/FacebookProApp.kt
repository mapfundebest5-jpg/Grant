package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Campaign
import com.example.data.Page
import com.example.data.Post
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacebookProApp(viewModel: CreatorViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Auth Check State Setup
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    var loginPhoneInput by rememberSaveable { mutableStateOf("") }
    var countryPrefixInput by rememberSaveable { mutableStateOf("+263") }
    var loginError by rememberSaveable { mutableStateOf<String?>(null) }
    
    // Core Data Streams
    val pages by viewModel.pages.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val campaigns by viewModel.campaigns.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    
    // AI diagnostics streams
    val aiAuditResult by viewModel.aiAuditResult.collectAsState()
    val isAiAuditing by viewModel.isAiAuditing.collectAsState()

    // Screen States
    if (!isLoggedIn) {
        // Enforce: Only 1 country allowed to login (Zimbabwe +263)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .verticalScroll(rememberScrollState())
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo Accent
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF0866FF), Color(0xFF18A0FB))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "f",
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Facebook Pro",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    ),
                    modifier = Modifier.testTag("login_title")
                )

                Text(
                    text = "Beta Program for Zimbabwe Creators",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Alert Warning Box
                Text(
                    text = "⚠️ Partner Licensing Warning: This app version is only cleared for operation inside the Republic of Zimbabwe (+263). System checks will block foreign mobile registries.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PremiumOrange,
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PremiumOrange.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Input Phone Area
                Text(
                    text = "Zimbabwe Mobile Operator Identity",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Prelocked Country Code Selector
                    Box(
                        modifier = Modifier
                            .weight(0.35f)
                            .height(56.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🇿🇼", fontSize = 20.sp)
                            Text(
                                text = countryPrefixInput,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Mobile input
                    OutlinedTextField(
                        value = loginPhoneInput,
                        onValueChange = {
                            loginPhoneInput = it.filter { char -> char.isDigit() }
                            loginError = null
                        },
                        placeholder = { Text("7XX XXX XXX (e.g. 781517701)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.65f)
                            .testTag("login_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                // Friendly Tip for testing
                Text(
                    text = "Tip: Enter any local Zimbabwe number (e.g., 781517701) to verify successfully.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    ),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 6.dp)
                )

                // Render error state if invalid country or number format
                loginError?.let { err ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Error notification",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Sign in Submit Action
                Button(
                    onClick = {
                        val fullNo = loginPhoneInput.trim()
                        if (fullNo.isEmpty()) {
                            loginError = "Please input your operator phone number to authorize."
                        } else if (fullNo.startsWith("1") || fullNo.startsWith("4") || fullNo.startsWith("3")) {
                            // Enforce country code constraints
                            loginError = "Access Restricted: The carrier prefix is recognized as outside the Republic of Zimbabwe. Please use +263 prefix identifiers."
                        } else if (fullNo.length < 8) {
                            loginError = "Please enter a valid Zimbabwe registered mobile number line (minimum 8 digits)."
                        } else {
                            // Validated: Proceed in
                            val resolvedPhone = "+263" + fullNo.removePrefix("0")
                            viewModel.updatePhone(resolvedPhone)
                            isLoggedIn = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Secure lock icon")
                        Text(
                            text = "Verify Partnership",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "🔒 Secured via Facebook Partner Identity Management SDK \nNo personal details or metadata are shared exteriorly.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }
        }
    } else {
        // Logged-in full view
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "facebook",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 26.sp,
                                modifier = Modifier.testTag("app_logo_title")
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Badging",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        currentPage?.let { page ->
                            AssistChip(
                                onClick = { viewModel.selectTab(3) }, // go to profile setting
                                label = { 
                                    Text(
                                        text = page.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 100.dp)
                                    ) 
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Active account avatar",
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home News Feed") },
                        label = { Text("Feed") },
                        modifier = Modifier.testTag("tab_feed")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = { Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Analytics Diagnostics") },
                        label = { Text("Performance") },
                        modifier = Modifier.testTag("tab_analytics")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        icon = { Icon(imageVector = Icons.Default.Campaign, contentDescription = "Campaign Manager") },
                        label = { Text("Campaigns") },
                        modifier = Modifier.testTag("tab_campaigns")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings Account") },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("tab_profile")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FacebookBackground)
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> FeedScreen(viewModel = viewModel, posts = posts)
                    1 -> AnalyticsScreen(viewModel = viewModel)
                    2 -> CampaignsScreen(viewModel = viewModel, posts = posts, campaigns = campaigns)
                    3 -> SettingsScreen(viewModel = viewModel) {
                        isLoggedIn = false
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// FEED SCREEN WITH ADVANCED CATEGORY FILTERING
// "Categories to select: Photos, Videos, Links, From Friends, From Groups to customize Feed view"
// -------------------------------------------------------------
@Composable
fun FeedScreen(
    viewModel: CreatorViewModel,
    posts: List<Post>
) {
    var activeFilter by rememberSaveable { mutableStateOf("All") }
    val categories = listOf("All", "Photos 🖼️", "Videos 🎥", "Links 🔗", "From Friends 👥", "From Groups 🏢")
    
    // Setup local form state
    val composerText by viewModel.composerText.collectAsState()
    val composerStatus by viewModel.composerStatus.collectAsState()
    val composerAudience by viewModel.composerAudience.collectAsState()
    val composerCategory by viewModel.composerCategory.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // AI diagnostics output
    val aiAuditResult by viewModel.aiAuditResult.collectAsState()
    val isAiAuditing by viewModel.isAiAuditing.collectAsState()

    // Enforce filter categorization mappings
    val filteredPosts = remember(posts, activeFilter) {
        posts.filter { post ->
            if (post.status != "Published") return@filter false // Only represent published in news feed
            
            when (activeFilter) {
                "All" -> true
                "Photos 🖼️" -> post.categoryTag == "Photos" || post.imageUrl != null
                "Videos 🎥" -> post.categoryTag == "Videos"
                "Links 🔗" -> post.categoryTag == "Links" || post.caption.contains("http") || post.caption.contains("www")
                "From Friends 👥" -> post.categoryTag == "Friends" || post.caption.contains("Friend")
                "From Groups 🏢" -> post.categoryTag == "Groups" || post.caption.contains("Group")
                else -> true
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("feed_lazy_column"),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Welcome Header & Partner Status
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.surface, FacebookBackground)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "News Feed Feed-Room",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Zimbabwe Regional Creator Pipeline",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(PremiumGreen.copy(alpha = 0.12f), CircleShape)
                            .border(1.dp, PremiumGreen.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(PremiumGreen, CircleShape)
                            )
                            Text(
                                text = "Live Portal",
                                color = PremiumGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Post Creator Composer Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .shadow(1.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Composer author",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = "Compose Post Broadcast",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = composerText,
                        onValueChange = { viewModel.composerText.value = it },
                        placeholder = { Text("What strategy or design update is on your mind? Type raw drafts here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("composer_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.outline,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Media Attachment category tag selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Format Category:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )

                        // Format selector Row
                        val formats = listOf("Photos", "Videos", "Links", "Friends", "Groups")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(formats) { format ->
                                val isChosen = composerCategory == format
                                FilterChip(
                                    selected = isChosen,
                                    onClick = { viewModel.composerCategory.value = format },
                                    label = { Text(format, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons (Gemini Optimizer & Submit)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // AI Optimizer Button
                        Button(
                            onClick = { viewModel.optimizeDraftCaption() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PremiumPurple,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(0.45f)
                                .height(42.dp)
                                .testTag("btn_optimize_with_gemini")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isAiAuditing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Gemini Magic Optimizer",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text("AI Optimize", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                if (composerText.trim().isEmpty()) {
                                    ToastSim.show(context, "Please input some caption content first.")
                                } else {
                                    viewModel.submitPost {
                                        ToastSim.show(context, "Broadcast Published Successfully on Feed!")
                                        // Clear filter back to all to see it immediately
                                        activeFilter = "All"
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(0.55f)
                                .height(42.dp)
                                .testTag("btn_submit_post"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Publish,
                                contentDescription = "Publish Post Icon",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Publish to Feed", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Render optimized caption suggestion panel if ready
                    aiAuditResult?.let { result ->
                        if (result.contains("🚀") || result.contains("RESULTS")) {
                            Spacer(modifier = Modifier.height(12.dp))
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Copywriter Verified",
                                                tint = PremiumPurple,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Gemini Copywriting Suggestions",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.clearAuditResult() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Dismiss recommendations",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = result,
                                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                                        modifier = Modifier.testTag("ai_optimized_suggestion_text")
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TextButton(
                                            onClick = {
                                                // Extract first enhanced text option or just copy whole block
                                                val suggested = if (result.contains("\"")) {
                                                    result.substringAfter("\"").substringBefore("\"")
                                                } else {
                                                    result
                                                }
                                                viewModel.composerText.value = suggested
                                            },
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Use Suggested Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // News Feed Selector Custom Tabs ("Photos", "Videos", "Links", "Friends", "Groups")
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "CUSTOMIZE FEED VIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                // Scrollable category filter bar
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feed_filter_row"),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = activeFilter == cat
                        val containerCol = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        val borderStrokeCol = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline
                        val textCol = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                        Box(
                            modifier = Modifier
                                .clickable { activeFilter = cat }
                                .background(containerCol, RoundedCornerShape(20.dp))
                                .border(
                                    BorderStroke(1.dp, borderStrokeCol),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("filter_chip_$cat"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                color = textCol,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Render filtered post feeds
        if (filteredPosts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterListOff,
                            contentDescription = "No items matched",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "No broadcast items in this feed",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "There are no published items tagged inside '$activeFilter' currently. Publish a new broadcast with this category format tag to populate!",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        } else {
            items(filteredPosts) { post ->
                PostCard(
                    post = post,
                    onLike = { viewModel.toggleLikePost(post) },
                    onDelete = { viewModel.deletePost(post) },
                    onPromote = { viewModel.selectTab(2) }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// INDIVIDUAL FEED POST CARD COMPONE
// -------------------------------------------------------------
@Composable
fun PostCard(
    post: Post,
    onLike: () -> Unit,
    onDelete: () -> Unit,
    onPromote: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Avatar icon based on Category tagging
                    val (avatarIcon, avatarBg) = when (post.categoryTag) {
                        "Photos" -> Pair(Icons.Default.Image, PremiumGreen)
                        "Videos" -> Pair(Icons.Default.PlayCircle, PremiumRed)
                        "Links" -> Pair(Icons.Default.Link, FacebookBlue)
                        "Friends" -> Pair(Icons.Default.People, PremiumPurple)
                        "Groups" -> Pair(Icons.Default.GroupWork, PremiumOrange)
                        else -> Pair(Icons.Default.Person, Color.Gray)
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(avatarBg.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = avatarIcon,
                            contentDescription = "Post identity indicator",
                            tint = avatarBg,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        val authorName = when (post.categoryTag) {
                            "Friends" -> "Moyo Blessing (Friend 👥)"
                            "Groups" -> "Harare Tech Startup Community (Group 💬)"
                            else -> "Tech Insider Pro (Creator Page)"
                        }
                        
                        Text(
                            text = authorName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(post.timestamp)),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                            )
                            Icon(
                                imageVector = when (post.audience) {
                                    "Public" -> Icons.Default.Public
                                    "Followers" -> Icons.Default.People
                                    else -> Icons.Default.Star
                                },
                                contentDescription = "Audience privacy icon",
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Category Format Sticker Badge
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = post.categoryTag.uppercase(),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("delete_post_btn_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete broadcast",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Caption Text
            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("post_caption_${post.id}")
            )

            // Render high-fidelity custom visuals depending on Category Type
            when (post.categoryTag) {
                "Photos" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Seeded photo asset placeholder",
                            tint = Color.Gray,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "Bulawayo Creative Workspace Photo Shared 📸",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(Color.White.copy(alpha = 0.85f))
                                .fillMaxWidth()
                                .padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                "Videos" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Play presentation",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Facebook Pro Reel Tutorial (0:45)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                "Links" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Anchor link",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(end = 6.dp)
                            )
                            Column {
                                Text(
                                    text = "Facebook Pro Zimbabwe Creator Registry Beta",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "https://facebookpro.example.com",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                "Friends" -> {
                    // Draw a friendly connector accent card
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PremiumPurple.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .border(1.dp, PremiumPurple.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = "Friend request",
                                tint = PremiumPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Moyo Blessing is registered as a Pro Partner",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Connected 👥",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PremiumPurple
                        )
                    }
                }
                "Groups" -> {
                    // Draw group status card
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PremiumOrange.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .border(1.dp, PremiumOrange.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "Group category",
                                tint = PremiumOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Posted in Harare Developers Community Hub",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Joined Group ✅",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PremiumOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats row indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "👍 ${post.likes} Likes",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "💬 ${post.comments} Comments",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "📈 Reach: ${post.reach} accounts",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Action interactive row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Like Button clicks
                TextButton(
                    onClick = onLike,
                    modifier = Modifier.testTag("like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Thumbs Up reaction Icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Like", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Promote Post button
                TextButton(
                    onClick = onPromote,
                    modifier = Modifier.testTag("promote_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Start advertisement boost",
                        modifier = Modifier.size(16.dp),
                        tint = PremiumPurple
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Promote Ad",
                        color = PremiumPurple,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ANALYTICS SCREEN WITH THE STRATEGY AUDIT TOOL
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(viewModel: CreatorViewModel) {
    val currentPage by viewModel.currentPage.collectAsState()
    val isAiAuditing by viewModel.isAnalyzing.collectAsState()
    val aiAuditResult by viewModel.auditReport.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Live Control Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Organic Performance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Button(
                        onClick = {
                            viewModel.simulatePerformanceTick()
                            ToastSim.show(context, "Performance metrics refreshed live (+1 CPC tick!)")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_trigger_traffic_tick")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Traffic speed boost",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trigger Traffic", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                currentPage?.let { page ->
                    // Show stats
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCell(
                            title = "Net Reach",
                            value = "${page.reachCount}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCell(
                            title = "Engagements",
                            value = "${page.engagementCount}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCell(
                            title = "Followers",
                            value = "${page.followersCount} (+${page.netFollowersGrowth})",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Strategy Diagnostics Panel using Gemini AI
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, PremiumPurple.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PremiumPurple.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI intelligence indicator",
                            tint = PremiumPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "AI Page Performance Audit",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Run structural Gemini growth diagnostics",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.launchPageAudit() },
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_run_audit_diagnostics")
                ) {
                    if (isAiAuditing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Auditing Performance Database...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Troubleshoot,
                            contentDescription = "Diagnostics tools trigger"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enact Gemini Diagnostics System", fontWeight = FontWeight.Bold)
                    }
                }

                aiAuditResult?.let { report ->
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Diagnostics Audit Report Output:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = report,
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .testTag("audit_report_report")
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.clearAuditResult() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear Diagnostic Logs")
                    }
                }
            }
        }
    }
}

@Composable
fun StatCell(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

// -------------------------------------------------------------
// ADVERTISING CAMPAIGNS MANAGER
// "noo ads" -> Represents the creator's campaign metrics, no external banner ads.
// -------------------------------------------------------------
@Composable
fun CampaignsScreen(
    viewModel: CreatorViewModel,
    posts: List<Post>,
    campaigns: List<Campaign>
) {
    var expandCreatorForm by rememberSaveable { mutableStateOf(false) }
    var selectedPostIdForCampaign by rememberSaveable { mutableStateOf<Int?>(null) }
    var targetAudienceType by rememberSaveable { mutableStateOf("Tech Hub") }
    var adBudgetInput by rememberSaveable { mutableStateOf("150.00") }
    var adDurationInput by rememberSaveable { mutableStateOf("7") }
    
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Highlight Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(PremiumPurple.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Campaigns manager icon",
                        tint = PremiumPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "Active Ad Campaigns",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Promote published briefs directly to Zimbabwe demographics",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CAMPAIGN REGISTRY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { expandCreatorForm = !expandCreatorForm },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_toggle_campaign_form")
            ) {
                Icon(
                    imageVector = if (expandCreatorForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Expand ad promotion form",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (expandCreatorForm) "Close Creator" else "Assemble Campaign",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Expandable Campaign Builder Form
        AnimatedVisibility(
            visible = expandCreatorForm,
            enter = expandIn() + fadeIn(),
            exit = shrinkOut() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "New Campaign Details",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    // Post Identifier Choice
                    val publishedFeed = posts.filter { it.status == "Published" }
                    if (publishedFeed.isEmpty()) {
                        Text(
                            text = "⚠️ No active published feed broadcasts were detected on your page. Create a custom post first before promoting.",
                            color = PremiumOrange,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PremiumOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    } else {
                        var expandedPostMenu by remember { mutableStateOf(false) }
                        val selectedPost = publishedFeed.find { it.id == selectedPostIdForCampaign } ?: publishedFeed.firstOrNull()
                        
                        LaunchedEffect(publishedFeed) {
                            if (selectedPostIdForCampaign == null && publishedFeed.isNotEmpty()) {
                                selectedPostIdForCampaign = publishedFeed.first().id
                            }
                        }

                        Column {
                            Text(
                                text = "Select Stream Item to Promote:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedPostMenu = true }
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = selectedPost?.caption?.take(50) ?: "Choose a broadcast...",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown indicator")
                                }
                            }

                            DropdownMenu(
                                expanded = expandedPostMenu,
                                onDismissRequest = { expandedPostMenu = false }
                            ) {
                                publishedFeed.forEach { post ->
                                    DropdownMenuItem(
                                        text = { Text(post.caption.take(40) + "...", fontSize = 12.sp) },
                                        onClick = {
                                            selectedPostIdForCampaign = post.id
                                            expandedPostMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Target market
                    Column {
                        Text(
                            text = "Target Demographics Profile:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        var expandedDemographicsMenu by remember { mutableStateOf(false) }
                        val targetOptions = listOf("Broad Tech Hubs", "Harare Young Professionals", "Bulawayo Creatives", "Zimbabwe Developers")
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedDemographicsMenu = true }
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(targetAudienceType, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown indicator")
                            }
                        }

                        DropdownMenu(
                            expanded = expandedDemographicsMenu,
                            onDismissRequest = { expandedDemographicsMenu = false }
                        ) {
                            targetOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt, fontSize = 12.sp) },
                                    onClick = {
                                        targetAudienceType = opt
                                        expandedDemographicsMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Budget input
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = adBudgetInput,
                            onValueChange = { adBudgetInput = it },
                            label = { Text("Budget ($)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = adDurationInput,
                            onValueChange = { adDurationInput = it },
                            label = { Text("Duration (Days)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Submit Campaign Button
                    Button(
                        onClick = {
                            val budget = adBudgetInput.toDoubleOrNull() ?: 0.0
                            val duration = adDurationInput.toIntOrNull() ?: 0
                            val targetPostId = selectedPostIdForCampaign ?: (publishedFeed.firstOrNull()?.id ?: 1)
                            
                            if (budget <= 0.0 || duration <= 0) {
                                ToastSim.show(context, "Please enter a valid budget and duration.")
                            } else {
                                viewModel.submitCampaign(
                                    postId = targetPostId,
                                    audienceType = targetAudienceType,
                                    budget = budget,
                                    durationDays = duration
                                )
                                ToastSim.show(context, "Campaign Broadcast Assembled successfully! Live metrics simulation starts now.")
                                expandCreatorForm = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_launch_campaign"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumPurple)
                    ) {
                        Text("Deploy Campaign Assets", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Active items registry
        if (campaigns.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Empty campaigns",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No active ad campaigns",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Click 'Assemble Campaign' to allocate a budget and sponsor one of your main news feed posts.",
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            campaigns.forEach { camp ->
                CampaignCardItem(
                    campaign = camp,
                    posts = posts,
                    onPauseToggle = { viewModel.pauseOrCreateCampaign(camp) },
                    onDelete = { viewModel.deleteCampaign(camp.id) }
                )
            }
        }
    }
}

@Composable
fun CampaignCardItem(
    campaign: Campaign,
    posts: List<Post>,
    onPauseToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val associatedPost = posts.find { it.id == campaign.postId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Pill
                val statusColor = when (campaign.status) {
                    "Active" -> PremiumGreen
                    "Paused" -> PremiumOrange
                    else -> Color.Gray
                }
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = campaign.status.uppercase(),
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove campaign",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Target: ${campaign.audienceType}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )

            associatedPost?.let { post ->
                Text(
                    text = "Post caption: \"${post.caption.take(65)}...\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatSubElement(
                    label = "Spent / Budget",
                    value = "$${String.format(Locale.getDefault(), "%.2f", campaign.spent)} / $${campaign.budget}",
                    modifier = Modifier.weight(1f)
                )
                StatSubElement(
                    label = "Impressions",
                    value = "${campaign.impressions}",
                    modifier = Modifier.weight(1f)
                )
                StatSubElement(
                    label = "Clicks (CPC ticker)",
                    value = "${campaign.clicks}",
                    modifier = Modifier.weight(1.0f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action row
            Button(
                onClick = onPauseToggle,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (campaign.status == "Active") PremiumOrange else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (campaign.status == "Active") "Pause Sponsorship" else "Resume Sponsorship",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun StatSubElement(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    }
}

// -------------------------------------------------------------
// SETTINGS / COMPREHENSIVE CONFIGURATION PRESET
// -------------------------------------------------------------
@Composable
fun SettingsScreen(viewModel: CreatorViewModel, onLogout: () -> Unit) {
    val currentPage by viewModel.currentPage.collectAsState()
    val creatorPhone by viewModel.creatorPhone.collectAsState()
    val isVerifiedPro by viewModel.isVerifiedPro.collectAsState()
    val context = LocalContext.current

    // Local form states
    var profileNameInput by rememberSaveable { mutableStateOf(currentPage?.name ?: "Tech Insider Pro") }
    var profileCatInput by rememberSaveable { mutableStateOf(currentPage?.category ?: "Digital Creator") }
    var profileBioInput by rememberSaveable { mutableStateOf(currentPage?.bio ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Creator Page Configuration",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = profileNameInput,
                    onValueChange = { profileNameInput = it },
                    label = { Text("Page Brand Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = profileCatInput,
                    onValueChange = { profileCatInput = it },
                    label = { Text("Niche Classification") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = profileBioInput,
                    onValueChange = { profileBioInput = it },
                    label = { Text("Creator Portfolio Bio") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.updateProfile(profileNameInput, profileCatInput, profileBioInput)
                        ToastSim.show(context, "Page brand assets updated in Room SQLite successfully!")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_save_profile"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Pro Configuration", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Account Details Panel & Logout
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Licensing & Registry",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Identity Registered Mobile:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(text = creatorPhone, fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Zimbabwe License Status:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Box(
                        modifier = Modifier
                            .background(PremiumGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "VERIFIED PRO",
                            color = PremiumGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_logout"),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Exit Pro Portal Sessions",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SIMPLE TOAST SIMULATOR HELPER FOR RELIABLE FEEDBACK
// -------------------------------------------------------------
object ToastSim {
    fun show(context: android.content.Context, message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
    }
}
