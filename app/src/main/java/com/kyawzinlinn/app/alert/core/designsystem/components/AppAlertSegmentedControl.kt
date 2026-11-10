package com.kyawzinlinn.app.alert.core.designsystem.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AppAlertSegmentedControl(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelected(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                modifier = Modifier.defaultMinSize(minHeight = Dimens.minTouchTarget),
            ) {
                Text(label(option))
            }
        }
    }
}
