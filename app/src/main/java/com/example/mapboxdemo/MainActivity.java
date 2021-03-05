package com.example.mapboxdemo;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import unl.core.Bounds;
import unl.core.UnlCore;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private MapboxMap mapboxMap;
    private static final LatLng INITIAL_COORDINATES = new LatLng(52.3676, 4.9041);
    private static final int INITIAL_ZOOM = 18;
    private static final int INITIAL_ANIMATION_DURATION_IN_MS = 2000;
    private static final String UNL_GRID_LINES_COLOR = "#C0C0C0";
    private static final int MIN_GRID_LINES_ZOOM = 17;

    private LineString fromLngLats(double[][] coordinates) {
        ArrayList<com.mapbox.geojson.Point> converted = new ArrayList<>(coordinates.length);
        for (double[] coordinate : coordinates) {
            converted.add(com.mapbox.geojson.Point.fromLngLat(coordinate[0], coordinate[1]));
        }
        return LineString.fromLngLats(converted);
    }

    private List<Feature> getGridLineFeatures() {
        List<Feature> features = new ArrayList<>();

        if (mapboxMap.getCameraPosition().zoom < MIN_GRID_LINES_ZOOM) {
            return features;
        }

        VisibleRegion visibleRegion = mapboxMap.getProjection().getVisibleRegion();
        Bounds bounds = new Bounds(
                visibleRegion.latLngBounds.getLatNorth(), visibleRegion.latLngBounds.getLonEast(),
                visibleRegion.latLngBounds.getLatSouth(), visibleRegion.latLngBounds.getLonWest()
        );
        List<double[][]> gridLines = UnlCore.gridLines(bounds);
        for (double[][] line : gridLines) {
            features.add(
                    Feature.fromGeometry(fromLngLats(line))
            );
        }
        return features;
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            MainActivity.this.mapboxMap = mapboxMap;
            mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                FeatureCollection gridLinesFeatureCollection = FeatureCollection.fromFeatures(getGridLineFeatures());
                style.addSource(new
                        GeoJsonSource("unl-grid-lines-source", gridLinesFeatureCollection));
                Layer gridLinesLayer = new LineLayer("unlGridLinesLayer", "unl-grid-lines-source").withProperties(
                        PropertyFactory.lineWidth(1f),
                        PropertyFactory.lineColor(Color.parseColor(UNL_GRID_LINES_COLOR))
                );
                gridLinesLayer.setMinZoom(MIN_GRID_LINES_ZOOM);
                style.addLayer(gridLinesLayer);
                CameraPosition position = new CameraPosition.Builder()
                        .target(INITIAL_COORDINATES)
                        .zoom(INITIAL_ZOOM)
                        .build();
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), INITIAL_ANIMATION_DURATION_IN_MS);
                mapboxMap.addOnCameraIdleListener(() -> {
                            GeoJsonSource geoJsonSource = style.getSourceAs("unl-grid-lines-source");
                            if (geoJsonSource != null) {
                                FeatureCollection newGridLinesFeatureCollection = FeatureCollection.fromFeatures(getGridLineFeatures());
                                geoJsonSource.setGeoJson(newGridLinesFeatureCollection);
                            }
                        }
                );

                findViewById(R.id.fab_layer_toggle).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toggleLayer();
                    }
                });
            });
        });
    }

    private void toggleLayer() {
        mapboxMap.getStyle(style -> {
            Layer layer = style.getLayer("unlGridLinesLayer");
            if (layer != null) {
                if (VISIBLE.equals(layer.getVisibility().getValue())) {
                    layer.setProperties(visibility(NONE));
                } else {
                    layer.setProperties(visibility(VISIBLE));
                }
            }
        });
    }
}

