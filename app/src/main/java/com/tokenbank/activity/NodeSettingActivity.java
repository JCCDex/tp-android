package com.tokenbank.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.scwang.smartrefresh.layout.internal.ProgressDrawable;
import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.PortScan;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;
import com.tokenbank.R;
import com.tokenbank.base.BlockNodeData;
import com.tokenbank.dialog.NodeCustomDialog;
import com.tokenbank.utils.ToastUtil;
import com.tokenbank.utils.ViewUtil;
import com.tokenbank.view.TitleBar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 节点设置
 * 从 BlockNodeData 读取节点列表显示，存储节点选择到 BlockNodeData
 */
public class NodeSettingActivity extends BaseActivity implements View.OnClickListener, TitleBar.TitleBarClickListener{

    private static final String TAG = "NodeSettingActivity" ;
    private TitleBar mTitleBar;
    private RecyclerView mNodeRecyclerView;
    private int mSelectedItem = -1;
    private List<BlockNodeData.Node> publicNodes = new ArrayList<>();
    private NodeRecordAdapter mAdapter;
    private Button mBtnAddNode;
    private static CompositeDisposable compositeDisposable;
    private final static BigDecimal PING_QUICK = new BigDecimal("60");
    private final static BigDecimal PING_LOW = new BigDecimal("100");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_node);
        initView();
    }

    private void initView() {
        mTitleBar = findViewById(R.id.title_bar);
        mTitleBar.setTitle(getString(R.string.title_SettingNode));
        mTitleBar.setLeftDrawable(R.drawable.ic_back);
        mTitleBar.setRightTextColor(R.color.white);
        mTitleBar.setTitleBarClickListener(this);

        mBtnAddNode = findViewById(R.id.btn_node_setting);
        mBtnAddNode.setOnClickListener(this);

        //开始设置RecyclerView
        mNodeRecyclerView=this.findViewById(R.id.nodesetting_ecycleview);
        mNodeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new NodeRecordAdapter();
        mNodeRecyclerView.setAdapter(mAdapter);
        compositeDisposable = new CompositeDisposable();
        getPublicNode();
        checkAllNodeStatus();
    }

    private void checkAllNodeStatus() {
        if(publicNodes == null || publicNodes.size() <0){
            ViewUtil.showSysAlertDialog(this, getString(R.string.dialog_all_node_invalid), getString(R.string.dialog_btn_confirm));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_node_setting :
                new NodeCustomDialog(NodeSettingActivity.this, new NodeCustomDialog.onConfirmOrderListener() {
                    @Override
                    public void onConfirmOrder() {
                        getPublicNode();
                    }
                }).show();
                break;
        }
    }

    @Override
    public void onLeftClick(View view) {
        this.finish();
    }

    @Override
    public void onRightClick(View view) {
        this.finish();
    }

    @Override
    public void onMiddleClick(View view) {
        this.finish();
    }

    public static void startNodeSettingActivity(Context from) {
        Intent intent = new Intent(from, NodeSettingActivity.class);
        intent.addFlags(from instanceof BaseActivity ? 0 : Intent.FLAG_ACTIVITY_NEW_TASK);
        from.startActivity(intent);
    }

    class NodeRecordAdapter extends RecyclerView.Adapter<NodeRecordAdapter.VH>{
        /**
         * init view
         */
        class VH extends RecyclerView.ViewHolder {
            RelativeLayout mLayoutItem;
            TextView mTvNodeUrl;
            TextView mTvNodeName;
            TextView mTvNodePing;
            ImageView mImgLoad;
            RadioButton mRadioSelected;
            ProgressDrawable mProgressDrawable;
            public VH(View itemView) {
                super(itemView);
                //设置栏目样式
                mLayoutItem = itemView.findViewById(R.id.layout_item);
                mTvNodeUrl = itemView.findViewById(R.id.tv_node_url);
                mTvNodeName = itemView.findViewById(R.id.tv_node_name);
                mTvNodePing = itemView.findViewById(R.id.tv_ping);
                mImgLoad = itemView.findViewById(R.id.img_ping);
                mRadioSelected = itemView.findViewById(R.id.radio_selected);
                mProgressDrawable = new ProgressDrawable();
                mProgressDrawable.setColor(0xff666666);
                mImgLoad.setImageDrawable(mProgressDrawable);
                mRadioSelected.setClickable(false);

                mLayoutItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        VH vh = (VH) mNodeRecyclerView.findViewHolderForLayoutPosition(mSelectedItem);
                        int position = getAdapterPosition();
                        //重复点击
                        if (position == mSelectedItem) {
                            return;
                        } else if (position != mSelectedItem && vh != null) {
                            //切换
                            vh.mRadioSelected.setChecked(false);
                            vh.mLayoutItem.setActivated(false);
                            mSelectedItem = position;
                            BlockNodeData.Node item = publicNodes.get(mSelectedItem);
                            item.position = mSelectedItem;
                            vh = (VH) mNodeRecyclerView.findViewHolderForLayoutPosition(mSelectedItem);
                            vh.mRadioSelected.setChecked(true);
                            vh.mLayoutItem.setActivated(true);
                        } else {
                            //vh == null
                            if (mSelectedItem != -1) {
                                notifyItemChanged(mSelectedItem);
                            }
                            mSelectedItem = position;
                            BlockNodeData.Node item = publicNodes.get(mSelectedItem);
                            item.position = mSelectedItem;
                            vh = (VH) mNodeRecyclerView.findViewHolderForLayoutPosition(position);
                            vh.mRadioSelected.setChecked(true);
                            vh.mLayoutItem.setActivated(true);
                        }
                    }
                });
                mLayoutItem.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //定义AlertDialog.Builder对象，当长按列表项的时候弹出确认删除对话框
                        AlertDialog.Builder builder=new AlertDialog.Builder(NodeSettingActivity.this);
                        BlockNodeData.Node item = publicNodes.get(getAdapterPosition());
                        builder.setMessage(getString(R.string.dialog_delete_node,item.url));
                        builder.setTitle(getString(R.string.dialog_title_reminder));
                        //添加AlertDialog.Builder对象的setPositiveButton()方法
                        builder.setPositiveButton(getString(R.string.dialog_btn_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DeleteNode(item);
                            }
                        });
                        //添加AlertDialog.Builder对象的setNegativeButton()方法
                        builder.setNegativeButton(getString(R.string.dialog_btn_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.create().show();
                        return true;
                    }
                });
            }
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = ViewUtil.inflatView(parent.getContext(), parent, R.layout.layout_item_node, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, @SuppressLint("RecyclerView") int position) {
            if (publicNodes == null || publicNodes.size() == 0) {
                return;
            }
            BlockNodeData.Node item = publicNodes.get(position);
            if(position == 0 && item.position == -1){
                item.position = position;
                //当前 url
                holder.mRadioSelected.setChecked(true);
                holder.mLayoutItem.setActivated(true);
                mSelectedItem = position;
            }
            if(item.position!= -1 && item.position == position){
                holder.mRadioSelected.setChecked(true);
                holder.mLayoutItem.setActivated(true);
                mSelectedItem = position;
            }
            holder.mTvNodeName.setText(item.nodeName);
            String url = item.url;
            holder.mTvNodeUrl.setText(url);

            holder.mProgressDrawable.start();
            String[] ws = url.replace("http://", "").replace("https://", "").split(":");
            if (ws.length != 2) {
                return;
            }
            String host = ws[0];
            String port = ws[1];
            Observable.create((ObservableOnSubscribe<String>) emitter -> {
                ArrayList<Integer> prots = PortScan.onAddress(host).setMethodTCP().setPort(Integer.valueOf(port)).doScan();
                if (prots != null && prots.size() == 1) {
                    Ping ping = Ping.onAddress(host);
                    ping.setTimeOutMillis(1000);
                    ping.setTimes(5);
                    ping.doPing(new Ping.PingListener() {
                        @Override
                        public void onResult(PingResult pingResult) {
                        }

                        @Override
                        public void onFinished(PingStats pingStats) {
                            String ping = String.format("%.2f", pingStats.getAverageTimeTaken());
                            emitter.onNext(ping);
                            emitter.onComplete();
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
                } else {
                    if (!emitter.isDisposed()) {
                        emitter.onError(new Throwable());
                    }
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
                @Override
                public void onSubscribe(Disposable d) {
                    compositeDisposable.add(d);
                }
                @Override
                public void onNext(String ping) {
                    holder.mTvNodePing.setText(ping + "ms");
                    BigDecimal pingBig = new BigDecimal(ping);
                    if (pingBig.compareTo(PING_QUICK) == -1) {
                        holder.mTvNodePing.setTextColor(getResources().getColor(R.color.color_ping_quick));
                    } else if (pingBig.compareTo(PING_LOW) == -1) {
                        holder.mTvNodePing.setTextColor(getResources().getColor(R.color.color_ping_normal));
                    } else {
                        holder.mTvNodePing.setTextColor(getResources().getColor(R.color.color_ping_low));
                    }
                    holder.mTvNodePing.setVisibility(View.VISIBLE);
                    holder.mImgLoad.setVisibility(View.GONE);
                    holder.mProgressDrawable.stop();
                }
                @Override
                public void onError(Throwable e) {
                    holder.mLayoutItem.setClickable(false);
                    holder.mRadioSelected.setEnabled(false);
                    holder.mTvNodePing.setText("---");
                    holder.mTvNodePing.setTextColor(getResources().getColor(R.color.color_ping_low));
                    holder.mTvNodePing.setVisibility(View.VISIBLE);
                    holder.mImgLoad.setVisibility(View.GONE);
                    holder.mProgressDrawable.stop();
                    //最后一条也错误时，有可能全部节点不可用
                    if(getItemCount() == position +1){
                        if(!isFinishing()){
                            checkAllNodeStatus();
                        }
                    }
                }

                @Override
                public void onComplete() {

                }
            });
        }
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return publicNodes.size();
        }
    }

    /**
     * 从配置文件读取 默认节点列表
     */
    private void getPublicNode() {
        publicNodes.clear();
        List<BlockNodeData.Node> list = BlockNodeData.getInstance().getPublicNodeList();
        if(list != null && list.size() != 0){
            publicNodes.addAll(list);
            getCustomNode();
        } else {
            this.finish();
        }
    }
    /**
     * 从本地读取用户节点列表
     */
    private void getCustomNode() {
        List<BlockNodeData.Node> list = BlockNodeData.getInstance().getCustomNodeList();
        if(list != null && list.size() != 0){
            publicNodes.addAll(list);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        } else {
            return;
        }
    }

    /**
     * 删除节点
     */
    private void DeleteNode(BlockNodeData.Node node) {
        if(node.isConfigNode == BlockNodeData.PUBLIC){
            ToastUtil.toast(NodeSettingActivity.this, getString(R.string.toast_ConfirmNode_delete));
        } else {
            BlockNodeData.getInstance().deleteCustomNode(node);
            getPublicNode();
            mAdapter.notifyDataSetChanged();
        }
    }
}
