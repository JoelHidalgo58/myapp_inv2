package com.example.myapp_inv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import java.text.NumberFormat
import java.util.*

// Color definitions
private val PrimaryColor = Color(0xFF1976D2)
private val BackgroundColor = Color(0xFFF5F5F5)
private val CardBackgroundColor = Color.White
private val TextPrimaryColor = Color(0xFF212121)
private val TextSecondaryColor = Color(0xFF757575)
private val SuccessColor = Color(0xFF43A047)
private val WarningColor = Color(0xFFE53935)
private val SelectedItemColor = Color(0xFFE3F2FD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentasScreen(
    userName: String,
    productos: List<Producto>,
    usuarios: List<Usuario>,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onVentaRealizada: (List<Pair<Producto, Int>>, Usuario) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCliente by remember { mutableStateOf<Usuario?>(null) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedProductos by remember { mutableStateOf<List<Pair<Producto, String>>>(emptyList()) }
    var searchMode by remember { mutableStateOf("cliente") }

    // Filtrar solo los clientes (no administradores)
    val clientes = usuarios.filter { it.rol in listOf("Regular", "VIP", "Mayorista") }
    
    // Filtrar clientes según la búsqueda
    val clientesFiltrados = remember(searchQuery, clientes) {
        if (searchQuery.isEmpty()) {
            clientes
        } else {
            clientes.filter { cliente ->
                cliente.nombre.contains(searchQuery, ignoreCase = true) ||
                cliente.cedula.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Filtrar productos según la búsqueda
    val productosFiltrados = remember(searchQuery, productos) {
        if (searchQuery.isEmpty()) {
            productos.filter { it.cantidad > 0 }
        } else {
            productos.filter { producto ->
                (producto.nombre.contains(searchQuery, ignoreCase = true) ||
                producto.id.toString().contains(searchQuery)) &&
                producto.cantidad > 0
            }
        }
    }

    // Función para validar la venta
    fun validarVenta(): Boolean {
        if (selectedCliente == null) {
            showError = true
            errorMessage = "Por favor seleccione un cliente"
            return false
        }
        if (selectedProductos.isEmpty()) {
            showError = true
            errorMessage = "Por favor seleccione al menos un producto"
            return false
        }
        
        // Validar cada producto seleccionado
        for ((producto, cantidadStr) in selectedProductos) {
            if (cantidadStr.isBlank()) {
                showError = true
                errorMessage = "Por favor ingrese una cantidad válida para ${producto.nombre}"
                return false
            }
            val cantidadInt = cantidadStr.toIntOrNull()
            if (cantidadInt == null || cantidadInt <= 0) {
                showError = true
                errorMessage = "Por favor ingrese una cantidad válida para ${producto.nombre}"
                return false
            }
            if (cantidadInt > producto.cantidad) {
                showError = true
                errorMessage = "Cantidad insuficiente en stock para ${producto.nombre}"
                return false
            }
        }
        return true
    }

    // Función para realizar la venta
    fun realizarVenta() {
        val productosConCantidad = selectedProductos.map { (producto, cantidadStr) ->
            producto to cantidadStr.toInt()
        }
        if (selectedCliente != null) {
            onVentaRealizada(productosConCantidad, selectedCliente!!)
            showSuccessDialog = true
            showConfirmDialog = false
            // Limpiar la selección
            selectedProductos = emptyList()
            searchQuery = ""
            searchMode = "producto"
        }
    }

    // Función para agregar producto a la lista
    fun agregarProducto(producto: Producto) {
        if (!selectedProductos.any { it.first.id == producto.id }) {
            selectedProductos = selectedProductos + (producto to "")
        }
    }

    // Función para actualizar cantidad de un producto
    fun actualizarCantidad(producto: Producto, cantidad: String) {
        selectedProductos = selectedProductos.map { (p, c) ->
            if (p.id == producto.id) p to cantidad else p to c
        }
    }

    // Función para eliminar producto de la lista
    fun eliminarProducto(producto: Producto) {
        selectedProductos = selectedProductos.filter { it.first.id != producto.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryColor)
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
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nueva Venta",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }
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

        // Contenido principal con scroll
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Paso 1: Selección de Cliente
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = PrimaryColor
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "1",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Seleccionar Cliente",
                                color = PrimaryColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Contenido del paso 1
                        if (selectedCliente == null) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { 
                                    searchQuery = it
                                    showError = false
                                },
                                label = { Text("Buscar por nombre o cédula") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar",
                                        tint = PrimaryColor
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = PrimaryColor,
                                    unfocusedTextColor = PrimaryColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (clientesFiltrados.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier.height(150.dp)
                                ) {
                                    items(clientesFiltrados) { cliente ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable { 
                                                    selectedCliente = cliente
                                                    showError = false
                                                    searchMode = "producto"
                                                    searchQuery = ""
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (cliente == selectedCliente) 
                                                    SelectedItemColor else CardBackgroundColor
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    text = cliente.nombre,
                                                    color = PrimaryColor,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Cédula: ${cliente.cedula}",
                                                    color = TextSecondaryColor,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "No se encontraron clientes",
                                    color = TextSecondaryColor,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        } else {
                            // Mostrar cliente seleccionado
                            selectedCliente?.let { cliente ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = SelectedItemColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Cliente Seleccionado",
                                                color = PrimaryColor,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = cliente.nombre,
                                                color = PrimaryColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                            Text(
                                                text = "Cédula: ${cliente.cedula}",
                                                color = TextSecondaryColor,
                                                fontSize = 14.sp
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                selectedCliente = null
                                                searchQuery = ""
                                                searchMode = "cliente"
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Cambiar cliente",
                                                tint = PrimaryColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Paso 2: Selección de Producto
                if (selectedCliente != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(32.dp),
                                    shape = CircleShape,
                                    color = PrimaryColor
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "2",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Seleccionar Producto",
                                    color = PrimaryColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { 
                                    searchQuery = it
                                    showError = false
                                },
                                label = { Text("Buscar por nombre o número de producto") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar",
                                        tint = PrimaryColor
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = PrimaryColor,
                                    unfocusedTextColor = PrimaryColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (productosFiltrados.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier.height(150.dp)
                                ) {
                                    items(productosFiltrados) { producto ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable { agregarProducto(producto) },
                                            colors = CardDefaults.cardColors(
                                                containerColor = SelectedItemColor
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = producto.nombre,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PrimaryColor
                                                    )
                                                    Text(
                                                        text = "ID: ${producto.id}",
                                                        color = TextSecondaryColor,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = "Stock: ${producto.cantidad}",
                                                        color = if (producto.cantidad < 10) WarningColor else SuccessColor,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                                Text(
                                                    text = "$${String.format("%.2f", producto.precio)}",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SuccessColor
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "No se encontraron productos",
                                    color = TextSecondaryColor,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        }
                    }
                }

                // Lista de productos seleccionados
                if (selectedProductos.isNotEmpty()) {
                    Text(
                        text = "Productos Seleccionados",
                        color = PrimaryColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    selectedProductos.forEach { (producto, cantidad) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = SelectedItemColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = producto.nombre,
                                        color = PrimaryColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", producto.precio)} c/u",
                                        color = TextSecondaryColor,
                                        fontSize = 14.sp
                                    )
                                }
                                
                                OutlinedTextField(
                                    value = cantidad,
                                    onValueChange = { 
                                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                            actualizarCantidad(producto, it)
                                            showError = false
                                        }
                                    },
                                    label = { Text("Cantidad") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(100.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryColor,
                                        unfocusedBorderColor = Color.Gray,
                                        focusedTextColor = PrimaryColor,
                                        unfocusedTextColor = PrimaryColor
                                    )
                                )
                                
                                IconButton(
                                    onClick = { eliminarProducto(producto) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = WarningColor
                                    )
                                }
                            }
                        }
                    }

                    // Mostrar total
                    val total = selectedProductos.sumOf { (producto, cantidadStr) ->
                        val cantidad = cantidadStr.toIntOrNull() ?: 0
                        producto.precio * cantidad
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = SuccessColor.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total",
                                color = PrimaryColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$${String.format("%.2f", total)}",
                                color = SuccessColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (showError) {
                        Text(
                            text = errorMessage,
                            color = WarningColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón de realizar venta
                    Button(
                        onClick = {
                            if (validarVenta()) {
                                showConfirmDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                    ) {
                        Text(
                            text = "Realizar Venta",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { 
                Text(
                    "Confirmar Venta",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Información del cliente
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = SelectedItemColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "DATOS DEL CLIENTE",
                                color = PrimaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nombre: ${selectedCliente?.nombre}",
                                color = TextPrimaryColor
                            )
                            Text(
                                text = "Cédula: ${selectedCliente?.cedula}",
                                color = TextPrimaryColor
                            )
                            Text(
                                text = "Tipo: ${selectedCliente?.rol}",
                                color = TextPrimaryColor
                            )
                        }
                    }

                    // Información de los productos
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = SelectedItemColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "DETALLES DE LA VENTA",
                                color = PrimaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            selectedProductos.forEach { (producto, cantidadStr) ->
                                val cantidad = cantidadStr.toIntOrNull() ?: 0
                                val subtotal = producto.precio * cantidad
                                Text(
                                    text = "${producto.nombre} x $cantidad = $${String.format("%.2f", subtotal)}",
                                    color = TextPrimaryColor
                                )
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = PrimaryColor
                            )
                            
                            val total = selectedProductos.sumOf { (producto, cantidadStr) ->
                                val cantidad = cantidadStr.toIntOrNull() ?: 0
                                producto.precio * cantidad
                            }
                            
                            Text(
                                text = "Total: $${String.format("%.2f", total)}",
                                color = SuccessColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { showConfirmDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = WarningColor)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { realizarVenta() },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                    ) {
                        Text("Confirmar Venta")
                    }
                }
            }
        )
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Venta Exitosa") },
            text = { Text("La venta se ha realizado correctamente") },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
} 