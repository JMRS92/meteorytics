/**
 * Módulo de conexión con la API backend de Meteorytics.
 */
const MeteoryticsAPI = {
    /**
     * Consulta el pronóstico meteorológico para las coordenadas dadas.
     * 
     * @param {number} lat Latitud geográfica
     * @param {number} lon Longitud geográfica
     * @param {string} name Nombre opcional de la ubicación
     * @returns {Promise<Object>} Datos del pronóstico
     */
    async getWeather(lat, lon, name = 'Ubicación') {
        const url = `/api/weather?lat=${lat}&lon=${lon}&name=${encodeURIComponent(name)}`;
        const response = await fetch(url);
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ error: 'Error de servidor' }));
            throw new Error(errorData.error || `Error HTTP: ${response.status}`);
        }
        
        return await response.json();
    },

    /**
     * Realiza una búsqueda de ubicaciones (geocodificación) por texto.
     * 
     * @param {string} query Nombre de la ciudad, país o dirección
     * @returns {Promise<Array>} Lista de ubicaciones encontradas
     */
    async searchLocations(query) {
        if (!query || query.trim().length === 0) return [];
        const url = `/api/geocoding?q=${encodeURIComponent(query.trim())}`;
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`Error HTTP: ${response.status}`);
        }

        return await response.json();
    }
};
