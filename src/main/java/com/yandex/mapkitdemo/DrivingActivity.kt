package com.yandex.mapkitdemo

import android.os.Bundle
import android.app.Activity
import android.widget.Toast
import java.util.ArrayList

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError

/**
 * This example shows how to build routes between two points and display them on the map.
 * Note: Routing API calls count towards MapKit daily usage limits. Learn more at
 * https://tech.yandex.ru/mapkit/doc/3.x/concepts/conditions-docpage/#conditions__limits
 */
class DrivingActivity : Activity(), DrivingSession.DrivingRouteListener {
    /**
     * Replace "your_api_key" with a valid developer key.
     * You can get it at the https://developer.tech.yandex.ru/ website.
     */
    private val MAPKIT_API_KEY = "843679b6-ddc8-4f6a-a4ca-40c7ea099ce8"
    private val ROUTE_START_LOCATION = Point(59.959194, 30.407094)
    private val ROUTE_END_LOCATION = Point(55.733330, 37.587649)
    private val SCREEN_CENTER = Point(
            (ROUTE_START_LOCATION.latitude + ROUTE_END_LOCATION.latitude) / 2,
            (ROUTE_START_LOCATION.longitude + ROUTE_END_LOCATION.longitude) / 2)

    private var mapView: MapView? = null
    private var mapObjects: MapObjectCollection? = null
    private var drivingRouter: DrivingRouter? = null
    private var drivingSession: DrivingSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        DirectionsFactory.initialize(this)

        setContentView(R.layout.driving)
        super.onCreate(savedInstanceState)

        mapView = findViewById(R.id.mapview)
        mapView!!.map.move(CameraPosition(
                SCREEN_CENTER, 5f, 0f, 0f))
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
        mapObjects = mapView!!.map.mapObjects.addCollection()

        submitRequest()
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

    override fun onDrivingRoutes(routes: List<DrivingRoute>) {
        for (route in routes) {
            mapObjects!!.addPolyline(route.geometry)
        }
    }

    override fun onDrivingRoutesError(error: Error) {
        var errorMessage = getString(R.string.unknown_error_message)
        if (error is RemoteError) {
            errorMessage = getString(R.string.remote_error_message)
        } else if (error is NetworkError) {
            errorMessage = getString(R.string.network_error_message)
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun submitRequest() {
        val options = DrivingOptions()
        val requestPoints = ArrayList<RequestPoint>()
        requestPoints.add(RequestPoint(
                ROUTE_START_LOCATION,
                RequestPointType.WAYPOINT,
                null))
        requestPoints.add(RequestPoint(
                ROUTE_END_LOCATION,
                RequestPointType.WAYPOINT, null))
        drivingSession = drivingRouter!!.requestRoutes(requestPoints, options, this)
    }
}
