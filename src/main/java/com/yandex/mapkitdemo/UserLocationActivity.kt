package com.yandex.mapkitdemo

import android.app.Activity
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle

import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CompositeIcon
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider

/**
 * This example shows how to display and customize user location arrow on the map.
 */
class UserLocationActivity : Activity(), UserLocationObjectListener {
    /**
     * Replace "your_api_key" with a valid developer key.
     * You can get it at the https://developer.tech.yandex.ru/ website.
     */
    private val MAPKIT_API_KEY = "843679b6-ddc8-4f6a-a4ca-40c7ea099ce8"

    private var mapView: MapView? = null
    private var userLocationLayer: UserLocationLayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        setContentView(R.layout.user_location)
        super.onCreate(savedInstanceState)
        mapView = findViewById(R.id.mapview)
        mapView!!.map.isRotateGesturesEnabled = false
        mapView!!.map.move(CameraPosition(Point(0.0, 0.0), 14f, 0f, 0f))

        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mapView!!.mapWindow)
        userLocationLayer!!.isVisible = true
        userLocationLayer!!.isHeadingEnabled = true

        userLocationLayer!!.setObjectListener(this)
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

    override fun onObjectAdded(userLocationView: UserLocationView) {
        userLocationLayer!!.setAnchor(
                PointF((mapView!!.width * 0.5).toFloat(), (mapView!!.height * 0.5).toFloat()),
                PointF((mapView!!.width * 0.5).toFloat(), (mapView!!.height * 0.83).toFloat()))

        userLocationView.arrow.setIcon(ImageProvider.fromResource(
                this, R.drawable.user_arrow))

        val pinIcon = userLocationView.pin.useCompositeIcon()

        pinIcon.setIcon(
                "icon",
                ImageProvider.fromResource(this, R.drawable.icon),
                IconStyle().setAnchor(PointF(0f, 0f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(0f)
                        .setScale(1f)
        )

        pinIcon.setIcon(
                "pin",
                ImageProvider.fromResource(this, R.drawable.search_result),
                IconStyle().setAnchor(PointF(0.5f, 0.5f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(1f)
                        .setScale(0.5f)
        )

        userLocationView.accuracyCircle.fillColor = Color.BLUE
    }

    override fun onObjectRemoved(view: UserLocationView) {}

    override fun onObjectUpdated(view: UserLocationView, event: ObjectEvent) {}
}
