document.addEventListener('DOMContentLoaded', () => {
    // --- Elementos del Modal ---
    const modalActualizarStock = document.getElementById('modalActualizarStock');
    const inputIdProducto = document.getElementById('inputIdProducto');
    const btnBuscarProductoId = document.getElementById('btnBuscarProductoId');
    const mensajeBusquedaProducto = document.getElementById('mensajeBusquedaProducto');
    const detallesContainer = document.getElementById('detallesProductoContainer');
    const nombreProductoDisplay = document.getElementById('nombreProductoDisplay'); // Nuevo
    const stockActualDisplay = document.getElementById('stockActualDisplay');
    const selectEstadoProducto = document.getElementById('selectEstadoProducto');
    const inputCantidadAgregar = document.getElementById('inputCantidadAgregar');
    const btnGuardarActualizacion = document.getElementById('btnGuardarActualizacion');
    const btnCancelarActualizacion = document.getElementById('btnCancelarActualizacion');
    const hiddenProductoId = document.getElementById('hiddenProductoId'); // Nuevo input oculto

    // --- Función para cerrar y resetear el modal ---
    function closeModalActualizarStock() {
        if (modalActualizarStock) {
            inputIdProducto.value = ''; // Limpia input ID
            mensajeBusquedaProducto.textContent = ''; // Limpia mensaje
            mensajeBusquedaProducto.className = 'text-sm mt-1 h-5'; // Resetea clases mensaje
            detallesContainer.style.display = 'none'; // Oculta detalles
            nombreProductoDisplay.textContent = '--';
            stockActualDisplay.textContent = '--';
            selectEstadoProducto.value = '';
            inputCantidadAgregar.value = '0';
            hiddenProductoId.value = ''; // Limpia ID oculto
            btnGuardarActualizacion.disabled = true; // Deshabilita guardar
            modalActualizarStock.close();
        }
    }

    // --- Asignar Listener al Botón Cancelar ---
    if (btnCancelarActualizacion) {
        btnCancelarActualizacion.addEventListener('click', closeModalActualizarStock);
    }

    // --- Función para buscar producto por ID ---
    async function buscarProductoPorId() {
        const productoId = inputIdProducto.value.trim();

        // Resetear estado antes de buscar
        detallesContainer.style.display = 'none';
        btnGuardarActualizacion.disabled = true;
        hiddenProductoId.value = '';
        mensajeBusquedaProducto.textContent = 'Buscando...';
        mensajeBusquedaProducto.className = 'text-sm mt-1 h-5 text-gray-500'; // Estilo "cargando"

        if (!productoId) {
            mensajeBusquedaProducto.textContent = 'Por favor, ingrese un ID.';
            mensajeBusquedaProducto.className = 'text-sm mt-1 h-5 text-red-600';
            return;
        }

        try {
            const response = await fetch(`/productos/detalles/${productoId}`);

            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error(`Producto con ID ${productoId} no encontrado.`);
                } else {
                    const errorText = await response.text();
                    console.error("Respuesta no OK del servidor:", response.status, errorText);
                    throw new Error(`Error ${response.status} al buscar el producto.`);
                }
            }

            const contentType = response.headers.get("content-type");
            if (!contentType || !contentType.includes("application/json")) {
                const responseText = await response.text();
                console.error("La respuesta del servidor no es JSON:", responseText);
                throw new Error('Respuesta inesperada del servidor.');
            }

            const detalles = await response.json();

            // Rellenar campos si se encontró
            nombreProductoDisplay.textContent = detalles.nombreProducto || 'Nombre no disponible';
            stockActualDisplay.textContent = detalles.stockActual !== undefined ? detalles.stockActual : 'N/A';
            selectEstadoProducto.value = detalles.estadoIdActual !== undefined ? detalles.estadoIdActual : '';
            inputCantidadAgregar.value = '0'; // Siempre resetear cantidad a agregar
            hiddenProductoId.value = productoId; // Guardar el ID encontrado para el form

            // Validar estado (opcional, como antes)
            if (selectEstadoProducto.value === '') {
                console.warn(`El estadoIdActual (${detalles.estadoIdActual}) no es una opción válida.`);
            }

            // Mostrar detalles y habilitar guardado
            mensajeBusquedaProducto.textContent = 'Producto encontrado.';
            mensajeBusquedaProducto.className = 'text-sm mt-1 h-5 text-green-600';
            detallesContainer.style.display = 'block';
            btnGuardarActualizacion.disabled = false;

        } catch (error) {
            console.error("Error al buscar producto por ID:", error);
            mensajeBusquedaProducto.textContent = error.message; // Mostrar mensaje de error
            mensajeBusquedaProducto.className = 'text-sm mt-1 h-5 text-red-600';
            // Mantener detalles ocultos y botón deshabilitado
            detallesContainer.style.display = 'none';
            btnGuardarActualizacion.disabled = true;
            hiddenProductoId.value = '';
        }
    }

    // --- Asignar Listener al Botón Buscar ID ---
    if (btnBuscarProductoId) {
        btnBuscarProductoId.addEventListener('click', buscarProductoPorId);
    }

    // Opcional: Permitir buscar al presionar Enter en el input de ID
    if (inputIdProducto) {
        inputIdProducto.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                event.preventDefault(); // Prevenir envío del formulario (si está dentro de uno)
                buscarProductoPorId();
            }
        });
    }

}); // Fin DOMContentLoaded