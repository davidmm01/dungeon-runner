package fit3037.dmmic2.dungeonrunner;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/*
* Adapter for DungeonRecord, for the list view on the DungeonJournal activity.
* */

public class DungeonRecordAdapter extends BaseAdapter {
    private Context mCurrentContext;
    private ArrayList<DungeonRecord> mRecordList;

    public DungeonRecordAdapter(Context con, ArrayList<DungeonRecord> records) {
        mCurrentContext = con;
        mRecordList = records;
    }

    @Override
    public int getCount() { return mRecordList.size(); }

    @Override
    public Object getItem(int i) { return mRecordList.get(i); }

    @Override
    public long getItemId(int i) { return i; }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // if the view already exists, inflate it
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mCurrentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.dungeon_record_list_element, null);
        }

        // link the text view fields
        TextView dateView = (TextView) view.findViewById(R.id.dateTextView);
        TextView typeOutcomeView = (TextView) view.findViewById(R.id.typeAndOutcomeTextView);
        TextView rewardSummaryView = (TextView) view.findViewById(R.id.rewardSummaryTextView);
        TextView timeTakenView = (TextView) view.findViewById(R.id.timeTakenTextView);
        TextView distanceView = (TextView) view.findViewById(R.id.distanceTextView);

        // set the values
        DungeonRecord target = mRecordList.get(i);
        dateView.setText(target.getDate());

        typeOutcomeView.setText(target.getType());

        rewardSummaryView.setText(target.getReward());

        String time = getDisplayTimeFromSeconds(target.getTime());
        timeTakenView.setText(time);

        distanceView.setText(getDisplayDistanceFromMeters(target.getDistance()));

        return view;

    }

    public String getDisplayTimeFromSeconds(int totalSeconds ){
        // Reference: https://stackoverflow.com/questions/6118922/convert-seconds-value-to-hours-minutes-seconds
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String getDisplayDistanceFromMeters(int meters ){
        double km = meters/1000.00;
        return km + "km";
    }


}
