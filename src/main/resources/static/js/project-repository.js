/**
 * PROJECT & REPOSITORY MANAGEMENT - INTERACTIVE FUNCTIONALITY
 * Professional GitHub/Drive-style interface with minimal design
 */

class ProjectRepositoryManager {
    constructor() {
        this.currentView = 'grid';
        this.currentFilter = 'all';
        this.isLoading = false;
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupViewToggle();
        this.setupFilters();
        this.setupSearch();
        this.setupModals();
        this.setupAnimations();
    }

    setupEventListeners() {
        // View toggle buttons
        document.querySelectorAll('.view-toggle button').forEach(btn => {
            btn.addEventListener('click', (e) => this.toggleView(e.target.dataset.view));
        });

        // Filter buttons (stat cards)
        document.querySelectorAll('.stat-card').forEach(card => {
            card.addEventListener('click', (e) => this.filterByType(card.dataset.filter));
        });

        // Create buttons
        document.querySelectorAll('.create-button').forEach(btn => {
            btn.addEventListener('click', (e) => this.openCreateModal(e));
        });

        // Item cards - DISABLED: Interfering with direct links
        // document.querySelectorAll('.item-card').forEach(card => {
        //     card.addEventListener('click', (e) => this.handleItemClick(e, card));
        // });

        // Action buttons
        document.querySelectorAll('.action-button').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleAction(e));
        });

        // Search input
        const searchInput = document.querySelector('.search-input');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => this.handleSearch(e.target.value));
        }

        // Filter form
        const filterForm = document.querySelector('.filters-form');
        if (filterForm) {
            filterForm.addEventListener('submit', (e) => this.handleFilterSubmit(e));
        }
    }

    setupViewToggle() {
        const gridView = document.querySelector('.items-grid');
        const listView = document.querySelector('.items-list');
        
        if (gridView && listView) {
            // Initially show grid view
            listView.style.display = 'none';
        }
    }

    setupFilters() {
        // Add active state to first stat card by default
        const firstStatCard = document.querySelector('.stat-card');
        if (firstStatCard) {
            firstStatCard.classList.add('active');
        }
    }

    setupSearch() {
        let searchTimeout;
        const searchInput = document.querySelector('.search-input');
        
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    this.performSearch(e.target.value);
                }, 300);
            });
        }
    }

    setupModals() {
        // Modal close buttons
        document.querySelectorAll('.modal-close').forEach(btn => {
            btn.addEventListener('click', (e) => this.closeModal(e.target.closest('.modal')));
        });

        // Click outside modal to close
        document.querySelectorAll('.modal').forEach(modal => {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    this.closeModal(modal);
                }
            });
        });

        // Cancel buttons
        document.querySelectorAll('.btn-cancel').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                this.closeModal(e.target.closest('.modal'));
            });
        });
    }

    setupAnimations() {
        // Add fade-in animation to cards on load
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('fade-in');
                }
            });
        }, { threshold: 0.1 });

        document.querySelectorAll('.item-card, .stat-card').forEach(card => {
            observer.observe(card);
        });
    }

    toggleView(view) {
        if (this.currentView === view) return;

        this.currentView = view;
        
        // Update active button
        document.querySelectorAll('.view-toggle button').forEach(btn => {
            btn.classList.remove('active');
        });
        document.querySelector(`[data-view="${view}"]`).classList.add('active');

        // Toggle views
        const gridView = document.querySelector('.items-grid');
        const listView = document.querySelector('.items-list');

        if (view === 'grid') {
            gridView.style.display = 'grid';
            listView.style.display = 'none';
        } else {
            gridView.style.display = 'none';
            listView.style.display = 'flex';
        }

        // Add animation
        const activeView = view === 'grid' ? gridView : listView;
        activeView.style.opacity = '0';
        activeView.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            activeView.style.transition = 'all 0.3s ease';
            activeView.style.opacity = '1';
            activeView.style.transform = 'translateY(0)';
        }, 50);
    }

    filterByType(filter) {
        if (this.currentFilter === filter) return;

        this.currentFilter = filter;

        // Update active stat card
        document.querySelectorAll('.stat-card').forEach(card => {
            card.classList.remove('active');
        });
        document.querySelector(`[data-filter="${filter}"]`).classList.add('active');

        // Show loading state
        this.showLoading();

        // Simulate API call (replace with actual API call)
        setTimeout(() => {
            this.filterItems(filter);
            this.hideLoading();
        }, 500);
    }

    filterItems(filter) {
        const items = document.querySelectorAll('.item-card, .item-row');
        
        items.forEach(item => {
            const itemType = item.dataset.type || 'all';
            
            if (filter === 'all' || itemType === filter) {
                item.style.display = '';
                item.classList.add('slide-in');
            } else {
                item.style.display = 'none';
                item.classList.remove('slide-in');
            }
        });

        this.updateEmptyState();
    }

    handleItemClick(e, card) {
        // Don't navigate if clicking on action buttons
        if (e.target.closest('.action-button') || e.target.closest('.item-actions')) {
            return;
        }

        const itemId = card.dataset.id;
        const itemType = card.dataset.type;
        
        // Add loading state to card
        card.style.opacity = '0.7';
        card.style.pointerEvents = 'none';

        // Navigate to item detail (replace with actual navigation)
        setTimeout(() => {
            if (itemType === 'project') {
                window.location.href = `/devportal/dev/username/projects/P-${itemId}`;
            } else {
                window.location.href = `/devportal/dev/username/repositories/R-${itemId}`;
            }
        }, 200);
    }

    handleAction(e) {
        // Only prevent default for buttons with actions, not for regular links
        const action = e.target.dataset.action;
        if (!action && e.target.tagName === 'A') {
            // This is a regular link (like the eye icon), let it work normally
            return;
        }
        
        e.preventDefault();
        e.stopPropagation();

        const itemId = e.target.closest('.item-card, .item-row').dataset.id;

        switch (action) {
            case 'edit':
                this.editItem(itemId);
                break;
            case 'delete':
                this.deleteItem(itemId);
                break;
            case 'clone':
                this.cloneItem(itemId);
                break;
            case 'view':
                this.viewItem(itemId);
                break;
            default:
                console.log(`Unknown action: ${action}`);
        }
    }

    handleSearch(query) {
        if (query.length < 2) {
            this.showAllItems();
            return;
        }

        const items = document.querySelectorAll('.item-card, .item-row');
        let visibleCount = 0;

        items.forEach(item => {
            const title = item.querySelector('.item-title, .row-title').textContent.toLowerCase();
            const description = item.querySelector('.item-description, .row-description')?.textContent.toLowerCase() || '';
            
            if (title.includes(query.toLowerCase()) || description.includes(query.toLowerCase())) {
                item.style.display = '';
                item.classList.add('slide-in');
                visibleCount++;
            } else {
                item.style.display = 'none';
                item.classList.remove('slide-in');
            }
        });

        this.updateEmptyState(visibleCount === 0 ? 'search' : null);
    }

    handleFilterSubmit(e) {
        e.preventDefault();
        
        const formData = new FormData(e.target);
        const filters = Object.fromEntries(formData.entries());
        
        this.showLoading();
        
        // Simulate API call
        setTimeout(() => {
            this.applyFilters(filters);
            this.hideLoading();
        }, 500);
    }

    performSearch(query) {
        this.handleSearch(query);
    }

    showAllItems() {
        document.querySelectorAll('.item-card, .item-row').forEach(item => {
            item.style.display = '';
            item.classList.add('slide-in');
        });
        this.updateEmptyState();
    }

    applyFilters(filters) {
        // Apply server-side filters (reload page with new parameters)
        const params = new URLSearchParams();
        Object.entries(filters).forEach(([key, value]) => {
            if (value) params.append(key, value);
        });
        
        const currentUrl = new URL(window.location);
        currentUrl.search = params.toString();
        window.location.href = currentUrl.toString();
    }

    openCreateModal(e) {
        e.preventDefault();
        const modalId = e.target.dataset.modal || 'createModal';
        const modal = document.getElementById(modalId);
        
        if (modal) {
            this.showModal(modal);
        }
    }

    showModal(modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
        
        // Focus first input
        const firstInput = modal.querySelector('input, select, textarea');
        if (firstInput) {
            setTimeout(() => firstInput.focus(), 100);
        }
    }

    closeModal(modal) {
        modal.classList.remove('active');
        document.body.style.overflow = '';
        
        // Reset form if exists
        const form = modal.querySelector('form');
        if (form) {
            form.reset();
        }
    }

    editItem(itemId) {
        // Navigate to edit page
        const itemType = document.querySelector(`[data-id="${itemId}"]`).dataset.type;
        const baseUrl = itemType === 'project' ? 'projects' : 'repositories';
        const prefix = itemType === 'project' ? 'P' : 'R';
        
        window.location.href = `/devportal/dev/username/${baseUrl}/${prefix}-${itemId}/edit`;
    }

    deleteItem(itemId) {
        if (confirm('¿Estás seguro de que quieres eliminar este elemento? Esta acción no se puede deshacer.')) {
            this.showLoading();
            
            // Simulate API call
            setTimeout(() => {
                const item = document.querySelector(`[data-id="${itemId}"]`);
                if (item) {
                    item.style.animation = 'fadeOut 0.3s ease-out';
                    setTimeout(() => {
                        item.remove();
                        this.updateEmptyState();
                    }, 300);
                }
                this.hideLoading();
            }, 500);
        }
    }

    cloneItem(itemId) {
        this.showLoading();
        
        // Simulate clone operation
        setTimeout(() => {
            this.hideLoading();
            this.showNotification('Elemento clonado exitosamente', 'success');
        }, 1000);
    }

    viewItem(itemId) {
        const itemType = document.querySelector(`[data-id="${itemId}"]`).dataset.type;
        const baseUrl = itemType === 'project' ? 'projects' : 'repositories';
        const prefix = itemType === 'project' ? 'P' : 'R';
        
        window.location.href = `/devportal/dev/username/${baseUrl}/${prefix}-${itemId}`;
    }

    showLoading() {
        this.isLoading = true;
        
        // Show loading overlay
        let overlay = document.querySelector('.loading-overlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.className = 'loading-overlay';
            overlay.innerHTML = `
                <div class="loading-spinner">
                    <div class="spinner"></div>
                    <p>Cargando...</p>
                </div>
            `;
            document.body.appendChild(overlay);
        }
        
        overlay.style.display = 'flex';
    }

    hideLoading() {
        this.isLoading = false;
        
        const overlay = document.querySelector('.loading-overlay');
        if (overlay) {
            overlay.style.display = 'none';
        }
    }

    updateEmptyState(type = null) {
        const visibleItems = document.querySelectorAll('.item-card:not([style*="display: none"]), .item-row:not([style*="display: none"])');
        const emptyState = document.querySelector('.empty-state');
        
        if (visibleItems.length === 0) {
            if (!emptyState) {
                this.createEmptyState(type);
            } else {
                emptyState.style.display = 'block';
                this.updateEmptyStateContent(emptyState, type);
            }
        } else if (emptyState) {
            emptyState.style.display = 'none';
        }
    }

    createEmptyState(type) {
        const container = document.querySelector('.items-grid') || document.querySelector('.items-list');
        if (!container) return;

        const emptyState = document.createElement('div');
        emptyState.className = 'empty-state';
        
        this.updateEmptyStateContent(emptyState, type);
        
        container.appendChild(emptyState);
    }

    updateEmptyStateContent(emptyState, type) {
        let content = '';
        
        switch (type) {
            case 'search':
                content = `
                    <div class="empty-icon">🔍</div>
                    <h3 class="empty-title">No se encontraron resultados</h3>
                    <p class="empty-description">
                        Intenta con otros términos de búsqueda o ajusta los filtros.
                    </p>
                `;
                break;
            default:
                content = `
                    <div class="empty-icon">📁</div>
                    <h3 class="empty-title">No hay elementos para mostrar</h3>
                    <p class="empty-description">
                        Comienza creando tu primer proyecto o repositorio.
                    </p>
                    <button class="btn btn-primary create-button">
                        Crear nuevo
                    </button>
                `;
        }
        
        emptyState.innerHTML = content;
    }

    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <span class="notification-message">${message}</span>
                <button class="notification-close">&times;</button>
            </div>
        `;
        
        document.body.appendChild(notification);
        
        // Auto remove after 5 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 5000);
        
        // Close button
        notification.querySelector('.notification-close').addEventListener('click', () => {
            notification.remove();
        });
        
        // Show with animation
        setTimeout(() => {
            notification.classList.add('show');
        }, 100);
    }
}

// Additional CSS for loading and notifications
const additionalStyles = `
<style>
.loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.9);
    display: none;
    align-items: center;
    justify-content: center;
    z-index: 9999;
}

.loading-spinner {
    text-align: center;
    color: var(--text-secondary);
}

.spinner {
    width: 40px;
    height: 40px;
    border: 3px solid var(--border-color);
    border-top: 3px solid var(--primary-color);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 1rem;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

@keyframes fadeOut {
    0% { opacity: 1; transform: translateY(0); }
    100% { opacity: 0; transform: translateY(-20px); }
}

.notification {
    position: fixed;
    top: 20px;
    right: 20px;
    background: var(--white);
    border-radius: var(--radius-md);
    box-shadow: var(--shadow-lg);
    border: 1px solid var(--border-color);
    min-width: 300px;
    z-index: 10000;
    transform: translateX(400px);
    transition: var(--transition);
    opacity: 0;
}

.notification.show {
    transform: translateX(0);
    opacity: 1;
}

.notification-content {
    padding: 1rem 1.5rem;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
}

.notification-message {
    color: var(--text-primary);
    font-weight: 500;
}

.notification-close {
    background: none;
    border: none;
    font-size: 1.25rem;
    color: var(--text-muted);
    cursor: pointer;
    padding: 0;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: var(--transition);
}

.notification-close:hover {
    background: var(--gray-100);
    color: var(--text-primary);
}

.notification-success {
    border-left: 4px solid var(--success-color);
}

.notification-error {
    border-left: 4px solid var(--danger-color);
}

.notification-warning {
    border-left: 4px solid var(--warning-color);
}

.notification-info {
    border-left: 4px solid var(--primary-color);
}
</style>
`;

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Add additional styles
    document.head.insertAdjacentHTML('beforeend', additionalStyles);
    
    // Initialize manager
    new ProjectRepositoryManager();
});

// Keyboard shortcuts
document.addEventListener('keydown', (e) => {
    // Ctrl/Cmd + K for search
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        const searchInput = document.querySelector('.search-input');
        if (searchInput) {
            searchInput.focus();
        }
    }
    
    // Escape to close modals
    if (e.key === 'Escape') {
        const activeModal = document.querySelector('.modal.active');
        if (activeModal) {
            activeModal.classList.remove('active');
            document.body.style.overflow = '';
        }
    }
});

// Export for use in other files
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ProjectRepositoryManager;
}