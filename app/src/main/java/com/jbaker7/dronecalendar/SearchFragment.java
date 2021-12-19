package com.jbaker7.dronecalendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;

public class SearchFragment extends Fragment {

    private String tokenParam;
    EventAdapter arrayAdapter;
    Calendar eventCalendar = Calendar.getInstance();
    DatePickerDialog picker;
    EditText searchTextBox;
    TextInputLayout editTextLayout;
    Button searchButton;
    ListView resultListView;

    ArrayList<Event> eventListJSON = new ArrayList<>();

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String param1) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString("token", param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tokenParam = getArguments().getString("token");
        }

        LoadCalendarEvents loading = new LoadCalendarEvents(tokenParam);
        loading.execute((Void) null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchTextBox = view.findViewById(R.id.searchTextBox);
        editTextLayout = view.findViewById(R.id.textLayout);
        searchButton = view.findViewById(R.id.searchButton);
        resultListView = view.findViewById(R.id.resultList);

        final Spinner dropdown = view.findViewById(R.id.searchByBox);
        String[] items = new String[]{"Date", "Name", "Description", "Type"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        searchTextBox.clearFocus();
                        searchTextBox.setFocusableInTouchMode(false);
                        searchTextBox.setText("");
                        editTextLayout.setHint("Select Date");
                        searchTextBox.setInputType(InputType.TYPE_CLASS_TEXT);
                        searchTextBox.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int day = eventCalendar.get(Calendar.DAY_OF_MONTH);
                                int month = eventCalendar.get(Calendar.MONTH);
                                int year = eventCalendar.get(Calendar.YEAR);
                                picker = new DatePickerDialog(v.getContext(),
                                        new DatePickerDialog.OnDateSetListener() {
                                            @Override
                                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                                eventCalendar.set(Calendar.YEAR, year);
                                                eventCalendar.set(Calendar.MONTH, month);
                                                eventCalendar.set(Calendar.DAY_OF_MONTH, day);
                                                searchTextBox.setText(getDateString(eventCalendar));
                                            }
                                        }, year, month, day);
                                picker.show();
                            }
                        });
                        break;

                    case 1:
                        searchTextBox.setFocusableInTouchMode(true);
                        searchTextBox.setText("");
                        editTextLayout.setHint("Name");
                        searchTextBox.setOnClickListener(null);
                        searchTextBox.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;

                    case 2:
                        searchTextBox.setFocusableInTouchMode(true);
                        searchTextBox.setText("");
                        editTextLayout.setHint("Description");
                        searchTextBox.setOnClickListener(null);
                        searchTextBox.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;

                    case 3:
                        searchTextBox.setFocusableInTouchMode(true);
                        searchTextBox.setText("");
                        editTextLayout.setHint("Event Type");
                        searchTextBox.setOnClickListener(null);
                        searchTextBox.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Dialog dialog = new Dialog(view.getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.event_dialog);

                TextView nameText = dialog.findViewById(R.id.dialog_name);
                TextView startText = dialog.findViewById(R.id.dialog_start);
                TextView endText = dialog.findViewById(R.id.dialog_end);
                TextView descriptionText = dialog.findViewById(R.id.dialog_description);
                TextView attendeesText = dialog.findViewById(R.id.dialog_attendees);
                TextView typeText = dialog.findViewById(R.id.dialog_type);
                nameText.setText(eventListJSON.get((int) id).getName());
                startText.setText(getTimeString(eventListJSON.get((int) id).getStart()));
                endText.setText(getTimeString(eventListJSON.get((int) id).getEnd()));
                descriptionText.setText(eventListJSON.get((int) id).getDescription());
                attendeesText.setText(String.valueOf(eventListJSON.get((int) id).getAttendees()));
                typeText.setText(eventListJSON.get((int) id).getType());

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

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            int searchType = dropdown.getSelectedItemPosition();
            String searchTerm = null;

            if (!TextUtils.isEmpty(searchTextBox.getText().toString())) {
                switch (searchType) {
                    case 0:
                        searchTerm = "DATE::" + searchTextBox.getText().toString();
                        break;
                    case 1:
                        searchTerm = "NAME::" + searchTextBox.getText().toString();
                        break;
                    case 2:
                        searchTerm = "DESCRIPTION::" + searchTextBox.getText().toString();
                        break;
                    case 3:
                        searchTerm = "TYPE::" + searchTextBox.getText().toString();
                        break;
                }
                arrayAdapter.getFilter().filter(searchTerm);
            }
            }
        });
    }

    public class LoadCalendarEvents extends AsyncTask<Void, Void, Boolean> {

        String apiToken;

        LoadCalendarEvents (String token) {
            apiToken = token;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
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
                    Toast.makeText(getActivity(), "The Connection was unsuccessful. Please try again later.", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            arrayAdapter = new EventAdapter(getContext(), eventListJSON);

            ListView eventList = getView().findViewById(R.id.resultList);
            eventList.setAdapter(arrayAdapter);
        }
    }

    private String getTimeString(String fullDate) {
        String newTime = null;
        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = fullFormat.parse(fullDate);
            String justTime = "hh:mm a";
            SimpleDateFormat sdf = new SimpleDateFormat(justTime, Locale.US);
            newTime =  sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newTime;
    }

    private String getDateString(Calendar calendar) {
        String myFormat = "MM-dd-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        return sdf.format(calendar.getTime());
    }
}
