package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Goal
import com.example.data.DailyGrade
import com.example.data.ScheduleTask
import com.example.ui.theme.*
import com.example.viewmodel.CalendarDay
import com.example.viewmodel.CalendarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCalendarApp(viewModel: CalendarViewModel) {
    val dailyGoalsList by viewModel.dailyGoals.collectAsStateWithLifecycle()
    val weeklyGoalsList by viewModel.weeklyGoals.collectAsStateWithLifecycle()
    val monthlyGoalsList by viewModel.monthlyGoals.collectAsStateWithLifecycle()
    val quarterlyGoalsList by viewModel.quarterlyGoals.collectAsStateWithLifecycle()
    val yearlyGoalsList by viewModel.yearlyGoals.collectAsStateWithLifecycle()

    val currentTasks by viewModel.scheduleTasks.collectAsStateWithLifecycle()
    val activeGrade by viewModel.dailyGrade.collectAsStateWithLifecycle()
    val historyLogs by viewModel.historicalGrades.collectAsStateWithLifecycle()
    val allStoredGoals by viewModel.allGoals.collectAsStateWithLifecycle()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showGraderDialog by remember { mutableStateOf(false) }
    var showInstructionOverview by remember { mutableStateOf(true) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var feedbackSenderName by remember { mutableStateOf("") }
    var feedbackSenderEmail by remember { mutableStateOf("sanjaysingh4215@gmail.com") }
    var feedbackTypeSelected by remember { mutableStateOf("Feature Request") }
    var feedbackSubject by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf("") }

    val activeGoals = when (viewModel.activeGoalTab) {
        "DAILY" -> dailyGoalsList
        "WEEKLY" -> weeklyGoalsList
        "MONTHLY" -> monthlyGoalsList
        "QUARTERLY" -> quarterlyGoalsList
        "YEARLY" -> yearlyGoalsList
        else -> dailyGoalsList
    }

    val (monthYearStr, dayNameNumStr) = remember(viewModel.selectedDate) {
        try {
            val sdfIn = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val date = sdfIn.parse(viewModel.selectedDate)
            if (date != null) {
                val sdfMonthYear = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.US)
                val sdfDayNameNum = java.text.SimpleDateFormat("EEEE d", java.util.Locale.US)
                Pair(sdfMonthYear.format(date).uppercase(), sdfDayNameNum.format(date))
            } else {
                Pair("ACTIVE PERIOD", viewModel.selectedDate)
            }
        } catch (e: Exception) {
            Pair("ACTIVE PERIOD", viewModel.selectedDate)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("W", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "Winlender",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "The calendar that helps you win in life",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { showInstructionOverview = !showInstructionOverview },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Toggle Help",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                    label = { Text("Calendar", fontSize = 10.sp) },
                    selected = viewModel.currentTab == "calendar",
                    onClick = { viewModel.currentTab = "calendar" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Share, contentDescription = "Partners") },
                    label = { Text("Partners", fontSize = 10.sp) },
                    selected = viewModel.currentTab == "social",
                    onClick = { viewModel.currentTab = "social" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Focus Wards") },
                    label = { Text("Shield", fontSize = 10.sp) },
                    selected = viewModel.currentTab == "focus",
                    onClick = { viewModel.currentTab = "focus" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 10.sp) },
                    selected = viewModel.currentTab == "settings",
                    onClick = { viewModel.currentTab = "settings" }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.testTag("root_scaffold")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Loading Overlay indicating AI Thinking
            if (viewModel.isAILoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = NeonPink,
                    trackColor = DeepViolet
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header overview details
                item {
                    AnimatedVisibility(
                        visible = showInstructionOverview,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "🏆 Winlender: Win in Life Dynamic Guide",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Welcome to your ultimate life-winning cockpit. Winlender seamlessly synchronizes your professional schedules with personal growth mechanics—enabling you to pair your Google Calendar records, grade daily completion metrics, shield high-priority slots from interruptions, and conquer challenges together with your accountability partners.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // --- 1. PROTAGONIST CHRONO DASHBOARD ---
                if (viewModel.currentTab == "focus") {
                    item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AnimeSurface),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, NeonPink, RoundedCornerShape(28.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(DeepViolet)
                                            .border(2.dp, GoldenLegend, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "👑",
                                            fontSize = 24.sp
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = viewModel.getProtocolLevelTitle(),
                                            color = GoldenLegend,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "Sanjay (Level ${viewModel.userLevel})",
                                            color = SolidWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DeepViolet)
                                        .border(1.dp, NeonPink, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("🔥", fontSize = 14.sp)
                                        Text(
                                            text = "${viewModel.userStreak}x Multiplier",
                                            color = NeonPink,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // Hero Class selector row
                            Text(
                                text = "SELECT YOUR PROTAGONIST CLASS:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftViolet,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val classes = listOf("Solo Leveler", "Hidden Leaf", "Alchemy Sage", "Lofi Wizard")
                                classes.forEach { className ->
                                    val isSelected = (viewModel.selectedHeroClass == className)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) NeonPink else DeepViolet)
                                            .border(1.dp, if (isSelected) GoldenLegend else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.selectHeroClass(className) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = className,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) SolidWhite else SoftViolet,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val progressFraction = viewModel.userXp.toFloat() / (viewModel.userLevel * 100)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mana: ${viewModel.userXp} / ${viewModel.userLevel * 100} XP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftViolet
                                )
                                Text(
                                    text = "${(progressFraction * 100).toInt()}% to Level ${viewModel.userLevel + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .background(DeepViolet)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = progressFraction.coerceIn(0f, 1f))
                                        .clip(CircleShape)
                                        .background(androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            listOf(NeonPink, ElectricCyan)
                                        ))
                                )
                            }

                            // LIMIT BREAK BUTTON
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.completeAllDailyTasksAtOnce() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color(0xFFEA580C)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, GoldenLegend, RoundedCornerShape(16.dp))
                            ) {
                                Text(
                                    text = "💥 LIMIT BREAK: ATTAIN INSTANT DAILY VICTORY!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SolidWhite
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "🏆 EARNED S-CLASS REWARDS:",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftViolet,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val hasNovice = (viewModel.userLevel > 1 || viewModel.userXp >= 25)
                                BadgeItem(
                                    emoji = "⚡",
                                    title = "S-Rank Target",
                                    description = "XP boundary crossed",
                                    isActive = hasNovice
                                )
                                val hasConsistent = viewModel.userStreak >= 3
                                BadgeItem(
                                    emoji = "🔥",
                                    title = "Hokage Dev",
                                    description = "Maintain focus fire",
                                    isActive = hasConsistent
                                )
                                val hasArchitect = viewModel.checklistUnlockBadge || viewModel.userLevel >= 2
                                BadgeItem(
                                    emoji = "👑",
                                    title = "S-Rank Slayer",
                                    description = "Conquer Level 2 threshold",
                                    isActive = hasArchitect
                                )
                            }
                        }
                    }
                }

                // --- 1.2 TEMPORAL CHRONO APP LOCK-IN SYSTEM ---
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AnimeSurface),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.2.dp, if (viewModel.isChronoLockActive) NeonPink else SoftViolet.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🛡️ CHRONO TEMPORAL LOCK-IN",
                                        color = NeonPink,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Study Hours Focus Shield",
                                        color = SolidWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Switch(
                                    checked = viewModel.isChronoLockActive,
                                    onCheckedChange = { viewModel.isChronoLockActive = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SolidWhite,
                                        checkedTrackColor = NeonPink,
                                        uncheckedThumbColor = SoftViolet,
                                        uncheckedTrackColor = DeepViolet
                                    ),
                                    modifier = Modifier.testTag("chrono_lock_switch")
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (viewModel.isChronoLockActive) NeonPink.copy(alpha = 0.15f) else DeepViolet)
                                    .border(1.dp, if (viewModel.isChronoLockActive) NeonPink else Color.Transparent, RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = if (viewModel.isChronoLockActive) "🟢" else "🔴",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (viewModel.isChronoLockActive) 
                                            "TEMPORAL WARDS ACTIVE: Restricting distracted channels to shield you! Launching restricted items will issue a defensive repulse." 
                                            else "STANDBY STATUS: Wards are offline. Wasted scrolls will not be intercepted automatically.",
                                        fontSize = 11.sp,
                                        color = if (viewModel.isChronoLockActive) SolidWhite else SoftViolet,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Text(
                                text = "RESTRICT TEMPORARY DISTRACTIONS:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftViolet,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val apps = listOf("Instagram", "YouTube", "TikTok", "Twitter / X")
                                apps.forEach { appName ->
                                    val isRestricted = viewModel.selectedRestrictedApps.contains(appName)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isRestricted) NeonPink else DeepViolet)
                                            .border(1.dp, if (isRestricted) ElectricCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setAppRestricted(appName, !isRestricted) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${if (isRestricted) "🔒 " else "🔓 "}$appName",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRestricted) SolidWhite else SoftViolet,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val apps2 = listOf("Reddit", "Facebook")
                                apps2.forEach { appName ->
                                    val isRestricted = viewModel.selectedRestrictedApps.contains(appName)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isRestricted) NeonPink else DeepViolet)
                                            .border(1.dp, if (isRestricted) ElectricCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setAppRestricted(appName, !isRestricted) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${if (isRestricted) "🔒 " else "🔓 "}$appName",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRestricted) SolidWhite else SoftViolet,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "🧪 TEMPORAL TEST CHAMBER (SIMULATE BACKGROUND TRIGGER):",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldenLegend,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sanjay, package tracking inside sandbox emulators requires device permissions, so utilize these direct simulator shortcuts to verify guards instantly!",
                                fontSize = 9.sp,
                                color = SoftViolet,
                                lineHeight = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val launchDemos = listOf("Instagram", "YouTube", "Twitter / X")
                                launchDemos.forEach { appTarget ->
                                    Button(
                                        onClick = { viewModel.attemptLaunchRestrictedApp(appTarget) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = DeepViolet,
                                            contentColor = SolidWhite
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Launch $appTarget",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                } // End focus tab

                // --- 2. MULTI-PLAYER ARENA & PARTY LEADERBOARD ---
                if (viewModel.currentTab == "social") {
                    item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AnimeSurface),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "⚔️ FELLOWSHIP LEADERS & GUILD CHAT",
                                color = ElectricCyan,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Compete with friends in real-time daily score battle arrays!",
                                fontSize = 11.sp,
                                color = SoftViolet
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DeepViolet)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Your Current Combat Power:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SolidWhite
                                )
                                Text(
                                    text = "${viewModel.getDailyCombatScore()} S-Score",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GoldenLegend
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // --- INCOMING PENDING FRIEND REQUESTS UPDATES ---
                            if (viewModel.pendingFriendRequests.isNotEmpty()) {
                                Text(
                                    text = "📥 PENDING INCOMING FRIEND REQUESTS (${viewModel.pendingFriendRequests.size}):",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldenLegend,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                viewModel.pendingFriendRequests.forEach { req ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = DeepViolet),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(NeonPink.copy(alpha = 0.15f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(req.avatarEmoji, fontSize = 14.sp)
                                                    }
                                                    Column {
                                                        Text(
                                                            text = req.name,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = SolidWhite
                                                        )
                                                        Text(
                                                            text = "Combat Score: ${req.startingCombatScore} PTS",
                                                            fontSize = 9.sp,
                                                            color = SoftViolet
                                                        )
                                                    }
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Button(
                                                        onClick = { viewModel.approveFriendRequest(req) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.height(28.dp).testTag("approve_${req.id}")
                                                    ) {
                                                        Text("Approve ✅", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SolidWhite)
                                                    }
                                                    Button(
                                                        onClick = { viewModel.declineFriendRequest(req.id) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = AnimeSurface),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.height(28.dp).border(1.dp, SoftViolet.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                    ) {
                                                        Text("Archive ❌", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SoftViolet)
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "🎯 Shared Mission Concept: ${req.sharedGoalTitle}",
                                                fontSize = 10.sp,
                                                color = ElectricCyan,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = req.description,
                                                fontSize = 9.sp,
                                                color = SoftViolet,
                                                lineHeight = 12.sp
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            // Leaderboard friends list
                            Text(
                                text = "ACTIVE PARTY LEADERBOARD:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftViolet
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            val activeFriendsAndMe = remember(viewModel.activeFriendsList, viewModel.userXp, viewModel.userLevel) {
                                val list = viewModel.activeFriendsList.map { Triple(it.name, it.combatScore, false) }.toMutableList()
                                list.add(Triple("Sanjay (You) 👑", viewModel.getDailyCombatScore(), true))
                                list.sortByDescending { it.second }
                                list
                            }

                            activeFriendsAndMe.forEachIndexed { index, (name, score, isUser) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "#${index + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (index == 0) GoldenLegend else SoftViolet
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isUser) ElectricCyan else SolidWhite
                                        )
                                    }
                                    Text(
                                        text = "$score PTS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (index == 0) GoldenLegend else SoftViolet
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "LAUNCH PARTY MUTUAL QUEST:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftViolet
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val templates = listOf("LeetCode Speedrun", "Protagonist Gym Prep", "Deep Writing Quest")
                                templates.forEach { tmpl ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DeepViolet)
                                            .border(1.dp, NeonPink.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .clickable { viewModel.startFriendChallenge(tmpl) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tmpl,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SolidWhite,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "SWITCH GUILD INTEREST CHANNEL:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftViolet
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val guilds = listOf(
                                    "DEEP_WORK_LEGION" to "🔮 Mages",
                                    "ANIME_GYM_PROTAGONIST" to "🏋️ Gym Slays",
                                    "LOFI_CHILL_SCRIBES" to "☕ Cozy Lofi"
                                )
                                guilds.forEach { (guildId, guildLabel) ->
                                    val isSelected = (viewModel.selectedGuildId == guildId)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) NeonPink else DeepViolet)
                                            .border(1.dp, if (isSelected) ElectricCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.joinGuild(guildId) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = guildLabel,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) SolidWhite else SoftViolet
                                        )
                                    }
                                }
                            }

                            // --- CHAT CHANNELS (PUBLIC CHAT vs DIRECT FOCUS DMs) ---
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "💬 CHRONO CHAT NETWORKING RIG:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftViolet,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val isPublicSelected = (viewModel.activeChatTab == "PUBLIC")
                                Box(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isPublicSelected) NeonPink else DeepViolet)
                                        .border(1.dp, if (isPublicSelected) ElectricCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.activeChatTab = "PUBLIC" }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🌐 PUBLIC FEED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPublicSelected) SolidWhite else SoftViolet
                                    )
                                }
                                
                                val approvedFriends = viewModel.activeFriendsList
                                approvedFriends.take(3).forEach { friend ->
                                    val isSelected = (viewModel.activeChatTab == friend.name)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) NeonPink else DeepViolet)
                                            .border(1.dp, if (isSelected) ElectricCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.activeChatTab = friend.name }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = friend.name.split(" ")[0].substringBefore("_"), // Goku, Mikasa, Zoro, Saitama
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) SolidWhite else SoftViolet,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            
                            if (viewModel.activeChatTab == "PUBLIC") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DeepViolet)
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    viewModel.communityChatFeed.takeLast(4).forEach { chatLine ->
                                        Text(
                                            text = chatLine,
                                            fontSize = 11.sp,
                                            color = SoftViolet,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            } else {
                                val activeDMTarget = viewModel.activeChatTab
                                val messages = viewModel.privateChatsMap[activeDMTarget] ?: emptyList()
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 80.dp, max = 150.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DeepViolet)
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (messages.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Transmitting with $activeDMTarget is silent. Send a message to invoke reaction!",
                                                fontSize = 10.sp,
                                                color = SoftViolet,
                                                style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                            )
                                        }
                                    } else {
                                        messages.takeLast(5).forEach { msg ->
                                            val isMe = msg.sender.startsWith("Sanjay")
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isMe) NeonPink.copy(alpha = 0.2f) else AnimeSurface)
                                                        .border(1.dp, if (isMe) NeonPink else ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = msg.sender,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isMe) ElectricCyan else GoldenLegend
                                                        )
                                                        Text(
                                                            text = msg.content,
                                                            fontSize = 11.sp,
                                                            color = SolidWhite,
                                                            lineHeight = 13.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Chat input
                            Spacer(modifier = Modifier.height(8.dp))
                            var userChatText by remember { mutableStateOf("") }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = userChatText,
                                    onValueChange = { userChatText = it },
                                    placeholder = { 
                                        Text(
                                            text = if (viewModel.activeChatTab == "PUBLIC") 
                                                "Post chat to guild companions..." 
                                                else "Send focus DM to ${viewModel.activeChatTab.split(" ")[0]}...", 
                                            fontSize = 11.sp, 
                                            color = SoftViolet.copy(alpha = 0.5f)
                                        ) 
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = DeepViolet,
                                        unfocusedContainerColor = DeepViolet,
                                        focusedTextColor = SolidWhite,
                                        unfocusedTextColor = SolidWhite,
                                        cursorColor = NeonPink
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        if (viewModel.activeChatTab == "PUBLIC") {
                                            viewModel.postChatMessage(userChatText)
                                        } else {
                                            viewModel.sendDirectMessage(viewModel.activeChatTab, userChatText)
                                        }
                                        userChatText = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.testTag("send_chat_msg")
                                ) {
                                    Text("Send", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SolidWhite)
                                }
                            }
                        }
                    }
                }
                } // End social tab

                // --- 3. GOOGLE CALENDAR MERGE PANEL ---
                if (viewModel.currentTab == "settings") {
                    item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AnimeSurface),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "☁️ CLOUD CHRONO INTEGRATION",
                                        color = ElectricCyan,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Google Calendar Link",
                                        color = SolidWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (viewModel.gcalConnected) DeepViolet else Color(0xFFC62828).copy(alpha = 0.2f))
                                        .border(1.dp, if (viewModel.gcalConnected) ElectricCyan else Color(0xFFC62828), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (viewModel.gcalConnected) "Live Linked" else "Not Paired",
                                        color = if (viewModel.gcalConnected) ElectricCyan else Color(0xFFEF9A9A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Link sanjaysingh4215@gmail.com dynamically to synchronize scheduled time-blocks, export completed targets, and coordinate multiplayer score plans.",
                                fontSize = 11.sp,
                                color = SoftViolet,
                                lineHeight = 15.sp
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            if (!viewModel.gcalConnected) {
                                Button(
                                    onClick = { viewModel.connectGoogleCalendar() },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink, contentColor = SolidWhite),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBox,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pair Google Calendar (sanjaysingh4215@gmail.com)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(DeepViolet)
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = ElectricCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column {
                                                Text("Active: sanjaysingh4215@gmail.com", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SolidWhite)
                                                Text("Bi-directional replication synced!", fontSize = 9.sp, color = SoftViolet)
                                            }
                                        }
                                        TextButton(
                                            onClick = { viewModel.disconnectGoogleCalendar() },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Disconnect", color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.syncGoogleCalendarEvents() },
                                            colors = ButtonDefaults.buttonColors(containerColor = DeepViolet, contentColor = ElectricCyan),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .weight(1.5f)
                                                .border(1.dp, ElectricCyan, RoundedCornerShape(16.dp))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Replica Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        
                                        Button(
                                            onClick = { viewModel.exportToGoogleCalendar() },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink, contentColor = SolidWhite),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.weight(1.5f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                             )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Export to GCal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                                .testTag("feedback_suggestions_card")
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("💡", fontSize = 18.sp)
                                    }
                                    Column {
                                        Text(
                                            text = "FEEDBACK & RECOMMENDATIONS",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "Recommend Product Changes",
                                            color = SolidWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = "Have ideas to make this a better productive and synchronized social learning platform? Recommend UI/UX or workflow updates below. This logs feedback local-side and prepares a real Email draft to Sanjay Singh (sanjaysingh4215@gmail.com).",
                                    fontSize = 11.sp,
                                    color = SoftViolet,
                                    lineHeight = 15.sp
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Category Selection Chips
                                Text(
                                    text = "SELECT CATEGORY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SageMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val categories = listOf("Feature Request", "UI/UX Suggestion", "Bug Report", "Social Mode")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    categories.forEach { cat ->
                                        val isSelected = feedbackTypeSelected == cat
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { feedbackTypeSelected = cat }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = cat,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) CharcoalDark else SolidWhite
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                // Sender Name
                                OutlinedTextField(
                                    value = feedbackSenderName,
                                    onValueChange = { feedbackSenderName = it },
                                    label = { Text("Your Name (Optional)", color = SoftViolet, fontSize = 11.sp) },
                                    placeholder = { Text("Anonymous Collaborator", color = SoftViolet.copy(alpha = 0.5f)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        focusedTextColor = SolidWhite,
                                        unfocusedTextColor = SolidWhite,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("feedback_name_input")
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Subject Input
                                OutlinedTextField(
                                    value = feedbackSubject,
                                    onValueChange = { feedbackSubject = it },
                                    label = { Text("Suggestion Subject", color = SoftViolet, fontSize = 11.sp) },
                                    placeholder = { Text("e.g. Dynamic workload heatmap indicators", color = SoftViolet.copy(alpha = 0.5f)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        focusedTextColor = SolidWhite,
                                        unfocusedTextColor = SolidWhite,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("feedback_subject_input")
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Recommendation detail
                                OutlinedTextField(
                                    value = feedbackMessage,
                                    onValueChange = { feedbackMessage = it },
                                    label = { Text("Describe Your Recommendation", color = SoftViolet, fontSize = 11.sp) },
                                    placeholder = { Text("What could we change to optimize focus and collaboration?", color = SoftViolet.copy(alpha = 0.5f)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        focusedTextColor = SolidWhite,
                                        unfocusedTextColor = SolidWhite,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth().testTag("feedback_message_input")
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = {
                                        viewModel.submitSuggestion(
                                            name = feedbackSenderName,
                                            email = "sanjaysingh4215@gmail.com",
                                            type = feedbackTypeSelected,
                                            subject = feedbackSubject,
                                            message = feedbackMessage,
                                            onIntentSend = { finalSubject, finalBody ->
                                                try {
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                                        data = android.net.Uri.parse("mailto:")
                                                        putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("sanjaysingh4215@gmail.com"))
                                                        putExtra(android.content.Intent.EXTRA_SUBJECT, finalSubject)
                                                        putExtra(android.content.Intent.EXTRA_TEXT, finalBody)
                                                    }
                                                    context.startActivity(android.content.Intent.createChooser(intent, "Send recommendations email..."))
                                                } catch (e: Exception) {
                                                    viewModel.triggerXpToast(0, "Synced locally to persistent ledger")
                                                }
                                            }
                                        )
                                        // Clear fields
                                        feedbackSubject = ""
                                        feedbackMessage = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = CharcoalDark
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("submit_feedback_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Submit Suggestion & Mail Sanjay", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    item {
                        // Synced Feedback Ledger History
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SoftViolet.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📋 SYNCHRONIZED FEEDBACK LEDGER",
                                        color = SageMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "${viewModel.feedbackSubmissions.size} Logged",
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                if (viewModel.feedbackSubmissions.isEmpty()) {
                                    Text(
                                        text = "No custom suggestions registered yet. Provide changes above to record recommendations.",
                                        color = SoftViolet,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        viewModel.feedbackSubmissions.forEach { feedback ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                    .border(1.dp, SoftViolet.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Column {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                                        ) {
                                                            Text(
                                                                text = feedback.feedbackType,
                                                                color = MaterialTheme.colorScheme.secondary,
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary)
                                                            )
                                                            Text(
                                                                text = "Synced cloud mail",
                                                                color = MaterialTheme.colorScheme.secondary,
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    
                                                    Text(
                                                        text = feedback.subject,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = SolidWhite
                                                    )
                                                    
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    
                                                    Text(
                                                        text = feedback.message,
                                                        fontSize = 11.sp,
                                                        color = SoftViolet,
                                                        lineHeight = 15.sp
                                                    )
                                                    
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "By: ${feedback.senderName} (${feedback.senderEmail})",
                                                            fontSize = 9.sp,
                                                            color = SageMuted,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = feedback.timestamp,
                                                            fontSize = 9.sp,
                                                            color = SageMuted
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                // --- 4. GUILD CHALLENGE TRACKING CHECKLIST ---
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AnimeSurface),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "🎯 TRACK ALL GUILD FEATURES",
                                        color = ElectricCyan,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "System Mastery Checklist",
                                        color = SolidWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                val completedCount = listOf(
                                    viewModel.checklistGcalConnect,
                                    viewModel.checklistGcalSync,
                                    viewModel.checklistGoalCreate,
                                    viewModel.checklistGcalSync, // double check safety
                                    viewModel.checklistGoalComplete,
                                    viewModel.checklistTaskCreate,
                                    viewModel.checklistTaskComplete,
                                    viewModel.checklistGradeDay,
                                    viewModel.checklistAiOptimize,
                                    viewModel.checklistUnlockBadge
                                ).count { it }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(DeepViolet)
                                        .border(1.dp, NeonPink, CircleShape)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$completedCount/9 Complete",
                                        color = NeonPink,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Experience the ultimate productivity pipeline. Perform actions to unlock tasks and earn bonus S-Rank XP rewards!",
                                fontSize = 11.sp,
                                color = SoftViolet,
                                lineHeight = 16.sp
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ChecklistItem(title = "Pair Google Calendar", description = "Connect cloud calendar account", isChecked = viewModel.checklistGcalConnect)
                                ChecklistItem(title = "Sync Calendar Blocks", description = "Import GCal tasks into day layout", isChecked = viewModel.checklistGcalSync)
                                ChecklistItem(title = "Formulate Tiered Target", description = "Create a custom Goal entry", isChecked = viewModel.checklistGoalCreate)
                                ChecklistItem(title = "Execute & Close Goal", description = "Check off any active goal", isChecked = viewModel.checklistGoalComplete)
                                ChecklistItem(title = "Schedule Calendar block", description = "Structure a custom timed block", isChecked = viewModel.checklistTaskCreate)
                                ChecklistItem(title = "Check Off Timed block", description = "Mark scheduled block as completed", isChecked = viewModel.checklistTaskComplete)
                                ChecklistItem(title = "Grade Your Performance", description = "Evaluate day with reflection", isChecked = viewModel.checklistGradeDay)
                                ChecklistItem(title = "AI Time Optimization", description = "Execute peak efficiency AI layout", isChecked = viewModel.checklistAiOptimize)
                                ChecklistItem(title = "Ascend To Badges Tier", description = "Accumulate 200 total XP blocks", isChecked = viewModel.checklistUnlockBadge)
                            }
                        }
                    }
                }

                // AI Response suggestion strip -> Styled as optimization engine in high vibe
                viewModel.aiCoachResponse?.let { aiResponse ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepViolet),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, ElectricCyan, RoundedCornerShape(28.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
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
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Optimization Icon",
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "S-RANK OPTIMIZATION ENGINE",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = ElectricCyan,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.dismissAiResponse() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close Advice",
                                            tint = SoftViolet.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                MarkdownSimpleRenderer(
                                    text = aiResponse,
                                    headerColor = ElectricCyan,
                                    textColor = SoftViolet,
                                    boldColor = SolidWhite
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { viewModel.dismissAiResponse() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NeonPink,
                                            contentColor = SolidWhite
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Apply S-Rank Adjustment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                } // End settings tab

                // High Level Progress statistics (dynamic summary stats)
                if (viewModel.currentTab == "calendar") {
                    item {
                    val activeGoalsProgress = remember(activeGoals) {
                        if (activeGoals.isEmpty()) 0f
                        else (activeGoals.count { it.isCompleted }.toFloat() / activeGoals.size)
                    }
                    val scheduleProgress = remember(currentTasks) {
                        if (currentTasks.isEmpty()) 0f
                        else (currentTasks.count { it.isCompleted }.toFloat() / currentTasks.size)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AnimeSurface),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${viewModel.activeGoalTab.lowercase().capitalize()} Targets",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = SoftViolet,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${(activeGoalsProgress * 100).toInt()}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp,
                                        color = SolidWhite
                                    )
                                    Text(
                                        text = "%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SoftViolet,
                                        modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(DeepViolet)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = activeGoalsProgress)
                                            .clip(CircleShape)
                                            .background(NeonPink)
                                    )
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = AnimeSurface),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Day Schedule",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = SoftViolet,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${(scheduleProgress * 100).toInt()}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp,
                                        color = SolidWhite
                                    )
                                    Text(
                                        text = "%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SoftViolet,
                                        modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(DeepViolet)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = scheduleProgress)
                                            .clip(CircleShape)
                                            .background(ElectricCyan)
                                    )
                                }
                            }
                        }
                    }
                }

                // --- CALENDAR VIEW CONTROLLER (GOOGLE CALENDAR DAILY/WEEKLY/MONTHLY MODES) ---
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AnimeSurface),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // View Switch Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DeepViolet)
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val views = listOf("DAILY" to "📅 Day Agenda", "WEEKLY" to "⚔️ 7-Day Week", "MONTHLY" to "🌌 Month Grid")
                                views.forEach { (viewKey, viewLabel) ->
                                    val isSelected = (viewModel.calendarViewMode == viewKey)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) NeonPink else Color.Transparent)
                                            .clickable { viewModel.calendarViewMode = viewKey }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = viewLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) SolidWhite else SoftViolet
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Conditional Render of Calendar View Mode
                            when (viewModel.calendarViewMode) {
                                "DAILY" -> {
                                    DailyAgendaTimeline(viewModel = viewModel)
                                }
                                "WEEKLY" -> {
                                    WeeklyCalendarPlanner(
                                        viewModel = viewModel,
                                        historyLogs = historyLogs,
                                        allStoredGoals = allStoredGoals
                                    )
                                }
                                "MONTHLY" -> {
                                    // Month Grid Navigator
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { viewModel.navigateMonth(-1) }) {
                                            Text("<", color = NeonPink, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                        }

                                        Text(
                                            text = viewModel.calendarMonthHeader,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = SolidWhite,
                                                letterSpacing = 0.5.sp
                                            )
                                        )

                                        IconButton(onClick = { viewModel.navigateMonth(1) }) {
                                            Text(">", color = NeonPink, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Day Headers (Mon - Sun)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
                                        weekdays.forEach { dayName ->
                                            Text(
                                                text = dayName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SoftViolet,
                                                modifier = Modifier.width(36.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Calendar grid blocks
                                    val chunks = viewModel.calendarDaysGrid.chunked(7)
                                    chunks.forEach { rowDays ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            rowDays.forEach { day ->
                                                val isSelected = (day.dateString == viewModel.selectedDate)
                                                
                                                val dailyGradeVal = historyLogs.find { it.date == day.dateString }
                                                val totalGoalsForDay = allStoredGoals.filter { it.targetDateOrPeriod == day.dateString }
                                                val completedGoalsCount = totalGoalsForDay.count { it.isCompleted }

                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .padding(2.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .clickable { viewModel.selectDate(day.dateString) }
                                                        .background(
                                                            if (isSelected) DeepViolet else Color.Transparent
                                                        )
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isSelected) NeonPink else if (day.isCurrentMonth) ElectricCyan.copy(alpha = 0.3f) else Color.Transparent,
                                                            shape = RoundedCornerShape(12.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = day.dayNumber.toString(),
                                                            fontSize = 13.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = when {
                                                                isSelected -> ElectricCyan
                                                                day.isCurrentMonth -> SolidWhite
                                                                else -> SoftViolet.copy(alpha = 0.4f)
                                                            }
                                                        )

                                                        if (dailyGradeVal != null) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(5.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) ElectricCyan else NeonPink)
                                                            )
                                                        } else if (completedGoalsCount > 0) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) ElectricCyan else SoftViolet)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // GOAL TIER SELECTOR PILL & LIST HEADER - Styled Geometric Balance
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎯 Goal Framework",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = CharcoalDark
                            )
 
                            IconButton(
                                onClick = { showAddGoalDialog = true },
                                modifier = Modifier.testTag("add_goal_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Goal Icon",
                                    tint = EmeraldPrimary
                                )
                            }
                        }
 
                        // Tab selectors for Daily, Weekly, Monthly, Quarterly, Yearly
                        val tabs = listOf("DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY")
                        ScrollableTabRow(
                            selectedTabIndex = tabs.indexOf(viewModel.activeGoalTab),
                            containerColor = Color.Transparent,
                            contentColor = EmeraldPrimary,
                            edgePadding = 0.dp,
                            divider = {},
                            indicator = {}
                        ) {
                            tabs.forEach { tab ->
                                val active = (viewModel.activeGoalTab == tab)
                                Tab(
                                    selected = active,
                                    onClick = { viewModel.activeGoalTab = tab },
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (active) PineGreen else GeoSurface
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (active) PineGreen else GeoBorder,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = tab,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (active) SolidWhite else SageMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
 
                // GOAL ITEMS LIST
                if (activeGoals.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GeoSurface),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GeoBorder, RoundedCornerShape(24.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Empty Goals Block",
                                    tint = SageMuted.copy(alpha = 0.3f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No ${viewModel.activeGoalTab.lowercase()} goals recorded.",
                                    color = SageMuted,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Press '+' above to add your first standard.",
                                    color = SageMuted.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    items(activeGoals, key = { it.id }) { goal ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (goal.isCompleted) GeoSurface.copy(alpha = 0.7f) else GeoSurface
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GeoBorder, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom Complete checkbox button
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (goal.isCompleted) StreakMintGreen else GeoBorder
                                            )
                                            .clickable { viewModel.toggleGoalCompletion(goal) }
                                            .testTag("completion_box_${goal.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (goal.isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Done",
                                                tint = SolidWhite,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
 
                                    Spacer(modifier = Modifier.width(10.dp))
 
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = goal.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (goal.isCompleted) SageMuted else CharcoalDark,
                                                modifier = Modifier.testTag("goal_title_${goal.id}")
                                            )
 
                                            // Highlight if AI Adjusted
                                            if (goal.aiEnhanced) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MintAccent.copy(alpha = 0.5f))
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "AI Enhanced",
                                                        fontSize = 9.sp,
                                                        color = PineGreen,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
 
                                        if (goal.description.isNotEmpty()) {
                                            Text(
                                                text = goal.description,
                                                fontSize = 12.sp,
                                                color = SageMuted,
                                                modifier = Modifier.padding(top = 2.dp),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
 
                                    IconButton(
                                        onClick = { viewModel.deleteGoal(goal) },
                                        modifier = Modifier.size(28.dp).testTag("delete_goal_${goal.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Goal Button",
                                            tint = SageMuted.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
 
                                // If has advice attached, render expanding text
                                goal.courseCorrectionAdvice?.let { advice ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MintAccent.copy(alpha = 0.2f))
                                            .padding(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.Top) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Advice Lightbulb",
                                                tint = EmeraldPrimary,
                                                modifier = Modifier.size(14.dp).padding(top = 2.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Adaptation Tip: $advice",
                                                fontSize = 11.sp,
                                                color = SageMuted,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 24-HOUR CALENDAR TIME BLOCKS (DAILY SCHEDULE)
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "📅 Schedule Time-Blocks",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = CharcoalDark
                                )
                                Text(
                                    text = "For date: ${viewModel.selectedDate}",
                                    fontSize = 11.sp,
                                    color = SageMuted
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { showAddTaskDialog = true },
                                    modifier = Modifier.testTag("add_task_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Task Indicator",
                                        tint = EmeraldPrimary
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.runScheduleOptimization() },
                                    modifier = Modifier.testTag("optimize_schedule_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Optimize Schedule",
                                        tint = EmeraldPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                if (currentTasks.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GeoSurface),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GeoBorder, RoundedCornerShape(24.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No time-blocked tasks added for today.",
                                    color = SageMuted,
                                    fontSize = 13.sp
                                )
                                TextButton(onClick = { showAddTaskDialog = true }) {
                                    Text("+ Structure Your Day", fontSize = 12.sp, color = EmeraldPrimary)
                                }
                            }
                        }
                    }
                } else {
                    items(currentTasks) { task ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isCompleted) GeoSurface.copy(alpha = 0.5f) else GeoSurface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GeoBorder, RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Composable Checkbox
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (task.isCompleted) StreakMintGreen else GeoBorder
                                            )
                                            .clickable { viewModel.toggleTaskCompletion(task) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (task.isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Task Done",
                                                tint = SolidWhite,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${task.startTime} - ${task.endTime}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PineGreen
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            
                                            // Priority tag label
                                            val badgeColor = when (task.importance) {
                                                "HIGH" -> Color(0xFFEA580C)
                                                "MEDIUM" -> Color(0xFFD97706)
                                                else -> SageMuted
                                            }
                                            val badgeBg = when (task.importance) {
                                                "HIGH" -> Color(0xFFFFF2EB)
                                                "MEDIUM" -> Color(0xFFFEF3C7)
                                                else -> GeoBackground
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(badgeBg)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = task.importance,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = badgeColor
                                                )
                                            }
                                        }
                                        Text(
                                            text = task.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (task.isCompleted) SageMuted else CharcoalDark,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deleteTask(task) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Task",
                                        tint = SageMuted.copy(alpha = 0.5f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // DAILY REFLECTION & GRADING BUFFER
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💯 Daily Grading & Optimization",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CharcoalDark
                    )
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GeoSurface),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GeoBorder, RoundedCornerShape(24.dp))
                            .padding(bottom = 24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (activeGrade != null) {
                                val currentGrading = activeGrade!!
                                val badgeColor = when (currentGrading.grade) {
                                    "A" -> StreakMintGreen
                                    "B" -> EmeraldPrimary
                                    "C" -> MediumPurple
                                    "D" -> AlertOrange
                                    else -> Color.Red
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(badgeColor.copy(alpha = 0.15f))
                                                .border(2.dp, badgeColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = currentGrading.grade,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = "Day Rating: ${currentGrading.score}/10",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CharcoalDark
                                            )
                                            Text(
                                                text = "Logged on ${currentGrading.date}",
                                                fontSize = 11.sp,
                                                color = SageMuted
                                            )
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteGrade() }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Grade Entry",
                                            tint = SageMuted
                                        )
                                    }
                                }

                                if (currentGrading.note.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MintAccent.copy(alpha = 0.2f))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "Reflection: \"${currentGrading.note}\"",
                                            fontSize = 12.sp,
                                            color = SageMuted,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                // Show AI course correction option if they have uncompleted daily tasks
                                val unfinishedGoals = dailyGoalsList.any { !it.isCompleted } || weeklyGoalsList.any { !it.isCompleted }
                                if (unfinishedGoals) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = {
                                            viewModel.runCourseCorrectionAndEnhancement(
                                                currentGrading.note,
                                                currentGrading.grade,
                                                currentGrading.score
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PineGreen),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("course_correct_button"),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Exclamation icon",
                                            tint = SolidWhite,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Course Correct Incomplete Goals (AI)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SolidWhite
                                        )
                                    }
                                }
                            } else {
                                // Empty state for grading / add reflection button
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Grade Today's Progress",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CharcoalDark
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Reflecting on your daily blocks and rating your performance helps provide telemetry logs for machine learning adaptation.",
                                        fontSize = 11.sp,
                                        color = SageMuted,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = { showGraderDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = PineGreen),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("add_grade_panel")
                                    ) {
                                        Text(
                                            text = "Record Grade & Reflection",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SolidWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                } // End calendar tab
            } // LazyColumn Ends
        }
    }

    // App Blocking Guard/Overlay Dialog
    if (viewModel.showAppBlockPopup) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.dismissAppBlockPopup() }
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AnimeSurface),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(2.5.dp, NeonPink, RoundedCornerShape(26.dp))
                    .testTag("app_block_popup")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(NeonPink.copy(alpha = 0.15f))
                            .border(2.dp, NeonPink, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛡️", fontSize = 32.sp)
                    }
                    
                    Text(
                        text = "CHRONO WARDS ACTIVE",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = NeonPink,
                        letterSpacing = 1.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Text(
                        text = "${viewModel.blockedAppNameAttempted} is temporarily chronologically blocked! Study Shield holds firm.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidWhite,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DeepViolet)
                            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "💬 CHRONO GUARDIAN REPRIMAND:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldenLegend,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.blockAnimeReprimandQuote,
                                fontSize = 11.sp,
                                color = SoftViolet,
                                lineHeight = 15.sp,
                                style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Focus Intercept Rate:", fontSize = 10.sp, color = SageMuted)
                        Text("100% S-Rank Secure", fontSize = 10.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Protagonist Penalty:", fontSize = 10.sp, color = SageMuted)
                        Text("-5 Stamina Points", fontSize = 10.sp, color = NeonPink, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(SoftViolet.copy(alpha = 0.15f)))
                    
                    Button(
                        onClick = { viewModel.dismissAppBlockPopup() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.5.dp, GoldenLegend, RoundedCornerShape(16.dp))
                            .testTag("dismiss_block_popup"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "⚔️ RECOMMIT TO FOCUS S-RANK",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = SolidWhite
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS CONTROLS ---

    // 1. ADD GOAL DIALOG
    if (showAddGoalDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newDesc by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(viewModel.activeGoalTab) }

        Dialog(onDismissRequest = { showAddGoalDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoSurface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, GeoBorder, RoundedCornerShape(28.dp))
                    .testTag("add_goal_dialog")
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "🎯 Create Target Goal",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = PineGreen
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Goal Objective", color = SageMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = GeoBorder,
                            focusedTextColor = CharcoalDark,
                            unfocusedTextColor = CharcoalDark,
                            focusedContainerColor = GeoBackground,
                            unfocusedContainerColor = GeoBackground
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_goal_title_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Description / Measures (Optional)", color = SageMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = GeoBorder,
                            focusedTextColor = CharcoalDark,
                            unfocusedTextColor = CharcoalDark,
                            focusedContainerColor = GeoBackground,
                            unfocusedContainerColor = GeoBackground
                        ),
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Goal Type Selection Dropdown / Selector Row
                    Text(text = "Goal Target Tier:", fontSize = 11.sp, color = SageMuted, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val types = listOf("DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY")
                        // Split into small chips
                        types.take(4).forEach { t ->
                            val activeChip = (selectedType == t)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (activeChip) PineGreen else GeoBackground)
                                    .border(1.dp, if (activeChip) PineGreen else GeoBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedType = t }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = t,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeChip) SolidWhite else CharcoalDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddGoalDialog = false }) {
                            Text("Cancel", color = SageMuted, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (newTitle.isNotEmpty()) {
                                    viewModel.saveGoal(newTitle, newDesc, selectedType)
                                    showAddGoalDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PineGreen,
                                contentColor = SolidWhite
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("save_goal_action")
                        ) {
                            Text("Create", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 2. ADD TASK DIALOG
    if (showAddTaskDialog) {
        var title by remember { mutableStateOf("") }
        var startHr by remember { mutableStateOf("09") }
        var startMn by remember { mutableStateOf("00") }
        var endHr by remember { mutableStateOf("10") }
        var endMn by remember { mutableStateOf("30") }
        var importance by remember { mutableStateOf("MEDIUM") }

        Dialog(onDismissRequest = { showAddTaskDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoSurface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, GeoBorder, RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "🕒 Set Calendar Time Block",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = PineGreen
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task / Activity Name", color = SageMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = GeoBorder,
                            focusedTextColor = CharcoalDark,
                            unfocusedTextColor = CharcoalDark,
                            focusedContainerColor = GeoBackground,
                            unfocusedContainerColor = GeoBackground
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_task_title_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Simplified time field selectors
                    Text(text = "Start Time (HH:MM):", fontSize = 11.sp, color = SageMuted, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = startHr,
                            onValueChange = { if (it.length <= 2) startHr = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = GeoBorder,
                                focusedTextColor = CharcoalDark,
                                unfocusedTextColor = CharcoalDark,
                                focusedContainerColor = GeoBackground,
                                unfocusedContainerColor = GeoBackground
                            ),
                            modifier = Modifier.width(64.dp),
                            singleLine = true
                        )
                        Text(" : ", color = CharcoalDark, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                        OutlinedTextField(
                            value = startMn,
                            onValueChange = { if (it.length <= 2) startMn = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = GeoBorder,
                                focusedTextColor = CharcoalDark,
                                unfocusedTextColor = CharcoalDark,
                                focusedContainerColor = GeoBackground,
                                unfocusedContainerColor = GeoBackground
                            ),
                            modifier = Modifier.width(64.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "End Time (HH:MM):", fontSize = 11.sp, color = SageMuted, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = endHr,
                            onValueChange = { if (it.length <= 2) endHr = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = GeoBorder,
                                focusedTextColor = CharcoalDark,
                                unfocusedTextColor = CharcoalDark,
                                focusedContainerColor = GeoBackground,
                                unfocusedContainerColor = GeoBackground
                            ),
                            modifier = Modifier.width(64.dp),
                            singleLine = true
                        )
                        Text(" : ", color = CharcoalDark, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                        OutlinedTextField(
                            value = endMn,
                            onValueChange = { if (it.length <= 2) endMn = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = GeoBorder,
                                focusedTextColor = CharcoalDark,
                                unfocusedTextColor = CharcoalDark,
                                focusedContainerColor = GeoBackground,
                                unfocusedContainerColor = GeoBackground
                            ),
                            modifier = Modifier.width(64.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Importance Level:", fontSize = 11.sp, color = SageMuted, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val levels = listOf("HIGH", "MEDIUM", "LOW")
                        levels.forEach { lv ->
                            val activeLv = (importance == lv)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (activeLv) PineGreen else GeoBackground)
                                    .border(1.dp, if (activeLv) PineGreen else GeoBorder, RoundedCornerShape(12.dp))
                                    .clickable { importance = lv }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = lv,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeLv) SolidWhite else CharcoalDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddTaskDialog = false }) {
                            Text("Cancel", color = SageMuted, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (title.isNotEmpty()) {
                                    val startStr = "${startHr.padStart(2, '0')}:${startMn.padStart(2, '0')}"
                                    val endStr = "${endHr.padStart(2, '0')}:${endMn.padStart(2, '0')}"
                                    viewModel.saveScheduleTask(title, startStr, endStr, importance)
                                    showAddTaskDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PineGreen,
                                contentColor = SolidWhite
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("save_task_action")
                        ) {
                            Text("Block", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 3. DAILY GRADER DIALOG
    if (showGraderDialog) {
        var selectedGrade by remember { mutableStateOf("A") }
        var scoreProgress by remember { mutableStateOf(8f) }
        var reflectionText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showGraderDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoSurface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, GeoBorder, RoundedCornerShape(28.dp))
                    .testTag("grader_dialog_root")
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "🏆 Performance Grade",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = PineGreen
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Assign Day Grade:", fontSize = 11.sp, color = SageMuted, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val grades = listOf("A", "B", "C", "D", "F")
                        grades.forEach { gr ->
                            val activeGr = (selectedGrade == gr)
                            val colorBnd = when (gr) {
                                "A" -> StreakMintGreen
                                "B" -> EmeraldPrimary
                                "C" -> MediumPurple
                                "D" -> AlertOrange
                                else -> Color.Red
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (activeGr) colorBnd else GeoBackground
                                    )
                                    .border(1.dp, if (activeGr) colorBnd else GeoBorder, CircleShape)
                                    .clickable { selectedGrade = gr },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = gr,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeGr) SolidWhite else CharcoalDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Productivity Score (1 - 10): ${scoreProgress.toInt()}",
                        fontSize = 11.sp,
                        color = SageMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = scoreProgress,
                        onValueChange = { scoreProgress = it },
                        valueRange = 1f..10f,
                        colors = SliderDefaults.colors(
                            thumbColor = PineGreen,
                            activeTrackColor = EmeraldPrimary,
                            inactiveTrackColor = GeoBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reflectionText,
                        onValueChange = { reflectionText = it },
                        label = { Text("Reflection / notes on blockers today", color = SageMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = GeoBorder,
                            focusedTextColor = CharcoalDark,
                            unfocusedTextColor = CharcoalDark,
                            focusedContainerColor = GeoBackground,
                            unfocusedContainerColor = GeoBackground
                        ),
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("reflection_note_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showGraderDialog = false }) {
                            Text("Cancel", color = SageMuted, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                viewModel.gradeDay(selectedGrade, scoreProgress.toInt(), reflectionText)
                                showGraderDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PineGreen,
                                contentColor = SolidWhite
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("save_grade_action")
                        ) {
                            Text("Grade Today", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- LEVEL UP POPUP CELEBRATION ---
    if (viewModel.showLevelUpNotification) {
        Dialog(onDismissRequest = { viewModel.dismissLevelUpNotification() }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoSurface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.5.dp, EmeraldPrimary, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "LEVEL UP ACCOMPLISHED!",
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You are now Level ${viewModel.userLevel}!",
                        color = CharcoalDark,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your architecture focus has ascended! Keep scheduling timed blocks and closing targets to maintain your streak metric multiplier.",
                        fontSize = 11.sp,
                        color = SageMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.dismissLevelUpNotification() },
                        colors = ButtonDefaults.buttonColors(containerColor = PineGreen, contentColor = SolidWhite),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Deconstruct More Goals!", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // --- FLOATING EXPERIENCE (XP) TOAST OVERLAY ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = viewModel.xpNotificationText.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PineGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MintAccent, RoundedCornerShape(16.dp))
                    .clickable { viewModel.dismissXpToast() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("✨", fontSize = 16.sp)
                        Text(
                            text = viewModel.xpNotificationText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolidWhite
                        )
                    }
                    IconButton(
                        onClick = { viewModel.dismissXpToast() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = SolidWhite.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Super clean basic parser that maps **Bold text**, list lines and headers of Gemini suggestions in structured UI blocks
 */
@Composable
fun MarkdownSimpleRenderer(
    text: String,
    headerColor: Color = EmeraldPrimary,
    textColor: Color = SageMuted,
    boldColor: Color = CharcoalDark
) {
    val lines = text.split("\n")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) return@forEach

            when {
                trimmed.startsWith("===") || trimmed.startsWith("###") || trimmed.startsWith("##") -> {
                    val cleanHeader = trimmed.replace("===", "").replace("###", "").replace("##", "").trim()
                    Text(
                        text = cleanHeader,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerColor,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("-") || trimmed.startsWith("*") -> {
                    val content = trimmed.substring(1).trim()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•", color = headerColor, fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
                        AnnotatedBoldText(
                            text = content,
                            style = MaterialTheme.typography.bodySmall.copy(color = textColor),
                            boldColor = boldColor,
                            normalColor = textColor
                        )
                    }
                }
                else -> {
                    AnnotatedBoldText(
                        text = trimmed,
                        style = MaterialTheme.typography.bodySmall.copy(color = textColor),
                        boldColor = boldColor,
                        normalColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun AnnotatedBoldText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    boldColor: Color = CharcoalDark,
    normalColor: Color = SageMuted
) {
    val annotatedString = remember(text, boldColor, normalColor) {
        androidx.compose.ui.text.buildAnnotatedString {
            val parts = text.split("**")
            parts.forEachIndexed { idx, part ->
                if (idx % 2 == 1) { // Odd indexes are between asterisks
                    withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) {
                        append(part)
                    }
                } else {
                    withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Normal, color = normalColor)) {
                        append(part)
                    }
                }
            }
        }
    }
    Text(text = annotatedString, style = style)
}

@Composable
fun RowScope.BadgeItem(
    emoji: String,
    title: String,
    description: String,
    isActive: Boolean
) {
    val bg = if (isActive) MintAccent.copy(alpha = 0.5f) else GeoBorder.copy(alpha = 0.5f)
    val textC = if (isActive) CharcoalDark else SageMuted.copy(alpha = 0.6f)
    val borderC = if (isActive) EmeraldPrimary.copy(alpha = 0.5f) else GeoBorder
    
    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .weight(1f)
            .border(1.dp, borderC, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 20.sp, modifier = Modifier.padding(bottom = 4.dp))
            Text(
                title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textC,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                description,
                fontSize = 8.sp,
                color = if (isActive) SageMuted else SageMuted.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                lineHeight = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ChecklistItem(
    title: String,
    description: String,
    isChecked: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isChecked) MintAccent.copy(alpha = 0.2f) else GeoBackground)
            .border(1.dp, if (isChecked) EmeraldPrimary.copy(alpha = 0.3f) else GeoBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isChecked) EmeraldPrimary else GeoBorder),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SolidWhite,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isChecked) SageMuted else CharcoalDark,
                style = if (isChecked) MaterialTheme.typography.bodySmall.copy(
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                ) else androidx.compose.ui.text.TextStyle.Default
            )
            Text(
                text = description,
                fontSize = 9.sp,
                color = SageMuted,
                lineHeight = 11.sp
            )
        }
    }
}

// ==========================================
// --- ANIME-RPG HELPER COMPOSABLES ---------
// ==========================================

@Composable
fun AnimeGuildLogo() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // High polish CSS style crest drawing in compose
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(NeonPink, ElectricCyan)
                ))
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AnimeBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚔️", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold,
                    color = GoldenLegend
                )
            }
        }
        
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CHRONO RIFT",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = SolidWhite,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonPink)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("GUILD", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SolidWhite)
                }
            }
            Text(
                text = "Dynamic Quest & Calendar Link",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = SoftViolet
            )
        }
    }
}

@Composable
fun DailyAgendaTimeline(viewModel: CalendarViewModel) {
    val tasks = viewModel.scheduleTasks.collectAsStateWithLifecycle().value
    val hours = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ CHRONO HOURS (DAY TIMELINE)",
                fontSize = 11.sp,
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DeepViolet)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${tasks.size} Blocked Slots",
                    fontSize = 9.sp,
                    color = NeonPink,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Loop standard day hours
        hours.forEach { hour ->
            // Find tasks falling around this hour!
            val matchingTasks = tasks.filter { task ->
                try {
                    val taskHour = task.startTime.substringBefore(":").toInt()
                    val agendaHour = hour.substringBefore(":").toInt()
                    taskHour >= agendaHour && taskHour < (agendaHour + 2)
                } catch (e: Exception) {
                    false
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Hour Label
                Text(
                    text = hour,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = SoftViolet,
                    modifier = Modifier.width(55.dp)
                )
                
                // Timeline Line Node
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (matchingTasks.isNotEmpty()) NeonPink else SoftViolet.copy(alpha = 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(38.dp)
                            .background(SoftViolet.copy(alpha = 0.15f))
                    )
                }
                
                Spacer(modifier = Modifier.width(6.dp))
                
                // Hour Content Details
                if (matchingTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(Color.Transparent),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "No quests scheduled. Idle Mana recovery.",
                            fontSize = 11.sp,
                            color = SageMuted,
                            style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        matchingTasks.forEach { task ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AnimeSurface),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (task.importance == "HIGH") NeonPink else ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = task.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SolidWhite
                                        )
                                        Text(
                                            text = "⏱️ ${task.startTime} - ${task.endTime} • Rank: ${task.importance}",
                                            fontSize = 10.sp,
                                            color = SoftViolet
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (task.isCompleted) NeonPink else SolidWhite.copy(alpha = 0.1f))
                                            .clickable { viewModel.toggleTaskCompletion(task) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (task.isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = SolidWhite,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyCalendarPlanner(viewModel: CalendarViewModel, historyLogs: List<DailyGrade>, allStoredGoals: List<Goal>) {
    val weekDays = remember(viewModel.selectedDate) {
        val days = mutableListOf<Triple<String, Int, String>>()
        val cal = java.util.Calendar.getInstance()
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            cal.time = sdf.parse(viewModel.selectedDate) ?: java.util.Date()
        } catch (e: Exception) {}
        
        // Find Monday of this week
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        val weekSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val nameSdf = java.text.SimpleDateFormat("EEE", java.util.Locale.US)
        
        for (i in 0..6) {
            val dayStr = weekSdf.format(cal.time)
            val dayNum = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val dayName = nameSdf.format(cal.time).uppercase()
            days.add(Triple(dayStr, dayNum, dayName))
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        days
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "⚔️ 7-DAY WEEKLY QUEST BOARD",
            fontSize = 11.sp,
            color = GoldenLegend,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Render 7 columns horizontally in a row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            weekDays.forEach { (dateStr, dayNum, dayName) ->
                val isSelected = (dateStr == viewModel.selectedDate)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) DeepViolet else AnimeSurface.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectDate(dateStr) }
                        .border(
                            width = 1.dp,
                            color = if (isSelected) NeonPink else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NeonPink else SoftViolet
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dayNum.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) ElectricCyan else SolidWhite
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Show detailed tasks list for each day of the week side by side or as scrollable summary rows!
        Text(
            text = "WEEK OUTLOOK TRACKER:",
            fontSize = 10.sp,
            color = SoftViolet,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        
        // We render a neat scrollable row of daily plans!
        weekDays.forEach { (dateStr, dayNum, dayName) ->
            val isSelectedDate = (dateStr == viewModel.selectedDate)
            
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isSelectedDate) DeepViolet.copy(alpha = 0.4f) else AnimeSurface.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable { viewModel.selectDate(dateStr) }
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSelectedDate) NeonPink else SoftViolet.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelectedDate) SolidWhite else SoftViolet
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$dayName - $dateStr",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelectedDate) SolidWhite else SoftViolet
                        )
                    }
                    
                    Text(
                        text = if (isSelectedDate) "👉 Selected Active Focus" else "Tap to plan",
                        fontSize = 10.sp,
                        color = if (isSelectedDate) ElectricCyan else SageMuted
                    )
                }
            }
        }
    }
}
