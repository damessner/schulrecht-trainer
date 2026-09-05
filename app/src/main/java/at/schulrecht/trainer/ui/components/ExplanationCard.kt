package at.schulrecht.trainer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.schulrecht.trainer.data.QuestionUi

@Composable
fun ExplanationCard(
    q: QuestionUi,
    headline: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(headline, style = MaterialTheme.typography.titleMedium)
            Text(q.aufloesung, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Zum Nachlesen: ${q.hauptquelle} (Stand ${q.stand})",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
