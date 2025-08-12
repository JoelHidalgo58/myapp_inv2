package com.example.myapp_inv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable

@Composable
fun SalidaScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    userName: String
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
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.Output,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Salida de Inventario",
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
                    imageVector = Icons.Default.ExitToApp,
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
            // Tarjeta de Último Producto Retirado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
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
                    Text(
                        text = "Último Producto Retirado",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1976D2),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Laptop HP - 2 unidades",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF2196F3),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Tarjeta de Salidas por Ventas
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Salidas por Ventas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Lista de salidas por ventas
                    SalidaItem(
                        producto = "Monitor Dell",
                        cantidad = 3,
                        tipo = "Venta",
                        fecha = "11:30 AM"
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    SalidaItem(
                        producto = "Teclado Mecánico",
                        cantidad = 5,
                        tipo = "Venta",
                        fecha = "10:45 AM"
                    )
                }
            }

            // Tarjeta de Movimientos Recientes
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Movimientos Recientes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Lista de movimientos
                    SalidaItem(
                        producto = "Mouse Gaming",
                        cantidad = 2,
                        tipo = "Transferencia",
                        fecha = "11:15 AM"
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    SalidaItem(
                        producto = "Auriculares",
                        cantidad = 4,
                        tipo = "Venta",
                        fecha = "11:00 AM"
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    SalidaItem(
                        producto = "Laptop HP",
                        cantidad = 1,
                        tipo = "Mantenimiento",
                        fecha = "10:30 AM"
                    )
                }
            }
        }
    }
}

@Composable
fun SalidaItem(
    producto: String,
    cantidad: Int,
    tipo: String,
    fecha: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = producto,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF1976D2)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Cantidad: $cantidad",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "•",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = tipo,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        Text(
            text = fecha,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
} 