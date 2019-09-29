package fit3037.dmmic2.dungeonrunner;


import android.os.Parcel;
import android.os.Parcelable;

/*
* This class defines ItemDescriptors.  Item Descriptors are nouns and adjectives used to describe
* equipment, with each word having associated stat biases to help the stats of an item match the
* items name.  Rules for applying and combining the item descriptors are found within the Equipment
* class.  Item Descriptors are stored in the itemDescriptors table.
* */

public class ItemDescriptor implements Parcelable {

    // Database Constants
    public static final String TABLE_NAME = "itemDescriptors";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DESCRIPTOR = "descriptor";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_MATCH = "match";
    public static final String COLUMN_ARMOUR_BIAS = "armourBias";
    public static final String COLUMN_DAMAGE_BIAS = "damageBias";
    public static final String COLUMN_STRENGTH_BIAS= "strengthBias";
    public static final String COLUMN_AGILITY_BIAS = "agilityBias";
    public static final String COLUMN_INTELLIGENCE_BIAS = "intelligenceBias";

    // Table create statement
    public static final String CREATE_STATEMENT = "CREATE TABLE "
            + TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COLUMN_DESCRIPTOR + " TEXT NOT NULL, " +
            COLUMN_TYPE + " TEXT NOT NULL, " +
            COLUMN_MATCH + " TEXT NOT NULL, " +
            COLUMN_ARMOUR_BIAS + " INTEGER NOT NULL, " +
            COLUMN_DAMAGE_BIAS + " INTEGER NOT NULL, " +
            COLUMN_STRENGTH_BIAS + " INTEGER NOT NULL, " +
            COLUMN_AGILITY_BIAS + " INTEGER NOT NULL, " +
            COLUMN_INTELLIGENCE_BIAS + " INTEGER NOT NULL" +
            ")";

    /*
    * ID:           for database purposes
    * Descriptor:   the associated word with the description,
    *               e.g. 'Sword' for a type noun, 'Heavy' for a style adjective
    * Type:         the kind of description this is.  Can be either 'typeNoun', "styleNoun" or
    *               "styleAdjective"
    * Match:        the kind of gear this descriptor can match to, can be either "head", "chest",
    *               "sharp", ..., "weapons", "armour", or "all"
    * x Bias:       value of bias the item has towards stat x
    * */

    private long _id;
    private String mDescriptor;
    private String mType;
    private String mMatch;
    private int mArmourBias;
    private int mDamageBias;
    private int mStrengthBias;
    private int mAgilityBias;
    private int mIntelligenceBias;

    // ItemDescriptor Constructor
    public ItemDescriptor(long id, String descriptor, String type, String match, int armourBias,
                          int damageBias, int strengthBias, int agilityBias, int intelligenceBias) {
        this._id = id;
        this.mDescriptor = descriptor;
        this.mType = type;
        this.mMatch = match;
        this.mArmourBias = armourBias;
        this.mDamageBias = damageBias;
        this.mStrengthBias = strengthBias;
        this.mAgilityBias = agilityBias;
        this.mIntelligenceBias = intelligenceBias;
    }

    // ItemDescriptor Constructor for  parcelable
    private ItemDescriptor(Parcel in){
        _id = in.readLong();
        mDescriptor = in.readString();
        mType = in.readString();
        mMatch = in.readString();
        mArmourBias = in.readInt();
        mDamageBias = in.readInt();
        mStrengthBias = in.readInt();
        mAgilityBias = in.readInt();
        mIntelligenceBias = in.readInt();
    }

    public static final Creator<ItemDescriptor> CREATOR = new Creator<ItemDescriptor>() {
        @Override
        public ItemDescriptor createFromParcel(Parcel in) {return new ItemDescriptor(in); }
        @Override
        public ItemDescriptor[] newArray(int size) { return new ItemDescriptor[size]; }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(_id);
        parcel.writeString(mDescriptor);
        parcel.writeString(mType);
        parcel.writeString(mMatch);
        parcel.writeInt(mArmourBias);
        parcel.writeInt(mDamageBias);
        parcel.writeInt(mStrengthBias);
        parcel.writeInt(mAgilityBias);
        parcel.writeInt(mIntelligenceBias);
    }

    // Getter methods
    public long getId () { return _id; }
    public String getDescriptor() { return mDescriptor; }
    public String getType() { return mType; }
    public String getMatch() { return mMatch; }
    public int getArmourBias() { return mArmourBias; }
    public int getDamageBias() { return mDamageBias; }
    public int getStrengthBias() { return mStrengthBias; }
    public int getAgilityBias() { return mAgilityBias; }
    public int getIntelligenceBias() { return mIntelligenceBias; }

}
