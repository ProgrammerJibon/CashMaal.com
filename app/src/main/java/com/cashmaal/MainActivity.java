package com.cashmaal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    CustomTools customTools;
    String TAG = "errnos";
    String url = "";
    WebView webView;
    //
    private static final String FILE_CHOOSER_TAG = MainActivity.class.getSimpleName();
    private static final int FILE_CHOOSER_RESULT_CODE = 1;
    String[] permissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.USE_FINGERPRINT
    };
    //
    SwipeRefreshLayout swipeRefreshLayout;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    // the same for Android 5.0 methods only
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    ProgressBar progressBar;
    private Activity activity;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        customTools = new CustomTools(activity);
        url = customTools.publicDomain();
        // find views
        webView = activity.findViewById(R.id.webView);
//        swipeRefreshLayout = activity.findViewById(R.id.main_activity_swiperef);
        progressBar = activity.findViewById(R.id.main_activity_progressbar);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("URL") != null) {
                url = extras.getString("URL");
            }
        }
        webView.loadUrl(url);
//        swipeRefreshLayout.setOnRefreshListener(() -> {
//            webView.loadUrl(webView.getUrl());
//        });
        Log.e(TAG, "Android com.cashmaal:" + BuildConfig.VERSION_NAME + " Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MANUFACTURER + " " + Build.MODEL + " " + Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME) + ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.5249.126 Mobile Safari/537.36");
        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webViewSettings.setUseWideViewPort(true);
        webViewSettings.setUserAgentString("Android com.cashmaal:" + Build.VERSION.RELEASE + " Mozilla/5.0 (Linux; Android 10; " + Build.MANUFACTURER + " " + Build.MODEL + " " + Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME) + ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.5249.126 Mobile Safari/537.36");
        webViewSettings.setAllowFileAccess(false);
        webViewSettings.setAllowContentAccess(false);
        webViewSettings.setSupportZoom(false);
        webViewSettings.setSupportMultipleWindows(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webViewSettings.setSafeBrowsingEnabled(true);
        }
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.e(TAG, "x - > " + url);
                if (!customTools.checkInternetConnection()) {
                    webView.setVisibility(View.GONE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Connection error!")
                            .setMessage("Unable to connect to the internet...")
                            .setPositiveButton("Retry", (dialog, which) -> {
                                webView.reload();
                                webView.setVisibility(View.VISIBLE);
                            })
                            .setCancelable(false);
                    builder.create().show();
                }
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int errorCode = error.getErrorCode();
                    if (errorCode == WebViewClient.ERROR_UNSUPPORTED_SCHEME) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                        if (webView.canGoBack()) {
                            webView.goBack();
                        } else {
                            activity.finish();
                        }
                    }
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e(TAG, url);
                if (Objects.equals(url, "https://www.cashmaal.com/?i=sign-in")
                        || Objects.equals(url, "https://www.cashmaal.net/sign-in")
                        || Objects.equals(url, "https://www.cashmaal.net/sign-in?i=sign-in")
                        || Objects.equals(url, "https://www.cashmaal.com/sign-in?i=sign-in")) {
                    activity.startActivity(new Intent(activity, LoginView.class));
                    activity.finish();
                    return true;
                }/*else if((!url.contains("cashmaal.net") && !url.contains("cashmaal.com"))){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    if (webView.canGoBack()){
                        webView.goBack();
                    }else{
                        activity.finish();
                    }
                    return true;
                }*/
                return false;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                WebView.HitTestResult result = view.getHitTestResult();
                String data = result.getExtra();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
                activity.startActivity(browserIntent);
                return false;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress > 99) {
                    progressBar.setVisibility(View.GONE);
//                    swipeRefreshLayout.setRefreshing(false);
                } else if (newProgress > 15) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(false);
                    progressBar.setProgress(newProgress);
                } else if (newProgress == 0) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    progressBar.setProgress(newProgress);
                }
            }

            // file uploader started
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                if (hasPermission()) {
                    return showWebViewFilePicker(webView, filePathCallback, fileChooserParams);
                } else {
                    ActivityCompat.requestPermissions(activity, permissions, 104);
                    return false;
                }
            }


        });
    }

    private File createImageFile() throws IOException {
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DirectoryNameHere");
        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        }
        imageStorageDir = new File(imageStorageDir + File.separator + "IMG_" + System.currentTimeMillis() + ".jpg");
        return imageStorageDir;
    }

    private boolean showWebViewFilePicker(
            WebView webView, ValueCallback<Uri[]> filePathCallback,
            WebChromeClient.FileChooserParams fileChooserParams) {
        String acceptType = fileChooserParams.getAcceptTypes()[0];
        if (Objects.equals(acceptType, "")) {
            acceptType = "*/*";
        }
        Log.e(TAG, acceptType);
        if (mFilePathCallback != null) {
            mFilePathCallback.onReceiveValue(null);
        }
        mFilePathCallback = filePathCallback;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
            } catch (IOException ex) {
                Log.e(TAG, "Unable to create Image File", ex);
            }
            if (photoFile != null) {
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
            } else {
                takePictureIntent = null;
            }
        }
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType(acceptType);
        Intent[] intentArray;
        if (takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }
        String[] acceptTypeSplit = acceptType.split("/");

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Please choose " + acceptTypeSplit[0] + " - " + getApplicationInfo().loadLabel(getPackageManager()).toString());
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            Intent startHomeScreen = new Intent(Intent.ACTION_MAIN);
            startHomeScreen.addCategory(Intent.CATEGORY_HOME);
            startActivity(startHomeScreen);
//            moveTaskToBack(true);
        }
    }

    public boolean hasPermission() {
        if (activity != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean results = true, need_settings = false;
        int i = 0;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])) {
                    need_settings = true;
                }
                results = false;
                break;
            }
            i++;
        }
        if (need_settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Permission denied!");
            builder.setMessage("App need permission");
            builder.setCancelable(true);
            builder.setPositiveButton("Open Settings", (dialogInterface, i1) -> {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            });
            builder.setNegativeButton("Cancel", (dialogInterface, i1) -> {
                dialogInterface.cancel();
            });
            builder.show();
        } else if (results) {
            Toast.makeText(activity, "Permission Granted, please try to pick again...", Toast.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Permission needed!");
            builder.setMessage("App need permission");
            builder.setCancelable(true);
            builder.setPositiveButton("Open Settings", (dialogInterface, i1) -> {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            });
            builder.setNegativeButton("Cancel", (dialogInterface, i1) -> {
                dialogInterface.cancel();
            });
            builder.show();
        }
    }

    // return here when file selected from camera or from SD Card
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null || data.getData() == null) {
                if (mCameraPhotoPath != null) {
                    results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                }
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }
        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
    }


}