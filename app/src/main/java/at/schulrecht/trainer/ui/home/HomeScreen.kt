package at.schulrecht.trainer.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.schulrecht.trainer.data.ModuleUi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenModule: (String) -> Unit,
    onOpenReview: () -> Unit,
    onInstallAppUpdate: (String, String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schulrecht Trainer") },
                actions = {
                    IconButton(onClick = { viewModel.toggleSearch() }) {
                        Icon(Icons.Filled.Search, contentDescription = "Suchen")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            SlimProgress(
                modules = state.modules,
                isSyncing = state.isSyncing,
                done = state.progress.done,
                total = state.progress.total
            )
            GameHeader(game = state.game)
            if (state.dueCount > 0 && !state.isSyncing) {
                Card(
                    onClick = onOpenReview,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Wiederholen fällig: ${state.dueCount}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Deine Schwachpunkte warten (Spaced Repetition).")
                    }
                }
            }
            if (state.modules.isEmpty() && !state.isSyncing) {
                Button(
                    onClick = { viewModel.sync() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Inhalte laden")
                }
            }
            state.appUpdate?.let { update ->
                if (!state.isSyncing) {
                    Button(
                        onClick = { onInstallAppUpdate(update.apkUrl, update.version) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("App-Update ${update.version} – herunterladen & installieren")
                    }
                }
            }
            if (state.updateAvailable && !state.isSyncing) {
                Button(
                    onClick = { viewModel.sync() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Update verfügbar (${state.remoteVersion}) – jetzt laden")
                }
            }
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 4.dp))
            }
            if (state.searchOpen) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { viewModel.setQuery(it) },
                    label = { Text("Modul suchen") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Schließen")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            GameHeader(game = state.game)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                groupedModules(state.modules, state.query).forEach { (saeule, modules) ->
                    stickyHeader {
                        Text(
                            saeuleTitle(saeule),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(modules, key = { it.id }) { module ->
                        ModuleCard(module = module, onClick = { onOpenModule(module.id) })
                    }
                }
                item {
                    TextButton(
                        onClick = { viewModel.resetProgress() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Lernfortschritt zurücksetzen")
                    }
                }
            }
        }
    }
}

@Composable
private fun SlimProgress(modules: List<ModuleUi>, isSyncing: Boolean, done: Int, total: Int) {
    val all = modules.sumOf { it.total }
    val answered = modules.sumOf { it.answered }
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        if (isSyncing && total > 0) {
            LinearProgressIndicator(
                progress = { done.toFloat() / total },
                modifier = Modifier.fillMaxWidth()
            )
            Text("$done / $total Module", style = MaterialTheme.typography.bodySmall)
        } else if (all > 0) {
            LinearProgressIndicator(
                progress = { answered.toFloat() / all },
                modifier = Modifier.fillMaxWidth()
            )
            Text("$answered / $all beantwortet", style = MaterialTheme.typography.bodySmall)
        } else if (!isSyncing) {
            Text("Noch keine Inhalte.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private val SAEULE_ORDER = mapOf(
    "A-SchUG" to 0,
    "B-LDG" to 1,
    "C-TirolSOG" to 2,
    "X-Transfer" to 3
)

private fun saeuleTitle(saeule: String): String = when (saeule) {
    "A-SchUG" -> "Schulunterrichtsgesetz"
    "B-LDG" -> "Lehrer-Dienstrecht"
    "C-TirolSOG" -> "Tirol Organisation"
    "X-Transfer" -> "Transfer & Notfall"
    else -> saeule
}

private fun groupedModules(
    modules: List<ModuleUi>,
    query: String
): List<Pair<String, List<ModuleUi>>> {
    val q = query.trim().lowercase()
    val filtered = if (q.isEmpty()) modules
    else modules.filter { it.id.lowercase().contains(q) || it.titel.lowercase().contains(q) }
    return filtered.groupBy { it.saeule }
        .toList()
        .sortedBy { (saeule, _) -> SAEULE_ORDER[saeule] ?: 99 }
}

@Composable
private fun GameHeader(game: at.schulrecht.trainer.ui.home.GameState) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Level ${game.level} · ${game.xp} XP · Serie ${game.streak} ${if (game.streak == 1) "Tag" else "Tage"}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(if (expanded) "–" else "+", style = MaterialTheme.typography.titleSmall)
            }
            LinearProgressIndicator(
                progress = { game.xpInLevel.toFloat() / game.xpForNext },
                modifier = Modifier.fillMaxWidth()
            )
            if (expanded) {
                Text(
                    "Noch ${game.xpForNext - game.xpInLevel} XP bis Level ${game.level + 1}",
                    style = MaterialTheme.typography.bodySmall
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        at.schulrecht.trainer.domain.ALL_BADGES,
                        key = { it.id }
                    ) { badge ->
                        val earned = badge.id in game.badges
                        AssistChip(
                            onClick = {},
                            label = { Text(if (earned) badge.title else "?") },
                            leadingIcon = { Text(if (earned) "★" else "·") },
                            enabled = earned
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleCard(module: ModuleUi, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            at.schulrecht.trainer.ui.components.OptionLetter(
                letter = module.id.firstOrNull() ?: '•'
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("${module.id} · ${module.titel}", style = MaterialTheme.typography.titleMedium)
                Text(module.saeule, style = MaterialTheme.typography.bodySmall)
                if (module.total > 0) {
                    LinearProgressIndicator(
                        progress = { module.answered.toFloat() / module.total },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    Text("${module.answered} / ${module.total} · ${module.correct} richtig")
                } else {
                    Text("Noch nicht geladen")
                }
            }
        }
    }
}
