package com.kyawzinlinn.app.alert.core.designsystem.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun AppAlertDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String? = null,
    text: String? = null,
    content: (@Composable () -> Unit)? = null,
) {
    val body: (@Composable () -> Unit)? = content ?: text?.let { message ->
        @Composable { Text(message) }
    }
    val dismissButton: (@Composable () -> Unit)? = dismissText?.let { label ->
        @Composable { TextButton(onClick = onDismiss) { Text(label) } }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = body,
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
        dismissButton = dismissButton,
    )
}
