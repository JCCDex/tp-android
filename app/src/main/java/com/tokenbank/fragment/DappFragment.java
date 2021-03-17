package com.tokenbank.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tokenbank.R;
import com.tokenbank.adapter.BaseListViewAdapter;
import com.tokenbank.base.BlockChainData;
import com.tokenbank.base.WalletInfoManager;
import com.tokenbank.config.AppConfig;
import com.tokenbank.config.Constant;
import com.tokenbank.dialog.MsgDialog;
import com.tokenbank.utils.FileUtil;
import com.tokenbank.utils.GsonUtil;
import com.tokenbank.utils.ViewUtil;
import com.tokenbank.web.WebActivity;

import java.util.List;

public class DappFragment extends BaseFragment implements View.OnClickListener{

    private EditText mEt_url;
    private TextView mTv_search;
    private GridView mViewGroup;
    private List<AppInfo> mAppList;
    private String searchUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return ViewUtil.inflatView(inflater, container, R.layout.fragment_dapp, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //TODO 针对浏览器进行UI开发
        init();
        initView(view);
    }

    private void init() {
        String data = FileUtil.getConfigFile(AppConfig.getContext(), "Apps.json");
        GsonUtil json = new GsonUtil(data);
        GsonUtil chains = json.getArray("data", "[]");
        int len = chains.getLength();
        for (int i = 0; i < len; i++) {
            GsonUtil item = chains.getObject(i, "{}");
            mAppList.add(getAppInfo(item));
        }
    }

    private AppInfo getAppInfo(GsonUtil item) {
        AppInfo app = new AppInfo();
        app.icinUrl = item.getString("dapp_name","");
        app.appName = item.getString("dapp_url","");
        app.appUrl = item.getString("desc","");
        app.desc = item.getString("support","");
        app.support = item.getString("icin_url","");
        return app;
    }

    private void initView(View view) {
        mEt_url = view.findViewById(R.id.et_url);
        mEt_url.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    startActivity(new Intent(getActivity(), WebActivity.class)
                            .putExtra(Constant.LOAD_URL, mEt_url.getText().toString().trim()));
                }
                return false;
            }
        });
        mTv_search = view.findViewById(R.id.tv_search);
        mTv_search.setOnClickListener(this);
        mViewGroup = (GridView)view.findViewById(R.id.grid_layout);
        GridViewAdapter mGridViewAdapter = new GridViewAdapter(getActivity());
        mViewGroup.setAdapter(mGridViewAdapter);
        mGridViewAdapter.setList(mAppList);
        mViewGroup.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppInfo appInfo = mAppList.get(position);
                if(appInfo.desc.equals(BlockChainData.getInstance().getDescByHid(WalletInfoManager.getInstance().getWalletType()))){
                    startActivity(new Intent(getActivity(), WebActivity.class).putExtra(Constant.LOAD_URL, appInfo));
                } else {
                    ViewUtil.showSysAlertDialog(getContext(), getString(R.string.toast_walletError_dapp,appInfo.desc), getString(R.string.dialog_btn_confirm));
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_search : search();break;
        }
    }


    public static DappFragment newInstance() {
        DappFragment dappFragment = new DappFragment();
        return dappFragment;
    }

    private void search() {
        if (!TextUtils.isEmpty(searchUrl)) {
            if (searchUrl.startsWith("http://") || searchUrl.startsWith("https://")) {
                if(searchUrl.equals("http://test")){
                    searchUrl = "file:///android_asset/MateMaskTestPage.html";//测试
                }
                startActivity(new Intent(getActivity(), WebActivity.class)
                        .putExtra(Constant.LOAD_URL, searchUrl));
            } else {
                new MsgDialog(getContext(), "err").show();
            }
        }
    }



    private class GridViewAdapter extends BaseListViewAdapter<AppInfo> {
        private Context context;

        public GridViewAdapter(Context ctx) {
            super(ctx);
            this.context = ctx;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.layout_view_app, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            AppInfo appInfo = mList.get(position);
            if (appInfo != null) {
                holder.nameText.setText(appInfo.appName);
                //holder.icon.setBackgroundResource();
            }
            return convertView;
        }

        private class ViewHolder {
            ImageView iconView;
            TextView nameText;
            RelativeLayout mLayoutItem;

            public ViewHolder(View view) {
                this.iconView = view.findViewById(R.id.imgView);
                this.nameText = view.findViewById(R.id.textView);
                this.mLayoutItem = view.findViewById(R.id.layout_app_item);
            }
        }

    }

    public static class AppInfo implements Parcelable {
        public String icinUrl;
        public String appName;
        public String desc;
        public String support;
        public String appUrl;
        private AppInfo(){

        }

        protected AppInfo(Parcel in) {
            icinUrl = in.readString();
            appName = in.readString();
            desc = in.readString();
            support = in.readString();
            appUrl = in.readString();
        }

        public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
            @Override
            public AppInfo createFromParcel(Parcel in) {
                return new AppInfo(in);
            }

            @Override
            public AppInfo[] newArray(int size) {
                return new AppInfo[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.icinUrl);
            dest.writeString(this.appName);
            dest.writeString(this.desc);
            dest.writeString(this.support);
            dest.writeString(this.appUrl);
        }
    }
}


