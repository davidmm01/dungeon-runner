package fit3037.dmmic2.dungeonrunner;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
* This class is used to represent a dungeon that a user has done in the past.  These are displayed
* on the Dungeon Journal screen (i.e. DungeonJournal activity).  These are stored in the
* dungeonRecords table.
* */

public class DungeonRecord implements Parcelable {

    // Database Constants
    public static final String TABLE_NAME = "dungeonRecords";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_OUTCOME = "outcome";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_REWARD = "reward";
    public static final String COLUMN_COORDS = "coordinates";
    public static final String COLUMN_SKIPS = "skips";


    // Table create statement
    public static final String CREATE_STATEMENT = "CREATE TABLE "
            + TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COLUMN_DATE + " TEXT NOT NULL, " +
            COLUMN_TYPE + " TEXT NOT NULL, " +
            COLUMN_OUTCOME + " INTEGER NOT NULL, " +
            COLUMN_DISTANCE + " INTEGER NOT NULL, " +
            COLUMN_TIME + " INTEGER NOT NULL, " +
            COLUMN_REWARD + " TEXT NOT NULL, " +
            COLUMN_COORDS + " TEXT NOT NULL, " +
            COLUMN_SKIPS + " TEXT NOT NULL" +
            ")";

    // Attributes
    private long _id;           // for database purposes
    private String mDate;       // when the dungeon took place
    private String mType;       // type of dungeon (e.g. Dungeon Farm, Easy Raid etc...)
                                // this will be saved as something ready to display to users,
                                // e.g. "Dungeon Farm" rather than "dungeonFarm"
    private int mOutcome;       // success (int 1) or failure (int 0)
    private int mDistance;      // length of dungeon in metres
    private int mTime;          // duration of dungeon in seconds
    private String mReward;     // basic summary of the item that was received for the dungeon.
    private String mCoords;     // coordinates of the run
    private String mSkips;      // indexes in coords that should not be graphed (pauses)

    // Getter Methods
    public long getId() { return _id; }
    public String getDate() { return mDate; }
    public String getType() { return mType; }
    public int getOutcome() { return mOutcome; }
    public int getDistance() { return mDistance; }
    public int getTime() { return mTime; }
    public String getReward() { return mReward; }
    public String getCoords() { return mCoords; }
    public String getSkips() { return mSkips; }


    // Setter Methods
    // none at the moment

    // Constructor for  parcelable
    private DungeonRecord(Parcel in){
        _id = in.readLong();
        mDate = in.readString();
        mType = in.readString();
        mOutcome = in.readInt();
        mDistance = in.readInt();
        mTime = in.readInt();
        mReward = in.readString();
        mCoords = in.readString();
        mSkips = in.readString();

    }


    // Constructor
    public DungeonRecord(long id, String date, String type, int outcome, int distance,
            int time, String reward, String coords, String skips) {
        this._id = id;
        this.mDate = date;
        this.mType = type;
        this.mOutcome = outcome;
        this.mDistance = distance;
        this.mTime = time;
        this.mReward = reward;
        this.mCoords = coords;
        this.mSkips = skips;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(_id);
        parcel.writeString(mDate);
        parcel.writeString(mType);
        parcel.writeInt(mOutcome);
        parcel.writeInt(mDistance);
        parcel.writeInt(mTime);
        parcel.writeString(mReward);
        parcel.writeString(mCoords);
        parcel.writeString(mSkips);
    }

    // Creator
    public static final Creator<DungeonRecord> CREATOR = new Creator<DungeonRecord>() {
        @Override
        public DungeonRecord createFromParcel(Parcel in) {return new DungeonRecord(in); }
        @Override
        public DungeonRecord[] newArray(int size) { return new DungeonRecord[size]; }
    };

    // This function coverts an ArrayList<LatLng> to a space efficient string that can be easily
    // stored in our database tables
    public static String convertCoordinatesArrayToString(ArrayList<LatLng> coordsArray){
        String result = "";
        LatLng coord;
        for (int i=0; i < coordsArray.size(); i+=1) {
            coord = coordsArray.get(i);
            result += coord.latitude + ",";
            result += coord.longitude;
            if (i+1 < coordsArray.size()){
                result += ",";
            }
        }
        return result;
    }

    // This function takes the strings generated by convertCoordinatesArrayToString and turns
    // them back into their original form, an ArrayList<LatLng>
    public static ArrayList<LatLng> convertCoordinatesStringToArrayLatLng(String stringCoords){
        List<String> arrayStringCoords = Arrays.asList(stringCoords.split(","));
        ArrayList<LatLng> coords = new ArrayList<LatLng>();
        LatLng current;
        double lat, lon;
        for(int i=0; i < arrayStringCoords.size(); i += 2){
            lat = Double.parseDouble(arrayStringCoords.get(i));
            lon = Double.parseDouble(arrayStringCoords.get(i+1));
            current = new LatLng(lat, lon);
            coords.add(current);
        }
        return coords;
    }

    // This function coverts an ArrayList<Integer> to a space efficient string that can be easily
    // stored in our database tables
    public static String convertSkipArrayToString(ArrayList<Integer> skipsArray){
        String result = "";
        for (int i=0; i < skipsArray.size(); i+=1) {
            result += skipsArray.get(i);
            if (i+1 < skipsArray.size()){
                result += ",";
            }
        }
        return result;
    }

    // This function takes the strings generated by convertSkipArrayToString and turns
    // them back into their original form, an ArrayList<Integer>
    public static ArrayList<Integer> convertSkipStringToArrayInteger(String stringSkips){
        // Help from https://stackoverflow.com/questions/27599847/convert-comma-separated-string-to-list-without-intermediate-container
        // note i did it the non fancy way since i wasn't comfortable with the other
        List<String> numbers = Arrays.asList(stringSkips.split(","));

        ArrayList<Integer> numbersInt = new ArrayList<>();
        for (String number : numbers) {
            numbersInt.add(Integer.valueOf(number));
        }
        return numbersInt;
    }

}
