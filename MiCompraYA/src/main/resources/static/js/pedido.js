document.addEventListener("DOMContentLoaded", () => {
    // --- GLOBAL ELEMENTS & BOOTSTRAP INSTANCES ---
    const detalleModalEl = document.getElementById('detallePedidoModal');
    const confirmarModalEl = document.getElementById('confirmarCancelacionModal');

    // Initialize Bootstrap modals only if the elements exist on the page
    const detalleModal = detalleModalEl ? new bootstrap.Modal(detalleModalEl) : null;
    const confirmarModal = confirmarModalEl ? new bootstrap.Modal(confirmarModalEl) : null;

    /**
     * Shows a dynamic Bootstrap toast notification.
     * @param {string} message - The message to display.
     * @param {string} type - 'success' (green) or 'danger' (red).
     */
    const showToast = (message, type = 'success') => {
        const toastContainer = document.querySelector('.toast-container');
        if (!toastContainer) return;

        const toastId = `toast-${Date.now()}`;
        const bgColor = type === 'success' ? 'bg-success' : 'bg-danger';

        const toastHTML = `
            <div id="${toastId}" class="toast align-items-center text-white ${bgColor} border-0" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                    <div class="toast-body">${message}</div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>
        `;

        toastContainer.insertAdjacentHTML('beforeend', toastHTML);

        const newToastEl = document.getElementById(toastId);
        const bsToast = new bootstrap.Toast(newToastEl, { delay: 4000 });
        bsToast.show();

        // Clean up the toast from the DOM after it's hidden to prevent clutter
        newToastEl.addEventListener('hidden.bs.toast', () => {
            newToastEl.remove();
        });
    };

    // --- EVENT LISTENERS ---

    // 1. Event listener for the main DETAILS MODAL
    if (detalleModalEl) {
        // This event fires just before the details modal is shown
        detalleModalEl.addEventListener('show.bs.modal', async (event) => {
            const button = event.relatedTarget;
            const pedidoId = button.getAttribute('data-pedido-id');

            // Get references to all modal elements
            const modalTitle = detalleModalEl.querySelector('.modal-title');
            const modalBody = detalleModalEl.querySelector('#detallePedidoBody');
            const modalTotal = detalleModalEl.querySelector('#modalTotalPedido');
            const modalPago = detalleModalEl.querySelector('#modalMetodoPago');
            const btnCancelar = detalleModalEl.querySelector('#btnCancelarPedido');
            const modalSpinner = detalleModalEl.querySelector('#modalSpinner');
            const modalContent = detalleModalEl.querySelector('#modalContent');

            // Reset modal state and show spinner
            modalTitle.textContent = 'Cargando detalles...';
            modalContent.classList.add('hidden');
            modalSpinner.classList.remove('hidden');
            modalBody.innerHTML = '';
            btnCancelar.style.display = 'none'; // Hide cancel button by default
            btnCancelar.setAttribute('data-pedido-id-cancelar', pedidoId);

            try {
                // Fetch order details from the backend
                const response = await fetch(`/pedidos/detalles/${pedidoId}`);
                if (!response.ok) {
                    throw new Error('No se pudo cargar la información del pedido.');
                }
                const detalles = await response.json();

                // Populate modal with fetched data
                if (detalles.length > 0) {
                    const primerDetalle = detalles[0];
                    modalTitle.textContent = `Detalles del Pedido: ${primerDetalle.pedido.codigo}`;

                    // Show the cancel button only if the order is 'Pendiente' (ID 1)
                    if (primerDetalle.pedido.estadoPedido.id === 1) {
                        btnCancelar.style.display = 'block';
                    }

                    // Build and insert product rows
                    const rowsHTML = detalles.map(detalle => {
                        const nombreProducto = detalle.producto?.nombre ?? 'Producto no disponible';
                        const urlImagen = detalle.producto?.urlImagen ?? '/images/placeholder.png'; // Default image
                        const precioUnitario = detalle.precioUnitario?.toFixed(2) ?? '0.00';
                        const subtotal = detalle.subtotal?.toFixed(2) ?? '0.00';

                        return `
                            <tr>
                                <td>
                                    <div class="d-flex align-items-center">
                                        <img src="${urlImagen}" alt="${nombreProducto}" style="width: 50px; height: 50px; object-fit: cover;" class="rounded me-3">
                                        <span class="fw-bold">${nombreProducto}</span>
                                    </div>
                                </td>
                                <td class="text-center align-middle">${detalle.cantidad ?? 0}</td>
                                <td class="text-end align-middle">$${precioUnitario}</td>
                                <td class="text-end align-middle fw-bold">$${subtotal}</td>
                            </tr>
                        `;
                    }).join('');
                    modalBody.innerHTML = rowsHTML;

                    // Populate summary
                    modalTotal.textContent = `$${primerDetalle.pedido.total.toFixed(2, 'POINT')}`;
                    modalPago.textContent = primerDetalle.pedido.tipoPago.tipoPago;

                } else {
                    modalBody.innerHTML = '<tr><td colspan="4" class="text-center">Este pedido no tiene productos.</td></tr>';
                }

            } catch (error) {
                modalBody.innerHTML = `<tr><td colspan="4" class="text-center text-danger">${error.message}</td></tr>`;
            } finally {
                // Hide spinner and show final content
                modalSpinner.classList.add('hidden');
                modalContent.classList.remove('hidden');
            }
        });

        // 2. Event listener for the CANCEL button inside the details modal
        const btnCancelarPedido = document.getElementById('btnCancelarPedido');
        if (btnCancelarPedido) {
            btnCancelarPedido.addEventListener('click', (event) => {
                const pedidoId = event.currentTarget.getAttribute('data-pedido-id-cancelar');
                const btnConfirmar = document.getElementById('btnConfirmarCancelacion');

                // Pass the ID to the confirmation modal's button
                if(btnConfirmar) {
                    btnConfirmar.setAttribute('data-pedido-id-confirmar', pedidoId);
                }

                // Hide details modal and show confirmation modal
                if(detalleModal) detalleModal.hide();
                if(confirmarModal) confirmarModal.show();
            });
        }
    }

    // 3. Event listener for the final CONFIRMATION button
    const btnConfirmarCancelacion = document.getElementById('btnConfirmarCancelacion');
    if (btnConfirmarCancelacion) {
        btnConfirmarCancelacion.addEventListener('click', async (event) => {
            const pedidoId = event.currentTarget.getAttribute('data-pedido-id-confirmar');

            try {
                const response = await fetch(`/pedidos/cancelar/${pedidoId}`, { method: 'POST' });
                const result = await response.json();

                if (response.ok) {
                    if(confirmarModal) confirmarModal.hide();

                    // Find the correct order card on the page to update it dynamically
                    const pedidoCard = document.querySelector(`.order-card[data-pedido-id='${pedidoId}']`);
                    if (pedidoCard) {
                        const badge = pedidoCard.querySelector('.px-3.py-1.rounded-full');

                        // Only update the badge. Do not remove or disable any buttons.
                        if (badge) {
                            badge.textContent = 'Cancelado';
                            badge.className = 'px-3 py-1 text-sm font-semibold rounded-full bg-red-100 text-red-800';
                        }
                    }

                    showToast(result.message, 'success'); // Show success toast
                } else {
                    showToast(`Error: ${result.message}`, 'danger'); // Show error toast
                }
            } catch (error) {
                showToast('Error de conexión al cancelar el pedido.', 'danger');
            }
        });
    }
});