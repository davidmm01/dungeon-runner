package fit3037.dmmic2.dungeonrunner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/*
* This activity display the equipped item for that slot to the user in detail, as well as all other
* unequipped items the user owns for that slot.  They can equip different items here.  The list view
* makes use of the equipment adapter.
* */

public class InspectSwapEquipped extends AppCompatActivity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView mListView;
    private Equipment mCurrentEquipped;
    private EquipmentAdapter mAdapter;
    private ArrayList<Equipment> mEquipmentList;
    private DatabaseHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspect_swap_equipped);

        // initialise the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Inspection");

        // Get the database handler
        mDBHelper = new DatabaseHelper(getApplicationContext());

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");

        // Get the currently equipped item of the relevant type
        mCurrentEquipped = mDBHelper.getEquippedEquipmentByType(type);

        // Initialise the text views to display current equipped item data
        TextView typeView = (TextView) findViewById(R.id.equipmentTypeTextView);
        TextView nameView = (TextView) findViewById(R.id.equipmentNameTextView);
        TextView armourView = (TextView) findViewById(R.id.armourInspectSwapTextView);
        TextView damageView = (TextView) findViewById(R.id.damageInspectSwapTextView);
        TextView strengthView = (TextView) findViewById(R.id.strengthInspectSwapTextView);
        TextView agilityView = (TextView) findViewById(R.id.agilityInspectSwapTextView);
        TextView intelligenceView = (TextView) findViewById(R.id.intelligenceInspectSwapTextView);
        TextView descriptionView = (TextView) findViewById(R.id.descriptionInspectSwapTextView);

        // Set text views appropriately
        typeView.append(type);
        nameView.setText(mCurrentEquipped.getName());
        armourView.append(String.valueOf(mCurrentEquipped.getArmour()));
        damageView.append(String.valueOf(mCurrentEquipped.getDamage()));
        strengthView.append(String.valueOf(mCurrentEquipped.getStrength()));
        agilityView.append(String.valueOf(mCurrentEquipped.getAgility()));
        intelligenceView.append(String.valueOf(mCurrentEquipped.getIntelligence()));
        descriptionView.append(mCurrentEquipped.getDescription());

        // initialise the non equipped equipment list
        mEquipmentList = new ArrayList<>(mDBHelper.getAllNonEquippedEquipmentByType(type));
        Log.d("INSPECT SWAP EQUIPPED", "found " + mEquipmentList.size() +
                " alternate items");

        mListView = (ListView) findViewById(R.id.swapToListView);

        // create adapter and associate it with equipment list
        mAdapter = new EquipmentAdapter(this, mEquipmentList);
        mListView.setOnItemLongClickListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);

    }

    // Click allows viewing of Book
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {

        // Set up data for display
        final Equipment toEquip = (Equipment) adapterView.getItemAtPosition(position);
        Equipment old = mDBHelper.getEquippedEquipmentByType(toEquip.getType());
        String armourDiff = presentableStatDifference(toEquip.getArmour(), old.getArmour());
        String damageDiff = presentableStatDifference(toEquip.getDamage(), old.getDamage());
        String strengthDiff = presentableStatDifference(toEquip.getStrength(), old.getStrength());
        String agilityDiff = presentableStatDifference(toEquip.getAgility(), old.getAgility());
        String intelligenceDiff = presentableStatDifference(toEquip.getIntelligence(), old.getIntelligence());

        AlertDialog.Builder builder = new AlertDialog.Builder(InspectSwapEquipped.this);
        builder.setTitle("Swap Equipped?");

        builder.setMessage("Do you want to equip " + toEquip.getName() + "?\n" +
                "The following stat changes will occur: \n" +
                "Armour: " + armourDiff + "\n" +
                "Damage: " + damageDiff + "\n" +
                "Strength: " + strengthDiff + "\n" +
                "Agility: " + agilityDiff + "\n" +
                "Intelligence: " + intelligenceDiff + "\n"
        );

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                mDBHelper.equipEquipment(toEquip.getId());

                // Refresh the current activity to display the equipment that is equipped
                finish();
                startActivity(getIntent());
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> adapterView, View view, int i, final long l) {
        // long click for deletion, with pop-up confirmation box
        AlertDialog.Builder builder = new AlertDialog.Builder(InspectSwapEquipped.this);
        builder.setTitle("Delete Equipment?");

        builder.setMessage("Do you want to delete this item?\nThis action is final!");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mDBHelper.removeEquipment((Equipment) adapterView.getItemAtPosition((int) l));
                // Refresh the current activity to display the equipment that is equipped
                finish();
                startActivity(getIntent());
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
        return true;
    }


    // Pretty way of showing the difference between two items to the player.  Ensures integers
    // are always signed, and gives Same instead of 0 since I think it looks a bit better
    public String presentableStatDifference(int previous, int replacement){
        int diff = previous - replacement;
        if (diff == 0){
            return "Same";
        }
        else if (diff < 0) {
            return String.valueOf(diff);
        }
        return "+" + diff;
    }

}
