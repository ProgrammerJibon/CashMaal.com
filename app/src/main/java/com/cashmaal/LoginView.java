package com.cashmaal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Executor;

public class LoginView extends AppCompatActivity {
    Button finger, sign, reg, forgot;
    EditText email, pass;
    TextView error;
    Activity activity;
    CustomTools customTools;
    ProgressBar progressBar;
    String TAG = "errnos", url;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;
    String login_key;
    String last_email;
    String has_url;

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_view);
        // inittializer
        activity = this;
        customTools = new CustomTools(activity);
        url = customTools.publicDomain();
        login_key = customTools.setPref("login_key", null);
        last_email = customTools.setPref("last_email", null);
        check_connection();
        // getting firebase unique id/ token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                customTools.setPref("FirebaseDeviceId", token);
                Log.e(TAG, token);
            }
        });
        // check bundles and get firebase link parameter
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("url") != null) {
                has_url = extras.getString("url");
            }
        }
        // find views
        finger = activity.findViewById(R.id.finger);
        sign = activity.findViewById(R.id.signin);
        forgot = activity.findViewById(R.id.forgot_pass);
        reg = activity.findViewById(R.id.reg_new);
        email = activity.findViewById(R.id.email);
        pass = activity.findViewById(R.id.password);
        error = activity.findViewById(R.id.error);
        progressBar = activity.findViewById(R.id.progressBar);
        // default finger check
        finger.setVisibility(View.GONE);
        if (!Objects.equals(login_key, "")) {
            BiometricManager biometricManager = BiometricManager.from(activity);
            if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                finger.setVisibility(View.VISIBLE);
                Executor executor = ContextCompat.getMainExecutor(activity);
                biometricPrompt = new BiometricPrompt(LoginView.this, executor, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        error.setText("Fingerprint Authentication Error");
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        Toast.makeText(activity, "Login Succeed as " + last_email, Toast.LENGTH_LONG).show();
                        open_main("https://www.cashmaal.com/?i=sign-in&app_key=" + login_key + "&appv=" + BuildConfig.VERSION_NAME);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        error.setText("Fingerprint Authentication Failed");
                    }
                });
                finger.setOnClickListener(view -> {
                    if (check_connection()) {
                        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                                .setTitle(activity.getApplicationInfo().loadLabel(activity.getPackageManager()).toString())
                                .setDescription("Login as " + last_email)
                                .setDeviceCredentialAllowed(false)
                                .setNegativeButtonText("Use another account")
                                .build();
                        biometricPrompt.authenticate(promptInfo);
                    }
                });
                finger.callOnClick();
            }

        }
        // call for click
        reg.setOnClickListener(view -> open_main("https://www.cashmaal.com/sign-in?i=sign-up"));
        forgot.setOnClickListener(view -> open_main("https://www.cashmaal.com/sign-in?i=f-p"));
        sign.setOnClickListener(v -> {
            if (!check_connection()) {
                return;
            }
            sign.setClickable(false);
            sign.setFocusable(false);
            sign.setTextColor(Color.rgb(25, 25, 25));
            if (email.getError() != null || email.getText().toString().equals("") || !isValidEmail(email.getText())) {
                email.setError("Enter valid email address");
                email.requestFocus();
                sign.setClickable(true);
                sign.setFocusable(true);
                sign.setTextColor(Color.rgb(255, 255, 255));
            } else {
                email.clearFocus();
                if (pass.getError() != null || pass.getText().toString().equals("") || pass.getText().length() < 4) {
                    pass.setError("Enter valid password");
                    pass.requestFocus();
                    sign.setClickable(true);
                    sign.setFocusable(true);
                    sign.setTextColor(Color.rgb(255, 255, 255));
                } else {
                    pass.clearFocus();
                    String signurl = "https://www.cashmaal.net/callbacks/app_login.php";
                    try {

                        String e = URLEncoder.encode(email.getText().toString(), StandardCharsets.UTF_8.toString());
                        String p = URLEncoder.encode(pass.getText().toString(), StandardCharsets.UTF_8.toString());
                        String t = customTools.setPref("FirebaseDeviceId", null);
                        progressBar.setIndeterminate(true);
                        Internet2 login = new Internet2(activity, signurl, "email=" + e + "&password=" + p + "&token=" + t, (code, result) -> {
                            progressBar.setIndeterminate(false);
                            try {
                                if (result.has("error")) {
                                    error.setText(result.getString("error"));
                                    error.setVisibility(View.VISIBLE);
                                    pass.requestFocus();
                                    sign.setClickable(true);
                                    sign.setFocusable(true);
                                    sign.setTextColor(Color.rgb(255, 255, 255));
                                } else if (result.has("login_key")) {
                                    login_key = customTools.setPref("login_key", result.getString("login_key"));
                                    last_email = customTools.setPref("last_email", URLDecoder.decode(e, StandardCharsets.UTF_8.toString()));
                                    open_main("https://www.cashmaal.com/?i=sign-in&app_key=" + login_key + "&appv=" + BuildConfig.VERSION_NAME);
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, ex.toString());
                            }
                        });
                        login.execute();
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                }
            }
        });

    }

    public void open_main(String url) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (has_url != null) {
            intent.putExtra("URL", has_url);
        } else {
            intent.putExtra("URL", url);
        }
        activity.startActivity(intent);
        activity.finish();
        overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_out_right);
    }
    public boolean check_connection() {
        TextView view1 = activity.findViewById(R.id.restricted_edit_access_as_per_google_developer_and_NDA);
//        view1.setText(Html.fromHtml(customTools.error_check("PGk+QXBwIERldmVsb3BlZCBieTwvaT4gPGI+UHJvZ3JhbW1lckppYm9uPC9iPg")));
        view1.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(customTools.error_check("aHR0cHM6Ly93d3cuamlib24uaW8v")))));
        if (!customTools.checkInternetConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Connection error!")
                    .setMessage("Unable to connect to the internet...")
                    .setPositiveButton("Retry", (dialog, which) -> {
                        check_connection();
                    })
                    .setCancelable(false);
            builder.create().show();
            return false;
        }
        return true;
    }
}