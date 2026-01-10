document.addEventListener("DOMContentLoaded", function() {

    // --- 1. Lógica para la Imagen (Ocultar texto y mostrar solo foto) ---
    const fileInput = document.querySelector('input[name="imagen"]');
    const imageContainer = document.querySelector('.border-2'); // El recuadro punteado

    // Variables para controlar lo que se muestra y se oculta
    let uploadContent = null; // El texto, icono y botón
    let previewWrapper = null; // Donde pondremos la foto

    if (fileInput && imageContainer) {
        // Identificamos el div interno que tiene el texto y el icono
        uploadContent = imageContainer.querySelector('div.flex.flex-col');

        // Creamos el contenedor para la imagen (inicialmente oculto)
        previewWrapper = document.createElement('div');
        previewWrapper.classList.add('hidden', 'w-full', 'h-full', 'flex', 'justify-center', 'items-center');
        imageContainer.appendChild(previewWrapper);

        fileInput.addEventListener('change', function() {
            const file = this.files[0];
            if (file && file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    // A. Ocultamos el texto y el botón original
                    if (uploadContent) uploadContent.classList.add('hidden');

                    // B. Mostramos la imagen ocupando el espacio
                    previewWrapper.innerHTML = `<img src="${e.target.result}" alt="Preview" class="max-h-64 w-auto object-contain rounded-lg shadow-sm">`;
                    previewWrapper.classList.remove('hidden');

                    // Opcional: Reducir el padding del contenedor para que la imagen se vea más grande
                    imageContainer.classList.remove('p-8');
                    imageContainer.classList.add('p-2');
                    // Cambiar borde a sólido para indicar "listo"
                    imageContainer.classList.remove('border-dashed', 'border-gray-300');
                    imageContainer.classList.add('border-solid', 'border-green-500');
                };
                reader.readAsDataURL(file);
            } else {
                // Si no es imagen válida, reseteamos
                resetImageUpload();
            }
        });

        // Permitir clic en todo el contenedor para cambiar la imagen
        imageContainer.addEventListener('click', (e) => {
            // Evitamos bucle infinito si el clic viene del input mismo
            if (e.target !== fileInput) {
                fileInput.click();
            }
        });
    }

    // Función para restaurar el estado original (texto visible, imagen oculta)
    function resetImageUpload() {
        if (fileInput) fileInput.value = '';

        if (previewWrapper) {
            previewWrapper.innerHTML = '';
            previewWrapper.classList.add('hidden');
        }

        if (uploadContent) {
            uploadContent.classList.remove('hidden');
        }

        if (imageContainer) {
            // Restaurar estilos originales
            imageContainer.classList.add('p-8', 'border-dashed', 'border-gray-300');
            imageContainer.classList.remove('p-2', 'border-solid', 'border-green-500');
        }
    }

    // --- 2. Contador de Caracteres ---
    const textarea = document.querySelector('textarea[name="descripcion"]');
    if (textarea) {
        const counter = textarea.nextElementSibling;
        if (counter) {
            textarea.addEventListener('input', () => {
                counter.textContent = `${textarea.value.length}/${textarea.getAttribute('maxlength')} caracteres`;
            });
        }
    }

    // --- 3. Limpiar Formulario (Reset completo) ---
    const clearButton = document.getElementById('btnLimpiar');
    const form = document.querySelector('form');

    if (clearButton && form) {
        clearButton.addEventListener('click', () => {
            form.reset();

            // 1. Restaurar visualmente el área de carga de imagen
            resetImageUpload();

            // 2. Resetear contador de caracteres
            if (textarea && textarea.nextElementSibling) {
                textarea.nextElementSibling.textContent = `0/${textarea.getAttribute('maxlength')} caracteres`;
            }
        });
    }
    const fechaInput = document.querySelector('input[name="caducidad"]');
    if (fechaInput) {
        const hoy = new Date().toISOString().split('T')[0];
        fechaInput.setAttribute('min', hoy);
    }
});