package at.schulrecht.trainer.ui.quiz

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import at.schulrecht.trainer.data.QuestionUi
import at.schulrecht.trainer.ui.components.ExplanationCard
import at.schulrecht.trainer.ui.components.OptionLetter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    moduleId: String,
    level: String,
    viewModel: QuizViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$moduleId · $level") },
                navigationIcon = {
                    androidx.compose.material3.TextButton(onClick = onBack) {
                        Text("Ende")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.loading -> Text("Lade …", modifier = Modifier.padding(padding).padding(16.dp))
            state.finished -> SummaryCard(state, onBack, Modifier.padding(padding))
            state.current == null -> Text("Keine Fragen.", modifier = Modifier.padding(padding).padding(16.dp))
            else -> QuestionCard(state = state, viewModel = viewModel, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun SummaryCard(state: QuizUiState, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Fertig!", style = MaterialTheme.typography.headlineMedium)
        val n = state.questions.size
        Text("Punkte: ${"%.1f".format(state.totalScore)} / $n")
        Text("Voll richtig: ${state.fullHits} / $n")
        Button(onClick = onBack) { Text("Zurück zur Übersicht") }
    }
}

@Composable
private fun QuestionCard(state: QuizUiState, viewModel: QuizViewModel, modifier: Modifier = Modifier) {
    val q = state.current ?: return
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LinearProgressIndicator(
            progress = { (state.index + 1).toFloat() / state.questions.size },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Frage ${state.index + 1} / ${state.questions.size} · ${typeLabel(q.typ)}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(q.situation, style = MaterialTheme.typography.bodyLarge)
        q.optionen.forEachIndexed { i, text ->
            OptionRow(
                letter = 'A' + i,
                text = text,
                checked = i in state.selected,
                enabled = !state.revealed,
                single = q.typ != "multiple",
                trailing = if (state.revealed) stateFeedback(q, i) else null,
                onClick = { viewModel.toggle(i) }
            )
        }
        if (!state.revealed) {
            Button(
                onClick = { viewModel.reveal() },
                enabled = state.selected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Antwort prüfen")
            }
        } else {
            ExplanationCard(
                q = q,
                headline = if (state.lastScore == 1f) {
                    "Richtig."
                } else {
                    "Teils richtig (${(state.lastScore * 100).toInt()} %)."
                }
            )
            OutlinedButton(onClick = { viewModel.next() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.index + 1 >= state.questions.size) "Zum Ergebnis" else "Weiter")
            }
        }
    }
}

@Composable
private fun OptionRow(
    letter: Char,
    text: String,
    checked: Boolean,
    enabled: Boolean,
    single: Boolean,
    trailing: String?,
    onClick: () -> Unit
) {
    val container = when (trailing) {
        "ok" -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        "miss" -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        else -> CardDefaults.cardColors()
    }
    Card(colors = container, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = checked, enabled = enabled, role = Role.RadioButton, onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (single) RadioButton(selected = checked, onClick = null, enabled = enabled)
            else Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
            OptionLetter(letter = letter)
            Text(text, modifier = Modifier.weight(1f))
        }
    }
}

private fun stateFeedback(q: QuestionUi, index: Int): String =
    if (index in q.richtig) "ok" else "miss"

private fun typeLabel(typ: String): String = when (typ) {
    "multiple" -> "Mehrere richtig"
    "tf" -> "Richtig / Falsch"
    else -> "Eine richtig"
}
