<div align="center">

  # ⚡ ZerithLag

  **Plugin antilag para servidores de Minecraft**

  ![Version](https://img.shields.io/badge/version-1.0.0-gold?style=for-the-badge)
  ![Minecraft](https://img.shields.io/badge/minecraft-1.20--1.21+-green?style=for-the-badge&logo=minecraft)
  ![Java](https://img.shields.io/badge/java-17%2B-orange?style=for-the-badge&logo=openjdk)
  ![Platform](https://img.shields.io/badge/platform-Paper%20%2F%20Spigot-blue?style=for-the-badge)
  ![Author](https://img.shields.io/badge/autor-zerinho23-purple?style=for-the-badge)

  </div>

  ---

  ## 📋 Descripción

  **ZerithLag** es un plugin antilag inspirado en ClearLagg, diseñado para mantener el rendimiento de tu servidor de Minecraft limpiando automáticamente entidades innecesarias como ítems en el suelo, flechas, orbes de experiencia, mobs hostiles y más.

  Totalmente configurable desde el `config.yml`, con soporte de colores usando códigos `&`, mensajes en la barra de acción, estadísticas en tiempo real y monitor de TPS.

  ---

  ## ✨ Características

  - 🧹 **Limpieza automática** de entidades cada X segundos (configurable)
  - ⏳ **Cuenta regresiva** con mensajes personalizables antes de cada limpieza
  - 📊 **Monitor de TPS** del servidor en tiempo real
  - 📋 **Reporte de entidades** por mundo y tipo
  - 🌍 **Soporte multimundo** — elige qué mundos limpiar
  - 🎨 **Colores personalizables** en todos los mensajes con códigos `&`
  - 💬 **Action Bar** — los avisos aparecen en la barra de acción (sin spam en el chat)
  - 🐾 **Protección de mascotas** — no elimina animales domesticados ni entidades con nombre
  - 📈 **Estadísticas** — total de entidades eliminadas y última limpieza
  - ✅ **Compatible** con Minecraft **1.20 → 1.21+**

  ---

  ## 📥 Instalación

  1. Descarga el archivo `ZerithLag-1.0.0.jar` desde [Releases](../../releases)
  2. Copia el JAR en la carpeta `plugins/` de tu servidor
  3. Reinicia el servidor
  4. Edita el `plugins/ZerithLag/config.yml` a tu gusto
  5. Usa `/zerith reload` para aplicar los cambios sin reiniciar

  **Requisitos:**
  | Requisito | Versión mínima |
  |---|---|
  | Minecraft | 1.20 |
  | Java | 17 |
  | Paper / Spigot | 1.20+ |

  ---

  ## 🎮 Comandos

  | Comando | Descripción | Permiso |
  |---|---|---|
  | `/zerith` | Muestra la ayuda | `zerithlag.use` |
  | `/zerith reload` | Recarga el config.yml | `zerithlag.reload` |
  | `/zerith clear` | Limpia entidades manualmente | `zerithlag.clear` |
  | `/zerith tps` | Muestra el TPS del servidor | `zerithlag.tps` |
  | `/zerith entities [mundo]` | Lista entidades por tipo | `zerithlag.entities` |
  | `/zerith info` | Info del plugin y estadísticas | `zerithlag.use` |

  **Aliases:** `/zl`, `/zerithlag`, `/antilag`

  ---

  ## 🔑 Permisos

  | Permiso | Descripción | Por defecto |
  |---|---|---|
  | `zerithlag.use` | Usar `/zerith` básico | OP |
  | `zerithlag.reload` | Recargar la configuración | OP |
  | `zerithlag.clear` | Limpiar entidades manualmente | OP |
  | `zerithlag.tps` | Ver el TPS del servidor | OP |
  | `zerithlag.entities` | Ver reporte de entidades | OP |

  ---

  ## ⚙️ Configuración

  El archivo `config.yml` se genera automáticamente en `plugins/ZerithLag/` al iniciar el servidor por primera vez.

  ```yaml
  # Prefijo del plugin (soporta &colores)
  prefix: "&8[&6ZerithLag&8] &r"

  auto-clear:
    enabled: true          # Activar limpieza automática
    interval: 300          # Segundos entre cada limpieza
    broadcast: true        # Enviar mensajes a todos los jugadores
    use-action-bar: true   # Mostrar en barra de acción (no en el chat)
    worlds:
      - all                # "all" = todos los mundos, o pon el nombre exacto

    # Segundos restantes en los que se enviará aviso
    countdown: [60, 30, 10, 5, 3, 2, 1]

    countdown-message: "&eLimpiando entidades en &c{time}s&e..."
    clear-message: "&aEliminadas &e{amount} &aentidades del mundo."

  entity-types:
    keep-named: true       # Conservar entidades con nombre personalizado
    keep-tamed: true       # Conservar animales domesticados
    remove: []             # Vacío = usa la lista inteligente automática

  messages:
    reload: "&aConfiguración recargada correctamente."
    clear-command: "&aEliminadas &e{amount} &aentidades manualmente."
    no-permission: "&cNo tienes permiso para usar este comando."
    info: "&6ZerithLag &fv{version} &7por &e{author} &8| &7Total eliminadas: &e{total}"
  ```

  ### 🎨 Códigos de color

  Usa `&` seguido de un carácter para colorear los mensajes:

  `&0` Negro | `&1` Azul oscuro | `&2` Verde | `&3` Cian | `&4` Rojo  
  `&5` Morado | `&6` Dorado | `&7` Gris | `&8` Gris oscuro | `&9` Azul  
  `&a` Verde claro | `&b` Cian claro | `&c` Rojo claro | `&d` Rosa | `&e` Amarillo | `&f` Blanco  
  `&l` **Negrita** | `&o` *Cursiva* | `&n` Subrayado | `&m` ~~Tachado~~ | `&r` Reset

  ---

  ## 🧹 Entidades que se eliminan (modo automático)

  Cuando `entity-types.remove` está vacío, ZerithLag usa su lista inteligente:

  - 📦 Ítems en el suelo (`ITEM`)
  - ✨ Orbes de experiencia (`EXPERIENCE_ORB`)
  - 🏹 Flechas y proyectiles (`ARROW`, `SPECTRAL_ARROW`, etc.)
  - 💣 TNT encendido y bloques en caída
  - 👾 Todos los mobs hostiles (Zombie, Skeleton, Creeper, **Bogged**, **Breeze** de 1.21...)
  - 🚤 Botes y vagonetas sin pasajeros
  - ⚗️ Pociones y objetos lanzados en vuelo

  **Nunca se eliminan:** jugadores, animales domesticados (perros, gatos, caballos con dueño) y entidades con nombre personalizado.

  ---

  ## 🔨 Compilar desde el código fuente

  ```bash
  git clone https://github.com/Zerinho23/ZerithLag.git
  cd ZerithLag
  mvn clean package
  # El JAR estará en target/ZerithLag-1.0.0.jar
  ```

  **Requisitos para compilar:** Java 17+, Maven 3.8+

  ---

  ## 📜 Licencia

  Este proyecto es de uso libre para servidores de Minecraft. Créditos a **zerinho23**.

  ---

  <div align="center">
  Hecho con ❤️ por <strong>zerinho23</strong>
  </div>
  