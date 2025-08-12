package com.example.myapp_inv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Div
import com.itextpdf.layout.properties.BorderRadius
import com.itextpdf.layout.element.Text
import com.itextpdf.kernel.colors.ColorConstants
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    userName: String,
    ventas: List<Venta>
) {
    val context = LocalContext.current
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var pdfFilePath by remember { mutableStateOf<String?>(null) }
    var showDateDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAdvancedFilters by remember { mutableStateOf(false) }
    var minAmount by remember { mutableStateOf("") }
    var maxAmount by remember { mutableStateOf("") }

    fun showSnackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    fun validateAmount(amount: String): Boolean {
        return try {
            amount.isEmpty() || amount.toDouble() >= 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun generarReporteVentas(fechaInicio: Date, fechaFin: Date) {
        // Validación de fechas
        val startDate = fechaInicio.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val endDate = fechaFin.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val daysBetween = ChronoUnit.DAYS.between(startDate, endDate)

        if (daysBetween > 365) {
            errorMessage = "El período seleccionado no puede ser mayor a un año"
            showErrorDialog = true
            return
        }

        if (fechaFin.before(fechaInicio)) {
            errorMessage = "La fecha final no puede ser anterior a la fecha inicial"
            showErrorDialog = true
            return
        }

        // Validación de montos
        if (minAmount.isNotEmpty() && maxAmount.isNotEmpty()) {
            if (!validateAmount(minAmount) || !validateAmount(maxAmount)) {
                errorMessage = "Los montos deben ser valores numéricos positivos"
                showErrorDialog = true
                return
            }
            if (minAmount.toDouble() > maxAmount.toDouble()) {
                errorMessage = "El monto mínimo no puede ser mayor al monto máximo"
                showErrorDialog = true
                return
            }
        }

        isLoading = true
        try {
            var ventasFiltradas = ventas.filter { 
                it.fecha in fechaInicio..fechaFin 
            }

            if (ventasFiltradas.isEmpty()) {
                errorMessage = "No hay ventas registradas en el período seleccionado"
                showErrorDialog = true
                return
            }

            // Aplicar filtros de búsqueda
            if (searchQuery.isNotEmpty()) {
                ventasFiltradas = when (selectedFilter) {
                    "Cliente" -> ventasFiltradas.filter { 
                        it.cliente.nombre.contains(searchQuery, ignoreCase = true) 
                    }
                    "Producto" -> ventasFiltradas.filter { 
                        it.producto.nombre.contains(searchQuery, ignoreCase = true) 
                    }
                    else -> ventasFiltradas.filter {
                        it.cliente.nombre.contains(searchQuery, ignoreCase = true) ||
                        it.producto.nombre.contains(searchQuery, ignoreCase = true)
                    }
                }
            }

            // Aplicar filtros de monto
            if (minAmount.isNotEmpty()) {
                ventasFiltradas = ventasFiltradas.filter { it.total >= minAmount.toDouble() }
            }
            if (maxAmount.isNotEmpty()) {
                ventasFiltradas = ventasFiltradas.filter { it.total <= maxAmount.toDouble() }
            }

            if (ventasFiltradas.isEmpty()) {
                errorMessage = "No hay resultados que coincidan con los filtros seleccionados"
                showErrorDialog = true
                return
            }

            // Agrupar ventas por cliente y fecha
            val ventasAgrupadas = ventasFiltradas.groupBy { it.cliente.nombre }
                .mapValues { (_, ventasCliente) ->
                    ventasCliente.groupBy { it.fecha }
                }

            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "Reporte_Ventas_${dateFormat.format(Date())}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            pdfFilePath = file.absolutePath

            PdfWriter(file).use { writer ->
                val pdf = PdfDocument(writer)
                Document(pdf).use { document ->
                    document.setMargins(30f, 30f, 30f, 30f)

                    // Encabezado profesional
                    val headerDiv = Div()
                        .setBackgroundColor(DeviceRgb(41, 128, 185))
                        .setPadding(15f)
                        .setBorderRadius(BorderRadius(8f))
                        .setMarginBottom(15f)

                    // Título centrado
                    val headerTable = Table(UnitValue.createPercentArray(1)).useAllAvailableWidth()
                        .setMarginBottom(10f)

                    val titleText = Text("REPORTE DE VENTAS")
                        .setFontSize(24f)
                        .setBold()
                        .setFontColor(DeviceRgb(255, 255, 255))
                    headerTable.addCell(
                        Cell().add(Paragraph(titleText))
                            .setBorder(null)
                            .setTextAlignment(TextAlignment.CENTER)
                    )

                    headerDiv.add(headerTable)

                    // Información del período
                    val periodoText = Text("Período: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechaInicio)} - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechaFin)}")
                        .setFontSize(12f)
                        .setFontColor(DeviceRgb(255, 255, 255))
                    val periodoInfo = Paragraph(periodoText)
                        .setTextAlignment(TextAlignment.CENTER)
                    headerDiv.add(periodoInfo)

                    document.add(headerDiv)

                    // Resumen ejecutivo
                    val resumenDiv = Div()
                        .setBackgroundColor(DeviceRgb(255, 255, 255))
                        .setPadding(10f)
                        .setMarginBottom(10f)
                        .setBorderRadius(BorderRadius(8f))
                        .setBorder(SolidBorder(DeviceRgb(200, 200, 200), 1f))

                    val resumenTitle = Text("RESUMEN EJECUTIVO")
                        .setFontSize(14f)
                        .setBold()
                        .setFontColor(DeviceRgb(41, 128, 185))
                    resumenDiv.add(Paragraph(resumenTitle).setMarginBottom(5f))

                    // Estadísticas
                    val totalVentas = ventasFiltradas.sumOf { it.total }
                    val numTransacciones = ventasFiltradas.size
                    val numClientes = ventasAgrupadas.size
                    val productosUnicos = ventasFiltradas.map { it.producto }.distinctBy { it.id }.size

                    // Tabla de resumen
                    val resumenTable = Table(UnitValue.createPercentArray(2)).useAllAvailableWidth()
                        .setMarginBottom(5f)
                        .setFontSize(10f)

                    resumenTable.addCell(createContrastCell("Total de Ventas", true))
                    resumenTable.addCell(createContrastCell("$${String.format("%.2f", totalVentas)}"))

                    resumenTable.addCell(createContrastCell("Número de Transacciones", true))
                    resumenTable.addCell(createContrastCell(numTransacciones.toString()))

                    resumenTable.addCell(createContrastCell("Número de Clientes", true))
                    resumenTable.addCell(createContrastCell(numClientes.toString()))

                    resumenTable.addCell(createContrastCell("Productos Únicos Vendidos", true))
                    resumenTable.addCell(createContrastCell(productosUnicos.toString()))

                    resumenDiv.add(resumenTable)
                    document.add(resumenDiv)

                    // Detalle de ventas por cliente
                    ventasAgrupadas.forEach { (nombreCliente, ventasPorFecha) ->
                        val clienteDiv = Div()
                            .setBackgroundColor(DeviceRgb(255, 255, 255))
                            .setPadding(10f)
                            .setMarginBottom(10f)
                            .setBorderRadius(BorderRadius(8f))
                            .setBorder(SolidBorder(DeviceRgb(200, 200, 200), 1f))

                        val clienteTitle = Text("CLIENTE: $nombreCliente")
                            .setFontSize(14f)
                            .setBold()
                            .setFontColor(DeviceRgb(41, 128, 185))
                        clienteDiv.add(Paragraph(clienteTitle).setMarginBottom(5f))

                        // Tabla de ventas por fecha
                        val ventasTable = Table(UnitValue.createPercentArray(3)).useAllAvailableWidth()
                            .setMarginBottom(5f)
                            .setFontSize(9f)

                        // Encabezados
                        val headers = listOf("FECHA", "PRODUCTOS", "TOTAL")
                        headers.forEach { header ->
                            val headerText = Text(header)
                                .setBold()
                                .setFontColor(DeviceRgb(255, 255, 255))
                            ventasTable.addHeaderCell(
                                Cell().add(Paragraph(headerText))
                                    .setBackgroundColor(DeviceRgb(41, 128, 185))
                                    .setTextAlignment(TextAlignment.CENTER)
                                    .setPadding(5f)
                                    .setBorder(SolidBorder(DeviceRgb(200, 200, 200), 0.5f))
                            )
                        }

                        // Datos agrupados por fecha
                        ventasPorFecha.forEach { (fecha, ventasDelDia) ->
                            val productosText = ventasDelDia.joinToString("\n") { venta ->
                                "• ${venta.producto.nombre} (${venta.cantidad} x $${venta.precioUnitario})"
                            }
                            val totalDia = ventasDelDia.sumOf { it.total }

                            ventasTable.addCell(createContrastCell(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fecha)))
                            ventasTable.addCell(createContrastCell(productosText))
                            ventasTable.addCell(createContrastCell("$${String.format("%.2f", totalDia)}"))
                        }

                        clienteDiv.add(ventasTable)
                        document.add(clienteDiv)
                    }

                    // Pie de página
                    val footerDiv = Div()
                        .setBackgroundColor(DeviceRgb(41, 128, 185))
                        .setPadding(8f)
                        .setBorderRadius(BorderRadius(8f))

                    val footerText = """
                        Informe generado automáticamente | Sistema de Gestión de Inventario
                        © 2025 Todos los derechos reservados | Versión 1.0
                    """.trimIndent()

                    val footerTextElement = Text(footerText)
                        .setFontSize(8f)
                        .setFontColor(DeviceRgb(255, 255, 255))
                    val footerParagraph = Paragraph(footerTextElement)
                        .setTextAlignment(TextAlignment.CENTER)
                    footerDiv.add(footerParagraph)

                    document.add(footerDiv)
                }
            }

            showSuccessDialog = true
            showSnackbar("Reporte generado exitosamente")
        } catch (e: Exception) {
            errorMessage = "Error al generar el PDF: ${e.message}"
            showErrorDialog = true
            showSnackbar("Error al generar el reporte")
        } finally {
            isLoading = false
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun mostrarSeleccionFechas() {
        var fechaInicio by remember { mutableStateOf<Date?>(null) }
        var fechaFin by remember { mutableStateOf<Date?>(null) }
        var showDatePickerInicio by remember { mutableStateOf(false) }
        var showDatePickerFin by remember { mutableStateOf(false) }
        var expanded by remember { mutableStateOf(false) }
        var showValidationError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDateDialog = false },
            title = { 
                Text(
                    "Seleccionar Período",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF1976D2)
                ) 
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Filtros de búsqueda
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar por cliente o producto") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar"
                            )
                        },
                        supportingText = {
                            Text(
                                when (selectedFilter) {
                                    "Cliente" -> "Buscar por nombre de cliente"
                                    "Producto" -> "Buscar por nombre de producto"
                                    else -> "Buscar en todos los campos"
                                }
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, "Limpiar búsqueda")
                                }
                            }
                        }
                    )

                    // Selector de filtro
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedFilter,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Filtrar por") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todos") },
                                onClick = { 
                                    selectedFilter = "Todos"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Por Cliente") },
                                onClick = { 
                                    selectedFilter = "Cliente"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Por Producto") },
                                onClick = { 
                                    selectedFilter = "Producto"
                                    expanded = false
                                }
                            )
                        }
                    }

                    // Botón para filtros avanzados
                    TextButton(
                        onClick = { showAdvancedFilters = !showAdvancedFilters },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (showAdvancedFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Mostrar filtros avanzados"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showAdvancedFilters) "Ocultar filtros avanzados" else "Mostrar filtros avanzados")
                    }

                    // Filtros avanzados
                    AnimatedVisibility(
                        visible = showAdvancedFilters,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = minAmount,
                                onValueChange = { 
                                    if (it.isEmpty() || validateAmount(it)) {
                                        minAmount = it
                                    }
                                },
                                label = { Text("Monto mínimo") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                supportingText = {
                                    if (minAmount.isNotEmpty() && !validateAmount(minAmount)) {
                                        Text("Ingrese un monto válido", color = Color.Red)
                                    }
                                }
                            )

                            OutlinedTextField(
                                value = maxAmount,
                                onValueChange = { 
                                    if (it.isEmpty() || validateAmount(it)) {
                                        maxAmount = it
                                    }
                                },
                                label = { Text("Monto máximo") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                supportingText = {
                                    if (maxAmount.isNotEmpty() && !validateAmount(maxAmount)) {
                                        Text("Ingrese un monto válido", color = Color.Red)
                                    }
                                }
                            )
                        }
                    }

                    // Selectores de fecha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fechaInicio?.let { 
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                            } ?: "",
                            onValueChange = { },
                            label = { Text("Fecha Inicio") },
                            readOnly = true,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePickerInicio = true },
                            trailingIcon = {
                                IconButton(onClick = { showDatePickerInicio = true }) {
                                    Icon(Icons.Default.DateRange, "Seleccionar fecha inicio")
                                }
                            },
                            isError = showValidationError && fechaInicio == null,
                            supportingText = {
                                if (showValidationError && fechaInicio == null) {
                                    Text("Seleccione una fecha de inicio")
                                }
                            }
                        )

                        OutlinedTextField(
                            value = fechaFin?.let { 
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                            } ?: "",
                            onValueChange = { },
                            label = { Text("Fecha Fin") },
                            readOnly = true,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePickerFin = true },
                            trailingIcon = {
                                IconButton(onClick = { showDatePickerFin = true }) {
                                    Icon(Icons.Default.DateRange, "Seleccionar fecha fin")
                                }
                            },
                            isError = showValidationError && fechaFin == null,
                            supportingText = {
                                if (showValidationError && fechaFin == null) {
                                    Text("Seleccione una fecha final")
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fechaInicio == null || fechaFin == null) {
                            showValidationError = true
                            return@Button
                        }
                        val fechaInicioValue = fechaInicio
                        val fechaFinValue = fechaFin
                        if (fechaInicioValue == null || fechaFinValue == null) {
                            showValidationError = true
                            return@Button
                        }
                        if (fechaFinValue.before(fechaInicioValue)) {
                            errorMessage = "La fecha final no puede ser anterior a la fecha inicial"
                            showErrorDialog = true
                            return@Button
                        }
                        generarReporteVentas(fechaInicioValue, fechaFinValue)
                        showDateDialog = false
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2),
                        disabledContainerColor = Color(0xFFBDBDBD)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                    Text("Generar Reporte")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDateDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF1976D2)
                    )
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = Color.White
        )

        if (showDatePickerInicio) {
            DatePickerDialog(
                onDismissRequest = { showDatePickerInicio = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePickerInicio = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePickerInicio = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(
                    state = rememberDatePickerState(
                        initialSelectedDateMillis = fechaInicio?.time ?: System.currentTimeMillis()
                    ),
                    title = { Text("Seleccione fecha inicio") },
                    headline = { Text("Fecha Inicio") },
                    showModeToggle = false
                )
            }
        }

        if (showDatePickerFin) {
            DatePickerDialog(
                onDismissRequest = { showDatePickerFin = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePickerFin = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePickerFin = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(
                    state = rememberDatePickerState(
                        initialSelectedDateMillis = fechaFin?.time ?: System.currentTimeMillis()
                    ),
                    title = { Text("Seleccione fecha fin") },
                    headline = { Text("Fecha Fin") },
                    showModeToggle = false
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        // Header azul
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2196F3))
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Reportes",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = userName,
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Salir",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Reporte de Ventas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clickable { showDateDialog = true },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Reporte de Ventas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF1976D2),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Generar reporte de ventas en PDF",
                        color = Color.Gray,
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            // Tarjeta de Bajo Stock
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clickable { /* TODO: Implementar navegación a Reporte de Bajo Stock */ },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Reporte de Bajo Stock",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF1976D2),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ver productos con stock bajo",
                        color = Color.Gray,
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            // Tarjeta de Exportar Datos
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clickable { /* TODO: Implementar navegación a Exportar Datos */ },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Exportar Datos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF1976D2),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Exportar datos del inventario",
                        color = Color.Gray,
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }

    if (showDateDialog) {
        mostrarSeleccionFechas()
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Éxito", color = Color(0xFF1976D2)) },
            containerColor = Color(0xFFE3F2FD),
            text = { 
                Column {
                    Text("El reporte PDF se ha generado correctamente.", color = Color(0xFF424242))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Ubicación: ${pdfFilePath ?: "No disponible"}",
                        color = Color(0xFF424242),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("Aceptar", color = Color.White)
                }
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error", color = Color(0xFFD32F2F)) },
            containerColor = Color(0xFFFBE9E7),
            text = { Text(errorMessage, color = Color(0xFF424242)) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Aceptar", color = Color.White)
                }
            }
        )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Generando reporte...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

private fun createContrastCell(text: String, isHeader: Boolean = false): Cell {
    val backgroundColor = if (isHeader) DeviceRgb(236, 240, 241) else DeviceRgb(255, 255, 255)
    val textColor = if (isHeader) DeviceRgb(41, 128, 185) else DeviceRgb(44, 62, 80)
    val textElement = Text(text).setFontColor(textColor)
    return Cell()
        .add(Paragraph(textElement))
        .setBackgroundColor(backgroundColor)
        .setPadding(8f)
        .setTextAlignment(TextAlignment.CENTER)
        .setBorder(SolidBorder(DeviceRgb(200, 200, 200), 0.5f))
} 