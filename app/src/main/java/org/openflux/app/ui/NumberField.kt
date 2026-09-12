package org.openflux.app.ui

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * An OutlinedTextField for an Int-backed value. Binding value straight to
 * `domainInt.toString()` looks fine until someone backspaces the field to
 * clear it before typing a new number: "".toIntOrNull() is null, the old
 * value never changes, and the field immediately redraws with that old
 * value - so it's impossible to ever get it empty. This keeps the actually-
 * typed text as its own state instead, and only reports upward once it
 * parses; the field can sit on "" or "12-" mid-edit without snapping back.
 */
@Composable
fun IntTextField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { new ->
            text = new
            new.toIntOrNull()?.let(onValueChange)
        },
        label = label,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

/** LongTextField is [IntTextField]'s Long counterpart - see its doc comment. */
@Composable
fun LongTextField(
    value: Long,
    onValueChange: (Long) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf(if (value == 0L) "" else value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { new ->
            text = new
            new.toLongOrNull()?.let(onValueChange)
        },
        label = label,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}
