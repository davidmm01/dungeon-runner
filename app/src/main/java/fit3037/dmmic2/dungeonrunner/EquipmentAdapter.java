package fit3037.dmmic2.dungeonrunner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/*
* Adapter for Equipment, for the list view on both the DressingRoom and InspectSwapEquipped
* activities.
* */

public class EquipmentAdapter extends BaseAdapter {
    private Context mCurrentContext;
    private ArrayList<Equipment> mEquipmentList;


    public EquipmentAdapter(Context con, ArrayList<Equipment> equipped) {
        mCurrentContext = con;
        mEquipmentList = equipped;
    }

    @Override
    public int getCount() { return mEquipmentList.size(); }

    @Override
    public Object getItem(int i) { return mEquipmentList.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // if the view already exists, inflate it
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mCurrentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.equipped_item_list_element, null);
        }

        // link the text view fields
        TextView nameView = (TextView) view.findViewById(R.id.nameTextView);
        TextView statSummaryView = (TextView) view.findViewById(R.id.statSummaryTextView);

        // set the values
        Equipment target = mEquipmentList.get(i);

        nameView.setText(target.getName());
        String statSummary = "Amr: " + target.getArmour() +
                ",   Dmg: " + target.getDamage() +
                ",   Str: " + target.getStrength() +
                ",   Agi: " + target.getAgility() +
                ",   Int: " + target.getIntelligence();
        statSummaryView.setText(statSummary);

        return view;
    }

}
