package com.tokenbank.base;


import java.util.ArrayList;
import java.util.List;


public class TBController {

    private final static String TAG = "TBController";

    public final static int ETH_INDEX = 1;
    public final static int SWT_INDEX = 2;
    public final static int MOAC_INDEX = 3;
    public final static int EOS_INDEX = 4;


    private BaseWalletUtil mWalletUtil;

    private BaseWalletUtil mEosWalletUtil;
    private BaseWalletUtil mEthWalletUtil;
    private BaseWalletUtil mSwtWalletUtil;
    private BaseWalletUtil mMoacWalletUtil;
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

        mEosWalletUtil = new EOSWalletBlockchain();
        mEosWalletUtil.init();

        mEthWalletUtil = new ETHWalletBlockchain();
        mEthWalletUtil.init();

        mSwtWalletUtil = new SWTWalletBlockchain();
        mSwtWalletUtil.init();

        mMoacWalletUtil = new MOACWalletBlockchain();
        mMoacWalletUtil.init();

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
        } else {
            mWalletUtil = mNullWalletUtil;// do nothing
        }
        return mWalletUtil;
    }

    public List<Integer> getSupportType() {
        return mSupportType;
    }

}
