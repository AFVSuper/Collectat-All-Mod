![Collectat-All Banner](https://cdn.modrinth.com/data/91FBikTV/images/dac55213ba5d4f32d899530c73c61dbda1783e03.png)

# Collectat-All Mod

## Language
The mod is written in English and translated to Spanish (Español España en el selector de Minecraft)

The mod page is also written in both languages, so choose yours:
<details>
  <summary>English</summary>
  <br>
  
  ## About
  This mod adds some minigame-like game modes, all based in one thing, collecting items.

  The name is based on **Collectathon**, a video-game genre based on collecting stuff. In this mod, the default mode is to collect **ALL** items. There are also some other modes, listed below.
  
  The current game modes are:
  - **Casual**: The standard mode, just collect all the items at your own pace.
  - **Speedrun**: Collect all the items with a playtime timer in the screen. Get Fast!
  - **Timed**: Set a time and then collect as many items as you can before it ends.
  - **Count**: Set a number of items to get as an objective. A timer will show the playtime.
  
  This can be set in the world-gen screen, and all this descriptions are featured in that screen.

  *(I would greatly appreciate it if you report any issue, glitch or bug you find in [Issues](https://github.com/AFVSuper/Collectat-All-Mod/issues))*
  
  ## Hud and Gui
  There is a counter in the left up corner, that shows the items you have, and if the mode needs it, a timer in the right up corner.
  
  Also, with the command `/collectatall check` you can see what items are you missing.
  
  Clicking the book on a player's name in chat, or using the command `/collectatall check-player <player>` you can see the same gui, but with another player's stats.
  
  ## Cooperative
  Currently, there is no way to make the item count be shared by players, I will be working in a coop setting that will share the item count among all players.

  ## Server Settings and Administrators
  Since when creating a server, usually there is no world-gen screen, there are these operator commands listed below:
  - `/collectatall mode <normal|speedrun>`
  - `/collectatall mode timed <minutes>`
  - `/collectatall mode count <item_count>`
  - `/collectatall reset-time <player>`
  
  The first three are mode changing, and the last resets the timer in case it is necessary.

  Also, there are new gamerules:
  - `/gamerule rare_broadcast <true|false>` *(`false` by default)*
  
  If it is set to `true`, when a player gets a rare item, it will be broadcasted in chat.

  - `/gamerule modify_obtained <NONE|MARK|REMOVE|MIXED>` *(`NONE` by default)*

  The options are:
  - **NONE:** By default, it does nothing to the items that are obtained.
  - **MARK:** The item that is used by a player to increase their count are marked so other players can't use that. This makes that item unstackable, but prevents cheating in competitive scenarios.
  - **REMOVE:** The item that is used by a player to increase their count are removed (only once). This makes the inventory be much cleaner than with MARK, but is somewhat annoying to lose the first rare item.
  - **MIXED:** It is a mix between Mark and Remove, it behaves like Mark if the item is rare (name is not white) or unstackable, and it behaves as Remove in other case.
</details>

<details>
  <summary>Español</summary>
  <br>
  
  ## Sobre el Mod
  Este mod añade algunos modos de juego como "minijuegos", todos basados en recolectar objetos.

  El nombre está basado en **Collectathon**, un género de videojuegos basado en recolectar cosas. En este mod, el modo por defecto es recolectar **TODOS** los objetos. También hay más modos, listados abajo.
  
  Los modos de juego actualmente son:
  - **Casual**: El modo estándar, recolecta todos los objetos a tu ritmo.
  - **Speedrun**: Recolecta todos los objetos mientras te cronometran. ¡A correr!
  - **Tiempo**: Coloca un tiempo y recolecta todos los objetos que puedas antes de que termine.
  - **Cantidad**: Determina un número de objetos como objetivo. Un cronómetro contará tu tiempo.
  
  Esto puede configurarse en la creación de mundo, y todas las descripciones están en la pantalla.

  *(Agradecería mucho si reportaseis cualquier problema, glitch o bug que encontreis en [Issues](https://github.com/AFVSuper/Collectat-All-Mod/issues))*
  
  ## Interfaces
  Hay un contador de objetos en la esquina superior izquierda, y si el modo lo requiere, un cronómetro en la esquina superior derecha.

  También, con el comando `/collectatall check` puedes ver los objetos que te faltan.

  Haciendo click en el libro del nombre de un jugador en chat, o usando el comando `/collectatall check-player <player>` puedes ver la misma interfaz, pero esta vez mostrando las estadísticas de ese jugador.
  
  ## Cooperativo
  Actualmente, no hay un modo para que el contador se comparta entre jugadores, trabajaré en un futuro en una opción para que eso sea posible.

  ## Configuración de Servidor y Administradores
  Como al crear un servidor generalmente no se pasa por la pantalla de creación, existen los siguientes comandos de operador:
  - `/collectatall mode <normal|speedrun>`
  - `/collectatall mode timed <minutes>`
  - `/collectatall mode count <item_count>`
  - `/collectatall reset-time <player>`
  
  Los tres primeros cambian el modo de juego, mientras que el último reinicia el temporizador del jugador seleccionado en caso de que sea necesario.

  También hay una nueva gamerule:
  - `/gamerule rare_broadcast <true|false>` *(`false` por defecto)*
  
  Si se activa en `true`, cuando un jugador consigue un objeto raro es anunciado en el chat.

  - `/gamerule modify_obtained <NONE|MARK|REMOVE|MIXED>` *(`NONE` por defecto)*

  Las opciones son:
  - **NONE:** Por defecto, no modifica los objetos obtenidos.
  - **MARK:** El objeto que usado para aumentar la puntuación es marcado para que no cuente para otros jugadores. Esto hace dicho objeto no stackeable, pero puede prevenir trampas en competición.
  - **REMOVE:** El objeto usado para aumentar la puntuación es eliminado (unicamente 1 unidad). Esto puede ser molesto, pero el inventario es más limpio que con Mark.
  - **MIXED:** Es una mezcla entre Mark y Remove, se comporta como Mark si el objeto es raro (el nombre no es blanco) o si no se puede stackear, y se comporta como Remove en los demás casos.
</details>
