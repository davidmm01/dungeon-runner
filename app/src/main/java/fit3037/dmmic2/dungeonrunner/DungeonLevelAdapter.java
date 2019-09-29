package fit3037.dmmic2.dungeonrunner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/*
* Adapter for DungeonLevel, for the list view on the DungeonSelection activity.
* */

public class DungeonLevelAdapter extends BaseAdapter {

    private Context mCurrentContext;
    private ArrayList<DungeonLevel> mLevels;

    public DungeonLevelAdapter(Context con, ArrayList<DungeonLevel> levels) {
        mCurrentContext = con;
        mLevels = levels;
    }

    @Override
    public int getCount() { return mLevels.size(); }

    @Override
    public Object getItem(int i) { return mLevels.get(i); }

    @Override
    public long getItemId(int i) { return i; }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // if the view already exists, inflate it
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mCurrentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.dungeon_level_list_element, null);
        }

        // link the text view fields
        TextView levelNameView = (TextView) view.findViewById(R.id.levelNameTextView);
        TextView levelDescriptionView = (TextView) view.findViewById(R.id.levelDescriptionTextView);
        TextView rewardMultiplierView = (TextView) view.findViewById(R.id.rewardMultiplierTextView);

        // set the values
        DungeonLevel target = mLevels.get(i);
        levelNameView.setText(target.getName());
        levelDescriptionView.setText(getDescriptionFromMetrics(target.getTime(), target.getDistance(), target.getPace()));
        String multiplierForDisplay = "Reward Multiplier: " + target.getMultiplier();
        rewardMultiplierView.setText(multiplierForDisplay);

        return view;
    }

    public String getDescriptionFromMetrics(int time, int distance, double pace) {
        if ((time == 0) && (distance != 0) && (pace == 0)){
            return "Conquer at least " + (distance/1000) + "km";
        }
        else if ((time != 0) && (distance != 0) && (pace != 0)){
            return "Cover at least " + + (distance/1000) + "km in " + (time/60) + " minutes, or maintain that pace for longer";
        }
        else if ((time != 0) && (distance == 0) && (pace != 0)){
            return "Maintain a pace of " + pace + "km/h for at least " + (time/60) + " minutes";
        }
        else if((time == 0)&&(distance == 0)&&(pace == 0)){
            return "Classic mindless farming";
        }
        return "something else should have been returned lol";
    }

}
