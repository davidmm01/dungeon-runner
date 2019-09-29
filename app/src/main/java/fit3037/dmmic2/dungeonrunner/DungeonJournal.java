package fit3037.dmmic2.dungeonrunner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/*
* This activity displays all the dungeons the user has run before, showing all the basic details
* in an easy format to understand.
* */

public class DungeonJournal extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ListView mListView;
    private DungeonRecordAdapter mAdapter;
    private ArrayList<DungeonRecord> mRecords;
    private DatabaseHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dungeon_journal);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dungeon Journal");

        // Get the database handler
        mDBHelper = new DatabaseHelper(getApplicationContext());

        // initialise the list of dungeon records
        mRecords = new ArrayList<>(mDBHelper.getAllDungeonRecords());
        mListView = (ListView) findViewById(R.id.journalListView);

        // create adapter and associate it with equipped list
        mAdapter = new DungeonRecordAdapter(this, mRecords);
        mListView.setAdapter(mAdapter);
        // set on clicks, long for deletion, regular for detailed view
        mListView.setOnItemLongClickListener(this);
        mListView.setOnItemClickListener(this);

    }


    // click allows viewing of specific equipment type
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
        Intent newIntent = new Intent(DungeonJournal.this, DungeonRecordMapDetails.class);
        DungeonRecord record = (DungeonRecord) adapterView.getItemAtPosition(position);
        newIntent.putExtra("record", record);
        startActivity(newIntent);
    }


    @Override
    public boolean onItemLongClick(final AdapterView<?> adapterView, View view, int i, final long l) {
        // long click for deletion, with pop-up confirmation box
        AlertDialog.Builder builder = new AlertDialog.Builder(DungeonJournal.this);
        builder.setTitle("Delete Record?");
        builder.setMessage("Do you want to delete this dungeon record?\nThis action is final!");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mDBHelper.removeDungeonRecord((DungeonRecord) adapterView.getItemAtPosition((int) l));
                // Refresh the current activity to display the equipment that is equipped
                finish();
                startActivity(getIntent());
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
        return true;
    }
}
