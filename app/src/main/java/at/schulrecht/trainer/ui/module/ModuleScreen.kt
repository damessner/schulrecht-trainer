package at.schulrecht.trainer.ui.module

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val LEVEL_TITLES = mapOf(
    "L1" to "L1 · Basis",
    "L2" to "L2 · Handlung",
    "L3" to "L3 · Experte",
    "L4" to "L4 · Transfer"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleScreen(
    moduleId: String,
    viewModel: ModuleViewModel,
    onBack: () -> Unit,
    onStartQuiz: (String, String) -> Unit,
    onStartExam: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var expanded by remember { mutableStateOf<String?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title.ifBlank { "Modul $moduleId" }) },
                navigationIcon = {
                    androidx.compose.material3.TextButton(onClick = onBack) {
                        Text("Zurück")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.ziele.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Lernziele", style = MaterialTheme.typography.titleMedium)
                        state.ziele.forEach { ziel ->
                            Text("• $ziel", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            Button(
                onClick = { onStartExam(moduleId) },
                enabled = state.levels.sumOf { it.total } > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Prüfung: 20 Fragen, 20 Minuten")
            }
            if (state.levels.isEmpty()) {
                Text("Lade Fragen …")
            }
            state.levels.forEach { level ->
                val open = expanded == level.level
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = if (open) null else level.level },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    LEVEL_TITLES[level.level] ?: level.level,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "${level.total} Fragen",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(if (open) "–" else "+", style = MaterialTheme.typography.headlineSmall)
                        }
                        if (open) {
                            Text(
                                LEVEL_DESCRIPTIONS[level.level] ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Button(
                                onClick = { onStartQuiz(moduleId, level.level) },
                                enabled = level.total > 0,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Quiz starten")
                            }
                        }
                    }
                }
            }
        }
    }
}
