document.addEventListener('DOMContentLoaded', () => {
    const emailInput = document.getElementById('correoRecuperacion');
    const submitButton = document.querySelector('button[type="submit"]');
    let debounceTimeout; // Para evitar exceso de peticiones

    // Deshabilitar botón al inicio
    submitButton.disabled = true;

    emailInput.addEventListener('input', () => {
        // Limpiar timeout anterior si existe
        clearTimeout(debounceTimeout);

        const email = emailInput.value.trim();

        // Resetear estilos y deshabilitar botón mientras se escribe/valida
        emailInput.classList.remove('border-green-500', 'focus:ring-green-500', 'border-red-500', 'focus:ring-red-500');
        emailInput.classList.add('border-gray-300', 'focus:border-primary', 'focus:ring-primary'); // Clases por defecto
        submitButton.disabled = true;
        // Opcional: añadir un icono de carga/spinner

        if (email.length < 5 || !email.includes('@') || !email.includes('.')) {
            // No hacer nada si no parece un correo válido aún
            return;
        }

        // Esperar 500ms después de que el usuario deje de escribir
        debounceTimeout = setTimeout(() => {
            verificarCorreo(email);
        }, 500);
    });

    async function verificarCorreo(correo) {
        try {
            const response = await fetch('/verificar-correo', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    // Si usas Spring Security con CSRF, necesitarás añadir el token CSRF aquí
                    // 'X-CSRF-TOKEN': 'valor_del_token'
                },
                body: JSON.stringify({ correo: correo })
            });

            if (!response.ok) {
                throw new Error('Error en la respuesta del servidor');
            }

            const data = await response.json();

            // Quitar clases de borde/foco por defecto antes de añadir las nuevas
            emailInput.classList.remove('border-gray-300', 'focus:border-primary', 'focus:ring-primary');

            if (data.existe) {
                // Correo existe: Borde verde y habilitar botón
                emailInput.classList.add('border-green-500', 'focus:ring-green-500');
                emailInput.classList.remove('border-red-500', 'focus:ring-red-500');
                submitButton.disabled = false;
                // Opcional: quitar icono de carga/spinner
            } else {
                // Correo NO existe: Borde rojo y mantener botón deshabilitado
                emailInput.classList.add('border-red-500', 'focus:ring-red-500');
                emailInput.classList.remove('border-green-500', 'focus:ring-green-500');
                submitButton.disabled = true;
                // Opcional: quitar icono de carga/spinner
            }

        } catch (error) {
            console.error('Error al verificar el correo:', error);
            // Opcional: Mostrar un mensaje de error genérico al usuario
            emailInput.classList.remove('border-green-500', 'focus:ring-green-500', 'border-red-500', 'focus:ring-red-500');
            emailInput.classList.add('border-gray-300', 'focus:border-primary', 'focus:ring-primary');
            submitButton.disabled = true; // Mantener deshabilitado en caso de error
        }
    }
});