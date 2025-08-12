package com.example.myapp_inv

import java.util.*

data class AccionHistorial(
    val tipo: TipoAccion,
    val descripcion: String,
    val fecha: Date,
    val usuario: String
) {
    init {
        require(descripcion.isNotBlank()) { "La descripción no puede estar vacía" }
        require(usuario.isNotBlank()) { "El usuario no puede estar vacío" }
        require(fecha.before(Date()) || fecha == Date()) { "La fecha no puede ser futura" }
    }

    fun validarDescripcion(): Boolean = descripcion.isNotBlank()
    fun validarUsuario(): Boolean = usuario.isNotBlank()
    fun validarFecha(): Boolean = fecha.before(Date()) || fecha == Date()
}

enum class TipoAccion {
    AGREGADO,
    EDICION,
    ELIMINACION,
    VENTA,
    ALERTA
} 