package fit3037.dmmic2.dungeonrunner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.zetterstrom.com.forecast.ForecastClient;
import android.zetterstrom.com.forecast.ForecastConfiguration;
import android.zetterstrom.com.forecast.models.Forecast;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
* This activity is used to display the outcome of the dungeon to the user, complete with times,
* distance, pace, and a map of the route.  This class is also responsible for saving the dungeon
* record and the item rewarded.
* */

public class DungeonResults extends AppCompatActivity implements MapControlFragment.OnMapClicked {

    String mDisplayTime;
    int mTime;
    int mDistance;
    String mWeatherStatus;
    double mWeatherModifier;
    double mTemperature;

    private MapControlFragment mMapFragment;
    private FrameLayout mFrame;
    private DatabaseHelper mDBHelper;

    private ArrayList<LatLng> mCoordinatesList = new ArrayList<LatLng>();
    private ArrayList<Integer> mSkipList = new ArrayList<Integer>();
    private DungeonLevel mSelection;
    private int mOutcome;
    private int mSkillPoints;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dungeon_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dungeon Results");

        // Set-up db and intent
        mDBHelper = new DatabaseHelper(getApplicationContext());
        Intent intent = getIntent();

        // Retrieve the selected dungeon and constraints  from previous activity
        mSelection = intent.getParcelableExtra("dungeonSelection");

        // Display total dungeon time and set time as an integer
        mDisplayTime = intent.getStringExtra("finalTime");
        TextView finalTimeView = (TextView) findViewById(R.id.timeView);
        finalTimeView.setText(mDisplayTime);
        mTime = displayToIntTimeConverter(mDisplayTime);

        // Display total dungeon distance
        TextView finalDistanceView = (TextView) findViewById(R.id.distanceView);
        mDistance = intent.getIntExtra("finalDistance", 0);
        String displayToDistance = mDistance + "m";
        finalDistanceView.setText(displayToDistance);

        // Display pace
        TextView finalPaceView = (TextView) findViewById(R.id.paceView);
        double paceUgly = calculatePace(mTime, mDistance);
        DecimalFormat df = new DecimalFormat("#.00");
        String finalPace = df.format(paceUgly) + "km/h";
        finalPaceView.setText(finalPace);

        // Determine outcome of dungeon and set Labels
        mOutcome = determineOutcome(mSelection.getTime(), mSelection.getDistance(), mSelection.getPace(), mTime, mDistance);
        TextView successOrFailView = (TextView) findViewById(R.id.successOrFailureTextView);
        TextView outcomeDescriptionView = (TextView) findViewById(R.id.outcomeDescriptionTextView);
        String successOrFailLabel;
        String outcomeDescriptionLabel;
        if (mOutcome == 1) {
            successOrFailLabel = "Success!";
            successOrFailView.setText(successOrFailLabel);
            outcomeDescriptionLabel = mSelection.getName() + ": passed";
            outcomeDescriptionView.setText(outcomeDescriptionLabel);
        }
        else {
            successOrFailLabel = "Failure...";
            successOrFailView.setText(successOrFailLabel);
            outcomeDescriptionLabel = mSelection.getName() + ": failed";
            outcomeDescriptionView.setText(outcomeDescriptionLabel);
        }

        // Get the date and Time
        // https://stackoverflow.com/questions/5683728/convert-java-util-date-to-string
        Date dateTime = Calendar.getInstance().getTime();
        Format formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.UK);
        String prettyTime = formatter.format(dateTime);

        // Map stuff
        mCoordinatesList = intent.getParcelableArrayListExtra("coordinatesList");
        mSkipList = intent.getIntegerArrayListExtra("skipIndicies");
        mMapFragment = new MapControlFragment();
        mMapFragment.initFragment(this, mCoordinatesList, mSkipList);
        mFrame = findViewById(R.id.mapFrame);
        getSupportFragmentManager().beginTransaction().add(mFrame.getId(), mMapFragment).commit();

        // This final function goes and calls the weather service to get weather info, and finally
        // can then reward an item based on this information.
        calculateWeatherStatus(mCoordinatesList, prettyTime);


    }

    private void finalRewardRoutine(String prettyTime) {
        // Calculate skill points of the item
        mSkillPoints = determineRewardSkillPoints(mTime, mDistance, mOutcome, mSelection.getMultiplier(), mWeatherModifier);

        // Generate a description for the item
        String outcomeDescription;
        if (mOutcome == 1) {
            outcomeDescription = "success";
        }
        else {
            outcomeDescription = "failure";
        }
        String description = "Source: " + mSelection.getName() + " [" + outcomeDescription + "]";

        // Generate an item as reward
        Equipment reward = new Equipment(0, mSkillPoints, description, 0);
        TextView rewardTextView = (TextView) findViewById(R.id.itemNameResultsTextView);
        rewardTextView.setText(reward.getName());
        TextView rewardStatTextView = (TextView) findViewById(R.id.itemStatsResultsTextView);
        String stats = "Amr: " + reward.getArmour() +
                ", Dmg: " + reward.getDamage() +
                ", Str: " + reward.getStrength() +
                ", Agi: " + reward.getAgility() +
                ", Int: " + reward.getIntelligence();
        rewardStatTextView.setText(stats);

        // Save the new equipment
        mDBHelper.addEquipment(reward);

        // mCoordinatesList and mSkipList must be as strings to store in the database.
        // rather than use a toString method, which results in strings such as:
        //      [lat/lng: (-37.78098475,145.11339419), lat/lng: (-37.78093189,145.1133283)], and
        //      [2, 5]
        // we do our own implementation that is slightly more space efficient, which gives strings:
        //      -37.78098475,145.11339419,-37.78093189,145.1133283          and
        //      2,5

        String coordsAsString = DungeonRecord.convertCoordinatesArrayToString(mCoordinatesList);
        String skipsAsString = DungeonRecord.convertSkipArrayToString(mSkipList);

        // Save the dungeon record
        DungeonRecord record = new DungeonRecord(0, prettyTime,
                mSelection.getName() + " [" + outcomeDescription + "]",
                mOutcome, mDistance, mTime, reward.getName() + " (" + mSkillPoints + ")",
                coordsAsString, skipsAsString);
        mDBHelper.addDungeonRecord(record);
    }

    @Override
    public void onMapClicked(String locName, LatLng position) {
        // do nothing on click for now
    }

    // Convert the time used for display on screen into a integer that can be stored in the database
    public int displayToIntTimeConverter(String displayTime){
        int seconds = 0;
        String[] parts = displayTime.split(":");
        if (parts.length == 2) {
            seconds += Integer.parseInt(parts[1]);
            seconds += (Integer.parseInt(parts[0])*60);
        }
        else if (parts.length == 3) {
            seconds += Integer.parseInt(parts[2]);
            seconds += Integer.parseInt(parts[1])*60;
            seconds += (Integer.parseInt(parts[0])*60*60);
        }
        return seconds;
    }

    // Determine whether the dungeon was a success or a failure
    // 1 is a success, 0 is a failure, as this corresponds to the dungeonRecords database
    public int determineOutcome(int timeLimit, int distanceRequired, double paceRequired, int timeTaken, int distanceCovered){
        if (timeTaken == 0) { timeTaken = 1; }

        if ((timeLimit == 0) && (distanceRequired != 0) && (paceRequired == 0)){
            return ((distanceCovered >= distanceRequired) ? 1 : 0);
        }

        else if ((timeLimit != 0) && (distanceRequired != 0) && (paceRequired != 0)){
            if((distanceCovered >= distanceRequired)&& (timeTaken <= timeLimit)){
                return 1;
            }
            double pace = calculatePace(timeTaken, distanceCovered);
            return (((pace >= paceRequired) && (timeTaken >= timeLimit)) ? 1 : 0);
        }

        else if ((timeLimit != 0) && (distanceRequired == 0) && (paceRequired != 0)){
            double pace = calculatePace(timeTaken, distanceCovered);
            return (((pace >= paceRequired) && (timeTaken >= timeLimit)) ? 1 : 0);
        }

        else if ((timeLimit== 0) && (distanceRequired == 0) && (paceRequired == 0)){
            // Dungeon Farm - always results in a success
            return 1;
        }

        Log.d("DETERMINEOUTCOME", "bad data in dungeonLevels... should always return before here");
        return 0;
    }

    // Determine the amount of skill points the item has, based on the running stats and the outcome
    // of the dungeon
    public int determineRewardSkillPoints(int time, int distance, int outcome, double levelMultiplier, double weatherModifier){
        double pace = calculatePace(time, distance);
        double base = pace * (distance/10.00);
        int randomInt;
        double randomMultiplier;
        double totalMultiplier;
        Random random = new Random();
        if (outcome == 1){
            // successful dungeons get a random multiplier between 1 and 1.3
            randomInt = (random.nextInt(13 - 10 + 1) + 10);
            randomMultiplier = randomInt / 10.00;

            totalMultiplier = randomMultiplier * levelMultiplier * weatherModifier;
        }
        else {
            // unsuccessful dungeons get a random multiplier between 0.7 and 1.0
            randomInt = (random.nextInt(10 - 7 + 1) + 7);
            randomMultiplier = randomInt / 10.00;
            // all failed dungeons get a multiplier of 0.9
            totalMultiplier = randomMultiplier * 0.9 * weatherModifier;
        }
        return (int) Math.round(base * totalMultiplier);
    }

    // calculate the pace using time and distance.  note that time is seconds, distance is meters,
    // but pace is km/h.  This goes for all usages in the app and databases!
    public double calculatePace(int time, int distance) {
        if (time > 0) {
            return ((distance / 1000.00) / (time / (60.00 * 60.00)));
        }
        else {
            return 0;
        }
    }

    // This function uses the Dark Sky API together with the kevinzetterstrom wrapper to determine
    // the weather conditions on the run.  It then goes and calls the finalRewardRoutine once it has
    // the data it requires.
    // https://darksky.net/dev
    // https://github.com/kevinzetterstrom/forecast-android
    private void calculateWeatherStatus(ArrayList<LatLng> givenCoordinates, final String prettyTime){
        ForecastConfiguration configuration =
                new ForecastConfiguration.Builder("YOUR API KEY HERE")
                        .setCacheDirectory(getCacheDir())
                        .build();
        ForecastClient.create(configuration);

        if (givenCoordinates.size() == 0){
            // if no gps data was recieved, then reflect a no data unsuccessful run,
            // setting all UI elements as such
            TextView successOrFailView = (TextView) findViewById(R.id.successOrFailureTextView);
            TextView outcomeDescriptionView = (TextView) findViewById(R.id.outcomeDescriptionTextView);
            successOrFailView.setText("No data");
            outcomeDescriptionView.setText("Couldn't collect any GPS coordinates...");
            TextView weatherView = (TextView) findViewById(R.id.weatherType);
            mWeatherModifier = calculateWeatherBonusFromStatus(mWeatherStatus);
            weatherView.setText("No data");
            TextView rewardTextView = (TextView) findViewById(R.id.itemNameResultsTextView);
            rewardTextView.setText("No item recieved!");
            TextView rewardStatTextView = (TextView) findViewById(R.id.itemStatsResultsTextView);
            rewardStatTextView.setText(":(");
            return;

        }
        LatLng dungeonStart = givenCoordinates.get(0);
        double latitude = dungeonStart.latitude;
        double longitude = dungeonStart.longitude;

        ForecastClient.getInstance()
                .getForecast(latitude, longitude, new Callback<Forecast>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(Call<Forecast> forecastCall, Response<Forecast> response) {

                        Log.d("WEATHER", "GOT A RESPONSE");
                        // Here we try to use the most accurate first, and keep trying until we
                        // have to use the least accurate
                        if (response.body().getCurrently()!= null) {
                            Log.d("WEATHER", "using CURRENTLY accuracy");
                            mTemperature = response.body().getCurrently().getTemperature();
                        }
                        else if (response.body().getMinutely()!= null) {
                            Log.d("WEATHER", "using MINUTELY accuracy");
                            mTemperature = response.body().getMinutely().getDataPoints().get(0).getTemperature();
                        }
                        else if (response.body().getHourly()!= null) {
                            Log.d("WEATHER", "using HOURLY accuracy");
                            mTemperature = response.body().getHourly().getDataPoints().get(0).getTemperature();
                        }
                        else if (response.body().getDaily()!= null) {
                            Log.d("WEATHER", "using DAILY accuracy");
                            mTemperature = response.body().getDaily().getDataPoints().get(0).getTemperature();
                        }
                        else {
                            Log.d("WEATHER", "no accuracy level available");
                            // if no data available, set temperature to 10000, which the routine
                            // identifies as a no data value
                            mTemperature = 10000;
                        }
                        // set weather UI things
                        mWeatherStatus = temperatureToStatusWord(mTemperature);
                        TextView weatherView = (TextView) findViewById(R.id.weatherType);
                        mWeatherModifier = calculateWeatherBonusFromStatus(mWeatherStatus);
                        weatherView.setText(mWeatherStatus + " [" + mWeatherModifier + "]");

                        // go on to reward generation, as we now have all necessary information
                        finalRewardRoutine(prettyTime);
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFailure(Call<Forecast> forecastCall, Throwable t) {
                        // in the case the weather service fails, we simply use a weather multiplier
                        // of 1.0 and continue with the reward routine
                        Log.d("WEATHER", "FAILED");
                        mWeatherStatus = "No Data";
                        TextView weatherView = (TextView) findViewById(R.id.weatherType);
                        mWeatherModifier = calculateWeatherBonusFromStatus(mWeatherStatus);
                        weatherView.setText(mWeatherStatus + " [" + mWeatherModifier + "]");
                        finalRewardRoutine(prettyTime);
                    }
                });
    }


    private String temperatureToStatusWord(Double temperature) {
        // Convert temperature in fahrenheit to an appropriate word description.
        // Fahrenheit has been used since that is what dark sky provides, and no need to introduce
        // more code to convert it to celcius for this simple relationship below
        if (temperature == 10000){
            return "No Data";
        }
        else if (temperature > 95) {
            // over 35 degrees C is scorching
            return "Scorching";
        } else if ((86 <= temperature) && (temperature < 95)) {
            // 30 to 34 degrees C is hot
            return "Hot";
        }
        else if ((77 <= temperature) && (temperature < 86)) {
            // 25 to 29 degrees C is warm
            return "Warm";
        } else if ((59 <= temperature) && (temperature < 77)) {
            // 15 to 24 is fine
            return "Fine";
        } else if ((50 <= temperature) && (temperature < 59)) {
            // 10 to 14 is cold
            return "Cold";
        } else if ((41 <= temperature) && (temperature < 50)) {
            // 5 to 9 is frosty
            return "Frosty";
        } else if ((41 < temperature)) {
            // under 5 degrees is glacial
            return "Glacial";
        }
        return "No Data";
    }

    // Take a string status of the weather and return an appropriate multiplier for it
    public double calculateWeatherBonusFromStatus(String status){
        if (Objects.equals(status, "Scorching")){ return 1.3; }
        if (Objects.equals(status, "Hot")){ return 1.2; }
        if (Objects.equals(status, "Warm")){ return 1.1; }
        if (Objects.equals(status, "Fine")){ return 1.0; }
        if (Objects.equals(status, "Cold")){ return 1.1; }
        if (Objects.equals(status, "Frosty")){ return 1.2; }
        if (Objects.equals(status, "Glacial")){ return 1.3; }
        return 1.0;
    }

}
