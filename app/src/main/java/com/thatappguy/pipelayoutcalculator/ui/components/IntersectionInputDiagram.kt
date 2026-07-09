package com.thatappguy.pipelayoutcalculator.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.thatappguy.pipelayoutcalculator.ui.util.DiagramConstants

/**
 * A static illustrative guide explaining Centerline Offset Definition.
 */
@Composable
fun IntersectionInputDiagram(modifier: Modifier = Modifier) {
    // Note: Ensure you place 'what_is_offset.png' in your res/drawable/ directory.
    // For now, this is a schematic explaining the visual definition.
    Image(
        painter = painterResource(id = android.R.drawable.stat_sys_warning), // Temporary placeholder
        contentDescription = "Diagram explaining centerline offset between pipes.",
        modifier = modifier
            .fillMaxWidth()
            .height(DiagramConstants.StandardDiagramHeight)
            .padding(bottom = 16.dp),
        contentScale = ContentScale.Inside // Keep schematic dimensions pure
    )
}
