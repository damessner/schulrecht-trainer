package at.schulrecht.trainer.ui.exam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import at.schulrecht.trainer.ui.components.ExplanationCard
import at.schulrecht.trainer.ui.components.OptionLetter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    moduleId: String,
    viewModel: ExamViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prüfung $moduleId · ${formatTime(state.remainingSeconds)}") },
                navigationIcon = {
                    androidx.compose.material3.TextButton(onClick = onBack) {
                        Text("Abbruch")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.loading -> Text("Bereite Prüfung vor …", modifier = Modifier.padding(padding).padding(16.dp))
            state.submitted -> ExamResultView(state = state, onBack = onBack, modifier = Modifier.padding(padding))
            state.current == null -> Text("Keine Fragen.", modifier = Modifier.padding(padding).padding(16.dp))
            else -> ExamQuestionView(state = state, viewModel = viewModel, modifier = Modifier.padding(padding))
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

@Composable
private fun ExamQuestionView(state: ExamUiState, viewModel: ExamViewModel, modifier: Modifier = Modifier) {
    val q = state.current ?: return
    val selected = state.answers[q.id].orEmpty()
    val answeredCount = state.answers.count { it.value.isNotEmpty() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.remainingSeconds <= 60) {
            Text(
                "Wenig Zeit! Noch ${formatTime(state.remainingSeconds)}.",
                color = MaterialTheme.colorScheme.error
            )
        }
        LinearProgressIndicator(
            progress = { (state.index + 1).toFloat() / state.questions.size },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Frage ${state.index + 1} / ${state.questions.size} · $answeredCount beantwortet",
            style = MaterialTheme.typography.bodySmall
        )
        Text(q.situation, style = MaterialTheme.typography.bodyLarge)
        q.optionen.forEachIndexed { i, text ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = i in selected,
                            role = Role.RadioButton,
                            onClick = { viewModel.select(i) }
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (q.typ == "multiple") {
                        Checkbox(checked = i in selected, onCheckedChange = null)
                    } else {
                        RadioButton(selected = i in selected, onClick = null)
                    }
                    OptionLetter(letter = 'A' + i)
                    Text(text, modifier = Modifier.weight(1f))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { viewModel.move(-1) }, enabled = state.index > 0) {
                Text("Zurück")
            }
            if (state.index + 1 < state.questions.size) {
                Button(onClick = { viewModel.move(1)}, modifier = Modifier.weight(1f)) {
                    Text("Weiter")
                }
            } else {
                Button(onClick = { viewModel.submit() }, modifier = Modifier.weight(1f)) {
                    Text("Abgeben")
                }
            }
        }
        OutlinedButton(onClick = { viewModel.submit() }, modifier = Modifier.fillMaxWidth()) {
            Text("Prüfung abgeben ($answeredCount / ${state.questions.size})")
        }
    }
}

@Composable
private fun ExamResultView(state: ExamUiState, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val n = state.questions.size
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (state.passed) "Bestanden." else "Nicht bestanden.",
            style = MaterialTheme.typography.headlineMedium,
            color = if (state.passed) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )
        Text("Punkte: ${"%.1f".format(state.totalScore)} / $n (Grenze 60 %)")
        state.results.forEachIndexed { i, r ->
            ReviewRow(index = i, result = r)
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Zurück zur Übersicht")
        }
    }
}

@Composable
private fun ReviewRow(index: Int, result: ExamResult) {
    var expanded by androidx.compose.runtime.remember(result.question.id) {
        androidx.compose.runtime.mutableStateOf(result.score < 1f)
    }
    Card(
        onClick = { expanded = !expanded },
        colors = if (result.score == 1f) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Frage ${index + 1}: ${"%.0f".format(result.score * 100)} % ${if (expanded) "–" else "+"}")
            if (expanded) {
                ExplanationCard(
                    q = result.question,
                    headline = if (result.selected.isEmpty()) "Nicht beantwortet."
                    else "Deine Wahl: ${result.selected.sorted().joinToString { "${'A' + it}" }}."
                )
            }
        }
    }
}
