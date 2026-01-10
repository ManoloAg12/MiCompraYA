document.addEventListener("DOMContentLoaded", () => {
    // --- LÓGICA DEL MENÚ MANTENIMIENTO ---
    const btnMantenimiento = document.getElementById("btnMantenimiento");
    const menuMantenimiento = document.getElementById("menuMantenimiento");

    // ✅ SOLUCIÓN: Solo agregamos los eventos si el botón existe en la página
    if (btnMantenimiento && menuMantenimiento) {

        btnMantenimiento.addEventListener("click", () => {
            menuMantenimiento.classList.toggle("hidden");
        });

        // Para cerrar el menú al hacer clic fuera
        document.addEventListener("click", (event) => {
            if (!btnMantenimiento.contains(event.target) && !menuMantenimiento.contains(event.target)) {
                menuMantenimiento.classList.add("hidden");
            }
        });
    }

    // --- LÓGICA DEL MENÚ DE USUARIO (Cerrar al hacer clic fuera) ---
    window.addEventListener('click', function(e) {
        const menu = document.getElementById('userMenu');
        const btnUser = document.querySelector('button[onclick="toggleMenu()"]'); // Buscamos el botón que lo abre

        // Verificamos que el menú exista y que no sea el propio botón el que recibió el clic
        if (menu && !menu.classList.contains('hidden')) {
            if (!menu.contains(e.target) && (btnUser && !btnUser.contains(e.target))) {
                menu.classList.add('hidden');
            }
        }
    });
});

// --- FUNCIONES GLOBALES (Necesarias porque las llamas desde el HTML con onclick) ---

// Sirve para el modal de login / cambiar contraseña
function toggleModal(id = 'userModal') {
    const modal = document.getElementById(id);
    if (modal) {
        // Si usas clases de Tailwind para mostrar/ocultar (ej: hidden vs flex)
        if (modal.classList.contains('hidden')) {
            modal.classList.remove('hidden');
            modal.classList.add('flex'); // O block, según tu diseño
        } else {
            modal.classList.add('hidden');
            modal.classList.remove('flex');
        }
    } else {
        console.warn(`Modal con id '${id}' no encontrado`);
    }
}

function toggleMenu() {
    const menu = document.getElementById('userMenu');
    if (menu) {
        menu.classList.toggle('hidden');
    }
}