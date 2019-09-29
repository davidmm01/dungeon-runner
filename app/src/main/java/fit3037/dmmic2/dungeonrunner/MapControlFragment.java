package fit3037.dmmic2.dungeonrunner;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/*
* This class supports the map that is viewed on the Dungeon Results screen.
* */


public class MapControlFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener{

    private GoogleMap mMap;
    private OnMapClicked mListener;
    private ArrayList<LatLng> mSavedLocations;
    private ArrayList<Integer> mSkipList;
    private LatLng mCurrentLoc;



    public MapControlFragment() {
        mSavedLocations = new ArrayList<>();
        // if nothing gets found, go to valhalla (monash caufield)
        mCurrentLoc = new LatLng(-37.8770, 145.0443);
    }


    public void initFragment(OnMapClicked listener,
                             ArrayList<LatLng> locations, ArrayList<Integer> skipList) {
        mListener = listener;
        mSavedLocations = locations;
        mSkipList = skipList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_control, container, false);
        SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFrag.getMapAsync(this);
        return v;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng start;
        try {
            start = mSavedLocations.get(0);
        }
        catch (IndexOutOfBoundsException e) {
            start = mCurrentLoc;
        }

        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                start, 15));
        updateMapMarkers();
    }


    public void updateMapMarkers() {
        // -1 is used here as a no skip scenario holder
        int currentSkipIndex = -1;
        int currentSkipValue = -1;

        // if there are points to skip drawing, prepare to use them
        if (mSkipList.size() != 0){
            currentSkipIndex = 0;
            currentSkipValue = mSkipList.get(0);
        }

        if (mMap != null) {
            mMap.clear();

            /*
            * Some snippets here from:
            * https://stackoverflow.com/questions/16311076/how-to-dynamically-add-polylines-from-an-arraylist/25978663
            * */

            for (int i = 0; i < mSavedLocations.size() - 1; i++) {

                // if the current index of mSavedLocations should not be graphed...
                if (currentSkipValue == i){
                    // then instead of drawing a poly line, get the next index of which we should be
                    // skipping
                    currentSkipIndex += 1;

                    if (currentSkipIndex < mSkipList.size()) {
                        currentSkipValue = mSkipList.get(currentSkipIndex);
                    }
                    else{
                        currentSkipValue = -1;
                        currentSkipIndex = -1;
                    }
                }
                // else there is no need to skip, continue drawing polylines
                else {
                    LatLng src = mSavedLocations.get(i);
                    LatLng dest = mSavedLocations.get(i + 1);

                    // mMap is the Map Object
                    Polyline line = mMap.addPolyline(
                            new PolylineOptions().add(
                                    new LatLng(src.latitude, src.longitude),
                                    new LatLng(dest.latitude, dest.longitude)
                            ).width(10).color(Color.BLACK).geodesic(true)
                    );
                }
            }
        }
    }

    public void setFocus(LatLng loc) {
        mCurrentLoc = loc;
        if(mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
        }
    }


    @Override
    public void onMapLongClick(final LatLng latLng) {
        final EditText inputText = new EditText(getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage("Please enter a name for the new location")
                .setView(inputText)
                .setPositiveButton("Create",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(mListener != null) {
                                    mListener.onMapClicked(inputText.getText().toString(),
                                            latLng);
                                }
                            }
                        })
                .setNegativeButton("Cancel", null);
        Dialog dialog = builder.create();
        dialog.show();
    }


    public interface OnMapClicked {
        void onMapClicked(String locName, LatLng position);
    }


}
