package fit3037.dmmic2.dungeonrunner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

/*
* This is the Main Menu activity, that provides users with various buttons to navigate the
* application.  It also performs some data initialisation required for other activities.
* */

public class MainMenu extends AppCompatActivity {

    Button mGoToDungeonSelectionButton;
    Button mGoToDungeonJournalButton;
    Button mGoToInventoryButton;
    Button mGoToOptionsButton;
    Button mGoToAboutButton;
    private DatabaseHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // On app startup, if the databases are empty, then populate them!
        mDBHelper = new DatabaseHelper(getApplicationContext());
        mDBHelper.initializeEquipment();
        mDBHelper.initializeItemDescriptors();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Main Menu");

        mGoToDungeonSelectionButton = findViewById(R.id.goToDungeonSelectionButton);
        mGoToDungeonJournalButton = findViewById(R.id.goToDungeonJournalButton);
        mGoToInventoryButton = findViewById(R.id.goToInventoryButton);
        mGoToOptionsButton = findViewById(R.id.goToOptionsButton);
        mGoToAboutButton = findViewById(R.id.goToAboutButton);

        // On Click: Go to the Dungeon Selection activity
        mGoToDungeonSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                Intent newIntent = new Intent(MainMenu.this, DungeonSelection.class);
                startActivity(newIntent);
            }
        });

        // On Click: Go to the Dungeon Journal activity
        mGoToDungeonJournalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                Intent newIntent = new Intent(MainMenu.this, DungeonJournal.class);
                startActivity(newIntent);
            }
        });

        // On Click: Go to the Inventory activity
        mGoToInventoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                Intent newIntent = new Intent(MainMenu.this, Inventory.class);
                startActivity(newIntent);
            }
        });

        // On Click: Go to the Options activity
        mGoToOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                Intent newIntent = new Intent(MainMenu.this, Options.class);
                startActivity(newIntent);
            }
        });

        // On Click: Go to the About activity
        mGoToAboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                Intent newIntent = new Intent(MainMenu.this, About.class);
                startActivity(newIntent);
            }
        });

    }

}
