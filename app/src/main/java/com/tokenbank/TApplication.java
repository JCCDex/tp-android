package com.tokenbank;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import com.tokenbank.activity.BaseActivity;
import com.tokenbank.base.BlockChainData;
import com.tokenbank.base.BlockNodeData;
import com.tokenbank.base.WalletInfoManager;
import com.tokenbank.base.TBController;
import com.tokenbank.config.AppConfig;
import com.tokenbank.utils.LanguageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class TApplication extends Application {

    private final static String TAG = "TApplication";
    private List<BaseActivity> mActivities = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        AppConfig.init(this);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        TBController.getInstance().init();
        BlockChainData.getInstance().init();
        WalletInfoManager.getInstance().init();
        BlockNodeData.getInstance().init();
    }

    public void addActivity(BaseActivity activity) {
        mActivities.add(activity);
        AppConfig.setCurActivity(activity);
    }

    public void popActivity(BaseActivity activity) {
        mActivities.remove(activity);
        if (!activity.isFinishing()) {
            activity.finish();
        }
    }

    public void clearActivity() {
        for (BaseActivity activity : mActivities
        ) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Locale locale = LanguageUtil.getUserLocale(base);
            super.attachBaseContext(LanguageUtil.updateLocale(base, locale));
        } else {
            super.attachBaseContext(base);
        }
    }
}
