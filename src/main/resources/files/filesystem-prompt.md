Sistema de archivos --- para la construcción del sistema de archivos noto que las tablas involucradas en el flujo serán principalmente (no es que sean todas):
- asignación_rol_proyecto
- categoria_has_proyecto
- categoria_has_repositorio
- enlace
- entorno_prueba (por el momento podemos ignorar por completo esto, queda pendiente hacer la modificación en la base de datos para que conecte con los proyectos y repositorios)
- equipo
- equipo_has_proyecto
- equipo_has_repositorio
- nodo
- nodo_tag
- nodo_tag_master
- permiso_nodo
- proyecto
- proyecto_has_repositorio
- repositorio
- rol
- rol_proyecto
- usuario
- usuario_has_equipo
- usuario_has_proyecto
- usuario_has_repositorio
- version_archivo
Es importante que notes que las tablas fundamentales involucradas son proyecto, repositorio y nodo, de manera que la idea es:
* Respecto a proyectos, poder asociar uno o múltiples repositorios y carpetas (nodos) al proyecto. A su vez, dentro de estos repositorios podremos encontrar una o múltiples carpetas (nodo), y a su vez, dentro de estas carpetas (nodos), encontrar una o múltiples carpetas (nodos), y así sucesivamente.
* Respecto a repositorios, poder asociar una o múltiples carpetas (nodos) al repositorio. A su vez dentro de estas carpetas (nodos) podremos encontrar una o múltiples carpetas (nodo), y a su vez, dentro de estas carpetas (nodos), encontrar una o múltiples carpetas (nodos), y así sucesivamente.

Básicamente quiero que la construcción de este sistema de archivos sea similar a Google Drive principalmente (o GitHub), de manera que yo puede Copiar, Cortar, Pegar, Crear (crear archivo o crear carpeta), Eliminar, Renombrar, Descargar carpeta, Descargar archivo, Comprimir (carpeta), Subir archivo (o archivos en caso de poderse), Subir carpeta (o carpetas en caso de poderse), así como navegar a través de las carpetas (nodos) del sistema de archivos entre las acciones principales. Para es importante tener en una cuenta que es deseado poder dar click derecho en una sección fuera de las carpetas existen en caso las haya, para así poder Crear una carpeta, Crear un archivo, Subir una carpeta, Subir un archivo, Pegar; mientras que si se da click derecho sobre una carpeta o grupo de carpetas (deberían de seleccionarse), las acciones disponibles serían Copiar, Cortar, Pegar, Descargar carpeta(s) [esto solo en caso de que se pueda hacer una descarga masiva], Descargar archivo(s) [esto solo en caso de que se pueda hacer una descarga masiva], Comprimir carpeta [esto solo en caso de que se pueda hacer una compresión masiva], Renombrar (solo si es que se selecciona una carpeta), y puede que por ahí me esté olvidando alguna. Cuando le dé doble click izquierdo a una carpeta deberá de mostrarse todo su contenido, y así sucesivamente cada vez que yo quiera abrir una carpeta, asimismo, deberá de ser posible regresar a la carpeta anterior, es decir, pasar de una carpeta hijo a una padre y de una padre a una hijo, etc. etc., y al darle doble click sobre un archivo, y si es que es un archivo, entonces este deberá de abrirse y mostrarse (como en GitHub), y si es que la plataforma web no puede abrirlo, entonces deberá de llamar a una aplicación (web o de escritorio) para que pueda hacerlo (POR EL MOMENTO PUEDES OLVIDAR ESTO DE LLAMAR A OTRAS APPS). Si es que se da click ya sea izquierdo o derecho sobre una carpeta o archivo solo deberá de seleccionarse.
La ruta base con la que trabajaremos será la siguiente para el sistema de archivos: 
* Asociación a la sección Proyectos: proyecto base --- "devportal/po/mlopez/projects/P-{id (del proyecto)}" / En una sección de esta vista se muestran Repositorios y Carpetas, siendo aquí donde deberá iniciar el Fyle System (FS) --- 
* Asociación a la sección Repositorios: proyecto base --- "devportal/po/mlopez/projects/R-{id (del repositorio)}" / En una sección de esta vista se muestran Carpetas siendo aquí donde deberá iniciar el Fyle System (FS) --- 
- Caso de Repositorio: si es que me adentro a una carpeta, por ejemplo "Authentication" al estar dentro de un Repositorio, entonces la ruta en el navegador deberá ser: "devportal/po/mlopez/repositories/R-{id (del repositorio)}/Authentication" (se deberá de respetar los caracteres y el uso de las mayúsculas) y si creo una dentro de esta, entonces "devportal/po/mlopez/repositories/R-{id (del repositorio)}/Authentication/OAuth2", por ejemplo. 
- Caso de Proyecto: si es que me adentro a una carpeta, por ejemplo "Developer tools" al estar dentro de un Proyecto, entonces la ruta en el navegador deberá ser: "devportal/po/mlopez/projects/P-{id (del proyecto)}/Developer tools" (se deberá de respetar los caracteres y el uso de las mayúsculas) y si creo una dentro de esta, entonces "devportal/po/mlopez/repositories/R-{id (del repositorio)}/Developer tools/SDK", por ejemplo. 

- Pero también podrá ser posible que si es que me adentro a un repositorio, por ejemplo "Desarrollo de sistemas de teledetección inteligentes" al estar dentro de un Proyectos, entonces la ruta en el navegador deberá ser: "devportal/po/mlopez/projects/P-{id (del proyecto)}/R-{id (del repositorio)}" (deberá permitirse crear las carpetas con espacios y respetar caracteres y uso de mayúsculas y minúsculas), a su vez, si yo creo una carpeta dentro de un repositorio, por ejemplo "Arduino UNO para radares" y ver la ruta "devportal/po/mlopez/projects/P-{id (del proyecto)}/R-{id (del repositorio)}/Arduino UNO para radares", pero también dentro de esta carpeta podría crear algo como por ejemplo la carpeta "Pines digitales", entonces la ruta será "devportal/po/mlopez/projects/P-{id (del proyecto)}/R-{id (del repositorio)}/Arduino UNO para radares/Desarrollo de sistemas de teledetección inteligentes/Arduino UNO para radares", por ejemplo. 

¿Sería mejor manejar las rutas de esa manera?¿O sería mejor algo como esto: "devportal/po/mlopez/projects/P-{id (del proyecto)}/R-{id (del repositorio)}/N-{id (del Nodo XXXXXXX)}/N-{id (del nodo YYYYYYY)}"?

Deberás revisar muy cuidadosamente todas las tablas involucradas, así que tómate el tiempo de entender bien mi base de datos que es fundamental, explícame qué función consideras que tiene cada tabla y que previo a la construcción del File System es necesario agregar o quitar algunas cosas.

Asimismo, traza un plan para ver tanto los ajustes que debo de hacer en mi proyecto así como en la nube (creé mi bucket, entonces será necesario que me detalles como vincular todo ello con mi proyecto, estoy usando GCP). No olvides decirme que funciones tendrá cada tabla para el sistema de archivos.

También cabe hacer mención que deseo integrar la plataforma con GitHub, en particular, conectar los repositorios con GitHub … para ello me sugirieron crear una o algunas tablas (o en español) más, algo como:

CREATE TABLE github_integration (
    github_integration_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    repositorio_id BIGINT UNSIGNED NOT NULL,
    github_repository_fullname VARCHAR(255) NOT NULL COMMENT 'owner/repo',
    github_repository_url VARCHAR(512) NOT NULL,
    access_token_id BIGINT UNSIGNED NULL,
    webhook_id VARCHAR(100) NULL,
    sync_mode ENUM('API', 'CLONE') DEFAULT 'API',
    last_sync DATETIME NULL,
    FOREIGN KEY (repositorio_id) REFERENCES repositorio(repositorio_id)
);

o también:

CREATE TABLE github_token (
    github_token_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT UNSIGNED NOT NULL,
    access_token VARCHAR(512) NOT NULL,
    scope VARCHAR(255) NULL,
    expires_at DATETIME NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id)
);

pero esto habrá que analizar … habrá que ver si es que correcto ello o si es que sería mejor tomar otra medida, podrían ser requeridas incluso más tablas. Además estoy considerando posiblemente integrar una CLI en la plataforma (sería la última medida), pero quería saber qué me conviene hacer.
