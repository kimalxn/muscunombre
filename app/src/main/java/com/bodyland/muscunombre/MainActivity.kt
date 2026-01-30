package com.bodyland.muscunombre

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bodyland.muscunombre.data.GymSession
import com.bodyland.muscunombre.data.ACTIVITIES
import com.bodyland.muscunombre.TIERS
import com.bodyland.muscunombre.getTierForSessions
import com.bodyland.muscunombre.getProgressInTier
import com.bodyland.muscunombre.ui.theme.MuscuNombreTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
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
    
    // Date de fin calculée automatiquement
    val endDate = startDate.plusDays(365)
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "🐀 Gym Rat",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "👋 Bienvenue !",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Choisis ta date de début d'abonnement pour commencer à tracker tes séances (365 jours).",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "📅 Période d'abonnement (365 jours)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Date de début: ${startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "Date de fin: ${endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Durée: 365 jours",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { viewModel.completeOnboarding(startDate) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("🚀 Commencer !", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    
    if (showStartDatePicker) {
        DatePickerDialog(
            currentDate = startDate,
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: GymViewModel) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Suivi", "Calendrier", "Utilisateur", "Réglages")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "🐀 Gym Rat",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Filled.Add, contentDescription = null)
                                1 -> Icon(Icons.Filled.DateRange, contentDescription = null)
                                2 -> Icon(Icons.Filled.Person, contentDescription = null)
                                3 -> Icon(Icons.Filled.Settings, contentDescription = null)
                            }
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> SessionTrackingTab(viewModel)
                1 -> CalendarTab(viewModel)
                2 -> UserTab(viewModel)
                3 -> SettingsTab(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionTrackingTab(viewModel: GymViewModel) {
    val sessionCount by viewModel.sessionCount.collectAsState()
    val subscriptionPrice by viewModel.subscriptionPrice.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    
    var selectedActivity by remember { mutableStateOf(ACTIVITIES[0]) }
    var activityExpanded by remember { mutableStateOf(false) }
    
    val pricePerSession = if (sessionCount > 0) subscriptionPrice / sessionCount else 0.0
    val daysRemaining = if (endDate != null) ChronoUnit.DAYS.between(LocalDate.now(), endDate).toInt().coerceAtLeast(0) else 0
    val totalDays = if (startDate != null && endDate != null) ChronoUnit.DAYS.between(startDate, endDate).toInt() else 0
    val daysPassed = if (startDate != null) ChronoUnit.DAYS.between(startDate, LocalDate.now()).toInt().coerceAtLeast(0) else 0
    
    // Vérifier si aujourd'hui est déjà pointé
    val today = LocalDate.now()
    val todayAlreadyLogged = allSessions.any { it.date == today }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Période de suivi
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "📅 Période de suivi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${startDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "--"} → ${endDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "--"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Jour $daysPassed / $totalDays • $daysRemaining jours restants",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        // Compteur de séances
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Total séances",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "$sessionCount",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Sélecteur d'activité
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "🏃 Activité",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = activityExpanded,
                    onExpandedChange = { activityExpanded = !activityExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedActivity,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = activityExpanded,
                        onDismissRequest = { activityExpanded = false }
                    ) {
                        ACTIVITIES.forEach { activity ->
                            DropdownMenuItem(
                                text = { Text(activity) },
                                onClick = {
                                    selectedActivity = activity
                                    activityExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Bouton principal
        Button(
            onClick = { viewModel.addTodaySession(selectedActivity) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (todayAlreadyLogged) Color(0xFF9E9E9E) else Color(0xFF4CAF50)
            ),
            enabled = !todayAlreadyLogged
        ) {
            Text(
                if (todayAlreadyLogged) "✅ Déjà pointé aujourd'hui !" else "✅ Pointer : $selectedActivity",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Prix par séance
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "💰 Coût par séance",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (sessionCount > 0) {
                    Text(
                        "%.2f €".format(pricePerSession),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        "(${subscriptionPrice.toInt()}€ ÷ $sessionCount séances)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        "-- €",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        "Commence à t'entraîner !",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Barre de progression Gamification
        val currentTier = getTierForSessions(sessionCount)
        val progress = getProgressInTier(sessionCount, currentTier)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "${currentTier.emoji} Tier ${currentTier.tier}: ${currentTier.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = Color(0xFF4CAF50),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (currentTier.tier < 6) {
                    val nextTier = TIERS[currentTier.tier]
                    Text(
                        "${currentTier.maxSessions - sessionCount + 1} séances pour Tier ${nextTier.tier}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Tu as atteint le niveau maximum ! 🏆",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFD700)
                    )
                }
            }
        }
        
        // Total activités par type pendant la période
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "🏃 Activités pendant la période",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Compter les activités par type
                val activityCounts = allSessions.groupingBy { it.activity }.eachCount()
                
                if (activityCounts.isEmpty()) {
                    Text(
                        "Aucune séance enregistrée",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                } else {
                    ACTIVITIES.forEach { activity ->
                        val count = activityCounts[activity] ?: 0
                        if (count > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(activity, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "$count séance${if (count > 1) "s" else ""}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Bouton de réinitialisation
        OutlinedButton(
            onClick = { viewModel.resetSessions() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔄 Réinitialiser le compteur")
        }
    }
}

// ============== USER TAB (GAMIFICATION) ==============

@Composable
fun UserTab(viewModel: GymViewModel) {
    val sessionCount by viewModel.sessionCount.collectAsState()
    val currentTier = getTierForSessions(sessionCount)
    val progress = getProgressInTier(sessionCount, currentTier)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profil actuel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    currentTier.emoji,
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tier ${currentTier.tier}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    currentTier.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    currentTier.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "$sessionCount séances",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Progression vers le prochain tier
        if (currentTier.tier < 6) {
            val nextTier = TIERS[currentTier.tier]
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Progression vers Tier ${nextTier.tier}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${nextTier.emoji} ${nextTier.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${sessionCount - currentTier.minSessions + 1} / ${currentTier.maxSessions - currentTier.minSessions + 1} séances",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Encore ${currentTier.maxSessions - sessionCount + 1} séances !",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Liste des tiers
        Text(
            "🏆 Tous les Tiers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        TIERS.forEach { tier ->
            val isCurrentTier = tier.tier == currentTier.tier
            val isUnlocked = sessionCount >= tier.minSessions
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isCurrentTier -> MaterialTheme.colorScheme.primaryContainer
                        isUnlocked -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        tier.emoji,
                        fontSize = 32.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Tier ${tier.tier}: ${tier.name}",
                                fontWeight = FontWeight.Bold,
                                color = if (!isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.onSurface
                            )
                            if (isCurrentTier) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "← TOI",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(
                            if (tier.tier == 6) "${tier.minSessions}+ séances"
                            else "${tier.minSessions}-${tier.maxSessions} séances",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (!isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isUnlocked) {
                        Text("✓", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    } else {
                        Text("🔒", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

// ============== CALENDAR TAB ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTab(viewModel: GymViewModel) {
    val allSessions by viewModel.allSessions.collectAsState()
    val sessionCount by viewModel.sessionCount.collectAsState()
    
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDateForActivity by remember { mutableStateOf<LocalDate?>(null) }
    
    val sessionDates = allSessions.map { it.date }.toSet()
    // Map date to activity for display
    val sessionActivities = allSessions.associate { it.date to it.activity }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Stats résumé
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$sessionCount", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text("séances", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val thisMonth = allSessions.count { 
                        YearMonth.from(it.date) == YearMonth.now() 
                    }
                    Text("$thisMonth", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text("ce mois", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Navigation mois
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Mois précédent")
            }
            Text(
                currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH))
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Mois suivant")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Jours de la semaine
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Grille du calendrier
        val firstDayOfMonth = currentMonth.atDay(1)
        val lastDayOfMonth = currentMonth.atEndOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 = Lundi
        val daysInMonth = currentMonth.lengthOfMonth()
        
        // Calculer les jours à afficher (avec padding pour aligner)
        val totalCells = ((firstDayOfWeek - 1) + daysInMonth + 6) / 7 * 7
        val days = (1..totalCells).map { index ->
            val dayOffset = index - firstDayOfWeek
            if (dayOffset in 0 until daysInMonth) {
                currentMonth.atDay(dayOffset + 1)
            } else {
                null
            }
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(days) { date ->
                CalendarDay(
                    date = date,
                    isGymDay = date?.let { sessionDates.contains(it) } ?: false,
                    activity = date?.let { sessionActivities[it] },
                    isToday = date == LocalDate.now(),
                    onClick = { 
                        date?.let { clickedDate ->
                            if (sessionDates.contains(clickedDate)) {
                                // Si déjà pointé, supprimer
                                viewModel.removeSessionOnDate(clickedDate)
                            } else {
                                // Sinon, afficher le dialog pour choisir l'activité
                                selectedDateForActivity = clickedDate
                            }
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bouton ajouter une date manuellement
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ Ajouter une date manuellement")
        }
        
        // Légende activités
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("🏃 Activités:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ACTIVITIES.forEach { activity ->
                        Text(activity, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
    
    // Dialog pour ajout manuel avec activité
    if (showAddDialog) {
        AddSessionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { date, activity ->
                viewModel.addSessionOnDate(date, activity)
                showAddDialog = false
            }
        )
    }
    
    // Dialog pour choisir l'activité sur une date cliquée
    selectedDateForActivity?.let { date ->
        ActivitySelectionDialog(
            date = date,
            onDismiss = { selectedDateForActivity = null },
            onActivitySelected = { activity ->
                viewModel.addSessionOnDate(date, activity)
                selectedDateForActivity = null
            }
        )
    }
}

// Dialog pour sélectionner une activité
@Composable
fun ActivitySelectionDialog(
    date: LocalDate,
    onDismiss: () -> Unit,
    onActivitySelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("📅 ${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Choisis ton activité :",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ACTIVITIES.forEach { activity ->
                    OutlinedButton(
                        onClick = { onActivitySelected(activity) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(activity)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun CalendarDay(
    date: LocalDate?,
    isGymDay: Boolean,
    activity: String?,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isGymDay -> Color(0xFF4CAF50) // Vert pour jour pointé
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    
    val textColor = when {
        isGymDay -> Color.White
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        date == null -> Color.Transparent
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isToday && !isGymDay) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else Modifier
            )
            .clickable(enabled = date != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = date.dayOfMonth.toString(),
                    color = textColor,
                    fontWeight = if (isGymDay || isToday) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
                if (isGymDay) {
                    Text("💪", fontSize = 8.sp)
                }
            }
        }
    }
}

// ============== ADD SESSION DIALOG ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, String) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedActivity by remember { mutableStateOf(ACTIVITIES[0]) }
    var activityExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("➕ Ajouter une séance") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sélecteur de date
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📅 ${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                }
                
                // Sélecteur d'activité
                ExposedDropdownMenuBox(
                    expanded = activityExpanded,
                    onExpandedChange = { activityExpanded = !activityExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedActivity,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Activité") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = activityExpanded,
                        onDismissRequest = { activityExpanded = false }
                    ) {
                        ACTIVITIES.forEach { activity ->
                            DropdownMenuItem(
                                text = { Text(activity) },
                                onClick = {
                                    selectedActivity = activity
                                    activityExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedDate, selectedActivity) }) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            currentDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

// ============== SETTINGS TAB ==============

@Composable
fun SettingsTab(viewModel: GymViewModel) {
    val subscriptionPrice by viewModel.subscriptionPrice.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    
    var priceText by remember(subscriptionPrice) { 
        mutableStateOf(if (subscriptionPrice > 0) subscriptionPrice.toInt().toString() else "") 
    }
    var showStartDatePicker by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Prix de l'abonnement
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "💳 Prix de l'abonnement annuel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            priceText = newValue
                            val price = newValue.toDoubleOrNull() ?: 0.0
                            viewModel.updateSubscriptionPrice(price)
                        }
                    },
                    label = { Text("Prix en euros (€)") },
                    placeholder = { Text("Ex: 400") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("€") }
                )
                if (subscriptionPrice > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Abonnement actuel: ${subscriptionPrice.toInt()}€/an",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Période de suivi
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "📅 Période de suivi (365 jours)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date de début (éditable)
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Date de début: ${startDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "--"}")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Date de fin (calculée automatiquement)
                Text(
                    "Date de fin: ${endDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "--"}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Durée: 365 jours (automatique)",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "ℹ️ Comment ça marche ?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "1. Renseigne le prix de ton abonnement annuel\n" +
                    "2. Définis ta date de début (fin = +365 jours)\n" +
                    "3. Clique sur le bouton à chaque visite à la salle\n" +
                    "4. Le coût par séance se calcule automatiquement !",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    
    // Date Picker pour la date de début
    if (showStartDatePicker) {
        DatePickerDialog(
            currentDate = startDate ?: LocalDate.now(),
            onDateSelected = { date ->
                viewModel.updateStartDateWithAutoEnd(date)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
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
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
