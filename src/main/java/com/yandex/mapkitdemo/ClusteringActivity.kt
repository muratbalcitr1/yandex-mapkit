package com.yandex.mapkitdemo

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Paint.FontMetrics
import android.graphics.Paint.Style

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Cluster
import com.yandex.mapkit.map.ClusterListener
import com.yandex.mapkit.map.ClusterTapListener
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.ClusterizedPlacemarkCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

import java.util.ArrayList
import java.util.Arrays
import java.util.Random

/**
 * This example shows how to add a collection of clusterized placemarks to the map.
 */
class ClusteringActivity : Activity(), ClusterListener, ClusterTapListener {
    /**
     * Replace "your_api_key" with a valid developer key.
     * You can get it at the https://developer.tech.yandex.ru/ website.
     */
    private val MAPKIT_API_KEY = "843679b6-ddc8-4f6a-a4ca-40c7ea099ce8"

    private var mapView: MapView? = null
    private val CLUSTER_CENTERS = Arrays.asList(
            Point(55.756, 37.618),
            Point(59.956, 30.313),
            Point(56.838, 60.597),
            Point(43.117, 131.900),
            Point(56.852, 53.204)
    )

    inner class TextImageProvider(private val text: String) : ImageProvider() {
        override fun getId(): String {
            return "text_$text"
        }

        override fun getImage(): Bitmap {
            val metrics = DisplayMetrics()
            val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            manager.defaultDisplay.getMetrics(metrics)

            val textPaint = Paint()
            textPaint.textSize = FONT_SIZE * metrics.density
            textPaint.textAlign = Align.CENTER
            textPaint.style = Style.FILL
            textPaint.isAntiAlias = true

            val widthF = textPaint.measureText(text)
            val textMetrics = textPaint.fontMetrics
            val heightF = Math.abs(textMetrics.bottom) + Math.abs(textMetrics.top)
            val textRadius = Math.sqrt((widthF * widthF + heightF * heightF).toDouble()).toFloat() / 2
            val internalRadius = textRadius + MARGIN_SIZE * metrics.density
            val externalRadius = internalRadius + STROKE_SIZE * metrics.density

            val width = (2 * externalRadius + 0.5).toInt()

            val bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val backgroundPaint = Paint()
            backgroundPaint.isAntiAlias = true
            backgroundPaint.color = Color.RED
            canvas.drawCircle((width / 2).toFloat(), (width / 2).toFloat(), externalRadius, backgroundPaint)

            backgroundPaint.color = Color.WHITE
            canvas.drawCircle((width / 2).toFloat(), (width / 2).toFloat(), internalRadius, backgroundPaint)

            canvas.drawText(
                    text,
                    (width / 2).toFloat(),
                    width / 2 - (textMetrics.ascent + textMetrics.descent) / 2,
                    textPaint)

            return bitmap
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        setContentView(R.layout.clustering)
        super.onCreate(savedInstanceState)

        mapView = findViewById(R.id.mapview)
        mapView!!.map.move(CameraPosition(
                CLUSTER_CENTERS[0], 3f, 0f, 0f))
        val imageProvider = ImageProvider.fromResource(
                this@ClusteringActivity, R.drawable.search_result)

        // Note that application must retain strong references to both
        // cluster listener and cluster tap listener
        val clusterizedCollection = mapView!!.map.mapObjects.addClusterizedPlacemarkCollection(this)

        val points = createPoints(PLACEMARKS_NUMBER)
        clusterizedCollection.addPlacemarks(points, imageProvider, IconStyle())

        // Placemarks won't be displayed until this method is called. It must be also called
        // to force clusters update after collection change
        clusterizedCollection.clusterPlacemarks(60.0, 15)
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


    override fun onClusterAdded(cluster: Cluster) {
        // We setup cluster appearance and tap handler in this method
        cluster.appearance.setIcon(
                TextImageProvider(Integer.toString(cluster.size)))
        cluster.addClusterTapListener(this)
    }

    override fun onClusterTap(cluster: Cluster): Boolean {
        Toast.makeText(
                applicationContext,
                String.format(getString(R.string.cluster_tap_message), cluster.size),
                Toast.LENGTH_SHORT).show()

        // We return true to notify map that the tap was handled and shouldn't be
        // propagated further.
        return true
    }

    private fun createPoints(count: Int): List<Point> {
        val points = ArrayList<Point>()
        val random = Random()

        for (i in 0 until count) {
            val clusterCenter = CLUSTER_CENTERS[random.nextInt(CLUSTER_CENTERS.size)]
            val latitude = clusterCenter.latitude + Math.random() - 0.5
            val longitude = clusterCenter.longitude + Math.random() - 0.5

            points.add(Point(latitude, longitude))
        }

        return points
    }

    companion object {
        private val FONT_SIZE = 15f
        private val MARGIN_SIZE = 3f
        private val STROKE_SIZE = 3f
        private val PLACEMARKS_NUMBER = 2000
    }
}
