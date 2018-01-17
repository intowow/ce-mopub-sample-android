package com.intowow.mopubdemo.Activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.intowow.mopubdemo.R;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;

public class MediationInterstitialCommonActivity extends Activity implements MoPubInterstitial.InterstitialAdListener {

    private static final String TAG = MediationInterstitialCommonActivity.class.getSimpleName();

    private static final String AD_UNIT_ID = "4e7cb9032a62450780dc2488b3059ff8";

    private MoPubInterstitial mMoPubInterstitial = null;

    //UI
    private Button mLoadAdBtn = null;
    private Button mShowAdBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_mediation_common);

        setupUI();

        mMoPubInterstitial = new MoPubInterstitial(this, AD_UNIT_ID);
        mMoPubInterstitial.setInterstitialAdListener(this);
    }

    @Override
    protected void onDestroy() {
        if (mMoPubInterstitial != null) {
            mMoPubInterstitial.destroy();
            mMoPubInterstitial = null;
        }
        super.onDestroy();
    }

    // Below function is InterstitialAdListener begin
    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        Toast.makeText(this, "onInterstitialLoaded", Toast.LENGTH_SHORT).show();
        mShowAdBtn.setEnabled(true);
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        Toast.makeText(this, "onInterstitialFailed, error code: " + errorCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        Toast.makeText(this, "onInterstitialShown", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        Toast.makeText(this, "onInterstitialClicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        Toast.makeText(this, "onInterstitialDismissed", Toast.LENGTH_SHORT).show();
        mShowAdBtn.setEnabled(false);
    }
    // InterstitialAdListener end

    private void setupUI() {
        mLoadAdBtn = (Button) findViewById(R.id.loadAd);
        mLoadAdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                loadAd();
            }
        });

        mShowAdBtn = (Button) findViewById(R.id.showAd);
        mShowAdBtn.setVisibility(View.VISIBLE);
        mShowAdBtn.setEnabled(false);
        mShowAdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showAd();
            }
        });
    }

    private void loadAd() {
        if (!mMoPubInterstitial.isReady()) {
            mMoPubInterstitial.load();
        }
    }

    private void showAd() {
        if (mMoPubInterstitial.isReady()) {
            mMoPubInterstitial.show();
        } else {
            Toast.makeText(this, "Dude, your ad's not loaded yet.", Toast.LENGTH_SHORT).show();
        }
    }
}
