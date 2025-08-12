package com.example.myapp_inv

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background

data class Alerta(
    val tipo: TipoAlerta,
    val mensaje: String,
    val fecha: Date,
    val prioridad: PrioridadAlerta,
    val producto: Producto? = null
)

enum class TipoAlerta {
    STOCK_BAJO,
    VENCIMIENTO,
    ERROR_SISTEMA
}

enum class PrioridadAlerta {
    ALTA,
    MEDIA,
    BAJA
}

fun generarAlertasInventario(productos: List<Producto>): List<Alerta> {
    val alertas = mutableListOf<Alerta>()
    val fechaActual = Date()

    productos.forEach { producto ->
        // Alerta de stock bajo (menos de 10 unidades)
        if (producto.cantidad < 10) {
            alertas.add(
                Alerta(
                    tipo = TipoAlerta.STOCK_BAJO,
                    mensaje = "El producto '${producto.nombre}' tiene stock bajo (${producto.cantidad} unidades)",
                    fecha = fechaActual,
                    prioridad = if (producto.cantidad < 5) PrioridadAlerta.ALTA else PrioridadAlerta.MEDIA,
                    producto = producto
                )
            )
        }

        // Aquí podrías agregar más lógica para otros tipos de alertas
        // Por ejemplo, alertas de vencimiento si agregas fechas a los productos
    }

    return alertas
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    productos: List<Producto>
) {
    var alertas by remember { mutableStateOf(generarAlertasInventario(productos)) }

    // Actualizar alertas cuando cambien los productos
    LaunchedEffect(productos) {
        alertas = generarAlertasInventario(productos)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Alertas",
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color(0xFF1976D2)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Text(
                            "Cerrar Sesión",
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE3F2FD), Color(0xFFF6F6F6))
                    )
                )
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Alertas del Sistema",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (alertas.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "No hay alertas activas",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF666666)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(alertas) { alerta ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when (alerta.prioridad) {
                                    PrioridadAlerta.ALTA -> Color(0xFFFFEBEE) // Rojo muy claro
                                    PrioridadAlerta.MEDIA -> Color(0xFFFFF3E0) // Naranja muy claro
                                    PrioridadAlerta.BAJA -> Color(0xFFE8F5E9) // Verde muy claro
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = when (alerta.prioridad) {
                                            PrioridadAlerta.ALTA -> Color(0xFFD32F2F) // Rojo
                                            PrioridadAlerta.MEDIA -> Color(0xFFF57C00) // Naranja
                                            PrioridadAlerta.BAJA -> Color(0xFF388E3C) // Verde
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (alerta.tipo) {
                                            TipoAlerta.STOCK_BAJO -> "Stock Bajo"
                                            TipoAlerta.VENCIMIENTO -> "Vencimiento Próximo"
                                            TipoAlerta.ERROR_SISTEMA -> "Error del Sistema"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = when (alerta.prioridad) {
                                            PrioridadAlerta.ALTA -> Color(0xFFD32F2F)
                                            PrioridadAlerta.MEDIA -> Color(0xFFF57C00)
                                            PrioridadAlerta.BAJA -> Color(0xFF388E3C)
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = alerta.mensaje,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                        .format(alerta.fecha),
                                    color = Color(0xFF666666),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 