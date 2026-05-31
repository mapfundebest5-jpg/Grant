package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.CreatorViewModel
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.FacebookBorder
import com.example.ui.theme.FacebookLightGray
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) { innerPadding ->
                    AppNavigationRoot(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigationRoot(
    modifier: Modifier = Modifier,
    viewModel: CreatorViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("facebook_pro_prefs", Context.MODE_PRIVATE) }
    
    // Check if the user is already logged in
    var isLoggedIn by remember { mutableStateOf(prefs.getBoolean("is_logged_in", false)) }
    
    // Save login phone number
    var verifiedPhone by remember { mutableStateOf(prefs.getString("verified_phone", "+263 771 000 000") ?: "") }

    if (!isLoggedIn) {
        FacebookProLoginScreen(
            modifier = modifier,
            onLoginSuccess = { phone ->
                prefs.edit().putBoolean("is_logged_in", true).putString("verified_phone", phone).apply()
                verifiedPhone = phone
                // Also update the ViewModel's phone state to unlock creator verified multipliers
                viewModel.updatePhone(phone)
                isLoggedIn = true
            }
        )
    } else {
        FacebookProMainDashboard(
            modifier = modifier,
            viewModel = viewModel,
            verifiedPhone = verifiedPhone
        )
    }
}

// ================= LOGIN SCREEN (STRICTLY ZIMBABWE ONLY) =================
@Composable
fun FacebookProLoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (String) -> Unit
) {
    var rawPhoneInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    
    var isAuthenticating by remember { mutableStateOf(false) }
    var currentProgressText by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    // Normalize and clean phone input
    val cleanPhone = remember(rawPhoneInput) {
        rawPhoneInput.filter { it.isDigit() }
    }

    // Determine correctness for Zimbabwe format (usually starts with 7, 1, or 07, 01, plus length)
    val isValidZimbabweNo = remember(cleanPhone) {
        // Must be between 9 and 10 digits
        cleanPhone.length in 9..10 && (cleanPhone.startsWith("7") || cleanPhone.startsWith("07") || cleanPhone.startsWith("1") || cleanPhone.startsWith("01"))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8EEF9),
                        FacebookLightGray
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .testTag("login_card")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Logo Branding
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "facebook",
                        color = FacebookBlue,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-1.5).sp,
                        modifier = Modifier.testTag("login_brand_logo")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(FacebookBlue)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PRO",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Text(
                    text = "Professional Creator Authenticator",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Locked Country Section (Zimbabwe Only)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Authorized Jurisdictional Gateway",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FacebookBlue
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FacebookBlue.copy(alpha = 0.06f))
                            .border(width = 1.2.dp, color = Color(0xFFD4AF37), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🇿🇼",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Zimbabwe (+263)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1E21)
                            )
                            Text(
                                text = "Beta Anchor Route Lockout",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFD4AF37).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LOCKED",
                                color = Color(0xFF9E7E0D),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Phone Input Field
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Creator Channel Phone Number",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )

                    OutlinedTextField(
                        value = rawPhoneInput,
                        onValueChange = {
                            if (it.length <= 11) {
                                rawPhoneInput = it
                                hasError = false
                            }
                        },
                        placeholder = { Text("771 234 567", color = Color.Gray) },
                        prefix = {
                            Text(
                                text = "+263 ",
                                color = FacebookBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FacebookBlue,
                            unfocusedBorderColor = FacebookBorder
                        )
                    )

                    if (rawPhoneInput.isNotEmpty()) {
                        if (isValidZimbabweNo) {
                            Text(
                                text = "✓ Compliant +263 Zimbabwe phone node",
                                color = Color(0xFF4CAF50),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        } else {
                            Text(
                                text = "Enter valid cell number (9 or 10 digits, e.g., 771 234 567)",
                                color = Color.Red,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                // Security Passcode Input Field
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Security Signature Passcode",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        placeholder = { Text("6-Digit Creator Code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = FacebookBlue)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("passcode_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FacebookBlue,
                            unfocusedBorderColor = FacebookBorder
                        )
                    )
                }

                // Error Indicator
                if (hasError) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // SECURE LOGIN BUTTON
                Button(
                    onClick = {
                        if (!isValidZimbabweNo) {
                            hasError = true
                            errorMessage = "Login restricted to Zimbabwe (+263) phone numbers only!"
                            return@Button
                        }
                        if (passwordInput.trim().length < 4) {
                            hasError = true
                            errorMessage = "Passcode must be at least 4 digits!"
                            return@Button
                        }

                        isAuthenticating = true
                        coroutineScope.launch {
                            // High Fidelity connection sequence
                            currentProgressText = "Resolving Zimbabwe fiber gateway..."
                            delay(600)
                            currentProgressText = "Verifying regional creator certificate..."
                            delay(500)
                            currentProgressText = "Establishing encrypted session node..."
                            delay(400)
                            
                            val fullFormattedPhone = "+263 " + cleanPhone.removePrefix("0")
                            onLoginSuccess(fullFormattedPhone)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_button"),
                    enabled = !isAuthenticating && isValidZimbabweNo && passwordInput.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Secure Professional Login",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                if (isAuthenticating) {
                    Text(
                        text = currentProgressText,
                        fontSize = 11.sp,
                        color = FacebookBlue,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ================= MAIN VIEW: DASHBOARD FLOW =================
@Composable
fun FacebookProMainDashboard(
    modifier: Modifier = Modifier,
    viewModel: CreatorViewModel,
    verifiedPhone: String
) {
    val pages by viewModel.pagesState.collectAsState()
    val posts by viewModel.postsState.collectAsState()
    val campaigns by viewModel.campaignsState.collectAsState()
    val selectedPageId by viewModel.selectedPageId.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val auditReport by viewModel.auditReport.collectAsState()
    val isVerifiedPro by viewModel.isVerifiedPro.collectAsState()

    val currentPage = pages.find { it.id == selectedPageId } ?: pages.firstOrNull()

    // Dialog layout triggers
    var showPostDialog by remember { mutableStateOf(false) }
    var showCampaignDialogForPost by remember { mutableStateOf<Post?>(null) }
    var showProfileEditDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FacebookLightGray)
    ) {
        // --- TOP NAV BAR ---
        HeaderBarPanel(
            onAuditingTriggered = {
                viewModel.selectTab(2) // Jump to AI Insights Tab
                viewModel.runPageDiagnostics()
                Toast.makeText(context, "Running real-time Gemini creator diagnostics...", Toast.LENGTH_SHORT).show()
            }
        )

        // --- ACTIVE CHANNEL PROFILE BLOCK ---
        currentPage?.let { page ->
            PageProfileCard(
                page = page,
                onEditClick = { showProfileEditDialog = true }
            )
        }

        // --- ACTIVE PAGE CORE TABS ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> FeedTabScreen(
                    posts = posts,
                    currentPage = currentPage,
                    onAddNewPostClick = { showPostDialog = true },
                    onPromotePostClick = { post -> showCampaignDialogForPost = post },
                    onDeletePostClick = { post -> viewModel.deletePost(post) },
                    onLikePostClick = { post -> viewModel.toggleLikePost(post) },
                    onPinPostClick = { post -> viewModel.togglePinPost(post.id) }
                )
                1 -> CampaignsTabScreen(
                    campaigns = campaigns,
                    posts = posts,
                    viewModel = viewModel,
                    onLaunchManualCampaign = {
                        if (posts.isEmpty()) {
                            Toast.makeText(context, "Publish a post before advertising!", Toast.LENGTH_LONG).show()
                        } else {
                            showCampaignDialogForPost = posts.first()
                        }
                    }
                )
                2 -> AiInsightsTabScreen(
                    page = currentPage,
                    auditReport = auditReport,
                    isAnalyzing = isAnalyzing,
                    onTriggerPageAudit = { viewModel.runPageDiagnostics() },
                    onPasteCaptionToComposer = { textToUse ->
                        showPostDialog = true
                        viewModel.selectTab(0)
                        Toast.makeText(context, "Draft captured into Composer!", Toast.LENGTH_SHORT).show()
                    }
                )
                3 -> ProfileSettingsTabScreen(
                    page = currentPage,
                    verifiedPhone = verifiedPhone,
                    isVerified = isVerifiedPro,
                    onEditClick = { showProfileEditDialog = true }
                )
            }
        }

        // --- SOLID MATERIAL 3 RESPONSIVE COMPOSABLE BOTTOM NAVIGATION BAR ---
        BottomTabNavigation(
            selectedTab = selectedTab,
            onTabSelected = { viewModel.selectTab(it) }
        )
    }

    // --- DIALOG: CREATE PRO POST ---
    if (showPostDialog) {
        var draftText by remember { mutableStateOf("") }
        var postStatus by remember { mutableStateOf("Published") }
        var postAudience by remember { mutableStateOf("Public") }
        var postCategory by remember { mutableStateOf("Engagement") }

        Dialog(onDismissRequest = { showPostDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("post_dialog_content")
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Compose Creator Post",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1C1E21)
                        )
                        IconButton(onClick = { showPostDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Composer")
                        }
                    }

                    OutlinedTextField(
                        value = draftText,
                        onValueChange = { draftText = it },
                        placeholder = { Text("What insights are you sharing from Zimbabwe today, Creator?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("post_caption_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FacebookBlue,
                            unfocusedBorderColor = FacebookBorder
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Audience Selec
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Audience scope", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FacebookLightGray)
                                    .clickable {
                                        postAudience = if (postAudience == "Public") "Followers" else "Public"
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(postAudience, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Status select
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Status category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FacebookLightGray)
                                    .clickable {
                                        postStatus = if (postStatus == "Published") "Scheduled" else "Published"
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(postStatus, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Category accent tags selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Category Accent Tag", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf("Engagement", "Lifestyle", "Promo").forEach { tag ->
                                val isSelected = postCategory == tag
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) FacebookBlue else FacebookLightGray)
                                        .clickable { postCategory = tag }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showPostDialog = false },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.createPost(
                                    caption = draftText,
                                    categoryTag = postCategory,
                                    audience = postAudience,
                                    status = postStatus
                                )
                                showPostDialog = false
                                Toast.makeText(context, "Post crafted successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            shape = RoundedCornerShape(10.dp),
                            enabled = draftText.isNotBlank(),
                            modifier = Modifier
                                .testTag("submit_post_button")
                                .minimumInteractiveComponentSize()
                        ) {
                            Text("Publish Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: SPONSOR CAMPAIGN CREATION ---
    showCampaignDialogForPost?.let { post ->
        var targetAudience by remember { mutableStateOf("Digital Professionals") }
        var currentBudget by remember { mutableFloatStateOf(150f) }
        var durationDays by remember { mutableIntStateOf(5) }

        val selectionAudiences = listOf("Broad Tech Connection", "Digital Professionals", "Harare Tech Incubator")

        Dialog(onDismissRequest = { showCampaignDialogForPost = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("campaign_dialog_content")
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Promote Post with Ads",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1C1E21)
                        )
                        IconButton(onClick = { showCampaignDialogForPost = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Campaign")
                        }
                    }

                    // Content review
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FacebookLightGray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = post.caption,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(10.dp),
                            color = Color.DarkGray
                        )
                    }

                    // Budget
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sponsor Budget", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "$${currentBudget.toInt()} USD",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = FacebookBlue
                            )
                        }
                        Slider(
                            value = currentBudget,
                            onValueChange = { currentBudget = it },
                            valueRange = 20f..1000f,
                            colors = SliderDefaults.colors(
                                thumbColor = FacebookBlue,
                                activeTrackColor = FacebookBlue
                            )
                        )
                    }

                    // Duration
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pacing duration", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "$durationDays Days",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = FacebookBlue
                            )
                        }
                        Slider(
                            value = durationDays.toFloat(),
                            onValueChange = { durationDays = it.toInt() },
                            valueRange = 1f..30f,
                            colors = SliderDefaults.colors(
                                thumbColor = FacebookBlue,
                                activeTrackColor = FacebookBlue
                            )
                        )
                    }

                    // Target Audience
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Target Demographic Node", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        selectionAudiences.forEach { aud ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (targetAudience == aud) FacebookBlue.copy(alpha = 0.08f) else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (targetAudience == aud) FacebookBlue else FacebookBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { targetAudience = aud }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(aud, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                if (targetAudience == aud) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.startCampaign(
                                postId = post.id,
                                audienceType = targetAudience,
                                budget = currentBudget.toDouble(),
                                durationDays = durationDays
                            )
                            showCampaignDialogForPost = null
                            viewModel.selectTab(1) // Transition to campaigns
                            Toast.makeText(context, "Sponsor Campaign launched securely!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_campaign_button")
                    ) {
                        Text("Launch Sponsor Ad Campaign", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- DIALOG: EDIT CHANNEL PROFILE (ULTRA-PREMIUM PORTAL) ---
    if (showProfileEditDialog) {
        var profileName by remember { mutableStateOf(currentPage?.name ?: "") }
        var profileCategory by remember { mutableStateOf(currentPage?.category ?: "") }
        var profileBio by remember { mutableStateOf(currentPage?.bio ?: "") }
        
        // Editable about fields
        var profileEmail by remember { mutableStateOf(currentPage?.aboutEmail ?: "contact@techinsider.pro") }
        var profileWebsite by remember { mutableStateOf(currentPage?.aboutWebsite ?: "www.techinsider.pro") }
        var profileLocation by remember { mutableStateOf(currentPage?.aboutLocation ?: "Harare, Zimbabwe") }
        var profileWork by remember { mutableStateOf(currentPage?.aboutWork ?: "Innovator & Platform Strategist") }
        var profileJoined by remember { mutableStateOf(currentPage?.aboutJoinedDate ?: "Joined May 2024") }
        
        var selectedBannerPreset by remember { mutableStateOf(currentPage?.bannerUrl ?: "cosmic_neon") }
        
        var dialogSubTab by remember { mutableStateOf(0) } // 0: Identity, 1: Contact Details, 2: Banner Preset

        Dialog(onDismissRequest = { showProfileEditDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .testTag("premium_customizer_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Customize Creator Profile",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = FacebookBlue,
                        letterSpacing = (-0.5).sp
                    )

                    // Tab selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Identity", "About Me", "Banner").forEachIndexed { index, title ->
                            val active = dialogSubTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) FacebookBlue else FacebookLightGray)
                                    .clickable { dialogSubTab = index }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.White else Color.DarkGray
                                )
                            }
                        }
                    }

                    Divider(color = FacebookBorder.copy(alpha = 0.5f))

                    when (dialogSubTab) {
                        0 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = profileName,
                                    onValueChange = { profileName = it },
                                    label = { Text("Display Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = profileCategory,
                                    onValueChange = { profileCategory = it },
                                    label = { Text("Creator Specialty") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = profileBio,
                                    onValueChange = { profileBio = it },
                                    label = { Text("Bio Insight Statement") },
                                    modifier = Modifier.fillMaxWidth().height(90.dp).testTag("bio_input_field"),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = profileEmail,
                                    onValueChange = { profileEmail = it },
                                    label = { Text("Contact Email Address") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = profileWebsite,
                                    onValueChange = { profileWebsite = it },
                                    label = { Text("Channel Website URL") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = profileLocation,
                                    onValueChange = { profileLocation = it },
                                    label = { Text("Physical Location Endpoint") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = profileWork,
                                    onValueChange = { profileWork = it },
                                    label = { Text("Role / Specialty Title") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = profileJoined,
                                    onValueChange = { profileJoined = it },
                                    label = { Text("Registration Node Date") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                        2 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    "Select Glowing Banner Gradient",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )

                                val bannerPresets = listOf(
                                    "cosmic_neon" to "Cosmic Neon (Purple-Yellow)",
                                    "sunset_gold" to "Sunset Gold (Orange-Peach)",
                                    "forest_emerald" to "Forest Emerald (Spruce-Mint)",
                                    "ocean_breeze" to "Ocean Breeze (Cyan-Sapphire)",
                                    "tech_slate" to "Classic Tech (Charcoal-Steel)"
                                )

                                bannerPresets.forEach { (presetId, label) ->
                                    val isSelected = selectedBannerPreset == presetId
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) FacebookBlue.copy(alpha = 0.08f) else Color.Transparent)
                                            .border(
                                                width = 1.2.dp,
                                                color = if (isSelected) FacebookBlue else FacebookBorder,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedBannerPreset = presetId }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Mini representative gradient bar
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when (presetId) {
                                                            "cosmic_neon" -> Brush.sweepGradient(listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121)))
                                                            "sunset_gold" -> Brush.sweepGradient(listOf(Color(0xFFFF5F6D), Color(0xFFFFC371)))
                                                            "forest_emerald" -> Brush.sweepGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D)))
                                                            "ocean_breeze" -> Brush.sweepGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF)))
                                                            else -> Brush.sweepGradient(listOf(Color(0xFF243B55), Color(0xFF141E30)))
                                                        }
                                                    )
                                            )
                                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showProfileEditDialog = false },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text("Discard", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                // Save standard profile updates
                                viewModel.updateProfile(profileName, profileCategory, profileBio)
                                // Save custom banner preset
                                viewModel.updateBanner(selectedBannerPreset)
                                // Save customizable About fields
                                viewModel.updateAboutFields(profileEmail, profileWebsite, profileLocation, profileWork, profileJoined)
                                
                                showProfileEditDialog = false
                                Toast.makeText(context, "All customizations saved successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .testTag("save_bio_button")
                                .minimumInteractiveComponentSize()
                        ) {
                            Text("Apply Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ================= UI SUBCOMPONENTS & TABS =================

@Composable
fun HeaderBarPanel(
    onAuditingTriggered: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "facebook",
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = FacebookBlue,
                    letterSpacing = (-1.2).sp,
                    modifier = Modifier.testTag("app_logo_facebook")
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(FacebookBlue)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PRO",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onAuditingTriggered,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FacebookLightGray)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "AI Audit",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = onAuditingTriggered,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FacebookLightGray)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alerts",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "3",
                            fontSize = 8.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BannerPresetBackground(presetId: String, modifier: Modifier = Modifier) {
    val brush = remember(presetId) {
        when (presetId) {
            "cosmic_neon" -> Brush.horizontalGradient(listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121)))
            "sunset_gold" -> Brush.horizontalGradient(listOf(Color(0xFFFF5F6D), Color(0xFFFFC371)))
            "forest_emerald" -> Brush.horizontalGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D)))
            "ocean_breeze" -> Brush.horizontalGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF)))
            "tech_slate" -> Brush.horizontalGradient(listOf(Color(0xFF243B55), Color(0xFF141E30)))
            else -> Brush.horizontalGradient(listOf(Color(0xFF1877F2), Color(0xFF003F91)))
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(brush)
    ) {
        // Overlay circular details to make preset designs premium and high-fidelity
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "ZIMBABWE PRO",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun PageProfileCard(
    page: Page,
    onEditClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(width = (0.5).dp, color = FacebookBorder)
    ) {
        Column {
            // Gradient banner box at top of card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clickable { onEditClick() }
            ) {
                BannerPresetBackground(presetId = page.bannerUrl, modifier = Modifier.fillMaxSize())
            }

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile avatar overlapping banner slightly
                    Box(
                        modifier = Modifier
                            .offset(y = (-28).dp)
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(FacebookBlue.copy(alpha = 0.12f))
                                .border(1.2.dp, FacebookBlue.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = page.name.split(" ").map { it.take(1) }.joinToString("").take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = FacebookBlue,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .offset(y = (-2).dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = page.name,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFF0F1115)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            // Glowing Premium Verification Badges
                            if (page.isMetaVerified) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(FacebookBlue)
                                        .padding(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "AI Meta Verified Crest",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Brush.horizontalGradient(listOf(Color(0xFF8A2387), Color(0xFFE94057))))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = page.metaVerifiedTier.uppercase(),
                                        color = Color.White,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Standard Pro Check",
                                    tint = FacebookBlue,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(FacebookBlue.copy(alpha = 0.15f))
                                )
                            }
                        }
                        Text(page.category, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(32.dp)
                            .offset(y = (-2).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = FacebookBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Description details
                Column(
                    modifier = Modifier.offset(y = (-14).dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = page.bio,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.testTag("profile_bio_label")
                    )

                    // Inline Location & URL fields for high polish customization
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(11.dp))
                            Text(page.aboutLocation, fontSize = 9.sp, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(11.dp))
                            Text(page.aboutWebsite, fontSize = 9.sp, color = FacebookBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 0: FEED WORKFLOW ---
@Composable
fun FeedTabScreen(
    posts: List<Post>,
    currentPage: Page?,
    onAddNewPostClick: () -> Unit,
    onPromotePostClick: (Post) -> Unit,
    onDeletePostClick: (Post) -> Unit,
    onLikePostClick: (Post) -> Unit,
    onPinPostClick: (Post) -> Unit
) {
    val pinnedPostId = currentPage?.pinnedPostId
    val sortedPosts = remember(posts, pinnedPostId) {
        posts.sortedWith(
            compareByDescending<Post> { it.id == pinnedPostId }
                .thenByDescending { it.timestamp }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("feed_tab_list"),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Analytics overview stats row
        currentPage?.let { page ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Creator Professional Insights",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Quality Rating: ", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    "${page.score}/100",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = FacebookBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatWidget(
                                label = "Total Followers",
                                value = formatCompactValue(page.followersCount),
                                growth = "+${formatCompactValue(page.netFollowersGrowth)}",
                                modifier = Modifier.weight(1f)
                            )
                            StatWidget(
                                label = "Audience Reach",
                                value = formatCompactValue(page.reachCount),
                                growth = "+12%",
                                modifier = Modifier.weight(1f)
                            )
                            StatWidget(
                                label = "Daily Clicks",
                                value = formatCompactValue(page.engagementCount),
                                growth = "High",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // What's on your mind? Box
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ZW", fontWeight = FontWeight.Black, color = FacebookBlue, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(FacebookLightGray)
                            .clickable { onAddNewPostClick() }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text("Share professional creator insights...", color = Color.Gray, fontSize = 12.sp)
                    }

                    IconButton(onClick = onAddNewPostClick, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = FacebookBlue)
                    }
                }
            }
        }

        // Post entries
        if (sortedPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No publications found.", color = Color.Gray, fontSize = 13.sp)
                        TextButton(onClick = onAddNewPostClick) {
                            Text("Share First Post", color = FacebookBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(sortedPosts, key = { it.id }) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(FacebookBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("ZW", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(currentPage?.name ?: "Tech Insider", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(FacebookBlue.copy(alpha = 0.08f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(post.categoryTag, fontSize = 8.sp, color = FacebookBlue, fontWeight = FontWeight.Bold)
                                        }

                                        // Pin Badge inside headers
                                        if (post.id == pinnedPostId) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFE2C044).copy(alpha = 0.15f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("📌 PINNED", fontSize = 8.sp, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    val dateStr = remember(post.timestamp) {
                                        val fmt = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                                        fmt.format(Date(post.timestamp))
                                    }
                                    Text("Zimbabwe Endpoint • $dateStr", fontSize = 9.sp, color = Color.Gray)
                                }
                            }

                            IconButton(onClick = { onDeletePostClick(post) }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Content caption
                        Text(
                            text = post.caption,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color.DarkGray
                        )

                        // Stats counters row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FacebookLightGray.copy(alpha = 0.5f))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Reach: ${formatCompactValue(post.reach)}", fontSize = 10.sp, color = Color.Gray)
                            Text("Likes: ${post.likes} 👍", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                        }

                        Divider(color = FacebookBorder.copy(alpha = 0.5f))

                        // Interaction Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = { onLikePostClick(post) },
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Like Post", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }

                            TextButton(
                                onClick = { onPromotePostClick(post) },
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Promote Ad", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                            }

                            val isThisPinned = post.id == pinnedPostId
                            TextButton(
                                onClick = { onPinPostClick(post) },
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                            ) {
                                Text(
                                    text = if (isThisPinned) "📌 Unpin" else "📌 Pin to Top",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isThisPinned) Color(0xFFD4AF37) else Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatWidget(
    label: String,
    value: String,
    growth: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FacebookLightGray),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(label, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(growth, fontSize = 9.sp, color = FacebookBlue, fontWeight = FontWeight.Bold)
        }
    }
}

// --- TAB 1: AD MANAGER & TRAFFIC SIMULATION ---
@Composable
fun CampaignsTabScreen(
    campaigns: List<Campaign>,
    posts: List<Post>,
    viewModel: CreatorViewModel,
    onLaunchManualCampaign: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isSimulating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Ad Traffic Simulation Hub", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "Simulate active traffic and conversions generated across Zimbabwe. Click 'Simulate Traffic Tick' below to securely stream budget spent, impressions, and results live to your stats!",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSimulating = true
                                // Simulate 4 clicks
                                repeat(5) {
                                    viewModel.simulatePerformanceTick()
                                    delay(400)
                                }
                                isSimulating = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        enabled = !isSimulating
                    ) {
                        Text(
                            text = if (isSimulating) "Streaming clicks..." else "Simulate Traffic Tick ⚡",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onLaunchManualCampaign,
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create Ad Campaign +", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Text("Active Sponsor Ad Campaigns", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(start = 2.dp))

        if (campaigns.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No active promoter campaigns running.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            campaigns.forEach { camp ->
                val linkedPost = posts.find { it.id == camp.postId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (camp.status == "Active") Color.Green else Color.Gray)
                                )
                                Text(
                                    camp.status,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (camp.status == "Active") FacebookBlue else Color.Gray
                                )
                            }

                            Text("Target: ${camp.audienceType}", fontSize = 11.sp, color = Color.Gray)
                        }

                        linkedPost?.let {
                            Text(
                                '"' + it.caption + '"',
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.Gray
                            )
                        }

                        // Budget Bar
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            val fraction = if (camp.budget > 0) (camp.spent / camp.budget).toFloat() else 0f
                            LinearProgressIndicator(
                                progress = { if (camp.status == "Completed") 1.0f else fraction.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = FacebookBlue,
                                trackColor = FacebookLightGray
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pacing spent: $${String.format("%.1f", camp.spent)}", fontSize = 9.sp, color = Color.Gray)
                                Text("Total: $${camp.budget.toInt()} USD", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Impressions", fontSize = 9.sp, color = Color.Gray)
                                Text(formatCompactValue(camp.impressions), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Ad Link Clicks", fontSize = 9.sp, color = Color.Gray)
                                Text(formatCompactValue(camp.clicks), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                            }
                            Column {
                                Text("Pacing duration", fontSize = 9.sp, color = Color.Gray)
                                Text("${camp.durationDays} Days", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 2: GEMINI INSIGHTS & AUDIT ---
@Composable
fun AiInsightsTabScreen(
    page: Page?,
    auditReport: String?,
    isAnalyzing: Boolean,
    onTriggerPageAudit: () -> Unit,
    onPasteCaptionToComposer: (String) -> Unit
) {
    var rawDraftToOptimize by remember { mutableStateOf("") }
    var optimizedResult by remember { mutableStateOf("") }
    var isOptimizingCaption by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main strength score meter
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Gemini Creator Diagnostics Hub", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = page?.score?.toString() ?: "94.2",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = FacebookBlue
                            )
                            Text("Score", fontSize = 8.sp, color = Color.Gray)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Strength Status: EXCELLENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                        Text(
                            "Optimize your creator specialized portfolio and timing suggestions using custom Gemini generative AI audits designed specifically for Zimbabwe profiles.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        // Diagnostic triggers
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Diagnostics Report Suite", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Button(
                    onClick = { onTriggerPageAudit() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Trigger Creator Diagnostics Audit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (isAnalyzing) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Crunching database logs...", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                auditReport?.let { report ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(FacebookLightGray)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = report,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // Caption optimizer
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Zimbabwe Content Captions Optimizer", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Insert flat captions to optimize copy using targeted hashtags, trending hooks, and local formatting advice.",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = rawDraftToOptimize,
                    onValueChange = { rawDraftToOptimize = it },
                    placeholder = { Text("e.g., Check out our professional beta suite expansion launching soon.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isOptimizingCaption = true
                            optimizedResult = ""
                            delay(1200) // simulated generation
                            optimizedResult = """
                                🚀 REVOLUTIONIZING LOCAL WORKFLOWS 🇿🇼
                                
                                $rawDraftToOptimize
                                
                                💡 Creator Insight: Leveraging custom companion tools inside tech hotspots generates immediate 72% CTR gains. Pair content tags accurately!
                                
                                #HarareTech #ZimbabweCreators #ProductivityAccelerator
                            """.trimIndent()
                            isOptimizingCaption = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA020F0)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = rawDraftToOptimize.isNotBlank() && !isOptimizingCaption
                ) {
                    Text("Optimize Draft caption", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (isOptimizingCaption) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp).align(Alignment.CenterHorizontally))
                }

                if (optimizedResult.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(FacebookBlue.copy(alpha = 0.05f))
                            .border(1.dp, FacebookBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(optimizedResult, fontSize = 11.sp, color = Color.DarkGray)
                            Button(
                                onClick = { onPasteCaptionToComposer(optimizedResult) },
                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                modifier = Modifier.align(Alignment.End),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Load in Composer", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 3: PROFILE SETTINGS (NO LOGOUT PERMITTED) ---
@Composable
fun ProfileSettingsTabScreen(
    page: Page?,
    verifiedPhone: String,
    isVerified: Boolean,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Creator Verification Security", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF4CAF50))
                    }

                    Column {
                        Text("Verified Endpoint: $verifiedPhone", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isVerified) "✓ Approved Professional Anchor Node" else "Provision Pending",
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Account Lock Status", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = "Professional network certification has anchored this terminal permanently to your Zimbabwe (+263) device. In compliance with physical authentication security protocol, log out functions are locked and permanently disabled on this channel configuration.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FacebookLightGray)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                        Text(
                            "Logout Disabled by System Administrator",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Button(
            onClick = onEditClick,
            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Modify Channel profile", fontWeight = FontWeight.Bold)
        }
    }
}

// ================= THE BOTTOM TAB NAVIGATION =================

@Composable
fun BottomTabNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTabItem(
                label = "Feed Feed",
                icon = Icons.Default.List,
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            BottomTabItem(
                label = "Ad Sponsor",
                icon = Icons.Default.PlayArrow,
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
            BottomTabItem(
                label = "AI Insights",
                icon = Icons.Default.Star,
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
            BottomTabItem(
                label = "Security",
                icon = Icons.Default.Lock,
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) }
            )
        }
    }
}

@Composable
fun BottomTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) FacebookBlue else Color.Gray,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) FacebookBlue else Color.Gray
        )
    }
}

// Helper formatting function for metric integers representation
fun formatCompactValue(value: Int): String {
    return when {
        value >= 1_000_000 -> "${String.format("%.1f", value / 1_000_000f)}M"
        value >= 1_000 -> "${String.format("%.1f", value / 1_000f)}K"
        else -> value.toString()
    }
}

fun formatCompactValue(value: Double): String {
    return formatCompactValue(value.toInt())
}

fun formatMetric(value: Int): String {
    return formatCompactValue(value)
}

fun formatMetric(value: Double): String {
    return formatCompactValue(value.toInt())
}
