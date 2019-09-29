package fit3037.dmmic2.dungeonrunner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/*
* This activity is for viewing the map of a dungeon record that has been previously completed.
* */

public class DungeonRecordMapDetails extends AppCompatActivity {

    DungeonRecord mRecord;
    ArrayList<LatLng> mCoordinatesList;
    ArrayList<Integer> mSkipList;
    private MapControlFragment mMapFragment;
    private FrameLayout mFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dungeon_record_map_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        mRecord = intent.getParcelableExtra("record");
        getSupportActionBar().setTitle(mRecord.getDate());

        // Map stuff
        mCoordinatesList = DungeonRecord.convertCoordinatesStringToArrayLatLng(mRecord.getCoords());
        mSkipList = DungeonRecord.convertSkipStringToArrayInteger(mRecord.getSkips());

        mMapFragment = new MapControlFragment();
        mMapFragment.initFragment(null, mCoordinatesList, mSkipList);
        mFrame = findViewById(R.id.mapRecordDetail);
        getSupportFragmentManager().beginTransaction().add(mFrame.getId(), mMapFragment).commit();

    }

}
