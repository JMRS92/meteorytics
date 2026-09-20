/**
 * Main Controller for Meteorytics — 100% Dynamic, Accessible, Internationalized (EN | ES).
 */
document.addEventListener('DOMContentLoaded', () => {
    // DOM Element References
    const geoBanner = document.getElementById('geo-banner');
    const btnUseLocation = document.getElementById('btn-use-location');
    const btnDismissLocation = document.getElementById('btn-dismiss-location');
    const btnRequestGps = document.getElementById('btn-request-gps');
    const searchInput = document.getElementById('search-input');
    const btnSearch = document.getElementById('btn-search');
    const searchResults = document.getElementById('search-results');
    const btnRefresh = document.getElementById('btn-refresh');
    const locationBadge = document.getElementById('location-badge');
    const statusMessage = document.getElementById('status-message');
    const lastUpdate = document.getElementById('last-update');
    const btnLangEn = document.getElementById('lang-en');
    const btnLangEs = document.getElementById('lang-es');

    // Chart.js instances
    let tempChartInstance = null;
    let humidityChartInstance = null;
    let windChartInstance = null;

    let currentLat = 40.4168;
    let currentLon = -3.7038;
    let currentName = 'Madrid, Spain';
    let lastWeatherData = null;

    init();

    function init() {
        // Initialize language (English default as requested, or load saved preference)
        const savedLang = localStorage.getItem('meteorytics_lang') || 'en';
        I18n.setLanguage(savedLang);

        setupEventListeners();

        // Check user geolocation preference
        const savedGeoPref = localStorage.getItem('meteorytics_use_geo');
        if (savedGeoPref === 'true') {
            requestUserLocation();
        } else {
            fetchAndRenderWeather(currentLat, currentLon, currentName);
        }
    }

    function setupEventListeners() {
        // Language Switcher Buttons
        if (btnLangEn) {
            btnLangEn.addEventListener('click', () => {
                I18n.setLanguage('en');
                if (lastWeatherData) renderAll(lastWeatherData);
            });
        }
        if (btnLangEs) {
            btnLangEs.addEventListener('click', () => {
                I18n.setLanguage('es');
                if (lastWeatherData) renderAll(lastWeatherData);
            });
        }

        btnUseLocation.addEventListener('click', () => {
            localStorage.setItem('meteorytics_use_geo', 'true');
            requestUserLocation();
        });

        btnDismissLocation.addEventListener('click', () => {
            localStorage.setItem('meteorytics_use_geo', 'false');
            geoBanner.classList.add('d-none');
            fetchAndRenderWeather(currentLat, currentLon, currentName);
        });

        btnRequestGps.addEventListener('click', () => {
            localStorage.setItem('meteorytics_use_geo', 'true');
            requestUserLocation();
        });

        // Interactive autocomplete search while typing
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

        if (btnRefresh) {
            btnRefresh.addEventListener('click', () => {
                fetchAndRenderWeather(currentLat, currentLon, currentName);
            });
        }

        // Hide autocomplete dropdown on outside click
        document.addEventListener('click', (e) => {
            if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
                searchResults.classList.add('d-none');
            }
        });
    }

    /**
     * Request real-time GPS location authorization from browser.
     */
    function requestUserLocation() {
        if (!navigator.geolocation) {
            showStatus(I18n.get('statusGeoUnsupported'), 'danger');
            fetchAndRenderWeather(currentLat, currentLon, currentName);
            return;
        }

        showStatus(I18n.get('statusGettingGps'), 'info');

        navigator.geolocation.getCurrentPosition(
            (position) => {
                currentLat = position.coords.latitude;
                currentLon = position.coords.longitude;
                currentName = `GPS (${currentLat.toFixed(2)}°, ${currentLon.toFixed(2)}°)`;

                geoBanner.classList.add('d-none');
                hideStatus();
                fetchAndRenderWeather(currentLat, currentLon, currentName);
            },
            (error) => {
                showStatus(I18n.get('statusGeoDenied'), 'warning');
                fetchAndRenderWeather(currentLat, currentLon, currentName);
            },
            { enableHighAccuracy: true, timeout: 8000, maximumAge: 0 }
        );
    }

    /**
     * Debounced search handler for fluid autocomplete suggestions.
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
     * Render dynamic autocomplete menu suggestions.
     */
    function renderSearchResults(locations) {
        searchResults.innerHTML = '';
        if (!locations || locations.length === 0) {
            searchResults.innerHTML = `<div class="list-group-item disabled small text-muted">${I18n.currentLang === 'es' ? 'No se encontraron ubicaciones' : 'No matching locations found'}</div>`;
            searchResults.classList.remove('d-none');
            return;
        }

        locations.forEach(loc => {
            const item = document.createElement('button');
            item.type = 'button';
            item.className = 'list-group-item list-group-item-action d-flex justify-content-between align-items-center py-2 px-3';
            item.innerHTML = `
                <div>
                    <i class="bi bi-geo-alt-fill text-primary me-2" aria-hidden="true"></i>
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

                fetchAndRenderWeather(currentLat, currentLon, currentName);
            });

            searchResults.appendChild(item);
        });

        searchResults.classList.remove('d-none');
    }

    /**
     * Query Meteorytics Backend API and render UI components.
     */
    async function fetchAndRenderWeather(lat, lon, name) {
        try {
            showStatus(I18n.get('statusLoadingWeather'), 'info');
            const data = await MeteoryticsAPI.getWeather(lat, lon, name);
            hideStatus();

            lastWeatherData = data;
            renderAll(data);
        } catch (err) {
            showStatus(`${I18n.get('statusWeatherError')} ${err.message}`, 'danger');
        }
    }

    /**
     * Render all components (badge, KPIs, analytics cards, charts).
     */
    function renderAll(data) {
        updateLocationBadge(data.location.name);
        renderKPIs(data.current);
        if (data.analytics) {
            renderAnalytics(data.analytics);
        }
        renderCharts(data.hourly);
        lastUpdate.textContent = `${I18n.get('lastUpdatePrefix')} ${new Date().toLocaleTimeString()}`;
    }

    /**
     * Render the 8 advanced atmospheric analytics cards.
     */
    function renderAnalytics(analytics) {
        const minLabel = I18n.currentLang === 'es' ? 'Mín' : 'Min';
        const maxLabel = I18n.currentLang === 'es' ? 'Máx' : 'Max';

        document.getElementById('analytics-comfort').textContent = analytics.thermalComfort || '--';
        document.getElementById('analytics-range').textContent = `${minLabel}: ${analytics.minTemperature.toFixed(1)} °C | ${maxLabel}: ${analytics.maxTemperature.toFixed(1)} °C`;
        document.getElementById('analytics-rain').textContent = analytics.rainRisk || '--';
        document.getElementById('analytics-wind').textContent = analytics.windStatus || '--';
        document.getElementById('analytics-dew').textContent = `${analytics.dewPoint.toFixed(1)} °C`;
        document.getElementById('analytics-baro').textContent = analytics.baroStatus || '--';
        document.getElementById('analytics-uv').textContent = `${analytics.uvIndex} / 11`;
        document.getElementById('analytics-density').textContent = `${analytics.airDensity.toFixed(3)} kg/m³`;
    }

    /**
     * Render real-time current KPI cards.
     */
    function renderKPIs(current) {
        document.getElementById('kpi-temp').textContent = `${current.temperature.toFixed(1)} °C`;
        document.getElementById('kpi-apparent').innerHTML = `<i class="bi bi-thermometer-half" aria-hidden="true"></i> ${I18n.get('tempApparentPrefix')} ${current.apparentTemperature.toFixed(1)} °C`;
        document.getElementById('kpi-humidity').textContent = `${current.humidity} %`;
        document.getElementById('kpi-pressure').textContent = `${current.pressure.toFixed(1)} hPa`;
        document.getElementById('kpi-wind').textContent = `${current.windSpeed.toFixed(1)} km/h`;
    }

    /**
     * Render 3 Chart.js interactive charts using real 24h hourly arrays.
     */
    function renderCharts(hourlyList) {
        const labels = hourlyList.map(h => h.time);
        const temps = hourlyList.map(h => h.temperature);
        const humidities = hourlyList.map(h => h.humidity);
        const precipProbs = hourlyList.map(h => h.precipitationProbability);
        const winds = hourlyList.map(h => h.windSpeed);

        // Chart 1: Temperature & Apparent Temperature
        const ctxTemp = document.getElementById('tempChart').getContext('2d');
        if (tempChartInstance) tempChartInstance.destroy();
        tempChartInstance = new Chart(ctxTemp, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: I18n.get('chartTempSeries'),
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

        // Chart 2: Humidity & Rain Probability
        const ctxHumidity = document.getElementById('humidityChart').getContext('2d');
        if (humidityChartInstance) humidityChartInstance.destroy();
        humidityChartInstance = new Chart(ctxHumidity, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: I18n.get('chartHumiditySeries'),
                        data: humidities,
                        backgroundColor: 'rgba(13, 202, 240, 0.6)'
                    },
                    {
                        label: I18n.get('chartRainSeries'),
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

        // Chart 3: Wind Velocity Trend Analysis
        const ctxWind = document.getElementById('windChart').getContext('2d');
        if (windChartInstance) windChartInstance.destroy();
        windChartInstance = new Chart(ctxWind, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: I18n.get('chartWindSeries'),
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
        locationBadge.innerHTML = `<i class="bi bi-geo-fill me-1" aria-hidden="true"></i> ${I18n.get('locBadgePrefix')} ${text}`;
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
