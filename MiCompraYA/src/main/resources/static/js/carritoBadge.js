document.addEventListener("DOMContentLoaded", () => {
    // --- ELEMENTOS GLOBALES ---
    const badge = document.getElementById("cart-count");

    // =====================================================================
    // FUNCIÓN PARA ACTUALIZAR EL RESUMEN DEL PEDIDO Y LA VISIBILIDAD
    // =====================================================================
    const actualizarResumenPedido = () => {
        const resumenDiv = document.getElementById('resumenPedido');
        const finalizarBtn = document.getElementById('finalizarBtn');
        const carritoVacioMensaje = document.getElementById('carritoVacioMensaje');

        // Solo se ejecuta si estamos en la página del carrito
        if (!resumenDiv) return;

        const totalSubtotalEl = document.getElementById('totalSubtotal');
        const totalFinalEl = document.getElementById('totalFinal');
        const filasProductos = document.querySelectorAll('.product-row');
        let totalGeneral = 0;

        filasProductos.forEach(fila => {
            const precio = parseFloat(fila.getAttribute('data-precio'));
            const cantidad = parseInt(fila.querySelector('.quantity-display').textContent);
            const subtotalFila = precio * cantidad;

            fila.querySelector('.subtotal').textContent = `$${subtotalFila.toFixed(2)}`;
            totalGeneral += subtotalFila;
        });

        totalSubtotalEl.textContent = `$${totalGeneral.toFixed(2)}`;
        totalFinalEl.textContent = `$${totalGeneral.toFixed(2)}`;

        // Gestiona la visibilidad usando la clase 'hidden' de Tailwind
        if (filasProductos.length > 0) {
            resumenDiv.classList.remove('hidden');
            if(finalizarBtn) finalizarBtn.disabled = false;
            if(carritoVacioMensaje) carritoVacioMensaje.classList.add('hidden');
        } else {
            resumenDiv.classList.add('hidden');
            if(finalizarBtn) finalizarBtn.disabled = true;
            if(carritoVacioMensaje) carritoVacioMensaje.classList.remove('hidden');
        }
    };

    // =====================================================================
    // FUNCIÓN PARA ACTUALIZAR EL BADGE DEL CARRITO
    // =====================================================================
    const actualizarBadge = async () => {
        try {
            if (!badge) return;
            const res = await fetch("/carrito/cantidad");
            const data = await res.json();
            badge.style.display = data.total > 0 ? "flex" : "none";
            badge.textContent = data.total;
        } catch (err) {
            console.error("Error actualizando badge:", err);
        }
    };

    // =====================================================================
    // ASIGNACIÓN DE EVENTOS
    // =====================================================================

    document.querySelectorAll(".agregar-carrito").forEach(boton => {
        boton.addEventListener("click", async () => {
            const id = boton.getAttribute("data-idproducto");
            await fetch(`/agregarCarrito/${id}`, { method: "POST" });
            actualizarBadge();
        });
    });

    document.querySelectorAll(".remove-btn").forEach(boton => {
        boton.addEventListener("click", async () => {
            const id = boton.getAttribute("data-product");
            await fetch(`/eliminarDelCarrito/${id}`, { method: "POST" });
            boton.closest(".product-row")?.remove();
            actualizarBadge();
            actualizarResumenPedido();
        });
    });

    const vaciarBtn = document.getElementById("vaciar-carrito");
    if (vaciarBtn) {
        vaciarBtn.addEventListener("click", async () => {
            await fetch("/carrito/vaciar", { method: "POST" });
            document.querySelectorAll(".product-row").forEach(fila => fila.remove());
            actualizarBadge();
            actualizarResumenPedido();
        });
    }

    document.querySelectorAll(".quantity-btn").forEach(boton => {
        boton.addEventListener("click", async () => {
            const action = boton.getAttribute("data-action");
            const productId = boton.getAttribute("data-product");
            const cantidadSpan = document.querySelector(`.quantity-display[data-product='${productId}']`);
            if (!cantidadSpan) return;

            const productRow = boton.closest(".product-row");
            const stock = parseInt(productRow.getAttribute("data-stock"));
            let cantidad = parseInt(cantidadSpan.textContent);

            if (action === "increase") {
                if (cantidad >= stock) {
                    alert("No puedes agregar más de este producto. Stock máximo alcanzado.");
                    return;
                }
                cantidad++;
            }

            if (action === "decrease") {
                cantidad = Math.max(cantidad - 1, 0);
            }

            cantidadSpan.textContent = cantidad;
            await fetch(`/actualizarCantidad/${productId}/${cantidad}`, { method: "POST" });

            if (cantidad === 0) {
                productRow.remove();
            }
            actualizarBadge();
            actualizarResumenPedido();
        });
    });

    // =====================================================================
    // LÓGICA DEL FORMULARIO DE PAGO
    // =====================================================================
    const metodoPago = document.getElementById("metodoPago");
    const mensajeEfectivo = document.getElementById("mensajeEfectivo");
    const formTarjeta = document.getElementById("formTarjeta");
    const fechaTarjeta = document.getElementById("fechaTarjeta");
    const finalizarBtn = document.getElementById("finalizarBtn");

    if (fechaTarjeta) {
        fechaTarjeta.addEventListener("input", (e) => {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 2) value = value.slice(0, 2) + '/' + value.slice(2, 4);
            e.target.value = value;
        });
    }

    if (metodoPago) {
        metodoPago.addEventListener("change", () => {
            const seleccion = metodoPago.value;
            if (seleccion === "efectivo") {
                mensajeEfectivo?.classList.remove("hidden");
                formTarjeta?.classList.add("hidden");
            } else if (seleccion === "tarjeta") {
                mensajeEfectivo?.classList.add("hidden");
                formTarjeta?.classList.remove("hidden");
            }
        });
    }

    if (finalizarBtn) {
        finalizarBtn.addEventListener("click", async () => {
            let tipoPagoId = null;

            // ✅ LÓGICA MODIFICADA: Comprobar la visibilidad de los divs
            // Usamos !.classList.contains('hidden') porque es la forma más fiable
            if (mensajeEfectivo && !mensajeEfectivo.classList.contains('hidden')) {
                tipoPagoId = 1; // ID para Efectivo
            } else if (formTarjeta && !formTarjeta.classList.contains('hidden')) {
                tipoPagoId = 2; // ID para Tarjeta
            }

            if (!tipoPagoId) {
                alert("Por favor, seleccione un método de pago válido.");
                return;
            }

            finalizarBtn.disabled = true;
            finalizarBtn.textContent = "Procesando...";

            try {
                const response = await fetch('/finalizar', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ tipoPagoId: tipoPagoId })
                });

                const result = await response.json();

                if (response.ok && result.success) {
                    window.location.href = `/pedido/exito?codigo=${result.codigoPedido}`;
                } else {
                    alert(`Error: ${result.message}`);
                    finalizarBtn.disabled = false;
                    finalizarBtn.textContent = "Finalizar compra";
                }
            } catch (error) {
                console.error("Error de conexión:", error);
                alert("Ocurrió un error de conexión.");
                finalizarBtn.disabled = false;
                finalizarBtn.textContent = "Finalizar compra";
            }
        });
    }

    // =====================================================================
    // INICIALIZACIÓN AL CARGAR LA PÁGINA
    // =====================================================================
    actualizarBadge();
    actualizarResumenPedido();
});