document.addEventListener("DOMContentLoaded", () => {

    // --- LÓGICA PARA EL MODAL DE EDICIÓN ---

    // 1. Asignar eventos a los botones de "Editar" que abren el modal
    document.querySelectorAll(".btn-editar").forEach(btn => {
        btn.addEventListener("click", () => {
            const usuarioId = btn.getAttribute("data-id");
            fetch(`/usuarios/${usuarioId}`)
                .then(response => response.ok ? response.json() : Promise.reject("Error en la respuesta"))
                .then(usuario => {
                    document.getElementById("editId").value = usuario.id;
                    document.getElementById("editUsuario").value = usuario.nombreUsuario;
                    document.getElementById("editCorreo").value = usuario.correo;
                    document.getElementById("editRol").value = usuario.rol.id;
                    document.getElementById("editEstado").value = usuario.estado.id;
                })
                .catch(error => console.error("Error al obtener usuario:", error));
        });
    });

    // 2. Manejar el clic del botón "Guardar Cambios" DENTRO del modal
    const btnGuardarCambios = document.getElementById("btnGuardarCambios");

    // ✅ Comprobación: Solo añade el listener si el botón existe
    if (btnGuardarCambios) {
        btnGuardarCambios.addEventListener("click", () => {
            const usuarioId = document.getElementById("editId").value;
            const usuario = {
                id: parseInt(usuarioId),
                nombreUsuario: document.getElementById("editUsuario").value,
                correo: document.getElementById("editCorreo").value,
                rol: { id: parseInt(document.getElementById("editRol").value) },
                estado: { id: parseInt(document.getElementById("editEstado").value) }
            };

            fetch("/usuarios/editar", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(usuario)
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        // Cierra el modal de forma segura
                        const modalEl = document.getElementById("editarModal");
                        if (modalEl) {
                            const modal = bootstrap.Modal.getInstance(modalEl);
                            if (modal) modal.hide();
                        }

                        showToast("Usuario actualizado correctamente ✅", "success");

                        // Actualizar la fila en la tabla dinámicamente
                        const fila = document.querySelector(`tr[data-id='${usuario.id}']`);
                        if (fila) {
                            fila.querySelector(".col-usuario").textContent = usuario.nombreUsuario;
                            fila.querySelector(".col-correo").textContent = usuario.correo;
                            // Obtener el texto del 'option' seleccionado
                            const rolSelect = document.getElementById("editRol");
                            const estadoSelect = document.getElementById("editEstado");
                            fila.querySelector(".col-rol").textContent = rolSelect.selectedOptions[0].text;
                            fila.querySelector(".col-estado").textContent = estadoSelect.selectedOptions[0].text;
                        }
                    } else {
                        showToast("Error al guardar cambios ❌", "error");
                    }
                })
                .catch(error => console.error("Error al editar usuario:", error));
        });
    }

    // --- LÓGICA PARA EL MODAL DE ELIMINAR/INACTIVAR ---

    let idUsuarioEliminar = null;
    const btnConfirmarEliminar = document.getElementById("btnConfirmarEliminar");

    // 1. Asignar eventos a los botones que abren el modal de eliminación
    document.querySelectorAll('[data-bs-target="#eliminarModal"]').forEach(btn => {
        btn.addEventListener("click", () => {
            idUsuarioEliminar = btn.getAttribute("data-id");
        });
    });

    // 2. Manejar el clic del botón "Confirmar Eliminar" DENTRO del modal
    // ✅ Comprobación: Solo añade el listener si el botón existe
    if (btnConfirmarEliminar) {
        btnConfirmarEliminar.addEventListener("click", async () => {
            if (!idUsuarioEliminar) return;

            try {
                const response = await fetch(`/usuarios/inactivar/${idUsuarioEliminar}`, {
                    method: "POST" // O PUT, según tu backend
                });
                const data = await response.json();

                // Cierra el modal
                const modalEl = document.getElementById("eliminarModal");
                if (modalEl) {
                    const modal = bootstrap.Modal.getInstance(modalEl);
                    if (modal) modal.hide();
                }

                if (data.success) {
                    showToast("✅ " + data.message, "success");
                    // Opcional: Recargar o eliminar la fila de la tabla dinámicamente
                    setTimeout(() => window.location.reload(), 1500);
                } else {
                    showToast("⚠️ " + data.message, "error");
                }
            } catch (error) {
                showToast("❌ Error de conexión con el servidor", "error");
            }
        });
    }

    // --- FUNCIÓN UTILITARIA PARA MOSTRAR TOASTS ---
    function showToast(message, type = "success") {
        const toastContainer = document.getElementById("toast-container");
        if (!toastContainer) return; // No hacer nada si el contenedor no existe

        const bgColor = {
            success: "bg-green-600",
            error: "bg-red-600",
            info: "bg-blue-600"
        }[type] || "bg-gray-600";

        const toast = document.createElement("div");
        toast.className = `text-white px-4 py-3 rounded-lg shadow-lg mb-2 animate-slide-in`;
        toast.style.backgroundColor = bgColor.startsWith('bg-') ? '' : bgColor; // Para Tailwind vs. colores directos
        if (bgColor.startsWith('bg-')) toast.classList.add(...bgColor.split(' '));

        toast.textContent = message;
        toastContainer.appendChild(toast);

        setTimeout(() => {
            toast.classList.add("animate-fade-out");
            setTimeout(() => toast.remove(), 500);
        }, 3000);
    }
});