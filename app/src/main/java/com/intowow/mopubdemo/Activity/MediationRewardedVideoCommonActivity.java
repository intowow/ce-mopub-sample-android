package com.intowow.mopubdemo.Activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.intowow.mopubdemo.R;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.mobileads.CECustomEventRewardedVideo;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideos;

import java.util.Set;

public class MediationRewardedVideoCommonActivity extends Activity implements MoPubRewardedVideoListener {

    private static final String TAG = MediationRewardedVideoCommonActivity.class.getSimpleName();

    private static final String AD_UNIT_ID = "a87e6f74a4f948928136ac71efb5801a";

    //UI
    private Button mLoadAdBtn = null;
    private Button mShowAdBtn = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_mediation_common);

        setupUI();

        MoPubRewardedVideos.initializeRewardedVideo(this);
        MoPub.onCreate(this);
        MoPubRewardedVideos.setRewardedVideoListener(this);
    }

    // MoPub rewarded video listener begin
    @Override
    public void onRewardedVideoLoadSuccess(@NonNull String adUnitId) {
        Toast.makeText(this, "onRewardedVideoLoadSuccess", Toast.LENGTH_SHORT).show();
        mShowAdBtn.setEnabled(true);
    }

    @Override
    public void onRewardedVideoLoadFailure(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
        Toast.makeText(this, "onRewardedVideoLoadFailure, error code: " + errorCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted(@NonNull String adUnitId) {
        Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoPlaybackError(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
        Toast.makeText(this, "onRewardedVideoPlaybackError, ad unit id: " + adUnitId + " error code: " + errorCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoClicked(@NonNull String adUnitId) {
        Toast.makeText(this, "onRewardedVideoClicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoClosed(@NonNull String adUnitId) {
        Toast.makeText(this, "onRewardedVideoClosed", Toast.LENGTH_SHORT).show();
        mShowAdBtn.setEnabled(false);
    }

    @Override
    public void onRewardedVideoCompleted(@NonNull Set<String> adUnitIds, @NonNull MoPubReward reward) {
        Toast.makeText(this, "onRewardedVideoCompleted, ad unit id: " + adUnitIds + " reward: " + reward.getAmount(), Toast.LENGTH_SHORT).show();
    }
    // MoPub rewarded video listener end

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
        if (!MoPubRewardedVideos.hasRewardedVideo(AD_UNIT_ID)) {
            MoPubRewardedVideos.loadRewardedVideo(AD_UNIT_ID, new CECustomEventRewardedVideo.CEGlobalMediationSettings(null, null));
        }
    }

    private void showAd() {
        if (MoPubRewardedVideos.hasRewardedVideo(AD_UNIT_ID)) {
            MoPubRewardedVideos.showRewardedVideo(AD_UNIT_ID);
        } else {
            Toast.makeText(this, "Dude, your ad's not loaded yet.", Toast.LENGTH_SHORT).show();
        }
    }
}
