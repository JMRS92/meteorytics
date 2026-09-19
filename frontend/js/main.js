/**
 * Controlador principal de la interfaz web de Meteorytics.
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

    // Inicializar aplicación
    init();

    function init() {
        setupEventListeners();
        
        // Comprobar si el usuario ya dio preferencia previa de ubicación
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
            geoBanner.classList.add('hidden');
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
     * Solicita acceso a la ubicación geográfica real del usuario mediante la API navigator.geolocation.
     */
    function requestUserLocation() {
        if (!navigator.geolocation) {
            showStatus('La geolocalización no está soportada por tu navegador.', 'error');
            loadSelectedCity();
            return;
        }

        showStatus('Obteniendo tu ubicación geográfica en tiempo real...', 'info');

        navigator.geolocation.getCurrentPosition(
            (position) => {
                const lat = position.coords.latitude;
                const lon = position.coords.longitude;
                
                geoBanner.classList.add('hidden');
                hideStatus();
                updateLocationBadge(`📍 Ubicación Real (${lat.toFixed(2)}°, ${lon.toFixed(2)}°)`);
                fetchAndRenderWeather(lat, lon, 'Tu Ubicación');
            },
            (error) => {
                let errorText = 'No se pudo acceder a tu ubicación.';
                switch (error.code) {
                    case error.PERMISSION_DENIED:
                        errorText = 'Acceso a la ubicación denegado por el usuario.';
                        break;
                    case error.POSITION_UNAVAILABLE:
                        errorText = 'La información de ubicación no está disponible.';
                        break;
                    case error.TIMEOUT:
                        errorText = 'Tiempo de espera agotado al obtener la ubicación.';
                        break;
                }
                showStatus(`${errorText} Cargando ciudad por defecto.`, 'error');
                loadSelectedCity();
            },
            {
                enableHighAccuracy: true,
                timeout: 8000,
                maximumAge: 0
            }
        );
    }

    /**
     * Carga el clima de la ciudad seleccionada en el menú desplegable.
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
     * Consulta la API meteorológica y renderiza la información en la UI.
     */
    async function fetchAndRenderWeather(lat, lon, name) {
        try {
            const data = await MeteoryticsAPI.getWeather(lat, lon, name);
            renderKPIs(data.current);
            renderCharts(data.hourly);
        } catch (err) {
            showStatus(`Error al cargar datos meteorológicos: ${err.message}`, 'error');
        }
    }

    /**
     * Renderiza las tarjetas de KPIs meteorológicos.
     */
    function renderKPIs(current) {
        document.getElementById('kpi-temp').textContent = `${current.temperature.toFixed(1)} °C`;
        document.getElementById('kpi-apparent').textContent = `Sensación: ${current.apparentTemperature.toFixed(1)} °C`;
        document.getElementById('kpi-humidity').textContent = `${current.humidity} %`;
        document.getElementById('kpi-pressure').textContent = `${current.pressure.toFixed(1)} hPa`;
        document.getElementById('kpi-wind').textContent = `${current.windSpeed.toFixed(1)} km/h`;
    }

    /**
     * Renderiza los gráficos evolutivos con Chart.js.
     */
    function renderCharts(hourlyList) {
        const labels = hourlyList.map(h => h.time);
        const temps = hourlyList.map(h => h.temperature);
        const humidities = hourlyList.map(h => h.humidity);
        const precipProbs = hourlyList.map(h => h.precipitationProbability);

        // Gráfico 1: Temperatura
        const ctxTemp = document.getElementById('tempChart').getContext('2d');
        if (tempChartInstance) tempChartInstance.destroy();
        tempChartInstance = new Chart(ctxTemp, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Temperatura (°C)',
                    data: temps,
                    borderColor: '#2563eb',
                    backgroundColor: 'rgba(37, 99, 235, 0.1)',
                    fill: true,
                    tension: 0.3
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: true } }
            }
        });

        // Gráfico 2: Humedad y Precipitación
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
                        backgroundColor: 'rgba(14, 165, 233, 0.6)'
                    },
                    {
                        label: 'Prob. Lluvia (%)',
                        data: precipProbs,
                        backgroundColor: 'rgba(99, 102, 241, 0.6)'
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
        locationBadge.textContent = text;
    }

    function showStatus(message, type = 'info') {
        statusMessage.textContent = message;
        statusMessage.className = `status-message ${type}`;
        statusMessage.classList.remove('hidden');
    }

    function hideStatus() {
        statusMessage.classList.add('hidden');
    }
});
