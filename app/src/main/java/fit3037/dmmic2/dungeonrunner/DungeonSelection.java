package fit3037.dmmic2.dungeonrunner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/*
* This activity displays all the possible dungeons the user can choose from, that is all of the
* dungeons within the dungeonLevels database.  These are clickable, and clicking one will take the
* user to the DungeonRunningTracker screen, and will pass along the constraints of the selected
* dungeon via intents.
* */

public class DungeonSelection extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView mListView;
    private DungeonLevelAdapter mAdapter;
    private ArrayList<DungeonLevel> mLevels;
    private DatabaseHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dungeon_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dungeon Selection");

        mDBHelper = new DatabaseHelper(getApplicationContext());
        mDBHelper.initializeDungeonLevels();

        // Get the database handler
        mDBHelper = new DatabaseHelper(getApplicationContext());

        // initialise the list of dungeon levels
        mLevels = new ArrayList<>(mDBHelper.getAllDungeonLevels());
        mListView = (ListView) findViewById(R.id.dungeonSelectionListView);

        // create adapter and associate it with equipped list
        mAdapter = new DungeonLevelAdapter(this, mLevels);
        // set on clicks (long for deletion, regular for viewing in detail)
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);

    }

    // click selects the clicked dungeon level
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
        Log.d("DATABASE SELECTION", "clicked a thing");

        Intent newIntent = new Intent(DungeonSelection.this, DungeonRunningTracker.class);
        DungeonLevel level = (DungeonLevel) adapterView.getItemAtPosition(position);
        newIntent.putExtra("dungeonLevel", level);
        startActivity(newIntent);
    }

}
