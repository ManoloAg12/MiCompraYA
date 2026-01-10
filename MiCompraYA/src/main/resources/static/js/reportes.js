document.addEventListener("DOMContentLoaded", async () => {

    // 1. Obtener datos del servidor
    try {
        const response = await fetch('/reportes/data');
        if (!response.ok) throw new Error('Error al cargar datos');
        const data = await response.json();

        // 2. Actualizar Tarjetas KPI
        document.getElementById('kpi-critico').textContent = data.stockCritico;
        document.getElementById('kpi-agotado').textContent = data.stockAgotado;

        // 3. Dibujar Gráfico de Pastel (Movimientos)
        const ctxPie = document.getElementById('chartMovimientos').getContext('2d');
        new Chart(ctxPie, {
            type: 'doughnut', // Tipo "Donut" se ve más moderno que Pie
            data: {
                labels: data.movimientosLabel,
                datasets: [{
                    data: data.movimientosData,
                    backgroundColor: [
                        '#4F46E5', // Indigo
                        '#10B981', // Emerald
                        '#F59E0B', // Amber
                        '#EF4444', // Red
                        '#3B82F6'  // Blue
                    ],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'right' }
                }
            }
        });

        // 4. Dibujar Gráfico de Barras (Top Ventas)
        const ctxBar = document.getElementById('chartTopVentas').getContext('2d');
        new Chart(ctxBar, {
            type: 'bar',
            data: {
                labels: data.topVentasLabel,
                datasets: [{
                    label: 'Unidades Vendidas',
                    data: data.topVentasData,
                    backgroundColor: '#10B981', // Verde corporativo
                    borderRadius: 5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true }
                }
            }
        });

    } catch (error) {
        console.error("Error cargando dashboard:", error);
    }
});