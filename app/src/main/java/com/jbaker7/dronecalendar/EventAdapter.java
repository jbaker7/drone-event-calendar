package com.jbaker7.dronecalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EventAdapter extends ArrayAdapter<Event>  implements Filterable {

    private ArrayList<Event> arrayList;
    private ArrayList<Event> originalArrayList;

    public EventAdapter(Context context, ArrayList<Event> objects) {
        super(context, 0, objects);
        arrayList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Event event = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_list, parent, false);
        }
        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvTime = convertView.findViewById(R.id.tvTime);
        TextView tvDescription = convertView.findViewById(R.id.tvDescription);
        String totalTime = getTimeString(event.getStart()) + " - " + getTimeString(event.getEnd());
        tvName.setText(event.getName());
        tvTime.setText(totalTime);
        tvDescription.setText(event.getDescription());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<Event> FilteredList = new ArrayList<>();
                String searchTerm = constraint.toString().toLowerCase();
                String searchType = searchTerm.substring(0, searchTerm.indexOf("::"));
                searchTerm = searchTerm.substring(searchTerm.indexOf("::")+2);

                if (originalArrayList == null) {
                    originalArrayList = new ArrayList<>(arrayList);
                }

                if (searchTerm == null || searchTerm.length() == 0) {
                    results.count = originalArrayList.size();
                    results.values = originalArrayList;
                } else {

                    for (int i = 0; i < originalArrayList.size(); i++) {
                        Event tempEvent = originalArrayList.get(i);
                        String data = null;
                        if (searchType.equals("date")) {
                            data = getDateString(originalArrayList.get(i).getStart());
                        }
                        if (searchType.equals("name")) {
                            data = originalArrayList.get(i).getName();
                        }
                        if (searchType.equals("description")) {
                            data = originalArrayList.get(i).getDescription();
                        }
                        if (searchType.equals("type")) {
                            data = originalArrayList.get(i).getType();
                        }

                        if (data.toLowerCase().contains(searchTerm)) {
                            FilteredList.add(tempEvent);
                        }
                    }

                    results.count = FilteredList.size();
                    results.values = FilteredList;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                arrayList = (ArrayList<Event>) results.values;
                clear();
                int count = arrayList.size();
                for(int i = 0; i < count; i++){
                    add(arrayList.get(i));
                    notifyDataSetInvalidated();
                }
                notifyDataSetChanged();
            }
        };
    }

    private String getDateString(String fullDate) {
        String newTime = null;
        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = fullFormat.parse(fullDate);
            String justDate = "MM-dd-yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(justDate, Locale.US);
            newTime =  sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newTime;
    }

    private String getTimeString(String fullDate) {
        String newTime = null;
        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = fullFormat.parse(fullDate);
            String justTime = "h:mm a";
            SimpleDateFormat sdf = new SimpleDateFormat(justTime, Locale.US);
            newTime =  sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    return newTime;
    }
}
