package com.example.myapp_inv

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportarScreen(
    userName: String = "admin",
    ventasCount: Int = 20,
    clientesCount: Int = 3,
    productosCount: Int = 6,
    ventas: List<Venta> = emptyList(),
    productos: List<Producto> = emptyList(),
    usuarios: List<Usuario> = emptyList(),
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var showSuccessDialog by remember { mutableStateOf(false) }
    var pdfFilePath by remember { mutableStateOf("") }
    var showSuccessDialogInventario by remember { mutableStateOf(false) }
    var pdfFilePathInventario by remember { mutableStateOf("") }
    var showSuccessDialogClientes by remember { mutableStateOf(false) }
    var pdfFilePathClientes by remember { mutableStateOf("") }
    var showDialogSeleccionCliente by remember { mutableStateOf(false) }
    var clienteSeleccionadoDialog by remember { mutableStateOf<Usuario?>(null) }
    var showDialogComprasCliente by remember { mutableStateOf(false) }
    var clienteParaCompras by remember { mutableStateOf<Usuario?>(null) }
    var showSuccessDialogFactura by remember { mutableStateOf(false) }
    var pdfFilePathFactura by remember { mutableStateOf("") }

    // Nuevas variables para el diálogo de fechas
    var showDialogFechasVentas by remember { mutableStateOf(false) }
    var fechaInicioVentas by remember { mutableStateOf<Date?>(null) }
    var fechaFinVentas by remember { mutableStateOf<Date?>(null) }

    // Filtrar solo clientes (excluir administradores)
    val clientesReales = usuarios.filter { it.rol != "Administrador" }.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF4A90E2), Color(0xFF357ABD))
                    )
                )
                .padding(top = 24.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Exportar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                    )
                    Text(
                        text = "Genera informes",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(top = 0.dp)
                    )
                }
                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Cerrar sesión",
                        tint = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Resumen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$ventasCount", color = Color(0xFF4A90E2), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Ventas", color = Color(0xFF666666), fontSize = 16.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$clientesReales", color = Color(0xFF4CAF50), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Clientes", color = Color(0xFF666666), fontSize = 16.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$productosCount", color = Color(0xFFFFB74D), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Productos", color = Color(0xFF666666), fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Tarjetas de reportes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f).clickable {
                        showDialogFechasVentas = true
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFF4A90E2),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Reporte de Ventas", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF357ABD))
                        Text("$ventasCount ventas registradas", color = Color(0xFF666666), fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Card(
                    modifier = Modifier.weight(1f).clickable {
                        val file = generarReporteInventarioPDF(context, productos)
                        if (file != null) {
                            pdfFilePathInventario = file.absolutePath
                            showSuccessDialogInventario = true
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Reporte de Inventario", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFFE65100))
                        Text("$productosCount productos", color = Color(0xFF666666), fontSize = 16.sp)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f).clickable {
                        val file = generarReporteClientesPDF(context, usuarios)
                        if (file != null) {
                            pdfFilePathClientes = file.absolutePath
                            showSuccessDialogClientes = true
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Reporte de Clientes", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF2E7D32))
                        Text("$clientesReales clientes registrados", color = Color(0xFF666666), fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Card(
                    modifier = Modifier.weight(1f).clickable {
                        showDialogSeleccionCliente = true
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Generar Factura", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFFC62828))
                        Text("Factura por cliente", color = Color(0xFF666666), fontSize = 16.sp)
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("PDF generado") },
            text = { Text("El reporte de ventas se generó correctamente. ¿Deseas abrirlo o compartirlo?") },
            confirmButton = {
                TextButton(onClick = {
                    abrirPDF(context, pdfFilePath)
                    showSuccessDialog = false
                }) { Text("Abrir PDF") }
            },
            dismissButton = {
                TextButton(onClick = {
                    compartirPDF(context, pdfFilePath)
                    showSuccessDialog = false
                }) { Text("Compartir PDF") }
            }
        )
    }

    if (showSuccessDialogInventario) {
        AlertDialog(
            onDismissRequest = { showSuccessDialogInventario = false },
            title = { Text("PDF generado") },
            text = { Text("El reporte de inventario se generó correctamente. ¿Deseas abrirlo o compartirlo?") },
            confirmButton = {
                TextButton(onClick = {
                    abrirPDF(context, pdfFilePathInventario)
                    showSuccessDialogInventario = false
                }) { Text("Abrir PDF") }
            },
            dismissButton = {
                TextButton(onClick = {
                    compartirPDF(context, pdfFilePathInventario)
                    showSuccessDialogInventario = false
                }) { Text("Compartir PDF") }
            }
        )
    }

    if (showSuccessDialogClientes) {
        AlertDialog(
            onDismissRequest = { showSuccessDialogClientes = false },
            title = { Text("PDF generado") },
            text = { Text("El reporte de clientes se generó correctamente. ¿Deseas abrirlo o compartirlo?") },
            confirmButton = {
                TextButton(onClick = {
                    abrirPDF(context, pdfFilePathClientes)
                    showSuccessDialogClientes = false
                }) { Text("Abrir PDF") }
            },
            dismissButton = {
                TextButton(onClick = {
                    compartirPDF(context, pdfFilePathClientes)
                    showSuccessDialogClientes = false
                }) { Text("Compartir PDF") }
            }
        )
    }

    if (showDialogSeleccionCliente) {
        AlertDialog(
            onDismissRequest = { showDialogSeleccionCliente = false },
            title = { 
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        "Seleccionar Cliente",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF7E57C2),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Seleccione un cliente para generar su factura",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF546E7A)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    usuarios.filter { it.rol != "Administrador" }.forEach { usuario ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    clienteSeleccionadoDialog = usuario
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (clienteSeleccionadoDialog == usuario) 
                                    Color(0xFFEDE7F6) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (clienteSeleccionadoDialog == usuario) 8.dp else 2.dp
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = clienteSeleccionadoDialog == usuario,
                                    onClick = { clienteSeleccionadoDialog = usuario },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF7E57C2),
                                        unselectedColor = Color(0xFF546E7A)
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        usuario.nombre,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color(0xFF7E57C2),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        usuario.username,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color(0xFF546E7A)
                                    )
                                }
                                if (clienteSeleccionadoDialog == usuario) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = Color(0xFF7E57C2),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { showDialogSeleccionCliente = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF546E7A)
                        )
                    ) {
                        Text(
                            "Cancelar",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            showDialogSeleccionCliente = false
                            clienteParaCompras = clienteSeleccionadoDialog
                            showDialogComprasCliente = true
                        },
                        enabled = clienteSeleccionadoDialog != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7E57C2),
                            disabledContainerColor = Color(0xFFB0BEC5)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            "Seleccionar",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            containerColor = Color.White,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDialogComprasCliente && clienteParaCompras != null) {
        val comprasCliente = ventas.filter { it.cliente.username == clienteParaCompras!!.username }
            .sortedByDescending { it.fecha }
        
        // Agrupar compras por mes y año
        val comprasPorMes = comprasCliente.groupBy { 
            SimpleDateFormat("MMMM yyyy", Locale("es")).format(it.fecha)
        }.toSortedMap(compareByDescending { 
            SimpleDateFormat("MMMM yyyy", Locale("es")).parse(it) 
        })
        
        val totalCompras = comprasCliente.sumOf { it.total }
        val cantidadCompras = comprasCliente.size
        val promedioCompra = if (cantidadCompras > 0) totalCompras / cantidadCompras else 0.0

        AlertDialog(
            onDismissRequest = { showDialogComprasCliente = false },
            title = { 
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        "Compras de ${clienteParaCompras!!.nombre}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF7E57C2),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Seleccione una fecha para generar la factura",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF546E7A)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Resumen de compras
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFEDE7F6)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Resumen de Compras",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF7E57C2),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "Total Compras",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF546E7A)
                                    )
                                    Text(
                                        "$${"%.2f".format(totalCompras)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF7E57C2),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text(
                                        "Cantidad",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF546E7A)
                                    )
                                    Text(
                                        cantidadCompras.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF7E57C2),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text(
                                        "Promedio",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF546E7A)
                                    )
                                    Text(
                                        "$${"%.2f".format(promedioCompra)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF7E57C2),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (comprasCliente.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = Color(0xFF546E7A),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Este cliente no tiene compras registradas",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF546E7A)
                                )
                            }
                        }
                    } else {
                        Text(
                            "Historial de Compras",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF7E57C2),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        comprasPorMes.forEach { (mesStr, ventasMes) ->
                            // Agrupar ventas por día dentro del mes
                            val ventasPorDia = ventasMes.groupBy { 
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.fecha)
                            }.toSortedMap(compareByDescending { 
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it) 
                            })
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFEDE7F6)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            mesStr.capitalize(),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color(0xFF7E57C2),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = Color(0xFF7E57C2),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    ventasPorDia.forEach { (fechaStr, ventasFecha) ->
                                        // Agrupar ventas por producto
                                        val ventasAgrupadas = ventasFecha.groupBy { it.producto }
                                            .map { (producto, ventas) ->
                                                val cantidadTotal = ventas.sumOf { it.cantidad }
                                                val total = ventas.sumOf { it.total }
                                                val precioUnitario = if (cantidadTotal > 0) total / cantidadTotal else 0.0
                                                Triple(producto, cantidadTotal, total)
                                            }
                                            .sortedBy { it.first.nombre }

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable {
                                                    val file = generarFacturaPDF(context, clienteParaCompras!!, ventasFecha, fechaStr)
                                                    if (file != null) {
                                                        pdfFilePathFactura = file.absolutePath
                                                        showSuccessDialogFactura = true
                                                    }
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color.White
                                            ),
                                            elevation = CardDefaults.cardElevation(
                                                defaultElevation = 2.dp
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp)
                                            ) {
                                                Text(
                                                    fechaStr,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color(0xFF7E57C2),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                ventasAgrupadas.forEach { (producto, cantidad, total) ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            "${producto.nombre} x$cantidad",
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = Color(0xFF546E7A)
                                                        )
                                                        Text(
                                                            "$${"%.2f".format(total)}",
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = Color(0xFF7E57C2),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Divider(
                                                    color = Color(0xFFEDE7F6),
                                                    thickness = 1.dp
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        "Total del día",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = Color(0xFF546E7A)
                                                    )
                                                    Text(
                                                        "$${"%.2f".format(ventasFecha.sumOf { it.total })}",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = Color(0xFF7E57C2),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(
                                        color = Color(0xFF7E57C2),
                                        thickness = 1.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Total del mes",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFF546E7A)
                                        )
                                        Text(
                                            "$${"%.2f".format(ventasMes.sumOf { it.total })}",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color(0xFF7E57C2),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDialogComprasCliente = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7E57C2)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Cerrar",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = Color.White,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showSuccessDialogFactura) {
        AlertDialog(
            onDismissRequest = { showSuccessDialogFactura = false },
            title = { Text("Factura generada") },
            text = { Text("La factura se generó correctamente. ¿Deseas abrirla o compartirla?") },
            confirmButton = {
                TextButton(onClick = {
                    abrirPDF(context, pdfFilePathFactura)
                    showSuccessDialogFactura = false
                }) { Text("Abrir PDF") }
            },
            dismissButton = {
                TextButton(onClick = {
                    compartirPDF(context, pdfFilePathFactura)
                    showSuccessDialogFactura = false
                }) { Text("Compartir PDF") }
            }
        )
    }

    // Agregar el diálogo de selección de fechas
    if (showDialogFechasVentas) {
        AlertDialog(
            onDismissRequest = { showDialogFechasVentas = false },
            title = { Text("Seleccionar Rango de Fechas") },
            text = {
                Column {
                    // Selector de fecha inicial
                    Text("Fecha Inicio:", fontWeight = FontWeight.Bold)
                    DatePicker(
                        selectedDate = fechaInicioVentas,
                        onDateSelected = { fechaInicioVentas = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Selector de fecha final
                    Text("Fecha Fin:", fontWeight = FontWeight.Bold)
                    DatePicker(
                        selectedDate = fechaFinVentas,
                        onDateSelected = { fechaFinVentas = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (fechaInicioVentas != null && fechaFinVentas != null) {
                            // Filtrar ventas por rango de fechas
                            val ventasFiltradas = ventas.filter { venta ->
                                venta.fecha in fechaInicioVentas!!..fechaFinVentas!!
                            }
                            val file = generarReporteVentasPDF(context, ventasFiltradas)
                            if (file != null) {
                                pdfFilePath = file.absolutePath
                                showSuccessDialog = true
                            }
                            showDialogFechasVentas = false
                        }
                    },
                    enabled = fechaInicioVentas != null && fechaFinVentas != null
                ) {
                    Text("Generar Reporte")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogFechasVentas = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DatePicker(
    selectedDate: Date?,
    onDateSelected: (Date) -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val calendar = Calendar.getInstance()
                selectedDate?.let { calendar.time = it }
                
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = android.app.DatePickerDialog(
                    context,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        calendar.set(selectedYear, selectedMonth, selectedDay)
                        onDateSelected(calendar.time)
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.show()
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedDate?.let { dateFormatter.format(it) } ?: "Seleccionar fecha",
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = "Seleccionar fecha"
        )
    }
}

fun generarReporteVentasPDF(context: Context, ventas: List<Venta>): File? {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 1200, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Configuración de estilos mejorados
        val titlePaint = Paint().apply {
            textSize = 28f
            color = AndroidColor.rgb(25, 118, 210) // Azul corporativo más oscuro
            isFakeBoldText = true
        }
        val subTitlePaint = Paint().apply {
            textSize = 16f
            color = AndroidColor.rgb(66, 66, 66) // Gris oscuro
        }
        val tableHeaderPaint = Paint().apply {
            textSize = 14f
            color = AndroidColor.WHITE
            isFakeBoldText = true
        }
        val tableCellPaint = Paint().apply {
            textSize = 12f
            color = AndroidColor.rgb(33, 33, 33)
        }
        val tableHeaderBgPaint = Paint().apply {
            color = AndroidColor.rgb(25, 118, 210) // Azul corporativo
        }
        val linePaint = Paint().apply {
            color = AndroidColor.rgb(224, 224, 224)
            strokeWidth = 0.5f
        }
        val highlightPaint = Paint().apply {
            color = AndroidColor.rgb(237, 247, 255) // Azul muy claro
        }
        val accentPaint = Paint().apply {
            color = AndroidColor.rgb(25, 118, 210) // Azul corporativo
            strokeWidth = 2f
        }
        val dateHeaderPaint = Paint().apply {
            textSize = 14f
            color = AndroidColor.rgb(25, 118, 210)
            isFakeBoldText = true
        }

        // Obtener fechas del reporte
        val fechaInicio = ventas.minOfOrNull { it.fecha }
        val fechaFin = ventas.maxOfOrNull { it.fecha }
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val rangoFechas = if (fechaInicio != null && fechaFin != null) {
            "Período: ${dateFormatter.format(fechaInicio)} - ${dateFormatter.format(fechaFin)}"
        } else {
            "Período: No especificado"
        }

        // Encabezado mejorado
        // Línea decorativa superior
        canvas.drawLine(35f, 35f, 560f, 35f, accentPaint)
        
        // Logo o nombre de la empresa
        canvas.drawText("EMPRESA EJEMPLO S.A.", 40f, 70f, titlePaint)
        
        // Información de la empresa
        val empresaInfo = listOf(
            "Dirección: Av. Principal 123, Ciudad",
            "Teléfono: (123) 456-7890",
            "Email: ventas@empresa.com",
            "RUC: 20123456789"
        )
        var yOffset = 100f
        empresaInfo.forEach { info ->
            canvas.drawText(info, 40f, yOffset, subTitlePaint)
            yOffset += 20f
        }

        // Información del reporte
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val horaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        canvas.drawText("Reporte de Ventas", 40f, yOffset + 20f, titlePaint)
        canvas.drawText(rangoFechas, 40f, yOffset + 45f, subTitlePaint)
        canvas.drawText("Generado el: $fechaActual", 400f, yOffset + 20f, subTitlePaint)
        canvas.drawText("Hora: $horaActual", 400f, yOffset + 40f, subTitlePaint)

        // Resumen de ventas
        val totalVentas = ventas.sumOf { it.total }
        val promedioVentas = if (ventas.isNotEmpty()) totalVentas / ventas.size else 0.0
        val startY = yOffset + 80f

        // Caja de resumen
        canvas.drawRect(35f, startY, 560f, startY + 80f, highlightPaint)
        canvas.drawRect(35f, startY, 560f, startY + 80f, accentPaint)
        
        paint.textSize = 14f
        paint.color = AndroidColor.rgb(25, 118, 210)
        paint.isFakeBoldText = true
        canvas.drawText("RESUMEN DE VENTAS", 50f, startY + 25f, paint)
        
        paint.textSize = 12f
        paint.color = AndroidColor.rgb(66, 66, 66)
        paint.isFakeBoldText = false
        canvas.drawText("Total de ventas: ${ventas.size}", 50f, startY + 45f, paint)
        canvas.drawText("Total general: $${"%.2f".format(totalVentas)}", 50f, startY + 65f, paint)
        canvas.drawText("Promedio por venta: $${"%.2f".format(promedioVentas)}", 300f, startY + 45f, paint)

        // Agrupar ventas por fecha y filtrar fechas sin ventas
        val ventasPorFecha = ventas
            .groupBy { dateFormatter.format(it.fecha) }
            .filter { it.value.isNotEmpty() } // Solo fechas con ventas
            .toSortedMap(compareBy { dateFormatter.parse(it) })

        // Si no hay ventas en el período, mostrar mensaje
        if (ventasPorFecha.isEmpty()) {
            paint.textSize = 16f
            paint.color = AndroidColor.rgb(66, 66, 66)
            paint.isFakeBoldText = true
            canvas.drawText("No hay ventas registradas en el período seleccionado", 40f, startY + 100f, paint)
        } else {
            // Tabla de ventas
            var currentY = startY + 100f
            val colX = listOf(40f, 200f, 300f, 380f, 460f)
            val headers = listOf("Producto", "Cantidad", "P.Unitario", "Total", "Cliente")

            ventasPorFecha.forEach { (fecha, ventasDelDia) ->
                // Agrupar ventas por cliente y producto
                val ventasAgrupadas = ventasDelDia
                    .groupBy { Pair(it.cliente, it.producto) }
                    .map { (clienteProducto, ventas) ->
                        val cantidadTotal = ventas.sumOf { it.cantidad }
                        val total = ventas.sumOf { it.total }
                        val precioUnitario = if (cantidadTotal > 0) total / cantidadTotal else 0.0
                        Triple(clienteProducto.first, clienteProducto.second, Venta(
                            cliente = clienteProducto.first,
                            producto = clienteProducto.second,
                            cantidad = cantidadTotal,
                            precioUnitario = precioUnitario,
                            total = total,
                            fecha = ventas.first().fecha,
                            vendedor = ventas.first().vendedor
                        ))
                    }
                    .sortedBy { it.first.nombre } // Ordenar por nombre de cliente

                // Encabezado de fecha
                canvas.drawRect(35f, currentY, 560f, currentY + 25f, highlightPaint)
                canvas.drawText("Fecha: $fecha", 50f, currentY + 17f, dateHeaderPaint)
                currentY += 35f

                // Header de tabla
                canvas.drawRect(35f, currentY, 570f, currentY + 30f, tableHeaderBgPaint)
                for (i in headers.indices) {
                    canvas.drawText(headers[i], colX[i], currentY + 20f, tableHeaderPaint)
                }
                currentY += 40f

                // Filas de ventas agrupadas
                var rowCount = 0
                ventasAgrupadas.forEach { (cliente, producto, venta) ->
                    if (currentY > 1150f) return@forEach
                    
                    if (rowCount % 2 == 0) {
                        canvas.drawRect(35f, currentY - 15f, 570f, currentY + 7f, highlightPaint)
                    }

                    canvas.drawText(producto.nombre, colX[0], currentY, tableCellPaint)
                    canvas.drawText(venta.cantidad.toString(), colX[1], currentY, tableCellPaint)
                    canvas.drawText("$${"%.2f".format(venta.precioUnitario)}", colX[2], currentY, tableCellPaint)
                    canvas.drawText("$${"%.2f".format(venta.total)}", colX[3], currentY, tableCellPaint)
                    canvas.drawText(cliente.nombre, colX[4], currentY, tableCellPaint)
                    
                    currentY += 22f
                    rowCount++
                    
                    canvas.drawLine(35f, currentY, 570f, currentY, linePaint)
                    currentY += 1f
                }

                // Subtotal del día
                val subtotalDia = ventasAgrupadas.sumOf { it.third.total }
                paint.textSize = 12f
                paint.color = AndroidColor.rgb(25, 118, 210)
                paint.isFakeBoldText = true
                canvas.drawText("Subtotal del día: $${"%.2f".format(subtotalDia)}", 400f, currentY + 10f, paint)
                currentY += 30f
            }
        }

        // Pie de página mejorado
        paint.textSize = 10f
        paint.color = AndroidColor.rgb(158, 158, 158)
        paint.isFakeBoldText = false
        
        // Línea decorativa inferior
        canvas.drawLine(35f, 1180f, 560f, 1180f, accentPaint)
        
        canvas.drawText("EMPRESA EJEMPLO S.A. - Reporte generado automáticamente", 40f, 1195f, paint)
        canvas.drawText("Página 1 de 1", 500f, 1195f, paint)

        pdfDocument.finishPage(page)
        
        // Guardar archivo
        val fileName = "Reporte_Ventas_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        return file
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun generarReporteInventarioPDF(context: Context, productos: List<Producto>): File? {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 1200, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Configuración de estilos mejorados
        val titlePaint = Paint().apply {
            textSize = 28f
            color = AndroidColor.rgb(25, 118, 210) // Azul corporativo
            isFakeBoldText = true
        }
        val subTitlePaint = Paint().apply {
            textSize = 16f
            color = AndroidColor.rgb(66, 66, 66)
        }
        val tableHeaderPaint = Paint().apply {
            textSize = 14f
            color = AndroidColor.WHITE
            isFakeBoldText = true
        }
        val tableCellPaint = Paint().apply {
            textSize = 12f
            color = AndroidColor.rgb(33, 33, 33)
        }
        val tableHeaderBgPaint = Paint().apply {
            color = AndroidColor.rgb(25, 118, 210)
        }
        val linePaint = Paint().apply {
            color = AndroidColor.rgb(224, 224, 224)
            strokeWidth = 0.5f
        }
        val highlightPaint = Paint().apply {
            color = AndroidColor.rgb(237, 247, 255)
        }
        val accentPaint = Paint().apply {
            color = AndroidColor.rgb(25, 118, 210)
            strokeWidth = 2f
        }
        val executivePaint = Paint().apply {
            textSize = 14f
            color = AndroidColor.rgb(25, 118, 210)
            isFakeBoldText = true
        }

        // Línea decorativa superior
        canvas.drawLine(35f, 35f, 560f, 35f, accentPaint)
        
        // Encabezado
        canvas.drawText("EMPRESA EJEMPLO S.A.", 40f, 70f, titlePaint)
        
        // Información de la empresa
        val empresaInfo = listOf(
            "Dirección: Av. Principal 123, Ciudad",
            "Teléfono: (123) 456-7890",
            "Email: inventario@empresa.com",
            "RUC: 20123456789"
        )
        var yOffset = 100f
        empresaInfo.forEach { info ->
            canvas.drawText(info, 40f, yOffset, subTitlePaint)
            yOffset += 20f
        }

        // Información del reporte
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val horaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        canvas.drawText("Reporte de Inventario", 40f, yOffset + 20f, titlePaint)
        canvas.drawText("Generado el: $fechaActual", 400f, yOffset + 20f, subTitlePaint)
        canvas.drawText("Hora: $horaActual", 400f, yOffset + 40f, subTitlePaint)

        // Resumen Ejecutivo
        val startY = yOffset + 80f
        // Fondo con gradiente
        val gradientPaint = Paint().apply {
            color = AndroidColor.rgb(240, 247, 255)
        }
        val borderPaint = Paint().apply {
            color = AndroidColor.rgb(25, 118, 210)
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        
        // Dibujar el cuadro con sombra
        canvas.drawRect(35f, startY, 560f, startY + 120f, gradientPaint)
        canvas.drawRect(35f, startY, 560f, startY + 120f, borderPaint)
        
        // Título del resumen
        paint.textSize = 18f
        paint.color = AndroidColor.rgb(25, 118, 210)
        paint.isFakeBoldText = true
        canvas.drawText("RESUMEN EJECUTIVO", 50f, startY + 25f, paint)
        
        // Línea separadora bajo el título
        canvas.drawLine(50f, startY + 35f, 545f, startY + 35f, Paint().apply {
            color = AndroidColor.rgb(25, 118, 210)
            strokeWidth = 1f
        })
        
        // Información del resumen
        paint.textSize = 13f
        paint.color = AndroidColor.rgb(33, 33, 33)
        paint.isFakeBoldText = true
        
        // Columna izquierda
        canvas.drawText("Total de productos:", 50f, startY + 60f, paint)
        canvas.drawText("Valor total inventario:", 50f, startY + 85f, paint)
        canvas.drawText("Precio promedio:", 50f, startY + 110f, paint)
        
        // Valores en azul corporativo
        paint.color = AndroidColor.rgb(25, 118, 210)
        canvas.drawText("${productos.size}", 220f, startY + 60f, paint)
        canvas.drawText("$${"%.2f".format(productos.sumOf { it.precio * it.cantidad })}", 220f, startY + 85f, paint)
        paint.color = AndroidColor.rgb(33, 33, 33)
        canvas.drawText("Productos con stock:", 350f, startY + 60f, paint)
        canvas.drawText("Productos sin stock:", 350f, startY + 85f, paint)
        
        // Valores en azul corporativo
        paint.color = AndroidColor.rgb(25, 118, 210)
        canvas.drawText("${productos.count { it.cantidad > 0 }}", 500f, startY + 60f, paint)
        canvas.drawText("${productos.count { it.cantidad == 0 }}", 500f, startY + 85f, paint)

        // Tabla de inventario
        var currentY = startY + 140f
        val colX = listOf(40f, 90f, 250f, 320f, 450f)
        val headers = listOf("Código", "Nombre", "Cantidad", "P.Unitario", "Valor Total")
        
        // Header de tabla con diseño mejorado
        canvas.drawRect(35f, currentY, 570f, currentY + 35f, tableHeaderBgPaint)
        for (i in headers.indices) {
            canvas.drawText(headers[i], colX[i], currentY + 22f, tableHeaderPaint)
        }
        currentY += 45f

        // Línea separadora después del header
        canvas.drawLine(35f, currentY, 570f, currentY, accentPaint)
        currentY += 5f

        // Filas de productos con diseño mejorado
        var rowCount = 0
        var totalCantidad = 0
        var totalValor = 0.0
        productos.forEach { producto ->
            if (currentY > 1150f) return@forEach
            
            // Fondo alternado para mejor legibilidad
            if (rowCount % 2 == 0) {
                canvas.drawRect(35f, currentY - 15f, 570f, currentY + 7f, highlightPaint)
            }

            // Datos del producto
            canvas.drawText(producto.id, colX[0], currentY, tableCellPaint)
            canvas.drawText(producto.nombre, colX[1], currentY, tableCellPaint)
            canvas.drawText(producto.cantidad.toString(), colX[2], currentY, tableCellPaint)
            canvas.drawText("$${"%.2f".format(producto.precio)}", colX[3], currentY, tableCellPaint)
            
            // Calcular y mostrar valor total por producto
            val valorTotalProducto = producto.precio * producto.cantidad
            canvas.drawText("$${"%.2f".format(valorTotalProducto)}", colX[4], currentY, tableCellPaint)
            
            // Acumular totales
            totalCantidad += producto.cantidad
            totalValor += valorTotalProducto
            
            currentY += 22f
            rowCount++
            
            // Línea separadora más sutil
            canvas.drawLine(35f, currentY, 570f, currentY, linePaint)
            currentY += 1f
        }

        // Totales de la tabla
        currentY += 10f
        paint.textSize = 12f
        paint.color = AndroidColor.rgb(25, 118, 210)
        paint.isFakeBoldText = true
        
        // Línea separadora antes de los totales
        canvas.drawLine(35f, currentY, 570f, currentY, accentPaint)
        currentY += 20f

        // Mostrar totales
        canvas.drawText("TOTALES:", colX[0], currentY, paint)
        canvas.drawText("$totalCantidad", colX[2], currentY, paint)
        canvas.drawText("$${"%.2f".format(totalValor)}", colX[4], currentY, paint)

        // Pie de página mejorado
        currentY = 1180f
        // Línea decorativa inferior
        canvas.drawLine(35f, currentY, 560f, currentY, accentPaint)
        
        // Derechos reservados
        paint.textSize = 10f
        paint.color = AndroidColor.rgb(158, 158, 158)
        paint.isFakeBoldText = false
        canvas.drawText("© ${SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())} EMPRESA EJEMPLO S.A.", 40f, currentY + 15f, paint)
        canvas.drawText("Todos los derechos reservados", 40f, currentY + 30f, paint)
        canvas.drawText("Este documento es confidencial y de uso interno", 40f, currentY + 45f, paint)
        canvas.drawText("Página 1 de 1", 500f, currentY + 15f, paint)

        pdfDocument.finishPage(page)
        
        // Guardar archivo
        val fileName = "Reporte_Inventario_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        return file
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun generarReporteClientesPDF(context: Context, usuarios: List<Usuario>): File? {
    try {
        // Filtrar solo clientes (excluir administradores)
        val clientes = usuarios.filter { it.rol != "Administrador" }
        
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 1200, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Configuración de estilos mejorados
        val titlePaint = Paint().apply {
            textSize = 28f
            color = AndroidColor.rgb(25, 118, 210) // Azul corporativo
            isFakeBoldText = true
        }
        val subTitlePaint = Paint().apply {
            textSize = 16f
            color = AndroidColor.rgb(66, 66, 66)
        }
        val tableHeaderPaint = Paint().apply {
            textSize = 14f
            color = AndroidColor.WHITE
            isFakeBoldText = true
        }
        val tableCellPaint = Paint().apply {
            textSize = 12f
            color = AndroidColor.rgb(33, 33, 33)
        }
        val tableHeaderBgPaint = Paint().apply {
            color = AndroidColor.rgb(25, 118, 210)
        }
        val linePaint = Paint().apply {
            color = AndroidColor.rgb(224, 224, 224)
            strokeWidth = 0.5f
        }
        val highlightPaint = Paint().apply {
            color = AndroidColor.rgb(237, 247, 255)
        }
        val accentPaint = Paint().apply {
            color = AndroidColor.rgb(25, 118, 210)
            strokeWidth = 2f
        }
        val valuePaint = Paint().apply {
            textSize = 12f
            color = AndroidColor.rgb(25, 118, 210)
            isFakeBoldText = true
        }

        // Línea decorativa superior
        canvas.drawLine(35f, 35f, 560f, 35f, accentPaint)
        
        // Encabezado
        canvas.drawText("EMPRESA EJEMPLO S.A.", 40f, 70f, titlePaint)
        
        // Información de la empresa
        val empresaInfo = listOf(
            "Dirección: Av. Principal 123, Ciudad",
            "Teléfono: (123) 456-7890",
            "Email: clientes@empresa.com",
            "RUC: 20123456789"
        )
        var yOffset = 100f
        empresaInfo.forEach { info ->
            canvas.drawText(info, 40f, yOffset, subTitlePaint)
            yOffset += 20f
        }

        // Información del reporte
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val horaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        canvas.drawText("Reporte de Clientes", 40f, yOffset + 20f, titlePaint)
        canvas.drawText("Generado el: $fechaActual", 400f, yOffset + 20f, subTitlePaint)
        canvas.drawText("Hora: $horaActual", 400f, yOffset + 40f, subTitlePaint)

        // Resumen Ejecutivo
        val startY = yOffset + 80f
        // Fondo con gradiente
        val gradientPaint = Paint().apply {
            color = AndroidColor.rgb(240, 247, 255)
        }
        val borderPaint = Paint().apply {
            color = AndroidColor.rgb(25, 118, 210)
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        
        // Dibujar el cuadro con sombra
        canvas.drawRect(35f, startY, 560f, startY + 120f, gradientPaint)
        canvas.drawRect(35f, startY, 560f, startY + 120f, borderPaint)
        
        // Título del resumen
        paint.textSize = 18f
        paint.color = AndroidColor.rgb(25, 118, 210)
        paint.isFakeBoldText = true
        canvas.drawText("RESUMEN EJECUTIVO", 50f, startY + 25f, paint)
        
        // Línea separadora bajo el título
        canvas.drawLine(50f, startY + 35f, 545f, startY + 35f, Paint().apply {
            color = AndroidColor.rgb(25, 118, 210)
            strokeWidth = 1f
        })
        
        // Información del resumen
        paint.textSize = 13f
        paint.color = AndroidColor.rgb(33, 33, 33)
        paint.isFakeBoldText = true
        
        // Columna izquierda
        canvas.drawText("Total de clientes:", 50f, startY + 60f, paint)
        canvas.drawText("Clientes activos:", 50f, startY + 85f, paint)
        canvas.drawText("Último registro:", 50f, startY + 110f, paint)
        
        // Valores en azul corporativo
        paint.color = AndroidColor.rgb(25, 118, 210)
        canvas.drawText("${clientes.size}", 220f, startY + 60f, paint)
        canvas.drawText("${clientes.size}", 220f, startY + 85f, paint)
        paint.color = AndroidColor.rgb(33, 33, 33)
        canvas.drawText(fechaActual, 220f, startY + 110f, paint)
        
        // Columna derecha
        paint.color = AndroidColor.rgb(33, 33, 33)
        canvas.drawText("Estado del sistema:", 350f, startY + 60f, paint)
        canvas.drawText("Versión del reporte:", 350f, startY + 85f, paint)
        paint.color = AndroidColor.rgb(25, 118, 210)
        canvas.drawText("Activo", 500f, startY + 60f, paint)
        canvas.drawText("1.0", 500f, startY + 85f, paint)

        // Tabla de clientes
        var currentY = startY + 140f
        val colX = listOf(40f, 80f, 150f, 250f, 450f)
        val headers = listOf("ID", "Nombre", "Usuario", "Correo", "Teléfono")
        
        // Header de tabla con más espacio
        canvas.drawRect(35f, currentY, 570f, currentY + 40f, tableHeaderBgPaint)
        for (i in headers.indices) {
            canvas.drawText(headers[i], colX[i], currentY + 25f, tableHeaderPaint)
        }
        currentY += 50f

        // Línea separadora después del header
        canvas.drawLine(35f, currentY, 570f, currentY, accentPaint)
        currentY += 5f

        // Filas de clientes con más espacio
        var rowCount = 0
        clientes.forEach { cliente ->
            if (currentY > 1150f) return@forEach
            
            if (rowCount % 2 == 0) {
                canvas.drawRect(35f, currentY - 15f, 570f, currentY + 7f, highlightPaint)
            }

            // ID (más estrecho)
            canvas.drawText((rowCount + 1).toString(), colX[0], currentY, tableCellPaint)
            // Nombre (más estrecho)
            canvas.drawText(cliente.nombre, colX[1], currentY, tableCellPaint)
            // Usuario (más estrecho)
            canvas.drawText(cliente.username, colX[2], currentY, tableCellPaint)
            // Correo (más ancho)
            canvas.drawText(cliente.password ?: "-", colX[3], currentY, tableCellPaint)
            // Teléfono (más ancho)
            canvas.drawText(cliente.username ?: "-", colX[4], currentY, tableCellPaint)
            
            currentY += 25f  // Aumentado el espacio entre filas
            rowCount++
            
            canvas.drawLine(35f, currentY, 570f, currentY, linePaint)
            currentY += 1f
        }

        // Pie de página mejorado
        currentY = 1180f
        // Línea decorativa inferior
        canvas.drawLine(35f, currentY, 560f, currentY, accentPaint)
        
        // Derechos reservados
        paint.textSize = 10f
        paint.color = AndroidColor.rgb(158, 158, 158)
        paint.isFakeBoldText = false
        canvas.drawText("© ${SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())} EMPRESA EJEMPLO S.A.", 40f, currentY + 15f, paint)
        canvas.drawText("Todos los derechos reservados", 40f, currentY + 30f, paint)
        canvas.drawText("Este documento es confidencial y de uso interno", 40f, currentY + 45f, paint)
        canvas.drawText("Página 1 de 1", 500f, currentY + 15f, paint)

        pdfDocument.finishPage(page)
        
        // Guardar archivo
        val fileName = "Reporte_Clientes_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        return file
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun generarFacturaPDF(context: Context, cliente: Usuario, ventas: List<Venta>, fecha: String): File? {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Configuración de estilos mejorados
        val titlePaint = Paint().apply {
            textSize = 28f
            color = AndroidColor.rgb(25, 118, 210) // Azul corporativo
            isFakeBoldText = true
        }
        val subTitlePaint = Paint().apply {
            textSize = 16f
            color = AndroidColor.rgb(66, 66, 66)
        }
        val tableHeaderPaint = Paint().apply {
            textSize = 14f
            color = AndroidColor.WHITE
            isFakeBoldText = true
        }
        val tableCellPaint = Paint().apply {
            textSize = 12f
            color = AndroidColor.rgb(33, 33, 33)
        }
        val tableHeaderBgPaint = Paint().apply {
            color = AndroidColor.rgb(25, 118, 210)
        }
        val linePaint = Paint().apply {
            color = AndroidColor.rgb(224, 224, 224)
            strokeWidth = 0.5f
        }
        val highlightPaint = Paint().apply {
            color = AndroidColor.rgb(237, 247, 255)
        }
        val accentPaint = Paint().apply {
            color = AndroidColor.rgb(25, 118, 210)
            strokeWidth = 2f
        }

        // Línea decorativa superior
        canvas.drawLine(35f, 35f, 560f, 35f, accentPaint)
        
        // Encabezado
        canvas.drawText("EMPRESA EJEMPLO S.A.", 40f, 70f, titlePaint)
        
        // Información de la empresa
        val empresaInfo = listOf(
            "Dirección: Av. Principal 123, Ciudad",
            "Teléfono: (123) 456-7890",
            "Email: ventas@empresa.com",
            "RUC: 20123456789"
        )
        var yOffset = 100f
        empresaInfo.forEach { info ->
            canvas.drawText(info, 40f, yOffset, subTitlePaint)
            yOffset += 20f
        }

        // Información de la factura
        canvas.drawText("FACTURA DE COMPRA", 40f, yOffset + 20f, titlePaint)
        canvas.drawText("Fecha: $fecha", 400f, yOffset + 20f, subTitlePaint)
        canvas.drawText("N° Factura: ${SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())}", 400f, yOffset + 40f, subTitlePaint)

        // Información del cliente
        yOffset += 60f
        canvas.drawRect(35f, yOffset, 560f, yOffset + 80f, highlightPaint)
        canvas.drawRect(35f, yOffset, 560f, yOffset + 80f, accentPaint)
        
        paint.textSize = 14f
        paint.color = AndroidColor.rgb(25, 118, 210)
        paint.isFakeBoldText = true
        canvas.drawText("INFORMACIÓN DEL CLIENTE", 50f, yOffset + 25f, paint)
        
        paint.textSize = 12f
        paint.color = AndroidColor.rgb(33, 33, 33)
        paint.isFakeBoldText = false
        canvas.drawText("Cliente: ${cliente.nombre}", 50f, yOffset + 45f, paint)
        canvas.drawText("Usuario: ${cliente.username}", 50f, yOffset + 65f, paint)
        canvas.drawText("Correo: ${cliente.password ?: "-"}", 300f, yOffset + 45f, paint)
        canvas.drawText("Teléfono: ${cliente.username ?: "-"}", 300f, yOffset + 65f, paint)

        // Tabla de productos
        yOffset += 100f
        val colX = listOf(40f, 220f, 320f, 400f, 480f)
        val headers = listOf("Producto", "Cantidad", "P.Unitario", "Total", "Vendedor")
        
        // Header de tabla
        canvas.drawRect(35f, yOffset, 560f, yOffset + 35f, tableHeaderBgPaint)
        for (i in headers.indices) {
            canvas.drawText(headers[i], colX[i], yOffset + 22f, tableHeaderPaint)
        }
        yOffset += 45f

        // Línea separadora después del header
        canvas.drawLine(35f, yOffset, 560f, yOffset, accentPaint)
        yOffset += 5f

        // Agrupar ventas por producto
        data class VentaAgrupada(
            val producto: Producto,
            val cantidad: Int,
            val total: Double,
            val precioUnitario: Double,
            val vendedor: String
        )

        val ventasAgrupadas = ventas.groupBy { it.producto }
            .map { (producto, ventas) ->
                val cantidadTotal = ventas.sumOf { it.cantidad }
                val total = ventas.sumOf { it.total }
                val precioUnitario = if (cantidadTotal > 0) total / cantidadTotal else 0.0
                val vendedor = ventas.first().vendedor
                VentaAgrupada(producto, cantidadTotal, total, precioUnitario, vendedor)
            }
            .sortedBy { it.producto.nombre }

        // Filas de productos
        var totalGeneral = 0.0
        var rowCount = 0
        ventasAgrupadas.forEach { ventaAgrupada ->
            if (yOffset > 700f) return@forEach // Evitar desbordar la hoja
            
            if (rowCount % 2 == 0) {
                canvas.drawRect(35f, yOffset - 15f, 560f, yOffset + 7f, highlightPaint)
            }

            canvas.drawText(ventaAgrupada.producto.nombre, colX[0], yOffset, tableCellPaint)
            canvas.drawText(ventaAgrupada.cantidad.toString(), colX[1], yOffset, tableCellPaint)
            canvas.drawText("$${"%.2f".format(ventaAgrupada.precioUnitario)}", colX[2], yOffset, tableCellPaint)
            canvas.drawText("$${"%.2f".format(ventaAgrupada.total)}", colX[3], yOffset, tableCellPaint)
            canvas.drawText(ventaAgrupada.vendedor, colX[4], yOffset, tableCellPaint)
            
            yOffset += 22f
            totalGeneral += ventaAgrupada.total
            rowCount++
            
            canvas.drawLine(35f, yOffset, 560f, yOffset, linePaint)
            yOffset += 1f
        }

        // Totales
        yOffset += 20f
        paint.textSize = 14f
        paint.color = AndroidColor.rgb(25, 118, 210)
        paint.isFakeBoldText = true
        
        // Línea separadora antes de los totales
        canvas.drawLine(35f, yOffset, 560f, yOffset, accentPaint)
        yOffset += 20f

        // Subtotal
        canvas.drawText("Subtotal:", 400f, yOffset, paint)
        canvas.drawText("$${"%.2f".format(totalGeneral)}", 480f, yOffset, paint)
        yOffset += 20f

        // IGV (18%)
        val igv = totalGeneral * 0.18
        canvas.drawText("IGV (18%):", 400f, yOffset, paint)
        canvas.drawText("$${"%.2f".format(igv)}", 480f, yOffset, paint)
        yOffset += 20f

        // Total
        paint.textSize = 16f
        canvas.drawText("TOTAL:", 400f, yOffset, paint)
        canvas.drawText("$${"%.2f".format(totalGeneral + igv)}", 480f, yOffset, paint)

        // Pie de página mejorado
        yOffset = 800f
        // Línea decorativa inferior
        canvas.drawLine(35f, yOffset, 560f, yOffset, accentPaint)
        
        // Información adicional
        paint.textSize = 10f
        paint.color = AndroidColor.rgb(158, 158, 158)
        paint.isFakeBoldText = false
        canvas.drawText("© ${SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())} EMPRESA EJEMPLO S.A.", 40f, yOffset + 15f, paint)
        canvas.drawText("Todos los derechos reservados", 40f, yOffset + 30f, paint)
        canvas.drawText("Esta factura es un documento legal", 40f, yOffset + 45f, paint)
        canvas.drawText("Página 1 de 1", 500f, yOffset + 15f, paint)

        pdfDocument.finishPage(page)
        
        // Guardar archivo
        val fileName = "Factura_${cliente.username}_${fecha.replace("/", "-")}_${SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        return file
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun abrirPDF(context: Context, filePath: String) {
    val file = File(filePath)
    val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, "application/pdf")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Abrir PDF"))
}

fun compartirPDF(context: Context, filePath: String) {
    val file = File(filePath)
    val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "application/pdf"
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Compartir PDF"))
}
