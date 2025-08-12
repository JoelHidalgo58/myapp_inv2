package com.example.myapp_inv

data class Producto(
    val id: String = "", // Formato: XXXX (4 dígitos numéricos)
    val nombre: String,
    val cantidad: Int,
    val precio: Double,
    val categoria: String = "General",
    val stock: Int = cantidad // Alias para cantidad para mantener compatibilidad
) {
    init {
        require(id.isEmpty() || id.matches(Regex("^\\d{4}$"))) { "El ID debe ser un número de 4 dígitos" }
        require(nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(cantidad >= 0) { "La cantidad no puede ser negativa" }
        require(precio >= 0) { "El precio no puede ser negativo" }
        require(categoria.isNotBlank()) { "La categoría no puede estar vacía" }
    }

    fun validarId(): Boolean = id.isEmpty() || id.matches(Regex("^\\d{4}$"))
    fun validarCantidad(): Boolean = cantidad >= 0
    fun validarPrecio(): Boolean = precio >= 0
    fun validarNombre(): Boolean = nombre.isNotBlank()
    fun validarCategoria(): Boolean = categoria.isNotBlank()
} 