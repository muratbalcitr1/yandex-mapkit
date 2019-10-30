package com.yandex.mapkitdemo

import android.app.Activity
import android.os.Bundle

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RawTile
import com.yandex.mapkit.TileId
import com.yandex.mapkit.Version
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.geo.Projection
import com.yandex.mapkit.geometry.geo.Projections
import com.yandex.mapkit.layers.Layer
import com.yandex.mapkit.layers.LayerOptions
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.resource_url_provider.ResourceUrlProvider
import com.yandex.mapkit.tiles.TileProvider

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.logging.Logger

/**
 * This example shows how to add layer with simple objects such as points, polylines, polygons
 * to the map using GeoJSON format.
 */
class GeoJsonActivity : Activity() {
    /**
     * Replace "your_api_key" with a valid developer key.
     * You can get it at the https://developer.tech.yandex.ru/ website.
     */
    private val MAPKIT_API_KEY = "843679b6-ddc8-4f6a-a4ca-40c7ea099ce8"
    private val CAMERA_TARGET = Point(59.952, 30.318)

    private val LOGGER = Logger.getLogger("mapkitdemo.geojson")
    private var projection: Projection? = null
    private var urlProvider: ResourceUrlProvider? = null
    private var tileProvider: TileProvider? = null
    private var mapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        setContentView(R.layout.geo_json)
        super.onCreate(savedInstanceState)
        mapView = findViewById(R.id.mapview)

        mapView!!.map.move(
                CameraPosition(CAMERA_TARGET, 15.0f, 0.0f, 0.0f))
        mapView!!.map.mapType = MapType.VECTOR_MAP

        // Client code must retain strong references to providers and projection
        projection = Projections.createWgs84Mercator()
        urlProvider = ResourceUrlProvider { s -> "https://raw.githubusercontent.com/yandex/mapkit-android-demo/master/src/main/%s" }
        try {
            tileProvider = createTileProvider()
        } catch (ex: IOException) {
            LOGGER.severe("Tile provider not created: cancel creation of geo json layer")
            return
        }

        createGeoJsonLayer()
    }

    override fun onStop() {
        mapView!!.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView!!.onStart()
    }

    private fun createGeoJsonLayer() {
        val layer = mapView!!.map.addLayer(
                "geo_json_layer",
                "application/geo-json",
                LayerOptions(),
                tileProvider!!,
                urlProvider!!,
                projection!!)

        layer.invalidate("0.0.0")
    }

     private fun createTileProvider(): TileProvider {
        val builder = StringBuilder()
        val resourceIdentifier = resources.getIdentifier("geo_json_example", "raw", packageName)
        val res = resources.openRawResource(resourceIdentifier)
        val reader = BufferedReader(InputStreamReader(res))

        try {
            var line: String
            while ((reader.readLine()) != null) {
                builder.append(reader.readLine())
            }
        } catch (ex: Exception) {
            reader.close()
            LOGGER.severe("Cannot read GeoJSON file")
            throw ex
        }

        val rawJson = builder.toString()
        return TileProvider { tileId, version, etag -> RawTile(version, etag, RawTile.State.OK, rawJson.toByteArray()) }
    }
}
