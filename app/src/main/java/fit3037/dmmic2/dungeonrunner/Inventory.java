package fit3037.dmmic2.dungeonrunner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/*
* This activity shows the user a summary of their current stats, being the addition of all the
* item points of their equipped items.  It also provides buttons to go to the Dressing Room, and
* stays up to date with the equipped equipment even when reached using the back arrow.
* */

public class Inventory extends AppCompatActivity {

    Button mGoToDressingRoomButton;
    private DatabaseHelper mDBHelper;
    ArrayList<Equipment> mEquippedEquipment;
    HashMap<String, Integer> mTotals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Inventory");

        // Get the database handler
        mDBHelper = new DatabaseHelper(getApplicationContext());

        TextView armourView = (TextView) findViewById(R.id.armourInventoryTextView);
        TextView damageView = (TextView) findViewById(R.id.damageInventoryTextView);
        TextView strengthView = (TextView) findViewById(R.id.strengthInventoryTextView);
        TextView agilityView = (TextView) findViewById(R.id.agilityInventoryTextView);
        TextView intelligenceView = (TextView) findViewById(R.id.intelligenceInventoryTextView);

        mGoToDressingRoomButton  = findViewById(R.id.goToDressingRoomButton);

        mEquippedEquipment = mDBHelper.getAllEquippedEquipment();
        mTotals = sumStatType(mEquippedEquipment);

        armourView.setText(String.valueOf(mTotals.get("armour")));
        damageView.setText(String.valueOf(mTotals.get("damage")));
        strengthView.setText(String.valueOf(mTotals.get("strength")));
        agilityView.setText(String.valueOf(mTotals.get("agility")));
        intelligenceView.setText(String.valueOf(mTotals.get("intelligence")));

        // On Click: Go to the Options activity
        mGoToDressingRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                Intent newIntent = new Intent(Inventory.this, DressingRoom.class);
                startActivity(newIntent);
            }
        });
    }

    // Used to calculate the total amount of points per stat type for a list of items.  Providing
    // this function with the players equipped items gives the players current power.
    // Eventually, this stuff can be put into another database, called Player or something.
    public HashMap<String, Integer> sumStatType(ArrayList<Equipment> equipmentList){
        HashMap<String, Integer> totals = new HashMap<>();
        totals.put("armour", 0);
        totals.put("damage", 0);
        totals.put("strength", 0);
        totals.put("agility", 0);
        totals.put("intelligence", 0);
        for (Equipment equipment : equipmentList) {
            totals.put("armour", totals.get("armour") + equipment.getArmour());
            totals.put("damage", totals.get("damage") + equipment.getDamage());
            totals.put("strength", totals.get("strength") + equipment.getStrength());
            totals.put("agility", totals.get("agility") + equipment.getAgility());
            totals.put("intelligence", totals.get("intelligence") + equipment.getIntelligence());
        }
        return totals;
    }

    // Refresh the contents of the page after reaching it from use of the back button
    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();

        mDBHelper = new DatabaseHelper(getApplicationContext());

        TextView armourView = (TextView) findViewById(R.id.armourInventoryTextView);
        TextView damageView = (TextView) findViewById(R.id.damageInventoryTextView);
        TextView strengthView = (TextView) findViewById(R.id.strengthInventoryTextView);
        TextView agilityView = (TextView) findViewById(R.id.agilityInventoryTextView);
        TextView intelligenceView = (TextView) findViewById(R.id.intelligenceInventoryTextView);

        mGoToDressingRoomButton  = findViewById(R.id.goToDressingRoomButton);

        mEquippedEquipment = mDBHelper.getAllEquippedEquipment();
        mTotals = sumStatType(mEquippedEquipment);

        armourView.setText(String.valueOf(mTotals.get("armour")));
        damageView.setText(String.valueOf(mTotals.get("damage")));
        strengthView.setText(String.valueOf(mTotals.get("strength")));
        agilityView.setText(String.valueOf(mTotals.get("agility")));
        intelligenceView.setText(String.valueOf(mTotals.get("intelligence")));

    }

}
