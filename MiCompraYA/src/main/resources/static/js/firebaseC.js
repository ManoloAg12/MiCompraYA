// --- TU CONFIGURACIÓN DE FIREBASE ---
const firebaseConfig = {
    apiKey: "AIzaSyA6Z-6nR2RVVqXZw265Uf76j-_vHhgSlPg",
    authDomain: "micompraya-51d89.firebaseapp.com",
    projectId: "micompraya-51d89",
    storageBucket: "micompraya-51d89.firebasestorage.app",
    messagingSenderId: "1051965234196",
    appId: "1:1051965234196:web:99a32778772c80bf05db01"
};

// Inicializar Firebase
firebase.initializeApp(firebaseConfig);
const auth = firebase.auth();
const provider = new firebase.auth.GoogleAuthProvider();

// Manejo del botón de Google
const btnGoogle = document.getElementById('btnGoogleLogin');

if (btnGoogle) {
    btnGoogle.addEventListener('click', () => {
        // Feedback visual: deshabilitar botón
        const originalText = btnGoogle.innerHTML;
        btnGoogle.disabled = true;
        btnGoogle.classList.add('opacity-75', 'cursor-not-allowed');

        auth.signInWithPopup(provider)
            .then((result) => {
                // Login exitoso en Google, obtenemos el token
                return result.user.getIdToken();
            })
            .then((token) => {
                // Enviamos el token a TU backend (Spring Boot)
                return fetch('/login/firebase', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ token: token })
                });
            })
            .then(response => {
                if (!response.ok) throw new Error('Error en el servidor');
                return response.json();
            })
            .then(data => {
                if (data.status === 'success' || data.status === 'new_user') {
                    // Redirigir según lo que diga el backend (Home o Completar Registro)
                    window.location.href = data.redirect;
                } else {
                    alert("Error: " + (data.message || "No se pudo iniciar sesión."));
                    // Restaurar botón
                    btnGoogle.disabled = false;
                    btnGoogle.classList.remove('opacity-75', 'cursor-not-allowed');
                }
            })
            .catch((error) => {
                console.error("Error en login Google:", error);
                // Restaurar botón
                btnGoogle.disabled = false;
                btnGoogle.classList.remove('opacity-75', 'cursor-not-allowed');
            });
    });
}