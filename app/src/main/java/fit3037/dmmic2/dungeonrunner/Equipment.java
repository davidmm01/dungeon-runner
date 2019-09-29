package fit3037.dmmic2.dungeonrunner;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

/*
* This class defines Equipment, that is weapons and armour that will be the reward given to players
* for completing dungeons. Equipment is stored in the equipment table, and equipment shows up in
* many different screens across the application.
* */

public class Equipment implements Parcelable  {

    // Database Constants
    public static final String TABLE_NAME = "equipment";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_ARMOUR = "armour";
    public static final String COLUMN_DAMAGE = "damage";
    public static final String COLUMN_STRENGTH = "strength";
    public static final String COLUMN_AGILITY = "agility";
    public static final String COLUMN_INTELLIGENCE = "intelligence";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_EQUIPPED = "equipped";

    // Table create statement
    public static final String CREATE_STATEMENT = "CREATE TABLE "
            + TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_TYPE + " TEXT NOT NULL, " +
            COLUMN_ARMOUR + " INTEGER NOT NULL, " +
            COLUMN_DAMAGE + " INTEGER NOT NULL, " +
            COLUMN_STRENGTH + " INTEGER NOT NULL, " +
            COLUMN_AGILITY + " INTEGER NOT NULL, " +
            COLUMN_INTELLIGENCE + " INTEGER NOT NULL, " +
            COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
            COLUMN_EQUIPPED + " INTEGER NOT NULL" +
            ")";

    // Equipment attributes
    private long _id; // for database purposes
    private String mName;
    private String mType;
    private int mArmour;
    private int mDamage;
    private int mStrength;
    private int mAgility;
    private int mIntelligence;
    private String mDescription;
    // mEquipped takes the value of 1 or 0.  1 means the item is equipped to the player and 0 means
    // it is not.  Care must be taken to ensure that only one item per equipment type ever has
    // equipped set to 1.
    private int mEquipped;

    // Getter methods
    public long getId () { return _id; }
    public String getName() { return mName; }
    public String getType() { return mType; }
    public int getArmour() { return mArmour; }
    public int getDamage() { return mDamage; }
    public int getStrength() { return mStrength; }
    public int getAgility() { return mAgility; }
    public int getIntelligence() { return mIntelligence; }
    public String getDescription() { return mDescription; }
    public int getEquipped() { return mEquipped; }

    // Setter methods
    public void setEquipped(int status) throws Exception {
        if ((status != 0) && (status != 1)){
            throw new Exception("Cannot set status of equipped to value " + status);
        }
        this.mEquipped = status;
    }

    // Note that biases are only for calculation and generation, they are not stored in the database
    // for Equipment (only for ItemDescriptors).
    // They are used to adjust attributes of an item based on its name, in an attempt to make the
    // random name generated have stats that somewhat match it.
    private int mArmourBias = 0;
    private int mDamageBias = 0;
    private int mStrengthBias = 0;
    private int mAgilityBias = 0;
    private int mIntelligenceBias = 0;

    private DatabaseHelper mDBHelper;

    private Context mContext = MyApplication.getContext();

    // Equipment Constructor for  parcelable
    private Equipment(Parcel in){
        _id = in.readLong();
        mName = in.readString();
        mType = in.readString();
        mArmour = in.readInt();
        mDamage = in.readInt();
        mStrength = in.readInt();
        mAgility = in.readInt();
        mIntelligence = in.readInt();
        mDescription = in.readString();
        mEquipped = in.readInt();
    }


    // Equipment Constructor
    // This constructor is useful to force creation of an item with certain stats or properties.
    // Currently only used to create some default items in the database.
    public Equipment(long id, String name, String type, int armour, int damage, int strength,
                     int agility, int intelligence, String description, int equipped) {
        this._id = id;
        this.mName = name;
        this.mType = type;
        this.mArmour = armour;
        this.mDamage = damage;
        this.mStrength = strength;
        this.mAgility = agility;
        this.mIntelligence = intelligence;
        this.mDescription = description;
        this.mEquipped = equipped;
    }

    // Another Equipment Constructor, that implements random item generation.
    // This is the usual constructor that is called to generate loot for players.
    public Equipment(long id, int itemPoints, String description, int equipped) {
        this._id = id;
        // determine the type of item it will be:
        this.mType = determineItemType();
        // determine the name of the weapon, and any biases towards certain stats that it implies
        this.mName = determineName();
        calculateStats(itemPoints);
        this.mDescription = description;
        this.mEquipped = equipped;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(_id);
        parcel.writeString(mName);
        parcel.writeString(mType);
        parcel.writeInt(mArmour);
        parcel.writeInt(mDamage);
        parcel.writeInt(mStrength);
        parcel.writeInt(mAgility);
        parcel.writeInt(mIntelligence);
        parcel.writeString(mDescription);
        parcel.writeInt(mEquipped);
    }

    // Creator
    public static final Creator<Equipment> CREATOR = new Creator<Equipment>() {
        @Override
        public Equipment createFromParcel(Parcel in) {return new Equipment(in); }
        @Override
        public Equipment[] newArray(int size) { return new Equipment[size]; }
    };

    private String determineItemType() {
        Random rand = new Random();

        // Returns a random item type
        ArrayList<String> equipment_types = new ArrayList<>();
        equipment_types.add("head");
        equipment_types.add("shoulders");
        equipment_types.add("chest");
        equipment_types.add("hands");
        equipment_types.add("feet");
        equipment_types.add("legs");
        equipment_types.add("sharp");
        equipment_types.add("blunt");
        equipment_types.add("ranged");
        int  n = rand.nextInt(9);
        return equipment_types.get(n);

    }


    private String determineName() {
        /*
        if styleNoun = Gladiator, styleAdjective = Bulky, typeNoun = Pauldrons, then...
             0: Gladiator's Bulky Pauldrons             needs A, N
             1: Bulky Gladiator's Pauldrons             needs A, N
             2: Bulky Pauldrons of the Gladiator        needs A, N
             3: Gladiator's Pauldrons                   needs N
             4: Pauldrons of the Gladiator              needs N
             5: Bulky Pauldrons                         needs A
        */

        String typeNoun = getTypeSpecificNoun();
        String styleNoun = "";
        String styleAdjective = "";

        Random rand = new Random();
        int  n = rand.nextInt(6);

        if (n < 5) {
            styleNoun = getStyleNoun();
        }

        if ((n != 3)&&(n != 4)) {
            styleAdjective = getStyleAdjective();
        }

        if (n == 0) { return styleNoun + "'s " + styleAdjective + " " + typeNoun; }
        else if (n == 1) { return styleAdjective + " " + styleNoun + "'s " + typeNoun; }
        else if (n == 2) { return styleAdjective + " " + typeNoun + " of the " + styleNoun; }
        else if (n == 3) { return styleNoun + "'s " + typeNoun; }
        else if (n == 4) { return typeNoun + " of the " + styleNoun; }
        else { return styleAdjective + " " + typeNoun; }
    }


    // Beware of possible confusion surrounding "type" below...
        // type for Equipment means legs, chest, sharp, ...
        // type for ItemDescriptor means typeNoun, styleNoun, styleAdjective
        // match from ItemDescriptor means legs, chest, sharp, all, weapon, armour, ...
    // Both of these two types are referred to below so be careful not to get confused.
    // type for Equipment is a subset of match for ItemDescriptor

    private String getTypeSpecificNoun(){
        // typeNouns only match to one of the nine standard equipment types
        mDBHelper = new DatabaseHelper(mContext);

        ArrayList<String> matchTo = new ArrayList<>();
        matchTo.add(this.getType()); // grabs whether its a helm, sharp, etc...
        ItemDescriptor typeNoun = mDBHelper.getRandomItemDescriptor("typeNoun", matchTo);

        accumulateBiases(typeNoun);
        return typeNoun.getDescriptor();
    }

    private String getStyleNoun(){
        // styleNouns always match "all"
        mDBHelper = new DatabaseHelper(mContext);

        ArrayList<String> matchTo = new ArrayList<>();
        matchTo.add("all");
        ItemDescriptor styleNoun = mDBHelper.getRandomItemDescriptor("styleNoun", matchTo);

        accumulateBiases(styleNoun);
        return styleNoun.getDescriptor();
    }

    private String getStyleAdjective(){
        // styleAdjectives have the most complex of matching rules.  They can match to:
        //      -"all"
        //      -"armour" if it is type head, shoulders, chest, hands, feet, legs
        //      -"weapon" if it is type sharp, blunt, ranged
        //      -"sharp" if its type sharp
        //      -"blunt" if its type blunt
        //      -"ranged" if its type ranged
        //      -there is no matching for specific armour, such as "chest" or "head"

        mDBHelper = new DatabaseHelper(mContext);

        ArrayList<String> matchTo = new ArrayList<>();
        matchTo.add("all");

        if (isWeapon(this.getType())) {
            matchTo.add(this.getType());
            matchTo.add("weapon");
        }
        else {
         // else it must be armour
            matchTo.add("armour");
        }

        ItemDescriptor styleNoun = mDBHelper.getRandomItemDescriptor("styleAdjective", matchTo);
        accumulateBiases(styleNoun);
        return styleNoun.getDescriptor();
    }

    private void accumulateBiases(ItemDescriptor itemDescriptor){
        this.mArmourBias += itemDescriptor.getArmourBias();
        this.mDamageBias += itemDescriptor.getDamageBias();
        this.mStrengthBias += itemDescriptor.getStrengthBias();
        this.mAgilityBias += itemDescriptor.getAgilityBias();
        this.mIntelligenceBias += itemDescriptor.getIntelligenceBias();
    }

    private void calculateStats(int itemPoints) {
        float totalBias = this.mArmourBias + this.mDamageBias + this.mStrengthBias + this.mAgilityBias + this.mIntelligenceBias;
        this.mArmour = Math.round((this.mArmourBias / totalBias) * itemPoints);
        this.mDamage = Math.round((this.mDamageBias / totalBias) * itemPoints);
        this.mStrength = Math.round((this.mStrengthBias / totalBias) * itemPoints);
        this.mAgility = Math.round((this.mAgilityBias / totalBias) * itemPoints);
        this.mIntelligence = Math.round((this.mIntelligenceBias / totalBias) * itemPoints);

        Log.d("EQUIPMENT CALC STATS", "total item points are: " + itemPoints);
        Log.d("EQUIPMENT CALC STATS", "armour: " + this.mArmour);
        Log.d("EQUIPMENT CALC STATS", "dmg: " + this.mDamage);
        Log.d("EQUIPMENT CALC STATS", "str: " + this.mStrength);
        Log.d("EQUIPMENT CALC STATS", "agi: " + this.mAgility);
        Log.d("EQUIPMENT CALC STATS", "int: " + this.mIntelligence);

    }

    // Used by getStyleAdjective to determine matching patterns
    private boolean isWeapon(String input){
        return (Objects.equals(input, "sharp")) ||
                (Objects.equals(input, "blunt")) ||
                (Objects.equals(input, "ranged"));
    }

    // Used by getStyleAdjective to determine matching patterns
    private boolean isArmour(String input){
        return (Objects.equals(input, "head")) ||
                (Objects.equals(input, "shoulders")) ||
                (Objects.equals(input, "chest")) ||
                (Objects.equals(input, "hands")) ||
                (Objects.equals(input, "legs")) ||
                (Objects.equals(input, "feet"));
    }

    // Useful function that simply returns an array list of the different kinds of equipment types.
    // Saves it having to be declared elsewhere, e.g. the DatabaseHelper or various activities
    public static ArrayList<String> getEquipmentTypes(){
        ArrayList<String> types = new ArrayList<>();
        types.add("head");
        types.add("shoulders");
        types.add("chest");
        types.add("hands");
        types.add("legs");
        types.add("feet");
        types.add("sharp");
        types.add("blunt");
        types.add("ranged");
        return types;
    }

}
