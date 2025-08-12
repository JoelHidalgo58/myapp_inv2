package com.example.myapp_inv

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapp_inv.ui.theme.Myapp_invTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.util.*
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.compose.ui.tooling.preview.Preview

/**
 * Actividad principal de la aplicación que maneja la navegación y el estado global.
 * Esta clase es responsable de:
 * - Gestionar el inicio de sesión de usuarios
 * - Mantener el estado de la aplicación (usuarios, productos, ventas, historial)
 * - Persistir datos usando DataStore
 * - Manejar la navegación entre pantallas
 * - Gestionar notificaciones del sistema
 */
class MainActivity : ComponentActivity() {
    companion object {
        private const val CHANNEL_ID = "inventario_channel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        enableEdgeToEdge()
        setContent {
            Myapp_invTheme {
                val context = this@MainActivity
                val scope = rememberCoroutineScope()

                /**
                 * Funciones de conversión entre modelos de dominio y persistencia
                 * Estas funciones permiten transformar los datos entre el formato
                 * usado en la aplicación y el formato almacenado en DataStore
                 */
                fun Usuario.toPersist() = UsuarioPersist(nombre, username, password, rol)
                fun UsuarioPersist.toDomain() = Usuario(nombre, username, password, rol)
                fun Producto.toPersist() = ProductoPersist(nombre, cantidad, precio)
                fun ProductoPersist.toDomain(id: String = "") = Producto(id = id, nombre = nombre, cantidad = cantidad, precio = precio)
                fun Venta.toPersist() = VentaPersist(id, producto.nombre, producto.precio, cantidad, total, fecha.time, vendedor, cliente.username)
                fun VentaPersist.toDomain(productos: List<Producto>, usuarios: List<Usuario>): Venta {
                    val producto = productos.find { it.nombre == productoNombre } ?: Producto(nombre = productoNombre, cantidad = productoCantidad, precio = productoPrecio)
                    val cliente = usuarios.find { it.username == clienteUsername } ?: Usuario("", clienteUsername, "", "Regular")
                    return Venta(id, producto, productoCantidad, productoPrecio, total, Date(fecha), vendedor, cliente)
                }
                fun AccionHistorial.toPersist() = AccionHistorialPersist(tipo.name, descripcion, fecha.time, usuario)
                fun AccionHistorialPersist.toDomain(): AccionHistorial = AccionHistorial(TipoAccion.valueOf(tipo), descripcion, Date(fecha), usuario)

                /**
                 * Estado global de la aplicación
                 * Estas variables mantienen el estado de:
                 * - Lista de usuarios registrados
                 * - Inventario de productos
                 * - Registro de ventas
                 * - Historial de acciones
                 */
                var usuarios by remember { mutableStateOf(listOf<Usuario>()) }
                var productos by remember { mutableStateOf(listOf<Producto>()) }
                var ventas by remember { mutableStateOf(listOf<Venta>()) }
                var historialAcciones by remember { mutableStateOf(listOf<AccionHistorial>()) }

                /**
                 * Funciones para persistir datos en DataStore
                 * Estas funciones se encargan de guardar los cambios en el almacenamiento
                 * persistente cada vez que se modifica el estado
                 */
                fun guardarUsuariosEnDataStore(nuevosUsuarios: List<Usuario>) {
                    scope.launch {
                        context.usuariosDataStore.updateData { nuevosUsuarios.map { it.toPersist() } }
                    }
                }

                /**
                 * Guarda la lista de productos en DataStore
                 */
                fun guardarProductosEnDataStore(nuevosProductos: List<Producto>) {
                    scope.launch {
                        context.productosDataStore.updateData { nuevosProductos.map { it.toPersist() } }
                    }
                }

                /**
                 * Guarda la lista de ventas en DataStore
                 */
                fun guardarVentasEnDataStore(nuevasVentas: List<Venta>) {
                    scope.launch {
                        context.ventasDataStore.updateData { nuevasVentas.map { it.toPersist() } }
                    }
                }

                /**
                 * Guarda el historial de acciones en DataStore
                 */
                fun guardarHistorialEnDataStore(nuevoHistorial: List<AccionHistorial>) {
                    scope.launch {
                        context.historialDataStore.updateData { nuevoHistorial.map { it.toPersist() } }
                    }
                }

                /**
                 * Efecto para cargar datos iniciales
                 * Este bloque se ejecuta al iniciar la aplicación y:
                 * - Carga usuarios, productos, ventas e historial desde DataStore
                 * - Crea un usuario admin por defecto si no hay usuarios
                 * - Inicializa el estado de la aplicación
                 */
                LaunchedEffect(Unit) {
                    val usuariosPersist = context.usuariosDataStore.data.first()
                    var usuariosCargados = usuariosPersist.map { it.toDomain() }
                    if (usuariosCargados.isEmpty()) {
                        val adminDefault = Usuario("Administrador", "admin", "admin123", "Administrador")
                        usuariosCargados = listOf(adminDefault)
                        guardarUsuariosEnDataStore(usuariosCargados)
                    }
                    usuarios = usuariosCargados
                    val productosPersist = context.productosDataStore.data.first()
                    productos = productosPersist.mapIndexed { idx, it -> it.toDomain(id = String.format("%04d", idx + 1)) }
                    val ventasPersist = context.ventasDataStore.data.first()
                    ventas = ventasPersist.map { it.toDomain(productos, usuarios) }
                    val historialPersist = context.historialDataStore.data.first()
                    historialAcciones = historialPersist.map { it.toDomain() }
                }

                /**
                 * Estado de la sesión y navegación
                 * Estas variables controlan:
                 * - Estado del inicio de sesión
                 * - Usuario actual y su rol
                 * - Navegación entre pantallas
                 */
                var loggedIn by remember { mutableStateOf(false) }
                var userName by remember { mutableStateOf("") }
                var showInventarioSuccess by remember { mutableStateOf(false) }
                var showDeleteDialog by remember { mutableStateOf(false) }
                var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
                val navController = rememberNavController()
                var currentUser by remember { mutableStateOf("") }
                var userRole by remember { mutableStateOf("") }
                
                /**
                 * Sistema de notificaciones
                 * Maneja las notificaciones del sistema y mensajes al usuario
                 */
                var showNotification by remember { mutableStateOf(false) }
                var notificationMessage by remember { mutableStateOf("") }
                var notificationType by remember { mutableStateOf(NotificationType.INFO) }
                
                /**
                 * Función para mostrar notificaciones del sistema
                 * Crea y muestra notificaciones con diferentes niveles de prioridad
                 * según el tipo de mensaje
                 */
                fun showSystemNotification(message: String, type: NotificationType = NotificationType.INFO) {
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    
                    val icon = when (type) {
                        NotificationType.INFO -> android.R.drawable.ic_dialog_info
                        NotificationType.WARNING -> android.R.drawable.ic_dialog_alert
                        NotificationType.ERROR -> android.R.drawable.ic_dialog_alert
                    }
                    
                    val priority = when (type) {
                        NotificationType.INFO -> NotificationCompat.PRIORITY_DEFAULT
                        NotificationType.WARNING -> NotificationCompat.PRIORITY_HIGH
                        NotificationType.ERROR -> NotificationCompat.PRIORITY_HIGH
                    }
                    
                    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(icon)
                        .setContentTitle("Inventario")
                        .setContentText(message)
                        .setPriority(priority)
                        .setAutoCancel(true)
                        .build()
                    
                    notificationManager.notify(1, notification)
                }

                /**
                 * Funciones para actualizar el estado global
                 * Estas funciones manejan las actualizaciones de:
                 * - Historial de acciones
                 * - Alertas de inventario
                 * - Productos
                 * - Usuarios
                 * - Ventas
                 */
                fun agregarAccionHistorial(accion: AccionHistorial) {
                    historialAcciones = historialAcciones + accion
                    guardarHistorialEnDataStore(historialAcciones)
                }

                fun generarYRegistrarAlertas(productos: List<Producto>) {
                    productos.forEach { producto ->
                        if (producto.cantidad < 10) {
                            agregarAccionHistorial(
                                AccionHistorial(
                                    tipo = TipoAccion.ALERTA,
                                    descripcion = "Stock bajo en '${producto.nombre}' (${producto.cantidad} unidades)",
                                    fecha = Date(),
                                    usuario = currentUser
                                )
                            )
                            showSystemNotification(
                                "¡Stock bajo! ${producto.nombre} tiene solo ${producto.cantidad} unidades",
                                NotificationType.WARNING
                            )
                        }
                        if (producto.cantidad == 0) {
                            showSystemNotification(
                                "¡Sin stock! ${producto.nombre} se ha agotado",
                                NotificationType.ERROR
                            )
                        }
                    }
                }

                fun actualizarProductos(nuevosProductos: List<Producto>) {
                    productos = nuevosProductos
                    guardarProductosEnDataStore(nuevosProductos)
                    generarYRegistrarAlertas(productos)
                }

                fun actualizarUsuarios(nuevosUsuarios: List<Usuario>) {
                    usuarios = nuevosUsuarios
                    guardarUsuariosEnDataStore(nuevosUsuarios)
                    val usuarioActual = usuarios.find { it.username == currentUser }
                    if (usuarioActual == null) {
                        loggedIn = false
                        currentUser = ""
                        userRole = ""
                        showSystemNotification("Tu cuenta ha sido eliminada o modificada", NotificationType.WARNING)
                    } else if (usuarioActual.rol != userRole) {
                        userRole = usuarioActual.rol
                        showSystemNotification("Tu rol ha sido actualizado", NotificationType.INFO)
                    }
                }

                fun actualizarVentas(nuevasVentas: List<Venta>) {
                    ventas = nuevasVentas
                    guardarVentasEnDataStore(nuevasVentas)
                }

                // Scaffold para manejar el Snackbar
                Scaffold(
                    snackbarHost = {
                        if (showNotification) {
                            Snackbar(
                                modifier = Modifier.padding(16.dp),
                                action = {
                                    TextButton(onClick = { showNotification = false }) {
                                        Text("CERRAR")
                                    }
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = when (notificationType) {
                                            NotificationType.INFO -> Icons.Default.Info
                                            NotificationType.WARNING -> Icons.Default.Warning
                                            NotificationType.ERROR -> Icons.Default.Error
                                        },
                                        contentDescription = null,
                                        tint = when (notificationType) {
                                            NotificationType.INFO -> MaterialTheme.colorScheme.primary
                                            NotificationType.WARNING -> MaterialTheme.colorScheme.tertiary
                                            NotificationType.ERROR -> MaterialTheme.colorScheme.error
                                        }
                                    )
                                    Text(notificationMessage)
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        if (!loggedIn) {
                            LoginScreen(
                                usuarios = usuarios,
                                onLoginSuccess = { name ->
                                    userName = name
                                    loggedIn = true
                                    currentUser = name
                                    userRole = usuarios.find { it.username == name }?.rol ?: "Usuario"
                                    showSystemNotification("Bienvenido, $name!", NotificationType.INFO)
                                }
                            )
                        } else {
                            NavHost(navController = navController, startDestination = "dashboard") {
                                composable("dashboard") {
                                    DashboardScreen(
                                        userName = currentUser,
                                        userRole = userRole,
                                        onLogout = { 
                                            loggedIn = false
                                            currentUser = ""
                                            userRole = ""
                                        },
                                        onInventarioClicked = { navController.navigate("inventario") },
                                        onNuevoProductoClicked = { navController.navigate("nuevo_producto/0/0/0") },
                                        onGestionUsuariosClicked = { navController.navigate("gestion_usuarios") },
                                        onVentasClicked = { navController.navigate("ventas") },
                                        onHistorialVentasClicked = { navController.navigate("historial_ventas") },
                                        onHistorialCambiosClicked = { navController.navigate("historial_cambios") },
                                        onAlertasClicked = { navController.navigate("alertas") },
                                        onExportarClicked = { navController.navigate("exportar_preview") }
                                    )
                                }
                                composable("exportar_preview") {
                                    ExportarScreen(
                                        userName = currentUser,
                                        ventasCount = ventas.size,
                                        clientesCount = usuarios.size,
                                        productosCount = productos.size,
                                        ventas = ventas,
                                        productos = productos,
                                        usuarios = usuarios,
                                        onBack = { navController.navigateUp() },
                                        onLogout = { 
                                            loggedIn = false
                                            currentUser = ""
                                            userRole = ""
                                        }
                                    )
                                }
                                composable("inventario") {
                                    InventarioScreen(
                                        productos = productos,
                                        onBack = { navController.navigateUp() },
                                        onLogout = { 
                                            loggedIn = false
                                            currentUser = ""
                                            userRole = ""
                                        },
                                        showSuccess = showInventarioSuccess,
                                        onSuccessShown = { showInventarioSuccess = false },
                                        onEditarProducto = { producto ->
                                            navController.navigate("nuevo_producto/${producto.nombre}/${producto.cantidad}/${producto.precio}")
                                        },
                                        onEliminarConfirmado = { producto ->
                                            actualizarProductos(productos.filter { it.nombre != producto.nombre })
                                            showInventarioSuccess = true
                                        },
                                        onAgregarProducto = { producto ->
                                            actualizarProductos(productos + producto)
                                            showInventarioSuccess = true
                                        }
                                    )
                                }
                                composable(
                                    route = "nuevo_producto/{nombre}/{cantidad}/{precio}",
                                    arguments = listOf(
                                        navArgument("nombre") { type = NavType.StringType },
                                        navArgument("cantidad") { type = NavType.IntType },
                                        navArgument("precio") { type = NavType.FloatType }
                                    )
                                ) { backStackEntry ->
                                    val nombre = backStackEntry.arguments?.getString("nombre")
                                    val cantidad = backStackEntry.arguments?.getInt("cantidad")
                                    val precio = backStackEntry.arguments?.getFloat("precio")
                                    
                                    val productoParaEditar = if (nombre != null && cantidad != null && precio != null && nombre != "0") {
                                        Producto(
                                            id = String.format("%04d", productos.size + 1),
                                            nombre = nombre,
                                            cantidad = cantidad,
                                            precio = precio.toDouble()
                                        )
                                    } else null

                                    NuevoProductoScreen(
                                        onBack = { navController.navigateUp() },
                                        onLogout = { 
                                            loggedIn = false
                                            currentUser = ""
                                            userRole = ""
                                        },
                                        onAgregarProducto = { nuevoProducto ->
                                            if (productoParaEditar != null) {
                                                actualizarProductos(productos.map {
                                                    if (it.nombre == productoParaEditar.nombre) nuevoProducto else it
                                                })
                                                agregarAccionHistorial(
                                                    AccionHistorial(
                                                        tipo = TipoAccion.EDICION,
                                                        descripcion = "Producto '${productoParaEditar.nombre}' editado",
                                                        fecha = Date(),
                                                        usuario = currentUser
                                                    )
                                                )
                                            } else {
                                                val productoExistente = productos.find { it.nombre.equals(nuevoProducto.nombre, ignoreCase = true) }
                                                
                                                if (productoExistente != null) {
                                                    actualizarProductos(productos.map {
                                                        if (it.nombre.equals(nuevoProducto.nombre, ignoreCase = true)) {
                                                            it.copy(cantidad = it.cantidad + nuevoProducto.cantidad)
                                                        } else {
                                                            it
                                                        }
                                                    })
                                                    agregarAccionHistorial(
                                                        AccionHistorial(
                                                            tipo = TipoAccion.AGREGADO,
                                                            descripcion = "Stock actualizado para '${nuevoProducto.nombre}'",
                                                            fecha = Date(),
                                                            usuario = currentUser
                                                        )
                                                    )
                                                } else {
                                                    actualizarProductos(productos + nuevoProducto)
                                                    agregarAccionHistorial(
                                                        AccionHistorial(
                                                            tipo = TipoAccion.AGREGADO,
                                                            descripcion = "Nuevo producto '${nuevoProducto.nombre}' agregado",
                                                            fecha = Date(),
                                                            usuario = currentUser
                                                        )
                                                    )
                                                }
                                            }
                                            
                                            showInventarioSuccess = true
                                            navController.navigate("inventario") {
                                                popUpTo("dashboard")
                                            }
                                        },
                                        productoParaEditar = productoParaEditar,
                                        productosExistentes = productos
                                    )
                                }
                                composable("gestion_usuarios") {
                                    GestionUsuariosScreen(
                                        userName = currentUser,
                                        onBack = { navController.navigateUp() },
                                        onLogout = { 
                                            loggedIn = false
                                            currentUser = ""
                                            userRole = ""
                                        },
                                        usuarios = usuarios,
                                        userRole = userRole,
                                        onUsuariosActualizados = { nuevosUsuarios ->
                                            actualizarUsuarios(nuevosUsuarios)
                                        }
                                    )
                                }
                                composable("ventas") {
                                    VentasScreen(
                                        userName = currentUser,
                                        productos = productos,
                                        usuarios = usuarios,
                                        onBack = { navController.navigateUp() },
                                        onLogout = { 
                                            loggedIn = false
                                            currentUser = ""
                                            userRole = ""
                                        },
                                        onVentaRealizada = { productosVenta, cliente ->
                                            // Actualizar el inventario para cada producto vendido
                                            actualizarProductos(productos.map { producto ->
                                                val ventaProducto = productosVenta.find { it.first.id == producto.id }
                                                if (ventaProducto != null) {
                                                    producto.copy(cantidad = producto.cantidad - ventaProducto.second)
                                                } else {
                                                    producto
                                                }
                                            })
                                            
                                            // Crear una venta por cada producto
                                            productosVenta.forEach { (producto, cantidad) ->
                                                val venta = Venta(
                                                    producto = producto,
                                                    cantidad = cantidad,
                                                    precioUnitario = producto.precio,
                                                    total = producto.precio * cantidad,
                                                    vendedor = currentUser,
                                                    cliente = cliente
                                                )
                                                actualizarVentas(ventas + venta)
                                                // Registrar la acción en el historial
                                                agregarAccionHistorial(
                                                    AccionHistorial(
                                                        tipo = TipoAccion.VENTA,
                                                        descripcion = "Venta de ${cantidad} ${producto.nombre} a ${cliente.nombre}",
                                                        fecha = Date(),
                                                        usuario = currentUser
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }
                                composable("historial_ventas") {
                                    HistorialVentasScreen(
                                        ventas = ventas,
                                        onBack = { navController.navigateUp() },
                                        onLogout = { 
                                            loggedIn = false
                                            currentUser = ""
                                            userRole = ""
                                        }
                                    )
                                }
                                composable("historial_cambios") {
                                    HistorialCambiosScreen(
                                        historial = historialAcciones,
                                        onNavigateBack = { navController.navigateUp() }
                                    )
                                }
                                composable("alertas") {
                                    AlertasScreen(
                                        onBack = { navController.navigateUp() },
                                        onLogout = { 
                                            loggedIn = false
                                            currentUser = ""
                                            userRole = ""
                                        },
                                        productos = productos
                                    )
                                }
                            }
                        }
                    }
                }

                if (showDeleteDialog && productoAEliminar != null) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false; productoAEliminar = null },
                        title = { Text("Eliminar producto") },
                        text = { Text("¿Estás seguro de que deseas eliminar el producto '${productoAEliminar?.nombre}'?") },
                        confirmButton = {
                            Button(onClick = {
                                actualizarProductos(productos.filter { it.nombre != productoAEliminar?.nombre })
                                showDeleteDialog = false
                                productoAEliminar = null
                            }) { Text("Eliminar") }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showDeleteDialog = false; productoAEliminar = null }) { Text("Cancelar") }
                        }
                    )
                }
            }
        }
    }

    /**
     * Crea el canal de notificaciones para Android 8.0 y superiores
     * Este canal es necesario para mostrar notificaciones del sistema
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificaciones de Inventario"
            val descriptionText = "Canal para notificaciones del sistema de inventario"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Myapp_invTheme {
        Greeting("Android")
    }
}

/**
 * Enumeración para los tipos de notificaciones del sistema
 * Define los diferentes niveles de notificación:
 * - INFO: Mensajes informativos
 * - WARNING: Advertencias
 * - ERROR: Errores críticos
 */
enum class NotificationType {
    INFO, WARNING, ERROR
}