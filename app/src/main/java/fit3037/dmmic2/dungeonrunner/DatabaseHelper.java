package fit3037.dmmic2.dungeonrunner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

    /*
    *
    * This DatabaseHelper class does many things such as provide functions to initialise databases,
    * populate databases with default data, getters, setters, deletes etc...
    *
    *         ---Databases in use---
    *
    * itemDescriptors:      Corresponds exactly to the ItemDescriptor class.  Stores ItemDescriptors
    *                       that are pre-determined and initialised upon the apps start-up.
    *
    * equipment:            Corresponds closely to the Equipment class.  Stores equipment that the
    *                       user accumulates as they play, as well as information on which pieces
    *                       of equipment are currently equipped.
    *
    * dungeonRecords:       Corresponds exactly to the DungeonRecord class.  Stores records of all
    *                       dungeons that the user has completed.
    *
    * dungeonLevels:        Corresponds exactly to the DungeonLevel class.  Stores the different
    *                       kinds of dungeons the user can do.  These are pre-determined and
    *                       initialized upon the app's start-up.
    *
    * */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Set Database Properties
    public static final String DATABASE_NAME = "DungeonRunnerDB";
    public static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(ItemDescriptor.CREATE_STATEMENT);
        sqLiteDatabase.execSQL(Equipment.CREATE_STATEMENT);
        sqLiteDatabase.execSQL(DungeonRecord.CREATE_STATEMENT);
        sqLiteDatabase.execSQL(DungeonLevel.CREATE_STATEMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ItemDescriptor.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Equipment.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DungeonRecord.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DungeonLevel.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void resetData(){
        SQLiteDatabase db = this.getReadableDatabase();
        onUpgrade(db, 1, 1);
        initializeEquipment();
        initializeItemDescriptors();
        initializeDungeonLevels();
    }

    // GET FROM DATABASE METHODS--------------------------------------------------------------------

    // Return a random ItemDescriptor that matches the provided criteria
    public ItemDescriptor getRandomItemDescriptor(String type, ArrayList<String> matches){
        ArrayList<ItemDescriptor> allMatches = new ArrayList<ItemDescriptor>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Build query string
        StringBuilder query = new StringBuilder("SELECT * FROM " + ItemDescriptor.TABLE_NAME + " WHERE type='" + type + "' AND (");
        int count = matches.size();

        for(int i=0; i < count; i++){
            query.append(" match='").append(matches.get(i)).append("'");
            if (i + 1 != count) {
                query.append(" OR");
            }
            else {
                query.append(")");
            }
        }

        // Process the database query results
        Log.d("DB QUERY", "querying " + ItemDescriptor.TABLE_NAME + " with query: " + query);
        Cursor cursor = db.rawQuery(query.toString(), null);
        while (cursor.moveToNext()) {
            ItemDescriptor descriptor = new ItemDescriptor(
                    cursor.getLong(0),      // _id
                    cursor.getString(1),    // descriptor
                    cursor.getString(2),    // type
                    cursor.getString(3),    // match
                    cursor.getInt(4),       // armour bias
                    cursor.getInt(5),       // damage bias
                    cursor.getInt(6),       // strength bias
                    cursor.getInt(7),       // agility bias
                    cursor.getInt(8));      // intelligence bias
            allMatches.add(descriptor);
        }

        // Select a random match from the query and close db connections
        int options = allMatches.size();
        Random rand = new Random();
        int  n = rand.nextInt(options);
        ItemDescriptor randomSelection = allMatches.get(n);
        cursor.close();
        db.close();
        return randomSelection;
    }

    public Equipment getEquippedEquipmentByType(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + Equipment.TABLE_NAME + " WHERE equipped=1 AND type = ?";
        Cursor cursor = db.rawQuery(query, new String[]{type});
        cursor.moveToFirst();
        Equipment equipped = new Equipment(
                cursor.getLong(0),      // id
                cursor.getString(1),    // name
                cursor.getString(2),    // type
                cursor.getInt(3),       // armour
                cursor.getInt(4),       // damage
                cursor.getInt(5),       // strength
                cursor.getInt(6),       // agility
                cursor.getInt(7),       // intelligence
                cursor.getString(8),    // description
                cursor.getInt(9)        // equipped
        );
        cursor.close();
        db.close();
        return equipped;
    }

    public ArrayList<Equipment> getAllEquippedEquipment() {
        ArrayList<Equipment> equippedEquipment = new ArrayList<Equipment>();
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<String> types = Equipment.getEquipmentTypes();

        // Note that here we purposely do a separate SQL statement instead of just grabbing all of
        // the equipped items at once.  This is slightly less efficient but has been done so that
        // we guarantee the order of times in the equippedEquipment ArrayList are always the same,
        // making the dressing room behave more predictably for the user.
        for (String type : types){
            String query = "SELECT * FROM " + Equipment.TABLE_NAME + " WHERE equipped=1 AND type = ?";
            Cursor cursor = db.rawQuery(query, new String[]{type});
            cursor.moveToFirst();
            Equipment equipped = new Equipment(
                    cursor.getLong(0),      // id
                    cursor.getString(1),    // name
                    cursor.getString(2),    // type
                    cursor.getInt(3),       // armour
                    cursor.getInt(4),       // damage
                    cursor.getInt(5),       // strength
                    cursor.getInt(6),       // agility
                    cursor.getInt(7),       // intelligence
                    cursor.getString(8),    // description
                    cursor.getInt(9)        // equipped
            );
            cursor.close();
            equippedEquipment.add(equipped);
        }
        db.close();
        return equippedEquipment;
    }

    public ArrayList<Equipment> getAllNonEquippedEquipmentByType(String type) {
        ArrayList<Equipment> nonEquippedEquipment= new ArrayList<Equipment>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + Equipment.TABLE_NAME + " WHERE equipped=0 AND type = ?";
        Cursor cursor = db.rawQuery(query, new String[]{type});
        while (cursor.moveToNext()) {
            Equipment nonEquipped = new Equipment(
                    cursor.getLong(0),      // id
                    cursor.getString(1),    // name
                    cursor.getString(2),    // type
                    cursor.getInt(3),       // armour
                    cursor.getInt(4),       // damage
                    cursor.getInt(5),       // strength
                    cursor.getInt(6),       // agility
                    cursor.getInt(7),       // intelligence
                    cursor.getString(8),    // description
                    cursor.getInt(9)        // equipped
            );
            nonEquippedEquipment.add(nonEquipped);
        }
        cursor.close();
        db.close();
        return nonEquippedEquipment;
    }

    public ArrayList<DungeonRecord> getAllDungeonRecords() {
        ArrayList<DungeonRecord> records = new ArrayList<DungeonRecord>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DungeonRecord.TABLE_NAME, null);
        // Here we iterate through the cursor backwards so that the records the user gets shows the
        // most recent first
        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
            DungeonRecord record = new DungeonRecord(
                    cursor.getLong(0),      // id
                    cursor.getString(1),    // date
                    cursor.getString(2),    // type
                    cursor.getInt(3),       // outcome
                    cursor.getInt(4),       // distance
                    cursor.getInt(5),       // time
                    cursor.getString(6),    // reward
                    cursor.getString(7),    // coords
                    cursor.getString(8)     // skips
            );
            records.add(record);
        }
        cursor.close();
        db.close();
        return records;
    }

    public ArrayList<DungeonLevel> getAllDungeonLevels() {
        // fetches all dungeon levels in the table
        ArrayList<DungeonLevel> levels = new ArrayList<DungeonLevel>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DungeonLevel.TABLE_NAME, null);
        while(cursor.moveToNext()){
            DungeonLevel level = new DungeonLevel(
                    cursor.getLong(0),      // id
                    cursor.getString(1),    // name
                    cursor.getInt(2),       // time
                    cursor.getInt(3),       // distance
                    cursor.getDouble(4),    // pace
                    cursor.getDouble(5)     // multiplier
            );
            levels.add(level);
        }
        cursor.close();
        db.close();
        return levels;
    }


    // ADD TO DATABASE METHODS----------------------------------------------------------------------

    public void addEquipment(Equipment equipment) {
        // add new equipment to Equipment table
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Equipment.COLUMN_NAME, equipment.getName());
        values.put(Equipment.COLUMN_TYPE, equipment.getType());
        values.put(Equipment.COLUMN_ARMOUR, equipment.getArmour());
        values.put(Equipment.COLUMN_DAMAGE, equipment.getDamage());
        values.put(Equipment.COLUMN_STRENGTH, equipment.getStrength());
        values.put(Equipment.COLUMN_AGILITY, equipment.getAgility());
        values.put(Equipment.COLUMN_INTELLIGENCE, equipment.getIntelligence());
        values.put(Equipment.COLUMN_DESCRIPTION, equipment.getDescription());
        values.put(Equipment.COLUMN_EQUIPPED, equipment.getEquipped());
        db.insert(Equipment.TABLE_NAME, null, values);
        db.close();
    }

    public void addItemDescriptor(ItemDescriptor itemDescriptor) {
        // add new item descriptor to itemDescriptors table
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ItemDescriptor.COLUMN_DESCRIPTOR, itemDescriptor.getDescriptor());
        values.put(ItemDescriptor.COLUMN_TYPE, itemDescriptor.getType());
        values.put(ItemDescriptor.COLUMN_MATCH, itemDescriptor.getMatch());
        values.put(ItemDescriptor.COLUMN_ARMOUR_BIAS, itemDescriptor.getArmourBias());
        values.put(ItemDescriptor.COLUMN_DAMAGE_BIAS, itemDescriptor.getDamageBias());
        values.put(ItemDescriptor.COLUMN_STRENGTH_BIAS, itemDescriptor.getStrengthBias());
        values.put(ItemDescriptor.COLUMN_AGILITY_BIAS, itemDescriptor.getAgilityBias());
        values.put(ItemDescriptor.COLUMN_INTELLIGENCE_BIAS, itemDescriptor.getIntelligenceBias());
        db.insert(ItemDescriptor.TABLE_NAME, null, values);
        db.close();
    }

    public void addDungeonRecord(DungeonRecord record) {
        // add new dungeon record to records table
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DungeonRecord.COLUMN_DATE , record.getDate());
        values.put(DungeonRecord.COLUMN_TYPE , record.getType());
        values.put(DungeonRecord.COLUMN_OUTCOME , record.getOutcome());
        values.put(DungeonRecord.COLUMN_DISTANCE , record.getDistance());
        values.put(DungeonRecord.COLUMN_TIME , record.getTime());
        values.put(DungeonRecord.COLUMN_REWARD , record.getReward());
        values.put(DungeonRecord.COLUMN_COORDS , record.getCoords());
        values.put(DungeonRecord.COLUMN_SKIPS , record.getSkips());
        db.insert(DungeonRecord.TABLE_NAME, null, values);
        db.close();
    }

    public void addDungeonLevel(DungeonLevel level) {
        // and new dungeon level to the dungeonLevels table
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DungeonLevel.COLUMN_NAME , level.getName());
        values.put(DungeonLevel.COLUMN_TIME , level.getTime());
        values.put(DungeonLevel.COLUMN_DISTANCE , level.getDistance());
        values.put(DungeonLevel.COLUMN_PACE , level.getPace());
        values.put(DungeonLevel.COLUMN_MULTIPLIER , level.getMultiplier());
        db.insert(DungeonLevel.TABLE_NAME, null, values);
        db.close();
    }


    // EDIT DATABASE METHODS------------------------------------------------------------------------

    public void equipEquipment(Long idToEquip) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Select the current item to equip and update its details
        String query = "SELECT * FROM " + Equipment.TABLE_NAME + " WHERE _id=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(idToEquip)});
        cursor.moveToFirst();
        Equipment toEquip = new Equipment(
                cursor.getLong(0),      // id
                cursor.getString(1),    // name
                cursor.getString(2),    // type
                cursor.getInt(3),       // armour
                cursor.getInt(4),       // damage
                cursor.getInt(5),       // strength
                cursor.getInt(6),       // agility
                cursor.getInt(7),       // intelligence
                cursor.getString(8),    // description
                cursor.getInt(9)        // equipped
        );
        cursor.close();

        try {
            toEquip.setEquipped(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fetch the item to get unequipped first else we will get confused as we will have
        // multiple items equipped.  This way we cannot select the wrong one.
        Equipment toUnequip = getEquippedEquipmentByType(toEquip.getType());
        try {
            toUnequip.setEquipped(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Unequip the item toUnequip
        editEquipment(toUnequip.getId(), toUnequip);
        // Equip the item toEquip
        editEquipment(toEquip.getId(), toEquip);
        db.close();
    }

    public void editEquipment(Long targetId, Equipment updatedEquipment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Equipment.COLUMN_NAME, updatedEquipment.getName());
        values.put(Equipment.COLUMN_TYPE, updatedEquipment.getType());
        values.put(Equipment.COLUMN_ARMOUR, updatedEquipment.getArmour());
        values.put(Equipment.COLUMN_DAMAGE, updatedEquipment.getDamage());
        values.put(Equipment.COLUMN_STRENGTH, updatedEquipment.getStrength());
        values.put(Equipment.COLUMN_AGILITY, updatedEquipment.getAgility());
        values.put(Equipment.COLUMN_INTELLIGENCE, updatedEquipment.getIntelligence());
        values.put(Equipment.COLUMN_DESCRIPTION, updatedEquipment.getDescription());
        values.put(Equipment.COLUMN_EQUIPPED, updatedEquipment.getEquipped());
        db.update(Equipment.TABLE_NAME, values, "_id=" + targetId, null);
    }

    public void editItemDescriptor(Long targetId, ItemDescriptor updatedItemDescriptor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ItemDescriptor.COLUMN_DESCRIPTOR, updatedItemDescriptor.getDescriptor());
        values.put(ItemDescriptor.COLUMN_TYPE, updatedItemDescriptor.getType());
        values.put(ItemDescriptor.COLUMN_MATCH, updatedItemDescriptor.getMatch());
        values.put(ItemDescriptor.COLUMN_ARMOUR_BIAS, updatedItemDescriptor.getArmourBias());
        values.put(ItemDescriptor.COLUMN_DAMAGE_BIAS, updatedItemDescriptor.getDamageBias());
        values.put(ItemDescriptor.COLUMN_STRENGTH_BIAS, updatedItemDescriptor.getStrengthBias());
        values.put(ItemDescriptor.COLUMN_AGILITY_BIAS, updatedItemDescriptor.getAgilityBias());
        values.put(ItemDescriptor.COLUMN_INTELLIGENCE_BIAS, updatedItemDescriptor.getIntelligenceBias());
        db.update(ItemDescriptor.TABLE_NAME, values, "_id=" + targetId, null);

    }

    // REMOVE FROM DATABASE METHODS-----------------------------------------------------------------

    public void removeEquipment(Equipment equipment) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Equipment.TABLE_NAME,
                Equipment.COLUMN_ID + " ?",
                new String[] {String.valueOf(equipment.getId())});
    }

    public void removeDungeonRecord(DungeonRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DungeonRecord.TABLE_NAME,
                DungeonRecord.COLUMN_ID + "=?",
                new String[] {String.valueOf(record.getId())});
    }

    public void removeItemDescriptor(ItemDescriptor itemDescriptor) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ItemDescriptor.TABLE_NAME,
                ItemDescriptor.COLUMN_ID + " ?",
                new String[] {String.valueOf(itemDescriptor.getId())});
    }

    public void removeDungeonLevel(DungeonLevel level) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Equipment.TABLE_NAME,
                DungeonLevel.COLUMN_ID + " ?",
                new String[] {String.valueOf(level.getId())});
    }


    // INITIALIZE DATA METHODS----------------------------------------------------------------------

    public void initializeEquipment() {
        // checks to see if the relevant table exists and has some contents.  If it doesn't,
        // it is created
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Equipment.TABLE_NAME, null);
        while (cursor.moveToNext()) {
            count += 1;
        }
        if (count == 0) {
            Log.d("DATABASEHELPER", Equipment.TABLE_NAME + " table DNE, creating with defaults");
            createDefaultEquipment();
        }
        else {
            Log.d("DATABASEHELPER", Equipment.TABLE_NAME + " table already exists with " + count + " elements");
        }
        cursor.close();
        db.close();
    }

    public void initializeItemDescriptors() {
        // checks to see if the relevant table exists and has some contents.  If it doesn't,
        // it is created
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ItemDescriptor.TABLE_NAME, null);
        while (cursor.moveToNext()) {
            count += 1;
        }
        if (count == 0) {
            Log.d("DATABASEHELPER", ItemDescriptor.TABLE_NAME + " table DNE, creating now");
            createItemDescriptors();
        }
        else {
            Log.d("DATABASEHELPER", ItemDescriptor.TABLE_NAME + " table already exists with " + count + " elements");
        }
        cursor.close();
        db.close();
    }

    public void initializeDungeonLevels() {
        // checks to see if the relevant table exists and has some contents.  If it doesn't,
        // it is created
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DungeonLevel.TABLE_NAME, null);
        while (cursor.moveToNext()) {
            count += 1;
        }
        if (count == 0) {
            Log.d("DATABASEHELPER", DungeonLevel.TABLE_NAME + " table DNE, creating now");
            createDungeonLevels();
        }
        else {
            Log.d("DATABASEHELPER", DungeonLevel.TABLE_NAME + " table already exists with " + count + " elements");
        }
        cursor.close();
        db.close();

    }


    // BASE DATA METHODS AND CONSTRUCTION ROUTINES--------------------------------------------------

    private void createDefaultEquipment() {
        // basic equipment a new player begins with
        addEquipment(new Equipment(0, "Initiate's Helmet", "head", 8, 0, 2, 0, 0, "Charity handout for noobs.", 1));
        addEquipment(new Equipment(0, "Spare Helmet", "head", 7, 0, 1, 4, 4, "Use to test item swapping.", 0));
        addEquipment(new Equipment(0, "Tattered Helmet", "head", 2, 1, 0, 6, 6, "Use to test item swapping.", 0));
        addEquipment(new Equipment(0, "Initiate's Shoulderpads", "shoulders", 6, 0, 2, 0, 2, "Charity handout for noobs.", 1));
        addEquipment(new Equipment(0, "Initiate's Chestplate", "chest", 7, 0, 3, 0, 0, "Charity handout for noobs.", 1));
        addEquipment(new Equipment(0, "Initiate's Gloves", "hands", 5, 2, 0, 3, 0, "Charity handout for noobs.", 1));
        addEquipment(new Equipment(0, "Initiate's Pants", "legs", 7, 0, 0, 0, 3, "Charity handout for noobs.", 1));
        addEquipment(new Equipment(0, "Initiate's Boots", "feet", 3, 4, 1, 1, 1, "Charity handout for noobs.", 1));
        addEquipment(new Equipment(0, "Initiate's Knife", "sharp", 0, 7, 0, 3, 0, "Charity handout for noobs.", 1));
        addEquipment(new Equipment(0, "Initiate's Hammer", "blunt", 1, 6, 1, 0, 2, "Charity handout for noobs.", 1));
        addEquipment(new Equipment(0, "Initiate's Bow", "ranged", 0, 7, 0, 1, 2, "Charity handout for noobs.", 1));
        addEquipment(new Equipment(0, "Spare Gun", "ranged", 1, 3, 2, 7, 0, "Use to test item swapping.", 0));
    }


    private void createDungeonLevels() {
        // no limits or restrictions, no risk of failure
        addDungeonLevel(new DungeonLevel(0, "Dungeon Farm", 0, 0, 0, 1));

        // at least 5km in 1h, or if over one hour, maintain a pace of at least 5km/h
        addDungeonLevel(new DungeonLevel(0, "Babies First Dungeon", 3600, 5000, 5, 1.05));

        // 8km, in any time
        addDungeonLevel(new DungeonLevel(0, "Cave Crawl", 0, 8000, 0, 1.1));

        // atleast 30 mins maintaing a pace of 7
        addDungeonLevel(new DungeonLevel(0, "Forbidden Tomb", 1800, 0, 7, 1.2));

        // 11km, in any time
        addDungeonLevel(new DungeonLevel(0, "11 Levels of Hell", 0, 11000, 0, 1.2));

        // 15km in any time
        addDungeonLevel(new DungeonLevel(0, "Bloodbath", 0, 15000, 0, 1.3));

        // atleast 30 mins maintaing a pace of 9
        addDungeonLevel(new DungeonLevel(0, "Defend the Keep", 1800, 0, 9, 1.4));

        // at least 5km in 30min, or if over 30 min, maintain a pace of at-least 10 km/h
        addDungeonLevel(new DungeonLevel(0, "Escape of the Crumbling Ruins", 1800, 5000, 10, 1.5));

        // 10km in 1h, or if over 1hr, maintain a pace of atleast 10 km/h
        addDungeonLevel(new DungeonLevel(0, "Eruption of Mount Death", 3600, 10000, 10, 1.65));

        // maintain 12km/h pace for at-least 20 minutes
        addDungeonLevel(new DungeonLevel(0, "Raining Fire", 1200, 0, 12, 1.65));

        // atleast 30 mins maintaing a pace of 11
        addDungeonLevel(new DungeonLevel(0, "Gruelling Assault", 1800, 0, 11, 1.8));

        // at least 21km in 2.5hrs, or if over 2.5hrs, maintain a pace of at-least 8.4km/h
        addDungeonLevel(new DungeonLevel(0, "Halfway to Heaven", 9000, 21000, 8.4, 1.8));

        // a marathon, 7h limit
        addDungeonLevel(new DungeonLevel(0, "Marathon Raid", 25200, 42000, 6, 3));
    }


    private void createItemDescriptors() {

        // Type nouns for equipment matching "head" (16)
        addItemDescriptor(new ItemDescriptor(0, "Bandana", "typeNoun", "head", 0, 0, 0, 10,0)); // agi
        addItemDescriptor(new ItemDescriptor(0, "Cowl", "typeNoun", "head", 0, 3, 0, 0, 7));
        addItemDescriptor(new ItemDescriptor(0, "Crown", "typeNoun", "head", 0, 3, 7, 0, 0)); // str
        addItemDescriptor(new ItemDescriptor(0, "Faceguard", "typeNoun", "head", 10, 0, 0, 0, 0)); // amr
        addItemDescriptor(new ItemDescriptor(0, "Goggles", "typeNoun", "head", 0, 10, 0, 0, 0)); // dmg
        addItemDescriptor(new ItemDescriptor(0, "Hat", "typeNoun", "head", 5, 5, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Headdress", "typeNoun", "head", 0, 5, 0, 0, 5));
        addItemDescriptor(new ItemDescriptor(0, "Headwrap", "typeNoun", "head", 0, 0, 0, 5, 5));
        addItemDescriptor(new ItemDescriptor(0, "Helm", "typeNoun", "head", 7, 0, 3, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Helmet", "typeNoun", "head", 8, 1, 2, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Hood", "typeNoun", "head", 0, 0, 0, 7, 3));
        addItemDescriptor(new ItemDescriptor(0, "Mail Coif", "typeNoun", "head", 5, 0, 5, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Mask", "typeNoun", "head", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Snorkel", "typeNoun", "head", 0, 0, 0, 0, 10)); // int
        addItemDescriptor(new ItemDescriptor(0, "Tricorne", "typeNoun", "head", 0, 0, 5, 0, 5));
        addItemDescriptor(new ItemDescriptor(0, "Veil", "typeNoun", "head", 3, 0, 0, 0, 7));

        // Type nouns for equipment matching "shoulders" (8)
        addItemDescriptor(new ItemDescriptor(0, "Amice", "typeNoun", "shoulders", 0, 3, 0, 0, 7));
        addItemDescriptor(new ItemDescriptor(0, "Epaulets", "typeNoun", "shoulders", 0, 10, 0, 0, 0)); // dmg
        addItemDescriptor(new ItemDescriptor(0, "Mantle", "typeNoun", "shoulders", 0, 0, 0, 0, 10)); // int
        addItemDescriptor(new ItemDescriptor(0, "Pauldrons", "typeNoun", "shoulders", 0, 0, 10, 0, 0)); // str
        addItemDescriptor(new ItemDescriptor(0, "Shoulderguards", "typeNoun", "shoulders", 5, 0, 0, 0, 5));
        addItemDescriptor(new ItemDescriptor(0, "Shoulderpads", "typeNoun", "shoulders", 0, 0, 0, 10, 0)); //agi
        addItemDescriptor(new ItemDescriptor(0, "Shoulderplates", "typeNoun", "shoulders", 10, 0, 0, 0, 0)); // amr
        addItemDescriptor(new ItemDescriptor(0, "Spaulders", "typeNoun", "shoulders", 6, 0, 4, 0, 0));

        // Type nouns for equipment matching "chest" (16)
        addItemDescriptor(new ItemDescriptor(0, "Breastplate", "typeNoun", "chest", 7, 0, 3, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Cassock", "typeNoun", "chest", 0, 0, 0, 5, 5));
        addItemDescriptor(new ItemDescriptor(0, "Chestguard", "typeNoun", "chest", 6, 0, 0, 4, 0));
        addItemDescriptor(new ItemDescriptor(0, "Chestpiece", "typeNoun", "chest", 5, 0, 5, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Chestplate", "typeNoun", "chest", 10, 0, 0, 0, 0)); // amr
        addItemDescriptor(new ItemDescriptor(0, "Harness", "typeNoun", "chest", 0, 10, 0, 0, 0)); // str
        addItemDescriptor(new ItemDescriptor(0, "Hauberk", "typeNoun", "chest", 5, 0, 5, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Jacket", "typeNoun", "chest", 0, 0, 0, 10, 0)); // agi
        addItemDescriptor(new ItemDescriptor(0, "Jerkin", "typeNoun", "chest", 0, 0, 4, 6, 0));
        addItemDescriptor(new ItemDescriptor(0, "Kimono", "typeNoun", "chest", 0, 10, 0, 3, 0)); // dmg
        addItemDescriptor(new ItemDescriptor(0, "Rags", "typeNoun", "chest", 0, 0, 0, 0, 10)); // int
        addItemDescriptor(new ItemDescriptor(0, "Robe", "typeNoun", "chest", 0, 2, 0, 0, 8));
        addItemDescriptor(new ItemDescriptor(0, "Shirt", "typeNoun", "chest", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Smock", "typeNoun", "chest", 3, 0, 0, 0, 7));
        addItemDescriptor(new ItemDescriptor(0, "Tunic", "typeNoun", "chest", 3, 0, 0, 7, 0));
        addItemDescriptor(new ItemDescriptor(0, "Vest", "typeNoun", "chest", 2, 0, 0, 8, 0));

        // Type nouns for equipment matching "hands" (9)
        addItemDescriptor(new ItemDescriptor(0, "Fingerguards", "typeNoun", "hands", 0, 5, 0, 5, 0));
        addItemDescriptor(new ItemDescriptor(0, "Fingerless Gloves", "typeNoun", "hands", 0, 0, 0, 10, 0)); // agi
        addItemDescriptor(new ItemDescriptor(0, "Fingerwarmers", "typeNoun", "hands", 0, 0, 0, 0, 10)); // int
        addItemDescriptor(new ItemDescriptor(0, "Gauntlets", "typeNoun", "hands", 0, 0, 10, 0, 0)); // str
        addItemDescriptor(new ItemDescriptor(0, "Gloves", "typeNoun", "hands", 4, 0, 0, 6, 0));
        addItemDescriptor(new ItemDescriptor(0, "Hand Socks", "typeNoun", "hands", 3, 0, 0, 0, 7));
        addItemDescriptor(new ItemDescriptor(0, "Handguards", "typeNoun", "hands", 10, 0, 0, 0, 0)); // amr
        addItemDescriptor(new ItemDescriptor(0, "Handwraps", "typeNoun", "hands", 0, 0, 0, 5, 5));
        addItemDescriptor(new ItemDescriptor(0, "Mittens", "typeNoun", "hands", 0, 10, 0, 0, 0)); // dmg

        // Type nouns for equipment matching "legs" (11)
        addItemDescriptor(new ItemDescriptor(0, "Breeches", "typeNoun", "legs", 5, 0, 0, 5, 0));
        addItemDescriptor(new ItemDescriptor(0, "Greaves", "typeNoun", "legs", 0, 0, 10, 0, 0)); // str
        addItemDescriptor(new ItemDescriptor(0, "Kilt", "typeNoun", "legs", 0, 5, 5, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Leggings", "typeNoun", "legs", 0, 0, 0, 10, 0)); // agi
        addItemDescriptor(new ItemDescriptor(0, "Legplates", "typeNoun", "legs", 10, 0, 0, 0, 0)); // amr
        addItemDescriptor(new ItemDescriptor(0, "Legwraps", "typeNoun", "legs", 0, 0, 0, 4, 6));
        addItemDescriptor(new ItemDescriptor(0, "Loincloth", "typeNoun", "legs", 0, 10, 0, 0, 0)); // dmg
        addItemDescriptor(new ItemDescriptor(0, "Pantaloons", "typeNoun", "legs", 0, 0, 0, 0, 10)); // int
        addItemDescriptor(new ItemDescriptor(0, "Pants", "typeNoun", "legs", 5, 0, 0, 0, 5));
        addItemDescriptor(new ItemDescriptor(0, "Skirt", "typeNoun", "legs", 0, 2, 0, 8, 0));
        addItemDescriptor(new ItemDescriptor(0, "Trousers", "typeNoun", "legs", 7, 0, 3, 0, 0));

        // Type nouns for equipment matching "feet" (15)
        addItemDescriptor(new ItemDescriptor(0, "Boots", "typeNoun", "feet", 7, 0, 0, 3, 0));
        addItemDescriptor(new ItemDescriptor(0, "Clogs", "typeNoun", "feet", 0, 0, 0, 2, 8));
        addItemDescriptor(new ItemDescriptor(0, "Crocs", "typeNoun", "feet", 1, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Flippers", "typeNoun", "feet", 0, 0, 0, 0, 10)); // int
        addItemDescriptor(new ItemDescriptor(0, "Footwraps", "typeNoun", "feet", 0, 0, 0, 6, 4));
        addItemDescriptor(new ItemDescriptor(0, "Geta", "typeNoun", "feet", 0, 0, 0, 5, 5));
        addItemDescriptor(new ItemDescriptor(0, "Moccasins", "typeNoun", "feet", 0, 3, 0, 0, 7));
        addItemDescriptor(new ItemDescriptor(0, "Pathfinders", "typeNoun", "feet", 0, 5, 5, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Sabatons", "typeNoun", "feet", 0, 0, 10, 0, 0)); // str
        addItemDescriptor(new ItemDescriptor(0, "Sandals", "typeNoun", "feet", 3, 0, 7, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Socks", "typeNoun", "feet", 0, 0, 0, 4, 6));
        addItemDescriptor(new ItemDescriptor(0, "Steel Toes", "typeNoun", "feet", 10, 0, 0, 0, 0)); // amr
        addItemDescriptor(new ItemDescriptor(0, "Stompers", "typeNoun", "feet", 0, 10, 0, 0, 0)); // dmg
        addItemDescriptor(new ItemDescriptor(0, "Treads", "typeNoun", "feet", 0, 0, 0, 10, 0)); // agi
        addItemDescriptor(new ItemDescriptor(0, "Warboots", "typeNoun", "feet", 5, 5, 0, 0, 0));

        // Type nouns for equipment matching "sharp" (35)
        addItemDescriptor(new ItemDescriptor(0, "Battle Axe", "typeNoun", "sharp", 0, 4, 6, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Bowie Knife", "typeNoun", "sharp", 0, 6, 0, 4, 0));
        addItemDescriptor(new ItemDescriptor(0, "Broadsword", "typeNoun", "sharp", 0, 5, 5, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Butterfly Knife", "typeNoun", "sharp", 0, 7, 0, 3, 0));
        addItemDescriptor(new ItemDescriptor(0, "Claymore", "typeNoun", "sharp", 0, 3, 7, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Cleaver", "typeNoun", "sharp", 0, 7, 0, 3, 0));
        addItemDescriptor(new ItemDescriptor(0, "Cutlass", "typeNoun", "sharp", 0, 3, 0, 7, 0));
        addItemDescriptor(new ItemDescriptor(0, "Dao", "typeNoun", "sharp", 0, 6, 0, 4, 0));
        addItemDescriptor(new ItemDescriptor(0, "Dirk", "typeNoun", "sharp", 0, 2, 0, 8, 0));
        addItemDescriptor(new ItemDescriptor(0, "Falchion", "typeNoun", "sharp", 0, 7, 3, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Gladius", "typeNoun", "sharp", 0, 8, 2, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Great Sword", "typeNoun", "sharp", 0, 2, 8, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Hatchet", "typeNoun", "sharp", 0, 8, 0, 2, 0));
        addItemDescriptor(new ItemDescriptor(0, "Japanese War Fan", "typeNoun", "sharp", 0, 3, 0, 0, 7));
        addItemDescriptor(new ItemDescriptor(0, "Karambit", "typeNoun", "sharp", 0, 7, 0, 3, 0));
        addItemDescriptor(new ItemDescriptor(0, "Katana", "typeNoun", "sharp", 0, 6, 0, 4, 0));
        addItemDescriptor(new ItemDescriptor(0, "Knife", "typeNoun", "sharp", 0, 7, 0, 0, 3));
        addItemDescriptor(new ItemDescriptor(0, "Kris", "typeNoun", "sharp", 0, 6, 0, 0, 4));
        addItemDescriptor(new ItemDescriptor(0, "Kusarigama", "typeNoun", "sharp", 0, 4, 0, 6, 0));
        addItemDescriptor(new ItemDescriptor(0, "Machete", "typeNoun", "sharp", 0, 5, 0, 5, 0));
        addItemDescriptor(new ItemDescriptor(0, "Military Fork", "typeNoun", "sharp", 0, 1, 9, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Naginata", "typeNoun", "sharp", 0, 2, 8, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Ono", "typeNoun", "sharp", 0, 7, 0, 3, 0));
        addItemDescriptor(new ItemDescriptor(0, "Pickaxe", "typeNoun", "sharp", 0, 2, 8, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Pike", "typeNoun", "sharp", 0, 4, 6, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Polearm", "typeNoun", "sharp", 0, 3, 7, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Rapier", "typeNoun", "sharp", 0, 6, 0, 0, 4));
        addItemDescriptor(new ItemDescriptor(0, "Sabre", "typeNoun", "sharp", 0, 5, 0, 5, 0));
        addItemDescriptor(new ItemDescriptor(0, "Sai", "typeNoun", "sharp", 0, 6, 0, 4, 0));
        addItemDescriptor(new ItemDescriptor(0, "Scimitar", "typeNoun", "sharp", 0, 7, 0, 3, 0));
        addItemDescriptor(new ItemDescriptor(0, "Scythe", "typeNoun", "sharp", 0, 6, 0, 0, 4));
        addItemDescriptor(new ItemDescriptor(0, "Shank", "typeNoun", "sharp", 0, 1, 0, 9, 0));
        addItemDescriptor(new ItemDescriptor(0, "Sickle", "typeNoun", "sharp", 0, 7, 0, 0, 3));
        addItemDescriptor(new ItemDescriptor(0, "Spetum", "typeNoun", "sharp", 0, 2, 8, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Splitting Maul", "typeNoun", "sharp", 0, 1, 9, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Stiletto", "typeNoun", "sharp", 0, 3, 0, 0, 7));
        addItemDescriptor(new ItemDescriptor(0, "Yoroi-Doshi", "typeNoun", "sharp", 0, 7, 0, 3, 0));
        addItemDescriptor(new ItemDescriptor(0, "Zweihander", "typeNoun", "sharp", 0, 5, 5, 0, 0));

        // Type nouns for equipment matching "blunt" (17)
        addItemDescriptor(new ItemDescriptor(0, "9 Iron", "typeNoun", "blunt", 0, 3, 0, 0, 7));
        addItemDescriptor(new ItemDescriptor(0, "Bo Staff", "typeNoun", "blunt", 0, 2, 0, 0, 8));
        addItemDescriptor(new ItemDescriptor(0, "Brass Knuckles", "typeNoun", "blunt", 0, 6, 4, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Club", "typeNoun", "blunt", 0, 3, 7, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Cudgel", "typeNoun", "blunt", 0, 5, 5, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Flail", "typeNoun", "blunt", 0, 6, 4, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Hammer", "typeNoun", "blunt", 0, 4, 6, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Hanbo", "typeNoun", "blunt", 0, 3, 0, 7, 0));
        addItemDescriptor(new ItemDescriptor(0, "Kanabo", "typeNoun", "blunt", 0, 5, 0, 5, 0));
        addItemDescriptor(new ItemDescriptor(0, "Mace", "typeNoun", "blunt", 0, 6, 4, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Maul", "typeNoun", "blunt", 0, 7, 3, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Morning Star", "typeNoun", "blunt", 0, 8, 2, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Nunchaku", "typeNoun", "blunt", 0, 6, 0, 4, 0));
        addItemDescriptor(new ItemDescriptor(0, "Plank", "typeNoun", "blunt", 0, 2, 0, 0, 8));
        addItemDescriptor(new ItemDescriptor(0, "Quarterstaff", "typeNoun", "blunt", 0, 2, 0, 0, 7));
        addItemDescriptor(new ItemDescriptor(0, "Tonfa", "typeNoun", "blunt", 0, 6, 0, 4, 0));
        addItemDescriptor(new ItemDescriptor(0, "War Hammer", "typeNoun", "blunt", 0, 4, 6, 0, 0));

        // Type nouns for equipment matching "ranged" (28)
        addItemDescriptor(new ItemDescriptor(0, "Arbalest", "typeNoun", "ranged", 0, 6, 4, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Ballista", "typeNoun", "ranged", 0, 7, 0, 3, 0));
        addItemDescriptor(new ItemDescriptor(0, "Bag of Rocks", "typeNoun", "ranged", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Bear Trap", "typeNoun", "ranged", 0, 4, 0, 0, 6));
        addItemDescriptor(new ItemDescriptor(0, "Blowdart", "typeNoun", "ranged", 0, 3, 0, 7, 0));
        addItemDescriptor(new ItemDescriptor(0, "Boomerang", "typeNoun", "ranged", 0, 4, 0, 6, 0));
        addItemDescriptor(new ItemDescriptor(0, "Bow", "typeNoun", "ranged", 0, 8, 0, 2, 0));
        addItemDescriptor(new ItemDescriptor(0, "Caltrops", "typeNoun", "ranged", 0, 3, 0, 0, 7));
        addItemDescriptor(new ItemDescriptor(0, "Chakram", "typeNoun", "ranged", 0, 4, 6, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Flintlock Pistol", "typeNoun", "ranged", 0, 8, 0, 0, 2));
        addItemDescriptor(new ItemDescriptor(0, "Glaves", "typeNoun", "ranged", 0, 3, 0, 0, 7));
        addItemDescriptor(new ItemDescriptor(0, "Grimoire", "typeNoun", "ranged", 0, 2, 0, 0, 8));
        addItemDescriptor(new ItemDescriptor(0, "Hand Cannon", "typeNoun", "ranged", 0, 6, 4, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Javelin", "typeNoun", "ranged", 0, 7, 3, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Kunai", "typeNoun", "ranged", 0, 4, 0, 6, 0));
        addItemDescriptor(new ItemDescriptor(0, "Lasso", "typeNoun", "ranged", 0, 3, 0, 0, 7));
        addItemDescriptor(new ItemDescriptor(0, "Longbow", "typeNoun", "ranged", 0, 9, 0, 1, 0));
        addItemDescriptor(new ItemDescriptor(0, "Molotov Cocktail", "typeNoun", "ranged", 0, 6, 0, 0, 4));
        addItemDescriptor(new ItemDescriptor(0, "Musket", "typeNoun", "ranged", 0, 8, 0, 2, 0));
        addItemDescriptor(new ItemDescriptor(0, "Necronomicon", "typeNoun", "ranged", 0, 1, 0, 0, 9));
        addItemDescriptor(new ItemDescriptor(0, "Recurve Bow", "typeNoun", "ranged", 0, 7, 0, 3, 0));
        addItemDescriptor(new ItemDescriptor(0, "Shruiken", "typeNoun", "ranged", 0, 5, 0, 5, 0));
        addItemDescriptor(new ItemDescriptor(0, "Slingshot", "typeNoun", "ranged", 0, 4, 0, 6, 0));
        addItemDescriptor(new ItemDescriptor(0, "Spell Tome", "typeNoun", "ranged", 0, 6, 0, 0, 4));
        addItemDescriptor(new ItemDescriptor(0, "Throwing Knives", "typeNoun", "ranged", 0, 4, 0, 6, 0));
        addItemDescriptor(new ItemDescriptor(0, "Throwing Stars", "typeNoun", "ranged", 0, 6, 0, 4, 0));
        addItemDescriptor(new ItemDescriptor(0, "Tomahawk", "typeNoun", "ranged", 0, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Wand", "typeNoun", "ranged", 0, 7, 0, 0, 3));
        addItemDescriptor(new ItemDescriptor(0, "Whip", "typeNoun", "ranged", 0, 3, 7, 0, 0));

        // Style adjectives for equipment type "all armours"
        addItemDescriptor(new ItemDescriptor(0,"Padded", "styleAdjective", "armour", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0,"Reinforced", "styleAdjective", "armour", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0,"Revealing", "styleAdjective", "armour", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0,"Skimpy", "styleAdjective", "armour", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0,"Sturdy", "styleAdjective", "armour", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0,"Stylish", "styleAdjective", "armour", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0,"Tattered", "styleAdjective", "armour", 0, 0, 0, 0, 10));

        // Style adjectives for equipment type "all weapons"
        addItemDescriptor(new ItemDescriptor(0, "Deadly", "styleAdjective", "weapon", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Disguised", "styleAdjective", "weapon", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Rusty", "styleAdjective", "weapon", 0, 0, 10, 0, 0));


        // Style adjectives for equipment type "sharp"
        addItemDescriptor(new ItemDescriptor(0, "Curved", "styleAdjective", "sharp", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Bent", "styleAdjective", "sharp", 0, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Blunt", "styleAdjective", "sharp", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Dragonglass", "styleAdjective", "sharp", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Glistening", "styleAdjective", "sharp", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Pointy", "styleAdjective", "sharp", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Shaky", "styleAdjective", "sharp", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Sharp", "styleAdjective", "sharp", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Sharpened", "styleAdjective", "sharp", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Straight", "styleAdjective", "sharp", 0, 10, 0, 0, 0));

        // Style adjectives for equipment type "blunt"
        // none yet

        // Style adjectives for equipment type "ranged"
        addItemDescriptor(new ItemDescriptor(0, "Accurate", "styleAdjective", "ranged", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Inaccurate", "styleAdjective", "ranged", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Limitless", "styleAdjective", "ranged", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Precise", "styleAdjective", "ranged", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Steady", "styleAdjective", "ranged", 0, 10, 0, 0, 0));

        // Style adjectives for equipment type "all"
        addItemDescriptor(new ItemDescriptor(0, "Barbed", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Battle Scarred", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Black", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Blackened", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Blood Stained", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Bloody", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Blue", "styleAdjective", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Bright", "styleAdjective", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Brown", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Broken", "styleAdjective", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Bulky", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Burning", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Copper", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Cursed", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Damaged", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Dark", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Devilish", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Diamond Studded", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Dirty", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Electro-charged", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Elemental", "styleAdjective", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Empowered", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Enchanted", "styleAdjective", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Evangelical", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Evil", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Experienced", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Exquisite", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Fiery", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Flamboyant", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Flexible", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Flimsy", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Frostbitten", "styleAdjective", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Futuristic", "styleAdjective", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Glowing", "styleAdjective", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Godly", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Golden", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Green", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Grey", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Heavy", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Holy", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Homemade", "styleAdjective", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Huge", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Icy", "styleAdjective", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Imbued", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Inscribed", "styleAdjective", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Indigo", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Jewel Encrusted", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Large", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Light", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Metallic", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "New", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Orange", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Poison Coated", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Pre-loved", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Red", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Scorched", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Second Hand", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Shoddy", "styleAdjective", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Silver", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Small", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Smelly", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Steel", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Tainted", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Unusually Large", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Used", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Violet", "styleAdjective", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Weighted", "styleAdjective", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Well Built", "styleAdjective", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Wet", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "White", "styleAdjective", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Wooden", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Worn", "styleAdjective", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Yellow", "styleAdjective", "all", 0, 0, 0, 10, 0));

        // Style nouns for equipment type "all"
        addItemDescriptor(new ItemDescriptor(0, "Acrobat", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Angel", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Archangel", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Archmage", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Archer", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Artisan", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Assassin", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Banshee", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Barbarian", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Bastard", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Behemoth", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Believer", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Berserker", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Bishop", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Blacksmith", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Blademaster", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Bloodseeker", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Bounty Hunter", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Brawler", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Burglar", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Butcher", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Cannibal", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Centaur", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Champion", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Conscript", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Convict", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Corrupted Soul", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Cowboy", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Coward", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Crazed Villager", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Creep", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Crusader", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Cthulhu", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Deathknight", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Defiler", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Demigod", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Demon", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Demon Enslaver", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Demon Summoner", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Demon Witch", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Demonhunter", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Deuler", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Devil", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Dictator", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Dwarf", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Dreadlord", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Druid", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Emperor", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Engineer", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Executioner", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Faceless Man", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Fallen Saint", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Fiend", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Fighter", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Footman", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Gladiator", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Gnome", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Goblin", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Godeater", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Godking", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Golem", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Gorgon", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Grim Reaper", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Guard", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Hellborn", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Heretic", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Holy Spirit", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Houndmaster", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Hunter", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Imp", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Judge", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Juggernaut", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "King", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Kingslayer", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Knight", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Kraken", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Lepper", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Lich", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Lizardman", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Mage", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Magical Girl", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Martyr", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Mastermind", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Midget", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Missionary", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Monk", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Murderer", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Mutant", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Necromancer", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Nightstalker", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Ninja", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Nobleman", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Ogre", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Opportunist", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Orphan", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Outlaw", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Paladin", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Peasant", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Pirate", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Priest", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Prince", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Princess", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Prisoner", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Professor", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Protector", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Psychic", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Psycho", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Pyromaniac", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Queen", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Ranger", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Rebel", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Rogue", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Sailor", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Saint", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Samurai", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Sasquatch", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Satanist", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Satyr", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Scoundrel", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Serial Killer", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Shadowdancer", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Shaman", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Shapeshifter", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Shogun", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Slayer", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Smuggler", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Sniper", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Soldier", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Sorcerer", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Succubus", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Summoner", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Sycophant", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Thief", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Torturer", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Treant", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Trickster", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Tyrant", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Vampire", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Unsullied", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Vampirehunter", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Warlock", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Warrior", "styleNoun", "all", 0, 0, 10, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Werewolf", "styleNoun", "all", 0, 0, 0, 10, 0));
        addItemDescriptor(new ItemDescriptor(0, "Whitewalker", "styleNoun", "all", 10, 0, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Wildling", "styleNoun", "all", 0, 10, 0, 0, 0));
        addItemDescriptor(new ItemDescriptor(0, "Witch", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Witchdoctor", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Wizard", "styleNoun", "all", 0, 0, 0, 0, 10));
        addItemDescriptor(new ItemDescriptor(0, "Wraith", "styleNoun", "all", 0, 0, 10, 0, 0));
    }

}
