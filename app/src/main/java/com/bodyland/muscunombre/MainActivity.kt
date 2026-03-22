package com.bodyland.muscunombre

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bodyland.muscunombre.data.ActivityDefinition
import com.bodyland.muscunombre.data.GymSession
import com.bodyland.muscunombre.data.getActivityEmoji
import com.bodyland.muscunombre.TIERS
import com.bodyland.muscunombre.getTierForSessions
import com.bodyland.muscunombre.getProgressInTier
import com.bodyland.muscunombre.ui.theme.MuscuNombreTheme
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MuscuNombreTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GymRatApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymRatApp() {
    val context = LocalContext.current
    val viewModel: GymViewModel = viewModel(
        factory = GymViewModelFactory(context)
    )
    
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    
    if (!onboardingCompleted) {
        OnboardingScreen(viewModel)
    } else {
        MainAppContent(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(viewModel: GymViewModel) {
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    val endDate = startDate.plusDays(365)
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Bienvenue",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Choisis ta date de début d'abonnement pour démarrer le suivi sur 365 jours.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Période de suivi", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Début : " + startDate.format(dateFormatter))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Fin", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(endDate.format(dateFormatter), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Durée", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("365 jours", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { viewModel.completeOnboarding(startDate) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.small
        ) {
            Text("Commencer", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            currentDate = startDate,
            onDateSelected = { startDate = it; showStartDatePicker = false },
            onDismiss = { showStartDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MainAppContent(viewModel: GymViewModel) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Muscunombre",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }
        },
        bottomBar = {
            AppNavBar(
                currentPage = pagerState.currentPage,
                onPageSelected = { coroutineScope.launch { pagerState.animateScrollToPage(it) } }
            )
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            userScrollEnabled = true,
            beyondBoundsPageCount = 1
        ) { page ->
            when (page) {
                0 -> SessionTrackingTab(viewModel)
                1 -> CalendarTab(viewModel)
                2 -> UserTab(viewModel)
                3 -> SettingsTab(viewModel)
            }
        }
    }
}

@Composable
fun AppNavBar(currentPage: Int, onPageSelected: (Int) -> Unit) {
    val items = listOf(
        Triple(Icons.Filled.Home,      "Suivi",       0),
        Triple(Icons.Filled.DateRange, "Calendrier",  1),
        Triple(Icons.Filled.Person,    "Profil",      2),
        Triple(Icons.Filled.Settings,  "Réglages",    3),
    )
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (icon, label, page) ->
                val selected = currentPage == page
                val iconColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary
                                  else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200),
                    label = "navIconColor"
                )
                val labelColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary
                                  else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200),
                    label = "navLabelColor"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onPageSelected(page) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = labelColor,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionTrackingTab(viewModel: GymViewModel) {
    val activitiesDefs by viewModel.activities.collectAsState()
    val subscriptionPrice by viewModel.subscriptionPrice.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val sessionsInPeriod by viewModel.sessionsInPeriod.collectAsState()

    val scope = rememberCoroutineScope()
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH)
    fun LocalDate.toFr(): String = format(dateFormatter).split(" ").let {
        "${it[0]} ${it[1].replaceFirstChar { c -> c.titlecase() }} ${it[2]}"
    }
    val dayNameFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)

    var selectedActivities by remember { mutableStateOf(setOf<String>()) }
    val todayActivities = allSessions.filter { it.date == today }.map { it.activity }.toSet()
    LaunchedEffect(todayActivities) { selectedActivities = todayActivities }

    val daysRemaining = endDate?.let { java.time.temporal.ChronoUnit.DAYS.between(today, it).toInt().coerceAtLeast(0) } ?: 0
    val totalDays = if (startDate != null && endDate != null) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt() else 0
    val daysPassed = startDate?.let { java.time.temporal.ChronoUnit.DAYS.between(it, today).toInt().coerceAtLeast(0) } ?: 0
    // Counts scoped to the active period (for stats, cost, summary)
    val activityCounts = sessionsInPeriod.groupingBy { it.activity }.eachCount()
    val periodSessionCount = sessionsInPeriod.size
    val paidSessionCount = activitiesDefs.filter { it.price > 0 }.sumOf { activityCounts[it.name] ?: 0 }
    val globalPricePerSession = if (paidSessionCount > 0 && subscriptionPrice > 0) subscriptionPrice / paidSessionCount else 0.0
    // Tier scoped to the active period
    val currentTier = getTierForSessions(periodSessionCount)
    val progress = getProgressInTier(periodSessionCount, currentTier)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // En-tête : date + période
        Text(
            today.format(dayNameFormatter).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (startDate != null) {
            Text(
                "Jour $daysPassed · $daysRemaining ${if (daysRemaining > 1) "jours restants" else "jour restant"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Stats row
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCell(value = "$periodSessionCount", label = "séances")
                VerticalDivider(modifier = Modifier.height(36.dp))
                StatCell(
                    value = if (globalPricePerSession > 0) "%.2f€".format(globalPricePerSession) else "--",
                    label = "/ séance"
                )
                VerticalDivider(modifier = Modifier.height(36.dp))
                StatCell(
                    value = if (totalDays > 0) "${((daysPassed.toFloat() / totalDays) * 100).toInt()}%" else "--",
                    label = "écoulé"
                )
            }
        }

        // Pointer aujourd'hui
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "AUJOURD'HUI",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                activitiesDefs.forEachIndexed { index, actDef ->
                    val activity = actDef.name
                    val alreadyLogged = todayActivities.contains(activity)
                    val isSelected = selectedActivities.contains(activity)

                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedActivities = if (isSelected) selectedActivities - activity else selectedActivities + activity
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(actDef.emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(activity, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                if (alreadyLogged && isSelected) {
                                    Text("Enregistrée", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        }
                        // Toggle pill
                        val pillColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            animationSpec = tween(200), label = "pill"
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 26.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(pillColor),
                            contentAlignment = if (isSelected) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(3.dp)
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                val hasChanges = selectedActivities != todayActivities
                Button(
                    onClick = {
                        scope.launch {
                            val today = LocalDate.now()
                            selectedActivities.forEach { if (!todayActivities.contains(it)) viewModel.addSessionSuspend(today, it) }
                            todayActivities.forEach { if (!selectedActivities.contains(it)) viewModel.removeActivitySuspend(today, it) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = hasChanges,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Valider", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = 0.sp)
                }
            }
        }

        // Période
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("PÉRIODE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Début", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(startDate?.toFr() ?: "--", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Fin", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(endDate?.toFr() ?: "--", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Prix par activité (si configuré)
        val paidActivities = activitiesDefs.filter { it.price > 0 }
        if (paidActivities.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("COÛT PAR SÉANCE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    paidActivities.forEachIndexed { index, actDef ->
                        val count = activityCounts[actDef.name] ?: 0
                        val pricePerSession = if (count > 0) actDef.price / count else 0.0
                        if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(actDef.emoji + "  " + actDef.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("$count séances · ${actDef.price.toInt()}€/an", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                if (count > 0) "%.2f €".format(pricePerSession) else "-- €",
                                fontWeight = FontWeight.SemiBold,
                                color = if (count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Progression tier
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("NIVEAU", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Text(currentTier.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(currentTier.colorHex))
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(currentTier.colorHex),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (currentTier.tier < 7) {
                    val nextTier = TIERS[currentTier.tier]
                    Text(
                        "${currentTier.maxSessions - periodSessionCount + 1} séances avant ${nextTier.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text("Niveau maximum atteint", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }

        // Activités enregistrées
        if (activityCounts.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ACTIVITÉS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    activityCounts.entries.sortedByDescending { it.value }.forEachIndexed { index, (activity, count) ->
                        if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        val emoji = getActivityEmoji(activity, activitiesDefs)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("$emoji  $activity", style = MaterialTheme.typography.bodyMedium)
                            Text("$count", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Compte rendu sur la période
        if (startDate != null && endDate != null && activityCounts.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("COMPTE RENDU", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Entre le ${startDate?.toFr() ?: "--"} et le ${endDate?.toFr() ?: "--"}, tu as fait :",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    activityCounts.entries.sortedByDescending { it.value }.forEach { (activity, count) ->
                        val suffix = if (count > 1) "s" else ""
                        val emoji = getActivityEmoji(activity, activitiesDefs)
                        Text(
                            "· $count séance$suffix de $activity $emoji",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCell(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun UserTab(viewModel: GymViewModel) {
    val sessionsInPeriod by viewModel.sessionsInPeriod.collectAsState()
    val periodSessionCount = sessionsInPeriod.size
    val endDate by viewModel.endDate.collectAsState()
    val currentTier = getTierForSessions(periodSessionCount)
    val progress = getProgressInTier(periodSessionCount, currentTier)
    val tierColor = Color(currentTier.colorHex)
    val today = LocalDate.now()
    val daysRemaining = endDate?.let { java.time.temporal.ChronoUnit.DAYS.between(today, it).toInt().coerceAtLeast(0) } ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tier actuel - header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    currentTier.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = tierColor
                )
                Text(
                    currentTier.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "$periodSessionCount",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("séances", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Progression vers le tier suivant
        if (currentTier.tier < 7) {
            val nextTier = TIERS[currentTier.tier]
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("PROGRESSION", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                        Text(nextTier.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = tierColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val currentProgress = periodSessionCount - currentTier.minSessions + 1
                    val tierRange = currentTier.maxSessions - currentTier.minSessions + 1
                    val remaining = currentTier.maxSessions - periodSessionCount + 1
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$currentProgress / $tierRange", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$remaining restantes", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = tierColor)
                    }
                    if (endDate != null && daysRemaining > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "$daysRemaining ${if (daysRemaining > 1) "jours" else "jour"} avant la fin de période",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Liste des tiers
        Text("Tous les niveaux", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, start = 4.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                TIERS.forEachIndexed { index, tier ->
                    val isCurrentTier = tier.tier == currentTier.tier
                    val isUnlocked = periodSessionCount >= tier.minSessions
                    val tc = Color(tier.colorHex)

                    if (index > 0) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isCurrentTier) tc.copy(alpha = 0.06f) else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    tier.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrentTier) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else tc
                                )
                                if (isCurrentTier) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(tc.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("toi", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = tc)
                                    }
                                }
                            }
                            val rangeText = if (tier.tier == 7) "${tier.minSessions}+ séances" else "${tier.minSessions}–${tier.maxSessions} séances"
                            Text(rangeText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (!isUnlocked) 0.4f else 1f))
                        }
                        if (isUnlocked && !isCurrentTier) {
                            Text("✓", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, fontSize = 16.sp)
                        } else if (!isUnlocked) {
                            Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTab(viewModel: GymViewModel) {
    val sessionCount by viewModel.sessionCount.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val activitiesDefs by viewModel.activities.collectAsState()
    
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDateForActivity by remember { mutableStateOf<LocalDate?>(null) }
    
    // Utiliser allSessions pour l'affichage du calendrier (pas limité à la période)
    val sessionsByDate = allSessions.groupBy { it.date }
    val datesWithNotes by viewModel.datesWithStandaloneNotes.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 20.dp)) {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val thisWeekCount = allSessions.count { it.date in startOfWeek..endOfWeek }
        val thisMonthCount = allSessions.count { YearMonth.from(it.date) == YearMonth.now() }

        // Stats row
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCell(value = "$sessionCount", label = "total")
                VerticalDivider(modifier = Modifier.height(32.dp))
                StatCell(value = "$thisMonthCount", label = "ce mois")
                VerticalDivider(modifier = Modifier.height(32.dp))
                StatCell(value = "$thisWeekCount", label = "cette sem.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mois précédent", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mois suivant", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val firstDayOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
        val daysInMonth = currentMonth.lengthOfMonth()
        val totalCells = ((firstDayOfWeek - 1) + daysInMonth + 6) / 7 * 7
        val days = (1..totalCells).map { index ->
            val dayOffset = index - firstDayOfWeek
            if (dayOffset in 0 until daysInMonth) currentMonth.atDay(dayOffset + 1) else null
        }
        
        // Grille calendrier (Column+Row au lieu de LazyVerticalGrid pour que la zone vide
        // en dessous ne consomme pas les swipes dédiés à la navigation d'onglets)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(currentMonth) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onDragEnd = {
                            if (totalDrag > 100f) currentMonth = currentMonth.minusMonths(1)
                            else if (totalDrag < -100f) currentMonth = currentMonth.plusMonths(1)
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDrag += dragAmount
                        }
                    )
                },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            days.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { date ->
                        val sessions = date?.let { sessionsByDate[it] } ?: emptyList()
                        val isGymDay = sessions.isNotEmpty()
                        val isFuture = date?.isAfter(today) == true
                        val hasNote = date != null && datesWithNotes.contains(date)
                        Box(modifier = Modifier.weight(1f)) {
                            CalendarDay(
                                date = date,
                                isGymDay = isGymDay,
                                activities = sessions.map { it.activity },
                                activityDefs = activitiesDefs,
                                isToday = date == today,
                                isFuture = isFuture,
                                hasNote = hasNote,
                                onClick = { if (date != null) selectedDateForActivity = date }
                            )
                        }
                    }
                }
            }
        }
        // Zone vide = propagée au HorizontalPager pour swipe vers autre onglet
        Spacer(modifier = Modifier.weight(1f))
    }
    
    selectedDateForActivity?.let { date ->
        MultiActivitySelectionDialog(
            date = date,
            viewModel = viewModel,
            existingSessions = allSessions.filter { it.date == date },
            onDismiss = { selectedDateForActivity = null }
        )
    }
}

@Composable
fun MultiActivitySelectionDialog(
    date: LocalDate,
    viewModel: GymViewModel,
    existingSessions: List<GymSession>,
    onDismiss: () -> Unit
) {
    val activitiesDefs by viewModel.activities.collectAsState()
    val existingActivities = existingSessions.map { it.activity }.toSet()
    var selectedActivities by remember { mutableStateOf(existingActivities) }
    var noteText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val today = LocalDate.now()
    val isFutureDate = date.isAfter(today)
    
    // Charger la note existante (session note OU note indépendante)
    LaunchedEffect(date) {
        val sessionNote = viewModel.getNoteForDate(date)
        val standaloneNote = viewModel.getStandaloneNoteForDate(date)
        noteText = sessionNote.ifEmpty { standaloneNote }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(date.format(dateFormatter), style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sélectionne les activités :", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                if (isFutureDate) {
                    Text("Séance en prévision", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                activitiesDefs.forEach { actDef ->
                    val activity = actDef.name
                    val isSelected = selectedActivities.contains(activity)
                    val isFree = actDef.price <= 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { selectedActivities = if (isSelected) selectedActivities - activity else selectedActivities + activity }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isSelected, onCheckedChange = { checked -> selectedActivities = if (checked) selectedActivities + activity else selectedActivities - activity })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(actDef.emoji + " " + activity)
                        if (isFree) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("(gratuit)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(if (isFutureDate) "Notes / prévision" else "Notes") },
                    placeholder = { Text(if (isFutureDate) "Objectifs, programme..." else "Ressenti, remarques...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        // Supprimer les activités désélectionnées (suspend → ordonné)
                        existingActivities.forEach { activity ->
                            if (!selectedActivities.contains(activity)) {
                                viewModel.removeActivitySuspend(date, activity)
                            }
                        }
                        // Ajouter les nouvelles activités (suspend → avant la note)
                        selectedActivities.forEach { activity ->
                            if (!existingActivities.contains(activity)) {
                                viewModel.addSessionSuspend(date, activity)
                            }
                        }
                        // Sauvegarder la note après insertion des séances
                        if (selectedActivities.isNotEmpty()) {
                            viewModel.updateNoteSuspend(date, noteText)
                            viewModel.saveStandaloneNote(date, "") // nettoyer la note standalone
                        } else {
                            // Pas d'activité → sauvegarder comme note indépendante
                            viewModel.saveStandaloneNote(date, noteText)
                        }
                        onDismiss()
                    }
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun CalendarDay(
    date: LocalDate?,
    isGymDay: Boolean,
    activities: List<String>,
    activityDefs: List<ActivityDefinition> = emptyList(),
    isToday: Boolean,
    isFuture: Boolean,
    hasNote: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isGymDay && isFuture -> Color(0xFF2563EB).copy(alpha = 0.35f)
        isGymDay -> Color(0xFF2563EB)
        isToday -> MaterialTheme.colorScheme.primaryContainer
        hasNote -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
        else -> Color.Transparent
    }

    val textColor = when {
        isGymDay && isFuture -> Color.White.copy(alpha = 0.7f)
        isGymDay -> Color.White
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        date == null -> Color.Transparent
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .then(if (isToday && !isGymDay) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)) else Modifier)
            .clickable(enabled = date != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(date.dayOfMonth.toString(), color = textColor, fontWeight = if (isGymDay || isToday) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                if (isGymDay && activities.isNotEmpty()) {
                    val displayEmoji = if (activities.size > 1) "🏆" else getActivityEmoji(activities.first(), activityDefs)
                    Text(displayEmoji, fontSize = 10.sp)
                } else if (hasNote && !isGymDay) {
                    // Point discret indiquant une note standalone
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f))
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsTab(viewModel: GymViewModel) {
    val activitiesDefs by viewModel.activities.collectAsState()
    val subscriptionPrice by viewModel.subscriptionPrice.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var autoEnd365 by remember { mutableStateOf(true) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var pendingDeleteName by remember { mutableStateOf<String?>(null) }
    var addingNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newEmoji by remember { mutableStateOf("\uD83C\uDFC3") }
    var newPriceText by remember { mutableStateOf("") }
    var editingActivity by remember { mutableStateOf<ActivityDefinition?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH)
    
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 20.dp, bottom = 24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ACTIVITÉS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                activitiesDefs.forEachIndexed { index, actDef ->
                    key(actDef.name) {
                        var priceText by remember(actDef.price) { 
                            mutableStateOf(if (actDef.price > 0) actDef.price.toInt().toString() else "") 
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                actDef.emoji + " " + actDef.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { editingActivity = actDef }
                                    .weight(1f)
                            )
                            if (pendingDeleteName == actDef.name) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = { pendingDeleteName = null },
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) { Text("Annuler", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    IconButton(
                                        onClick = { viewModel.removeActivity(actDef.name); pendingDeleteName = null },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Confirmer suppression", modifier = Modifier.size(18.dp), tint = Color(0xFFDC2626))
                                    }
                                }
                            } else {
                                IconButton(
                                    onClick = { pendingDeleteName = actDef.name },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Supprimer", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                    priceText = newValue
                                    viewModel.updateActivity(actDef.name, actDef.copy(price = newValue.toDoubleOrNull() ?: 0.0))
                                }
                            },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            suffix = { Text("€/an") }
                        )
                        
                        if (index < activitiesDefs.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (addingNew) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("NOUVELLE ACTIVITÉ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newEmoji,
                            onValueChange = { newEmoji = it },
                            label = { Text("Tag") },
                            modifier = Modifier.width(76.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Nom") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPriceText,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) newPriceText = it },
                        label = { Text("Prix annuel") },
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        suffix = { Text("€/an") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { addingNew = false; newName = ""; newEmoji = "\uD83C\uDFC3"; newPriceText = "" },
                            modifier = Modifier.weight(1f)
                        ) { Text("Annuler") }
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    viewModel.addActivity(ActivityDefinition(newName.trim(), newEmoji.trim().ifEmpty { "\uD83C\uDFC3" }, newPriceText.toDoubleOrNull() ?: 0.0))
                                    addingNew = false; newName = ""; newEmoji = "\uD83C\uDFC3"; newPriceText = ""
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Ajouter") }
                    }
                } else {
                    OutlinedButton(
                        onClick = { addingNew = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ajouter une activité")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL ANNUEL:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(subscriptionPrice.toInt().toString() + "€", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("PÉRIODE DE SUIVI", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { showStartDatePicker = true }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                    Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Début : " + (startDate?.format(dateFormatter)?.replaceFirstChar { it.uppercase() } ?: "--"))
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (!autoEnd365) {
                    OutlinedButton(onClick = { showEndDatePicker = true }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fin : " + (endDate?.format(dateFormatter)?.replaceFirstChar { it.uppercase() } ?: "--"))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text(
                        "Fin : " + (endDate?.format(dateFormatter)?.replaceFirstChar { it.uppercase() } ?: "--"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            autoEnd365 = !autoEnd365
                            if (autoEnd365 && startDate != null) viewModel.updateStartDateWithAutoEnd(startDate!!)
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("365 jours automatiques", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Switch(
                        checked = autoEnd365,
                        onCheckedChange = {
                            autoEnd365 = it
                            if (it && startDate != null) viewModel.updateStartDateWithAutoEnd(startDate!!)
                        }
                    )
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("COMMENT ÇA MARCHE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "1. Ajoute tes activités et définis ta période dans Réglages\n" +
                    "2. Pointe tes séances du jour dans Suivi\n" +
                    "3. Consulte ton calendrier, tes stats et ton niveau",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("EXPORT / IMPORT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Sauvegarde tes données (séances + config) en JSON.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                val exportImportScope = rememberCoroutineScope()
                val exportImportContext = LocalContext.current
                var showImportDialog by remember { mutableStateOf(false) }
                var pendingImportJson by remember { mutableStateOf<String?>(null) }
                
                val exportLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/json")
                ) { uri ->
                    uri?.let {
                        exportImportScope.launch {
                            try {
                                val json = viewModel.exportDataToJson()
                                exportImportContext.contentResolver.openOutputStream(it)?.use { stream ->
                                    stream.write(json.toByteArray())
                                }
                                Toast.makeText(exportImportContext, "Export réussi", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(exportImportContext, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                
                val importLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri ->
                    uri?.let {
                        try {
                            val json = exportImportContext.contentResolver.openInputStream(it)?.bufferedReader()?.readText() ?: ""
                            pendingImportJson = json
                            showImportDialog = true
                        } catch (e: Exception) {
                            Toast.makeText(exportImportContext, "❌ Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { exportLauncher.launch("muscunombre_backup.json") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Exporter")
                    }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Importer")
                    }
                }
                
                if (showImportDialog) {
                    AlertDialog(
                        onDismissRequest = { showImportDialog = false; pendingImportJson = null },
                        title = { Text("Importer des données", fontWeight = FontWeight.Bold) },
                        text = {
                            Text("Cette action va remplacer toutes tes données actuelles (séances et configuration) par celles du fichier.\n\nCette action est irréversible.")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    pendingImportJson?.let { json ->
                                        try {
                                            viewModel.importDataFromJson(json)
                                            Toast.makeText(exportImportContext, "Import réussi", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(exportImportContext, "Fichier invalide: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    showImportDialog = false
                                    pendingImportJson = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) { Text("Oui, importer") }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showImportDialog = false; pendingImportJson = null }) {
                                Text("Annuler")
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().clickable { showAboutDialog = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("À propos", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text("›", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDC2626).copy(alpha = 0.06f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Réinitialiser toutes les données", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
    
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Confirmation", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
            text = {
                Text("Supprimer toutes les données ?\n\nCette action est irréversible : séances, historique, progression et configuration seront perdus.", style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetAllData(); showResetDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) { Text("OUI, supprimer") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) { Text("NON, annuler") }
            }
        )
    }
    
    if (showAboutDialog) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher),
                        contentDescription = "Logo Muscunombre",
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Muscunombre", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Version 4.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    HorizontalDivider()
                    
                    Text("Suivi de séances et calcul du coût par séance.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                    
                    HorizontalDivider()
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Développeurs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Jade Senterre", fontWeight = FontWeight.Medium)
                            Text(
                                "senterrejade@gmail.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:senterrejade@gmail.com") })
                                }
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Alexandre Kim", fontWeight = FontWeight.Medium)
                            Text(
                                "kim.alxn@gmail.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:kim.alxn@gmail.com") })
                                }
                            )
                        }
                    }
                    
                    HorizontalDivider()
                    
                    Text(
                        "github.com/kimalxn/muscunombre",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kimalxn/muscunombre")))
                        }
                    )
                    
                    Text("Kotlin · Jetpack Compose · Room · Material 3", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) { Text("Fermer") }
            }
        )
    }
    
    if (showStartDatePicker) {
        DatePickerDialog(
            currentDate = startDate ?: LocalDate.now(),
            onDateSelected = {
                if (autoEnd365) viewModel.updateStartDateWithAutoEnd(it) else viewModel.updateStartDateOnly(it)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            currentDate = endDate ?: (startDate?.plusDays(365) ?: LocalDate.now()),
            onDateSelected = { viewModel.updateEndDate(it); showEndDatePicker = false },
            onDismiss = { showEndDatePicker = false }
        )
    }
    
    editingActivity?.let { actDef ->
        EditActivityDialog(
            activityDef = actDef,
            onSave = { viewModel.updateActivity(actDef.name, it); editingActivity = null },
            onDismiss = { editingActivity = null }
        )
    }
}

@Composable
fun EditActivityDialog(
    activityDef: ActivityDefinition?,
    onSave: (ActivityDefinition) -> Unit,
    onDismiss: () -> Unit
) {
    val isNew = activityDef == null
    var name by remember { mutableStateOf(activityDef?.name ?: "") }
    var emoji by remember { mutableStateOf(activityDef?.emoji ?: "🏃") }
    var priceText by remember { mutableStateOf(if (activityDef != null && activityDef.price > 0) activityDef.price.toInt().toString() else "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Nouvelle activité" else "Modifier l'activité", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Tag") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de l'activité") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) priceText = it },
                    label = { Text("Prix annuel") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("€/an") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(ActivityDefinition(
                            name = name.trim(),
                            emoji = emoji.ifBlank { "🏃" },
                            price = priceText.toDoubleOrNull() ?: 0.0
                        ))
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (isNew) "Ajouter" else "Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    currentDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate.toEpochDay() * 24 * 60 * 60 * 1000
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onDateSelected(selectedDate)
                    }
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
