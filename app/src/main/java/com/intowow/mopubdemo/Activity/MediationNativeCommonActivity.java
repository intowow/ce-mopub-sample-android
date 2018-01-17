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
import com.mopub.nativeads.CEAdRenderer;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.ViewBinder;

public class MediationNativeCommonActivity extends Activity {

    private static final String TAG = MediationNativeCommonActivity.class.getSimpleName();

    private static final String	AD_UNIT_ID = "e63d588dab5641f9b7b59dc9b99ed6ac";

    private MoPubNative mMoPubNative = null;

    // UI
    private Button mLoadAdBtn = null;
    private RelativeLayout mAdView = null;

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
        if (mMoPubNative != null) {
            mMoPubNative.destroy();
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
        if (mMoPubNative != null) {
            mMoPubNative.destroy();
            mMoPubNative = null;
        }

        MoPubNative.MoPubNativeNetworkListener moPubNativeNetworkListener = new
                MoPubNative.MoPubNativeNetworkListener() {
                    @Override
                    public void onNativeLoad(com.mopub.nativeads.NativeAd nativeAd) {
                        mAdView.removeAllViews();
                        if (nativeAd != null) {
                            View adView = nativeAd.createAdView(getApplicationContext(), mAdView);
                            nativeAd.prepare(adView);
                            nativeAd.renderAdView(adView);
                            mAdView.addView(adView);
                        }
                        Log.d(TAG, "onNativeLoad");
                    }

                    @Override
                    public void onNativeFail(NativeErrorCode errorCode) {
                        mAdView.removeAllViews();
                        Log.d(TAG, "onNativeFail error code: " + errorCode.toString());
                        String errorStr = "Load Ad failed, error code: " + errorCode.toString();
                        Toast.makeText(MediationNativeCommonActivity.this, errorStr, Toast.LENGTH_SHORT).show();
                    }
                };

        mMoPubNative = new MoPubNative(this, AD_UNIT_ID, moPubNativeNetworkListener);

        ViewBinder viewBinder = new ViewBinder.Builder(R.layout.item_mopub_native)
                .titleId(R.id.native_title)
                .textId(R.id.native_text)
                .mainImageId(R.id.native_main_image)
                .iconImageId(R.id.native_icon_image)
                .callToActionId(R.id.native_cta)
                .privacyInformationIconImageId(R.id.native_privacy_information_icon_image)
                .build();

        mMoPubNative.registerAdRenderer(new CEAdRenderer(viewBinder));
        mMoPubNative.makeRequest();
    }

}
