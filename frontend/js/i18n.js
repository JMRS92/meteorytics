/**
 * Dictionary and I18n translation engine for Meteorytics (EN / ES).
 */
const I18n = {
    currentLang: 'en', // English default as requested

    translations: {
        en: {
            appTitle: "Meteorytics",
            appTagline: "Atmospheric Data Intelligence",
            geoTitle: "Allow real-time location access?",
            geoDesc: "Meteorytics requests authorization to get your device's exact GPS coordinates and load precise atmospheric data analytics.",
            geoAllowBtn: "Allow GPS Location",
            geoManualBtn: "Search manually",
            searchLabel: "Search any city, country, or address:",
            searchPlaceholder: "Type 'Madrid', 'London', 'Lima', 'Tokyo', 'Pamplona'...",
            searchBtn: "Search",
            myGpsBtn: "My GPS Location",
            locBadgePrefix: "Location:",
            locLoading: "Loading...",
            lastUpdatePrefix: "Last update:",
            analyticsHeader: "Atmospheric Intelligence & Advanced Analytics Module",
            comfortTitle: "Thermal Comfort",
            rangeTitle: "24h Thermal Range",
            rainTitle: "24h Rain Risk",
            windTitle: "Wind Condition",
            dewTitle: "Dew Point",
            baroTitle: "Barometric State",
            uvTitle: "UV Index Estimate",
            densityTitle: "Air Density",
            tempKpiTitle: "Temperature",
            tempApparentPrefix: "Feels like:",
            humidityKpiTitle: "Humidity",
            humidityKpiSub: "Relative humidity",
            pressureKpiTitle: "Pressure",
            pressureKpiSub: "Surface pressure",
            windKpiTitle: "Wind Speed",
            windKpiSub: "Wind velocity",
            chartTempTitle: "24h Temperature vs Apparent Temperature Evolution",
            chartTempSeries: "Real Temperature (°C)",
            chartTempApparentSeries: "Feels Like (°C)",
            chartHumidityTitle: "Hourly Humidity & Precipitation Probability",
            chartHumiditySeries: "Humidity (%)",
            chartRainSeries: "Rain Prob. (%)",
            chartWindTitle: "Wind Velocity Trend Analysis (km/h)",
            chartWindSeries: "Wind Speed (km/h)",
            footerText: "Meteorytics — Atmospheric Data Intelligence",
            statusGeoUnsupported: "Geolocation is not supported by your browser.",
            statusGettingGps: "Getting real-time GPS location...",
            statusGeoDenied: "Location permission denied. Please search a city manually.",
            statusLoadingWeather: "Loading atmospheric analytics in real-time...",
            statusWeatherError: "Error loading weather data:",
            statusNoLocationFound: "No locations found matching your query.",
            
            // Analytics translations
            "Optimal (Comfortable)": "Optimal (Comfortable)",
            "Warm / Humid": "Warm / Humid",
            "Cool / Cold": "Cool / Cold",
            "High Pressure (Stable)": "High Pressure (Stable)",
            "Low Pressure (Unstable)": "Low Pressure (Unstable)",
            "Strong Wind": "Strong Wind",
            "Moderate Breeze": "Moderate Breeze",
            "Calm": "Calm"
        },
        es: {
            appTitle: "Meteorytics",
            appTagline: "Inteligencia de Datos Atmosféricos",
            geoTitle: "¿Permitir acceso a tu ubicación real en tiempo real?",
            geoDesc: "Meteorytics requiere autorización para obtener la posición GPS exacta de tu dispositivo y cargar analíticas meteorológicas precisas.",
            geoAllowBtn: "Permitir mi ubicación GPS",
            geoManualBtn: "Buscar ubicación manualmente",
            searchLabel: "Buscar cualquier ciudad, país o dirección:",
            searchPlaceholder: "Escribe 'Madrid', 'Londres', 'Lima', 'Tokio', 'Pamplona'...",
            searchBtn: "Buscar",
            myGpsBtn: "Mi Ubicación GPS",
            locBadgePrefix: "Ubicación:",
            locLoading: "Cargando...",
            lastUpdatePrefix: "Última actualización:",
            analyticsHeader: "Módulo de Analítica Atmosférica e Inteligencia Avanzada",
            comfortTitle: "Confort Térmico",
            rangeTitle: "Rango Térmico 24h",
            rainTitle: "Riesgo Lluvia 24h",
            windTitle: "Estado del Viento",
            dewTitle: "Punto de Rocío",
            baroTitle: "Estado Barométrico",
            uvTitle: "Índice UV Estimado",
            densityTitle: "Densidad del Aire",
            tempKpiTitle: "Temperatura",
            tempApparentPrefix: "Sensación:",
            humidityKpiTitle: "Humedad",
            humidityKpiSub: "Humedad relativa",
            pressureKpiTitle: "Presión",
            pressureKpiSub: "Presión superficie",
            windKpiTitle: "Viento",
            windKpiSub: "Velocidad viento",
            chartTempTitle: "Evolución de Temperatura vs Sensación Térmica (24h Real API)",
            chartTempSeries: "Temperatura Real (°C)",
            chartTempApparentSeries: "Sensación Térmica (°C)",
            chartHumidityTitle: "Humedad y Probabilidad de Lluvia por Hora",
            chartHumiditySeries: "Humedad (%)",
            chartRainSeries: "Prob. Lluvia (%)",
            chartWindTitle: "Análisis Evolutivo de Velocidad del Viento (km/h)",
            chartWindSeries: "Velocidad del Viento (km/h)",
            footerText: "Meteorytics — Inteligencia de Datos Atmosféricos",
            statusGeoUnsupported: "La geolocalización no está soportada en tu navegador.",
            statusGettingGps: "Obteniendo ubicación GPS en tiempo real...",
            statusGeoDenied: "Acceso a ubicación denegado. Ingresa una ciudad manualmente.",
            statusLoadingWeather: "Cargando analíticas meteorológicas en tiempo real...",
            statusWeatherError: "Error al cargar datos meteorológicos:",
            statusNoLocationFound: "No se encontraron ubicaciones coincidentes.",

            // Analytics translations
            "Optimal (Comfortable)": "Óptimo (Confortable)",
            "Warm / Humid": "Cálido / Húmedo",
            "Cool / Cold": "Fresco / Frío",
            "High Pressure (Stable)": "Alta Presión (Estable)",
            "Low Pressure (Unstable)": "Baja Presión (Inestable)",
            "Strong Wind": "Viento Fuerte",
            "Moderate Breeze": "Brisa Moderada",
            "Calm": "Calma"
        }
    },

    setLanguage(lang) {
        if (this.translations[lang]) {
            this.currentLang = lang;
            localStorage.setItem('meteorytics_lang', lang);
            this.applyTranslations();
        }
    },

    get(key) {
        return (this.translations[this.currentLang] && this.translations[this.currentLang][key])
            || (this.translations['en'][key])
            || key;
    },

    translateValue(val) {
        if (!val) return '--';
        if (this.translations[this.currentLang] && this.translations[this.currentLang][val]) {
            return this.translations[this.currentLang][val];
        }
        if (this.currentLang === 'es') {
            return val
                .replace(/^High \((.*)\)$/, 'Alto ($1)')
                .replace(/^Moderate \((.*)\)$/, 'Moderado ($1)')
                .replace(/^Low \((.*)\)$/, 'Bajo ($1)');
        }
        return val;
    },

    applyTranslations() {
        document.documentElement.lang = this.currentLang;
        document.querySelectorAll('[data-i18n]').forEach(el => {
            const key = el.getAttribute('data-i18n');
            if (key && this.translations[this.currentLang][key]) {
                el.textContent = this.translations[this.currentLang][key];
            }
        });

        document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
            const key = el.getAttribute('data-i18n-placeholder');
            if (key && this.translations[this.currentLang][key]) {
                el.placeholder = this.translations[this.currentLang][key];
            }
        });

        document.querySelectorAll('[data-i18n-aria]').forEach(el => {
            const key = el.getAttribute('data-i18n-aria');
            if (key && this.translations[this.currentLang][key]) {
                el.setAttribute('aria-label', this.translations[this.currentLang][key]);
            }
        });

        // Toggle Language Button active state
        const btnEn = document.getElementById('lang-en');
        const btnEs = document.getElementById('lang-es');
        if (btnEn && btnEs) {
            if (this.currentLang === 'en') {
                btnEn.classList.add('active', 'btn-light');
                btnEn.classList.remove('btn-outline-light');
                btnEs.classList.remove('active', 'btn-light');
                btnEs.classList.add('btn-outline-light');
            } else {
                btnEs.classList.add('active', 'btn-light');
                btnEs.classList.remove('btn-outline-light');
                btnEn.classList.remove('active', 'btn-light');
                btnEn.classList.add('btn-outline-light');
            }
        }
    }
};
