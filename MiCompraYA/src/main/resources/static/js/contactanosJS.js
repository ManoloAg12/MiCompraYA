document.addEventListener("DOMContentLoaded", () => {
    // --- ELEMENTOS GLOBALES ---
    const responderModalEl = document.getElementById('responderModal');
    // Ya no necesitamos el modal de confirmación ni su botón

    // --- LÓGICA DE EVENTOS ---

    // 1. Abrir el modal de respuesta y pre-llenar campos
    document.querySelectorAll('.reply-button').forEach(button => {
        button.addEventListener('click', (event) => {
            if (!responderModalEl) return;

            const btn = event.currentTarget;
            const recipient = btn.getAttribute('data-reply-to');
            const subject = btn.getAttribute('data-reply-subject');
            const mensajeId = btn.getAttribute('data-mensaje-id');

            // Llenar los campos del formulario en el modal
            const replyToInput = responderModalEl.querySelector('#replyTo');
            const replySubjectInput = responderModalEl.querySelector('#replySubject');
            const replyMensajeIdInput = responderModalEl.querySelector('#replyMensajeId');
            const replyBodyTextarea = responderModalEl.querySelector('#replyBody');

            if (replyToInput) replyToInput.value = recipient;
            if (replySubjectInput) replySubjectInput.value = subject;
            if (replyMensajeIdInput) replyMensajeIdInput.value = mensajeId;
            if (replyBodyTextarea) {
                replyBodyTextarea.value = ''; // Limpiar respuesta anterior
                replyBodyTextarea.focus(); // Poner el foco en el área de texto
            }
        });
    });

    // Ya no se necesita el listener para el formulario ni para el botón de confirmación.
    // El envío del formulario ahora es manejado por el navegador.
});