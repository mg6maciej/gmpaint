package com.mdevcon.gmpaint;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaintActivity extends ActionBarActivity implements View.OnClickListener, GoogleMap.OnMarkerDragListener {

    private void getPolyLineWithMarkerAtIndex(Marker marker)
    {
        Polyline line = markerLines.get(marker);
        int index = markerIndex.get(marker);
        List<LatLng> points = line.getPoints();
          points.set(index, marker.getPosition());
        line.setPoints(points);


    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        getPolyLineWithMarkerAtIndex(marker);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        getPolyLineWithMarkerAtIndex(marker);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        getPolyLineWithMarkerAtIndex(marker);
    }

    enum Mode {
        ADDING_MARKER,DRAWING_LINE,ALTER_LINES;
    }

    private View placeholder;
    private GoogleMap googleMap;
    private Mode mode = Mode.ADDING_MARKER;
    private Button drawLine;
    private ArrayList<MarkerOptions> markers;
    private ArrayList<Marker> tempMarkers;
    private ArrayList<PolylineOptions> lines;
    private ArrayList<Polyline> drawnLines;
    private Map<Marker , Polyline> markerLines;
    private Map<Marker, Integer> markerIndex;
    private GestureDetector gestureDetector;
    private Polyline line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paint_activity);
        drawnLines = new ArrayList<Polyline>();
        markerIndex = new HashMap<Marker, Integer>();
        markerLines = new HashMap<Marker, Polyline>();
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {

                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Point point = new Point(Math.round(e.getX()), Math.round(e.getY()));
                LatLng latLng = googleMap.getProjection().fromScreenLocation(point);
                if (mode == Mode.ADDING_MARKER) {
                    MarkerOptions marker = new MarkerOptions().position(latLng);
                    markers.add(marker);
                    googleMap.addMarker(marker);
                }else if(mode == Mode.DRAWING_LINE){
                    if(line == null){
                        PolylineOptions polyLineOptions = new PolylineOptions().add(latLng);
                        line =  googleMap.addPolyline(polyLineOptions);
                        lines.add( polyLineOptions );
                        drawnLines.add(line);
                    }else{
                        List<LatLng> points = line.getPoints();
                        points.add(latLng);
                        line.setPoints(points);

                        lines.get( lines.size()-1 ).add(latLng);

                    }
                }
                return true;
            }


        });
        findViewById(R.id.enable).setOnClickListener(this);
        findViewById(R.id.add_marker_edit_mode).setOnClickListener(this);
        findViewById(R.id.alter_lines).setOnClickListener(this);
        drawLine = (Button)findViewById(R.id.draw_line);
        drawLine.setOnClickListener(this);
        if (savedInstanceState != null) {
            markers = savedInstanceState.getParcelableArrayList("markers");
            lines = savedInstanceState.getParcelableArrayList("lines");
        } else {
            markers = new ArrayList<MarkerOptions>();
            lines = new ArrayList<PolylineOptions>();
        }

        tempMarkers = new ArrayList<Marker>();

        SupportMapFragment f = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.paint_map);
        googleMap = f.getMap();
        for (MarkerOptions marker : markers) {
            googleMap.addMarker(marker);
        }

        for (PolylineOptions line : lines) {
            googleMap.addPolyline( line );
        }

        placeholder = findViewById(R.id.view_placeholder);
        placeholder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });


    }

    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelableArrayList("markers", markers);
        bundle.putParcelableArrayList("lines", lines );
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.enable:
                if (placeholder.getVisibility() == View.VISIBLE) {
                    placeholder.setVisibility(View.GONE);
                    findViewById(R.id.add_marker_edit_mode).setVisibility(View.GONE);
                } else {
                    placeholder.setVisibility(View.VISIBLE);
                    findViewById(R.id.add_marker_edit_mode).setVisibility(View.VISIBLE);
                }
                break;
            case R.id.add_marker_edit_mode:
                mode = Mode.ADDING_MARKER;
                break;
            case R.id.draw_line:
                mode = Mode.DRAWING_LINE;
                line = null;
                break;
            case R.id.alter_lines:
                mode = Mode.ALTER_LINES;
                for( Polyline lineOptions : drawnLines ) {
                    List<LatLng> latLngs = lineOptions.getPoints();
                    for(int i=0;i<latLngs.size();i++ )
                    {
                        LatLng latLng = latLngs.get(i);
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true);

                        Marker marker = googleMap.addMarker( markerOptions );
                        markerLines.put(marker,lineOptions);
                        markerIndex.put(marker,i);
                        tempMarkers.add( marker );
                    }
                }
                googleMap.setOnMarkerDragListener(this);
                break;
            default:
                break;
        }

        if( mode != Mode.ALTER_LINES )
        {
            for( Marker marker : tempMarkers )
            {
                marker.remove();
            }

            tempMarkers.clear();
        }
    }
}
