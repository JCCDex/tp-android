package com.tokenbank.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.jccdex.app.base.JCallback;
import com.android.jccdex.app.eos.EosWallet;
import com.android.jccdex.app.ethereum.EthereumWallet;
import com.android.jccdex.app.fst.FstWallet;
import com.android.jccdex.app.jingtum.JingtumWallet;
import com.android.jccdex.app.moac.MoacWallet;
import com.android.jccdex.app.util.JCCJson;
import com.tokenbank.TApplication;
import com.tokenbank.base.BlockNodeData;
import com.tokenbank.base.TBController;
import com.tokenbank.utils.LanguageUtil;
import com.tokenbank.utils.PermissionUtil;
import com.tokenbank.web.ChainChangeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;


public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(BaseActivity.this, requestCode, permissions, grantResults);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TApplication application = (TApplication) getApplication();
        application.addActivity(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TApplication application = (TApplication) getApplication();
        application.popActivity(this);
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Locale userLocale = LanguageUtil.getUserLocale(this);
        //系统语言改变了应用保持之前设置的语言
        if (userLocale != null) {
            Locale.setDefault(userLocale);
            Configuration configuration = new Configuration(newConfig);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLocale(userLocale);
                createConfigurationContext(configuration);
            } else {
                configuration.locale = userLocale;
            }
            getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String str) {
        switch (str) {
            case "EVENT_REFRESH_LANGUAGE":
                Locale locale = LanguageUtil.getUserLocale(this);
                LanguageUtil.updateLocale(this, locale);
//                recreate();//刷新界面
                refresh();
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onEvent(ChainChangeEvent chainChangeEvent) {
        String eventName = chainChangeEvent.getEventName();
        if(eventName.equals("BlockNodeDataInitOver")){
            List<BlockNodeData.Node> NodeList = BlockNodeData.getInstance().getChooseNodeList();
            if(NodeList.size()!=0){
                JingtumWallet.getInstance().init(this);
                EthereumWallet.getInstance().init(this);
                EthereumWallet.getInstance().initWeb3Provider(NodeList.get(TBController.ETH_INDEX).url);
                MoacWallet.getInstance().init(this);
                MoacWallet.getInstance().initChain3Provider(NodeList.get(TBController.MOAC_INDEX).url);
                EosWallet.getInstance().init(this);
                EosWallet.getInstance().initEosProvider("aca376f206b8fc25a6ed44dbdc66547c36c6c33e3a119ffbeaef943642f0e906", NodeList.get(TBController.EOS_INDEX).url);
                FstWallet.getInstance().init(this, NodeList.get(TBController.FST_INDEX).url, new JCallback() {
                    @Override
                    public void completion(JCCJson json) {
                        Log.d(TAG, "completion: init storm3 over");
                    }
                });
            }else {
                Log.e(TAG, "onEvent: 读取节点列表失败");
            }
        }
        if (chainChangeEvent.getEventName().equals("chainChanged")) {
            String url = BlockNodeData.getInstance().getCurrentNode().url;
            switch (TBController.getInstance().getCurrentChainType()) {
                case TBController.ETH_INDEX:
                    EthereumWallet.getInstance().init(this);
                    EthereumWallet.getInstance().initWeb3Provider(url);
                    Log.d(TAG, "completion: init web3 over");
                    break;
                case TBController.SWT_INDEX:
                    JingtumWallet.getInstance().init(this);
                    Log.d(TAG, "completion: init jingtum over");
                    break;
                case TBController.MOAC_INDEX:
                    MoacWallet.getInstance().init(this);
                    MoacWallet.getInstance().initChain3Provider(url);
                    Log.d(TAG, "completion: init moac over");
                    break;
                case TBController.EOS_INDEX:
                    EosWallet.getInstance().init(this);
                    EosWallet.getInstance().initEosProvider("aca376f206b8fc25a6ed44dbdc66547c36c6c33e3a119ffbeaef943642f0e906", url);
                    Log.d(TAG, "completion: init eos over");
                    break;
                case TBController.FST_INDEX:
                    FstWallet.getInstance().init(this, url, new JCallback() {
                        @Override
                        public void completion(JCCJson json) {
                            Log.d(TAG, "completion: init storm3 over");
                        }
                    });
                    break;
            }
            refresh();
        }
    }

    public void refresh(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
