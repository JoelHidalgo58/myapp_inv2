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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialVentasScreen(
    ventas: List<Venta>,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

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
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Historial de Ventas",
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

        // Resumen
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Resumen",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val totalVentas = ventas.sumOf { it.total }
                val cantidadVentas = ventas.size
                
                Text("Total de ventas: $cantidadVentas")
                Text(
                    text = "Monto total: ${formato.format(totalVentas)}",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Lista de ventas
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(ventas.sortedByDescending { it.fecha }) { venta ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = venta.producto.nombre,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cantidad: ${venta.cantidad}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Precio unitario: $${venta.precioUnitario}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Total: $${venta.total}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Cliente: ${venta.cliente.nombre}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Vendedor: ${venta.vendedor}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Fecha: ${dateFormat.format(venta.fecha)}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
} 