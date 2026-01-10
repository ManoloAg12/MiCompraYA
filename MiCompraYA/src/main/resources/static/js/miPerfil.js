// 1. Abrir Modal y Cargar Datos
async function abrirModalPerfil() {
    toggleModal('modalPerfil');
    // Resetear formulario visualmente
    document.getElementById('accionesPerfil').classList.add('hidden');
    document.querySelectorAll('#formPerfil input, #formPerfil textarea').forEach(i => {
        i.disabled = true;
        i.classList.add('bg-gray-50');
    });

    try {
        const res = await fetch('/perfil/datos');
        if (res.ok) {
            const data = await res.json();
            document.getElementById('perfilUsuario').value = data.nombreUsuario;
            document.getElementById('perfilCorreo').value = data.correo;
            document.getElementById('perfilNombre').value = data.nombreCompleto || '';
            document.getElementById('perfilTelefono').value = data.telefono || '';
            document.getElementById('perfilDireccion').value = data.direccion || '';
        }
    } catch (e) {
        console.error("Error cargando perfil", e);
    }
}

// 2. Habilitar Edición al hacer clic en el lápiz
function habilitarEdicion(idInput) {
    const input = document.getElementById(idInput);
    input.disabled = false;
    input.classList.remove('bg-gray-50');
    input.classList.add('bg-white');
    input.focus();

    // Mostrar botones de guardar
    document.getElementById('accionesPerfil').classList.remove('hidden');
}

// 3. Guardar Cambios
document.getElementById('formPerfil').addEventListener('submit', async (e) => {
    e.preventDefault();

    const datos = {
        nombreUsuario: document.getElementById('perfilUsuario').value,
        correo: document.getElementById('perfilCorreo').value,
        nombreCompleto: document.getElementById('perfilNombre').value,
        telefono: document.getElementById('perfilTelefono').value,
        direccion: document.getElementById('perfilDireccion').value
    };

    try {
        const res = await fetch('/perfil/actualizar', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(datos)
        });

        const result = await res.json();

        if (res.ok) {
            alert(result.message); // O usa tu showToast()
            location.reload(); // Recargar para actualizar nombre en header
        } else {
            alert("Error: " + result.message);
        }
    } catch (e) {
        console.error(e);
        alert("Error de conexión");
    }
});