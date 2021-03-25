package com.tokenbank.base;


import com.tokenbank.wallet.EOSWalletBlockchain;
import com.tokenbank.wallet.ETHWalletBlockchain;
import com.tokenbank.wallet.JSTWalletBlockchain;
import com.tokenbank.wallet.MOACWalletBlockchain;
import com.tokenbank.wallet.SWTWalletBlockchain;
import com.tokenbank.wallet.TestWalletBlockchain;

import java.util.ArrayList;
import java.util.List;


public class TBController {

    private final static String TAG = "TBController";

    public final static int ETH_INDEX = 1;
    public final static int SWT_INDEX = 2;
    public final static int MOAC_INDEX = 3;
    public final static int EOS_INDEX = 4;
    public final static int FST_INDEX = 5;

    private BaseWalletUtil mWalletUtil;

    private BaseWalletUtil mEosWalletUtil;
    private BaseWalletUtil mEthWalletUtil;
    private BaseWalletUtil mSwtWalletUtil;
    private BaseWalletUtil mMoacWalletUtil;
    private BaseWalletUtil mFstWalletUtil;
    private TestWalletBlockchain mNullWalletUtil;

    private static TBController sInstance = new TBController();
    private List<Integer> mSupportType = new ArrayList<>();

    private TBController() {

    }

    public static TBController getInstance() {
        return sInstance;
    }

    public void init() {
        mSupportType.add(this.ETH_INDEX);
        mSupportType.add(this.SWT_INDEX);
        mSupportType.add(this.MOAC_INDEX);
        mSupportType.add(this.EOS_INDEX);
        mSupportType.add(this.FST_INDEX);
        mEosWalletUtil = new EOSWalletBlockchain();
        mEosWalletUtil.init();

        mEthWalletUtil = new ETHWalletBlockchain();
        mEthWalletUtil.init();

        mSwtWalletUtil = new SWTWalletBlockchain();
        mSwtWalletUtil.init();

        mMoacWalletUtil = new MOACWalletBlockchain();
        mMoacWalletUtil.init();

        mFstWalletUtil = new JSTWalletBlockchain();
        mFstWalletUtil.init();

        mNullWalletUtil = new TestWalletBlockchain();
    }

    public BaseWalletUtil getWalletUtil(int type) {
        if (type == this.ETH_INDEX) {
            mWalletUtil = mEthWalletUtil;
        } else if (type == this.SWT_INDEX) {
            mWalletUtil = mSwtWalletUtil;
        } else if (type == this.MOAC_INDEX) {
            mWalletUtil = mMoacWalletUtil;
        } else if (type == this.EOS_INDEX) {
            mWalletUtil = mEosWalletUtil;
        } else if (type == this.FST_INDEX) {
            mWalletUtil = mFstWalletUtil;
        } else {
            mWalletUtil = mNullWalletUtil;// do nothing
        }
        return mWalletUtil;
    }

    public List<Integer> getSupportType() {
        return mSupportType;
    }

}
