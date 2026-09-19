/**
 * Controlador principal de la interfaz web de Meteorytics con Bootstrap 5.3.
 */
document.addEventListener('DOMContentLoaded', () => {
    // Referencias a elementos DOM
    const geoBanner = document.getElementById('geo-banner');
    const btnUseLocation = document.getElementById('btn-use-location');
    const btnDismissLocation = document.getElementById('btn-dismiss-location');
    const citySelect = document.getElementById('city-select');
    const btnRefresh = document.getElementById('btn-refresh');
    const locationBadge = document.getElementById('location-badge');
    const statusMessage = document.getElementById('status-message');

    // Instancias de Chart.js
    let tempChartInstance = null;
    let humidityChartInstance = null;

    init();

    function init() {
        setupEventListeners();
        
        // Comprobar preferencia guardada de ubicación
        const savedGeoPref = localStorage.getItem('meteorytics_use_geo');
        if (savedGeoPref === 'true') {
            requestUserLocation();
        } else {
            loadSelectedCity();
        }
    }

    function setupEventListeners() {
        btnUseLocation.addEventListener('click', () => {
            localStorage.setItem('meteorytics_use_geo', 'true');
            requestUserLocation();
        });

        btnDismissLocation.addEventListener('click', () => {
            localStorage.setItem('meteorytics_use_geo', 'false');
            geoBanner.classList.add('d-none');
            loadSelectedCity();
        });

        citySelect.addEventListener('change', () => {
            localStorage.setItem('meteorytics_use_geo', 'false');
            loadSelectedCity();
        });

        btnRefresh.addEventListener('click', () => {
            if (localStorage.getItem('meteorytics_use_geo') === 'true') {
                requestUserLocation();
            } else {
                loadSelectedCity();
            }
        });
    }

    /**
     * Solicita acceso a la geolocalización del navegador.
     */
    function requestUserLocation() {
        if (!navigator.geolocation) {
            showStatus('La geolocalización no está soportada por tu navegador.', 'danger');
            loadSelectedCity();
            return;
        }

        showStatus('Obteniendo tu ubicación en tiempo real...', 'info');

        navigator.geolocation.getCurrentPosition(
            (position) => {
                const lat = position.coords.latitude;
                const lon = position.coords.longitude;
                
                geoBanner.classList.add('d-none');
                hideStatus();
                updateLocationBadge(`📍 Ubicación Real (${lat.toFixed(2)}°, ${lon.toFixed(2)}°)`);
                fetchAndRenderWeather(lat, lon, 'Tu Ubicación');
            },
            (error) => {
                let errorText = 'No se pudo acceder a tu ubicación.';
                switch (error.code) {
                    case error.PERMISSION_DENIED:
                        errorText = 'Acceso a ubicación denegado.';
                        break;
                    case error.POSITION_UNAVAILABLE:
                        errorText = 'Ubicación no disponible.';
                        break;
                    case error.TIMEOUT:
                        errorText = 'Tiempo de espera agotado al obtener ubicación.';
                        break;
                }
                showStatus(`${errorText} Cargando ciudad seleccionada.`, 'warning');
                loadSelectedCity();
            },
            { enableHighAccuracy: true, timeout: 8000, maximumAge: 0 }
        );
    }

    /**
     * Carga el clima para la ciudad seleccionada.
     */
    function loadSelectedCity() {
        const selectedOption = citySelect.options[citySelect.selectedIndex];
        const lat = parseFloat(selectedOption.getAttribute('data-lat'));
        const lon = parseFloat(selectedOption.getAttribute('data-lon'));
        const cityName = selectedOption.text;

        updateLocationBadge(`🏙️ ${cityName}`);
        fetchAndRenderWeather(lat, lon, cityName);
    }

    /**
     * Consulta la API backend de Meteorytics y renderiza datos.
     */
    async function fetchAndRenderWeather(lat, lon, name) {
        try {
            const data = await MeteoryticsAPI.getWeather(lat, lon, name);
            renderKPIs(data.current);
            renderCharts(data.hourly);
        } catch (err) {
            showStatus(`Error al cargar datos meteorológicos: ${err.message}`, 'danger');
        }
    }

    /**
     * Renderiza las tarjetas KPI de Bootstrap.
     */
    function renderKPIs(current) {
        document.getElementById('kpi-temp').textContent = `${current.temperature.toFixed(1)} °C`;
        document.getElementById('kpi-apparent').innerHTML = `<i class="bi bi-thermometer-half"></i> Sensación: ${current.apparentTemperature.toFixed(1)} °C`;
        document.getElementById('kpi-humidity').textContent = `${current.humidity} %`;
        document.getElementById('kpi-pressure').textContent = `${current.pressure.toFixed(1)} hPa`;
        document.getElementById('kpi-wind').textContent = `${current.windSpeed.toFixed(1)} km/h`;
    }

    /**
     * Renderiza los gráficos de Chart.js dentro de tarjetas de Bootstrap.
     */
    function renderCharts(hourlyList) {
        const labels = hourlyList.map(h => h.time);
        const temps = hourlyList.map(h => h.temperature);
        const humidities = hourlyList.map(h => h.humidity);
        const precipProbs = hourlyList.map(h => h.precipitationProbability);

        const ctxTemp = document.getElementById('tempChart').getContext('2d');
        if (tempChartInstance) tempChartInstance.destroy();
        tempChartInstance = new Chart(ctxTemp, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Temperatura (°C)',
                    data: temps,
                    borderColor: '#0d6efd',
                    backgroundColor: 'rgba(13, 110, 253, 0.1)',
                    fill: true,
                    tension: 0.3
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: true } }
            }
        });

        const ctxHumidity = document.getElementById('humidityChart').getContext('2d');
        if (humidityChartInstance) humidityChartInstance.destroy();
        humidityChartInstance = new Chart(ctxHumidity, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Humedad (%)',
                        data: humidities,
                        backgroundColor: 'rgba(13, 202, 240, 0.6)'
                    },
                    {
                        label: 'Prob. Lluvia (%)',
                        data: precipProbs,
                        backgroundColor: 'rgba(255, 193, 7, 0.6)'
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: true } }
            }
        });
    }

    function updateLocationBadge(text) {
        locationBadge.innerHTML = `<i class="bi bi-geo-fill me-1"></i> ${text}`;
    }

    function showStatus(message, type = 'info') {
        statusMessage.textContent = message;
        statusMessage.className = `alert alert-${type} mb-4`;
        statusMessage.classList.remove('d-none');
    }

    function hideStatus() {
        statusMessage.classList.add('d-none');
    }
});
