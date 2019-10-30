package com.yandex.mapkitdemo

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.places.PlacesFactory
import com.yandex.mapkit.places.panorama.NotFoundError
import com.yandex.mapkit.places.panorama.PanoramaService
import com.yandex.mapkit.places.panorama.PanoramaView
import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError

/**
 * This example shows how to find a panorama that is nearest to a given point and display it
 * in the PanoramaView object. User is not limited to viewing the panorama found and can
 * use arrows to navigate.
 * Note: Nearest panorama search API calls count towards MapKit daily usage limits.
 * Learn more at https://tech.yandex.ru/mapkit/doc/3.x/concepts/conditions-docpage/#conditions__limits
 */
class PanoramaActivity : Activity(), PanoramaService.SearchListener {
    /**
     * Replace "your_api_key" with a valid developer key.
     * You can get it at the https://developer.tech.yandex.ru/ website.
     */
    private val MAPKIT_API_KEY = "843679b6-ddc8-4f6a-a4ca-40c7ea099ce8"
    private val SEARCH_LOCATION = Point(55.733330, 37.587649)

    private var panoramaView: PanoramaView? = null
    private var panoramaService: PanoramaService? = null
    private var searchSession: PanoramaService.SearchSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        PlacesFactory.initialize(this)
        setContentView(R.layout.panorama)
        super.onCreate(savedInstanceState)
        panoramaView = findViewById(R.id.panoview)

        panoramaService = PlacesFactory.getInstance().createPanoramaService()
        searchSession = panoramaService!!.findNearest(SEARCH_LOCATION, this)
    }

    override fun onStop() {
        panoramaView!!.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        panoramaView!!.onStart()
    }

    override fun onPanoramaSearchResult(panoramaId: String) {
        panoramaView!!.player.openPanorama(panoramaId)
        panoramaView!!.player.enableMove()
        panoramaView!!.player.enableRotation()
        panoramaView!!.player.enableZoom()
        panoramaView!!.player.enableMarkers()
    }

    override fun onPanoramaSearchError(error: Error) {
        var errorMessage = getString(R.string.unknown_error_message)
        if (error is NotFoundError) {
            errorMessage = getString(R.string.not_found_error_message)
        } else if (error is RemoteError) {
            errorMessage = getString(R.string.remote_error_message)
        } else if (error is NetworkError) {
            errorMessage = getString(R.string.network_error_message)
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
