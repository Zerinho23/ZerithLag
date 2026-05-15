<div align="center">

  # ⚡ ZerithLag

  **Plugin antilag para servidores de Minecraft**
  Desarrollado por **zerinho23**

  [![Versión](https://img.shields.io/badge/versión-1.1.0-gold?style=for-the-badge)](https://github.com/Zerinho23/ZerithLag/releases/latest)
  [![Paper](https://img.shields.io/badge/Paper-1.20--1.21+-blue?style=for-the-badge)](https://papermc.io)
  [![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge)](https://adoptium.net)
  [![Licencia](https://img.shields.io/badge/licencia-MIT-green?style=for-the-badge)](LICENSE)

  </div>

  ---

  ## 📖 ¿Qué es ZerithLag?

  ZerithLag es un plugin antilag completo para servidores Paper/Spigot que reduce el lag causado por exceso de entidades mediante limpieza automática programada, limpieza de emergencia por TPS bajo y apilado inteligente de mobs.

  ---

  ## ✨ Características

  | Característica | Descripción |
  |---|---|
  | 🔄 **Limpieza automática** | Elimina entidades cada X segundos (configurable) |
  | ⚡ **Guardia de TPS** | Dispara limpieza de emergencia si el TPS cae por debajo del umbral |
  | 📦 **Mob Stacker** | Apila mobs idénticos cercanos en uno solo, reduciendo entidades |
  | 🎨 **Soporte HEX** | Colores `&#RRGGBB` en todos los mensajes |
  | 📊 **Estadísticas** | Contador total de entidades eliminadas y última limpieza |
  | 💬 **Alertas en chat** | Avisos configurables al chat independientes del action bar |
  | 🖥️ **Banner ANSI** | Banner colorido en la consola al cargar el plugin |

  ---

  ## 📥 Instalación

  1. Descarga el JAR más reciente desde [Releases](https://github.com/Zerinho23/ZerithLag/releases/latest)
  2. Copia `ZerithLag-X.X.X.jar` en la carpeta `plugins/` de tu servidor
  3. Reinicia el servidor
  4. Edita `plugins/ZerithLag/config.yml` a tu gusto
  5. Usa `/zerith reload` para aplicar cambios sin reiniciar

  **Requisitos:** Paper o Spigot **1.20 – 1.21+** · Java **17+**

  ---

  ## 🧹 Limpieza automática

  Limpia entidades cada cierto tiempo (300 segundos por defecto).

  ```yaml
  auto-clear:
    enabled: true
    interval: 300        # segundos entre limpiezas
    broadcast: true      # avisar a los jugadores
    use-action-bar: true # true = action bar, false = chat
    worlds:
      - all              # "all" o nombres de mundos específicos
    countdown: [60, 30, 10, 5, 4, 3, 2, 1]
    countdown-message: "&eLimpiando entidades en &c{time}s&e..."
    clear-message: "&aEliminadas &e{amount} &aentidades del mundo."
  ```

  ---

  ## ⚡ Guardia de TPS (auto-clear de emergencia)

  Monitorea el TPS cada 10 segundos. Si cae por debajo del umbral, limpia entidades automáticamente.

  ```yaml
  tps-clear:
    enabled: true
    threshold: 15.0  # TPS mínimo antes de activarse (20.0 = ideal)
    cooldown: 60     # segundos de espera entre limpiezas de emergencia
    message: "&cTPS bajo &7(&c{tps}&7) &c— limpiando &e{amount} &centidades de emergencia."
  ```

  **Variables:** `{tps}` = TPS detectado · `{amount}` = entidades eliminadas

  ---

  ## 📦 Mob Stacker

  Agrupa mobs idénticos cercanos en un solo mob con un nombre que muestra la cantidad apilada.

  ```
  [×4] Zombie   ← 4 zombies apilados en uno
  ```

  ```yaml
  mob-stacker:
    enabled: true
    radius: 5.0           # bloques de radio para agrupar
    auto: false           # true = apila antes de cada auto-clear
    name-format: "&7[&e×{count}&7] &f{type}"
    types: []             # vacío = todos los Monster
    # Ejemplos: [ZOMBIE, SKELETON, CREEPER, SPIDER]
  ```

  **Variables del nombre:** `{count}` = cantidad · `{type}` = nombre del mob

  ---

  ## 💬 Alertas en el chat

  Mensajes adicionales siempre enviados al chat (independientes del action bar).

  ```yaml
  chat-alerts:
    enabled: true
    minute-message: "&8[&6ZerithLag&8] &r&7La limpieza ocurrirá en &61 minuto&7."
    seconds-message: "&8[&6ZerithLag&8] &r&7La limpieza ocurrirá en &e{time} segundos&7."
    second-alerts: [30]   # segundos que activan el 'seconds-message'
    countdown-message: "&8[&6ZerithLag&8] &r&cLimpiando en &l{time}&c..."
    countdown-from: 5     # desde cuántos segundos iniciar la cuenta regresiva
  ```

  ---

  ## 🛡️ Tipos de entidades

  ```yaml
  entity-types:
    keep-named: true   # nunca eliminar mobs con nombre personalizado
    keep-tamed: true   # nunca eliminar animales domesticados
    remove: []         # lista personalizada (vacío = lista inteligente automática)
  ```

  **Lista automática (cuando `remove` está vacío):**
  Items sueltos · Orbes de experiencia · Flechas · Bolas de fuego · TNT activado · Bloques cayendo · Monsters · Botes/Minecarts vacíos · Proyectiles varios

  ---

  ## 🎨 Colores en mensajes

  Todos los mensajes de la config soportan dos formatos mezclables:

  ```
  Códigos &:    &a Verde   &c Rojo   &e Amarillo   &6 Dorado   &l Negrita
  Hex RGB:      &#FF6600   &#00AAFF   &#FF0066

  Ejemplo:  "&l&#FF6600ZerithLag &r&7v1.1.0"
  ```

  ---

  ## 🖥️ Comandos

  | Comando | Descripción | Permiso |
  |---|---|---|
  | `/zerith` | Muestra la ayuda | `zerithlag.use` |
  | `/zerith reload` | Recarga la configuración | `zerithlag.reload` |
  | `/zerith clear` | Limpia entidades manualmente | `zerithlag.clear` |
  | `/zerith stack` | Apila mobs cercanos manualmente | `zerithlag.stack` |
  | `/zerith tps` | Muestra el TPS del servidor | `zerithlag.tps` |
  | `/zerith entities [mundo]` | Reporte de entidades por tipo | `zerithlag.entities` |
  | `/zerith info` | Estadísticas del plugin | `zerithlag.use` |

  **Alias:** `/zl` · `/zerithlag` · `/antilag`

  ---

  ## 🔑 Permisos

  | Permiso | Descripción | Por defecto |
  |---|---|---|
  | `zerithlag.use` | Usar /zerith e /info | OP |
  | `zerithlag.reload` | Recargar configuración | OP |
  | `zerithlag.clear` | Limpiar entidades | OP |
  | `zerithlag.stack` | Apilar mobs | OP |
  | `zerithlag.tps` | Ver TPS | OP |
  | `zerithlag.entities` | Ver reporte de entidades | OP |

  ---

  ## 📋 Changelog

  ### v1.1.1
  - ✨ Nuevo: Formato de tiempo legible en config (`5m`, `1h`, `30s`, `1h30m`, etc.) para `interval` y `cooldown`
  - ✅ Compatibilidad hacia atrás: números enteros (segundos) siguen funcionando

  ### v1.1.0
  - ✨ Nuevo: Guardia de TPS — limpieza automática de emergencia por TPS bajo
  - ✨ Nuevo: Mob Stacker — apila mobs idénticos cercanos (`/zerith stack`)
  - ✨ Nuevo: Opción `mob-stacker.auto` para apilar antes de cada limpieza programada
  - ✨ Nuevo: Permiso `zerithlag.stack`

  ### v1.0.3
  - 🐛 Fix crítico: `api-version: "1.20"` entre comillas para evitar error *"Unsupported API version 1.2"*

  ### v1.0.2
  - ✨ Alertas en el chat (`chat-alerts`)
  - ✨ Soporte de colores HEX (`&#RRGGBB`)
  - ✨ Banner ANSI en consola

  ### v1.0.0 / v1.0.1
  - 🎉 Release inicial: limpieza automática, TPS monitor, estadísticas

  ---

  ## 🤝 Créditos

  Desarrollado con ❤️ por **zerinho23**

  [![GitHub](https://img.shields.io/badge/GitHub-Zerinho23-black?style=flat-square&logo=github)](https://github.com/Zerinho23)
  