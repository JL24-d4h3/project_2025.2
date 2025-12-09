/**
 * API DOCUMENTATION - REDESIGN
 * JavaScript para menú lateral unificado y navegación
 */

class APIDocumentationNav {
    constructor() {
        this.sidebar = null;
        this.navContainer = null;
        this.currentActiveLink = null;
        this.openApiSpec = null;
        this.cmsSeciones = [];
        
        this.init();
    }

    /**
     * Inicialización principal
     */
    init() {
        console.log('Inicializando navegación de documentación...');
        
        // Esperar a que el DOM esté listo
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.setup());
        } else {
            this.setup();
        }
    }

    /**
     * Configuración inicial
     */
    setup() {
        this.sidebar = document.querySelector('.docs-sidebar');
        this.navContainer = document.querySelector('.docs-nav-list');
        
        if (!this.sidebar || !this.navContainer) {
            console.error('No se encontraron elementos del sidebar');
            return;
        }

        // Inicializar componentes
        this.setupMobileToggle();
        this.setupSmoothScroll();
        this.setupScrollSpy();
        this.setupExpandableItems();
        
        console.log('Navegación inicializada correctamente');
    }

    /**
     * Genera el menú completo a partir del OpenAPI spec y secciones CMS
     */
    generateMenu(openApiSpec, cmsSeciones) {
        this.openApiSpec = openApiSpec;
        this.cmsSeciones = cmsSeciones || [];
        
        const menuItems = [];

        // 1. INFORMACIÓN GENERAL
        if (openApiSpec.info) {
            menuItems.push({
                id: 'info-section',
                title: 'Información General',
                icon: 'fa-info-circle',
                type: 'section'
            });
        }

        // 2. SERVIDORES
        if (openApiSpec.servers && openApiSpec.servers.length > 0) {
            menuItems.push({
                id: 'servers-section',
                title: 'Servidores',
                icon: 'fa-server',
                type: 'section'
            });
        }

        // 3. AUTENTICACIÓN
        if (openApiSpec.components && openApiSpec.components.securitySchemes) {
            menuItems.push({
                id: 'security-section',
                title: 'Autenticación',
                icon: 'fa-shield-halved',
                type: 'section'
            });
        }

        // 4. ENDPOINTS (agrupados por tags)
        if (openApiSpec.paths && Object.keys(openApiSpec.paths).length > 0) {
            const endpointsByTag = this.groupEndpointsByTag(openApiSpec.paths, openApiSpec.tags);
            
            menuItems.push({
                id: 'endpoints-section',
                title: 'Endpoints',
                icon: 'fa-plug',
                type: 'expandable',
                children: endpointsByTag
            });
        }

        // 5. MODELOS/SCHEMAS
        if (openApiSpec.components && openApiSpec.components.schemas) {
            menuItems.push({
                id: 'schemas-section',
                title: 'Modelos',
                icon: 'fa-cube',
                type: 'section'
            });
        }

        // 6. SECCIONES CMS (después de OpenAPI)
        if (this.cmsSeciones.length > 0) {
            this.cmsSeciones.forEach(seccion => {
                menuItems.push({
                    id: `cms-section-${seccion.contenidoId}`,
                    title: seccion.titulo,
                    icon: seccion.icono || 'fa-file-lines',
                    type: 'section',
                    isCMS: true
                });
            });
        }

        // Renderizar el menú
        this.renderMenu(menuItems);
    }

    /**
     * Agrupa endpoints por tags para el menú
     */
    groupEndpointsByTag(paths, tags) {
        const grouped = {};
        const tagDescriptions = {};
        
        // Mapear descripciones de tags
        if (tags) {
            tags.forEach(tag => {
                tagDescriptions[tag.name] = tag.description || tag.name;
            });
        }

        // Agrupar endpoints
        Object.entries(paths).forEach(([path, methods]) => {
            Object.entries(methods).forEach(([method, operation]) => {
                if (method === 'parameters') return;
                
                const tag = (operation.tags && operation.tags[0]) || 'General';
                
                if (!grouped[tag]) {
                    grouped[tag] = {
                        id: `tag-${this.slugify(tag)}`,
                        title: tagDescriptions[tag] || tag,
                        children: []
                    };
                }

                grouped[tag].children.push({
                    id: `operation-${method}-${this.slugify(path)}`,
                    title: operation.summary || path,
                    path: path,
                    method: method.toUpperCase(),
                    type: 'endpoint'
                });
            });
        });

        return Object.values(grouped);
    }

    /**
     * Renderiza el menú en el DOM
     */
    renderMenu(menuItems) {
        this.navContainer.innerHTML = '';

        menuItems.forEach(item => {
            const li = document.createElement('li');
            li.className = `docs-nav-item${item.type === 'expandable' ? ' expandable' : ''}`;

            const link = document.createElement('a');
            link.href = `#${item.id}`;
            link.className = 'docs-nav-link';
            link.dataset.section = item.id;
            
            // Icono
            const icon = document.createElement('i');
            icon.className = `fa-solid ${item.icon}`;
            link.appendChild(icon);
            
            // Texto
            const text = document.createTextNode(item.title);
            link.appendChild(text);

            li.appendChild(link);

            // Subitems (si los hay)
            if (item.children && item.children.length > 0) {
                const sublist = document.createElement('ul');
                sublist.className = 'docs-nav-sublist';

                item.children.forEach(child => {
                    const subli = document.createElement('li');
                    subli.className = 'docs-nav-subitem';

                    const sublink = document.createElement('a');
                    sublink.href = `#${child.id}`;
                    sublink.className = 'docs-nav-sublink';
                    sublink.dataset.section = child.id;

                    // Badge de método HTTP si es endpoint
                    if (child.type === 'endpoint' && child.method) {
                        const badge = document.createElement('span');
                        badge.className = `method-badge ${child.method.toLowerCase()}`;
                        badge.textContent = child.method;
                        sublink.appendChild(badge);
                    }

                    const subtext = document.createTextNode(child.title);
                    sublink.appendChild(subtext);

                    subli.appendChild(sublink);
                    sublist.appendChild(subli);
                });

                li.appendChild(sublist);
                
                // Click para expandir/contraer
                link.addEventListener('click', (e) => {
                    if (item.type === 'expandable') {
                        e.preventDefault();
                        li.classList.toggle('expanded');
                    }
                });
            }

            this.navContainer.appendChild(li);
        });

        console.log(`Menú generado con ${menuItems.length} items principales`);
    }

    /**
     * Configura el toggle para móvil
     */
    setupMobileToggle() {
        const toggleBtn = document.querySelector('.sidebar-toggle');
        const overlay = document.querySelector('.sidebar-overlay');

        if (!toggleBtn || !overlay) return;

        toggleBtn.addEventListener('click', () => {
            this.sidebar.classList.toggle('mobile-open');
            overlay.classList.toggle('active');
        });

        overlay.addEventListener('click', () => {
            this.sidebar.classList.remove('mobile-open');
            overlay.classList.remove('active');
        });

        // Cerrar al hacer click en un enlace (móvil)
        this.navContainer.addEventListener('click', (e) => {
            if (e.target.classList.contains('docs-nav-link') || 
                e.target.classList.contains('docs-nav-sublink')) {
                if (window.innerWidth <= 992) {
                    this.sidebar.classList.remove('mobile-open');
                    overlay.classList.remove('active');
                }
            }
        });
    }

    /**
     * Configura smooth scroll
     */
    setupSmoothScroll() {
        // Delegación de eventos en el contenedor de navegación
        this.navContainer.addEventListener('click', (e) => {
            const link = e.target.closest('a[href^="#"]');
            if (!link) return;
            
            const href = link.getAttribute('href');
            if (href === '#') return;

            e.preventDefault();
            
            const targetId = href.substring(1);
            let target = document.getElementById(targetId);
            
            // Si no se encuentra el elemento, intentar con querySelector más flexible
            if (!target) {
                // Buscar en Swagger UI por data-tag
                const tagElements = document.querySelectorAll('.swagger-ui .opblock-tag');
                for (const tagEl of tagElements) {
                    const tagName = tagEl.getAttribute('data-tag');
                    if (tagName && `tag-${tagName.toLowerCase().replace(/[^a-z0-9]/g, '-')}` === targetId) {
                        target = tagEl;
                        break;
                    }
                }
            }
            
            if (target) {
                const offsetTop = target.getBoundingClientRect().top + window.pageYOffset - 80;
                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth'
                });
                
                // Cerrar sidebar en móvil
                if (window.innerWidth <= 992) {
                    this.sidebar.classList.remove('mobile-open');
                    document.querySelector('.sidebar-overlay').classList.remove('active');
                }
            } else {
                console.warn(`No se encontró el elemento con ID: ${targetId}`);
            }
        });
    }

    /**
     * Configura scroll spy para destacar sección activa
     */
    setupScrollSpy() {
        let ticking = false;

        window.addEventListener('scroll', () => {
            if (!ticking) {
                window.requestAnimationFrame(() => {
                    this.updateActiveSection();
                    ticking = false;
                });
                ticking = true;
            }
        });
    }

    /**
     * Actualiza la sección activa en el menú
     */
    updateActiveSection() {
        const sections = document.querySelectorAll('[id]');
        const scrollPos = window.scrollY + 100; // Offset para activación temprana

        let currentSection = null;

        sections.forEach(section => {
            const sectionTop = section.offsetTop;
            const sectionHeight = section.offsetHeight;

            if (scrollPos >= sectionTop && scrollPos < sectionTop + sectionHeight) {
                currentSection = section.id;
            }
        });

        if (currentSection) {
            this.setActiveLink(currentSection);
        }
    }

    /**
     * Marca un enlace como activo
     */
    setActiveLink(sectionId) {
        // Remover clase active de todos
        document.querySelectorAll('.docs-nav-link, .docs-nav-sublink').forEach(link => {
            link.classList.remove('active');
        });

        // Agregar clase active al enlace correspondiente
        const activeLink = document.querySelector(`[data-section="${sectionId}"]`);
        if (activeLink) {
            activeLink.classList.add('active');

            // Si es un subitem, expandir su padre
            const parentItem = activeLink.closest('.docs-nav-item.expandable');
            if (parentItem) {
                parentItem.classList.add('expanded');
            }

            // Scroll suave del sidebar para mostrar el item activo
            this.scrollSidebarToActive(activeLink);
        }
    }

    /**
     * Hace scroll en el sidebar para mostrar el item activo
     */
    scrollSidebarToActive(activeLink) {
        const sidebarRect = this.sidebar.getBoundingClientRect();
        const linkRect = activeLink.getBoundingClientRect();

        if (linkRect.top < sidebarRect.top || linkRect.bottom > sidebarRect.bottom) {
            activeLink.scrollIntoView({
                behavior: 'smooth',
                block: 'nearest'
            });
        }
    }

    /**
     * Configura items expandibles
     */
    setupExpandableItems() {
        document.querySelectorAll('.docs-nav-item.expandable').forEach(item => {
            const link = item.querySelector('.docs-nav-link');
            
            link.addEventListener('click', (e) => {
                e.preventDefault();
                item.classList.toggle('expanded');
            });
        });
    }

    /**
     * Utilidad: convierte texto a slug
     */
    slugify(text) {
        return text
            .toString()
            .toLowerCase()
            .trim()
            .replace(/\s+/g, '-')
            .replace(/[^\w\-]+/g, '')
            .replace(/\-\-+/g, '-');
    }

    /**
     * Utilidad: copia link al portapapeles
     */
    copyLinkToClipboard(sectionId) {
        const url = `${window.location.origin}${window.location.pathname}#${sectionId}`;
        
        navigator.clipboard.writeText(url).then(() => {
            console.log('Link copiado:', url);
            // Aquí puedes mostrar un toast o notificación
        }).catch(err => {
            console.error('Error al copiar link:', err);
        });
    }
}

// Inicializar automáticamente cuando se carga el script
window.apiDocNav = new APIDocumentationNav();

// Función global para generar el menú desde la plantilla Thymeleaf
window.generateDocumentationMenu = function(openApiSpec, cmsSeciones) {
    if (window.apiDocNav) {
        window.apiDocNav.generateMenu(openApiSpec, cmsSeciones);
    }
};

// Función global para copiar link
window.copyDocLink = function() {
    const currentSection = document.querySelector('.docs-nav-link.active, .docs-nav-sublink.active');
    if (currentSection) {
        const sectionId = currentSection.dataset.section;
        if (window.apiDocNav) {
            window.apiDocNav.copyLinkToClipboard(sectionId);
        }
    }
};
