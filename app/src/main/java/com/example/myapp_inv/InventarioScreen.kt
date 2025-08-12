package com.example.myapp_inv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

/**
 * Pantalla de Inventario que muestra y gestiona los productos del sistema.
 * Esta pantalla permite:
 * - Ver la lista de productos disponibles y no disponibles
 * - Buscar productos por nombre
 * - Editar productos existentes
 * - Eliminar productos
 * - Ver alertas de stock bajo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(
    productos: List<Producto>,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    showSuccess: Boolean,
    onSuccessShown: () -> Unit,
    onEditarProducto: (Producto) -> Unit,
    onEliminarConfirmado: (Producto) -> Unit,
    onAgregarProducto: (Producto) -> Unit
) {
    /**
     * Configuración del formato de moneda para mostrar precios
     * Usa el formato de moneda mexicana con dos decimales
     */
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    /**
     * Estado local de la pantalla
     * Controla:
     * - Diálogo de confirmación para eliminar productos
     * - Producto seleccionado para eliminar
     * - Término de búsqueda
     */
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showNuevoProductoDialog by remember { mutableStateOf(false) }
    var nombreProducto by remember { mutableStateOf("") }
    var cantidadProducto by remember { mutableStateOf("") }
    var precioProducto by remember { mutableStateOf("") }

    /**
     * Filtra los productos basándose en el término de búsqueda
     * La búsqueda es insensible a mayúsculas/minúsculas
     */
    val productosFiltrados = productos.filter { producto ->
        producto.nombre.contains(searchQuery, ignoreCase = true)
    }

    /**
     * Layout principal de la pantalla
     * Usa un gradiente de fondo y organiza los elementos en una columna
     */
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),  // Azul muy claro
                            Color(0xFFF5F5F5)   // Gris muy claro
                        )
                    )
                )
                .padding(16.dp)
        ) {
            /**
             * Encabezado de la pantalla
             * Muestra el título y botones de navegación
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color(0xFF1976D2)
                    )
                }
                Text(
                    text = "Inventario",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                IconButton(onClick = onLogout) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Salir",
                        tint = Color(0xFF1976D2)
                    )
                }
            }

            /**
             * Campo de búsqueda
             * Permite filtrar productos por nombre
             */
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Buscar producto...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color(0xFF1976D2)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color(0xFF1976D2),
                    unfocusedTextColor = Color(0xFF1976D2)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            /**
             * Sección de alertas de stock bajo
             * Muestra una tarjeta con los productos que tienen menos de 10 unidades
             */
            val productosStockBajo = productosFiltrados.filter { it.cantidad < 10 }
            if (productosStockBajo.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "¡Alerta de Stock Bajo!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        productosStockBajo.forEach { producto ->
                            Text(
                                text = "• ${producto.nombre}: ${producto.cantidad} unidades",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            /**
             * Lista principal de productos
             * Muestra los productos en dos secciones:
             * 1. Productos disponibles (stock > 0)
             * 2. Productos no disponibles (stock = 0)
             */
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (productosFiltrados.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Producto no encontrado",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFF666666)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No se encontraron productos que coincidan con '$searchQuery'",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF999999),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // Productos disponibles
                    item {
                        Text(
                            text = "Productos Disponibles",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(productosFiltrados.filter { it.cantidad > 0 }) { producto ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (producto.cantidad < 10) 
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                else 
                                    Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = producto.nombre,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF333333)
                                        )
                                        Text(
                                            text = "Stock: ${String.format("%.2f", producto.cantidad.toDouble())}",
                                            color = Color(0xFF666666)
                                        )
                                        Text(
                                            text = "Precio: ${formato.format(producto.precio)}",
                                            color = Color(0xFF666666)
                                        )
                                    }
                                    Row {
                                        IconButton(onClick = { onEditarProducto(producto) }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(onClick = { 
                                            productoAEliminar = producto
                                            showDeleteDialog = true
                                        }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Productos no disponibles
                    val productosNoDisponibles = productosFiltrados.filter { it.cantidad <= 0 }
                    if (productosNoDisponibles.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Productos No Disponibles",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(productosNoDisponibles) { producto ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = producto.nombre,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF666666)
                                            )
                                            Text(
                                                text = "Stock: ${String.format("%.2f", producto.cantidad.toDouble())}",
                                                color = Color(0xFF999999)
                                            )
                                            Text(
                                                text = "Precio: ${formato.format(producto.precio)}",
                                                color = Color(0xFF999999)
                                            )
                                        }
                                        Row {
                                            IconButton(onClick = { onEditarProducto(producto) }) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Editar",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(onClick = { 
                                                productoAEliminar = producto
                                                showDeleteDialog = true
                                            }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Eliminar",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Botón flotante para nuevo producto
        FloatingActionButton(
            onClick = { showNuevoProductoDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF1976D2),
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar Producto",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    /**
     * Diálogo de confirmación para eliminar productos
     * Se muestra cuando el usuario intenta eliminar un producto
     */
    if (showDeleteDialog && productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                productoAEliminar = null
            },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Estás seguro de que deseas eliminar ${productoAEliminar?.nombre}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productoAEliminar?.let { onEliminarConfirmado(it) }
                        showDeleteDialog = false
                        productoAEliminar = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        productoAEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para nuevo producto
    if (showNuevoProductoDialog) {
        AlertDialog(
            onDismissRequest = { showNuevoProductoDialog = false },
            title = { 
                Text(
                    "Nuevo Producto",
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            },
            containerColor = Color.White,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = nombreProducto,
                        onValueChange = { nombreProducto = it },
                        label = { Text("Nombre del Producto", color = Color(0xFF1976D2)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFF1976D2),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color(0xFF1976D2),
                            unfocusedLabelColor = Color(0xFF1976D2)
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cantidadProducto,
                        onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) cantidadProducto = it },
                        label = { Text("Cantidad", color = Color(0xFF1976D2)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFF1976D2),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color(0xFF1976D2),
                            unfocusedLabelColor = Color(0xFF1976D2)
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = precioProducto,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                precioProducto = it
                            }
                        },
                        label = { Text("Precio", color = Color(0xFF1976D2)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFF1976D2),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color(0xFF1976D2),
                            unfocusedLabelColor = Color(0xFF1976D2)
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { showNuevoProductoDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text("Cancelar", color = Color.White)
                    }
                    Button(
                        onClick = {
                            try {
                                val nuevoProducto = Producto(
                                    id = String.format("%04d", productos.size + 1),
                                    nombre = nombreProducto,
                                    cantidad = cantidadProducto.toInt(),
                                    precio = precioProducto.toDouble()
                                )
                                onAgregarProducto(nuevoProducto)
                                nombreProducto = ""
                                cantidadProducto = ""
                                precioProducto = ""
                                showNuevoProductoDialog = false
                            } catch (e: Exception) {
                                // Manejar error de conversión
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        enabled = nombreProducto.isNotBlank() && 
                                cantidadProducto.isNotBlank() && 
                                precioProducto.isNotBlank()
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                }
            }
        )
    }

    /**
     * Efecto para mostrar mensaje de éxito
     * Se ejecuta cuando se completa una operación exitosa
     */
    if (showSuccess) {
        LaunchedEffect(Unit) {
            onSuccessShown()
        }
    }
} 