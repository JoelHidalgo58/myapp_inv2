package com.example.myapp_inv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.foundation.clickable
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    userName: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    usuarios: List<Usuario>,
    userRole: String,
    onUsuariosActualizados: (List<Usuario>) -> Unit
) {
    if (userRole != "Administrador") {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Acceso no permitido", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) { Text("Volver al Dashboard") }
        }
        return
    }

    var usuariosList by remember { mutableStateOf(usuarios) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }
    var usuarioEnEdicion by remember { mutableStateOf<Usuario?>(null) }
    
    // Estado para las pestañas
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Clientes", "Administradores")
    
    // Colores para las pestañas
    val tabColors = listOf(
        Color(0xFF4CAF50), // Verde para Clientes
        Color(0xFF212121)  // Negro para Administradores
    )
    
    // Estado para mostrar/ocultar formulario
    var showForm by remember { mutableStateOf(false) }
    
    // Campos del formulario de cliente
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoApellido by remember { mutableStateOf("") }
    var nuevoNumeroIdentidad by remember { mutableStateOf("") }
    var nuevoTelefono by remember { mutableStateOf("") }
    var nuevoCorreo by remember { mutableStateOf("") }
    var nuevaDireccion by remember { mutableStateOf("") }
    var nuevoTipoCliente by remember { mutableStateOf("Regular") }
    var nuevoMetodoPago by remember { mutableStateOf("Efectivo") }
    var expandedTipoCliente by remember { mutableStateOf(false) }
    var expandedMetodoPago by remember { mutableStateOf(false) }
    
    // Errores
    var errorNombre by remember { mutableStateOf(false) }
    var errorApellido by remember { mutableStateOf(false) }
    var errorNumeroIdentidad by remember { mutableStateOf(false) }
    var errorTelefono by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf(false) }
    var errorDireccion by remember { mutableStateOf(false) }
    
    val tiposCliente = listOf("Regular", "VIP", "Mayorista")
    val metodosPago = listOf("Efectivo", "Transferencia")
    
    // Lista de dominios más comunes de correo
    val emailDomains = listOf(
        "@gmail.com",
        "@outlook.com",
        "@hotmail.com",
        "@yahoo.com"
    )
    
    // Función para generar sugerencias de email
    fun getEmailSuggestions(input: String): List<String> {
        if (input.isEmpty()) return emptyList()
        val base = input.split("@")[0]
        return emailDomains.map { "$base$it" }
    }
    
    // Campos del formulario de administrador
    var nuevoNombreAdmin by remember { mutableStateOf("") }
    var nuevoUsername by remember { mutableStateOf("") }
    var nuevoPassword by remember { mutableStateOf("") }
    var nuevoRol by remember { mutableStateOf("Administrador") }
    
    // Errores
    var errorNombreAdmin by remember { mutableStateOf(false) }
    var errorUsername by remember { mutableStateOf(false) }
    var errorPassword by remember { mutableStateOf(false) }

    // Función para validar si una cadena contiene solo letras
    fun contieneSoloLetras(texto: String): Boolean {
        return texto.all { it.isLetter() || it.isWhitespace() }
    }

    // Función para validar si una cadena contiene solo números
    fun contieneSoloNumeros(texto: String): Boolean {
        return texto.all { it.isDigit() }
    }

    // Función para validar formato de correo electrónico
    fun esCorreoValido(correo: String): Boolean {
        return correo.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))
    }

    fun limpiarCamposCliente() {
        nuevoNombre = ""
        nuevoApellido = ""
        nuevoNumeroIdentidad = ""
        nuevoTelefono = ""
        nuevoCorreo = ""
        nuevaDireccion = ""
        nuevoTipoCliente = "Regular"
        nuevoMetodoPago = "Efectivo"
        errorNombre = false
        errorApellido = false
        errorNumeroIdentidad = false
        errorTelefono = false
        errorCorreo = false
        errorDireccion = false
        usuarioEnEdicion = null
    }

    fun limpiarCamposAdmin() {
        nuevoNombreAdmin = ""
        nuevoUsername = ""
        nuevoPassword = ""
        nuevoRol = "Administrador"
        errorNombreAdmin = false
        errorUsername = false
        errorPassword = false
        usuarioEnEdicion = null
    }

    fun validarCamposCliente(): Boolean {
        var esValido = true
        if (nuevoNombre.isBlank()) {
            errorNombre = true
            esValido = false
        }
        if (nuevoApellido.isBlank()) {
            errorApellido = true
            esValido = false
        }
        if (nuevoNumeroIdentidad.isBlank()) {
            errorNumeroIdentidad = true
            esValido = false
        }
        if (nuevoTelefono.isBlank()) {
            errorTelefono = true
            esValido = false
        }
        if (nuevoCorreo.isBlank() || !esCorreoValido(nuevoCorreo)) {
            errorCorreo = true
            esValido = false
        }
        if (nuevaDireccion.isBlank()) {
            errorDireccion = true
            esValido = false
        }
        return esValido
    }

    fun validarCamposAdmin(): Boolean {
        var esValido = true
        if (nuevoNombreAdmin.isBlank()) {
            errorNombreAdmin = true
            esValido = false
        }
        if (nuevoUsername.isBlank()) {
            errorUsername = true
            esValido = false
        }
        if (nuevoPassword.isBlank()) {
            errorPassword = true
            esValido = false
        }
        return esValido
    }

    fun guardarCliente() {
        if (!validarCamposCliente()) return

        val nuevoCliente = if (usuarioEnEdicion != null) {
            usuarioEnEdicion!!.copy(
                nombre = "$nuevoNombre $nuevoApellido",
                username = nuevoNumeroIdentidad,
                password = nuevoCorreo,
                rol = nuevoTipoCliente
            )
        } else {
            Usuario(
                nombre = "$nuevoNombre $nuevoApellido",
                username = nuevoNumeroIdentidad,
                password = nuevoCorreo,
                rol = nuevoTipoCliente
            )
        }

        val clientesActualizados = if (usuarioEnEdicion != null) {
            usuariosList.map { if (it.username == usuarioEnEdicion!!.username) nuevoCliente else it }
        } else {
            usuariosList + nuevoCliente
        }

        usuariosList = clientesActualizados
        onUsuariosActualizados(clientesActualizados)
        limpiarCamposCliente()
        showForm = false
    }

    // Función para cargar los datos del cliente en el formulario
    fun cargarDatosCliente(usuario: Usuario) {
        val nombreCompleto = usuario.nombre.split(" ")
        nuevoNombre = nombreCompleto.getOrNull(0) ?: ""
        nuevoApellido = nombreCompleto.getOrNull(1) ?: ""
        nuevoNumeroIdentidad = usuario.username
        nuevoTelefono = usuario.username
        nuevoCorreo = usuario.password
        nuevaDireccion = usuario.password
        nuevoTipoCliente = usuario.rol
        usuarioEnEdicion = usuario
        showForm = true
    }

    // Observar cambios en los campos para actualizar automáticamente
    LaunchedEffect(nuevoNombre, nuevoApellido, nuevoNumeroIdentidad, nuevoTelefono, nuevoCorreo, nuevaDireccion, nuevoTipoCliente) {
        if (selectedTab == 0 && showForm && usuarioEnEdicion != null) {
            val nuevoCliente = usuarioEnEdicion!!.copy(
                nombre = "$nuevoNombre $nuevoApellido",
                username = nuevoNumeroIdentidad,
                password = nuevoCorreo,
                rol = nuevoTipoCliente
            )
            usuariosList = usuariosList.map { if (it.username == usuarioEnEdicion!!.username) nuevoCliente else it }
            onUsuariosActualizados(usuariosList)
        }
    }

    fun guardarAdmin() {
        if (!validarCamposAdmin()) return

        val nuevoAdmin = if (usuarioEnEdicion != null) {
            usuarioEnEdicion!!.copy(
                nombre = nuevoNombreAdmin,
                username = nuevoUsername,
                password = nuevoPassword,
                rol = nuevoRol
            )
        } else {
            Usuario(nuevoNombreAdmin, nuevoUsername, nuevoPassword, nuevoRol)
        }

        val adminsActualizados = if (usuarioEnEdicion != null) {
            usuariosList.map { if (it.username == usuarioEnEdicion!!.username) nuevoAdmin else it }
        } else {
            usuariosList + nuevoAdmin
        }

        usuariosList = adminsActualizados
        onUsuariosActualizados(adminsActualizados)
        limpiarCamposAdmin()
    }

    // Función para guardar automáticamente cuando se completa el formulario
    fun validarYGuardarCliente() {
        if (validarCamposCliente()) {
            guardarCliente()
            showForm = false
        }
    }

    fun validarYGuardarAdmin() {
        if (validarCamposAdmin()) {
            guardarAdmin()
            showForm = false
        }
    }

    LaunchedEffect(nuevoNombreAdmin, nuevoUsername, nuevoPassword, nuevoRol) {
        if (selectedTab == 1 && showForm && usuarioEnEdicion != null) {
            validarYGuardarAdmin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
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
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Usuarios",
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
        }

        // Pestañas
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = tabColors[selectedTab],
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) tabColors[index] else Color.Gray
                        )
                    }
                )
            }
        }

        // Contenido de las pestañas
        when (selectedTab) {
            0 -> { // Clientes
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Botón Nuevo Cliente
                    AnimatedVisibility(
                        visible = !showForm,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Button(
                            onClick = { 
                                showForm = true
                                limpiarCamposCliente()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Agregar",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Nuevo Cliente",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    // Formulario de nuevo cliente
                    AnimatedVisibility(
                        visible = showForm,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 8.dp
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(24.dp)
                            ) {
                                // Encabezado del formulario
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = if (usuarioEnEdicion != null) "Editar Cliente" else "Nuevo Cliente",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1976D2)
                                        )
                                    }
                                    IconButton(
                                        onClick = { 
                                            showForm = false
                                            limpiarCamposCliente()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Cerrar",
                                            tint = Color.Gray
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    color = Color(0xFFE0E0E0),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )

                                // Contenedor principal del formulario
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    // Sección de Información Personal
                                    Text(
                                        text = "Información Personal",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    // Nombre y Apellido en la misma fila
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = nuevoNombre,
                                            onValueChange = { 
                                                if (contieneSoloLetras(it)) {
                                                    nuevoNombre = it
                                                    errorNombre = false
                                                }
                                            },
                                            label = { Text("Nombre", color = Color.Black) },
                                            isError = errorNombre,
                                            supportingText = {
                                                if (errorNombre) {
                                                    Text("El nombre es requerido", color = Color.Red)
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF4CAF50),
                                                focusedLabelColor = Color(0xFF4CAF50),
                                                cursorColor = Color(0xFF4CAF50),
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black
                                            )
                                        )
                                        OutlinedTextField(
                                            value = nuevoApellido,
                                            onValueChange = { 
                                                if (contieneSoloLetras(it)) {
                                                    nuevoApellido = it
                                                    errorApellido = false
                                                }
                                            },
                                            label = { Text("Apellido", color = Color.Black) },
                                            isError = errorApellido,
                                            supportingText = {
                                                if (errorApellido) {
                                                    Text("El apellido es requerido", color = Color.Red)
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF4CAF50),
                                                focusedLabelColor = Color(0xFF4CAF50),
                                                cursorColor = Color(0xFF4CAF50),
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Sección de Información de Contacto
                                    Text(
                                        text = "Información de Contacto",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    // Número de Identidad y Teléfono en la misma fila
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = nuevoNumeroIdentidad,
                                            onValueChange = { 
                                                if (contieneSoloNumeros(it)) {
                                                    nuevoNumeroIdentidad = it
                                                    errorNumeroIdentidad = false
                                                }
                                            },
                                            label = { Text("CI/RUC", color = Color.Black) },
                                            isError = errorNumeroIdentidad,
                                            supportingText = {
                                                if (errorNumeroIdentidad) {
                                                    Text("El número de identidad es requerido", color = Color.Red)
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF4CAF50),
                                                focusedLabelColor = Color(0xFF4CAF50),
                                                cursorColor = Color(0xFF4CAF50),
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black
                                            )
                                        )
                                        OutlinedTextField(
                                            value = nuevoTelefono,
                                            onValueChange = { 
                                                if (contieneSoloNumeros(it)) {
                                                    nuevoTelefono = it
                                                    errorTelefono = false
                                                }
                                            },
                                            label = { Text("Teléfono", color = Color.Black) },
                                            isError = errorTelefono,
                                            supportingText = {
                                                if (errorTelefono) {
                                                    Text("El teléfono es requerido", color = Color.Red)
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF4CAF50),
                                                focusedLabelColor = Color(0xFF4CAF50),
                                                cursorColor = Color(0xFF4CAF50),
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))

                                    OutlinedTextField(
                                        value = nuevoCorreo,
                                        onValueChange = { 
                                            nuevoCorreo = it
                                            errorCorreo = false
                                        },
                                        label = { Text("Correo Electrónico", color = Color.Black) },
                                        isError = errorCorreo,
                                        supportingText = {
                                            if (errorCorreo) {
                                                Text("Ingrese un correo válido", color = Color.Red)
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF4CAF50),
                                            focusedLabelColor = Color(0xFF4CAF50),
                                            cursorColor = Color(0xFF4CAF50),
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    OutlinedTextField(
                                        value = nuevaDireccion,
                                        onValueChange = { 
                                            nuevaDireccion = it
                                            errorDireccion = false
                                        },
                                        label = { Text("Dirección", color = Color.Black) },
                                        isError = errorDireccion,
                                        supportingText = {
                                            if (errorDireccion) {
                                                Text("La dirección es requerida", color = Color.Red)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF4CAF50),
                                            focusedLabelColor = Color(0xFF4CAF50),
                                            cursorColor = Color(0xFF4CAF50),
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Sección de Preferencias
                                    Text(
                                        text = "Preferencias",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    // Tipo de Cliente y Método de Pago en la misma fila
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ExposedDropdownMenuBox(
                                            expanded = expandedTipoCliente,
                                            onExpandedChange = { expandedTipoCliente = it },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            OutlinedTextField(
                                                value = nuevoTipoCliente,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Tipo de Cliente", color = Color.Black) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoCliente) },
                                                modifier = Modifier
                                                    .menuAnchor()
                                                    .fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFF4CAF50),
                                                    focusedLabelColor = Color(0xFF4CAF50),
                                                    cursorColor = Color(0xFF4CAF50),
                                                    focusedTextColor = Color.Black,
                                                    unfocusedTextColor = Color.Black
                                                )
                                            )
                                            ExposedDropdownMenu(
                                                expanded = expandedTipoCliente,
                                                onDismissRequest = { expandedTipoCliente = false },
                                                modifier = Modifier.background(Color.White)
                                            ) {
                                                tiposCliente.forEach { tipo ->
                                                    DropdownMenuItem(
                                                        text = { 
                                                            Text(
                                                                tipo,
                                                                color = Color.Black,
                                                                modifier = Modifier.background(Color.White)
                                                            )
                                                        },
                                                        onClick = {
                                                            nuevoTipoCliente = tipo
                                                            expandedTipoCliente = false
                                                        },
                                                        modifier = Modifier.background(Color.White)
                                                    )
                                                }
                                            }
                                        }

                                        ExposedDropdownMenuBox(
                                            expanded = expandedMetodoPago,
                                            onExpandedChange = { expandedMetodoPago = it },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            OutlinedTextField(
                                                value = nuevoMetodoPago,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Método de Pago", color = Color.Black) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMetodoPago) },
                                                modifier = Modifier
                                                    .menuAnchor()
                                                    .fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFF4CAF50),
                                                    focusedLabelColor = Color(0xFF4CAF50),
                                                    cursorColor = Color(0xFF4CAF50),
                                                    focusedTextColor = Color.Black,
                                                    unfocusedTextColor = Color.Black
                                                )
                                            )
                                            ExposedDropdownMenu(
                                                expanded = expandedMetodoPago,
                                                onDismissRequest = { expandedMetodoPago = false },
                                                modifier = Modifier.background(Color.White)
                                            ) {
                                                metodosPago.forEach { metodo ->
                                                    DropdownMenuItem(
                                                        text = { 
                                                            Text(
                                                                metodo,
                                                                color = Color.Black,
                                                                modifier = Modifier.background(Color.White)
                                                            )
                                                        },
                                                        onClick = {
                                                            nuevoMetodoPago = metodo
                                                            expandedMetodoPago = false
                                                        },
                                                        modifier = Modifier.background(Color.White)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Botones de acción
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = { 
                                                showForm = false
                                                limpiarCamposCliente()
                                            },
                                            modifier = Modifier.padding(end = 8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color(0xFF1976D2)
                                            )
                                        ) {
                                            Text("Cancelar")
                                        }
                                        Button(
                                            onClick = { 
                                                if (validarCamposCliente()) {
                                                    guardarCliente()
                                                    showForm = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4CAF50)
                                            ),
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Text(
                                                if (usuarioEnEdicion != null) "Actualizar" else "Guardar",
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Lista de clientes
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(usuariosList.filter { it.rol != "Administrador" && it.rol != "Supervisor" }) { usuario ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        cargarDatosCliente(usuario)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE8F5E9)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = usuario.nombre,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1976D2)
                                            )
                                            Text(
                                                text = "Teléfono: ${usuario.username}",
                                                color = Color.Gray,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "Rol: ${usuario.rol}",
                                                color = Color.Gray,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    cargarDatosCliente(usuario)
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Editar",
                                                    tint = Color(0xFF1976D2)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    usuarioAEliminar = usuario
                                                    showDeleteDialog = true
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Eliminar",
                                                    tint = Color.Red
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
            1 -> { // Administradores
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(usuariosList.filter { it.rol == "Administrador" || it.rol == "Supervisor" }) { usuario ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    usuarioEnEdicion = usuario
                                    nuevoNombreAdmin = usuario.nombre
                                    nuevoUsername = usuario.username
                                    nuevoPassword = usuario.password
                                    nuevoRol = usuario.rol
                                    showForm = true
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFEEEEEE) // Fondo gris oscuro
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = usuario.nombre,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1976D2)
                                        )
                                        Text(
                                            text = "Usuario: ${usuario.username}",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Rol: ${usuario.rol}",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Row {
                                        IconButton(
                                            onClick = {
                                                usuarioEnEdicion = usuario
                                                nuevoNombreAdmin = usuario.nombre
                                                nuevoUsername = usuario.username
                                                nuevoPassword = usuario.password
                                                nuevoRol = usuario.rol
                                                showForm = true
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = Color(0xFF1976D2)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                usuarioAEliminar = usuario
                                                showDeleteDialog = true
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color.Red
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

    if (showDeleteDialog && usuarioAEliminar != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                usuarioAEliminar = null
            },
            title = { Text("Eliminar ${if (selectedTab == 0) "Cliente" else "Administrador"}") },
            text = { Text("¿Estás seguro de que deseas eliminar a '${usuarioAEliminar?.nombre}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        usuariosList = usuariosList.filter { it.username != usuarioAEliminar?.username }
                        onUsuariosActualizados(usuariosList)
                        showDeleteDialog = false
                        usuarioAEliminar = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        usuarioAEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
} 