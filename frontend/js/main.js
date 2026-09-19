/**
 * Controlador principal de Meteorytics con Autocompletado de Ubicación e Inteligencia Atmosférica.
 */
document.addEventListener('DOMContentLoaded', () => {
    // Referencias a elementos DOM
    const geoBanner = document.getElementById('geo-banner');
    const btnUseLocation = document.getElementById('btn-use-location');
    const btnDismissLocation = document.getElementById('btn-dismiss-location');
    const searchInput = document.getElementById('search-input');
    const btnSearch = document.getElementById('btn-search');
    const searchResults = document.getElementById('search-results');
    const citySelect = document.getElementById('city-select');
    const btnRefresh = document.getElementById('btn-refresh');
    const locationBadge = document.getElementById('location-badge');
    const statusMessage = document.getElementById('status-message');
    const lastUpdate = document.getElementById('last-update');

    // Instancias de Chart.js
    let tempChartInstance = null;
    let humidityChartInstance = null;
    let windChartInstance = null;

    let currentLat = 40.4168;
    let currentLon = -3.7038;
    let currentName = 'Madrid, España';

    init();

    function init() {
        setupEventListeners();
        
        // Comprobar si hay preferencia de geolocalización previa
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

        // Búsqueda interactiva tipo Google Places mientras el usuario escribe
        searchInput.addEventListener('input', () => {
            handleSearchDebounced();
        });

        btnSearch.addEventListener('click', handleSearch);

        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                handleSearch();
            }
        });

        citySelect.addEventListener('change', () => {
            localStorage.setItem('meteorytics_use_geo', 'false');
            loadSelectedCity();
        });

        btnRefresh.addEventListener('click', () => {
            fetchAndRenderWeather(currentLat, currentLon, currentName);
        });

        // Ocultar desplegable de sugerencias al hacer clic fuera
        document.addEventListener('click', (e) => {
            if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
                searchResults.classList.add('d-none');
            }
        });
    }

    /**
     * Solicita acceso a la geolocalización en tiempo real del usuario.
     */
    function requestUserLocation() {
        if (!navigator.geolocation) {
            showStatus('La geolocalización no está soportada en tu navegador.', 'danger');
            loadSelectedCity();
            return;
        }

        showStatus('Obteniendo tu ubicación en tiempo real...', 'info');

        navigator.geolocation.getCurrentPosition(
            (position) => {
                currentLat = position.coords.latitude;
                currentLon = position.coords.longitude;
                currentName = `Ubicación Real (${currentLat.toFixed(2)}°, ${currentLon.toFixed(2)}°)`;
                
                geoBanner.classList.add('d-none');
                hideStatus();
                updateLocationBadge(`📍 ${currentName}`);
                fetchAndRenderWeather(currentLat, currentLon, currentName);
            },
            (error) => {
                let errorText = 'No se pudo acceder a tu ubicación.';
                switch (error.code) {
                    case error.PERMISSION_DENIED:
                        errorText = 'Acceso a ubicación denegado por el usuario.';
                        break;
                    case error.POSITION_UNAVAILABLE:
                        errorText = 'Ubicación no disponible.';
                        break;
                    case error.TIMEOUT:
                        errorText = 'Tiempo de espera agotado.';
                        break;
                }
                showStatus(`${errorText} Ingresa una ciudad manualmente.`, 'warning');
                loadSelectedCity();
            },
            { enableHighAccuracy: true, timeout: 8000, maximumAge: 0 }
        );
    }

    /**
     * Búsqueda con debounce para auto-sugerencias fluidas.
     */
    let searchTimeout = null;
    function handleSearchDebounced() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(handleSearch, 300);
    }

    async function handleSearch() {
        const query = searchInput.value.trim();
        if (query.length < 2) {
            searchResults.classList.add('d-none');
            return;
        }

        try {
            const locations = await MeteoryticsAPI.searchLocations(query);
            renderSearchResults(locations);
        } catch (err) {
            console.error(err);
        }
    }

    /**
     * Renderiza el menú flotante de autosugerencias tipo Google Places.
     */
    function renderSearchResults(locations) {
        searchResults.innerHTML = '';
        if (!locations || locations.length === 0) {
            searchResults.innerHTML = '<div class="list-group-item disabled small text-muted">Sin coincidencias para esta búsqueda.</div>';
            searchResults.classList.remove('d-none');
            return;
        }

        locations.forEach(loc => {
            const item = document.createElement('button');
            item.type = 'button';
            item.className = 'list-group-item list-group-item-action d-flex justify-content-between align-items-center py-2 px-3';
            item.innerHTML = `
                <div>
                    <i class="bi bi-geo-alt-fill text-primary me-2"></i>
                    <strong>${loc.name}</strong>
                </div>
                <small class="text-muted ms-2">${loc.latitude.toFixed(2)}°, ${loc.longitude.toFixed(2)}°</small>
            `;
            
            item.addEventListener('click', () => {
                currentLat = loc.latitude;
                currentLon = loc.longitude;
                currentName = loc.name;
                
                localStorage.setItem('meteorytics_use_geo', 'false');
                geoBanner.classList.add('d-none');
                searchResults.classList.add('d-none');
                searchInput.value = loc.name;

                updateLocationBadge(`🔍 ${loc.name}`);
                fetchAndRenderWeather(currentLat, currentLon, currentName);
            });

            searchResults.appendChild(item);
        });

        searchResults.classList.remove('d-none');
    }

    function loadSelectedCity() {
        const selectedOption = citySelect.options[citySelect.selectedIndex];
        currentLat = parseFloat(selectedOption.getAttribute('data-lat'));
        currentLon = parseFloat(selectedOption.getAttribute('data-lon'));
        currentName = selectedOption.text;

        updateLocationBadge(`🏙️ ${currentName}`);
        fetchAndRenderWeather(currentLat, currentLon, currentName);
    }

    /**
     * Consulta la API backend de Meteorytics y renderiza KPIs, analíticas y gráficos.
     */
    async function fetchAndRenderWeather(lat, lon, name) {
        try {
            showStatus('Cargando analíticas meteorológicas...', 'info');
            const data = await MeteoryticsAPI.getWeather(lat, lon, name);
            hideStatus();
            
            renderKPIs(data.current);
            if (data.analytics) {
                renderAnalytics(data.analytics);
            }
            renderCharts(data.hourly);
            lastUpdate.textContent = `Última actualización: ${new Date().toLocaleTimeString()}`;
        } catch (err) {
            showStatus(`Error al cargar datos meteorológicos: ${err.message}`, 'danger');
        }
    }

    /**
     * Renderiza las tarjetas de analítica inteligente.
     */
    function renderAnalytics(analytics) {
        document.getElementById('analytics-comfort').textContent = analytics.thermalComfort || '--';
        document.getElementById('analytics-range').textContent = `Mín: ${analytics.minTemperature.toFixed(1)} °C | Máx: ${analytics.maxTemperature.toFixed(1)} °C`;
        document.getElementById('analytics-rain').textContent = analytics.rainRisk || '--';
        document.getElementById('analytics-wind').textContent = analytics.windStatus || '--';
    }

    /**
     * Renderiza los KPIs climáticos actuales.
     */
    function renderKPIs(current) {
        document.getElementById('kpi-temp').textContent = `${current.temperature.toFixed(1)} °C`;
        document.getElementById('kpi-apparent').innerHTML = `<i class="bi bi-thermometer-half"></i> Sensación: ${current.apparentTemperature.toFixed(1)} °C`;
        document.getElementById('kpi-humidity').textContent = `${current.humidity} %`;
        document.getElementById('kpi-pressure').textContent = `${current.pressure.toFixed(1)} hPa`;
        document.getElementById('kpi-wind').textContent = `${current.windSpeed.toFixed(1)} km/h`;
    }

    /**
     * Renderiza 3 gráficos interactivos con Chart.js.
     */
    function renderCharts(hourlyList) {
        const labels = hourlyList.map(h => h.time);
        const temps = hourlyList.map(h => h.temperature);
        const humidities = hourlyList.map(h => h.humidity);
        const precipProbs = hourlyList.map(h => h.precipitationProbability);
        const winds = hourlyList.map(h => h.windSpeed);

        // Gráfico 1: Temperatura vs Sensación Térmica
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

        // Gráfico 2: Humedad y Probabilidad de Lluvia
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
                        backgroundColor: 'rgba(255, 193, 7, 0.7)'
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: true } }
            }
        });

        // Gráfico 3: Análisis de Viento
        const ctxWind = document.getElementById('windChart').getContext('2d');
        if (windChartInstance) windChartInstance.destroy();
        windChartInstance = new Chart(ctxWind, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Velocidad del Viento (km/h)',
                    data: winds,
                    borderColor: '#198754',
                    backgroundColor: 'rgba(25, 135, 84, 0.15)',
                    fill: true,
                    tension: 0.4
                }]
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
