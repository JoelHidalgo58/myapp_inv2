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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoProductoScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onAgregarProducto: (Producto) -> Unit,
    productoParaEditar: Producto?,
    productosExistentes: List<Producto>
) {
    var nombre by remember { mutableStateOf(productoParaEditar?.nombre ?: "") }
    var cantidad by remember { mutableStateOf(productoParaEditar?.cantidad?.toString() ?: "") }
    var precio by remember { mutableStateOf(
        if (productoParaEditar != null) 
            String.format("%.2f", productoParaEditar.precio)
        else 
            ""
    ) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Actualizar campos cuando cambia el producto a editar
    LaunchedEffect(productoParaEditar) {
        nombre = productoParaEditar?.nombre ?: ""
        cantidad = productoParaEditar?.cantidad?.toString() ?: ""
        precio = productoParaEditar?.precio?.let { String.format("%.2f", it) } ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFF6F6F6))
                )
            )
    ) {
        // Header
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
                imageVector = Icons.Default.AddBox,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (productoParaEditar != null) "Editar Producto" else "Nuevo Producto",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Salir",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Formulario
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Producto") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { 
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            cantidad = it
                        }
                    },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = precio,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            precio = it
                        }
                    },
                    label = { Text("Precio") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        when {
                            nombre.isBlank() -> {
                                errorMessage = "Por favor ingrese el nombre del producto"
                                showError = true
                            }
                            cantidad.isBlank() -> {
                                errorMessage = "Por favor ingrese la cantidad"
                                showError = true
                            }
                            precio.isBlank() -> {
                                errorMessage = "Por favor ingrese el precio"
                                showError = true
                            }
                            cantidad.toIntOrNull() == null || cantidad.toInt() <= 0 -> {
                                errorMessage = "La cantidad debe ser un número positivo"
                                showError = true
                            }
                            precio.toDoubleOrNull() == null || precio.toDouble() <= 0 -> {
                                errorMessage = "El precio debe ser un número positivo"
                                showError = true
                            }
                            productoParaEditar == null && productosExistentes.any { 
                                it.nombre.equals(nombre.trim(), ignoreCase = true) 
                            } -> {
                                errorMessage = "Ya existe un producto con ese nombre"
                                showError = true
                            }
                            else -> {
                                val nuevoProducto = Producto(
                                    id = if (productoParaEditar != null) 
                                        productoParaEditar.id 
                                    else 
                                        String.format("%04d", productosExistentes.size + 1),
                                    nombre = nombre.trim(),
                                    cantidad = cantidad.toInt(),
                                    precio = String.format("%.2f", precio.toDouble()).toDouble()
                                )
                                onAgregarProducto(nuevoProducto)
                                showSuccessDialog = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (productoParaEditar == null) "Agregar Producto" else "Actualizar Producto")
                }
            }
        }
    }

    // Diálogo de error
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { showError = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onBack()
            },
            title = { Text("Éxito") },
            text = { 
                Text(
                    if (productoParaEditar == null) 
                        "Producto agregado exitosamente" 
                    else 
                        "Producto actualizado exitosamente"
                )
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showSuccessDialog = false
                        onBack()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
} 