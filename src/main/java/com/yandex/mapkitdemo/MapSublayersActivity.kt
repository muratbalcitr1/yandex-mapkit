package com.yandex.mapkitdemo

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.NonNull

import com.yandex.mapkit.ConflictResolutionMode
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.LayerNames
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PolygonMapObject
import com.yandex.mapkit.map.Sublayer
import com.yandex.mapkit.map.SublayerFeatureType
import com.yandex.mapkit.map.SublayerManager
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.AnimatedImageProvider

import java.util.ArrayList

/**
 * This example shows how to reorder sublayers and use conflict resolving.
 */
class MapSublayersActivity : Activity() {
    /**
     * Replace "your_api_key" with a valid developer key.
     * You can get it at the https://developer.tech.yandex.ru/ website.
     */
    private val MAPKIT_API_KEY = "843679b6-ddc8-4f6a-a4ca-40c7ea099ce8"
    private val CAMERA_TARGET = Point(59.951029, 30.317181)

    private var mapView: MapView? = null
    private var sublayerManager: SublayerManager? = null
    private var mapObjects: MapObjectCollection? = null

    private val inputListener = object : InputListener {
        override fun onMapTap(@NonNull map: Map, @NonNull point: Point) {}

        override fun onMapLongTap(@NonNull map: Map, @NonNull point: Point) {
            val provider = AnimatedImageProvider.fromAsset(applicationContext, "animation.png")
            val iconStyle = IconStyle().setScale(4f)
            mapObjects!!.addPlacemark(point, provider, iconStyle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        setContentView(R.layout.map_sublayers)
        super.onCreate(savedInstanceState)
        mapView = findViewById(R.id.mapview)
        mapView!!.map.move(
                CameraPosition(CAMERA_TARGET, 16.0f, 0.0f, 45.0f))

        sublayerManager = mapView!!.map.sublayerManager
        mapObjects = mapView!!.map.mapObjects

        val circle = Circle(CAMERA_TARGET, 100f)
        mapObjects!!.addCircle(circle, Color.RED, 2f, Color.WHITE)

        val points = ArrayList<Point>()
        points.add(Point(59.949911, 30.316560))
        points.add(Point(59.949121, 30.316008))
        points.add(Point(59.949441, 30.318132))
        points.add(Point(59.950075, 30.316915))
        points.add(Point(59.949911, 30.316560))
        val polygon = Polygon(LinearRing(points), ArrayList())
        val polygonMapObject = mapObjects!!.addPolygon(polygon)
        polygonMapObject.fillColor = 0x3300FF00
        polygonMapObject.strokeWidth = 3.0f
        polygonMapObject.strokeColor = Color.GREEN

        // Example of changing the order of sublayers
        val switchSublayersOrder = findViewById<Button>(R.id.switch_sublayers_order)
        switchSublayersOrder.setOnClickListener(View.OnClickListener {
            val buildingsSublayerIndex = sublayerManager!!.findFirstOf(LayerNames.getBuildingsLayerName(), SublayerFeatureType.MODELS)
            if (buildingsSublayerIndex == null) {
                Toast.makeText(applicationContext,
                        "Buildings sublayer not found.", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            val mapObjectGeometrySublayerIndex = sublayerManager!!.findFirstOf(LayerNames.getMapObjectsLayerName(), SublayerFeatureType.GROUND)
            if (mapObjectGeometrySublayerIndex == null) {
                Toast.makeText(applicationContext,
                        "MapObject ground sublayer not found!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            if (buildingsSublayerIndex < mapObjectGeometrySublayerIndex) {
                sublayerManager!!.moveAfter(buildingsSublayerIndex, mapObjectGeometrySublayerIndex)
                switchSublayersOrder.text = getString(R.string.sublayer_buildings_before_mo_geometry)
            } else {
                sublayerManager!!.moveAfter(mapObjectGeometrySublayerIndex, buildingsSublayerIndex)
                switchSublayersOrder.text = getString(R.string.sublayer_buildings_after_mo_geometry)
            }
        })

        // Example of conflict resolving
        val mapObjectPlacemarkSublayerIndex = sublayerManager!!.findFirstOf(LayerNames.getMapObjectsLayerName(), SublayerFeatureType.PLACEMARKS)
        if (mapObjectPlacemarkSublayerIndex != null) {
            val sublayer = sublayerManager!!.get(mapObjectPlacemarkSublayerIndex)

            // The placemarks from lower sublayers will be displaced in case of conflict
            sublayer!!.modeAgainstPlacemarks = ConflictResolutionMode.MAJOR

            // The labels from lower sublayers will be displaced in case of conflict
            sublayer.modeAgainstLabels = ConflictResolutionMode.MAJOR
        }

        // Client code must retain strong reference to the listener.
        mapView!!.map.addInputListener(inputListener)
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
