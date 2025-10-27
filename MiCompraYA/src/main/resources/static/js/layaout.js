//Sirve para el boton de despliegue del menu de mantenimiento
const btnMantenimiento = document.getElementById("btnMantenimiento");
const menuMantenimiento = document.getElementById("menuMantenimiento");

btnMantenimiento.addEventListener("click", () => {
    menuMantenimiento.classList.toggle("hidden");
});

// Para cerrar el menú al hacer clic fuera
document.addEventListener("click", (event) => {
    if (!btnMantenimiento.contains(event.target) && !menuMantenimiento.contains(event.target)) {
        menuMantenimiento.classList.add("hidden");
    }
});
//Sirve para el modal de login
function toggleModal(id = 'userModal') {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.toggle('hidden');
        modal.classList.toggle('flex');
    } else {
        console.warn(`Modal con id '${id}' no encontrado`);
    }
}

function toggleMenu() {
    const menu = document.getElementById('userMenu');
    menu.classList.toggle('hidden');
}

// Cierra el menú al hacer clic fuera
window.addEventListener('click', function(e) {
    const menu = document.getElementById('userMenu');
    if (!menu.contains(e.target) && !e.target.closest('button')) {
        menu.classList.add('hidden');
    }

});
