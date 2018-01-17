package com.mopub.nativeads;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.intowow.sdk.Ad;
import com.intowow.sdk.AdError;
import com.intowow.sdk.AdListener;
import com.intowow.sdk.I2WAPI;
import com.intowow.sdk.NativeAd;
import com.mopub.common.Preconditions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom event native for Intowow SDK
 *
 * minimum support Intowow SDK 3.30.1
 */
public class CECustomEventNative extends CustomEventNative {

    private static final String LOG_TAG = CECustomEventNative.class.getSimpleName();
    private static final String PLACEMENT_ID_KEY = "placement_id";
    private static final String TITLE_KEY = "default_title";
    private static final String MAINTEXT_KEY = "default_mainText";
    private static final String CTATEXT_KEY = "default_CTAText";
    private static final String AUDIENCE_TAGS_KEY = "audience_tags";

    @Override
    protected void loadNativeAd(@NonNull Context context, @NonNull CustomEventNativeListener customEventNativeListener, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) {
        // Get placement
        final String placementId = getExtrasString(PLACEMENT_ID_KEY, localExtras, serverExtras);
        if (placementId == null) {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        Log.d(LOG_TAG, "Placement is: " + placementId);

        final String defaultTitle = getExtrasString(TITLE_KEY, localExtras, serverExtras);
        Log.d(LOG_TAG, "Default title is: " + defaultTitle);

        final String defaultMainText = getExtrasString(MAINTEXT_KEY, localExtras, serverExtras);
        Log.d(LOG_TAG, "Default main text is: " + defaultMainText);

        final String defaultCTA = getExtrasString(CTATEXT_KEY, localExtras, serverExtras);
        Log.d(LOG_TAG, "Default CTA is: " + defaultCTA);

        // Get audience tags
        List<String> audienceTags = getAudienceTags(localExtras, serverExtras);
        I2WAPI.setAudienceTargetingTags(context, audienceTags);

        final CECustomEventNative.CENativeAd ceNativeAd = new CECustomEventNative.CENativeAd(context, new com.intowow.sdk.NativeAd(context, placementId), customEventNativeListener);
        ceNativeAd.addExtra(TITLE_KEY, defaultTitle);
        ceNativeAd.addExtra(MAINTEXT_KEY, defaultMainText);
        ceNativeAd.addExtra(CTATEXT_KEY, defaultCTA);
        ceNativeAd.loadAd();
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

    static class CENativeAd extends BaseNativeAd implements AdListener {

        private static final int DEFAULT_TIMEOUT_MILLIS = 10000;

        private final Context mContext;
        private final com.intowow.sdk.NativeAd mNativeAd;
        private final CustomEventNativeListener mCustomEventNativeListener;

        private final Map<String, Object> mExtras;

        private NativeAd.MediaView mMediaView = null;

        CENativeAd(final Context context,
                   final com.intowow.sdk.NativeAd nativeAd,
                   final CustomEventNativeListener customEventNativeListener) {
            mContext = context;
            mNativeAd = nativeAd;
            mCustomEventNativeListener = customEventNativeListener;
            mExtras = new HashMap<String, Object>();
        }

        void loadAd() {
            mNativeAd.setAdListener(this);
            mNativeAd.loadAd(DEFAULT_TIMEOUT_MILLIS);
        }

        /**
         * Returns the String corresponding to the ad's title.
         */
        final public String getTitle() {
            return getString(mNativeAd.getAdTitle(), mExtras.get(TITLE_KEY));
        }

        /**
         * Returns the String corresponding to the ad's body text. May be null.
         */
        final public String getText() {
            return getString(mNativeAd.getAdBody(), mExtras.get(MAINTEXT_KEY));
        }

        /**
         * Returns the String url corresponding to the ad's main image. May be null.
         */
        final public String getCoverImageUrl() {
            return mNativeAd.getVideoCoverPath(mContext);
        }

        /**
         * Returns the NativeAd image corresponding to the ad's icon image. May be null.
         */
        final public NativeAd.Image getAdIcon() {
            return mNativeAd.getAdIcon();
        }

        /**
         * Returns the Call To Action String (i.e. "Download" or "Learn More") associated with this ad.
         */
        final public String getCallToAction() {
            return getString(mNativeAd.getAdCallToAction(), mExtras.get(CTATEXT_KEY));
        }

        /**
         * Returns the NativeAd size.
         */
        final public Rect getSize() {
            return mNativeAd.getSize();
        }

        private String getString(String original, Object candidate) {
            String outputStr = original;
            if (TextUtils.isEmpty(outputStr)) {
                if (candidate instanceof String && !TextUtils.isEmpty((String)candidate)) {
                    outputStr = (String)candidate;
                }
            }
            return outputStr;
        }

        @Override
        public void prepare(@NonNull View view) {
            mNativeAd.registerViewForInteraction(view);
        }

        @Override
        public void clear(@NonNull View view) {
            mNativeAd.unregisterView();
        }

        @Override
        public void destroy() {
            if (mMediaView != null) {
                mMediaView.destroy();
            }
        }

        final public Object getExtra(final String key) {
            if (!Preconditions.NoThrow.checkNotNull(key, "getExtra key is not allowed to be null")) {
                return null;
            }
            return mExtras.get(key);
        }

        final public Map<String, Object> getExtras() {
            return new HashMap<String, Object>(mExtras);
        }

        final public void addExtra(final String key, final Object value) {
            if (!Preconditions.NoThrow.checkNotNull(key, "addExtra key is not allowed to be null")) {
                return;
            }
            mExtras.put(key, value);
        }

        public void updateMediaView(final NativeAd.MediaView mediaView) {
            if (mediaView != null) {
                mediaView.setNativeAd(mNativeAd);
                mMediaView = mediaView;
            }
        }

        @Override
        public void onError(Ad ad, AdError adError) {
            Log.d(LOG_TAG, "Failed to load native ad, Error: " + (adError == null ? String.valueOf(AdError.CODE_NO_FILL_ERROR) : String.valueOf(adError.getErrorCode())));
            if (adError == null) {
                mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.UNSPECIFIED);
                return;
            }

            switch(adError.getErrorCode()) {
                case AdError.CODE_NO_FILL_ERROR:
                case AdError.CODE_REQUEST_TIMEOUT_ERROR:
                    mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_NO_FILL);
                    break;

                case AdError.CODE_INTERNAL_ERROR:
                    mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_INVALID_STATE);
                    break;

                default:
                    mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.UNSPECIFIED);
                    break;
            }
        }

        @Override
        public void onAdLoaded(Ad ad) {
            if (!mNativeAd.equals(ad)) {
                Log.d(LOG_TAG, "Native ad loaded failed");
                mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_INVALID_STATE);
                return;
            }

            Log.d(LOG_TAG, "Native ad loaded success");
            mCustomEventNativeListener.onNativeAdLoaded(CECustomEventNative.CENativeAd.this);
        }

        @Override
        public void onAdClicked(Ad ad) {
            Log.d(LOG_TAG, "Native ad clicked");
            notifyAdClicked();
        }

        @Override
        public void onAdImpression(Ad ad) {
            Log.d(LOG_TAG, "Native ad impression");
            notifyAdImpressed();
        }

        @Override
        public void onAdMute(Ad ad) {
            Log.d(LOG_TAG, "Native ad mute");
        }

        @Override
        public void onAdUnmute(Ad ad) {
            Log.d(LOG_TAG, "Native ad unmute");
        }

        @Override
        public void onVideoStart(Ad ad) {
            Log.d(LOG_TAG, "Native ad video start");
        }

        @Override
        public void onVideoEnd(Ad ad) {
            Log.d(LOG_TAG, "Native ad video end");
        }

        @Override
        public void onVideoProgress(Ad ad, int totalDuration, int currentPosition) {

        }
    }
}
