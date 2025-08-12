package com.example.myapp_inv

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class UsuarioPersist(val nombre: String, val username: String, val password: String, val rol: String)
@Serializable
data class ProductoPersist(val nombre: String, val cantidad: Int, val precio: Double)
@Serializable
data class VentaPersist(
    val id: String,
    val productoNombre: String,
    val productoPrecio: Double,
    val productoCantidad: Int,
    val total: Double,
    val fecha: Long,
    val vendedor: String,
    val clienteUsername: String
)
@Serializable
data class AccionHistorialPersist(
    val tipo: String,
    val descripcion: String,
    val fecha: Long,
    val usuario: String
)

object UsuariosSerializer : Serializer<List<UsuarioPersist>> {
    override val defaultValue: List<UsuarioPersist> = emptyList()
    override suspend fun readFrom(input: InputStream): List<UsuarioPersist> =
        try { Json.decodeFromString(input.readBytes().decodeToString()) } catch (e: Exception) { emptyList() }
    override suspend fun writeTo(t: List<UsuarioPersist>, output: OutputStream) =
        output.write(Json.encodeToString(t).encodeToByteArray())
}

object ProductosSerializer : Serializer<List<ProductoPersist>> {
    override val defaultValue: List<ProductoPersist> = emptyList()
    override suspend fun readFrom(input: InputStream): List<ProductoPersist> =
        try { Json.decodeFromString(input.readBytes().decodeToString()) } catch (e: Exception) { emptyList() }
    override suspend fun writeTo(t: List<ProductoPersist>, output: OutputStream) =
        output.write(Json.encodeToString(t).encodeToByteArray())
}

object VentasSerializer : Serializer<List<VentaPersist>> {
    override val defaultValue: List<VentaPersist> = emptyList()
    override suspend fun readFrom(input: InputStream): List<VentaPersist> =
        try { Json.decodeFromString(input.readBytes().decodeToString()) } catch (e: Exception) { emptyList() }
    override suspend fun writeTo(t: List<VentaPersist>, output: OutputStream) =
        output.write(Json.encodeToString(t).encodeToByteArray())
}

object AccionHistorialSerializer : Serializer<List<AccionHistorialPersist>> {
    override val defaultValue: List<AccionHistorialPersist> = emptyList()
    override suspend fun readFrom(input: InputStream): List<AccionHistorialPersist> =
        try { Json.decodeFromString(input.readBytes().decodeToString()) } catch (e: Exception) { emptyList() }
    override suspend fun writeTo(t: List<AccionHistorialPersist>, output: OutputStream) =
        output.write(Json.encodeToString(t).encodeToByteArray())
}

val Context.usuariosDataStore: DataStore<List<UsuarioPersist>> by dataStore(
    fileName = "usuarios.json",
    serializer = UsuariosSerializer
)

val Context.productosDataStore: DataStore<List<ProductoPersist>> by dataStore(
    fileName = "productos.json",
    serializer = ProductosSerializer
)

val Context.ventasDataStore: DataStore<List<VentaPersist>> by dataStore(
    fileName = "ventas.json",
    serializer = VentasSerializer
)

val Context.historialDataStore: DataStore<List<AccionHistorialPersist>> by dataStore(
    fileName = "historial.json",
    serializer = AccionHistorialSerializer
) 