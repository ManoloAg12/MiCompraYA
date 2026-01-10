document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('modalEditarProducto');
    const btnCerrar = document.getElementById('btnCerrarEditar');
    const btnBuscar = document.getElementById('btnBuscarParaEditar');
    const inputId = document.getElementById('inputBuscarIdEditar');
    const form = document.getElementById('formEditarProducto');
    const msg = document.getElementById('msgEditar');

    // Inputs del form
    const hiddenId = document.getElementById('hiddenIdEditar');
    const nombre = document.getElementById('editNombre');
    const desc = document.getElementById('editDescripcion');
    const caducidad = document.getElementById('editCaducidad');
    const stock = document.getElementById('editStockReadOnly');
    const imgPreview = document.getElementById('imgPreviewEditar');
    const fileInput = document.getElementById('fileInputEditar');


    // Cerrar Modal
    if(btnCerrar) {
        btnCerrar.addEventListener('click', () => {
            modal.close();
            form.style.display = 'none';
            inputId.value = '';
            msg.textContent = '';
        });
    }

    // Dentro del DOMContentLoaded...
    const editCaducidad = document.getElementById('editCaducidad');
    if (editCaducidad) {
        const hoy = new Date().toISOString().split('T')[0];
        editCaducidad.setAttribute('min', hoy);
    }

    // Buscar Producto
    if(btnBuscar) {
        btnBuscar.addEventListener('click', async () => {
            const id = inputId.value;
            if(!id) return;

            msg.textContent = "Buscando...";
            msg.className = "text-sm mb-4 h-5 text-gray-500";
            form.style.display = 'none';

            try {
                const res = await fetch(`/productos/detalles/${id}`);
                if(res.ok) {
                    const data = await res.json();

                    // Rellenar Formulario
                    hiddenId.value = id;
                    nombre.value = data.nombreProducto;
                    desc.value = data.descripcion || '';
                    caducidad.value = data.caducidad || '';
                    stock.value = data.stockActual;

                    const selectMarca = document.getElementById('editMarca');
                    const selectCategoria = document.getElementById('editCategoria');

                    if(selectMarca) selectMarca.value = data.marcaId;
                    if(selectCategoria) selectCategoria.value = data.categoriaId;

                    // Imagen
                    if(data.urlImagen) {
                        imgPreview.src = data.urlImagen;
                        imgPreview.classList.remove('hidden');
                    } else {
                        imgPreview.src = '/images/placeholder.png'; // O una imagen por defecto
                    }
                    // Lógica para marcar el checkbox si ya es estado 3 (Descontinuado)
                    const checkDescontinuar = document.getElementById('checkDescontinuar');
                    if (checkDescontinuar) {
                        // Si el estado actual es 3, marcamos el check. Si es 1 o 2, lo desmarcamos.
                        checkDescontinuar.checked = (data.estadoIdActual === 3);
                    }

                    form.style.display = 'block';
                    msg.textContent = "Producto encontrado.";
                    msg.className = "text-sm mb-4 h-5 text-green-600";
                } else {
                    msg.textContent = "Producto no encontrado.";
                    msg.className = "text-sm mb-4 h-5 text-red-600";
                }
            } catch(e) {
                console.error(e);
                msg.textContent = "Error de conexión.";
            }
        });
    }





    // Previsualizar nueva imagen al seleccionar
    if(fileInput) {
        fileInput.addEventListener('change', function() {
            const file = this.files[0];
            if(file) {
                const reader = new FileReader();
                reader.onload = (e) => imgPreview.src = e.target.result;
                reader.readAsDataURL(file);
            }
        });
    }
});