// Funciones de autenticación
function isLoggedIn() {
    return localStorage.getItem('token') !== null;
}

function isAdmin() {
    const roles = JSON.parse(localStorage.getItem('roles') || '[]');
    // ✅ CORRECCIÓN: Verificar si roles es un array de objetos o strings
    if (Array.isArray(roles) && roles.length > 0) {
        // Si roles es un array de objetos como [{id: 2, name: "ADMIN"}]
        if (typeof roles[0] === 'object') {
            return roles.some(role => role.name === 'ADMIN' || role.name === 'ROLE_ADMIN');
        }
        // Si roles es un array de strings como ["ROLE_ADMIN"]
        return roles.includes('ROLE_ADMIN') || roles.includes('ADMIN');
    }
    return false;
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('roles');
    window.location.href = '/index.html';
}

function updateNavigation() {
    const authLinks = document.getElementById('authLinks');
    if (!authLinks) return;
    
    if (isLoggedIn()) {
        const username = localStorage.getItem('username');
        const isAdminUser = isAdmin();
        
        authLinks.innerHTML = `
            <span style="color: white; margin-right: 1rem;">Hola, ${username}</span>
            ${isAdminUser ? '<a href="/admin/dashboard.html">Admin</a>' : ''}
            <a href="#" onclick="logout()">Cerrar Sesión</a>
        `;
    } else {
        authLinks.innerHTML = `
            <a href="/login.html">Login</a>
            <a href="/register.html">Registro</a>
        `;
    }
}

// Funciones del carrito
function getCart() {
    return JSON.parse(localStorage.getItem('cart') || '[]');
}

function saveCart(cart) {
    localStorage.setItem('cart', JSON.stringify(cart));
    updateCartCount();
}

function updateCartCount() {
    const cart = getCart();
    const count = cart.reduce((sum, item) => sum + item.quantity, 0);
    const cartCountEl = document.getElementById('cartCount');
    if (cartCountEl) {
        cartCountEl.textContent = count;
    }
}

async function addToCart(productId) {
    try {
        const response = await fetch(`/api/products/${productId}`);
        if (!response.ok) {
            alert('Error al obtener el producto');
            return;
        }
        
        const product = await response.json();
        
        if (product.stock <= 0) {
            alert('Producto sin stock');
            return;
        }
        
        let cart = getCart();
        const existingItem = cart.find(item => item.id === productId);
        
        if (existingItem) {
            if (existingItem.quantity < product.stock) {
                existingItem.quantity++;
            } else {
                alert('No hay más stock disponible');
                return;
            }
        } else {
            cart.push({
                id: product.id,
                name: product.name,
                price: product.price,
                quantity: 1,
                imageUrl: product.imageUrl
            });
        }
        
        saveCart(cart);
        alert('Producto agregado al carrito');
    } catch (error) {
        console.error('Error:', error);
        alert('Error al agregar al carrito');
    }
}

function removeFromCart(productId) {
    let cart = getCart();
    cart = cart.filter(item => item.id !== productId);
    saveCart(cart);
    
    if (typeof loadCart === 'function') {
        loadCart();
    }
}

function updateQuantity(productId, quantity) {
    const qty = parseInt(quantity);
    if (qty <= 0) {
        removeFromCart(productId);
        return;
    }
    
    let cart = getCart();
    const item = cart.find(i => i.id === productId);
    if (item) {
        item.quantity = qty;
        saveCart(cart);
        
        if (typeof loadCart === 'function') {
            loadCart();
        }
    }
}

function clearCart() {
    localStorage.removeItem('cart');
    updateCartCount();
}

// Funciones de API con autenticación
async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem('token');
    const headers = { ...options.headers };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    // Solo añadir Content-Type si no es FormData y no está ya definido
    if (!(options.body instanceof FormData) && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(url, { ...options, headers });

    if (response.status === 401) {
        alert('Sesión expirada. Por favor inicia sesión nuevamente.');
        logout();
        return null;
    }

    return response;
}


// ✅ Función para verificar si el usuario necesita autenticación en esta página
function checkAuthRequired() {
    const path = window.location.pathname;

    const protectedPages = ['/cart.html', '/checkout.html'];
    const protectedDirs = ['/admin/'];

    if ((protectedPages.includes(path) || protectedDirs.some(dir => path.startsWith(dir))) && !isLoggedIn()) {
        alert('Debes iniciar sesión para acceder a esta página');
        window.location.href = '/login.html';
        return false;
    }

    if (path.startsWith('/admin/') && !isAdmin()) {
        alert('No tienes permisos para acceder a esta página');
        window.location.href = '/index.html';
        return false;
    }

    return true;
}

// Inicializar
document.addEventListener('DOMContentLoaded', () => {
    updateCartCount();
    updateNavigation();
    checkAuthRequired();
});