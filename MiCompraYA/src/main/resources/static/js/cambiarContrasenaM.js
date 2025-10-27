// --- Mostrar/Ocultar contraseñas ---
document.addEventListener('click', function (e) {
    if (e.target.classList.contains('toggle-pass')) {
        const inputId = e.target.getAttribute('data-target');
        const input = document.getElementById(inputId);
        if (input.type === 'password') {
            input.type = 'text';
            e.target.classList.remove('fa-eye');
            e.target.classList.add('fa-eye-slash');
        } else {
            input.type = 'password';
            e.target.classList.remove('fa-eye-slash');
            e.target.classList.add('fa-eye');
        }
    }
});

// --- Validación en tiempo real ---
const nueva = document.getElementById('nueva');
const confirmar = document.getElementById('confirmar');
const seguridad = document.getElementById('seguridad');
const coincidencia = document.getElementById('coincidencia');
const btnGuardar = document.getElementById('btnGuardar');

function validarCampos() {
    const pass1 = nueva.value;
    const pass2 = confirmar.value;
    let valido = true;

    // Validar longitud
    if (pass1.length < 8) {
        seguridad.textContent = "❌ Mínimo 8 caracteres";
        seguridad.className = "text-xs text-red-500 mt-1 block";
        valido = false;
    } else {
        seguridad.textContent = "✅ Longitud segura";
        seguridad.className = "text-xs text-green-600 mt-1 block";
    }

    // Validar coincidencia
    if (pass2.length > 0) {
        if (pass1 !== pass2) {
            coincidencia.textContent = "❌ Las contraseñas no coinciden";
            coincidencia.className = "text-xs text-red-500 mt-1 block";
            valido = false;
        } else {
            coincidencia.textContent = "✅ Las contraseñas coinciden";
            coincidencia.className = "text-xs text-green-600 mt-1 block";
        }
    } else {
        coincidencia.textContent = "";
    }

    btnGuardar.disabled = !valido;
}

nueva.addEventListener('input', validarCampos);
confirmar.addEventListener('input', validarCampos);
