package com.mopub.nativeads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.intowow.sdk.NativeAd;
import com.mopub.common.Preconditions;

import java.util.WeakHashMap;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Ad render for Intowow SDK
 *
 * minimum support Intowow SDK 3.30.1
 */
public class CEAdRenderer implements MoPubAdRenderer<CECustomEventNative.CENativeAd> {

    private static final String LOG_TAG = CEAdRenderer.class.getSimpleName();

    private static final int DEFAULT_SCREEN_WIDTH 	 = 720;
    private static final int DEFAULT_SCREEN_HEIGHT 	 = 1280;

    private final ViewBinder mViewBinder;
    private final WeakHashMap<View, CENativeViewHolder> mViewHolderMap;

    private int mScreenWidth = DEFAULT_SCREEN_WIDTH;
    private int mScreenHeight = DEFAULT_SCREEN_HEIGHT;

    public CEAdRenderer(ViewBinder viewBinder) {
        mViewBinder = viewBinder;
        mViewHolderMap = new WeakHashMap<View, CENativeViewHolder>();
    }

    @NonNull
    @Override
    public View createAdView(@NonNull Context context, @Nullable ViewGroup parent) {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);

        if(dm.widthPixels > dm.heightPixels){
            mScreenHeight = dm.widthPixels;
            mScreenWidth = dm.heightPixels;
        } else {
            mScreenWidth = dm.widthPixels;
            mScreenHeight = dm.heightPixels;
        }

        final View adView = LayoutInflater
                .from(context)
                .inflate(mViewBinder.layoutId, parent, false);

        final View mainImageView = adView.findViewById(mViewBinder.mainImageId);
        if (mainImageView == null) {
            return adView;
        }

        final ViewGroup.LayoutParams mainImageViewLayoutParams = mainImageView.getLayoutParams();
        //  TODO
        //  you can set layout width before updateMediaView() and SDK will auto adjust ad height.
        //  For example,
        //  params = new RelativeLayout.LayoutParams(720, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //
        final NativeAd.MediaView.LayoutParams mediaViewLayoutParams = new NativeAd.MediaView.LayoutParams(
                mScreenWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (mainImageViewLayoutParams instanceof ViewGroup.MarginLayoutParams) {
            final ViewGroup.MarginLayoutParams marginParams =
                    (ViewGroup.MarginLayoutParams) mainImageViewLayoutParams;
            mediaViewLayoutParams.setMargins(marginParams.leftMargin,
                    marginParams.topMargin,
                    marginParams.rightMargin,
                    marginParams.bottomMargin);
        }

        if (mainImageViewLayoutParams instanceof RelativeLayout.LayoutParams) {
            final RelativeLayout.LayoutParams mainImageViewRelativeLayoutParams =
                    (RelativeLayout.LayoutParams) mainImageViewLayoutParams;
            final int[] rules = mainImageViewRelativeLayoutParams.getRules();
            for (int i = 0; i < rules.length; i++) {
                mediaViewLayoutParams.addRule(i, rules[i]);
            }
            mediaViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            mediaViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            mediaViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            mediaViewLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            mainImageView.setVisibility(View.INVISIBLE);
        } else {
            mainImageView.setVisibility(View.GONE);
        }

        final NativeAd.MediaView mediaView = new NativeAd.MediaView(context);
        ViewGroup mainImageParent = (ViewGroup) mainImageView.getParent();
        int mainImageIndex = mainImageParent.indexOfChild(mainImageView);
        mainImageParent.addView(mediaView, mainImageIndex + 1, mediaViewLayoutParams);
        return adView;
    }

    @Override
    public void renderAdView(@NonNull View view, @NonNull CECustomEventNative.CENativeAd ceNativeAd) {
        CEAdRenderer.CENativeViewHolder ceNativeViewHolder = mViewHolderMap.get(view);
        if (ceNativeViewHolder == null) {
            ceNativeViewHolder = CEAdRenderer.CENativeViewHolder.fromViewBinder(view, mViewBinder);
            mViewHolderMap.put(view, ceNativeViewHolder);
        }

        update(ceNativeViewHolder, ceNativeAd);
        NativeRendererHelper.updateExtras(ceNativeViewHolder.getMainView(),
                mViewBinder.extras,
                ceNativeAd.getExtras());
        setViewVisibility(ceNativeViewHolder.getMainView(), VISIBLE);
    }

    @Override
    public boolean supports(@NonNull BaseNativeAd nativeAd) {
        Preconditions.checkNotNull(nativeAd);
        return nativeAd instanceof CECustomEventNative.CENativeAd;
    }

    private void update(final CEAdRenderer.CENativeViewHolder ceNativeViewHolder,
                        final CECustomEventNative.CENativeAd nativeAd) {
        final ImageView mainImageView = ceNativeViewHolder.getMainImageView();
        if (nativeAd.getTitle() != null && !nativeAd.getTitle().isEmpty()) {
            NativeRendererHelper.addTextView(ceNativeViewHolder.getTitleView(), nativeAd.getTitle());
        } else {
            setViewVisibility(ceNativeViewHolder.getTitleView(), GONE);
        }

        if (nativeAd.getText() != null && !nativeAd.getText().isEmpty()) {
            NativeRendererHelper.addTextView(ceNativeViewHolder.getTextView(), nativeAd.getText());
        } else {
            setViewVisibility(ceNativeViewHolder.getTextView(), GONE);
        }

        if (nativeAd.getCallToAction() != null && !nativeAd.getCallToAction().isEmpty()) {
            NativeRendererHelper.addTextView(ceNativeViewHolder.getCallToActionView(),
                    nativeAd.getCallToAction());
        }

        NativeAd.Image iconImage = nativeAd.getAdIcon();
        if (iconImage != null && iconImage.getUrl() != null) {
            NativeAd.downloadAndDisplayImage(iconImage, ceNativeViewHolder.getIconImageView());
        } else {
            setViewVisibility(ceNativeViewHolder.getIconImageView(), GONE);
        }

        final NativeAd.MediaView mediaView = ceNativeViewHolder.getMediaView();
        if (mediaView != null && mainImageView != null) {
            nativeAd.updateMediaView(mediaView);
            resizeMediaView(ceNativeViewHolder, nativeAd, mediaView);
            mediaView.setVisibility(View.VISIBLE);
            if (ceNativeViewHolder.isMainImageViewInRelativeView()) {
                mainImageView.setVisibility(View.INVISIBLE);
            } else {
                mainImageView.setVisibility(View.GONE);
            }
        }
    }

    private static void setViewVisibility(@Nullable final View view,
                                          final int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    //Do not set both width and height as 'wrap content'
    private void resizeMediaView(final CEAdRenderer.CENativeViewHolder ceNativeViewHolder,
                                 final CECustomEventNative.CENativeAd nativeAd,
                                 final NativeAd.MediaView mediaView) {

        int maxAdWidth = mScreenWidth;
        int maxAdHeight = mScreenHeight;
        int adWidth = nativeAd.getSize().width();
        int adHeight = nativeAd.getSize().height();

        final ViewGroup.LayoutParams mainImageViewLayoutParams = ceNativeViewHolder.getMainImageView().getLayoutParams();
        if (mainImageViewLayoutParams.width < 0 && mainImageViewLayoutParams.height > 0) {
            maxAdHeight = mainImageViewLayoutParams.height;
        } else if (mainImageViewLayoutParams.width > 0 && mainImageViewLayoutParams.height < 0) {
            maxAdWidth = mainImageViewLayoutParams.width;
        } else if (mainImageViewLayoutParams.width > 0 && mainImageViewLayoutParams.height > 0) {
            maxAdWidth = mainImageViewLayoutParams.width;
            maxAdHeight = mainImageViewLayoutParams.height;
        } else {
            Log.d(LOG_TAG, "Warning: Media view may resize as full screen ad, " +
                    "because image view's width and height are both not define");
        }

        if (mainImageViewLayoutParams instanceof ViewGroup.MarginLayoutParams) {
            final ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) mainImageViewLayoutParams;
            maxAdWidth = maxAdWidth - marginParams.leftMargin - marginParams.rightMargin;
            maxAdHeight = maxAdHeight - marginParams.topMargin - marginParams.bottomMargin;
        }

        if (adWidth > maxAdWidth || adHeight > maxAdHeight) {
            float widthRatio = (float)adWidth / maxAdWidth;
            float heightRatio = (float)adHeight / maxAdHeight;
            if (widthRatio > heightRatio) {
                mediaView.getLayoutParams().width = maxAdWidth;
                mediaView.getLayoutParams().height = (int) (adHeight / widthRatio);
            } else {
                mediaView.getLayoutParams().width = (int) (adWidth / heightRatio);
                mediaView.getLayoutParams().height = maxAdHeight;
            }
        } else {
            float widthRatio = (float)maxAdWidth / adWidth;
            float heightRatio = (float)maxAdHeight / adHeight;
            if (widthRatio > heightRatio) {
                mediaView.getLayoutParams().width = (int) (adWidth * heightRatio);
                mediaView.getLayoutParams().height = maxAdHeight;
            } else {
                mediaView.getLayoutParams().width = maxAdWidth;
                mediaView.getLayoutParams().height = (int) (adHeight * widthRatio);
            }
        }
    }

    static class CENativeViewHolder {
        private final StaticNativeViewHolder mStaticNativeViewHolder;
        private final NativeAd.MediaView mMediaView;
        private final boolean isMainImageViewInRelativeView;

        // Use fromViewBinder instead of a constructor
        private CENativeViewHolder(final StaticNativeViewHolder staticNativeViewHolder,
                                   final NativeAd.MediaView mediaView,
                                   final boolean mainImageViewInRelativeView) {
            mStaticNativeViewHolder = staticNativeViewHolder;
            mMediaView = mediaView;
            isMainImageViewInRelativeView = mainImageViewInRelativeView;
        }

        static CEAdRenderer.CENativeViewHolder fromViewBinder(final View view,
                                                              final ViewBinder viewBinder) {
            StaticNativeViewHolder staticNativeViewHolder = StaticNativeViewHolder.fromViewBinder(view, viewBinder);
            final View mainImageView = staticNativeViewHolder.mainImageView;
            boolean mainImageViewInRelativeView = false;
            NativeAd.MediaView mediaView = null;
            if (mainImageView != null) {
                final ViewGroup mainImageParent = (ViewGroup) mainImageView.getParent();
                if (mainImageParent instanceof RelativeLayout) {
                    mainImageViewInRelativeView = true;
                }
                final int mainImageIndex = mainImageParent.indexOfChild(mainImageView);
                final View viewAfterImageView = mainImageParent.getChildAt(mainImageIndex + 1);
                if (viewAfterImageView instanceof NativeAd.MediaView) {
                    mediaView = (NativeAd.MediaView) viewAfterImageView;
                }
            }
            return new CEAdRenderer.CENativeViewHolder(staticNativeViewHolder, mediaView, mainImageViewInRelativeView);
        }

        public View getMainView() {
            return mStaticNativeViewHolder.mainView;
        }

        public TextView getTitleView() {
            return mStaticNativeViewHolder.titleView;
        }

        public TextView getTextView() {
            return mStaticNativeViewHolder.textView;
        }

        public TextView getCallToActionView() {
            return mStaticNativeViewHolder.callToActionView;
        }

        public ImageView getMainImageView() {
            return mStaticNativeViewHolder.mainImageView;
        }

        public ImageView getIconImageView() {
            return mStaticNativeViewHolder.iconImageView;
        }

        public ImageView getPrivacyInformationIconImageView() {
            return mStaticNativeViewHolder.privacyInformationIconImageView;
        }

        public NativeAd.MediaView getMediaView() {
            return mMediaView;
        }

        public boolean isMainImageViewInRelativeView() {
            return isMainImageViewInRelativeView;
        }
    }

}
