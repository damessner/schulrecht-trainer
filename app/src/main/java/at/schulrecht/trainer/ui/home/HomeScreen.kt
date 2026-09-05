package at.schulrecht.trainer.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.schulrecht.trainer.data.ModuleUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenModule: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Schulrecht Trainer") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OverallStats(modules = state.modules)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.sync() },
                    enabled = !state.isSyncing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isSyncing) "Lade …" else "Inhalte laden")
                }
                OutlinedButton(
                    onClick = { viewModel.resetProgress() },
                    enabled = !state.isSyncing
                ) {
                    Text("Reset")
                }
            }
            if (state.isSyncing && state.progress.total > 0) {
                LinearProgressIndicator(
                    progress = { state.progress.done.toFloat() / state.progress.total },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${state.progress.done} / ${state.progress.total} Module")
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.modules, key = { it.id }) { module ->
                    ModuleCard(module = module, onClick = { onOpenModule(module.id) })
                }
            }
        }
    }
}

@Composable
private fun OverallStats(modules: List<ModuleUi>) {
    val total = modules.sumOf { it.total }
    val answered = modules.sumOf { it.answered }
    val correct = modules.sumOf { it.correct }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Gesamt: $answered / $total beantwortet", style = MaterialTheme.typography.titleMedium)
            if (answered > 0) {
                Text("Richtig (letzter Versuch): $correct (${correct * 100 / answered} %)")
            } else {
                Text("Noch keine Versuche. Inhalte laden und loslegen.")
            }
        }
    }
}

@Composable
private fun ModuleCard(module: ModuleUi, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
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
