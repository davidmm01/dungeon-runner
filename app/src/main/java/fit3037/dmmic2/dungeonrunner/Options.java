package fit3037.dmmic2.dungeonrunner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

/*
* This activity provides users with some basic options, such as a full data reset.  The contents of
* this activity is rather bare bones and is expected to grow in the future as this application is
* developed further.
* */

public class Options extends AppCompatActivity {

    Button mDataResetButton;
    private DatabaseHelper mDBHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Options");

        mDBHelper = new DatabaseHelper(getApplicationContext());
        mDataResetButton = findViewById(R.id.resetDataButton);

        // On Click: Go to the Dungeon Selection activity
        mDataResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Options.this);
                // gives users the ability to wipe all data at once, instead of doing manual
                // deletion of things
                builder.setTitle("Reset all data?");
                builder.setNegativeButton("No", null);
                builder.setMessage("Are you sure?  This is irreversible!  " +
                        "All dungeon records will be removed, and all items will be deleted.");
                builder.setPositiveButton("Yes, delete everything",  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mDBHelper.resetData();
                    }
                });
                builder.show();
            }
        });
    }

}
