package fit3037.dmmic2.dungeonrunner;

import android.os.Parcel;
import android.os.Parcelable;

/*
* This class defines a Dungeon Level - which is a set of constraints and an item reward multiplier.
* These will correspond directly to the dungeons the user can choose from on the Dungeon Selection
* screen (i.e. DungeonSelection activity).  Instances of these are stored in the dungeonLevels
* table.
* */

public class DungeonLevel implements Parcelable {

    // Database Constants
    public static final String TABLE_NAME = "dungeonLevels";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_PACE = "pace";
    public static final String COLUMN_MULTIPLIER = "multiplier";

    // Table create statement
    public static final String CREATE_STATEMENT = "CREATE TABLE "
            + TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_TIME + " INTEGER NOT NULL, " +
            COLUMN_DISTANCE + " INTEGER NOT NULL, " +
            COLUMN_PACE + " REAL NOT NULL, " +
            COLUMN_MULTIPLIER + " REAL NOT NULL" +
            ")";

    // Attributes
    private long _id;                       // for database purposes
    private String mName;                   // name of dungeon
    // When distance is not null, time is a max limit. When pace is not null, time is a min limit
    private int mTime;                      // time constraint of dungeon (s), can be null
    private int mDistance;                  // distance constraint of dungeon (m), can be null
    private double mPace;                   // pace constraint of dungeon (km/h), can be null
    private double mMultiplier;             // multiplier to be applied to the reward's item points

    // Getter Methods
    public long getId() { return _id; }
    public String getName() { return mName; }
    public int getTime() { return mTime; }
    public int getDistance() { return mDistance; }
    public double getPace() { return mPace; }
    public double getMultiplier() { return mMultiplier; }

    // Setter Methods
    // none at the moment

    // Constructor for  parcelable
    private DungeonLevel(Parcel in){
        _id = in.readLong();
        mName = in.readString();
        mTime = in.readInt();
        mDistance = in.readInt();
        mPace = in.readDouble();
        mMultiplier = in.readDouble();
    }

    // Constructor
    public DungeonLevel(long id, String name, int time, int distance, double pace, double multiplier) {
        this._id = id;
        this.mName = name;
        this.mTime = time;
        this.mDistance = distance;
        this.mPace = pace;
        this.mMultiplier = multiplier;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(_id);
        parcel.writeString(mName);
        parcel.writeInt(mTime);
        parcel.writeInt(mDistance);
        parcel.writeDouble(mPace);
        parcel.writeDouble(mMultiplier);
    }

    // Creator
    public static final Creator<DungeonLevel> CREATOR = new Creator<DungeonLevel>() {
        @Override
        public DungeonLevel createFromParcel(Parcel in) {return new DungeonLevel(in); }
        @Override
        public DungeonLevel[] newArray(int size) { return new DungeonLevel[size]; }
    };

}
