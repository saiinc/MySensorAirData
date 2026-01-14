package com.saionji.mysensor.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saionji.mysensor.ui.map.model.SelectedMarkerUi

@Composable
fun MarkerPopup(
    marker: SelectedMarkerUi,
    address: String?,
    isAdded: Boolean,
    isLimitReached: Boolean,
    onClose: () -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(marker.valueType, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(marker.value)

                Spacer(Modifier.height(8.dp))
                Text(
                    text = address ?: "Определяем адрес…",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (isAdded) onRemove() else onAdd()
                    },
                    enabled = isAdded || !isLimitReached,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isAdded) "Удалить из дашборда"
                        else "Добавить в дашборд"
                    )
                }
            }
        }
    }
}