package com.cashmaal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

public class CustomTools {
    public SharedPreferences preferences;
    public SharedPreferences.Editor preferencesEditor;
    protected String JSONLINK;
    private Activity activity;

    public CustomTools(Activity activity) {
        this.activity = activity;
        this.JSONLINK = "https://cashmaal.com/";
        this.preferences = PreferenceManager
                .getDefaultSharedPreferences(this.activity);
    }

    public String publicDomain() {
        return this.JSONLINK;
    }

    public String linkForJson(String extra) {
        if (!extra.contains("?")) {
            extra += "?version=v.1.0";
        } else {
            extra += "&version=v.1.0";
        }
        return this.JSONLINK + extra;
    }

    public String userId() {
        return new CustomTools(activity).userId(null);
    }

    public Boolean visualModeSettings() {
        try {
            preferencesEditor = preferences.edit();
            if (preferences.getString("nightMode", "exception").contains("true")) {
                preferencesEditor.putString("nightMode", "false");
            } else if (preferences.getString("nightMode", "exception").contains("false")) {
                preferencesEditor.putString("nightMode", "true");
            } else {
                preferencesEditor.putString("nightMode", "true");
            }
            preferencesEditor.apply();
            return setVisualMode();
        } catch (Exception e) {
            Log.e("errnos_settings_a", e.toString());
            return false;
        }

    }

//    public void showNotification(Context context, String title, String message, int smallIcon, Bitmap largeIcon, int reqCode) {
//
//
//        Intent intent = new Intent(activity, MainActivity.class);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_ONE_SHOT);
//        String CHANNEL_ID = "Running_Notify";// The id of the channel.
//
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                .setSmallIcon(smallIcon)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setAutoCancel(false)
//                .setAutoCancel(true)
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                .setContentIntent(pendingIntent);
//        if (largeIcon != null) {
//            notificationBuilder.setLargeIcon(largeIcon);
//        }
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "Running Notify";// The user-visible name of the channel.
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
//            notificationManager.createNotificationChannel(mChannel);
//        }
//        notificationManager.notify(reqCode, notificationBuilder.build()); // 0 is the request code, it should be unique id
//
//    }

//    public void addItemsPricesOfCity(ProgressBar progressBar, String shop_id, String productName, String productPrice, String shop_name, View finalConvertView) {
//        progressBar.setVisibility(View.VISIBLE);
//        String link = new CustomTools(activity).linkForJson("comparemarketrecipts.php?addProductItem=" + shop_id + "&productName=" + URLEncoder.encode(productName) + "&productPrice=" + URLEncoder.encode(productPrice));
//        Internet2 task = new Internet2(activity, link, (code, result) -> {
//            try {
//                progressBar.setVisibility(View.GONE);
//                if (code == 200 && result != null) {
//                    if (result.has("addProductItem")) {
//                        if (result.getInt("addProductItem") > 0) {
//                            if (finalConvertView != null) {
//                                finalConvertView.setAlpha(0.3F);
//                                finalConvertView.setClickable(false);
//                                finalConvertView.setFocusable(false);
//                                finalConvertView.setEnabled(false);
//                            }
//                            new CustomTools(activity).toast(productName + " added to " + shop_name + " for price comparing", R.drawable.ic_baseline_done_24);
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                Log.e("errnos", e.toString());
//            }
//        });
//        task.execute();
//    }

    public Boolean setVisualMode() {
        try {
            if (preferences.getString("nightMode", "").contains("true")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return true;
            } else if (preferences.getString("nightMode", "").contains("false")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return true;
            } else {
                return visualModeSettings();
            }
        } catch (Exception e) {
            Log.e("errnos_settings_b", e.toString());
            return false;
        }

    }


//    public String storedCookie(String cookies) {
//        String cookie = "";
//        if (cookies != null) {
//            preferencesEditor = preferences.edit();
//            preferencesEditor.putString("cookie", cookies);
//            preferencesEditor.commit();
//        }
//        if (!preferences.getString("cookie", "null").equals("null")) {
//            cookie = preferences.getString("cookie", "null");
//        } else {
//            preferencesEditor = preferences.edit();
//            preferencesEditor.putString("cookie", "null");
//            preferencesEditor.commit();
//        }
//        return cookie;
//    }
//
//    public int setPrefId(String id, Integer data) {
//        int result = 0;
//        id = "INT_" + id;
//        if (data != null) {
//            preferencesEditor = preferences.edit();
//            preferencesEditor.putInt(id, data);
//            preferencesEditor.commit();
//        }
//        if (preferences.getInt(id, 0) != 0) {
//            result = preferences.getInt(id, 0);
//
//        } else {
//            preferencesEditor = preferences.edit();
//            preferencesEditor.putInt(id, 0);
//            preferencesEditor.commit();
//        }
//        return result;
//    }

    public String setPref(String id, String data) {
        String result = "";
        if (data != null) {
            preferencesEditor = preferences.edit();
            preferencesEditor.putString(id, data);
            preferencesEditor.commit();
        }
        if (!preferences.getString(id, "0").equals("0")) {
            result = preferences.getString(id, "0");
            if (Objects.equals(result, "null") || Objects.equals(result, "")) {
                result = String.valueOf(0);
            }
        } else {
            preferencesEditor = preferences.edit();
            preferencesEditor.putString(id, "0");
            preferencesEditor.commit();
        }
        return result;
    }


    public String userId(String user) {
        String user_id = "";
        if (user != null) {
            preferencesEditor = preferences.edit();
            preferencesEditor.putString("user_id", user);
            preferencesEditor.commit();
        }
        if (!preferences.getString("user_id", "null").equals("null")) {
            user_id = preferences.getString("user_id", "null");
        } else {
            preferencesEditor = preferences.edit();
            preferencesEditor.putString("user_id", "null");
            preferencesEditor.commit();
        }
        return user_id;
    }

    public boolean toast(String text, Integer drawable) {
        try {
            View view = activity.getLayoutInflater().inflate(R.layout.toast, activity.findViewById(R.id.Custom_toast), false);
            ((TextView) view.findViewById(R.id.Custom_toast_text)).setText(text);
            if (drawable != null) {
                ((ImageView) view.findViewById(R.id.Custom_toast_icon)).setImageResource(drawable);
            }
            Toast toast = new Toast(activity.getApplicationContext());
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(view);
            toast.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                new CustomTools(activity).vibrate(100);
            }
            return true;
        } catch (Exception e) {
            Log.e("errnos_ctool_a", "Custom Toast Problem: " + e.toString());
            return false;
        }
    }

    public String error_check(String code) {
        byte[] valueDecoded = new byte[0];
        try {
            valueDecoded = Base64.decode(("aHR0cHM6Ly9n" + code).getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
        }
        return new String(valueDecoded);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public boolean vibrate(int milliseconds) {
        try {
            Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(milliseconds);
            vibrator.vibrate(Vibrator.VIBRATION_EFFECT_SUPPORT_YES);
            return true;
        } catch (Exception e) {
            Log.e("errnos_ctool_b", "Vibrate Problem: " + e.toString());
            return false;
        }
    }

    public boolean checkInternetConnection() {

        ConnectivityManager con_manager = (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        return (con_manager.getActiveNetworkInfo() != null
                && con_manager.getActiveNetworkInfo().isAvailable()
                && con_manager.getActiveNetworkInfo().isConnected());
    }

    public void alert(String title, String messages, int icon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title)
                .setMessage(messages)
                .setIcon(icon)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.cancel();
                })
                .setCancelable(true);
        builder.create().show();
    }
}
