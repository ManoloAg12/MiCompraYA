//funcion para mostrar toast
$(document).ready(function() {
    const tipo = $('#tipo-toast').val();
    const mensaje = $('#mensaje-toast').val();

    if(tipo && mensaje) {

        const tipoToast = {
            'success': {titulo: 'Notificacion',
                subtitulo: 'registro almacenado',
                icono:'fa-regular fa-circle-check',
                font: 'text-light',
                color: 'bg-success',
            },
            'warning':{
                titulo: 'Advertencia',
                subtitulo: 'Advertencia',
                icono:'fa-solid fa-triangle-exclamation',
                font: 'text-dark',
                color: 'bg-warning',
            },
            'error':{
                titulo: 'Error',
                subtitulo: 'Registro no almacenado',
                icono:'fa-regular fa-circle-xmark',
                font: 'text-light',
                color: 'bg-danger',
            },
        };
        const {titulo, subtitulo, icono, font, color} = tipoToast[tipo] || {};

        const toastHTML = `
            <div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="width: 100%">
                <div class="toast-header ${font} ${color} ">
                    <span class="fs-5"><i class="${icono}"></i></span>
                    <span class="me-auto fs-5">&#160;&#160;${titulo}</span>
                    <small class="fs-6">${subtitulo}</small>
                    <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
                <div class="toast-body ${font} ${color} fs-5">
                    ${mensaje}
                </div>
            </div>
        `;
        $('#toast-container').append(toastHTML).show();

        $('#toast-container .toast:first').hide() .fadeTo(1000, 1) .slideDown(500);
        setTimeout(() => {
            $('#toast-container .toast:first').fadeTo(2000, 0).slideUp(500, function() {
                $(this).remove();
            });
        }, 2500);

    }
});