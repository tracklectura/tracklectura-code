# 📖 TrackLectura

Aplicación de escritorio para registrar y analizar tus hábitos de lectura. Permite llevar un seguimiento de sesiones, páginas leídas, velocidad lectora y progreso por libro, con sincronización en la nube mediante Supabase.

## Características

- Registro de sesiones de lectura (páginas, tiempo, velocidad)
- Estadísticas y gráficas por libro
- Sincronización en la nube con Supabase
- Modo offline (sin conexión)
- Exportación a CSV e imagen
- Tema claro / oscuro
- Búsqueda de portadas automática

## 🚀 Descarga e Instalación

La forma más sencilla de usar TrackLectura en Windows es descargando el instalador oficial desde nuestra sección de lanzamientos:

👉 **[Descargar TrackLectura v1.3.9(.exe)](https://github.com/Pau-Balsach/tracklectura-code/releases/latest/download/TrackLectura-1.3.9.exe)**

> **Nota:** Si prefieres no instalar el programa en tu equipo o utilizas otro sistema operativo (Linux / macOS), puedes usar los comandos de compilación y ejecución que se detallan más abajo.

## Requisitos

- Java 17 o superior

## 🛠️ Compilar y ejecutar (Alternativa a la instalación)

Si no deseas instalar el programa usando el ejecutable o estás en un sistema operativo diferente a Windows, puedes compilar y ejecutar el código fuente directamente usando la terminal. 

**Windows (PowerShell)**
```powershell
if (!(Test-Path out)) { mkdir out }
$libs = (Get-ChildItem lib/*.jar | Select-Object -ExpandProperty FullName) -join ';'
javac -cp "$libs;out" -d out (Get-ChildItem -Path src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
java -cp "$libs;out" main.TrackerApp
```

**Linux / macOS**

```bash

mkdir -p out
javac -cp "lib/*:out" -d out $(find src -name "*.java")
java -cp "lib/*:out" main.TrackerApp

```

## Documentación

- [Manual en Español](docs/manual_es.md)

- [Manual in English](docs/manual_en.md)

- [Manuel en Français](docs/manual_fr.md)



## 🧪 Testing

El proyecto incluye una suite de tests unitarios que cubre la lógica de negocio principal, diseñada para ejecutarse en CI sin necesidad de base de datos ni conexión a Supabase.

**Tecnologías:** JUnit 5 (Jupiter) · JUnit Params (`@ParameterizedTest` + `@CsvSource`)

**Cobertura:**

| Clase bajo test | Qué se verifica |
|---|---|
| `ReadingCalculator` | Páginas leídas, PPM, PPH, porcentaje, estimación de tiempo, validación de sesión, extracción de fecha/hora |
| `Sesion` / `Libro` | Getters, valores límite, coherencia PPM × 60 ≈ PPH |
| `SessionTimer` | Estado inicial, inicio/pausa/reinicio, acumulación de tiempo, `ajustarTiempo()`, formateo `HH:mm:ss` |
| `DatabaseManager` | Idempotencia de inicialización, guards de null, contrato de `haySincronizacionPendiente()` |

**Ejecutar los tests:**

```bash
# Linux / macOS
javac -cp "lib/*:out" -d out $(find src test -name "*.java")
java -cp "lib/*:out:lib/junit-platform-console-standalone.jar" \
     org.junit.platform.console.ConsoleLauncher --scan-classpath

# Windows (PowerShell)
$libs = (Get-ChildItem lib/*.jar | % FullName) -join ';'
javac -cp "$libs;out" -d out (Get-ChildItem -Path src,test -Recurse -Filter *.java | % FullName)
java -cp "$libs;out" org.junit.platform.console.ConsoleLauncher --scan-classpath
```

> Los tests de integración que requieren disco o Supabase están marcados con `@Tag("integration")` y pueden excluirse con `-Dgroups="!integration"`.

## Licencia



MIT License — ver archivo [LICENSE](LICENSE)