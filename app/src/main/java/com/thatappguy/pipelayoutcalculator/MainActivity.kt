package com.thatappguy.pipelayoutcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

// ==========================================
// 1. ACTIVITY CORE
// ==========================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00E676), // Electric Green
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppLayout()
                }
            }
        }
    }
}

// ==========================================
// 2. MAIN NAVIGATION & LAYOUT CONTAINER
// ==========================================
@Composable
fun MainAppLayout() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Intersection", "Miter Cut", "Rolling Offset")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.statusBarsPadding()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(
                                text = title, 
                                fontSize = 14.sp, 
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal 
                            ) 
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> IntersectionTab()
                1 -> MiterTab()
                2 -> RollingOffsetTab()
            }
        }
    }
}

// ==========================================
// 3. UI TAB 1: UNIVERSAL INTERSECTION SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntersectionTab() {
    val pipeLabels = remember { PipeScheduleRegistry.getAllNpsLabels() }
    
    var headerExpanded by remember { mutableStateOf(false) }
    var selectedHeaderLabel by remember { mutableStateOf("4\"") }

    var branchExpanded by remember { mutableStateOf(false) }
    var selectedBranchLabel by remember { mutableStateOf("3\"") }

    var angleInput by remember { mutableStateOf("90.0") }
    var offsetInput by remember { mutableStateOf("0.0") }
    var resultsText by remember { mutableStateOf("Enter values and tap Calculate.") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Pipe Intersection", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        // Header Picker
        Text("Header Pipe Size (NPS)", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = headerExpanded,
            onExpandedChange = { headerExpanded = !headerExpanded }
        ) {
            OutlinedTextField(
                value = selectedHeaderLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = headerExpanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 12.dp)
            )
            ExposedDropdownMenu(expanded = headerExpanded, onDismissRequest = { headerExpanded = false }) {
                pipeLabels.forEach { label ->
                    DropdownMenuItem(text = { Text(label) }, onClick = { selectedHeaderLabel = label; headerExpanded = false })
                }
            }
        }

        // Branch Picker
        Text("Branch Pipe Size (NPS)", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = branchExpanded,
            onExpandedChange = { branchExpanded = !branchExpanded }
        ) {
            OutlinedTextField(
                value = selectedBranchLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchExpanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 12.dp)
            )
            ExposedDropdownMenu(expanded = branchExpanded, onDismissRequest = { branchExpanded = false }) {
                pipeLabels.forEach { label ->
                    DropdownMenuItem(text = { Text(label) }, onClick = { selectedBranchLabel = label; branchExpanded = false })
                }
            }
        }

        OutlinedTextField(
            value = angleInput,
            onValueChange = { angleInput = it },
            label = { Text("Intersection Angle (degrees)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = offsetInput,
            onValueChange = { offsetInput = it },
            label = { Text("Centerline Offset (inches)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        )

        Button(
            onClick = {
                focusManager.clearFocus()
                val headerOd = PipeScheduleRegistry.getPipeByNps(selectedHeaderLabel)?.outsideDiameter ?: 0.0
                val branchOd = PipeScheduleRegistry.getPipeByNps(selectedBranchLabel)?.outsideDiameter ?: 0.0
                val angle = angleInput.toDoubleOrNull() ?: 90.0
                val offset = offsetInput.toDoubleOrNull() ?: 0.0

                val ordinates = LayoutEngine.calculateUniversalIntersection(headerOd, branchOd, angle, offset, 16)
                if (ordinates.isEmpty()) {
                    resultsText = "Geometric Error: Branch misses the Header entirely."
                } else {
                    val builder = StringBuilder()
                    builder.append("Station | Measurement (Inches)\n")
                    builder.append("-----------------------------\n")
                    ordinates.forEachIndexed { index, value ->
                        val degrees = (360 / 16) * index
                        builder.append(String.format("Stn %2d (%3d°): %.3f\"\n", index + 1, degrees, value))
                    }
                    resultsText = builder.toString()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Calculate Ordinates")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Results (16 Divisions)", style = MaterialTheme.typography.titleMedium, color = Color.White)
            val clipboard = LocalClipboardManager.current
            IconButton(onClick = { clipboard.setText(AnnotatedString(resultsText)) }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = resultsText,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().background(Color(0xFF262626)).padding(12.dp),
            color = Color(0xFF00E676)
        )
    }
}

// ==========================================
// 4. UI TAB 2: MITER CUT SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiterTab() {
    val pipeLabels = remember { PipeScheduleRegistry.getAllNpsLabels() }
    var pipeExpanded by remember { mutableStateOf(false) }
    var selectedPipeLabel by remember { mutableStateOf("4\"") }
    var miterAngleInput by remember { mutableStateOf("45.0") }
    var resultsText by remember { mutableStateOf("Enter values and tap Calculate.") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Miter Cut Layout", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Pipe Size (NPS)", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = pipeExpanded,
            onExpandedChange = { pipeExpanded = !pipeExpanded }
        ) {
            OutlinedTextField(
                value = selectedPipeLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pipeExpanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 12.dp)
            )
            ExposedDropdownMenu(expanded = pipeExpanded, onDismissRequest = { pipeExpanded = false }) {
                pipeLabels.forEach { label ->
                    DropdownMenuItem(text = { Text(label) }, onClick = { selectedPipeLabel = label; pipeExpanded = false })
                }
            }
        }

        OutlinedTextField(
            value = miterAngleInput,
            onValueChange = { miterAngleInput = it },
            label = { Text("Miter Cut Angle (degrees)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        )

        Button(
            onClick = {
                focusManager.clearFocus()
                val pipeOd = PipeScheduleRegistry.getPipeByNps(selectedPipeLabel)?.outsideDiameter ?: 0.0
                val angle = miterAngleInput.toDoubleOrNull() ?: 45.0

                val ordinates = LayoutEngine.calculateMiterCut(pipeOd, angle, 8)
                val builder = StringBuilder()
                builder.append("Station | Measurement (Inches)\n")
                builder.append("-----------------------------\n")
                ordinates.forEachIndexed { index, value ->
                    val degrees = (360 / 8) * index
                    builder.append(String.format("Stn %2d (%3d°): %.3f\"\n", index + 1, degrees, value))
                }
                resultsText = builder.toString()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Calculate Miter")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Results (8 Divisions)", style = MaterialTheme.typography.titleMedium, color = Color.White)
            val clipboard = LocalClipboardManager.current
            IconButton(onClick = { clipboard.setText(AnnotatedString(resultsText)) }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = resultsText,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().background(Color(0xFF262626)).padding(12.dp),
            color = Color(0xFF00E676)
        )
    }
}

// ==========================================
// 5. UI TAB 3: ROLLING OFFSET SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RollingOffsetTab() {
    val pipeLabels = remember { PipeScheduleRegistry.getAllNpsLabels() }
    var pipeExpanded by remember { mutableStateOf(false) }
    var selectedPipeLabel by remember { mutableStateOf("4\"") }

    var riseInput by remember { mutableStateOf("10.0") }
    var rollInput by remember { mutableStateOf("12.0") }
    var fittingAngleInput by remember { mutableStateOf("45.0") }
    var resultsText by remember { mutableStateOf("Enter dimensions to calculate spool cut.") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("3D Rolling Offset", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Pipe Size (Used for Auto-Fitting Takeout)", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = pipeExpanded,
            onExpandedChange = { pipeExpanded = !pipeExpanded }
        ) {
            OutlinedTextField(
                value = selectedPipeLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pipeExpanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 12.dp)
            )
            ExposedDropdownMenu(expanded = pipeExpanded, onDismissRequest = { pipeExpanded = false }) {
                pipeLabels.forEach { label ->
                    DropdownMenuItem(text = { Text(label) }, onClick = { selectedPipeLabel = label; pipeExpanded = false })
                }
            }
        }

        OutlinedTextField(
            value = riseInput,
            onValueChange = { riseInput = it },
            label = { Text("Vertical Rise (inches)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = rollInput,
            onValueChange = { rollInput = it },
            label = { Text("Horizontal Roll (inches)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = fittingAngleInput,
            onValueChange = { fittingAngleInput = it },
            label = { Text("Fitting Angle (e.g. 45 or 90)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        )

        Button(
            onClick = {
                focusManager.clearFocus()
                val rise = riseInput.toDoubleOrNull() ?: 0.0
                val roll = rollInput.toDoubleOrNull() ?: 0.0
                val fAngle = fittingAngleInput.toDoubleOrNull() ?: 45.0
                
                // Extract numeric value from label (handles "1-1/2", "3/4", etc.)
                val npsValue = LayoutEngine.parseNpsLabel(selectedPipeLabel)
                val autoTakeout = if (fAngle == 45.0) npsValue * 0.625 else npsValue * 1.5

                val result = LayoutEngine.calculateRollingOffset(rise, roll, fAngle, autoTakeout)
                
                resultsText = """
                    True Offset:       ${String.format("%.3f", result.trueOffset)}"
                    Center-to-Center:  ${String.format("%.3f", result.travelCenterToCenter)}"
                    Run Advance:       ${String.format("%.3f", result.runAdvance)}"
                    ---------------------------------------
                    Fitting Takeout:   ${String.format("%.3f", autoTakeout)}" (x2)
                    ACTUAL CUT LENGTH: ${String.format("%.3f", result.actualCutLength)}"
                """.trimIndent()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Calculate Offset Spool")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Spool Specifications", style = MaterialTheme.typography.titleMedium, color = Color.White)
            val clipboard = LocalClipboardManager.current
            IconButton(onClick = { clipboard.setText(AnnotatedString(resultsText)) }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = resultsText,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().background(Color(0xFF262626)).padding(12.dp),
            color = Color(0xFF00E676)
        )
    }
}

// ==========================================
// 6. CORE MATHEMATICS & REGISTRY DATA
// ==========================================
data class PipeDimensions(
    val nps: String,
    val outsideDiameter: Double,
    val wallThicknessSch40: Double,
    val wallThicknessSch80: Double,
    val wallThicknessXXH: Double
)

object PipeScheduleRegistry {
    val commonPipes = listOf(
        PipeDimensions("1/2\"", 0.840, 0.109, 0.147, 0.294),
        PipeDimensions("3/4\"", 1.050, 0.113, 0.154, 0.308),
        PipeDimensions("1\"", 1.315, 0.133, 0.179, 0.358),
        PipeDimensions("1-1/2\"", 1.900, 0.145, 0.200, 0.400),
        PipeDimensions("2\"", 2.375, 0.154, 0.218, 0.436),
        PipeDimensions("3\"", 3.500, 0.216, 0.300, 0.600),
        PipeDimensions("4\"", 4.500, 0.237, 0.337, 0.674),
        PipeDimensions("6\"", 6.625, 0.280, 0.432, 0.864),
        PipeDimensions("8\"", 8.625, 0.322, 0.500, 0.875),
        PipeDimensions("12\"", 12.750, 0.406, 0.562, 1.000)
    )

    fun getPipeByNps(nps: String): PipeDimensions? = commonPipes.find { it.nps == nps }
    fun getAllNpsLabels(): List<String> = commonPipes.map { it.nps }
}

object LayoutEngine {
    
    /**
     * Parses a pipe NPS label (e.g. "1-1/2\"", "3/4\"") into a numeric double value.
     */
    fun parseNpsLabel(label: String): Double {
        val clean = label.replace("\"", "").trim()
        return try {
            if (clean.contains("-")) {
                val parts = clean.split("-")
                val whole = parts[0].toDouble()
                val fraction = parts[1].split("/")
                whole + (fraction[0].toDouble() / fraction[1].toDouble())
            } else if (clean.contains("/")) {
                val fraction = clean.split("/")
                fraction[0].toDouble() / fraction[1].toDouble()
            } else {
                clean.toDouble()
            }
        } catch (e: Exception) {
            1.0
        }
    }

    fun calculateUniversalIntersection(
        headerOd: Double, branchOd: Double, intersectionAngle: Double, offset: Double, divisions: Int
    ): List<Double> {
        val R = headerOd / 2.0
        val r = branchOd / 2.0
        val alpha = Math.toRadians(intersectionAngle)
        val ordinates = mutableListOf<Double>()

        if (abs(offset) + r > R) return emptyList()

        val rawLengths = DoubleArray(divisions)
        var minLength = Double.MAX_VALUE

        for (i in 0 until divisions) {
            val theta = Math.toRadians((360.0 / divisions) * i)
            val xb = r * cos(theta)
            val yb = r * sin(theta)
            val xh = xb + offset
            val radical = (R * R) - (xh * xh)
            if (radical < 0) return emptyList()
            
            val zh = sqrt(radical)
            val calculatedLength = (zh - (yb * cos(alpha))) / sin(alpha)
            rawLengths[i] = calculatedLength
            if (calculatedLength < minLength) minLength = calculatedLength
        }

        for (i in 0 until divisions) {
            ordinates.add(rawLengths[i] - minLength)
        }
        return ordinates
    }

    fun calculateMiterCut(pipeOd: Double, miterAngle: Double, divisions: Int): List<Double> {
        val r = pipeOd / 2.0
        val maxCutback = r * tan(Math.toRadians(miterAngle))
        val ordinates = mutableListOf<Double>()

        for (i in 0 until divisions) {
            val theta = Math.toRadians((360.0 / divisions) * i)
            ordinates.add(maxCutback * (1.0 - cos(theta)))
        }
        return ordinates
    }

    fun calculateRollingOffset(rise: Double, roll: Double, fAngle: Double, takeout: Double): OffsetResult {
        val trueOffset = sqrt((rise * rise) + (roll * roll))
        val travel = trueOffset / sin(Math.toRadians(fAngle))
        val run = trueOffset / tan(Math.toRadians(fAngle))
        val cutLength = travel - (2 * takeout)

        return OffsetResult(trueOffset, travel, run, if (cutLength > 0) cutLength else 0.0)
    }

    data class OffsetResult(
        val trueOffset: Double, val travelCenterToCenter: Double, val runAdvance: Double, val actualCutLength: Double
    )
}
