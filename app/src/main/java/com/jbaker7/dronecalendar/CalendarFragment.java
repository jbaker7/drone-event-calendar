package com.jbaker7.dronecalendar;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class CalendarFragment extends Fragment {

    private String tokenParam;
    private String userParam;
    EventAdapter arrayAdapter;
    ListView eventList;
    ArrayList<Event> eventListJSON = new ArrayList<>();

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putString("token", param1);
        args.putString("user_id", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tokenParam = getArguments().getString("token");
            userParam = getArguments().getString("user_id");
        }

        LoadCalendarEvents loading = new LoadCalendarEvents(tokenParam);
        loading.execute((Void) null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LoadCalendarEvents loading = new LoadCalendarEvents(tokenParam);
        loading.execute((Void) null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.calendar_fragment, container, false);

        View empty = v.findViewById(R.id.empty);
        eventList = v.findViewById(R.id.event_list);
        eventList.setEmptyView(empty);

        CalendarView myCalendar = v.findViewById(R.id.calendar_view);
        myCalendar.setOnDateChangeListener( new CalendarView.OnDateChangeListener() {
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                String padMonth=String.format("%02d",month+1);
                String padDay=String.format("%02d",dayOfMonth);
                String padYear=String.format("%02d",year);
                String fullDate = padMonth+ "-" + padDay + "-" +padYear;
                arrayAdapter.getFilter().filter("DATE::" + fullDate);
            }
        });

        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final Integer cID = (int) id;

                final Dialog dialog = new Dialog(view.getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.event_dialog);

                TextView nameText = dialog.findViewById(R.id.dialog_name);
                TextView startText = dialog.findViewById(R.id.dialog_start);
                TextView endText = dialog.findViewById(R.id.dialog_end);
                TextView descriptionText = dialog.findViewById(R.id.dialog_description);
                TextView attendeesText = dialog.findViewById(R.id.dialog_attendees);
                TextView typeText = dialog.findViewById(R.id.dialog_type);
                nameText.setText(eventListJSON.get(cID).getName());
                startText.setText(getTimeString(eventListJSON.get(cID).getStart()));
                endText.setText(getTimeString(eventListJSON.get(cID).getEnd()));
                descriptionText.setText(eventListJSON.get(cID).getDescription());
                attendeesText.setText(String.valueOf(eventListJSON.get(cID).getAttendees()));
                typeText.setText(eventListJSON.get(cID).getType());

                if (String.valueOf(eventListJSON.get(cID).getCreatedBy()).equals(userParam)) {
                    Button editButton = dialog.findViewById(R.id.editButton);
                    editButton.setVisibility(View.VISIBLE);
                    editButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            Intent editIntent = new Intent(v.getContext(), EditEventActivity.class);
                            editIntent.putExtra("id", String.valueOf(eventListJSON.get(cID).getId()));
                            editIntent.putExtra("name", String.valueOf(eventListJSON.get(cID).getName()));
                            editIntent.putExtra("start", String.valueOf(eventListJSON.get(cID).getStart()));
                            editIntent.putExtra("end", String.valueOf(eventListJSON.get(cID).getEnd()));
                            editIntent.putExtra("description", String.valueOf(eventListJSON.get(cID).getDescription()));
                            editIntent.putExtra("attendees", String.valueOf(eventListJSON.get(cID).getAttendees()));
                            editIntent.putExtra("type", String.valueOf(eventListJSON.get(cID).getType()));
                            editIntent.putExtra("token", tokenParam);
                            startActivityForResult(editIntent, 1);
                        }
                    });

                }

                Button dialogButton = dialog.findViewById(R.id.okButton);
                dialogButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        return v;
    }

    public class LoadCalendarEvents extends AsyncTask<Void, Void, Boolean> {

        String apiToken;

        LoadCalendarEvents (String token) {
            apiToken = token;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            eventListJSON.clear();
            try {
                URL queryUrl = new URL(getString(R.string.API_ADDRESS) + "events/");
                HttpsURLConnection queryConn = (HttpsURLConnection) queryUrl.openConnection();
                queryConn.setRequestMethod("GET");
                queryConn.setRequestProperty("User-Agent", "android-schedule-app-v0.1");
                queryConn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                queryConn.setRequestProperty("Accept", "application/json");
                queryConn.setRequestProperty("Authorization", "Bearer " + apiToken);
                queryConn.setDoInput(true);

                if (queryConn.getResponseCode() == 200) {
                    InputStream response = queryConn.getInputStream();
                    InputStreamReader responseReader =
                            new InputStreamReader(response, "UTF-8");

                    BufferedReader r = new BufferedReader(responseReader);
                    StringBuilder total = new StringBuilder();
                    for (String line; (line = r.readLine()) != null; ) {
                        total.append(line).append('\n');
                    }
                    try {
                        JSONArray jArray = new JSONArray(total.toString());

                        for (int i=0; i < jArray.length(); i++)
                        {
                            JSONObject oneObject = jArray.getJSONObject(i);
                            int id = oneObject.getInt("id");

                            String name = oneObject.getString("name");
                            String start = oneObject.getString("start_date");
                            String end = oneObject.getString("end_date");
                            String description = oneObject.getString("description");
                            int attendees = oneObject.getInt("attendees");
                            String type = oneObject.getString("type");
                            int createdBy =oneObject.getInt("created_by");

                            eventListJSON.add(new Event(id, name, start, end, description, attendees, type, createdBy));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    queryConn.disconnect();

                } else {
                    Toast.makeText(getActivity(), "Connection unsuccessful. Please try again later", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            arrayAdapter = new EventAdapter(getContext(), eventListJSON);
            arrayAdapter.sort(new Comparator<Event>() {
                @Override
                public int compare(Event o1, Event o2) {
                    return o1.getStart().compareTo(o2.getStart());
                }
            });

            ListView eventList = getView().findViewById(R.id.event_list);
            eventList.setAdapter(arrayAdapter);
        }
    }

    public String getTimeString(String fullDate) {

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
