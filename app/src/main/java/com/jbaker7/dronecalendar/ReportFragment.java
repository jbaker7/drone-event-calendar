package com.jbaker7.dronecalendar;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
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

public class ReportFragment extends Fragment {

    private String tokenParam;

    Spinner reportDropdown;
    Spinner yearDropdown;
    Spinner monthDropdown;
    LinearLayout yearLayout;
    LinearLayout monthLayout;
    Button generateButton;

    ArrayList<Event> eventListJSON = new ArrayList<>();

    public ReportFragment() {
        // Required empty public constructor
    }

    public static ReportFragment newInstance(String param1) {
        ReportFragment fragment = new ReportFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.report_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reportDropdown = view.findViewById(R.id.report_type_box);
        yearDropdown = view.findViewById(R.id.reportYearBox);
        monthDropdown = view.findViewById(R.id.reportMonthBox);

        yearLayout = view.findViewById(R.id.year_layout);
        monthLayout = view.findViewById(R.id.month_layout);

        generateButton = view.findViewById(R.id.generate_button);

        String[] reportList = new String[]{"Events per Month", "Busiest Days of the Week"};
        String[] yearList = new String[]{"2018", "2019", "2020"};
        String[] monthList = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        ArrayAdapter<String> reportAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, reportList);
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, yearList);
        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, monthList);
        reportDropdown.setAdapter(reportAdapter);
        yearDropdown.setAdapter(monthAdapter);
        monthDropdown.setAdapter(weekAdapter);

        reportDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        yearLayout.setVisibility(View.VISIBLE);
                        monthLayout.setVisibility(View.GONE);
                        break;

                    case 1:
                        yearLayout.setVisibility(View.GONE);
                        monthLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int reportType = reportDropdown.getSelectedItemPosition();
                    switch (reportType) {
                        case 0:
                            LoadEvents loadMonthReport = new LoadEvents(tokenParam, "month");
                            loadMonthReport.execute((Void) null);
                            break;
                        case 1:
                            LoadEvents loadWeekReport = new LoadEvents(tokenParam, "week");
                            loadWeekReport.execute((Void) null);
                            break;
                    }
            }
        });
    }

    private void generateMonthReport() {
        String yearType = yearDropdown.getSelectedItem().toString();
        int[] eventCount = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < eventListJSON.size(); i++) {
            if (getYear(eventListJSON.get(i).getStart()).equals(yearType)) {
                int mon;
                mon = getMonth(eventListJSON.get(i).getStart());
                eventCount[mon] = eventCount[mon] + 1;
            }
        }

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.report_dialog);

        TableLayout monthView = dialog.findViewById(R.id.months_report);
        TableLayout weekView = dialog.findViewById(R.id.week_report);
        monthView.setVisibility(View.VISIBLE);
        weekView.setVisibility(View.GONE);

        TextView title = dialog.findViewById(R.id.report_title);
        title.setText("Report:\nNumber of Events per Month");

        TextView jan = dialog.findViewById(R.id.january);
        TextView feb = dialog.findViewById(R.id.february);
        TextView march = dialog.findViewById(R.id.march);
        TextView april = dialog.findViewById(R.id.april);
        TextView may = dialog.findViewById(R.id.may);
        TextView june = dialog.findViewById(R.id.june);
        TextView july = dialog.findViewById(R.id.july);
        TextView aug = dialog.findViewById(R.id.august);
        TextView sept = dialog.findViewById(R.id.september);
        TextView oct = dialog.findViewById(R.id.october);
        TextView nov = dialog.findViewById(R.id.november);
        TextView dec = dialog.findViewById(R.id.december);
        jan.setText(String.valueOf(eventCount[0]));
        feb.setText(String.valueOf(eventCount[1]));
        march.setText(String.valueOf(eventCount[2]));
        april.setText(String.valueOf(eventCount[3]));
        may.setText(String.valueOf(eventCount[4]));
        june.setText(String.valueOf(eventCount[5]));
        july.setText(String.valueOf(eventCount[6]));
        aug.setText(String.valueOf(eventCount[7]));
        sept.setText(String.valueOf(eventCount[8]));
        oct.setText(String.valueOf(eventCount[9]));
        nov.setText(String.valueOf(eventCount[10]));
        dec.setText(String.valueOf(eventCount[11]));
        Calendar genTime = Calendar.getInstance();

        TextView timeStamp = dialog.findViewById(R.id.time_stamp);
        timeStamp.setText("Report generated on " + getDateString(genTime) + " at " + getTimeString(genTime));

                Button dialogButton = dialog.findViewById(R.id.okButton);
                dialogButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
    }

    private void generateWeekReport() {
        String monthType = monthDropdown.getSelectedItem().toString();
        int[] eventCount = {0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < eventListJSON.size(); i++) {
            if (getMonthString(eventListJSON.get(i).getStart()).equals(monthType)) {
                int day;
                day = getDayOfWeek(eventListJSON.get(i).getStart())-1;
                eventCount[day] = eventCount[day] + 1;
            }
        }

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.report_dialog);

        TableLayout monthView = dialog.findViewById(R.id.months_report);
        TableLayout weekView = dialog.findViewById(R.id.week_report);
        monthView.setVisibility(View.GONE);
        weekView.setVisibility(View.VISIBLE);

        TextView title = dialog.findViewById(R.id.report_title);
        title.setText("Report:\nNumber of Events by Day of the Week in " + monthType);

        TextView sun = dialog.findViewById(R.id.sunday);
        TextView mon = dialog.findViewById(R.id.monday);
        TextView tue = dialog.findViewById(R.id.tuesday);
        TextView wed = dialog.findViewById(R.id.wednesday);
        TextView thu = dialog.findViewById(R.id.thursday);
        TextView fri = dialog.findViewById(R.id.friday);
        TextView sat = dialog.findViewById(R.id.saturday);

        sun.setText(String.valueOf(eventCount[0]));
        mon.setText(String.valueOf(eventCount[1]));
        tue.setText(String.valueOf(eventCount[2]));
        wed.setText(String.valueOf(eventCount[3]));
        thu.setText(String.valueOf(eventCount[4]));
        fri.setText(String.valueOf(eventCount[5]));
        sat.setText(String.valueOf(eventCount[6]));

        Calendar genTime = Calendar.getInstance();

        TextView timeStamp = dialog.findViewById(R.id.time_stamp);
        timeStamp.setText("Report generated on " + getDateString(genTime) + " at " + getTimeString(genTime));

        Button dialogButton = dialog.findViewById(R.id.okButton);
        dialogButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public class LoadEvents extends AsyncTask<Void, Void, Boolean> {

        String apiToken;
        String reportType;

        LoadEvents (String token, String type) {
            apiToken = token;
            reportType = type;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean queryResult = false;
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
                            //try {
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
                    Toast.makeText(getActivity(), "The connection was unsuccessful. Please try again later.", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return queryResult;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (reportType.equals("month")) {
                generateMonthReport();
            }

            if (reportType.equals("week")) {
                generateWeekReport();
            }
        }
    }

    private int getMonth(String fullDate) {
        String newMonth = null;
        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = fullFormat.parse(fullDate);
            String justMonth = "MM";
            SimpleDateFormat sdf = new SimpleDateFormat(justMonth, Locale.US);
            newMonth =  sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int monthInt = Integer.parseInt(newMonth);
        monthInt = monthInt - 1;
        return monthInt;
    }

    private int getDayOfWeek(String fullDate) {
        Calendar dayCal = Calendar.getInstance();
        try {
        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dayCal.setTime(fullFormat.parse(fullDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int dayInt = dayCal.get(Calendar.DAY_OF_WEEK);
        return dayInt;
    }

    private String getMonthString(String fullDate) {
        String newMonth = null;
        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = fullFormat.parse(fullDate);
            String justMonth = "MMMM";
            SimpleDateFormat sdf = new SimpleDateFormat(justMonth, Locale.US);
            newMonth =  sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newMonth;
    }

    private String getYear(String fullDate) {
        String newYear = null;
        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = fullFormat.parse(fullDate);
            String justYear = "yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(justYear, Locale.US);
            newYear =  sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newYear;
    }

    private String getTimeString(Calendar calendar) {
        String myFormat = "hh:mm:ss a";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        return sdf.format(calendar.getTime());
    }

    private String getDateString(Calendar calendar) {
        String myFormat = "MM-dd-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        return sdf.format(calendar.getTime());
    }
}
