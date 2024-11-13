package org.maplibre.navigation.android.navigation.v5.utils

import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection

/**
 * Utils class useful for performing map operations such as adding sources, layers, and more.
 *
 * @since 0.8.0
 */
object MapUtils {

    /**
     * Takes a [FeatureCollection] and creates a map GeoJson source using the sourceId also
     * provided.
     *
     * @param mapLibreMap  that the current mapView is using
     * @param collection the feature collection to be added to the map style
     * @param sourceId   the source's id for identifying it when adding layers
     * @since 0.8.0
     */
    @JvmStatic
    fun updateMapSourceFromFeatureCollection(
        mapLibreMap: MapLibreMap,
        collection: FeatureCollection,
        sourceId: String
    ) {
        mapLibreMap.style?.let { style ->
            val source = style.getSourceAs<GeoJsonSource>(sourceId)
            source?.setGeoJson(collection) ?: run {
                val routeGeoJsonOptions = GeoJsonOptions().withMaxZoom(16)
                val routeSource = GeoJsonSource(sourceId, collection, routeGeoJsonOptions)
                style.addSource(routeSource)
            }
        }
    }

    /**
     * Generic method for adding layers to the map.
     *
     * @param mapLibreMap    that the current mapView is using
     * @param layer        a layer that will be added to the map
     * @param idBelowLayer optionally providing the layer which the new layer should be placed below
     * @since 0.8.0
     */
    @JvmStatic
    fun addLayerToMap(mapLibreMap: MapLibreMap, layer: Layer, idBelowLayer: String?) {
        try {
            mapLibreMap.style?.let { style ->
                style.getLayer(layer.id)?.let { layer ->
                    if (idBelowLayer != null) {
                        style.addLayerBelow(layer, idBelowLayer)
                    } else {
                        style.addLayer(layer)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
