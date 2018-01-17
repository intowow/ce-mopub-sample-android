package com.intowow.mopubdemo.Activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.intowow.mopubdemo.R;
import com.mopub.common.util.Views;
import com.mopub.mobileads.CEAdSize;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;

import java.util.HashMap;
import java.util.Map;

public class MediationCardCommonActivity extends Activity implements MoPubView.BannerAdListener {

    private static final String TAG = MediationCardCommonActivity.class.getSimpleName();

    private static final String AD_UNIT_ID = "d40a59a5ad234a5d8849809d9d2f2345";

    // UI
    private Button mLoadAdBtn = null;
    private RelativeLayout mAdView = null;

    private MoPubView mMoPubView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_mediation_common);

        setupUI();
    }

    @Override
    protected void onDestroy() {
        if (mMoPubView != null) {
            mMoPubView.destroy();
        }
        super.onDestroy();
    }

    private void setupUI() {
        mAdView = (RelativeLayout) findViewById(R.id.adView);

        mLoadAdBtn = (Button) findViewById(R.id.loadAd);
        mLoadAdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                loadAd();
            }
        });
    }

    private void loadAd() {
        if (mMoPubView != null) {
            Views.removeFromParent(mMoPubView);
            mMoPubView.destroy();
            mMoPubView = null;
        }
        mMoPubView = new MoPubView(this);
        // You can set your custom ad view size
        Map<String, Object> localExtra = new HashMap<String, Object>();
        localExtra.put(CEAdSize.CEADSIZE, CEAdSize.SMART_FULL_WIDTH);
        mMoPubView.setLocalExtras(localExtra);
        mMoPubView.setAdUnitId(AD_UNIT_ID);
        mMoPubView.setBannerAdListener(this);
        mMoPubView.setAutorefreshEnabled(false);
        mMoPubView.loadAd();
    }

    @Override
    public void onBannerLoaded(MoPubView banner) {
        mAdView.removeAllViews();
        if(banner != null) {
            //	add ad view into the Display Ad Layout
            //
            mAdView.addView(banner);
        }
        Log.d(TAG, "onBannerLoaded");
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        Log.d(TAG, "onBannerFailed error code: " + errorCode.toString());
        String errorStr = "Load Ad failed, error code: " + errorCode.toString();
        Toast.makeText(MediationCardCommonActivity.this, errorStr, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBannerClicked(MoPubView banner) {
        Log.d(TAG, "onBannerClicked");
    }

    @Override
    public void onBannerExpanded(MoPubView banner) {
        Log.d(TAG, "onBannerExpanded");
    }

    @Override
    public void onBannerCollapsed(MoPubView banner) {
        Log.d(TAG, "onBannerCollapsed");
    }
}
