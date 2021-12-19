package com.jbaker7.dronecalendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;

public class AddEventActivity extends AppCompatActivity {

    private AddEvent eventTask = null;
    private View mProgressView;

    Calendar eventCalendar = Calendar.getInstance();
    Calendar startTime = null;
    Calendar endTime = null;

    DatePickerDialog picker;
    TimePickerDialog timePicker;

    EditText eventName;
    EditText eventDate;
    EditText eventStartTime;
    EditText eventEndTime;
    EditText eventDescription;
    EditText eventAttendees;
    EditText eventType;
    Button saveButton;

    String apiToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        apiToken = getIntent().getStringExtra("token");
        mProgressView = findViewById(R.id.login_progress);

        eventName = findViewById(R.id.eventNameBox);
        eventDate = findViewById(R.id.eventDateBox);
        eventStartTime = findViewById(R.id.eventStartTimeBox);
        eventEndTime = findViewById(R.id.eventEndTimeBox);
        eventDescription = findViewById(R.id.eventDescriptionBox);
        eventAttendees = findViewById(R.id.eventAttendeesBox);
        eventType = findViewById(R.id.eventTypeBox);
        saveButton = findViewById(R.id.saveEventButton);

        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            int day = eventCalendar.get(Calendar.DAY_OF_MONTH);
            int month = eventCalendar.get(Calendar.MONTH);
            int year = eventCalendar.get(Calendar.YEAR);
            picker = new DatePickerDialog(AddEventActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        eventCalendar.set(Calendar.YEAR, year);
                        eventCalendar.set(Calendar.MONTH, month);
                        eventCalendar.set(Calendar.DAY_OF_MONTH, day);
                        eventDate.setText(getDateString(eventCalendar));
                    }
                }, year, month, day);
            picker.show();
            }
        });

        eventStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            startTime = Calendar.getInstance();
            int hour = startTime.get(Calendar.HOUR_OF_DAY);
            int minute = startTime.get(Calendar.MINUTE);
            timePicker = new TimePickerDialog(AddEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                startTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                startTime.set(Calendar.MINUTE, selectedMinute);
                eventStartTime.setText(getTimeString(startTime));
                }
            }, hour, minute, false);
            timePicker.show();
            }
        });

        eventEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            endTime = Calendar.getInstance();
            int hour = endTime.get(Calendar.HOUR_OF_DAY);
            int minute = endTime.get(Calendar.MINUTE);
            timePicker = new TimePickerDialog(AddEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                endTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                endTime.set(Calendar.MINUTE, selectedMinute);
                eventEndTime.setText( getTimeString(endTime));
                }
            }, hour, minute, false);
            timePicker.show();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptEventAdd();
            }
        });

        FloatingActionButton fab = findViewById(R.id.trashButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            if (button == DialogInterface.BUTTON_POSITIVE) {
                                Toast.makeText(AddEventActivity.this,
                                    "Changes cancelled.",
                                    Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            }
                        }
                    };

                AlertDialog.Builder builder = new AlertDialog.Builder(AddEventActivity.this);
                builder.setMessage("Are you sure you wish to cancel?\nChanges will not be saved.")
                    .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                    .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                    .show();
            }
        });
    }

    private void attemptEventAdd() {
        String mEventName = eventName.getText().toString();
        String mEventDate = eventDate.getText().toString();
        String mEventStartTime = eventStartTime.getText().toString();
        String mEventEndTime = eventEndTime.getText().toString();
        String mEventDescription = eventDescription.getText().toString();
        String mEventAttendees = eventAttendees.getText().toString();
        String mEventType = eventType.getText().toString();

        boolean cancel = false;

        if (TextUtils.isEmpty(mEventName) || TextUtils.isEmpty(mEventStartTime) || TextUtils.isEmpty(mEventEndTime) || TextUtils.isEmpty(mEventDate)
                || TextUtils.isEmpty(mEventDescription) || TextUtils.isEmpty(mEventAttendees) || TextUtils.isEmpty(mEventType)) {
            Toast.makeText(AddEventActivity.this, "All fields are required.", Toast.LENGTH_LONG).show();
            cancel = true;
        }

        else if (startTime == null || endTime == null || startTime.after(endTime)) {
            Toast.makeText(AddEventActivity.this, "Event End Time must be after Start Time.", Toast.LENGTH_LONG).show();
            cancel = true;
        }

        if (!cancel) {
            eventTask = new AddEvent(mEventName, getDateTimeString(mEventDate, mEventStartTime), getDateTimeString(mEventDate, mEventEndTime), mEventDescription, mEventAttendees, mEventType, apiToken);
            eventTask.execute((Void) null);
        }
    }

    public class AddEvent extends AsyncTask<Void, Void, Boolean> {

        private final String eventName;
        private final String eventStartDate;
        private final String eventEndDate;
        private final String eventDescription;
        private final String eventAttendees;
        private final String eventType;

        String apiToken;

        private String result = null;

        AddEvent(String name, String start, String end, String description, String attendees, String type, String token) {
            eventName = name;
            eventStartDate = start;
            eventEndDate = end;
            eventDescription = description;
            eventAttendees = attendees;
            eventType = type;
            apiToken = token;
        }

        @Override
        protected Boolean doInBackground(Void... params)  {
            boolean eventResult = false;

            try {
                URL loginUrl = new URL(getString(R.string.API_ADDRESS) + "events");
                HttpsURLConnection loginConn = (HttpsURLConnection) loginUrl.openConnection();
                loginConn.setRequestMethod("POST");
                loginConn.setRequestProperty("User-Agent", "android-schedule-app-v0.1");
                loginConn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                loginConn.setRequestProperty("Accept","application/json");
                loginConn.setRequestProperty("Authorization", "Bearer " + apiToken);
                loginConn.setDoOutput(true);
                loginConn.setDoInput(true);

                JSONObject loginParam = new JSONObject();
                try {
                    loginParam.put("name", eventName);
                    loginParam.put("start_date", eventStartDate);
                    loginParam.put("end_date", eventEndDate);
                    loginParam.put("description", eventDescription);
                    loginParam.put("attendees", eventAttendees);
                    loginParam.put("type", eventType);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                DataOutputStream os = new DataOutputStream(loginConn.getOutputStream());
                os.writeBytes(loginParam.toString());
                os.flush();
                os.close();

                if (loginConn.getResponseCode() == 200) {
                    InputStream response = loginConn.getInputStream();
                    InputStreamReader responseReader =
                            new InputStreamReader(response, "UTF-8");
                    JsonReader jsonReader = new JsonReader(responseReader);

                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            String key = jsonReader.nextName();
                            if (key.equals("result")) {
                                String value = jsonReader.nextString();
                                result = value;
                                if (value.equals("Success.")) {
                                    eventResult = true;
                                } else {
                                    eventResult = false;
                                }
                            } else {
                                jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                    }
                    jsonReader.endArray();
                    jsonReader.close();
                    loginConn.disconnect();

                } else {
                    eventResult = false;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return eventResult;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                Toast.makeText(AddEventActivity.this, result, Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(AddEventActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getDateString(Calendar calendar) {
        String myFormat = "MM-dd-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        return sdf.format(calendar.getTime());
    }

    private String getTimeString(Calendar calendar) {
        String myFormat = "hh:mm a";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        return sdf.format(calendar.getTime());
    }

    private String getDateTimeString(String halfDate, String halfTime) {
        String newTime = null;
        SimpleDateFormat fullFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm a");

        try {
            Date date = fullFormat.parse(halfDate + " " + halfTime);
            String dateTimeString = "MM-dd-yyyy hh:mm:ss a";
            SimpleDateFormat sdf = new SimpleDateFormat(dateTimeString, Locale.US);
            newTime =  sdf.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newTime;
    }
}
