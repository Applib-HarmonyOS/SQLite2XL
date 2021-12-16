package com.example.sqlite2xl.adapter;

import ohos.agp.components.BaseItemProvider;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.Text;
import ohos.app.Context;
import com.example.sqlite2xl.ResourceTable;
import com.example.sqlite2xl.model.Users;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomAdapter class.
 */
public class CustomAdapter extends BaseItemProvider {
    private Context context;
    private List<Users> usersList;

    /**
     * CustomAdapter constructor.
     *
     * @param context context
     * @param usersList List of users
     */
    public CustomAdapter(Context context, List<Users> usersList) {
        this.context = context;
        this.usersList = new ArrayList<>();
        this.usersList = usersList;
    }

    @Override
    public int getCount() {
        return usersList.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public Component getComponent(int position, Component convertView, ComponentContainer parent) {
        LayoutScatter inflater = LayoutScatter.getInstance(context);
        Component rowView = inflater.parse(ResourceTable.Layout_list_item, parent, false);
        Text yourFirstTextView = (Text) rowView.findComponentById(ResourceTable.Id_listview_firsttextview);
        Text yourSecondTextView = (Text) rowView.findComponentById(ResourceTable.Id_listview_secondtextview);
        yourFirstTextView.setText(usersList.get(position).getContactPersonName());
        yourSecondTextView.setText(usersList.get(position).getContactNumber());
        return rowView;
    }
}