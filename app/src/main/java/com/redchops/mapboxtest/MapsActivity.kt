package com.redchops.mapboxtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mapbox.bindgen.Value.valueOf
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.Style
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.boolean
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.featureState
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.literal
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.switchCase
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.VectorSource

class MapsActivity : AppCompatActivity() {

    private var mapView: MapView? = null
    private var selectedId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mapView = findViewById(R.id.mapView)
        val mapboxMap = mapView!!.getMapboxMap()
        mapboxMap.loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            style ->
            mapboxMap.setCamera(
                CameraOptions.Builder().center(Point.fromLngLat(-82.9718, 38.7581)).zoom(13.0).build()
            )
            addParcelsToMap(style)
        }
        mapboxMap.addOnCameraChangeListener { onCameraChangeListener() }
    }

    private fun onCameraChangeListener() {
        val mapboxMap = mapView!!.getMapboxMap()
        val center = mapboxMap.cameraState.center
        val coordinate = mapboxMap.pixelForCoordinate(center)
        mapboxMap.queryRenderedFeatures(coordinate, RenderedQueryOptions(listOf("parcels-line", "parcels-fill"), null)) {
            features ->
            if (features.value != null && features.value!!.size > 0) {
                val feature = features.value!![0].feature
                val featureId = feature.id()
                if (featureId != null && featureId != this.selectedId) {
                    mapboxMap.setFeatureState("parcels", "sci-parcels-geojson-79v4aw",
                        featureId, valueOf(hashMapOf("hovered" to valueOf(true)))
                    )
                    this.selectedId = featureId
                } else {
                    mapboxMap.setFeatureState("parcels", "sci-parcels-geojson-79v4aw",
                        this.selectedId!!, valueOf(hashMapOf("hovered" to valueOf(false))))
                }
            }
        }
    }

    private fun addParcelsToMap(style: Style) {
        val sourceBuilder: VectorSource.Builder = VectorSource.Builder("parcels")
            .tiles(listOf("https://b.tiles.mapbox.com/v4/ovrdc.9h65l843/{z}/{x}/{y}.vector.pbf?access_token=pk.eyJ1Ijoib3ZyZGMiLCJhIjoiY2pjbHpsNTNhMGxpOTJ3cm4xODE3bGpsaSJ9.YvQrP4s_CIuz365le0eMQg"))
            .maxzoom(22)
            .minzoom(12)

        style.addSource(
            VectorSource(sourceBuilder)
        )

        val parcelsLineLayer: LineLayer = LineLayer("parcels-line", "parcels")
            .sourceLayer("sci-parcels-geojson-79v4aw")
            .lineColor("#9e9e9e")
            .lineWidth(
                switchCase(
                    boolean(
                        featureState(literal("selected")),
                        literal(false)
                    ),
                    literal(4),
                    literal(2)
                )
            )
        style.addLayer(parcelsLineLayer)

        val parcelsFillLayer: FillLayer = FillLayer("parcels-fill", "parcels")
            .sourceLayer("sci-parcels-geojson-79v4aw")
            .fillOutlineColor("transparent")
            .fillColor(
                switchCase(
                    boolean(
                        featureState(literal("selected")),
                        literal(false)
                    ),
                    literal("rgba(0, 192, 128, 0.4)"),
                    boolean(
                        featureState(literal("hovered")),
                        literal(false)
                    ),
                    literal("rgba(248, 204, 137, 0.6)"),
                    literal("transparent")
                )
            )
        style.addLayer(parcelsFillLayer)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }
}