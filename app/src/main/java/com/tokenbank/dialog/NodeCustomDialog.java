package com.tokenbank.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;


import com.tokenbank.R;
import com.tokenbank.base.BlockNodeData;
import com.tokenbank.config.AppConfig;
import com.tokenbank.utils.ToastUtil;
import com.tokenbank.utils.ViewUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mob.tools.utils.Strings.getString;

/**
 * 自定义节点Dialog
 */
public class NodeCustomDialog extends BaseDialog implements View.OnClickListener {

    private static final String TAG = "NodeCustomDialog";
    private EditText mEdtNode;
    private TextView mTvErr;
    private TextView mTvCancel;
    private TextView mTvConfirm;

    public interface onConfirmOrderListener {
        void onConfirmOrder();
    }

    private onConfirmOrderListener mOnConfirmOrderListener;

    public NodeCustomDialog(@NonNull Context context, onConfirmOrderListener onConfirmOrderListener) {
        super(context, R.style.DialogStyle);
        mOnConfirmOrderListener = onConfirmOrderListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.layout_dialog_custom_node);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        getWindow().setAttributes(lp);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        initView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initView() {
        mEdtNode = findViewById(R.id.edt_node);
        mEdtNode.requestFocus();
        mTvErr = findViewById(R.id.tv_err);
        mEdtNode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && mTvErr.isShown()) {
                    mTvErr.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mTvCancel = (TextView) findViewById(R.id.tv_cancel);
        mTvCancel.setText(getContext().getString(R.string.dialog_btn_confirm));
        mTvCancel.setOnClickListener(this);
        mTvConfirm = (TextView) findViewById(R.id.tv_confirm);
        mTvCancel.setText(getContext().getString(R.string.dialog_btn_cancel));
        mTvConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mTvCancel) {
            dismiss();
        } else if (v == mTvConfirm) {
            String url = mEdtNode.getText().toString();
            if (!TextUtils.isEmpty(url) && (url.startsWith("http://") || url.startsWith("https://"))) {
                String[] ws = url.replace("http://", "").replace("https://", "").split(":");
                if (ws.length == 2) {
                    BlockNodeData.Node node = new BlockNodeData.Node();
                    node.url = url;
                    node.nodeName = "自定义节点";
                    node.position = -1;
                    node.isSelect = 0;
                    node.isConfigNode = BlockNodeData.CUSTOM;
                    if(BlockNodeData.getInstance().addCustomNode(node)){
                        mOnConfirmOrderListener.onConfirmOrder();
                    } else {
                        ToastUtil.toast(getContext(), getContext().getString(R.string.toast_exists_node));
                    }
                    dismiss();
                    return;
                }
            }
            mEdtNode.setText("");
            mTvErr.setVisibility(View.VISIBLE);
        }
    }
}
