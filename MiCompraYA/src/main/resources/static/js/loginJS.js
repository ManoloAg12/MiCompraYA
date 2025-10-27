// Cambio entre pestañas de Login y Registro
document.addEventListener('DOMContentLoaded', function() {
    const loginTab = document.getElementById('loginTab');
    const registerTab = document.getElementById('registerTab');
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    loginTab.addEventListener('click', function() {
        loginTab.classList.add('tab-active');
        loginTab.classList.remove('tab-inactive');
        registerTab.classList.add('tab-inactive');
        registerTab.classList.remove('tab-active');
        loginForm.classList.add('active');
        registerForm.classList.remove('active');
    });

    registerTab.addEventListener('click', function() {
        registerTab.classList.add('tab-active');
        registerTab.classList.remove('tab-inactive');
        loginTab.classList.add('tab-inactive');
        loginTab.classList.remove('tab-active');
        registerForm.classList.add('active');
        loginForm.classList.remove('active');
    });
});

// Mostrar/ocultar contraseña
function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    const icon = document.getElementById('toggleIcon');

    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'ri-eye-off-line text-gray-400 hover:text-gray-600';
    } else {
        input.type = 'password';
        icon.className = 'ri-eye-line text-gray-400 hover:text-gray-600';
    }
}


// Verificar fortaleza de contraseña
function checkPasswordStrength(password) {
    const strengthBar = document.getElementById('passwordStrength');
    const strengthText = document.getElementById('strengthText');
    let strength = 0;
    let feedback = '';

    if (password.length >= 8) strength++;
    if (/[a-z]/.test(password)) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[^A-Za-z0-9]/.test(password)) strength++;

    strengthBar.className = 'password-strength bg-gray-200 rounded';

    if (strength <= 2) {
        strengthBar.classList.add('strength-weak');
        feedback = 'Contraseña débil';
    } else if (strength <= 3) {
        strengthBar.classList.add('strength-medium');
        feedback = 'Contraseña media';
    } else {
        strengthBar.classList.add('strength-strong');
        feedback = 'Contraseña segura';
    }

    strengthText.textContent = feedback;
}

// Validación de formularios
document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.querySelector('#registerForm form');
    const loginForm = document.querySelector('#loginForm form');
    const rememberMeCheckbox = document.getElementById('rememberMe');
    const loginUser = document.getElementById('usuario');
    const loginPassword = document.getElementById('contrasena');
    const rememberMessage = document.getElementById('rememberMessage');

    function showMessage(message) {
        rememberMessage.textContent = message;
        rememberMessage.style.opacity = '1';
        setTimeout(() => {
            rememberMessage.style.opacity = '0';
        }, 3000);
    }
    loginForm.addEventListener('submit', function() {
        saveCredentials();
    });


    function saveCredentials() {
        if (rememberMeCheckbox.checked) {
            localStorage.setItem('rememberedUser', loginUser.value);
            localStorage.setItem('rememberedPassword', loginPassword.value);
            showMessage('Tus credenciales serán recordadas');
        } else {
            localStorage.removeItem('rememberedUser');
            localStorage.removeItem('rememberedPassword');
        }
    }

    function loadCredentials() {
        const savedUser = localStorage.getItem('rememberedUser');
        const savedPassword = localStorage.getItem('rememberedPassword');

        if (savedUser && savedPassword) {
            loginUser.value = savedUser;
            loginPassword.value = savedPassword;
            rememberMeCheckbox.checked = true;
        }
    }



    loadCredentials();


    document.addEventListener("DOMContentLoaded", () => {
        const tipo = document.getElementById("tipo-toast")?.value;
        const mensaje = document.getElementById("mensaje-toast")?.value;

        if (tipo && mensaje) {
            mostrarToast(tipo, mensaje);
        }
    });

    function mostrarToast(tipo, mensaje) {
        const colores = {
            success: 'bg-green-500',
            error: 'bg-red-500',
            warning: 'bg-yellow-500',
            info: 'bg-blue-500'
        };

        const toast = document.createElement('div');
        toast.className = `fixed top-5 right-5 text-white px-6 py-3 rounded-lg shadow-lg ${colores[tipo] || 'bg-gray-500'} animate-fade-in z-50`;
        toast.innerHTML = `<div class="flex items-center gap-2">
        <i class="ri-information-line text-xl"></i>
        <span>${mensaje}</span>
    </div>`;

        document.body.appendChild(toast);

        // Desvanecer después de 4 segundos
        setTimeout(() => {
            toast.classList.add('opacity-0', 'transition-opacity', 'duration-700');
            setTimeout(() => toast.remove(), 700);
        }, 4000);
    }

    const telefonoInput = document.getElementById('telefono');
    telefonoInput.addEventListener('input', function () {
        this.value = this.value.replace(/[^0-9]/g, '');
    });

    const nombreCompletoInput = document.getElementById('nombreCompleto');
    const nombreUsuarioInput = document.getElementById('nombreUsuario');

    nombreCompletoInput.addEventListener('input', () => {
        let nombre = nombreCompletoInput.value.trim().toLowerCase();

        if (nombre.length === 0) {
            nombreUsuarioInput.value = '';
            return;
        }

        // Convertir nombre completo a formato "nombre.apellido" (solo primera palabra y última palabra)
        let partes = nombre.split(' ').filter(p => p.length > 0);
        let usuarioBase = partes[0]; // primer nombre
        if (partes.length > 1) {
            usuarioBase += '.' + partes[partes.length - 1]; // último apellido
        }

        // Agregar un número aleatorio de 2 dígitos para diferenciar
        let numeroRandom = Math.floor(Math.random() * 90 + 10); // entre 10 y 99

        // Ponerlo en el input de nombre de usuario
        nombreUsuarioInput.value = usuarioBase + numeroRandom;
    });
});