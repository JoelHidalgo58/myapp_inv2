package com.example.myapp_inv

import java.util.*

data class Venta(
    val id: String = UUID.randomUUID().toString(),
    val producto: Producto,
    val cantidad: Int,
    val precioUnitario: Double,
    val total: Double,
    val fecha: Date = Date(),
    val vendedor: String,
    val cliente: Usuario
) {
    init {
        require(cantidad > 0) { "La cantidad debe ser mayor que 0" }
        require(precioUnitario >= 0) { "El precio unitario no puede ser negativo" }
        require(total >= 0) { "El total no puede ser negativo" }
        require(vendedor.isNotBlank()) { "El vendedor no puede estar vacío" }
        require(Math.abs(total - (cantidad * precioUnitario)) < 0.01) { "El total no coincide con la cantidad por precio unitario" }
        require(precioUnitario == producto.precio) { "El precio unitario debe coincidir con el precio del producto" }
    }

    fun validarCantidad(): Boolean = cantidad > 0
    fun validarPrecioUnitario(): Boolean = precioUnitario >= 0
    fun validarTotal(): Boolean = total >= 0 && Math.abs(total - (cantidad * precioUnitario)) < 0.01
    fun validarVendedor(): Boolean = vendedor.isNotBlank()
    fun validarCliente(): Boolean = cliente.username.isNotBlank()
    fun validarFecha(): Boolean = fecha.before(Date()) || fecha == Date()
} 