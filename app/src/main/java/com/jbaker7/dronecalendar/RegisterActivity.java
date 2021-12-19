package com.jbaker7.dronecalendar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class RegisterActivity extends AppCompatActivity {

    private RegisterUser mAuthTask = null;

    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private EditText mEmailView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mUsernameView = findViewById(R.id.username);
        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        Button mRegisterButton = findViewById(R.id.sign_in_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        mUsernameView.setError(null);
        mPasswordView.setError(null);

        String username = mUsernameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError("This field is required.");
            focusView = mUsernameView;
            cancel = true;
        }

        if (!verifyEmail(email) || TextUtils.isEmpty(email)) {
            mEmailView.setError("Please enter a valid email address");
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("This field is required.");
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            showProgress(true);
            mAuthTask = new RegisterUser(username, email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean verifyEmail(String email) {
        boolean validEmail = false;
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            validEmail = true;
        }
        return validEmail;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class RegisterUser extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mEmail;
        private final String mPassword;
        private String result = null;

        RegisterUser(String username, String email, String password) {
            mUsername = username;
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params)  {
            boolean loginResult = false;
                try {
                    URL loginUrl = new URL(getString(R.string.API_ADDRESS) + "users");
                    HttpsURLConnection loginConn = (HttpsURLConnection) loginUrl.openConnection();
                    loginConn.setRequestMethod("POST");
                    loginConn.setRequestProperty("User-Agent", "android-schedule-app-v0.1");
                    loginConn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    loginConn.setRequestProperty("Accept","application/json");
                    loginConn.setDoOutput(true);
                    loginConn.setDoInput(true);

                    JSONObject loginParam = new JSONObject();
                    try {
                        loginParam.put("username", mUsername);
                        loginParam.put("email", mEmail);
                        loginParam.put("password", mPassword);
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
                                        loginResult = true;
                                    } else {
                                        loginResult = false;
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
                        loginResult = false;
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            return loginResult;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_LONG).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            } else {
                Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_LONG).show();
                mUsernameView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

