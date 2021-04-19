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

/**
 * @ClassName DappMessageDialog
 * @Authur name
 * @Date 21-4-19
 * Description
 */
public class DappMessageDialog extends BaseDialog implements View.OnClickListener {

    private static final String TAG = "DappMessageDialog";
    private String address;
    private String message;
    private OnOrderListener mOnOrderListener;
    private TextView addressText;
    private TextView messageText;
    private TextView mTvConfirm;
    private TextView mTvReject;
    private ImageView imageView;

    public interface OnOrderListener {
        void onConfirmOrder();
        void onRejectOrder();
    }

    public DappMessageDialog(@NonNull Context context, OnOrderListener onOrderListener,String address,String message) {
        super(context, R.style.DialogStyle);
        this.address = address;
        this.message = message;
        mOnOrderListener = onOrderListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.layout_dialog_confirmorder_sign);
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
        mTvConfirm = findViewById(R.id.tv_confirm);
        mTvReject = findViewById(R.id.tv_reject);
        messageText = findViewById(R.id.tv_dapp_message);
        addressText = findViewById(R.id.tv_choose_address);
        imageView = findViewById(R.id.img_close);
        messageText.setText(message);
        addressText.setText(address);
        mTvConfirm.setOnClickListener(this);
        mTvReject.setOnClickListener(this);
        imageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == mTvConfirm) {
            if (mOnOrderListener != null) {
                mOnOrderListener.onConfirmOrder();
                dismiss();
            }
        }
        else if (view == mTvReject){
            if (mOnOrderListener != null) {
                mOnOrderListener.onRejectOrder();
            }
            dismiss();
        } else {
            dismiss();
        }
    }
}
