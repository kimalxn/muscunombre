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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.bodyland.muscunombre.ui.theme.LC
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

        LcCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                LcSectionHeader("PÉRIODE DE SUIVI")
                Column(modifier = Modifier.padding(20.dp)) {
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
                    Text("365 jours", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = LC.Blue)
                }
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LC.Blue)
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Muscunombre",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = LC.White
                    )
                }
                HorizontalDivider(color = LC.Black, thickness = 2.dp)
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
            userScrollEnabled = pagerState.currentPage != 1,
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LC.White)
    ) {
        HorizontalDivider(color = LC.Black, thickness = 2.dp)
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
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (selected) LC.Blue else Color(0xFF555555),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) LC.Black else Color(0xFF555555),
                            letterSpacing = 0.3.sp
                        )
                        // Yellow indicator
                        Box(
                            modifier = Modifier
                                .size(width = 28.dp, height = 3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (selected) LC.Yellow else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Ligne Claire helpers — flat white card + 2dp black border
// ──────────────────────────────────────────────────────────────
@Composable
fun LcCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium,
    containerColor: Color = LC.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.border(2.dp, LC.Black, shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

@Composable
fun LcSectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LC.Yellow)
            .border(width = 0.dp, color = Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = LC.Black,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Black
        )
    }
    HorizontalDivider(color = LC.Black, thickness = 1.5.dp)
}

@Composable
fun SessionTrackingTab(viewModel: GymViewModel) {
    val sessionCount by viewModel.sessionCount.collectAsState()
    val activitiesDefs by viewModel.activities.collectAsState()
    val subscriptionPrice by viewModel.subscriptionPrice.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    val scope = rememberCoroutineScope()
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val dayNameFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)

    var selectedActivities by remember { mutableStateOf(setOf<String>()) }
    val todayActivities = allSessions.filter { it.date == today }.map { it.activity }.toSet()
    LaunchedEffect(todayActivities) { selectedActivities = todayActivities }

    val daysRemaining = endDate?.let { java.time.temporal.ChronoUnit.DAYS.between(today, it).toInt().coerceAtLeast(0) } ?: 0
    val totalDays = if (startDate != null && endDate != null) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt() else 0
    val daysPassed = startDate?.let { java.time.temporal.ChronoUnit.DAYS.between(it, today).toInt().coerceAtLeast(0) } ?: 0
    val activityCounts = allSessions.groupingBy { it.activity }.eachCount()
    val paidSessionCount = activitiesDefs.filter { it.price > 0 }.sumOf { activityCounts[it.name] ?: 0 }
    val globalPricePerSession = if (paidSessionCount > 0 && subscriptionPrice > 0) subscriptionPrice / paidSessionCount else 0.0
    val currentTier = getTierForSessions(sessionCount)
    val progress = getProgressInTier(sessionCount, currentTier)

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
                "Jour $daysPassed · $daysRemaining jours restants",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Stats row
        LcCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCell(value = "$sessionCount", label = "séances")
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
        LcCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                LcSectionHeader("AUJOURD'HUI")
                Column(modifier = Modifier.padding(16.dp)) {

                activitiesDefs.forEachIndexed { index, actDef ->
                    val activity = actDef.name
                    val alreadyLogged = todayActivities.contains(activity)
                    val isSelected = selectedActivities.contains(activity)

                    if (index > 0) HorizontalDivider(color = LC.Black, thickness = 1.dp)

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
                            targetValue = if (isSelected) LC.Yellow else Color(0xFFDDDDDD),
                            animationSpec = tween(200), label = "pill"
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 26.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .border(1.5.dp, LC.Black, RoundedCornerShape(13.dp))
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
                            selectedActivities.forEach { if (!todayActivities.contains(it)) viewModel.addTodaySession(it) }
                            todayActivities.forEach { if (!selectedActivities.contains(it)) viewModel.removeTodaySession(it) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(2.dp, LC.Black, MaterialTheme.shapes.small),
                    enabled = hasChanges,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LC.Yellow,
                        contentColor = LC.Black,
                        disabledContainerColor = Color(0xFFEEEEEE),
                        disabledContentColor = Color(0xFF888888)
                    )
                ) {
                    Text("Valider", fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 0.sp)
                }
            }
            }
        }

        // Période
        LcCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                LcSectionHeader("PÉRIODE")
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Début", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(startDate?.format(dateFormatter) ?: "--", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fin", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(endDate?.format(dateFormatter) ?: "--", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Prix par activité (si configuré)
        val paidActivities = activitiesDefs.filter { it.price > 0 }
        if (paidActivities.isNotEmpty()) {
            LcCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    LcSectionHeader("COÛT PAR SÉANCE")
                    Column(modifier = Modifier.padding(16.dp)) {
                        paidActivities.forEachIndexed { index, actDef ->
                            val count = activityCounts[actDef.name] ?: 0
                            val pricePerSession = if (count > 0) actDef.price / count else 0.0
                            if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LC.Black, thickness = 1.dp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(actDef.emoji + "  " + actDef.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("$count séances · ${actDef.price.toInt()}€/an", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    if (count > 0) "%.2f €".format(pricePerSession) else "-- €",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (count > 0) LC.Blue else Color(0xFF555555)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Progression tier
        LcCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LC.Yellow)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("NIVEAU", style = MaterialTheme.typography.labelMedium, color = LC.Black, letterSpacing = 1.sp, fontWeight = FontWeight.Black)
                    Text(currentTier.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = Color(currentTier.colorHex))
                }
                HorizontalDivider(color = LC.Black, thickness = 1.5.dp)
                Column(modifier = Modifier.padding(16.dp)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color(currentTier.colorHex),
                        trackColor = LC.BgBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (currentTier.tier < 7) {
                        val nextTier = TIERS[currentTier.tier]
                        Text(
                            "${currentTier.maxSessions - sessionCount + 1} séances avant ${nextTier.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text("Niveau maximum atteint", style = MaterialTheme.typography.bodySmall, color = LC.Red)
                    }
                }
            }
        }

        // Activités enregistrées
        if (activityCounts.isNotEmpty()) {
            LcCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    LcSectionHeader("ACTIVITÉS")
                    Column(modifier = Modifier.padding(16.dp)) {
                        activityCounts.entries.sortedByDescending { it.value }.forEachIndexed { index, (activity, count) ->
                            if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LC.Black, thickness = 1.dp)
                            val emoji = getActivityEmoji(activity, activitiesDefs)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("$emoji  $activity", style = MaterialTheme.typography.bodyMedium)
                                Text("$count", fontWeight = FontWeight.Black, fontSize = 15.sp, color = LC.Blue)
                            }
                        }
                    }
                }
            }
        }

        // Compte rendu sur la période
        if (startDate != null && endDate != null && activityCounts.isNotEmpty()) {
            LcCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    LcSectionHeader("COMPTE RENDU")
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Entre le ${startDate?.format(dateFormatter) ?: "--"} et le ${endDate?.format(dateFormatter) ?: "--"}, tu as fait :",
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
}

@Composable
private fun StatCell(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = LC.Black)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF555555))
    }
}

@Composable
fun UserTab(viewModel: GymViewModel) {
    val sessionCount by viewModel.sessionCount.collectAsState()
    val currentTier = getTierForSessions(sessionCount)
    val progress = getProgressInTier(sessionCount, currentTier)
    val tierColor = Color(currentTier.colorHex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tier actuel - header
        LcCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(currentTier.emoji, fontSize = 52.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    currentTier.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
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
                    "$sessionCount",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = LC.Black
                )
                Text("séances", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF555555))
            }
        }

        // Progression vers le tier suivant
        if (currentTier.tier < 7) {
            val nextTier = TIERS[currentTier.tier]
            LcCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LC.Yellow)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PROGRESSION", style = MaterialTheme.typography.labelMedium, color = LC.Black, letterSpacing = 1.sp, fontWeight = FontWeight.Black)
                        Text(nextTier.emoji + "  " + nextTier.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = LC.Black, thickness = 1.5.dp)
                    Column(modifier = Modifier.padding(16.dp)) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = tierColor,
                            trackColor = LC.BgBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val currentProgress = sessionCount - currentTier.minSessions + 1
                        val tierRange = currentTier.maxSessions - currentTier.minSessions + 1
                        val remaining = currentTier.maxSessions - sessionCount + 1
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("$currentProgress / $tierRange", style = MaterialTheme.typography.bodySmall, color = Color(0xFF555555))
                            Text("$remaining restantes", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = tierColor)
                        }
                    }
                }
            }
        }

        // Liste des tiers
        Text("Tous les niveaux", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp, start = 4.dp))

        LcCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                TIERS.forEachIndexed { index, tier ->
                    val isCurrentTier = tier.tier == currentTier.tier
                    val isUnlocked = sessionCount >= tier.minSessions
                    val tc = Color(tier.colorHex)

                    if (index > 0) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = LC.Black, thickness = 1.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isCurrentTier) tc.copy(alpha = 0.06f) else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            tier.emoji,
                            fontSize = 24.sp,
                            modifier = Modifier.width(40.dp),
                            color = if (!isUnlocked) Color.Unspecified.copy(alpha = 0.3f) else Color.Unspecified
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    tier.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrentTier) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else tc
                                )
                                if (isCurrentTier) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(1.5.dp, LC.Black, RoundedCornerShape(4.dp))
                                            .background(LC.Yellow)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("vous", fontSize = 10.sp, fontWeight = FontWeight.Black, color = LC.Black)
                                    }
                                }
                            }
                            val rangeText = if (tier.tier == 7) "${tier.minSessions}+ séances" else "${tier.minSessions}–${tier.maxSessions} séances"
                            Text(rangeText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (!isUnlocked) 0.4f else 1f))
                        }
                        if (isUnlocked && !isCurrentTier) {
                            Text("✓", fontWeight = FontWeight.Black, color = LC.Blue, fontSize = 16.sp)
                        } else if (!isUnlocked) {
                            Text("·", color = Color(0xFF888888), fontSize = 20.sp)
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
    val sessionsInPeriod by viewModel.sessionsInPeriod.collectAsState()
    val sessionCount by viewModel.sessionCount.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val activitiesDefs by viewModel.activities.collectAsState()
    
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDateForActivity by remember { mutableStateOf<LocalDate?>(null) }
    
    // Utiliser allSessions pour l'affichage du calendrier (pas limité à la période)
    val sessionsByDate = allSessions.groupBy { it.date }
    
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 20.dp)) {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val thisWeekCount = allSessions.count { it.date in startOfWeek..endOfWeek }
        val thisMonthCount = allSessions.count { YearMonth.from(it.date) == YearMonth.now() }

        // Stats row
        LcCard(modifier = Modifier.fillMaxWidth()) {
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
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .weight(1f)
                .pointerInput(currentMonth) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onDragEnd = {
                            if (totalDrag > 100f) {
                                currentMonth = currentMonth.minusMonths(1)
                            } else if (totalDrag < -100f) {
                                currentMonth = currentMonth.plusMonths(1)
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDrag += dragAmount
                        }
                    )
                },
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(days) { date ->
                val sessions = date?.let { sessionsByDate[it] } ?: emptyList()
                val isGymDay = sessions.isNotEmpty()
                val isFuture = date?.isAfter(today) == true
                
                CalendarDay(
                    date = date,
                    isGymDay = isGymDay,
                    activities = sessions.map { it.activity },
                    activityDefs = activitiesDefs,
                    isToday = date == today,
                    isFuture = isFuture,
                    onClick = { 
                        if (date != null) {
                            selectedDateForActivity = date
                        }
                    }
                )
            }
        }
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
    
    // Charger la note existante
    LaunchedEffect(date) {
        noteText = viewModel.getNoteForDate(date)
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
                    placeholder = { Text(if (isFutureDate) "Ex: Objectifs, programme prévu..." else "Ex: Bonne séance, fatigue...") },
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
                        // Supprimer les activités désélectionnées
                        existingActivities.forEach { activity ->
                            if (!selectedActivities.contains(activity)) {
                                viewModel.removeActivityOnDate(date, activity)
                            }
                        }
                        // Ajouter les nouvelles activités
                        selectedActivities.forEach { activity ->
                            if (!existingActivities.contains(activity)) {
                                viewModel.addSessionOnDate(date, activity)
                            }
                        }
                        // Sauvegarder la note
                        if (selectedActivities.isNotEmpty()) {
                            viewModel.updateNoteForDate(date, noteText)
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
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isGymDay && isFuture -> Color(0xFF2563EB).copy(alpha = 0.35f)
        isGymDay -> Color(0xFF2563EB)
        isToday -> MaterialTheme.colorScheme.primaryContainer
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
    var showResetDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAddActivityDialog by remember { mutableStateOf(false) }
    var editingActivity by remember { mutableStateOf<ActivityDefinition?>(null) }
    
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 20.dp, bottom = 24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LcCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                LcSectionHeader("ACTIVITÉS")
                Column(modifier = Modifier.padding(16.dp)) {
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
                            IconButton(
                                onClick = { viewModel.removeActivity(actDef.name) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Supprimer",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                
                OutlinedButton(
                    onClick = { showAddActivityDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajouter une activité")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = LC.Black, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL ANNUEL:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(subscriptionPrice.toInt().toString() + "€", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = LC.Blue)
                }
                }
            }
        }
        
        LcCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                LcSectionHeader("PÉRIODE DE SUIVI")
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedButton(onClick = { showStartDatePicker = true }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                        Text("Date de début: " + (startDate?.format(dateFormatter) ?: "--"))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Date de fin: " + (endDate?.format(dateFormatter) ?: "--"), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Durée: 365 jours (automatique)", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = LC.Blue)
                }
            }
        }
        
        LcCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                LcSectionHeader("COMMENT ÇA MARCHE")
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "1. Configure tes activités et leurs prix annuels\n" +
                        "2. Définis ta date de début (fin = +365 jours)\n" +
                        "3. Pointe tes activités dans 'Suivi' ou 'Calendrier'\n" +
                        "4. Les activités à 0€ sont gratuites\n" +
                        "5. Le coût par séance est calculé par activité et globalement",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        LcCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                LcSectionHeader("EXPORT / IMPORT")
                Column(modifier = Modifier.padding(16.dp)) {
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
                                Toast.makeText(exportImportContext, "✅ Export réussi !", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(exportImportContext, "❌ Erreur: ${e.message}", Toast.LENGTH_LONG).show()
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
                        title = { Text("⚠️ Importer des données", fontWeight = FontWeight.Bold) },
                        text = {
                            Text("Cette action va remplacer toutes tes données actuelles (séances et configuration) par celles du fichier.\n\nCette action est irréversible.")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    pendingImportJson?.let { json ->
                                        try {
                                            viewModel.importDataFromJson(json)
                                            Toast.makeText(exportImportContext, "✅ Import réussi !", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(exportImportContext, "❌ Fichier invalide: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    showImportDialog = false
                                    pendingImportJson = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LC.Blue)
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
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LcCard(modifier = Modifier.fillMaxWidth().clickable { showAboutDialog = true }) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("À propos", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("›", fontSize = 20.sp, color = LC.Black)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LcCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color(0xFFFFEBEE)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .border(2.dp, LC.Black, MaterialTheme.shapes.small),
                    colors = ButtonDefaults.buttonColors(containerColor = LC.Red),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Réinitialiser toutes les données", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
    
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("⚠️ Confirmation", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
            text = {
                Text("Es-vous sûr de vouloir tout supprimer ?\n\nCette action est irréversible et vous allez perdre :\n• Toutes vos séances enregistrées\n• Votre historique complet\n• Votre progression de tier\n• Tous vos prix d'abonnements", style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetAllData(); showResetDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = LC.Red)
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
                    Text("Muscunombre", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF2E7D32))
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    HorizontalDivider()
                    
                    Text("🎯 Track tes séances de sport et optimise ton budget fitness !", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                    
                    HorizontalDivider()
                    
                    Text("👨‍💻 Réalisé avec ❤️ par", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Jade Senterre", fontWeight = FontWeight.Medium)
                        Text(
                            "senterrejade@gmail.com", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:senterrejade@gmail.com")
                                }
                                context.startActivity(intent)
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
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:kim.alxn@gmail.com")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                    
                    HorizontalDivider()
                    
                    Text("🔗 Code source", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "github.com/kimalxn/muscunombre", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kimalxn/muscunombre"))
                            context.startActivity(intent)
                        }
                    )
                    
                    HorizontalDivider()
                    
                    Text("🛠️ Technologies", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Kotlin • Jetpack Compose • Room • Material 3", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Made in Paris 🇫🇷 • 2026", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            onDateSelected = { viewModel.updateStartDateWithAutoEnd(it); showStartDatePicker = false },
            onDismiss = { showStartDatePicker = false }
        )
    }
    
    if (showAddActivityDialog) {
        EditActivityDialog(
            activityDef = null,
            onSave = { viewModel.addActivity(it); showAddActivityDialog = false },
            onDismiss = { showAddActivityDialog = false }
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
        title = { Text(if (isNew) "➕ Nouvelle activité" else "✏️ Modifier l'activité", fontWeight = FontWeight.Bold) },
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
