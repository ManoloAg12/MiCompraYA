document.addEventListener('DOMContentLoaded', () => {
    const startScanButton = document.getElementById('startScanButton');
    const stopScanButton = document.getElementById('stopScanButton');
    const qrReaderContainer = document.getElementById('qr-reader-container');
    const qrReaderElement = document.getElementById('qr-reader');
    const codigoInput = document.getElementById('codigo');
    const buscarForm = document.getElementById('buscarForm');
    let html5QrCode = null; // Variable para mantener la instancia del escáner

    // --- Función para iniciar el escaneo ---
    function startScanning() {
        // Mostrar el contenedor del lector y ocultar el botón de inicio
        qrReaderContainer.style.display = 'block';
        startScanButton.style.display = 'none'; // Opcional: ocultar botón "Escanear"

        // Crear instancia del escáner
        html5QrCode = new Html5Qrcode("qr-reader");

        // Configuración básica (puedes ajustar fps, qrbox, etc.)
        const config = { fps: 10, qrbox: { width: 250, height: 250 } };

        // Función que se llama cuando se decodifica un QR
        const qrCodeSuccessCallback = (decodedText, decodedResult) => {
            console.log(`Código escaneado: ${decodedText}`);

            // Rellenar el input con el código
            codigoInput.value = decodedText.toUpperCase(); // Convertir a mayúsculas si es necesario

            // Detener el escaneo
            stopScanning();

            // Enviar el formulario automáticamente
            buscarForm.submit();
        };

        // Función para manejar errores (opcional pero útil)
        const qrCodeErrorCallback = (errorMessage) => {
            // Puedes ignorar errores comunes como "QR code not found"
            // console.warn(`QR Error: ${errorMessage}`);
        };

        // Iniciar el escaneo usando la cámara trasera preferiblemente
        html5QrCode.start({ facingMode: "environment" }, config, qrCodeSuccessCallback, qrCodeErrorCallback)
            .catch((err) => {
                console.error("Error al iniciar el escáner:", err);
                alert(`No se pudo iniciar la cámara. Verifica los permisos o si otra aplicación la está usando. Error: ${err}`);
                stopScanning(); // Asegura limpiar si falla el inicio
            });
    }

    // --- Función para detener el escaneo ---
    function stopScanning() {
        if (html5QrCode && html5QrCode.isScanning) {
            html5QrCode.stop().then(() => {
                console.log("Escaneo detenido.");
                qrReaderContainer.style.display = 'none'; // Ocultar contenedor
                startScanButton.style.display = 'inline-flex'; // Mostrar botón "Escanear" de nuevo
                html5QrCode = null; // Limpiar instancia
            }).catch((err) => {
                console.error("Error al detener el escaneo:", err);
                // Incluso si hay error al detener, ocultamos la UI
                qrReaderContainer.style.display = 'none';
                startScanButton.style.display = 'inline-flex';
            });
        } else {
            // Si no estaba escaneando, solo oculta la UI
            qrReaderContainer.style.display = 'none';
            startScanButton.style.display = 'inline-flex';
        }
    }

    // --- Asignar Eventos a los Botones ---
    startScanButton.addEventListener('click', startScanning);
    stopScanButton.addEventListener('click', stopScanning);

});