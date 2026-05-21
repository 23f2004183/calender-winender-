package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Goal
import com.example.data.DailyGrade
import com.example.data.ScheduleTask
import com.example.repository.GoalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarViewModel(
    application: Application,
    private val repository: GoalRepository
) : AndroidViewModel(application) {

    // --- Calendar & Period State ---
    var selectedDate: String by mutableStateOf("") // "YYYY-MM-DD"
    var calendarMonthHeader: String by mutableStateOf("") // "May 2026"
    var calendarDaysGrid: List<CalendarDay> by mutableStateOf(emptyList())

    private var currentCalendar = Calendar.getInstance()

    // --- Selected Date Period Codes for Tiered Goals ---
    var selectedDailyPeriod: String by mutableStateOf("")
    var selectedWeeklyPeriod: String by mutableStateOf("")
    var selectedMonthlyPeriod: String by mutableStateOf("")
    var selectedQuarterlyPeriod: String by mutableStateOf("")
    var selectedYearlyPeriod: String by mutableStateOf("")

    // --- Reactive Data Lists (Flow Collected as State) ---
    val dailyGoals = MutableStateFlow<List<Goal>>(emptyList())
    val weeklyGoals = MutableStateFlow<List<Goal>>(emptyList())
    val monthlyGoals = MutableStateFlow<List<Goal>>(emptyList())
    val quarterlyGoals = MutableStateFlow<List<Goal>>(emptyList())
    val yearlyGoals = MutableStateFlow<List<Goal>>(emptyList())

    val scheduleTasks = MutableStateFlow<List<ScheduleTask>>(emptyList())
    val dailyGrade = MutableStateFlow<DailyGrade?>(null)

    val historicalGrades = repository.allDailyGrades.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allGoals = repository.allGoals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- UI Logic State ---
    var isSavingGoal by mutableStateOf(false)
    var isAILoading by mutableStateOf(false)
    var aiCoachResponse by mutableStateOf<String?>(null)
    var activeGoalTab: String by mutableStateOf("DAILY") // "DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY"

    // --- Calendar View Mode (Google Calendar Style Daily/Weekly/Monthly switching) ---
    var calendarViewMode by mutableStateOf("MONTH") // "DAILY", "WEEKLY", "MONTHLY"

    // --- Anime Vibe & Style States ---
    var selectedHeroClass by mutableStateOf("") // Lazily loaded from prefs in init
    var protagonistStamina by mutableStateOf(100) // Hero power pool (recharged by tasks)

    // --- Competitive Combat Score & Friends List ---
    var friendsChallengeActive by mutableStateOf(false)
    var activeChallengeName by mutableStateOf("")
    var selectedGuildId by mutableStateOf("") // Lazily loaded from prefs in init
    
    // Custom simulated battle live chat feed
    var communityChatFeed by mutableStateOf(listOf(
        "Goku_Coder ⚡: Standard morning time-block cleared! Just pushed to +40 XP!",
        "Mikasa_Focus 🗡️: Focused block completed. We must secure our daily boundary.",
        "Tanjiro_Breath 🌊: Total concentration deep work initiated! Sanjay, let's complete today's goals together!",
        "Sailor_Scout 🌙: Daily checklist looking super sparkly today!"
    ))

    // --- Gamification & Google Calendar Integration State ---
    private val prefs = application.getSharedPreferences("goal_calendar_prefs", android.content.Context.MODE_PRIVATE)

    var userXp by mutableStateOf(prefs.getInt("user_xp", 25))
    var userLevel by mutableStateOf(prefs.getInt("user_level", 1))
    var userStreak by mutableStateOf(prefs.getInt("user_streak", 3)) // default starter streak of 3
    var gcalConnected by mutableStateOf(prefs.getBoolean("gcal_connected", false))
    var showLevelUpNotification by mutableStateOf(false)
    var xpNotificationText by mutableStateOf("")

    // Checklist interactive items
    var checklistGcalConnect by mutableStateOf(prefs.getBoolean("chk_gcal_connect", false))
    var checklistGcalSync by mutableStateOf(prefs.getBoolean("chk_gcal_sync", false))
    var checklistGoalCreate by mutableStateOf(prefs.getBoolean("chk_goal_create", false))
    var checklistGoalComplete by mutableStateOf(prefs.getBoolean("chk_goal_complete", false))
    var checklistTaskCreate by mutableStateOf(prefs.getBoolean("chk_task_create", false))
    var checklistTaskComplete by mutableStateOf(prefs.getBoolean("chk_task_complete", false))
    var checklistGradeDay by mutableStateOf(prefs.getBoolean("chk_grade_day", false))
    var checklistAiOptimize by mutableStateOf(prefs.getBoolean("chk_ai_optimize", false))
    var checklistUnlockBadge by mutableStateOf(prefs.getBoolean("chk_unlock_badge", false))

    // --- TEMPORAL CHRONO APP BLOCK-IN STATE ---
    var currentTab by mutableStateOf("calendar") // "calendar", "social", "focus", "settings"
    
    // --- FEEDBACK AND RECOMMENDATIONS STATE ---
    var feedbackSubmissions by mutableStateOf<List<WorkspaceFeedback>>(listOf(
        WorkspaceFeedback(
            id = "feedback_init_1",
            senderName = "Sanjay Singh",
            senderEmail = "sanjaysingh4215@gmail.com",
            feedbackType = "UI/UX Suggestion",
            subject = "Dynamic Workloads on Monthly Grid",
            message = "We should add a small heatmap indicator highlighting days that contain high importance tasks to provide intuitive visual emphasis directly on the month overview.",
            timestamp = "2026-05-20 14:32"
        )
    ))

    fun submitSuggestion(
        name: String,
        email: String,
        type: String,
        subject: String,
        message: String,
        onIntentSend: (subject: String, body: String) -> Unit
    ) {
        if (subject.isBlank() || message.isBlank()) {
            triggerXpToast(0, "Error: Subject and description are required!")
            return
        }

        val timestamp = "Just now"
        val newFeedback = WorkspaceFeedback(
            id = "feedback_${System.currentTimeMillis()}",
            senderName = name.ifBlank { "Anonymous Collaborator" },
            senderEmail = email.ifBlank { "sanjaysingh4215@gmail.com" },
            feedbackType = type,
            subject = subject,
            message = message,
            timestamp = timestamp
        )

        feedbackSubmissions = listOf(newFeedback) + feedbackSubmissions
        addXp(30)
        triggerXpToast(30, "Feedback recorded on Winlender!")

        // Format body for the real email client transition
        val emailBody = """
            Dear Sanjay,
            
            A new suggestion was submitted via the Winlender applet settings feedback framework.
            
            -- Feedback Details --
            Sender Name: ${newFeedback.senderName}
            Sender Contact: ${newFeedback.senderEmail}
            Feedback Type: ${newFeedback.feedbackType}
            
            Subject: ${newFeedback.subject}
            Message: 
            ${newFeedback.message}
            
            Timestamp: ${newFeedback.timestamp}
            ------------------------------------
            Submitted via Android Winlender Applet
        """.trimIndent()

        onIntentSend(
            "Winlender Suggestion: [${newFeedback.feedbackType}] ${newFeedback.subject}",
            emailBody
        )
    }
    var isChronoLockActive by mutableStateOf(false)
    var selectedRestrictedApps by mutableStateOf(setOf("Instagram", "YouTube")) // default restricted apps
    val availableRestrictedApps = listOf("Instagram", "YouTube", "TikTok", "Twitter / X", "Reddit", "Facebook")
    
    // Attempt tracking & warning popup state
    var showAppBlockPopup by mutableStateOf(false)
    var blockedAppNameAttempted by mutableStateOf("")
    var blockAnimeReprimandQuote by mutableStateOf("")
    
    // Simulate distraction launching
    fun setAppRestricted(appName: String, isRestricted: Boolean) {
        val updated = selectedRestrictedApps.toMutableSet()
        if (isRestricted) {
            updated.add(appName)
        } else {
            updated.remove(appName)
        }
        selectedRestrictedApps = updated
    }
    
    fun attemptLaunchRestrictedApp(appName: String) {
        if (isChronoLockActive && selectedRestrictedApps.contains(appName)) {
            blockedAppNameAttempted = appName
            showAppBlockPopup = true
            
            // Random funny anime reprimands matching Sanjay's universe
            val animeQuotes = listOf(
                "Goku: Sanjay! You can't scroll $appName while training in high gravity! Real power demands absolute focus! 🛡️",
                "Mikasa: Put down $appName, Sanjay. The world outside needs your goals completed. Recommit right now! ⚔️",
                "Tanjiro: Sanjay, are you breathing with total concentration? $appName has been sealed by the Chrono Wards! 🌊",
                "Saitama: Checking $appName will only make you lose your focus strength. One punch doesn't require scrolling! 🥊",
                "Zoro: You're off course! $appName leads to distraction. Refocus your sword-arm, Sanjay! 🗡️",
                "L: I have calculated a 99% probability that opening $appName will derail Sanjay's S-Rank productivity streak today. 📓"
            )
            blockAnimeReprimandQuote = animeQuotes.random()
            
            // Stamina penalty
            if (protagonistStamina > 5) {
                protagonistStamina -= 5
                triggerXpToast(-5, "$appName block! Focus broke. (-5 Stamina)")
                addXp(-5) // Dynamic penalty
            } else {
                triggerXpToast(0, "$appName block! Chrono shields holding strong!")
            }
        } else {
            // Success simulated launching (either lock inactive or app not restricted)
            triggerXpToast(10, "Simulated launch: Opened $appName successfully since focus block is off!")
            addXp(10)
        }
    }
    
    fun dismissAppBlockPopup() {
        showAppBlockPopup = false
    }
    
    // --- SOCIAL GUILD FRIENDS, APPROVALS & LEADERBOARD --
    var pendingFriendRequests by mutableStateOf(listOf(
        FriendRequest(
            id = "req_1",
            name = "Zoro_Nav 🧭",
            avatarEmoji = "🗡️",
            description = "S-Rank Swordsman. Wants to lock goals with you.",
            sharedGoalTitle = "🗡️ Zoro's Core Balance: 30m Deep Work",
            sharedGoalDescription = "Approved partnership quest with Zoro to maintain daily focus coordinates.",
            startingCombatScore = 295
        ),
        FriendRequest(
            id = "req_2",
            name = "Saitama_Focus 🥊",
            avatarEmoji = "🥊",
            description = "Fists of absolute focus. Wants to share a high-intensity coding run.",
            sharedGoalTitle = "🥊 Saitama's Strength: Complete 4 Low-Rank Tasks",
            sharedGoalDescription = "Clear smaller milestones without breaking sweat, Saitama style.",
            startingCombatScore = 245
        ),
        FriendRequest(
            id = "req_3",
            name = "L_Logic 📓",
            avatarEmoji = "📓",
            description = "Task master analyst. Wants to analyze calendar block errors with you.",
            sharedGoalTitle = "📓 L's Case Study: Log 100% Calendar Reflection",
            sharedGoalDescription = "Ensure full self-assessment of the day with detailed notes.",
            startingCombatScore = 310
        )
    ))
    
    var activeFriendsList by mutableStateOf(listOf(
        FriendItem("Goku_Coder ⚡", 260, false, "⚡"),
        FriendItem("Mikasa_Focus 🗡️", 210, false, "🗡️"),
        FriendItem("Tanjiro_Breath 🌊", 185, false, "🌊"),
        FriendItem("Sailor_Scout 🌙", 120, false, "🌙")
    ))
    
    // To support chat, we track selected direct chat friend name
    var activeChatTab by mutableStateOf("PUBLIC") // "PUBLIC" or friend's name (e.g. "Goku_Coder ⚡")
    
    // Private chats map
    var privateChatsMap by mutableStateOf(mapOf(
        "Goku_Coder ⚡" to listOf(
            ChatMessage("Goku_Coder ⚡", "Sanjay! My work power is rising! Did you complete your morning goals yet?"),
            ChatMessage("Goku_Coder ⚡", "Let's push to over 9000 capacity today!")
        ),
        "Mikasa_Focus 🗡️" to listOf(
            ChatMessage("Mikasa_Focus 🗡️", "I am monitoring the schedule timeline closely. If something goes wrong, I'll protect your focus bounds."),
            ChatMessage("Mikasa_Focus 🗡️", "Only those who work hard survive the leaderboard battle.")
        ),
        "Tanjiro_Breath 🌊" to listOf(
            ChatMessage("Tanjiro_Breath 🌊", "Sanjay, take a deep breath. Use total concentration focus during study hours. The path is hard, but you will overcome!"),
            ChatMessage("Tanjiro_Breath 🌊", "Even if my back breaks, I will log my schedule!")
        ),
        "Sailor_Scout 🌙" to listOf(
            ChatMessage("Sailor_Scout 🌙", "Sanjay! Today is a beautiful day to clean up our backlog of tasks! ✨"),
            ChatMessage("Sailor_Scout 🌙", "Let's shine bright and score S-Rank badges!")
        )
    ))
    
    fun approveFriendRequest(request: FriendRequest) {
        // 1. Remove from pending
        pendingFriendRequests = pendingFriendRequests.filter { it.id != request.id }
        
        // 2. Add to active friends list
        val newFriend = FriendItem(request.name, request.startingCombatScore, false, request.avatarEmoji)
        activeFriendsList = activeFriendsList + newFriend
        
        // 3. Share announcement to Public Chat
        val updatedFeed = communityChatFeed.toMutableList()
        updatedFeed.add("ANNOUNCEMENT 🔔: Sanjay approved ${request.name}'s request! They have joined the Chrono S-Rank Leaderboard and shared a goal!")
        updatedFeed.add("${request.name}: Honored to join your party, Sanjay! Let's conquer the Rift together! 🌟")
        communityChatFeed = updatedFeed
        
        // 4. Initialize private chat with funny welcome message matching theme
        val initialMessage = when (request.name) {
            "Zoro_Nav 🧭" -> "I got lost again on my way to this screen, Sanjay! But my katana is ready. Let's conquer the task board!"
            "Saitama_Focus 🥊" -> "Hey. Approved. What's the plan? I did 100 lines of code already. Do you have any groceries or focus bounds?"
            "L_Logic 📓" -> "Sanjay, I will be analyzing your focus logs. My analysis predicts we will increase productivity by 34% as partners."
            else -> "Hello Sanjay! Let's lock our goals today!"
        }
        val updatedDMs = privateChatsMap.toMutableMap()
        updatedDMs[request.name] = listOf(
            ChatMessage(request.name, initialMessage)
        )
        privateChatsMap = updatedDMs
        
        // 5. Automatically create a joint buddy goal in active goals!
        saveGoal(
            title = request.sharedGoalTitle,
            description = request.sharedGoalDescription,
            type = "DAILY"
        )
        
        addXp(35)
        triggerXpToast(35, "Approved partnership quest with ${request.name}!")
        
        // Track badges checklist completion if applicable
        markChecklistCompleted("unlock_badge")
    }
    
    fun declineFriendRequest(requestId: String) {
        pendingFriendRequests = pendingFriendRequests.filter { requestId != it.id }
        triggerXpToast(5, "Request archived.")
    }
    
    fun sendDirectMessage(friendName: String, text: String) {
        if (text.isBlank()) return
        
        val currentDMs = privateChatsMap[friendName] ?: emptyList()
        val userMsg = ChatMessage("Sanjay (You) 👑", text)
        val withUser = currentDMs + userMsg
        
        val updatedDMs = privateChatsMap.toMutableMap()
        updatedDMs[friendName] = withUser
        privateChatsMap = updatedDMs
        
        // Trigger simulated reply from friend!
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200) // typing wait simulation
            val replyText = when (friendName) {
                "Goku_Coder ⚡" -> {
                    val responses = listOf(
                        "INCREDIBLE! Sanjay, your chatting stamina is pure energy! I'm going back to gravity coding!",
                        "YES! Keep training that productivity power level! Don't let your stamina dip!",
                        "Want to spark a joint gym challenge next? Let's clear more high-importance quests!"
                    )
                    responses.random()
                }
                "Mikasa_Focus 🗡️" -> {
                    val responses = listOf(
                        "Acknowledged. I will guard the perimeter. Make sure you complete your checklist.",
                        "Are you focusing? Distractions can be lethal to goals.",
                        "I will keep track of the daily timeline for you. Do not falter."
                    )
                    responses.random()
                }
                "Tanjiro_Breath 🌊" -> {
                    val responses = listOf(
                        "Sanjay, your flame of determination is beautiful! Keep swinging your sword at those tasks!",
                        "I can smell the scent of completion! Let's finish today's quests hand-in-hand!",
                        "Total concentration! Let's take a deep breath and conquer the next time block!"
                    )
                    responses.random()
                }
                "Sailor_Scout 🌙" -> {
                    val responses = listOf(
                        "Sanjay! Sailor Moon style! I am sending you sparkly cosmic motivation right now! ✨🍃",
                        "Your current level is looking super celestial! Let's keep it up!",
                        "Yay! All tasks must face justice! Let's complete everything together!"
                    )
                    responses.random()
                }
                "Zoro_Nav 🧭" -> {
                    val responses = listOf(
                        "Sanjay, which way is the home button? Just kidding. Let's focus on our goals so we don't get lost.",
                        "A swordsman never breaks focus. Let's finish the S-Rank target goals.",
                        "My Swords of Focus are always sharp when typing with you!"
                    )
                    responses.random()
                }
                "Saitama_Focus 🥊" -> {
                    val responses = listOf(
                        "Sanjay. No problem. Focus completed in one second. Time for afternoon sales at the market.",
                        "Is your focus level rising? Keep up the high intensity work. Don't go bald over it.",
                        "100 pushups done, 100 sit-ups! Every single day until S-Rank!"
                    )
                    responses.random()
                }
                "L_Logic 📓" -> {
                    val responses = listOf(
                        "Sanjay, my models predict your success rate is at 94.6% today. Let's maintain this momentum.",
                        "Interesting typing structure. It denotes high concentration capacity.",
                        "I am snacking on some target-focused cake right now. Sweets stimulate S-Class brain activity."
                    )
                    responses.random()
                }
                else -> "Awesome Sanjay! Together we are unstoppable! ⚔️"
            }
            
            val finalDMs = withUser + ChatMessage(friendName, replyText)
            val updatedFinal = privateChatsMap.toMutableMap()
            updatedFinal[friendName] = finalDMs
            privateChatsMap = updatedFinal
            
            // Gain small xp
            addXp(5)
            triggerXpToast(5, "Chatted with $friendName (+5 XP)")
        }
    }

    fun addXp(amount: Int) {
        val nextLevelThreshold = userLevel * 100
        val totalXp = userXp + amount
        if (totalXp >= nextLevelThreshold) {
            userLevel += 1
            userXp = totalXp - nextLevelThreshold
            showLevelUpNotification = true
            prefs.edit().putInt("user_level", userLevel).apply()
        } else {
            userXp = totalXp
        }
        prefs.edit().putInt("user_xp", userXp).apply()
        
        // Checking badge unlock checklist
        val currentTotalXpEarned = (1 until userLevel).sumOf { it * 100 } + userXp
        if (currentTotalXpEarned >= 200) {
            markChecklistCompleted("unlock_badge")
        }
    }

    fun triggerXpToast(amount: Int, reason: String) {
        xpNotificationText = "+$amount XP: $reason"
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            if (xpNotificationText.startsWith("+$amount XP")) {
                xpNotificationText = ""
            }
        }
    }

    fun dismissXpToast() {
        xpNotificationText = ""
    }

    fun dismissLevelUpNotification() {
        showLevelUpNotification = false
    }

    fun getProtocolLevelTitle(): String {
        return when (userLevel) {
            1 -> "E-Rank Recruit Soloist"
            2 -> "D-Rank Pathfinder"
            3 -> "C-Rank Iron Vanguard"
            4 -> "B-Rank Fate Binder"
            5 -> "A-Rank Star Weaver"
            else -> "S-Rank Supreme Protagonist ✨"
        }
    }

    fun selectHeroClass(heroClass: String) {
        selectedHeroClass = heroClass
        prefs.edit().putString("anime_hero_class", heroClass).apply()
        triggerXpToast(15, "Hero Class changed to $heroClass!")
        addXp(15)
    }

    fun joinGuild(guildId: String) {
        selectedGuildId = guildId
        prefs.edit().putString("selected_guild_id", guildId).apply()
        
        val guildGreeting = when (guildId) {
            "DEEP_WORK_LEGION" -> "Welcomed to Deep Work Magic Portal!"
            "ANIME_GYM_PROTAGONIST" -> "Joined the Iron Protagonist Gym!"
            "LOFI_CHILL_SCRIBES" -> "Entered Lofi Cozy Scribbles library!"
            else -> "Guild joined!"
        }
        
        communityChatFeed = when (guildId) {
            "DEEP_WORK_LEGION" -> listOf(
                "Tanjiro_Breath 🌊: Guild Quest 'Absolute Concentration' is active! Sanjay joined us!",
                "Goku_Coder ⚡: Welcome! Grab your coffee, let's break some milestones!",
                "Mikasa_Focus 🗡️: Safeguarding the perimeter so Sanjay can study without distractions."
            )
            "ANIME_GYM_PROTAGONIST" -> listOf(
                "Goku_Coder ⚡: TIME TO OVERLIMIT PRESS! Who is ready to finish 3 high-importance goals?",
                "Rock_Lee 🔥: Youth and sweat! 100 timed-blocks or 100 pushups, let's GO!",
                "Saitama 🥊: Coded for 5 minutes, now my head is shiny."
            )
            "LOFI_CHILL_SCRIBES" -> listOf(
                "Lofi_Panda 🐼: Grab a tea, turn on the beats. No rushes, only steady focus ticks.",
                "Usagi_Sailor 🌙: Just did my daily planner journaling. It's so cute!",
                "Kiki_Scribbles 🧹: Delivered 3 completed tasks to the town today. Productive wizardry!"
            )
            else -> communityChatFeed
        }
        
        triggerXpToast(25, guildGreeting)
        addXp(25)
    }

    fun postChatMessage(msgText: String) {
        if (msgText.isBlank()) return
        val updatedFeed = communityChatFeed.toMutableList()
        updatedFeed.add("Sanjay (You) 👑: $msgText")
        communityChatFeed = updatedFeed
        addXp(5)
        triggerXpToast(5, "Chat energy shared with guild!")
    }

    fun startFriendChallenge(challengeName: String) {
        activeChallengeName = challengeName
        friendsChallengeActive = true
        
        // Post shared goal to Goku, Mikasa, etc.
        val updatedFeed = communityChatFeed.toMutableList()
        updatedFeed.add("ANNOUNCEMENT 🔔: Sanjay initiated a daily battle target: '$challengeName'!")
        updatedFeed.add("Goku_Coder ⚡: YESSS! CHALLENGE ACCEPTED! I WILL DO DOUBLE XP!")
        updatedFeed.add("Mikasa_Focus 🗡️: Understood. I will complete this goal in half the time.")
        communityChatFeed = updatedFeed
        
        // Add goal to personal daily goals list as a joint quest
        saveGoal(
            title = "⚔️ Joint Battle: $challengeName",
            description = "Competitive multi-player challenge against your party members!",
            type = "DAILY"
        )
        
        triggerXpToast(30, "Day Battle Started!")
    }

    fun getDailyCombatScore(): Int {
        // Dynamic formula: Streaks, Tasks, Goals completed
        val tasksCompleted = scheduleTasks.value.count { it.isCompleted }
        val goalsCompleted = (dailyGoals.value.count { it.isCompleted } +
                weeklyGoals.value.count { it.isCompleted } +
                monthlyGoals.value.count { it.isCompleted })
        
        return userStreak * 12 + tasksCompleted * 20 + goalsCompleted * 35
    }

    fun completeAllDailyTasksAtOnce() {
        // Special Limit Break anime reward action!!
        viewModelScope.launch {
            scheduleTasks.value.forEach { task ->
                if (!task.isCompleted) {
                    toggleTaskCompletion(task)
                }
            }
            dailyGoals.value.forEach { goal ->
                if (!goal.isCompleted) {
                    toggleGoalCompletion(goal)
                }
            }
            addXp(40)
            triggerXpToast(40, "LIMIT BREAK: All Daily Quests Neutralized!")
            
            val updatedFeed = communityChatFeed.toMutableList()
            updatedFeed.add("Sanjay (You) 👑: 💥 LIMIT BREAK ACTIVATED!! My tasks have been conquered completely!")
            updatedFeed.add("Goku_Coder ⚡: WHOA! Insane power level! Sanjay went over 9,000!")
            communityChatFeed = updatedFeed
        }
    }

    fun markChecklistCompleted(key: String) {
        when (key) {
            "gcal_connect" -> {
                if (!checklistGcalConnect) {
                    checklistGcalConnect = true
                    prefs.edit().putBoolean("chk_gcal_connect", true).apply()
                    addXp(20)
                    triggerXpToast(20, "Checklist complete: Google Calendar Linked!")
                }
            }
            "gcal_sync" -> {
                if (!checklistGcalSync) {
                    checklistGcalSync = true
                    prefs.edit().putBoolean("chk_gcal_sync", true).apply()
                    addXp(30)
                    triggerXpToast(30, "Checklist complete: GCal Events Swarmed!")
                }
            }
            "gcal_export" -> {
                addXp(15)
                triggerXpToast(15, "Export pushed to cloud Google Calendar!")
            }
            "goal_create" -> {
                if (!checklistGoalCreate) {
                    checklistGoalCreate = true
                    prefs.edit().putBoolean("chk_goal_create", true).apply()
                    addXp(20)
                    triggerXpToast(20, "Checklist complete: Target Goal Created!")
                }
            }
            "goal_complete" -> {
                if (!checklistGoalComplete) {
                    checklistGoalComplete = true
                    prefs.edit().putBoolean("chk_goal_complete", true).apply()
                    addXp(30)
                    triggerXpToast(30, "Checklist complete: Closed Target Goal!")
                }
            }
            "task_create" -> {
                if (!checklistTaskCreate) {
                    checklistTaskCreate = true
                    prefs.edit().putBoolean("chk_task_create", true).apply()
                    addXp(20)
                    triggerXpToast(20, "Checklist complete: Added Time Block!")
                }
            }
            "task_complete" -> {
                if (!checklistTaskComplete) {
                    checklistTaskComplete = true
                    prefs.edit().putBoolean("chk_task_complete", true).apply()
                    addXp(30)
                    triggerXpToast(30, "Checklist complete: Cleared Time-Block!")
                }
            }
            "grade_day" -> {
                if (!checklistGradeDay) {
                    checklistGradeDay = true
                    prefs.edit().putBoolean("chk_grade_day", true).apply()
                    addXp(25)
                    triggerXpToast(25, "Checklist complete: Logged Day Rating!")
                }
            }
            "ai_optimize" -> {
                if (!checklistAiOptimize) {
                    checklistAiOptimize = true
                    prefs.edit().putBoolean("chk_ai_optimize", true).apply()
                    addXp(35)
                    triggerXpToast(35, "Checklist complete: Optimized Schedule with AI!")
                }
            }
            "unlock_badge" -> {
                if (!checklistUnlockBadge) {
                    checklistUnlockBadge = true
                    prefs.edit().putBoolean("chk_unlock_badge", true).apply()
                    addXp(40)
                    triggerXpToast(40, "Checklist complete: Earned 200+ XP Badges!")
                }
            }
        }
    }

    fun connectGoogleCalendar() {
        gcalConnected = true
        prefs.edit().putBoolean("gcal_connected", true).apply()
        addXp(50)
        triggerXpToast(50, "Google Account sanjaysingh4215@gmail.com paired!")
        markChecklistCompleted("gcal_connect")
    }

    fun disconnectGoogleCalendar() {
        gcalConnected = false
        prefs.edit().putBoolean("gcal_connected", false).apply()
    }

    fun syncGoogleCalendarEvents() {
        if (!gcalConnected) return
        viewModelScope.launch {
            isAILoading = true
            // Create Google sync blocks
            val mockEvents = listOf(
                ScheduleTask(
                    date = selectedDailyPeriod,
                    title = "📅 Google Sync: Team Core Planning",
                    startTime = "10:00",
                    endTime = "11:00",
                    importance = "HIGH"
                ),
                ScheduleTask(
                    date = selectedDailyPeriod,
                    title = "📅 Google Sync: Creative Assets Review",
                    startTime = "13:30",
                    endTime = "14:15",
                    importance = "MEDIUM"
                ),
                ScheduleTask(
                    date = selectedDailyPeriod,
                    title = "📅 Google Sync: 1:1 Sponsor Alignment",
                    startTime = "16:00",
                    endTime = "16:30",
                    importance = "LOW"
                )
            )
            mockEvents.forEach {
                repository.insertScheduleTask(it)
            }
            isAILoading = false
            addXp(35)
            triggerXpToast(35, "GCal events synced!)")
            markChecklistCompleted("gcal_sync")
        }
    }

    fun exportToGoogleCalendar() {
        if (!gcalConnected) return
        addXp(20)
        triggerXpToast(20, "Schedule export pushed to Google Calendar")
        markChecklistCompleted("gcal_export")
    }

    init {
        // Initialize with local time: 2026-05-21 as stated in metadata
        val initialCal = Calendar.getInstance()
        // Override with the current system time from metadata if possible
        initialCal.set(2026, Calendar.MAY, 21)
        currentCalendar = initialCal
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        selectedDate = sdf.format(initialCal.time)
        
        selectedHeroClass = prefs.getString("anime_hero_class", "Solo Leveler") ?: "Solo Leveler"
        selectedGuildId = prefs.getString("selected_guild_id", "DEEP_WORK_LEGION") ?: "DEEP_WORK_LEGION"
        
        updatePeriodCodes(initialCal.time)
        rebuildCalendarGrid()
        observeDataForSelectedPeriods()
    }

    private fun updatePeriodCodes(date: Date) {
        val sdfDaily = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        selectedDailyPeriod = sdfDaily.format(date)

        val cal = Calendar.getInstance().apply { time = date }
        val year = cal.get(Calendar.YEAR)
        
        // Weekly Code: "YYYY-Www"
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        selectedWeeklyPeriod = String.format(Locale.US, "%d-W%02d", year, week)

        // Monthly Code: "YYYY-MM"
        val month = cal.get(Calendar.MONTH) + 1
        selectedMonthlyPeriod = String.format(Locale.US, "%d-%02d", year, month)

        // Quarterly Code: "YYYY-Qq"
        val quarter = (month - 1) / 3 + 1
        selectedQuarterlyPeriod = String.format(Locale.US, "%d-Q%d", year, quarter)

        // Yearly Code: "YYYY"
        selectedYearlyPeriod = year.toString()
    }

    fun selectDate(dateStr: String) {
        selectedDate = dateStr
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            val date = sdf.parse(dateStr)
            if (date != null) {
                currentCalendar.time = date
                updatePeriodCodes(date)
                observeDataForSelectedPeriods()
                rebuildCalendarGrid()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun navigateMonth(offset: Int) {
        currentCalendar.add(Calendar.MONTH, offset)
        rebuildCalendarGrid()
    }

    fun rebuildCalendarGrid() {
        val yearSdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        calendarMonthHeader = yearSdf.format(currentCalendar.time)

        val grid = mutableListOf<CalendarDay>()
        
        // Make draft of the month
        val tempCal = currentCalendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 7 = Saturday
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Offset to match Monday as first day of week or Sunday. Let's use Monday (standard)
        // If firstDayOfWeek is Sunday(1), it translates to position index 6. 
        // Sunday (1), Monday(2), Tuesday(3), Wednesday(4), Thursday(5), Friday(6), Saturday(7)
        var emptyPrefix = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
        
        val prevMonthCal = tempCal.clone() as Calendar
        prevMonthCal.add(Calendar.MONTH, -1)
        val daysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Filled with previous month days as faded
        for (i in (daysInPrevMonth - emptyPrefix + 1)..daysInPrevMonth) {
            val dateStr = String.format(
                Locale.US, "%d-%02d-%02d",
                prevMonthCal.get(Calendar.YEAR),
                prevMonthCal.get(Calendar.MONTH) + 1,
                i
            )
            grid.add(CalendarDay(dayNumber = i, dateString = dateStr, isCurrentMonth = false))
        }

        // Current Month Days
        val daySdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        for (i in 1..daysInMonth) {
            tempCal.set(Calendar.DAY_OF_MONTH, i)
            val dateStr = daySdf.format(tempCal.time)
            grid.add(CalendarDay(dayNumber = i, dateString = dateStr, isCurrentMonth = true))
        }

        // Fill remaining empty slots to complete 42 items grid
        val remaining = 42 - grid.size
        if (remaining > 0) {
            val nextMonthCal = tempCal.clone() as Calendar
            nextMonthCal.add(Calendar.MONTH, 1)
            for (i in 1..remaining) {
                val dateStr = String.format(
                    Locale.US, "%d-%02d-%02d",
                    nextMonthCal.get(Calendar.YEAR),
                    nextMonthCal.get(Calendar.MONTH) + 1,
                    i
                )
                grid.add(CalendarDay(dayNumber = i, dateString = dateStr, isCurrentMonth = false))
            }
        }

        calendarDaysGrid = grid
    }

    private var selectedPeriodsJob: kotlinx.coroutines.Job? = null

    private fun observeDataForSelectedPeriods() {
        selectedPeriodsJob?.cancel()
        selectedPeriodsJob = viewModelScope.launch {
            // Setup continuous flow gathering
            launch {
                repository.getGoalsByPeriod("DAILY", selectedDailyPeriod).collect { dailyGoals.value = it }
            }
            launch {
                repository.getGoalsByPeriod("WEEKLY", selectedWeeklyPeriod).collect { weeklyGoals.value = it }
            }
            launch {
                repository.getGoalsByPeriod("MONTHLY", selectedMonthlyPeriod).collect { monthlyGoals.value = it }
            }
            launch {
                repository.getGoalsByPeriod("QUARTERLY", selectedQuarterlyPeriod).collect { quarterlyGoals.value = it }
            }
            launch {
                repository.getGoalsByPeriod("YEARLY", selectedYearlyPeriod).collect { yearlyGoals.value = it }
            }
            launch {
                repository.getScheduleTasks(selectedDailyPeriod).collect { scheduleTasks.value = it }
            }
            launch {
                repository.getDailyGrade(selectedDailyPeriod).collect { dailyGrade.value = it }
            }
        }
    }

    // --- Action Handlers ---

    fun saveGoal(title: String, description: String, type: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            isSavingGoal = true
            val period = when (type) {
                "DAILY" -> selectedDailyPeriod
                "WEEKLY" -> selectedWeeklyPeriod
                "MONTHLY" -> selectedMonthlyPeriod
                "QUARTERLY" -> selectedQuarterlyPeriod
                "YEARLY" -> selectedYearlyPeriod
                else -> selectedDailyPeriod
            }
            val newGoal = Goal(
                type = type,
                targetDateOrPeriod = period,
                title = title,
                description = description
            )
            repository.insertGoal(newGoal)
            isSavingGoal = false
            markChecklistCompleted("goal_create")
        }
    }

    fun toggleGoalCompletion(goal: Goal) {
        viewModelScope.launch {
            val nextState = !goal.isCompleted
            repository.updateGoal(goal.copy(isCompleted = nextState))
            if (nextState) {
                val reward = when (goal.type) {
                    "DAILY" -> 15
                    "WEEKLY" -> 35
                    "MONTHLY" -> 60
                    "QUARTERLY" -> 100
                    "YEARLY" -> 150
                    else -> 15
                }
                addXp(reward)
                triggerXpToast(reward, "Target Goal closed!")
                markChecklistCompleted("goal_complete")
            }
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal.id)
        }
    }

    fun saveScheduleTask(title: String, startTime: String, endTime: String, importance: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = ScheduleTask(
                date = selectedDailyPeriod,
                title = title,
                startTime = startTime,
                endTime = endTime,
                importance = importance
            )
            repository.insertScheduleTask(task)
            markChecklistCompleted("task_create")
        }
    }

    fun toggleTaskCompletion(task: ScheduleTask) {
        viewModelScope.launch {
            val nextState = !task.isCompleted
            repository.updateScheduleTask(task.copy(isCompleted = nextState))
            if (nextState) {
                val reward = when (task.importance) {
                    "HIGH" -> 20
                    "MEDIUM" -> 10
                    "LOW" -> 5
                    else -> 10
                }
                addXp(reward)
                triggerXpToast(reward, "Time-block cleared!")
                markChecklistCompleted("task_complete")
            }
        }
    }

    fun deleteTask(task: ScheduleTask) {
        viewModelScope.launch {
            repository.deleteScheduleTask(task.id)
        }
    }

    fun gradeDay(grade: String, score: Int, reflection: String) {
        viewModelScope.launch {
            val gradeRecord = DailyGrade(
                date = selectedDailyPeriod,
                grade = grade,
                score = score,
                note = reflection
            )
            repository.saveDailyGrade(gradeRecord)
            addXp(30)
            triggerXpToast(30, "Checked today's progress & logged self-reflection!")
            markChecklistCompleted("grade_day")
        }
    }

    fun deleteGrade() {
        viewModelScope.launch {
            repository.deleteDailyGrade(selectedDailyPeriod)
        }
    }

    // --- AI Operations ---

    fun runScheduleOptimization() {
        viewModelScope.launch {
            isAILoading = true
            aiCoachResponse = null
            try {
                val optimizedText = repository.optimizeSchedule(selectedDailyPeriod)
                aiCoachResponse = optimizedText
                addXp(40)
                triggerXpToast(40, "AI schedule optimization executed.")
                markChecklistCompleted("ai_optimize")
            } catch (e: Exception) {
                aiCoachResponse = "Failed to run schedule optimization: ${e.localizedMessage}"
            } finally {
                isAILoading = false
            }
        }
    }

    fun runCourseCorrectionAndEnhancement(reflectionNote: String, grade: String, score: Int) {
        viewModelScope.launch {
            isAILoading = true
            aiCoachResponse = null
            try {
                val incomplete = dailyGoals.value.filter { !it.isCompleted } + 
                        weeklyGoals.value.filter { !it.isCompleted }
                
                val report = repository.requestCourseCorrection(
                    selectedDailyPeriod,
                    incomplete,
                    reflectionNote,
                    grade,
                    score
                )
                aiCoachResponse = report
            } catch (e: Exception) {
                aiCoachResponse = "Failed to run course correction advice: ${e.localizedMessage}"
            } finally {
                isAILoading = false
            }
        }
    }

    fun dismissAiResponse() {
        aiCoachResponse = null
    }

    // Nested Factory to safely instantiate room resources without direct Hilt DI
    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = AppDatabase.getDatabase(application)
                    val repository = GoalRepository(database.goalDao())
                    return CalendarViewModel(application, repository) as T
                }
            }
    }
}

data class CalendarDay(
    val dayNumber: Int,
    val dateString: String, // "YYYY-MM-DD"
    val isCurrentMonth: Boolean
)

data class FriendRequest(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val description: String,
    val sharedGoalTitle: String,
    val sharedGoalDescription: String,
    val startingCombatScore: Int
)

data class FriendItem(
    val name: String,
    val combatScore: Int,
    val isUser: Boolean,
    val avatarEmoji: String = "👤"
)

data class ChatMessage(
    val sender: String,
    val content: String,
    val timestamp: String = "Now"
)

data class WorkspaceFeedback(
    val id: String,
    val senderName: String,
    val senderEmail: String,
    val feedbackType: String, // "Feature Request", "UI/UX Suggestion", "Bug Report", "Productivity Tip"
    val subject: String,
    val message: String,
    val timestamp: String,
    val isSyncedCloud: Boolean = true
)
