package fit3037.dmmic2.dungeonrunner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/*
* This activity display the user all of their equipped items in a list view.  Any of the items
* can be clicked which will send them to an InspectSwapEquipped activity for that item (allowing the
* user to view the item in detail, and see other items they can equip in that slot).
* */

public class DressingRoom extends AppCompatActivity implements AdapterView.OnItemClickListener  {

    private ListView mListView;
    private EquipmentAdapter mAdapter;
    private ArrayList<Equipment> mEquippedList;
    private DatabaseHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dressing_room);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dressing Room");

        // Get the database handler
        mDBHelper = new DatabaseHelper(getApplicationContext());

        // initialise the equipped equipment list
        mEquippedList = new ArrayList<>(mDBHelper.getAllEquippedEquipment());
        mListView = (ListView) findViewById(R.id.equippedListView);

        // create adapter and associate it with equipped list
        mAdapter = new EquipmentAdapter(this, mEquippedList);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);

    }


    // Refresh the contents of the page after reaching it from use of the back button
    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        mEquippedList = new ArrayList<>(mDBHelper.getAllEquippedEquipment());  // possible type in lab notes... they writes GetAllBooks (capital G?)
        mListView = (ListView) findViewById(R.id.equippedListView);

        // create adapter and associate it with book list
        mAdapter = new EquipmentAdapter(this, mEquippedList);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);
    }


    // click allows viewing of specific equipment type
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
        Intent newIntent = new Intent(DressingRoom.this, InspectSwapEquipped.class);
        Equipment equipment = (Equipment) adapterView.getItemAtPosition(position);
        newIntent.putExtra("type", equipment.getType());
        startActivity(newIntent);
    }

}
