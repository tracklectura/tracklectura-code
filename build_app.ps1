# Script de construcción para TrackLectura (Portable + EXE)
$ErrorActionPreference = "Stop"

# --- Configuración de Versión ---
$AppVersion = "1.2.7"
# Este UUID es fijo para permitir actualizaciones de versiones instaladas
$UpgradeUuid = "e7b9f3a1-c2d4-4e56-8a9b-0c1d2e3f4a5b"

# --- Configuración del Icono ---
$IconPath = "assets\icon.ico"   # <-- Cambia esta ruta si tu .ico está en otro sitio

# --- Credenciales de Supabase ---
# Estas credenciales se escriben en supabase.properties dentro del paquete.
# Ese archivo NO debe subirse al repositorio (añádelo al .gitignore).
# La app lo lee al arrancar, cifra los valores en config.properties y ya no lo necesita.
$SupabaseUrl = "https://cptbivigiemodirfwgny.supabase.co"
$SupabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNwdGJpdmlnaWVtb2RpcmZ3Z255Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI1ODgwMDIsImV4cCI6MjA4ODE2NDAwMn0.CALP_aPbh-olrJQIqENIlFI4T1SLz4Okdjj2PebAdD4"

Write-Host "--- Iniciando proceso de construcción (v$AppVersion) ---" -ForegroundColor Cyan

# 1. Limpieza de seguridad
Write-Host "1. Limpiando procesos y carpetas antiguas..." -ForegroundColor Yellow
Stop-Process -Name TrackLectura -ErrorAction SilentlyContinue 2>$null
Start-Sleep -Seconds 1

$folders = "out", "release", "dist", "packaging_temp"
foreach ($f in $folders) {
    if (Test-Path $f) { Remove-Item -Recurse -Force $f -ErrorAction SilentlyContinue }
}

# 2. Compilación
Write-Host "2. Compilando código Java..." -ForegroundColor Yellow
$libs = Get-ChildItem lib/*.jar | ForEach-Object { $_.FullName }
$classpath = ($libs -join ";") + ";src"
New-Item -ItemType Directory -Path "out/production/TrackLectura" -Force

javac -d out/production/TrackLectura -cp $classpath (Get-ChildItem src/*.java, src/**/*.java | ForEach-Object { $_.FullName })

# 3. Creación del Fat JAR
Write-Host "3. Extrayendo librerías y creando JAR único..." -ForegroundColor Yellow
$tempDir = "packaging_temp"
New-Item -ItemType Directory -Path $tempDir -Force
Copy-Item -Path "out/production/TrackLectura/*" -Destination $tempDir -Recurse -Force

$originalPath = Get-Location
Set-Location $tempDir
foreach ($jar in Get-ChildItem ../lib/*.jar) {
    Write-Host "   -> Procesando $($jar.Name)" -ForegroundColor Gray
    jar xf $jar.FullName
}
# No borremos todo META-INF, solo las firmas que dan error en un Fat JAR
if (Test-Path "META-INF") {
    Get-ChildItem "META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA", "META-INF/MANIFEST.MF" -ErrorAction SilentlyContinue | Remove-Item -Force
}
Set-Location $originalPath

jar --create --file TrackLectura.jar --main-class main.TrackerApp -C $tempDir .

# 4. Preparar distribución
Write-Host "4. Organizando carpeta dist..." -ForegroundColor Yellow
New-Item -ItemType Directory -Path dist -Force
Move-Item TrackLectura.jar dist/

# 4b. Escribir supabase.properties junto al JAR
# La app lee este archivo al arrancar (TrackerApp.cargarCredencialesSupabase),
# cifra los valores en %LOCALAPPDATA%\TrackLectura\config.properties
# y en ejecuciones posteriores ya no necesita este archivo.
Write-Host "   -> Escribiendo supabase.properties..." -ForegroundColor Yellow
$supabaseConfig = @"
# Credenciales del proyecto Supabase para TrackLectura
# Este archivo es leído una sola vez al primer arranque.
# NO lo subas a ningún repositorio público.
supabase.url=$SupabaseUrl
supabase.anonKey=$SupabaseAnonKey
"@
$supabaseConfig | Out-File -FilePath "dist/supabase.properties" -Encoding utf8 -NoNewline
Write-Host "   -> supabase.properties creado en dist/" -ForegroundColor Green

# 5. Generar Formatos de Salida
Write-Host "5. Generando formatos de aplicación..." -ForegroundColor Yellow

# Parámetros base para jpackage
$baseArgs = @(
    "--name", "TrackLectura",
    "--input", "dist",
    "--main-jar", "TrackLectura.jar",
    "--main-class", "main.TrackerApp",
    "--dest", "release",
    "--app-version", $AppVersion,
    "--vendor", "pbalsach"
)

# Añadir icono si existe el archivo
if (Test-Path $IconPath) {
    Write-Host "   -> Icono encontrado: $IconPath" -ForegroundColor Green
    $baseArgs += "--icon", $IconPath
}
else {
    Write-Host "   -> AVISO: No se encontró el icono en '$IconPath'. Se usará el icono por defecto." -ForegroundColor DarkYellow
    Write-Host "      Coloca tu .ico en esa ruta o cambia la variable `$IconPath al inicio del script." -ForegroundColor DarkYellow
}

# A. Versión PORTABLE (App-Image)
Write-Host "   -> Creando Versión Portable (Carpeta)..." -ForegroundColor Cyan
jpackage @baseArgs --type app-image

# B. Instalador EXE
Write-Host "   -> Creando Instalador EXE..." -ForegroundColor Cyan
# --win-upgrade-uuid es necesario para detectar instalaciones anteriores y actualizarlas
jpackage @baseArgs --type exe --win-dir-chooser --win-shortcut --win-menu --win-per-user-install --win-upgrade-uuid $UpgradeUuid

Write-Host "`n¡TODO LISTO!" -ForegroundColor Green
Write-Host "1. Portable: release/TrackLectura/" -ForegroundColor White
Write-Host "2. Instalador: release/TrackLectura-$AppVersion.exe" -ForegroundColor White

# Recordatorio final
Write-Host "`nRECUERDA: Añade estas líneas a tu .gitignore si no están ya:" -ForegroundColor DarkYellow
Write-Host "  dist/supabase.properties" -ForegroundColor DarkYellow
Write-Host "  supabase.properties" -ForegroundColor DarkYellow