package org.openflux.app.ui.deploy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.openflux.app.R
import org.openflux.app.data.AdminIngestToken

@Composable
fun DeployIngestTab(viewModel: DeployServerDetailViewModel) {
    val tokens by viewModel.ingestTokens.collectAsState()
    var label by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        Card(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.deploy_ingest_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = {
                        viewModel.createIngestToken(label)
                        label = ""
                    },
                    modifier = Modifier.padding(top = 8.dp),
                ) { Text(stringResource(R.string.deploy_ingest_create)) }
            }
        }

        if (tokens.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.deploy_ingest_empty))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(tokens, key = { it.id }) { token ->
                    IngestTokenRow(
                        token = token,
                        onToggleEnabled = { viewModel.setIngestTokenEnabled(token.id, !token.enabled) },
                    )
                }
            }
        }
    }
}

@Composable
private fun IngestTokenRow(token: AdminIngestToken, onToggleEnabled: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(token.label.ifBlank { "(no label)" }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(token.scope)
            }
            OutlinedButton(onClick = onToggleEnabled) {
                Text(stringResource(if (token.enabled) R.string.deploy_keys_disable else R.string.deploy_keys_enable))
            }
        }
    }
}
