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
import com.tokenbank.R;
import com.tokenbank.base.BlockNodeData;
import com.tokenbank.utils.NodeCheckUtil;
import com.tokenbank.dialog.NodeCustomDialog;
import com.tokenbank.utils.ToastUtil;
import com.tokenbank.utils.ViewUtil;
import com.tokenbank.view.TitleBar;

import java.math.BigDecimal;
import java.nio.file.ClosedFileSystemException;
import java.util.List;


import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 节点设置
 * 从 BlockNodeData 读取节点列表显示，存储节点选择到 BlockNodeData
 */
public class NodeSettingActivity extends BaseActivity implements View.OnClickListener, TitleBar.TitleBarClickListener{

    private static final String TAG = "NodeSettingActivity" ;
    private TitleBar mTitleBar;
    private RecyclerView mNodeRecyclerView;
    private int mSelectedItem = -1;
    private List<BlockNodeData.Node> publicNodes;
    private int SELECT = 1;
    private int NOT_SELECT = 0;
    private NodeRecordAdapter mAdapter;
    private Button mBtnAddNode;
    private static CompositeDisposable compositeDisposable;
    private final static BigDecimal PING_QUICK = new BigDecimal("60");
    private final static BigDecimal PING_ZERO = new BigDecimal("01");
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
    }

    private void checkAllNodeStatus() {
            ViewUtil.showSysAlertDialog(this, getString(R.string.dialog_all_node_invalid), getString(R.string.dialog_btn_confirm));
    }

    @Override
    public void onClick(View v) {
        new NodeCustomDialog(NodeSettingActivity.this, new NodeCustomDialog.onConfirmOrderListener() {
            @Override
            public void onConfirmOrder() {
                getPublicNode();
            }
        }).show();
    }

    @Override
    public void onLeftClick(View view) {
        saveNode();
        this.finish();
    }

    @Override
    public void onRightClick(View view) {
        saveNode();
        this.finish();
    }

    @Override
    public void onMiddleClick(View view) {
        saveNode();
        this.finish();
    }

    public static void startNodeSettingActivity(Context from) {
        Intent intent = new Intent(from, NodeSettingActivity.class);
        intent.addFlags(from instanceof BaseActivity ? 0 : Intent.FLAG_ACTIVITY_NEW_TASK);
        from.startActivity(intent);
    }

    class NodeRecordAdapter extends RecyclerView.Adapter<NodeRecordAdapter.VH>{
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
            if(item.isSelect == SELECT ){
                holder.mRadioSelected.setChecked(true);
                holder.mLayoutItem.setActivated(true);
                mSelectedItem = position;
            }
            holder.mTvNodeName.setText(item.nodeName);
            holder.mTvNodeUrl.setText(item.url);
            holder.mProgressDrawable.start();
            NodeCheckUtil.checkNode(item.url, new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }
                        @Override
                        public void onNext(String ping) {
                            holder.mTvNodePing.setText(ping + "ms");
                            BigDecimal pingBig = new BigDecimal(ping);
                            if(pingBig.compareTo(PING_ZERO) == -1){
                                holder.mLayoutItem.setClickable(false);
                                holder.mRadioSelected.setEnabled(false);
                                holder.mTvNodePing.setText("---");
                                holder.mTvNodePing.setTextColor(getResources().getColor(R.color.color_ping_low));
                            }
                            if (pingBig.compareTo(PING_QUICK) == -1 && pingBig.compareTo(PING_ZERO) == 1) {
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
                            publicNodes.get(mSelectedItem).isSelect = NOT_SELECT;
                            publicNodes.get(position).isSelect = SELECT;
                            mSelectedItem = position;
                            vh = (VH) mNodeRecyclerView.findViewHolderForLayoutPosition(mSelectedItem);
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
    }

    /**
     * 读取节点列表
     */
    private void getPublicNode() {
        publicNodes = BlockNodeData.getInstance().getNodeList();
        if(publicNodes == null || publicNodes.size() == 0){
            this.finish();
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 删除节点
     */
    private void DeleteNode(BlockNodeData.Node node) {
        if(node.isConfigNode == BlockNodeData.PUBLIC){
            ToastUtil.toast(NodeSettingActivity.this, getString(R.string.toast_ConfirmNode_delete));
        } else {
            if (node.isSelect == SELECT) {
                NodeRecordAdapter.VH vh = (NodeRecordAdapter.VH) mNodeRecyclerView.findViewHolderForLayoutPosition(mSelectedItem);
                vh.mRadioSelected.setChecked(false);
                vh.mLayoutItem.setActivated(false);
                mSelectedItem--;
                publicNodes.get(mSelectedItem).isSelect = SELECT;
            }
            BlockNodeData.getInstance().deleteNode(node);
            getPublicNode();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void saveNode(){
        BlockNodeData.getInstance().saveNodeToSp();
    }
}
