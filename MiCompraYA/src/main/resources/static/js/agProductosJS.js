document.addEventListener("DOMContentLoaded", function() {

    // --- 1. Lógica para la Previsualización de la imagen ---
    const fileInput = document.querySelector('input[name="imagen"]');
    const imageContainer = document.querySelector('.border-2'); // Contenedor de la imagen

    // ✅ Solo ejecuta este bloque si los elementos necesarios existen
    if (fileInput && imageContainer) {
        const previewWrapper = document.createElement('div');
        previewWrapper.classList.add('mt-4', 'flex', 'justify-center');
        imageContainer.appendChild(previewWrapper);

        fileInput.addEventListener('change', function() {
            const file = this.files[0];
            if (file && file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    previewWrapper.innerHTML = `<img src="${e.target.result}" alt="Preview" class="max-h-48 rounded-lg">`;
                };
                reader.readAsDataURL(file);
            } else {
                previewWrapper.innerHTML = '';
            }
        });

        // Hacer clic en el contenedor para abrir el selector de archivos
        imageContainer.addEventListener('click', () => fileInput.click());
    }

    // --- 2. Lógica para el Contador de caracteres de la descripción ---
    const textarea = document.querySelector('textarea[name="descripcion"]');

    // ✅ Solo ejecuta si la textarea existe
    if (textarea) {
        const counter = textarea.nextElementSibling;

        // Y solo añade el listener si el contador también existe
        if (counter) {
            textarea.addEventListener('input', () => {
                const length = textarea.value.length;
                const max = textarea.getAttribute('maxlength');
                counter.textContent = `${length}/${max} caracteres`;
            });
        }
    }

    // --- 3. Lógica para Limpiar el formulario ---
    const clearButton = document.querySelector('button[type="button"]'); // Botón Limpiar
    const form = document.querySelector('form');

    // ✅ Solo ejecuta si el botón y el formulario existen
    if (clearButton && form) {
        clearButton.addEventListener('click', () => {
            form.reset(); // Limpiar todos los inputs y selects

            // Limpiar la previsualización de la imagen (si existe)
            const previewWrapper = imageContainer?.querySelector('.flex');
            if (previewWrapper) {
                previewWrapper.innerHTML = '';
            }

            // Resetear el contador de caracteres (si existe)
            if (textarea) {
                const counter = textarea.nextElementSibling;
                if(counter) {
                    counter.textContent = `0/${textarea.getAttribute('maxlength')} caracteres`;
                }
            }
        });
    }
});