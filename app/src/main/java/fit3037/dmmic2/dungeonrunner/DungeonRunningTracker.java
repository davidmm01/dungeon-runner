package fit3037.dmmic2.dungeonrunner;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


/*
* This activity coordinates the tracking of the user and provides them with the start/pause/finish
* interface.  It allows them to start and stop tracking of their movement as appropriate.
* It does not implement the location things as was done in the tute sheets, it instead calls a
* service to do this for us.  This is because the in-activity implementation does not account for
* the need of the onLocationCalled method to continue operation when the screen is off.
*
* Implementing the GPS as a service was done with the help of:
* https://www.youtube.com/watch?v=lvcGh2ZgHeA
* https://github.com/miskoajkula/GPS_service
*
* Help with chronometer was thanks to the follow video:
* https://www.youtube.com/watch?v=LzfaWf7_iHw&t=173s
*
* Help with distance calculation:
* https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
*
* */

/*
* ----How paused runs are handled in the code so distance and run map make sense------
* The user should have the ability to pause a run when they wish, and continue it later.
* In order to account for this, the follow things are done:
* When pause is hit:
*   -Set skipNext flag to true
*       Doing this makes sure that the next call of onResume does not execute the distance function
*       with the last coordinate before pause was hit, and the first coordinate after pause was hit.
*       This way the next distance to be calculated will be with two coordinates retrieved after
*       having resumed the run.  Failure to do this would cause the side effect of a user being able
*       to hit pause, travel some distance, hit resume, and have the distance added to their tracker
*       without time added.  So in theory, you could start the app, jump in a car, pause, drive
*       50km, unpause, and have an impossibly high pace with very long kilometers, and get great
*       rewards.  We can't have that!
*  -Take note of the index of mRunningCoordinates, saved in mSkipIndicies
*       By recording the index of mRunningCoordinates we can ensure that a pause is not recorded
*       on the users map.  In case they travel during the pause, we must avoid having a large
*       straight line on the map that wont actually be indicative of the dungeon they undertook.
*       Pause indexes are passed to the Dungeon Results, that knows to not graph at the time of
*       a pause.
* */


public class DungeonRunningTracker extends AppCompatActivity {
    // Request code we will be checking for
    private static final int LOCATION_REQUEST_CODE = 1337;

    private boolean mCanAccessLocation;

    private LatLng mCurrentLocation;
    private double mCurrentElevation;
    private double totalDistance = 0;

    private ArrayList<LatLng> mRunningCoordinates = new ArrayList<LatLng>();
    private boolean mSkipNext = false;
    private ArrayList<Integer> mSkipIndicies = new ArrayList<Integer>();

    Button mStartButton;
    Button mPauseButton;
    Button mResumeButton;
    Button mFinishButton;

    private BroadcastReceiver broadcastReceiver;

    private Chronometer mChronometer;
    private long lastPause;

    // FOR DEBUGGING
    public int count_calls_for_coordinates = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dungeon_running_tracker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dungeon Tracker");

        Intent intent = getIntent();
        final DungeonLevel selection = intent.getParcelableExtra("dungeonLevel");

        TextView displayCoordsView = (TextView) findViewById(R.id.coordsView);
        TextView displayCounter = (TextView) findViewById(R.id.countTimesLocChangedView);

        mStartButton = findViewById(R.id.startButton);
        mPauseButton = findViewById(R.id.pauseButton);
        mResumeButton = findViewById(R.id.resumeButton);
        mFinishButton = findViewById(R.id.finishButton);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);


        // Start, Pause, Resume and Finish button are disable on create
        mStartButton.setEnabled(false);
        mPauseButton.setEnabled(false);
        mResumeButton.setEnabled(false);
        mFinishButton.setEnabled(false);

        mCanAccessLocation = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
        // If we do not have permissions then request them
        if(!mCanAccessLocation) {
            requestPermissions();
        } else {
            Log.d("LOCATION FUNCTIONALITY", "We have permission to do location stuff");
            // Since we have permission, enable the start button
            mStartButton.setEnabled(true);
        }

        // On Click: Start a new fitness tracking session
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.start();
                // disable the start button
                mStartButton.setEnabled(false);
                // enable the pause button
                mPauseButton.setEnabled(true);

                // start up the gps service
                Intent gps = new Intent(getApplicationContext(), GPSService.class);
                startService(gps);
            }
        });

        // On Click: Pause current session
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {

                lastPause = SystemClock.elapsedRealtime();
                mChronometer.stop();

                // enable the resume button
                mResumeButton.setEnabled(true);
                // enable the finish button
                mFinishButton.setEnabled(true);
                // disable the pause button
                mPauseButton.setEnabled(false);

                // stop the gps service
                Intent gps = new Intent(getApplicationContext(), GPSService.class);
                stopService(gps);

                // this flag is set true so distance is not calculated between last point before
                // pausing and first point after resuming (it is reset to false by call of distance
                // function)
                mSkipNext = true;
                // store the index of the running coord at the skip spot
                if (mRunningCoordinates.size() != 0) {
                    mSkipIndicies.add(mRunningCoordinates.size() - 1);
                }
            }
        });

        // On Click: Resume current session
        mResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {

                mChronometer.setBase(mChronometer.getBase() + SystemClock.elapsedRealtime() - lastPause);
                mChronometer.start();

                // disable the resume button
                mResumeButton.setEnabled(false);
                // enable the pause button
                mPauseButton.setEnabled(true);
                // disable the finish button
                mFinishButton.setEnabled(false);

                // start up the gps service once more
                Intent gps = new Intent(getApplicationContext(), GPSService.class);
                startService(gps);
            }
        });

        // On Click: Finish current session
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                Log.d("LOCATION FUNCTIONALITY", "total times we called onLocationChanged: " + count_calls_for_coordinates);
                Intent newIntent = new Intent(DungeonRunningTracker.this, DungeonResults.class);
                CharSequence finalTime = mChronometer.getText().toString();
                Log.d("INTENT EXTRA", "finalTime before intent: " + finalTime);
                Log.d("INTENT EXTRA", "chronometer test to get final: " + mChronometer.getText());
                newIntent.putExtra("finalTime", finalTime);
                int roundedDistance = (int) Math.rint(totalDistance);
                newIntent.putExtra("finalDistance", roundedDistance);
                newIntent.putExtra("coordinatesList", mRunningCoordinates);
                newIntent.putExtra("skipIndicies", mSkipIndicies);
                newIntent.putExtra("dungeonSelection", selection);
                // Go to Dungeon Results
                startActivity(newIntent);
            }
        });
    }

    public void requestPermissions() {
        // We are checking if we need permission for fine location
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // http://stackoverflow.com/questions/41310510/what-is-the-difference-between-shouldshowrequestpermissionrationale-and-requestp
            new AlertDialog.Builder(DungeonRunningTracker.this)
                    .setTitle("Permission required")
                    .setMessage("This is a map application. You need to enable" +
                            "location services for it to work!")
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(DungeonRunningTracker.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {@Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                    })
                    .show();
        } else {
            // We do not need to show the user info we can just request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // Check with request code has been given to us
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                // This is a location permission request so lets handle it
                if(grantResults.length > 0) {
                    // Can access coarse is equal to
                    mCanAccessLocation = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default:
                break;
        }

        // If at this point we have permissions for location, allow user to click start
        mStartButton = findViewById(R.id.startButton);
        mStartButton.setEnabled(true);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Log.d("LOCATIONFUNCTIONALITY", "onReceieve called, must have got an update from the service");
                    count_calls_for_coordinates += 1;  // FOR DEBUG

                    // display the coords on screen
                    TextView displayCoordsView = (TextView) findViewById(R.id.coordsView);
                    String extraCoordinateAltitudeString = intent.getStringExtra("coordinates");
                    displayCoordsView.setText(extraCoordinateAltitudeString);

                    // FOR DEBUG: display how many times this has been called
                    TextView displayCounter = (TextView) findViewById(R.id.countTimesLocChangedView);
                    displayCounter.setText("Polls: " + count_calls_for_coordinates);

                    // Convert the string into a nice LatLng that can be used by google map in
                    // next activity
                    // https://stackoverflow.com/questions/27261670/convert-string-to-latlng

                    // save the previous coordinates and elevation
                    LatLng mPreviousLocation = mCurrentLocation;
                    double mPreviousElevation = mCurrentElevation;

                    // extract the latitude, longitude and elevation from the string extra
                    String[] chunkedCoordinateAltitude =  extraCoordinateAltitudeString.split(" ");
                    double parsedLatitude = Double.parseDouble(chunkedCoordinateAltitude[0]);
                    double parsedLongitude = Double.parseDouble(chunkedCoordinateAltitude[1]);
                    double parsedElevation = Double.parseDouble(chunkedCoordinateAltitude[2]);

                    // update the current location and elevation
                    mCurrentLocation = new LatLng(parsedLatitude, parsedLongitude);
                    mCurrentElevation = parsedElevation;
                    Log.d("LOCATION FUNCTIONALITY", "the location is: " + mCurrentLocation.toString());

                    // if current location is null we can't do anything, so wait until its not null
                    if (mCurrentLocation != null) {

                        // Don't bother saving previous location if it is the exact same as current
                        // location, as this will only create more work for map creation and
                        // distance calculation with no gain
                        if (mCurrentLocation != mPreviousLocation){
                            mRunningCoordinates.add(mCurrentLocation);
                            Log.d("COORDS ARRAY BUILDING", "the location is: " + mCurrentLocation.toString());
                            Log.d("ALTITUDES", "current: " + mCurrentElevation);
                            Log.d("ALTITUDES", "prev: " + mPreviousElevation);
                            // if the previous location is also not null, we can go on with a
                            // distance calculation
                            if (mPreviousLocation != null) {
                                // if the skip next flag is set, then skip the next distance
                                // calculation, and set the flag to false.  This flag is set by
                                // the pause button to address concerns of distance between pauses
                                // being added up.
                                if (mSkipNext){
                                    mSkipNext = false;
                                }
                                else {
                                    // we have everything right to calculate distance!
                                    double segmentDistance = distance(
                                            mPreviousLocation.latitude, mCurrentLocation.latitude,
                                            mPreviousLocation.longitude, mCurrentLocation.longitude,
                                            mPreviousElevation, mCurrentElevation);
                                    totalDistance += segmentDistance;
                                    TextView distanceCounter = (TextView) findViewById(R.id.distanceCoveredDebugViewer);
                                    distanceCounter.setText(Math.round(totalDistance) + "m");
                                }
                            }
                        }
                    }
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    /*
    * This way of getting the distance between two lat long coordiantes is known as the  Haversine
    * Method.  I prefer to use this as I minimise the use of external API's where possible (and it
    * is supposedly more accurate than a similar function offered by the google toolkit).
    *
    * Taken from:
    * https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
    * */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

}
