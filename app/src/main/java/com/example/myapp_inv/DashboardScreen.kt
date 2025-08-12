package com.example.myapp_inv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String,
    userRole: String,
    onLogout: () -> Unit,
    onInventarioClicked: () -> Unit,
    onNuevoProductoClicked: () -> Unit,
    onGestionUsuariosClicked: () -> Unit,
    onVentasClicked: () -> Unit,
    onHistorialVentasClicked: () -> Unit,
    onHistorialCambiosClicked: () -> Unit,
    onAlertasClicked: () -> Unit,
    onExportarClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFF6F6F6))
                )
            )
    ) {
        // Header con gradiente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1976D2), Color(0xFF2196F3))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bienvenido",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Text(
                            text = userName,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Grid de opciones
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DashboardCard(
                    icon = Icons.Default.Inventory,
                    title = "Inventario",
                    subtitle = "Gestionar productos",
                    onClick = onInventarioClicked,
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF1976D2), Color(0xFF2196F3))
                    )
                )
            }
            item {
                DashboardCard(
                    icon = Icons.Default.AddBox,
                    title = "Nuevo Producto",
                    subtitle = "Agregar al inventario",
                    onClick = onNuevoProductoClicked,
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
                    )
                )
            }
            item {
                DashboardCard(
                    icon = Icons.Default.ShoppingCart,
                    title = "Ventas",
                    subtitle = "Gestionar transacciones",
                    onClick = onVentasClicked,
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFFFF9800), Color(0xFFFFA726))
                    )
                )
            }
            item {
                DashboardCard(
                    icon = Icons.Default.History,
                    title = "Historial",
                    subtitle = "Ver movimientos",
                    onClick = onHistorialVentasClicked,
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF9C27B0), Color(0xFFAB47BC))
                    )
                )
            }
            if (userRole == "Administrador") {
                item {
                    DashboardCard(
                        icon = Icons.Default.People,
                        title = "Cliente",
                        subtitle = "Administrar clientes",
                        onClick = onGestionUsuariosClicked,
                        gradient = Brush.linearGradient(
                            colors = listOf(Color(0xFFE91E63), Color(0xFFEC407A))
                        )
                    )
                }
            }
            item {
                DashboardCard(
                    icon = Icons.Default.Warning,
                    title = "Alertas",
                    subtitle = "Ver notificaciones",
                    onClick = onAlertasClicked,
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFFF44336), Color(0xFFEF5350))
                    )
                )
            }
            item {
                DashboardCard(
                    icon = Icons.Default.History,
                    title = "Historial de Cambios",
                    subtitle = "Historial",
                    onClick = onHistorialCambiosClicked,
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF607D8B), Color(0xFF78909C))
                    )
                )
            }
            item {
                DashboardCard(
                    icon = Icons.Default.FileDownload,
                    title = "Exportar",
                    subtitle = "Exportar datos",
                    onClick = onExportarClicked,
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF009688), Color(0xFF26A69A))
                    )
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    gradient: Brush
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ActionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .let { if (onClick != null) it.clickable { onClick() } else it },
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
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF1976D2),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 15.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
} 