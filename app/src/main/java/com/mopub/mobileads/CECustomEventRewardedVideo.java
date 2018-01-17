package com.mopub.mobileads;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.intowow.sdk.Ad;
import com.intowow.sdk.AdError;
import com.intowow.sdk.I2WAPI;
import com.intowow.sdk.RewardedVideoAd;
import com.mopub.common.BaseLifecycleListener;
import com.mopub.common.LifecycleListener;
import com.mopub.common.MediationSettings;
import com.mopub.common.MoPubReward;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom event rewarded video for Intowow SDK
 *
 * minimum support Intowow SDK 3.36
 */
public class CECustomEventRewardedVideo extends CustomEventRewardedVideo implements RewardedVideoAd.RewardedVideoAdListener {

    private static final String LOG_TAG = CECustomEventRewardedVideo.class.getSimpleName();
    private static final String PLACEMENT_ID_KEY = "placement_id";
    private static final String AUDIENCE_TAGS_KEY = "audience_tags";

    private static final int DEFAULT_TIMEOUT_MILLIS = 10000;

    private RewardedVideoAd mRewardedVideoAd = null;

    private boolean mAutoCloseWhenEngaged = false;
    private boolean mInitialized = false;
    private boolean mHasRewardedVideoAd = false;

    private String mThirdPartyId = "";

    private static final LifecycleListener sLifecycleListener = new BaseLifecycleListener() {

        @Override
        public void onPause(@NonNull final Activity activity) {
            super.onPause(activity);
            I2WAPI.onActivityPause(activity);
        }

        @Override
        public void onResume(@NonNull final Activity activity) {
            super.onResume(activity);
            I2WAPI.onActivityResume(activity);
        }

    };

    @Override
    protected boolean hasVideoAvailable() {
        return mHasRewardedVideoAd;
    }

    @Override
    protected void showVideo() {
        //	show rewarded video ad here
        //
        if (mRewardedVideoAd != null) {
            mRewardedVideoAd.show();
        }
    }

    @Nullable
    @Override
    protected LifecycleListener getLifecycleListener() {
        return sLifecycleListener;
    }

    @Override
    protected boolean checkAndInitializeSdk(@NonNull Activity launcherActivity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        if (mInitialized) {
            return false;
        }

        I2WAPI.init(launcherActivity, false, false);

        // Get audience tags
        List<String> audienceTags = getAudienceTags(localExtras, serverExtras);
        I2WAPI.setAudienceTargetingTags(launcherActivity, audienceTags);
        mInitialized = true;

        return true;
    }

    @Override
    protected void loadWithSdkInitialized(@NonNull Activity activity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        if (mInitialized) {
            // Error checking
            // The SDK requires activity context to initialize, so check that the context
            // provided by the app is an activity context before initializing.
            if (!(activity instanceof Activity)) {
                // Context not an Activity context, log the reason for failure and fail the
                // initialization.
                Log.w(LOG_TAG, "Failed to request rewarded video ad, context is null or not activity");
                return;
            }
            // Get placement
            final String placementId = getExtrasString(PLACEMENT_ID_KEY, localExtras, serverExtras);
            if (TextUtils.isEmpty(placementId)) {
                Log.w(LOG_TAG, "Failed to request rewarded video ad, placement id is null");
                return;
            }

            Log.d(LOG_TAG, "Placement is: " + placementId);
            mThirdPartyId = placementId;

            if (mRewardedVideoAd != null) {
                mRewardedVideoAd.destroy();
            }

            mRewardedVideoAd = new RewardedVideoAd(activity, placementId);

            //	you can close the interstitial ad while user engaging the ad
            //
            mRewardedVideoAd.setAutoCloseWhenEngaged(mAutoCloseWhenEngaged);
            mRewardedVideoAd.setAdListener(this);
            mRewardedVideoAd.loadAd(DEFAULT_TIMEOUT_MILLIS);
        }
    }

    @NonNull
    @Override
    protected String getAdNetworkId() {
        return mThirdPartyId;
    }

    @Override
    protected void onInvalidate() {
        releaseRewardedVideoAd();
        mInitialized = false;
    }

    // SDK rewarded video callback listener begin
    @Override
    public void onError(Ad ad, AdError adError) {
        Log.w(LOG_TAG, "Failed to load rewarded video ad, Error: " + (adError == null ? String.valueOf(AdError.CODE_NO_FILL_ERROR) : String.valueOf(adError.getErrorCode())));
        if (adError == null) {
            MoPubRewardedVideoManager.onRewardedVideoLoadFailure(CECustomEventRewardedVideo.class,
                                                                 mThirdPartyId,
                                                                 MoPubErrorCode.NO_FILL);
            return;
        }

        MoPubErrorCode errorCode = MoPubErrorCode.NO_FILL;
        switch (adError.getErrorCode()) {
            case AdError.CODE_INTERNAL_ERROR:
                errorCode = MoPubErrorCode.INTERNAL_ERROR;
                break;

            case AdError.CODE_NETWORK_ERROR:
                errorCode = MoPubErrorCode.NETWORK_INVALID_STATE;
                break;

            case AdError.CODE_INVALID_PLACEMENT_ERROR:
                errorCode = MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
                break;

            case AdError.CODE_NO_FILL_ERROR:
            case AdError.CODE_REQUEST_TIMEOUT_ERROR:
            default:
                break;
        }

        MoPubRewardedVideoManager.onRewardedVideoLoadFailure(
                CECustomEventRewardedVideo.class,
                mThirdPartyId,
                errorCode);
    }

    @Override
    public void onAdLoaded(Ad ad) {
        Log.d(LOG_TAG, "Rewarded video ad loaded");
        mHasRewardedVideoAd = true;
        MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(CECustomEventRewardedVideo.class, mThirdPartyId);
    }

    @Override
    public void onRewardedVideoDisplayed(Ad ad) {
        Log.d(LOG_TAG, "Rewarded video displayed");
        MoPubRewardedVideoManager.onRewardedVideoStarted(CECustomEventRewardedVideo.class, mThirdPartyId);
    }

    @Override
    public void onRewardedVideoDismissed(Ad ad) {
        Log.d(LOG_TAG, "Rewarded video dismissed");
        releaseRewardedVideoAd();
        MoPubRewardedVideoManager.onRewardedVideoClosed(
                CECustomEventRewardedVideo.class, mThirdPartyId);
    }

    @Override
    public void onAdClicked(Ad ad) {
        Log.d(LOG_TAG, "Rewarded video ad clicked");
        MoPubRewardedVideoManager.onRewardedVideoClicked(CECustomEventRewardedVideo.class, mThirdPartyId);
    }

    @Override
    public void onAdImpression(Ad ad) {
        Log.d(LOG_TAG, "Rewarded video ad impression");
    }

    @Override
    public void onAdMute(Ad ad) {
        Log.d(LOG_TAG, "Rewarded video ad mute");
    }

    @Override
    public void onAdUnmute(Ad ad) {
        Log.d(LOG_TAG, "Rewarded video unmute");
    }

    @Override
    public void onVideoStart(Ad ad) {
        Log.d(LOG_TAG, "Rewarded video start");
    }

    @Override
    public void onVideoEnd(Ad ad) {
        Log.d(LOG_TAG, "Rewarded video end");
    }

    @Override
    public void onVideoProgress(Ad ad, int totalDuration, int currentPosition) {

    }

    @Override
    public void onRewarded(Ad ad) {
        MoPubRewardedVideoManager.onRewardedVideoCompleted(CECustomEventRewardedVideo.class,
                mThirdPartyId,
                MoPubReward.success(MoPubReward.NO_REWARD_LABEL, MoPubReward.NO_REWARD_AMOUNT));
    }
    // SDK rewarded video callback listener end

    public static final class CEGlobalMediationSettings implements MediationSettings {

        @Nullable private final String mCustomId;
        @Nullable private final String mDeviceId;

        public CEGlobalMediationSettings(@Nullable String customId, @Nullable String deviceId) {
            mCustomId = customId;
            mDeviceId = deviceId;
        }

        @Nullable
        public String getCustomId() {
            return mCustomId;
        }

        @Nullable
        public String getDeviceId() {
            return mDeviceId;
        }
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

    private void releaseRewardedVideoAd() {
        if(mRewardedVideoAd != null) {
            mRewardedVideoAd.destroy();
            mRewardedVideoAd = null;
        }

        mHasRewardedVideoAd = false;
    }
}
