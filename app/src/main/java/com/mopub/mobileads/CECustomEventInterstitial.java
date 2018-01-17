package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.intowow.sdk.Ad;
import com.intowow.sdk.AdError;
import com.intowow.sdk.I2WAPI;
import com.intowow.sdk.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom event interstitial for Intowow SDK
 *
 * minimum support Intowow SDK 3.36
 */
public class CECustomEventInterstitial extends CustomEventInterstitial implements InterstitialAd.InterstitialAdListener {

    private static final String LOG_TAG = CECustomEventInterstitial.class.getSimpleName();
    private static final String PLACEMENT_ID_KEY = "placement_id";
    private static final String AUDIENCE_TAGS_KEY = "audience_tags";

    private static final int DEFAULT_TIMEOUT_MILLIS = 10000;

    private CustomEventInterstitialListener mInterstitialListener;

    private InterstitialAd mInterstitialAd = null;

    private boolean mAutoCloseWhenEngaged = false;

    @Override
    protected void loadInterstitial(Context context,
                                    CustomEventInterstitialListener customEventInterstitialListener,
                                    Map<String, Object> localExtras,
                                    Map<String, String> serverExtras) {
        // Error checking
        if (customEventInterstitialListener == null) {
            Log.w(LOG_TAG, "Failed to request interstitial ad, listener is null");
            return;
        }

        // The SDK requires activity context to initialize, so check that the context
        // provided by the app is an activity context before initializing.
        if (!(context instanceof Activity)) {
            // Context not an Activity context, log the reason for failure and fail the
            // initialization.
            Log.w(LOG_TAG, "Failed to request interstitial ad, context is null or not activity");
            customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        // Get placement
        final String placementId = getExtrasString(PLACEMENT_ID_KEY, localExtras, serverExtras);
        if (TextUtils.isEmpty(placementId)) {
            Log.w(LOG_TAG, "Failed to request interstitial ad, placement id is null");
            customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        Log.d(LOG_TAG, "Placement is: " + placementId);

        // Get audience tags
        List<String> audienceTags = getAudienceTags(localExtras, serverExtras);
        I2WAPI.setAudienceTargetingTags(context, audienceTags);

        if (mInterstitialAd != null) {
            mInterstitialAd.destroy();
        }

        mInterstitialListener = customEventInterstitialListener;

        mInterstitialAd = new InterstitialAd(context, placementId);

        //	you can close the interstitial ad while user engaging the ad
        //
        mInterstitialAd.setAutoCloseWhenEngaged(mAutoCloseWhenEngaged);
        mInterstitialAd.setAdListener(this);
        mInterstitialAd.loadAd(DEFAULT_TIMEOUT_MILLIS);
    }

    @Override
    protected void showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show();
        }
    }

    @Override
    protected void onInvalidate() {
        if (mInterstitialAd != null) {
            mInterstitialAd.destroy();
            mInterstitialAd = null;
        }
    }

    // Below function is AdListener callback
    @Override
    public void onError(Ad ad, AdError adError) {
        Log.w(LOG_TAG, "Failed to load interstitial ad, Error: " + (adError == null ? String.valueOf(AdError.CODE_NO_FILL_ERROR) : String.valueOf(adError.getErrorCode())));
        if (adError == null) {
            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NO_FILL);
            return;
        }

        switch (adError.getErrorCode()) {
            case AdError.CODE_NO_FILL_ERROR:
            case AdError.CODE_REQUEST_TIMEOUT_ERROR:
                mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NO_FILL);
                break;

            case AdError.CODE_INTERNAL_ERROR:
                mInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
                break;

            case AdError.CODE_NETWORK_ERROR:
                mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NO_CONNECTION);
                break;

            default:
                mInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
                break;
        }

    }

    @Override
    public void onAdLoaded(Ad ad) {
        Log.d(LOG_TAG,"Interstitial ad loaded success");
        mInterstitialListener.onInterstitialLoaded();
    }

    @Override
    public void onInterstitialDisplayed(Ad ad) {
        Log.d(LOG_TAG,"Interstitial ad displayed");
        mInterstitialListener.onInterstitialShown();
    }

    @Override
    public void onInterstitialDismissed(Ad ad) {
        Log.d(LOG_TAG,"Interstitial ad dismissed");
        mInterstitialListener.onInterstitialDismissed();
    }

    @Override
    public void onAdClicked(Ad ad) {
        Log.d(LOG_TAG,"Interstitial ad clicked");
        mInterstitialListener.onInterstitialClicked();
    }

    @Override
    public void onAdImpression(Ad ad) {
        Log.d(LOG_TAG,"Interstitial ad impression");
    }

    @Override
    public void onAdMute(Ad ad) {
        Log.d(LOG_TAG,"Interstitial ad mute");
    }

    @Override
    public void onAdUnmute(Ad ad) {
        Log.d(LOG_TAG,"Interstitial ad unmute");
    }

    @Override
    public void onVideoStart(Ad ad) {
        Log.d(LOG_TAG,"Interstitial video ad start");
    }

    @Override
    public void onVideoEnd(Ad ad) {
        Log.d(LOG_TAG,"Interstitial video ad end");
    }

    @Override
    public void onVideoProgress(Ad ad, int i, int i1) {

    }

    private String getExtrasString(final String key, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        if (serverExtras != null) {
            String value = serverExtras.get(key);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }

        if (localExtras != null) {
            Object value = localExtras.get(key);
            if (value instanceof String && !TextUtils.isEmpty((String) value)) {
                return (String) value;
            }
        }

        return null;
    }

    private Integer getExtrasInteger(String key, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        if (serverExtras != null) {
            String value = serverExtras.get(key);
            if (!TextUtils.isEmpty(value)) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Cannot parse '" + serverExtras.get(key) + "'"
                            + " in serverExtras to Integer.  Trying from localExtras instead.");
                }
            }
        }

        if (localExtras != null) {
            Object value = localExtras.get(key);
            if (value instanceof Integer) {
                return (Integer) value;
            }
        }

        return null;
    }

    private List<String> getAudienceTags(Map<String, Object> localExtras, Map<String, String> serverExtras) {
        List<String> audienceTagsList = null;
        String audienceTags = getExtrasString(AUDIENCE_TAGS_KEY, localExtras, serverExtras);
        if (audienceTags != null && !audienceTags.isEmpty()) {
            try {
                JSONArray tagArray = new JSONArray(audienceTags);
                if (tagArray != null && tagArray.length() > 0) {
                    audienceTagsList = new ArrayList<String>();
                    for (int i = 0; i < tagArray.length(); i++) {
                        audienceTagsList.add(tagArray.getString(i));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return audienceTagsList;
    }
}
