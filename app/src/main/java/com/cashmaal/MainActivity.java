package com.cashmaal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    CustomTools customTools;
    String TAG = "errnos";
    String url = "";
    WebView webView;
    String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
    };
    SwipeRefreshLayout swipeRefreshLayout;
    ProgressBar progressBar;
    private Activity activity;
    private LinearLayout adViewContainer;

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
        Log.e(TAG, url);
        webView.loadUrl(url);
//        swipeRefreshLayout.setOnRefreshListener(() -> {
//            webView.loadUrl(webView.getUrl());
//        });
        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setPluginState(WebSettings.PluginState.OFF);
        webViewSettings.setLoadWithOverviewMode(true);
        webViewSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webViewSettings.setUseWideViewPort(true);
        webViewSettings.setUserAgentString("Android io.jibon.cashmaal_com/v.1.0 Mozilla/5.0 (Linux; Android 10; SM-A205U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.5249.126 Mobile Safari/537.36");
        webViewSettings.setAllowFileAccess(true);
        webViewSettings.setAllowFileAccess(true);
        webViewSettings.setAllowContentAccess(true);
        webViewSettings.setSupportZoom(false);
        adViewContainer = activity.findViewById(R.id.adView1);
        String AD_UNIT_ID = "";
        if (BuildConfig.DEBUG) {
            AD_UNIT_ID = ("ca-app-pub-3940256099942544/6300978111");
        } else {
            AD_UNIT_ID = ("ca-app-pub-6695709429891253/8540260689");//my
        }
        AdView mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.FULL_BANNER);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.e("errnos", loadAdError.getMessage());
            }
        });
        mAdView.setAdUnitId(AD_UNIT_ID);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        MobileAds.initialize(this, initializationStatus -> {

        });
        adViewContainer.removeAllViews();
        adViewContainer.removeAllViewsInLayout();
        adViewContainer.addView(mAdView);
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
        });
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
}