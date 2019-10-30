package com.yandex.mapkitdemo

import android.app.Activity
import android.os.Bundle
import android.view.View

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.TileId
import com.yandex.mapkit.Version
import com.yandex.mapkit.layers.Layer
import com.yandex.mapkit.layers.LayerOptions
import com.yandex.mapkit.map.MapType
import com.yandex.mapkit.tiles.UrlProvider
import com.yandex.mapkit.resource_url_provider.DefaultUrlProvider
import com.yandex.mapkit.geometry.geo.Projection
import com.yandex.mapkit.geometry.geo.Projections
import com.yandex.mapkit.mapview.MapView

/**
 * This example shows how to add a user-defined layer to the map.
 * We use the UrlProvider class to format requests to a remote server that renders
 * tiles. For simplicity, we ignore map coordinates and zoom here, and
 * just provide a URL for the static image.
 */
class CustomLayerActivity : Activity() {
    /**
     * Replace "your_api_key" with a valid developer key.
     * You can get it at the https://developer.tech.yandex.ru/ website.
     */
    private val MAPKIT_API_KEY = "843679b6-ddc8-4f6a-a4ca-40c7ea099ce8"

    private var urlProvider: UrlProvider? = null
    private var resourceUrlProvider: DefaultUrlProvider? = null
    private var projection: Projection? = null
    private var mapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        setContentView(R.layout.custom_layer)
        super.onCreate(savedInstanceState)

        urlProvider = UrlProvider { tileId, version -> "https://maps-ios-pods-public.s3.yandex.net/mapkit_logo.png" }
        resourceUrlProvider = DefaultUrlProvider()
        projection = Projections.createWgs84Mercator()

        mapView = findViewById(R.id.mapview)
        mapView!!.map.mapType = MapType.NONE
        val l = mapView!!.map.addLayer(
                "mapkit_logo",
                "image/png",
                LayerOptions(),
                urlProvider!!,
                resourceUrlProvider!!,
                projection!!)
        l.invalidate("0.0.0")
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
}
