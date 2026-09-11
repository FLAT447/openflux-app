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
import org.openflux.app.data.AdminNode

@Composable
fun DeployNodesTab(viewModel: DeployServerDetailViewModel) {
    val nodes by viewModel.nodes.collectAsState()
    var name by remember { mutableStateOf("") }
    var maxKeys by remember { mutableStateOf("500") }

    Column(Modifier.fillMaxSize()) {
        Card(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.deploy_edit_node_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = maxKeys,
                    onValueChange = { maxKeys = it },
                    label = { Text(stringResource(R.string.deploy_edit_node_max_keys)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                Button(
                    onClick = {
                        viewModel.createNode(name, maxKeys.toIntOrNull() ?: 500)
                        name = ""
                    },
                    modifier = Modifier.padding(top = 8.dp),
                ) { Text(stringResource(R.string.deploy_nodes_create)) }
            }
        }

        if (nodes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.deploy_nodes_empty))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(nodes, key = { it.id }) { node -> NodeRow(node, onRotate = { viewModel.rotateNodeToken(node.id) }) }
            }
        }
    }
}

@Composable
private fun NodeRow(node: AdminNode, onRotate: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(node.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${node.status} · max ${node.maxKeys} keys")
            }
            OutlinedButton(onClick = onRotate) {
                Text(stringResource(R.string.deploy_nodes_rotate_token))
            }
        }
    }
}
