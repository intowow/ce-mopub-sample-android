package com.mopub.mobileads;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.intowow.sdk.Ad;
import com.intowow.sdk.AdError;
import com.intowow.sdk.AdListener;
import com.intowow.sdk.DisplayAd;
import com.intowow.sdk.I2WAPI;
import com.intowow.sdk.RequestInfo;
import com.mopub.common.DataKeys;
import com.mopub.common.util.Views;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom event card for Intowow SDK
 *
 * minimum support Intowow SDK 3.30.1
 */
public class CECustomEventCard extends CustomEventBanner implements AdListener {

    private static final String LOG_TAG = CECustomEventCard.class.getSimpleName();
    private static final String PLACEMENT_ID_KEY = "placement_id";
    private static final String AUDIENCE_TAGS_KEY = "audience_tags";

    private static final int DEFAULT_TIMEOUT_MILLIS = 10000;

    private CustomEventBannerListener mBannerListener = null;

    private DisplayAd mDisplayAd = null;
    private Context mContext = null;
    private Map<String, Object> mLocalExtras = null;

    @Override
    protected void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        // Error checking
        if(context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (customEventBannerListener == null) {
            throw new IllegalArgumentException("CustomEventBannerListener cannot be null");
        }

        mContext = context;
        mBannerListener = customEventBannerListener;
        mLocalExtras = localExtras;

        // Get placement
        final String placementId = getExtrasString(PLACEMENT_ID_KEY, localExtras, serverExtras);
        if (placementId == null) {
            Log.w(LOG_TAG, "Placement is null");
            customEventBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        Log.d(LOG_TAG, "Placement is: " + placementId);

        //Check custom ad width & height
        if (localExtras.containsKey(CEAdSize.CEADSIZE)) {
            CEAdSize ceAdSize = (CEAdSize) localExtras.get(CEAdSize.CEADSIZE);
            int customWidth = ceAdSize.getWidthInPixels(mContext);
            int customHeight = ceAdSize.getHeightInPixels(mContext);
            if (customWidth == 0 || customHeight == 0) {
                Log.w(LOG_TAG, "Ad size invalid, width: " + customWidth + " height: " + customHeight);
                customEventBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
                return;
            }
        }

        // Get audience tags
        List<String> audienceTags = getAudienceTags(localExtras, serverExtras);
        I2WAPI.setAudienceTargetingTags(context, audienceTags);

        if(mDisplayAd != null) {
            mDisplayAd.destroy();
        }

        mDisplayAd = new DisplayAd(context);
        mDisplayAd.setAdListener(this);
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setPlacement(placementId);
        requestInfo.setTimeout(DEFAULT_TIMEOUT_MILLIS);
        mDisplayAd.loadAd(requestInfo);
    }

    @Override
    protected void onInvalidate() {
        if (mDisplayAd != null) {
            Views.removeFromParent(mDisplayAd.getView());
            mDisplayAd.destroy();
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

    @Override
    public void onError(Ad ad, AdError adError) {
        Log.d(LOG_TAG, "Failed to load banner ad, Error: " + (adError == null ? String.valueOf(AdError.CODE_NO_FILL_ERROR) : String.valueOf(adError.getErrorCode())));
        if (adError == null) {
            mBannerListener.onBannerFailed(MoPubErrorCode.UNSPECIFIED);
            return;
        }

        switch (adError.getErrorCode()) {
            case AdError.CODE_NO_FILL_ERROR:
            case AdError.CODE_REQUEST_TIMEOUT_ERROR:
                mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_NO_FILL);
                break;

            case AdError.CODE_INTERNAL_ERROR:
                mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
                break;

            default:
                mBannerListener.onBannerFailed(MoPubErrorCode.UNSPECIFIED);
                break;
        }
    }

    @Override
    public void onAdLoaded(Ad ad) {
        if (ad != mDisplayAd) {
            Log.d(LOG_TAG, "Card ad loaded failed");
            mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
            return;
        }

        View adView = getAdView(mContext, mLocalExtras, mDisplayAd);
        if (adView != null) {
            Log.d(LOG_TAG, "Card ad loaded success");
            mBannerListener.onBannerLoaded(adView);
        } else {
            Log.d(LOG_TAG, "Card ad loaded failed");
            mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_NO_FILL);
        }
    }

    @Override
    public void onAdClicked(Ad ad) {
        Log.d(LOG_TAG, "Card ad clicked");
        mBannerListener.onBannerClicked();
    }

    @Override
    public void onAdImpression(Ad ad) {
        Log.d(LOG_TAG, "Card ad impression");
    }

    @Override
    public void onAdMute(Ad ad) {
        Log.d(LOG_TAG, "Card ad mute");
    }

    @Override
    public void onAdUnmute(Ad ad) {
        Log.d(LOG_TAG, "Card ad unmute");
    }

    @Override
    public void onVideoStart(Ad ad) {
        Log.d(LOG_TAG, "Card ad video start");
    }

    @Override
    public void onVideoEnd(Ad ad) {
        Log.d(LOG_TAG, "Card ad video end");
    }

    @Override
    public void onVideoProgress(Ad ad, int totoalDuration, int currentPosition) {

    }

    private View getAdView(final Context context,
                           final Map<String, Object> localExtras,
                           final DisplayAd displayAd) {
        int screenWidth;
        int screenHeight;
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);

        if (dm.widthPixels > dm.heightPixels) {
            screenHeight = dm.widthPixels;
            screenWidth = dm.heightPixels;
        } else {
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;
        }

        int requiredWidthInPixels = Integer.MIN_VALUE;
        int requiredHeightInPixels = Integer.MIN_VALUE;

        if (localExtras.containsKey(CEAdSize.CEADSIZE)) {
            CEAdSize ceAdSize = (CEAdSize) localExtras.get(CEAdSize.CEADSIZE);
            requiredWidthInPixels = ceAdSize.getWidthInPixels(mContext);
            requiredHeightInPixels = ceAdSize.getHeightInPixels(mContext);
        }

        if ((requiredWidthInPixels == Integer.MIN_VALUE && requiredHeightInPixels == Integer.MIN_VALUE) ||
                (requiredWidthInPixels == CEAdSize.AUTO_WIDTH && requiredHeightInPixels == CEAdSize.AUTO_HEIGHT)) {
            requiredWidthInPixels = Math.round(getExtrasInteger(DataKeys.AD_WIDTH, localExtras, null) * dm.density);
            requiredHeightInPixels = Math.round(getExtrasInteger(DataKeys.AD_HEIGHT, localExtras, null) * dm.density);
        } else {
            if (requiredWidthInPixels > screenWidth) {
                requiredWidthInPixels = screenWidth;
            }
            if (requiredHeightInPixels > screenHeight) {
                requiredHeightInPixels = screenHeight;
            }
            if (requiredWidthInPixels == CEAdSize.AUTO_WIDTH) {
                requiredWidthInPixels = (int) (((float)requiredHeightInPixels / screenHeight) * screenWidth);
            }
        }

        int maxAdWidth = requiredWidthInPixels < screenWidth ? requiredWidthInPixels : screenWidth;
        int maxAdHeight = requiredHeightInPixels < screenHeight ? requiredHeightInPixels : screenHeight;
        int displayAdWidth = displayAd.getSize().width();
        int displayAdHeight = displayAd.getSize().height();
        int newAdWidth;
        int newAdHeight;

        if (displayAdWidth > maxAdWidth || displayAdHeight > maxAdHeight) {
            float widthRatio = (float)displayAdWidth / maxAdWidth;
            float heightRatio = (float)displayAdHeight / maxAdHeight;
            if (widthRatio > heightRatio) {
                newAdWidth = maxAdWidth;
                newAdHeight = (int) (displayAdHeight / widthRatio);
            } else {
                newAdWidth = (int) (displayAdWidth / heightRatio);
                newAdHeight = maxAdHeight;
            }
        } else {
            float widthRatio = (float)maxAdWidth / displayAdWidth;
            float heightRatio = (float)maxAdHeight / displayAdHeight;
            if (widthRatio > heightRatio) {
                newAdWidth = (int) (displayAdWidth * heightRatio);
                newAdHeight = maxAdHeight;
            } else {
                newAdWidth = maxAdWidth;
                newAdHeight = (int) (displayAdHeight * widthRatio);
            }
        }

        displayAd.resize(new com.intowow.sdk.CEAdSize(context, newAdWidth, newAdHeight));

        View adView = displayAd.getView();
        ViewGroup.LayoutParams adViewLayoutParams = adView.getLayoutParams();
        RelativeLayout.LayoutParams adViewRelativeLayoutParams = (RelativeLayout.LayoutParams) adViewLayoutParams;
        adViewRelativeLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        adView.setLayoutParams(adViewRelativeLayoutParams);

        RelativeLayout wrapperAdView = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(maxAdWidth, maxAdHeight);
        wrapperAdView.setLayoutParams(params);
        wrapperAdView.setBackgroundColor(Color.BLACK);
        wrapperAdView.addView(adView);

        RelativeLayout customAdView = new RelativeLayout(mContext);
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        customAdView.setLayoutParams(params);
        customAdView.addView(wrapperAdView);

        return customAdView;
    }
}
