package com.example.mastermartini.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mastermartini.R
import com.example.mastermartini.util.DisplayDateFormatter
import com.example.mastermartini.util.validation.FieldState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Read-only date field that opens a Material 3 date picker. Days for which
 * [isSelectable] is false (e.g. past days and Sundays, decided by Validators in the
 * ViewModel) cannot be picked. The field still shows ✓ / ⚠ from [state].
 *
 * Dismissing the picker without choosing calls [onFocusLost] so the "required" error appears.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    state: FieldState,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onFocusLost: () -> Unit,
    label: String,
    isSelectable: (LocalDate) -> Boolean,
    minYear: Int,
    modifier: Modifier = Modifier,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val focusManager = LocalFocusManager.current
    // Clear focus first: otherwise closing the dialog returns focus to the last text field
    // and the keyboard pops up again
    val openPicker = {
        focusManager.clearFocus()
        showPicker = true
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) openPicker()
        }
    }

    ValidatedTextField(
        state = state,
        onValueChange = {},
        onFocusLost = onFocusLost,
        label = label,
        leadingIcon = Icons.Outlined.Event,
        readOnly = true,
        interactionSource = interactionSource,
        modifier = modifier,
        extraTrailing = {
            IconButton(onClick = openPicker) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = stringResource(R.string.cd_pick_date))
            }
        },
    )

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.toUtcMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = isSelectable(utcTimeMillis.toUtcLocalDate())
                override fun isSelectableYear(year: Int) = year >= minYear
            },
        )
        val close = {
            showPicker = false
            onFocusLost()
        }
        DatePickerDialog(
            onDismissRequest = close,
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onDateSelected(it.toUtcLocalDate()) }
                        close()
                    },
                    enabled = pickerState.selectedDateMillis != null,
                ) { Text(stringResource(R.string.action_accept)) }
            },
            dismissButton = {
                TextButton(onClick = close) { Text(stringResource(R.string.action_cancel)) }
            },
        ) {
            // Custom headline and no keyboard-input mode: the defaults follow the device
            // language, which showed English text ("Selected date") on an English emulator.
            DatePicker(
                state = pickerState,
                title = {
                    Text(
                        stringResource(R.string.date_picker_title),
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                    )
                },
                headline = {
                    Text(
                        text = pickerState.selectedDateMillis?.toUtcLocalDate()?.format(DisplayDateFormatter)
                            ?: stringResource(R.string.date_picker_headline_empty),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                    )
                },
                showModeToggle = false,
            )
        }
    }
}

// The Material date picker works with UTC midnight milliseconds.
private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
