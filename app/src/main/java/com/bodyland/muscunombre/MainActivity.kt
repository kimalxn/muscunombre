package com.bodyland.muscunombre

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bodyland.muscunombre.data.GymSession
import com.bodyland.muscunombre.data.ACTIVITIES
import com.bodyland.muscunombre.data.GYMLIB_ACTIVITIES
import com.bodyland.muscunombre.data.SALLE_ACTIVITIES
import com.bodyland.muscunombre.data.EQUIPEMENT_ACTIVITIES
import com.bodyland.muscunombre.data.FREE_ACTIVITIES
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
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🐀 Vieux Rongeur", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6B7280)
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
            Text("👋 Bienvenue !", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Choisis ta date de début d'abonnement pour commencer à tracker tes séances (365 jours).",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📅 Période d'abonnement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Date de début: " + startDate.format(dateFormatter))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Date de fin: " + endDate.format(dateFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Durée: 365 jours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { viewModel.completeOnboarding(startDate) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("🚀 Commencer !", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
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
    val tabs = listOf("Suivi", "Calendrier", "Utilisateur", "Réglages")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    
    // Récupérer le tier courant pour le header dynamique
    val sessionCount by viewModel.sessionCount.collectAsState()
    val currentTier = getTierForSessions(sessionCount)
    val tierColor = Color(currentTier.colorHex)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        currentTier.emoji + " " + currentTier.name, 
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = tierColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
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

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
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
}

@Composable
fun SessionTrackingTab(viewModel: GymViewModel) {
    val sessionCount by viewModel.sessionCount.collectAsState()
    val gymlibPrice by viewModel.gymlibPrice.collectAsState()
    val runningPrice by viewModel.runningPrice.collectAsState()
    val workoutPrice by viewModel.workoutPrice.collectAsState()
    val subscriptionPrice by viewModel.subscriptionPrice.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    var selectedActivities by remember { mutableStateOf(setOf<String>()) }
    
    val todayActivities = allSessions.filter { it.date == today }.map { it.activity }.toSet()
    
    val daysRemaining = endDate?.let { java.time.temporal.ChronoUnit.DAYS.between(today, it).toInt().coerceAtLeast(0) } ?: 0
    val totalDays = if (startDate != null && endDate != null) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt() else 0
    val daysPassed = startDate?.let { java.time.temporal.ChronoUnit.DAYS.between(it, today).toInt().coerceAtLeast(0) } ?: 0
    
    // Compter les séances par catégorie (exclure "Autres")
    val gymlibCount = allSessions.count { GYMLIB_ACTIVITIES.contains(it.activity) }
    val salleCount = allSessions.count { SALLE_ACTIVITIES.contains(it.activity) }
    val equipementCount = allSessions.count { EQUIPEMENT_ACTIVITIES.contains(it.activity) }
    
    // Prix par séance par catégorie (seulement si prix > 0)
    val gymlibPricePerSession = if (gymlibPrice > 0 && gymlibCount > 0) gymlibPrice / gymlibCount else 0.0
    val sallePricePerSession = if (workoutPrice > 0 && salleCount > 0) workoutPrice / salleCount else 0.0
    val equipementPricePerSession = if (runningPrice > 0 && equipementCount > 0) runningPrice / equipementCount else 0.0
    
    // Séances payantes = uniquement les catégories avec prix > 0
    val paidSessionCount = (if (gymlibPrice > 0) gymlibCount else 0) + 
                           (if (workoutPrice > 0) salleCount else 0) + 
                           (if (runningPrice > 0) equipementCount else 0)
    val globalPricePerSession = if (paidSessionCount > 0 && subscriptionPrice > 0) subscriptionPrice / paidSessionCount else 0.0
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📅 Période de suivi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    (startDate?.format(dateFormatter) ?: "--") + " → " + (endDate?.format(dateFormatter) ?: "--"),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Jour $daysPassed / $totalDays • $daysRemaining jours restants",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Total séances", style = MaterialTheme.typography.titleMedium)
                Text("$sessionCount", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🏃 Pointer des activités", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                ACTIVITIES.forEach { activity ->
                    val alreadyLogged = todayActivities.contains(activity)
                    val isSelected = selectedActivities.contains(activity)
                    val isFree = FREE_ACTIVITIES.contains(activity)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (alreadyLogged) Color(0xFF059669).copy(alpha = 0.1f)
                                else if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable(enabled = !alreadyLogged) {
                                selectedActivities = if (isSelected) selectedActivities - activity else selectedActivities + activity
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected || alreadyLogged,
                            onCheckedChange = { checked ->
                                if (!alreadyLogged) {
                                    selectedActivities = if (checked) selectedActivities + activity else selectedActivities - activity
                                }
                            },
                            enabled = !alreadyLogged
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(getActivityEmoji(activity) + " " + activity, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        if (alreadyLogged) {
                            Text("✅", fontSize = 16.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val buttonText = if (selectedActivities.isEmpty()) "Sélectionne des activités" 
                    else "✅ Pointer " + selectedActivities.size + " activité(s)"
                
                Button(
                    onClick = {
                        scope.launch {
                            selectedActivities.forEach { activity ->
                                viewModel.addTodaySession(activity)
                            }
                            selectedActivities = emptySet()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = selectedActivities.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // Prix par séance GLOBAL
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("💰 Coût par séance (global)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (paidSessionCount > 0) {
                    Text("%.2f €".format(globalPricePerSession), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    Text("(" + subscriptionPrice.toInt() + "€ ÷ " + paidSessionCount + " séances payantes)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f))
                } else {
                    Text("-- €", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    Text("Commence à t'entraîner !", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        
        // Prix par catégorie
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💳 Coût par séance (par catégorie)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Gymlib
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("🚴💪🥊 Gymlib", fontWeight = FontWeight.Bold)
                        Text("$gymlibCount séances", style = MaterialTheme.typography.bodySmall)
                    }
                    if (gymlibPrice > 0 && gymlibCount > 0) {
                        Text("%.2f €".format(gymlibPricePerSession), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Text("-- €", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Salle
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("🏋️ Salle (Workout)", fontWeight = FontWeight.Bold)
                        Text("$salleCount séances", style = MaterialTheme.typography.bodySmall)
                    }
                    if (workoutPrice > 0 && salleCount > 0) {
                        Text("%.2f €".format(sallePricePerSession), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Text("-- €", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Équipement Running
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("👟 Équipement Running", fontWeight = FontWeight.Bold)
                        Text("$equipementCount séances", style = MaterialTheme.typography.bodySmall)
                    }
                    if (runningPrice > 0 && equipementCount > 0) {
                        Text("%.2f €".format(equipementPricePerSession), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Text("-- €", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        val currentTier = getTierForSessions(sessionCount)
        val progress = getProgressInTier(sessionCount, currentTier)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(currentTier.colorHex).copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(currentTier.emoji + " Tier " + currentTier.tier + ": " + currentTier.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color(currentTier.colorHex),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (currentTier.tier < 7) {
                    val nextTier = TIERS[currentTier.tier]
                    val sessionsLeft = currentTier.maxSessions - sessionCount + 1
                    Text("$sessionsLeft séances pour Tier " + nextTier.tier, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Tu as atteint le niveau maximum ! 🏆", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFD700))
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📊 Activités enregistrées", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                val activityCounts = allSessions.groupingBy { it.activity }.eachCount()
                
                if (activityCounts.isEmpty()) {
                    Text("Aucune séance enregistrée", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
                } else {
                    ACTIVITIES.forEach { activity ->
                        val count = activityCounts[activity] ?: 0
                        if (count > 0) {
                            val suffix = if (count > 1) "s" else ""
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(getActivityEmoji(activity) + " " + activity, style = MaterialTheme.typography.bodyMedium)
                                Text("$count séance$suffix", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserTab(viewModel: GymViewModel) {
    val sessionCount by viewModel.sessionCount.collectAsState()
    val currentTier = getTierForSessions(sessionCount)
    val progress = getProgressInTier(sessionCount, currentTier)
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(currentTier.colorHex).copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(currentTier.emoji, fontSize = 64.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tier " + currentTier.tier, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                Text(currentTier.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.height(8.dp))
                Text(currentTier.description, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Text("$sessionCount séances", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        if (currentTier.tier < 7) {
            val nextTier = TIERS[currentTier.tier]
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Progression vers Tier " + nextTier.tier, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(nextTier.emoji + " " + nextTier.name, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFBDBDBD))
                            .border(2.dp, Color(0xFF757575), RoundedCornerShape(10.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(currentTier.colorHex))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val currentProgress = sessionCount - currentTier.minSessions + 1
                    val tierRange = currentTier.maxSessions - currentTier.minSessions + 1
                    Text("$currentProgress / $tierRange séances", style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    val remaining = currentTier.maxSessions - sessionCount + 1
                    Text("Encore $remaining séances !", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        Text("🏆 Tous les Tiers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        
        TIERS.forEach { tier ->
            val isCurrentTier = tier.tier == currentTier.tier
            val isUnlocked = sessionCount >= tier.minSessions
            
            // Couleur du tier (grisée si non débloqué)
            val tierColor = Color(tier.colorHex)
            val cardColor = when {
                isCurrentTier -> tierColor.copy(alpha = 0.3f)
                isUnlocked -> tierColor.copy(alpha = 0.2f)
                else -> tierColor.copy(alpha = 0.08f) // Grisé mais visible
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        tier.emoji, 
                        fontSize = 32.sp, 
                        modifier = Modifier.padding(end = 16.dp),
                        color = if (!isUnlocked) Color.Gray else Color.Unspecified
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Tier " + tier.tier + ": " + tier.name, 
                                fontWeight = FontWeight.Bold, 
                                color = if (!isUnlocked) tierColor.copy(alpha = 0.5f) else tierColor
                            )
                            if (isCurrentTier) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("← TOI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tierColor)
                            }
                        }
                        val rangeText = if (tier.tier == 7) tier.minSessions.toString() + "+ séances" else tier.minSessions.toString() + "-" + tier.maxSessions + " séances"
                        Text(rangeText, style = MaterialTheme.typography.bodySmall, color = if (!isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        // Séances par mois et par semaine - seulement pour Tier 3+
                        if (tier.tier >= 3) {
                            val (monthText, weekText) = when (tier.tier) {
                                3 -> Pair("≈ 2-4 séances/mois", "≈ 0.5-1 séance/sem")
                                4 -> Pair("≈ 4-8 séances/mois", "≈ 1-2 séances/sem")
                                5 -> Pair("≈ 8-15 séances/mois", "≈ 2-3 séances/sem")
                                6 -> Pair("≈ 15-21 séances/mois", "≈ 3-5 séances/sem")
                                7 -> Pair("≈ 21+ séances/mois", "≈ 5+ séances/sem")
                                else -> Pair("", "")
                            }
                            val textAlpha = if (!isUnlocked) 0.4f else 0.7f
                            Text(monthText, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha))
                            Text(weekText, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha))
                        }
                    }
                    if (isUnlocked) {
                        Text("✓", color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                    } else {
                        Text("🔒", fontSize = 20.sp)
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
    
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDateForActivity by remember { mutableStateOf<LocalDate?>(null) }
    
    // Utiliser allSessions pour l'affichage du calendrier (pas limité à la période)
    val sessionsByDate = allSessions.groupBy { it.date }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val thisWeekCount = allSessions.count { it.date in startOfWeek..endOfWeek }
        val thisMonthCount = allSessions.count { YearMonth.from(it.date) == YearMonth.now() }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$sessionCount", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("total", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$thisMonthCount", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("ce mois", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$thisWeekCount", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("cette sem.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Mois précédent")
            }
            Text(
                currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Mois suivant")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            modifier = Modifier.weight(1f),
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
                    isToday = date == today,
                    isFuture = isFuture,
                    onClick = { 
                        if (date != null && !isFuture) {
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
    val existingActivities = existingSessions.map { it.activity }.toSet()
    var selectedActivities by remember { mutableStateOf(existingActivities) }
    val scope = rememberCoroutineScope()
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📅 " + date.format(dateFormatter)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sélectionne les activités :", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                ACTIVITIES.forEach { activity ->
                    val isSelected = selectedActivities.contains(activity)
                    val isFree = FREE_ACTIVITIES.contains(activity)
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
                        Text(getActivityEmoji(activity) + " " + activity)
                        if (isFree) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("(gratuit)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
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
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isGymDay -> Color(0xFF2563EB)
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    
    val textColor = when {
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
            .clickable(enabled = date != null && !isFuture) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(date.dayOfMonth.toString(), color = textColor, fontWeight = if (isGymDay || isToday) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                if (isGymDay && activities.isNotEmpty()) {
                    val displayEmoji = if (activities.size > 1) "🏆" else getActivityEmoji(activities.first())
                    Text(displayEmoji, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun SettingsTab(viewModel: GymViewModel) {
    val gymlibPrice by viewModel.gymlibPrice.collectAsState()
    val runningPrice by viewModel.runningPrice.collectAsState()
    val workoutPrice by viewModel.workoutPrice.collectAsState()
    val subscriptionPrice by viewModel.subscriptionPrice.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    
    var gymlibText by remember(gymlibPrice) { mutableStateOf(if (gymlibPrice > 0) gymlibPrice.toInt().toString() else "") }
    var runningText by remember(runningPrice) { mutableStateOf(if (runningPrice > 0) runningPrice.toInt().toString() else "") }
    var workoutText by remember(workoutPrice) { mutableStateOf(if (workoutPrice > 0) workoutPrice.toInt().toString() else "") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("💳 Prix des abonnements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Gymlib
                Text("🚴💪🥊 Gymlib (Dynamo, Circuit Training, Cardio Boxing)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = gymlibText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            gymlibText = newValue
                            viewModel.updateGymlibPrice(newValue.toDoubleOrNull() ?: 0.0)
                        }
                    },
                    placeholder = { Text("") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("€") }
                )
                if (gymlibPrice > 0) {
                    Text("Sous-total: " + gymlibPrice.toInt() + "€", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Salle (Workout)
                Text("🏋️ Salle (Workout)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = workoutText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            workoutText = newValue
                            viewModel.updateWorkoutPrice(newValue.toDoubleOrNull() ?: 0.0)
                        }
                    },
                    placeholder = { Text("") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("€") }
                )
                if (workoutPrice > 0) {
                    Text("Sous-total: " + workoutPrice.toInt() + "€", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Équipement Running
                Text("👟 Équipement Running (chaussures, etc.)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = runningText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            runningText = newValue
                            viewModel.updateRunningPrice(newValue.toDoubleOrNull() ?: 0.0)
                        }
                    },
                    placeholder = { Text("") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("€") }
                )
                if (runningPrice > 0) {
                    Text("Sous-total: " + runningPrice.toInt() + "€", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("📅 Période de suivi (365 jours)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = { showStartDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Date de début: " + (startDate?.format(dateFormatter) ?: "--"))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Date de fin: " + (endDate?.format(dateFormatter) ?: "--"), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Durée: 365 jours (automatique)", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ℹ️ Comment ça marche ?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "1. Renseigne tes prix par catégorie :\n" +
                    "   • Gymlib = Dynamo + Circuit Training + Cardio Boxing\n" +
                    "   • Salle = Workout (abonnement gym)\n" +
                    "   • Équipement = Running (chaussures, etc.)\n" +
                    "2. Définis ta date de début (fin = +365 jours)\n" +
                    "3. Pointe tes activités dans 'Suivi' ou 'Calendrier'\n" +
                    "4. L'activité 'Autres' est gratuite (non comptabilisée)\n" +
                    "5. Le coût par séance est calculé par catégorie et globalement",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Export / Import
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("📦 Exporter / Importer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Sauvegarde tes données (séances + config) en JSON pour les restaurer plus tard ou les transférer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                
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
                        Text("📤 Exporter")
                    }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📥 Importer")
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
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
        
        // À propos
        Card(
            modifier = Modifier.fillMaxWidth().clickable { showAboutDialog = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ℹ️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("À propos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text("→", fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDC2626).copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🗑️ Réinitialiser toutes les données", fontWeight = FontWeight.Bold)
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
