/**
 * Controlador principal de la interfaz web de Meteorytics con Geolocalización y Búsqueda Interactiva.
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
    let currentLat = 40.4168;
    let currentLon = -3.7038;
    let currentName = 'Madrid, España';

    init();

    function init() {
        setupEventListeners();
        
        // Comprobar preferencia previa de ubicación guardada
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

        // Búsqueda interactiva por input de texto
        btnSearch.addEventListener('click', handleSearch);
        searchInput.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') handleSearch();
            else if (searchInput.value.trim().length >= 2) handleSearchDebounced();
        });

        // Selector rápido
        citySelect.addEventListener('change', () => {
            localStorage.setItem('meteorytics_use_geo', 'false');
            loadSelectedCity();
        });

        btnRefresh.addEventListener('click', () => {
            fetchAndRenderWeather(currentLat, currentLon, currentName);
        });

        // Ocultar resultados de búsqueda al hacer clic fuera
        document.addEventListener('click', (e) => {
            if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
                searchResults.classList.add('d-none');
            }
        });
    }

    /**
     * Solicita acceso a la geolocalización real del usuario.
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
                        errorText = 'Ubicación no disponible en tu dispositivo.';
                        break;
                    case error.TIMEOUT:
                        errorText = 'Tiempo de espera agotado al obtener ubicación.';
                        break;
                }
                showStatus(`${errorText} Ingresa una ciudad manualmente.`, 'warning');
                loadSelectedCity();
            },
            { enableHighAccuracy: true, timeout: 8000, maximumAge: 0 }
        );
    }

    /**
     * Maneja la búsqueda de ubicaciones con la API de geocodificación.
     */
    let searchTimeout = null;
    function handleSearchDebounced() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(handleSearch, 400);
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
            showStatus(`Error al buscar ubicaciones: ${err.message}`, 'warning');
        }
    }

    /**
     * Renderiza la lista desplegable de resultados de geocodificación.
     */
    function renderSearchResults(locations) {
        searchResults.innerHTML = '';
        if (locations.length === 0) {
            searchResults.innerHTML = '<div class="list-group-item disabled small">No se encontraron ubicaciones para esa búsqueda.</div>';
            searchResults.classList.remove('d-none');
            return;
        }

        locations.forEach(loc => {
            const item = document.createElement('button');
            item.type = 'button';
            item.className = 'list-group-item list-group-item-action d-flex justify-content-between align-items-center small';
            item.innerHTML = `<span><i class="bi bi-geo-alt me-1 text-primary"></i> <strong>${loc.name}</strong></span> <span class="text-muted text-nowrap">(${loc.latitude.toFixed(2)}°, ${loc.longitude.toFixed(2)}°)</span>`;
            
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

    /**
     * Carga el clima para la ciudad seleccionada en el menú desplegable.
     */
    function loadSelectedCity() {
        const selectedOption = citySelect.options[citySelect.selectedIndex];
        currentLat = parseFloat(selectedOption.getAttribute('data-lat'));
        currentLon = parseFloat(selectedOption.getAttribute('data-lon'));
        currentName = selectedOption.text;

        updateLocationBadge(`🏙️ ${currentName}`);
        fetchAndRenderWeather(currentLat, currentLon, currentName);
    }

    /**
     * Consulta la API backend de Meteorytics y renderiza datos.
     */
    async function fetchAndRenderWeather(lat, lon, name) {
        try {
            showStatus('Cargando datos meteorológicos...', 'info');
            const data = await MeteoryticsAPI.getWeather(lat, lon, name);
            hideStatus();
            renderKPIs(data.current);
            renderCharts(data.hourly);
            lastUpdate.textContent = `Actualizado: ${new Date().toLocaleTimeString()}`;
        } catch (err) {
            showStatus(`Error al cargar datos meteorológicos: ${err.message}`, 'danger');
        }
    }

    /**
     * Renderiza las tarjetas KPI.
     */
    function renderKPIs(current) {
        document.getElementById('kpi-temp').textContent = `${current.temperature.toFixed(1)} °C`;
        document.getElementById('kpi-apparent').innerHTML = `<i class="bi bi-thermometer-half"></i> Sensación: ${current.apparentTemperature.toFixed(1)} °C`;
        document.getElementById('kpi-humidity').textContent = `${current.humidity} %`;
        document.getElementById('kpi-pressure').textContent = `${current.pressure.toFixed(1)} hPa`;
        document.getElementById('kpi-wind').textContent = `${current.windSpeed.toFixed(1)} km/h`;
    }

    /**
     * Renderiza los gráficos de Chart.js.
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
