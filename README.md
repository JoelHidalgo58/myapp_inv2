📱 APIN - Sistema de Gestión de Inventarios Móvil

[![Android](https://img.shields.io/badge/Android-API%2024+-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5+-purple.svg)](https://developer.android.com/jetpack/compose)

> Sistema de gestión de inventarios móvil desarrollado en Kotlin con Jetpack Compose para PyMEs

📖 Introducción

APIN es una aplicación móvil diseñada para **Pequeñas y Medianas Empresas (PyMEs)** que necesitan digitalizar su gestión de inventarios. Permite controlar stock, procesar ventas, gestionar usuarios y generar reportes desde dispositivos Android.
 ¿Qué hace APIN?
📦 Control de inventario en tiempo real
💰 Procesamiento de ventas con facturación
📊 Reportes automáticos en PDF
⚠️ Alertas inteligentes de stock bajo
👥 Gestión de usuarios con roles
📱 Interfaz moderna con Material Design 3

## 🚀 Instalación

### Requisitos
- Android Studio 2023.1.1+
- JDK 11+
- Android SDK API 24+
- Dispositivo Android 7.0+ o emulador

Pasos de Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/apin-inventario.git
cd apin-inventario
```
2. **Abrir en Android Studio**
   - Abrir Android Studio
   - Seleccionar "Open an existing project"
   - Navegar a la carpeta del proyecto

3. **Ejecutar la aplicación**
   - Conectar dispositivo o iniciar emulador
   - Click en Run (▶️) en Android Studio

4. **Configuración inicial**
```
Usuario: admin
Contraseña: admin123
Rol: Administrador
```
## 🔐 Cómo Ingresar

1. **Abrir la aplicación** APIN
2. **Ingresar credenciales**:
   - Usuario: `admin`
   - Contraseña: `admin123`
3. **Seleccionar rol**: Administrador
4. **Acceder al dashboard**

## 🛠️ Tecnologías

- **Kotlin** - Lenguaje de programación
- **Jetpack Compose** - UI Framework
- **DataStore** - Almacenamiento local
- **iText 7** - Generación de PDF
- **MVVM** - Arquitectura

## 📊 Funcionalidades

| Módulo | Estado |
|--------|--------|
| 🔐 Autenticación | ✅ Completado |
| 📦 Inventario | ✅ Completado |
| 💰 Ventas | ✅ Completado |
| 👥 Usuarios | ✅ Completado |
| 📊 Reportes | ✅ Completado |
| ⚠️ Alertas | ✅ Completado |

## 🚀 Comandos Útiles

```bash
# Limpiar y compilar
./gradlew clean build

# Instalar en dispositivo
./gradlew installDebug

# Ver logs
adb logcat | grep "APIN"
```

## 🤝 Contribución

1. Fork el proyecto
2. Crear rama para tu feature (`git checkout -b feature/NuevaFuncionalidad`)
3. Commit tus cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/NuevaFuncionalidad`)
5. Abrir un Pull Request

## 📄 Licencia

Este proyecto es parte de una tesis de grado y está destinado para fines educativos.

## 👨‍💻 Autor

- **Nombre del Estudiant**: Andy Hidalgo
- **Institución**: Instituto Superior Tecnológico Mayor Pedro Traversari
- **Tutor**: Ing. Kevin Chuquitarco

---

⭐ **Si este proyecto te ayuda, dale una estrella!**

**APIN** - Transformando la gestión de inventarios en PyMEs 🚀


