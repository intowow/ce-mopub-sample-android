package com.intowow.mopubdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.intowow.mopubdemo.Activity.MediationCardCommonActivity;
import com.intowow.mopubdemo.Activity.MediationInterstitialCommonActivity;
import com.intowow.mopubdemo.Activity.MediationNativeCommonActivity;
import com.intowow.mopubdemo.Activity.MediationRewardedVideoCommonActivity;
import com.intowow.mopubdemo.common.CEMenu;
import com.intowow.mopubdemo.common.LayoutManager;

public class MainActivity extends Activity {

    private CEMenu mMenu = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mMenu = new CEMenu(this, LayoutManager.getInstance(this));
        mMenu.addButton(new CEMenu.CEButton(this, "Card Ad", MediationCardCommonActivity.class));
        mMenu.addButton(new CEMenu.CEButton(this, "Native Ad", MediationNativeCommonActivity.class));
        mMenu.addButton(new CEMenu.CEButton(this, "Interstitial Ad", MediationInterstitialCommonActivity.class));
        mMenu.addButton(new CEMenu.CEButton(this, "Rewarded Video Ad", MediationRewardedVideoCommonActivity.class));
        setContentView(mMenu);
    }
}
