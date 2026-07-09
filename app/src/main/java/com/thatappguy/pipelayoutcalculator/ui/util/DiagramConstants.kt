package com.thatappguy.pipelayoutcalculator.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object DiagramConstants {
    // A light grey background to make technical marks pop
    val DiagramBackground = Color(0xFFF0F0F0)
    
    // Technical reference marks (Gridlines, centerlines)
    val MarkColor = Color(0x60000000) // 40% opaque black
    
    // Highlight colors (used for active dimensions)
    val ActivePrimary = Color(0xFFE53935) // Deep Red (Material)
    val ActiveSecondary = Color(0xFF00C853) // Electric Green (ThemeAccent)
    
    // Pipe steel texture base
    val PipeBaseColor = Color(0xFF455A64) 
    
    // Standard visual padding
    val StandardDiagramHeight = 160.dp
}
