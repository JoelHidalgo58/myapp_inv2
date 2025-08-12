package com.example.myapp_inv

enum class RolUsuario {
    ADMINISTRADOR,
    VENDEDOR,
    INVENTARISTA,
    REGULAR
}

data class Usuario(
    val nombre: String,
    val username: String,
    val password: String,
    val rol: String,
    val cedula: String = username // Por defecto usamos el username como cédula
) {
    init {
        require(nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(username.isNotBlank()) { "El nombre de usuario no puede estar vacío" }
        require(password.isNotBlank()) { "La contraseña no puede estar vacía" }
        require(rol.isNotBlank()) { "El rol no puede estar vacío" }
        require(validarRol(rol)) { "Rol inválido. Debe ser uno de: ${RolUsuario.values().joinToString()}" }
    }

    companion object {
        fun validarRol(rol: String): Boolean {
            return try {
                RolUsuario.valueOf(rol.uppercase())
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }
    }

    fun validarNombre(): Boolean = nombre.isNotBlank()
    fun validarUsername(): Boolean = username.isNotBlank()
    fun validarPassword(): Boolean = password.isNotBlank()
    fun validarRol(): Boolean = validarRol(rol)
    fun validarCedula(): Boolean = cedula.isNotBlank()
} 