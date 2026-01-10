$(document).ready(function() {
    const tipo = $('#tipo-toast').val();
    const mensaje = $('#mensaje-toast').val();
    const toastContainer = $('.toast-container'); // Selecciona el contenedor Bootstrap

    if(tipo && mensaje && toastContainer.length > 0) { // Verifica que el contenedor exista

        // Configuraciones de estilo mejoradas (puedes ajustar)
        const tipoToastConfig = {
            'success': {
                titulo: 'Éxito',
                icono: 'fa-solid fa-circle-check', // Icono sólido
                colorClasses: 'bg-success text-white', // Clases Bootstrap para fondo y texto
                subtitulo: '' // Subtítulo opcional
            },
            'warning':{
                titulo: 'Advertencia',
                icono: 'fa-solid fa-triangle-exclamation',
                colorClasses: 'bg-warning text-dark', // Texto oscuro para contraste
                subtitulo: ''
            },
            'error':{
                titulo: 'Error',
                icono: 'fa-solid fa-circle-xmark',
                colorClasses: 'bg-danger text-white',
                subtitulo: ''
            },
            'info': { // Añadimos un tipo 'info'
                titulo: 'Información',
                icono: 'fa-solid fa-circle-info',
                colorClasses: 'bg-info text-white',
                subtitulo: ''
            }
        };

        // Obtener configuración o usar una por defecto
        const config = tipoToastConfig[tipo] || {
            titulo: 'Notificación',
            icono: 'fa-solid fa-bell',
            colorClasses: 'bg-secondary text-white',
            subtitulo: ''
        };

        // Crear el HTML del toast con estructura Bootstrap 5 mejorada
        const toastHTML = `
            <div class="toast align-items-center ${config.colorClasses} border-0 fade" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="5000">
                <div class="d-flex">
                    <div class="toast-body d-flex align-items-center">
                        <i class="${config.icono} fs-4 me-2"></i> ${mensaje}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>
        `;

        // Convertir el HTML string a un elemento jQuery
        const $toastElement = $(toastHTML);

        // Añadir el nuevo toast al contenedor
        toastContainer.append($toastElement);

        // Crear la instancia de Bootstrap Toast usando el elemento DOM
        const bootstrapToast = new bootstrap.Toast($toastElement[0]); // Se accede al elemento DOM con [0]

        // Mostrar el toast usando la API de Bootstrap
        bootstrapToast.show();

        // Limpiar el elemento del DOM después de que se oculte (usando evento Bootstrap)
        $toastElement.on('hidden.bs.toast', function () {
            $(this).remove(); // Usar $(this) dentro del listener de jQuery
        });

    }
});