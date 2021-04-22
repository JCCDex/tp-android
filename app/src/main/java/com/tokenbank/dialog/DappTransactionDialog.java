package com.tokenbank.dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tokenbank.R;
import com.tokenbank.base.BaseWalletUtil;
import com.tokenbank.base.TBController;
import com.tokenbank.base.WCallback;
import com.tokenbank.base.WalletInfoManager;
import com.tokenbank.utils.GsonUtil;

/**
 * @ClassName DappTransactionDialog
 * @Authur name
 * @Date 21-4-20
 * Description
 */
public class DappTransactionDialog extends BaseDialog implements View.OnClickListener {

    private static final String TAG = "DappTransactionDialog";
    private ImageView mImgClose;
    private TextView mTvReceiverAddress;
    private TextView mTvSenderAddress;
    private TextView mTvGasInfo;
    private TextView mTvTokenCount;
    private TextView mTvConfirm;
    private TextView mTvReject;

    private OnOrderListener mOnConfirmOrderListener;
    private WalletInfoManager.WData mWalletData; //当前使用哪个钱包转账
    private int mBlockChain;

    private String from;
    private String to;
    private String remark;
    private String value;
    private double mGasPrice;
    private Context mContext;

    private BaseWalletUtil mWalletUtil;

    public DappTransactionDialog(@NonNull Context context,String from, String to, String value,  String remark ,OnOrderListener onConfirmOrderListener) {
        super(context, R.style.DialogStyle);
        this.mContext = context;
        this.from = from;
        this.to = to;
        this.remark = remark;
        this.value = value;

        this.mOnConfirmOrderListener = onConfirmOrderListener;
        mWalletData = WalletInfoManager.getInstance().getCurrentWallet();

        if (mWalletData == null) {
            this.dismiss();
            return;
        }

        mWalletUtil = TBController.getInstance().getWalletUtil(mWalletData.type);

        mBlockChain = WalletInfoManager.getInstance().getWalletType();
    }

    public interface OnOrderListener {
        void onConfirmOrder();
        void onRejectOrder();
    }

    @Override
    public void onClick(View view) {
        if (view == mTvConfirm) {
            if (mOnConfirmOrderListener != null) {
                mOnConfirmOrderListener.onConfirmOrder();
                dismiss();
            }
        } else if (view == mTvReject){
            if (mOnConfirmOrderListener != null) {
                mOnConfirmOrderListener.onRejectOrder();
            }
            dismiss();
        } else if(view == mImgClose ) {
            dismiss();
        } else if(view == mTvGasInfo){
            mWalletUtil.gasSetting(mContext, mGasPrice, true, new WCallback() {
                @Override
                public void onGetWResult(int ret, GsonUtil extra) {
                    if (ret == 0) {
                        String gas = extra.getString("gas", "");
                        mGasPrice = extra.getDouble("gasPrice", 0.0f);
                        mTvGasInfo.setText(gas);
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.layot_dialog_transfor_confirm);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = -1;
        lp.height = -2;
        lp.x = 0;
        lp.y = 0;
        lp.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(lp);
        initView();
    }

    public void initView(){
        mImgClose = findViewById(R.id.img_close);
        mTvReceiverAddress = findViewById(R.id.to_wallet_address);
        mTvSenderAddress = findViewById(R.id.from_wallet_address);
        mTvTokenCount = findViewById(R.id.edt_transfer_num);
        mTvGasInfo = findViewById(R.id.tv_transfer_gas);
        mTvConfirm = findViewById(R.id.tv_confirm);
        mTvReject = findViewById(R.id.tv_reject);

        mTvGasInfo.setOnClickListener(this);
        mTvConfirm.setOnClickListener(this);
        mTvReject.setOnClickListener(this);
        mImgClose.setOnClickListener(this);

        mTvReceiverAddress.setText(to);
        mTvSenderAddress.setText(from);
        Double count = 0.0;
        if(value != null && !value.equals("")){
            count=  mWalletUtil.getValue(mWalletUtil.getDefaultDecimal(),Double.valueOf(value));
        }
        mTvTokenCount.setText(count.toString()+"ETH");
        mTvGasInfo.setText("0.0002 "+remark);
    }
}
