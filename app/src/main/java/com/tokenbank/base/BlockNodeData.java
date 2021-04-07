package com.tokenbank.base;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.tokenbank.config.AppConfig;
import com.tokenbank.config.Constant;
import com.tokenbank.utils.FileUtil;
import com.tokenbank.utils.GsonUtil;
import com.tokenbank.web.ChainChangeEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来维护当前钱包所在链的节点列表
 */
public class BlockNodeData {
    private static final String TAG = "BlockNodeData";
    private List<Node> mNodeList = new ArrayList<>(); //系统默认列点列表    通过配置文件获取
    private static BlockNodeData instance;
    public static final int PUBLIC = 0; //从配置读取的公共节点  不可删除
    public static final int PRIVATE = 1; //用户添加的的个人节点  可以删除
    private Node mCurrentNode;
    private int ChainType;
    private ChainChangeEvent event = new ChainChangeEvent();
    private BlockNodeData() {
    }

    public static BlockNodeData getInstance() {
        if (instance == null) {
            instance = new BlockNodeData();
        }
        return instance;
    }

    public void init(){
        initNode();
        event.setEventName("BlockNodeDataInitOver");
        EventBus.getDefault().postSticky(event);
    }

    public void initNode(){
        if (mNodeList != null && mNodeList.size() != 0){
            mNodeList.clear();
        }
        if(WalletInfoManager.getInstance().getWalletType() != 0){
            ChainType = TBController.getInstance().getCurrentChainType();
            mNodeList = toNodeList(getNodesInfo(),ChainType);
        }
    }

    /**
     * 读取文件中的节点信息
     * @return
     */
    public String getNodesInfo(){
        String NodesInfo = FileUtil.getStringFromSp(AppConfig.getContext(), Constant._node, Constant.NodeList);
        if(NodesInfo.equals("")){
            NodesInfo = FileUtil.getConfigFile(AppConfig.getContext(), "publicNode.json");
        }
        return NodesInfo;
    }

    /**
     * 返回所有被选中的节点列表  地址下标和链索引匹配 特别的,下标为0时 为mCurrentNode
     * @return
     */
    public List<Node> getChooseNodeList(){
        List<Integer> mTypeList = TBController.getInstance().getSupportType();
        List<Node> NodeList = new ArrayList<>();
        NodeList.add(mCurrentNode);
        String NodeInfo = getNodesInfo();
        for(int i =1; i <= mTypeList.size();i++){
            List<Node> item = toNodeList(NodeInfo,i);
            for (Node node : item) {
                if (node.isSelect == 1) {
                    NodeList.add(node);
                    break;
                }
            }
        }
        return NodeList;
    }


    public List<Node> getNodeList(){
        if (mNodeList == null || mNodeList.size() == 0) {
            initNode();
            return mNodeList;
        }
        return mNodeList;
    }

    public Node getCurrentNode(){
        return this.mCurrentNode;
    }

    public void setCurrentNode(Node node){
        this.mCurrentNode = node;
    }

    public boolean addNode(Node node){
        for (Node item : mNodeList) {
            if(node.url.equals(item.url)){
                return false;
            }
        }
        mNodeList.add(node);
        saveNodeToSp();
        return true;
    }

    public void deleteNode(Node node){
        mNodeList.remove(node);
        saveNodeToSp();
    }

    private GsonUtil toJson(List<Node> nodes){
        GsonUtil data = new GsonUtil("[]");
        for (int i = 0; i < nodes.size(); i++) {
            Node item = nodes.get(i);
            GsonUtil node = new GsonUtil("{}");
            node.putString("name",item.nodeName);
            node.putString("url",item.url);
            node.putInt("isSelect",item.isSelect);
            node.putInt("isConfigNode",item.isConfigNode);
            data.put(node);
        }
        return data;
    }

    private List<Node> toNodeList(String JsonStr,int chainType) {
        List<Node> NodeList = new ArrayList<>();
        GsonUtil json = new GsonUtil(JsonStr);
        GsonUtil data = json.getArray("data","");
        if(data.isValid()){
            for (int i= 0;i<data.getLength();i++){
                GsonUtil item = data.getObject(i, "{}");
                if(item.isValid()){
                    GsonUtil nodes = item.getArray(TBController.getInstance().getDescByIndex(chainType), "");
                    if(nodes.isValid()){
                        for (int j = 0; j < nodes.getLength(); j++) {
                            GsonUtil nodeList = nodes.getObject(j, "{}");
                            Node node = new Node();
                            if(j == 0 && nodeList.getInt("isSelect",-1) == -1){
                                //未初始化时 第一位默认选中
                                node.isSelect = 1;
                                node.nodeName = nodeList.getString("name","");
                                node.url = nodeList.getString("url","");
                                node.isConfigNode = nodeList.getInt("isConfigNode",PUBLIC);
                                if(chainType == TBController.getInstance().getCurrentChainType()){
                                    mCurrentNode = node;
                                }
                            } else {
                                //从配置列表读取的节点需初始化, 逻辑和从本地读取的节点相同
                                node.isSelect = nodeList.getInt("isSelect",0);
                                if(node.isSelect == 1 && chainType == TBController.getInstance().getCurrentChainType()){
                                    mCurrentNode = node;
                                }
                                node.nodeName = nodeList.getString("name","");
                                node.url = nodeList.getString("url","");
                                node.isConfigNode = nodeList.getInt("isConfigNode",PUBLIC);
                            }
                            NodeList.add(node);
                        }
                        break;
                    }
                }
            }
        }
        return NodeList;
    }

    public void saveNodeToSp(){
        //保存的逻辑为覆盖保存, 存储时所有节点一并存储到 sp , 存储格式为配置文件的格式,方便读取.
        GsonUtil oldJson = new GsonUtil(getNodesInfo());
        GsonUtil data = oldJson.getArray("data","");

        GsonUtil json = new GsonUtil("{}");
        GsonUtil data1 = new GsonUtil("[]");
        GsonUtil item1 = new GsonUtil("{}");

        if(data.isValid()){
            for (int i= 0;i<data.getLength();i++){
                GsonUtil item = data.getObject(i, "{}");
                if(item.getArray(TBController.getInstance().getDescByIndex(ChainType)) == null){
                    data1.put(item);
                }
            }
        }
        item1.put(TBController.getInstance().getDescByIndex(ChainType),toJson(mNodeList));
        data1.put(item1);
        json.put("data",data1);
        FileUtil.putStringToSp(AppConfig.getContext(), Constant._node, Constant.NodeList, json.toString());
    }

    public static class Node implements Parcelable{
        public String nodeName;
        public String url;
        public int isSelect; // 未初始化(直接从配置表中读取)为-1  初始化后(从sp读取)  0 未选择    1 被选择
        public int isConfigNode; // 0 为系统设定节点 ，1为用户自定义节点

        public Node() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public boolean equals(Object obj){
            if (obj == null || !(obj instanceof Node)) {
                return false;
            }
            Node node = (Node) obj;
            return TextUtils.equals(node.nodeName, this.nodeName) && TextUtils.equals(node.url, this.url) &&
                    node.isSelect == this.isSelect && node.isConfigNode == this.isConfigNode;
        }

        protected Node(Parcel in) {
            this.isSelect = in.readInt();
            this.nodeName = in.readString();
            this.url = in.readString();
            this.isConfigNode = in.readInt();
        }

        public static final Parcelable.Creator<Node> CREATOR = new Parcelable.Creator<Node>() {
            @Override
            public Node createFromParcel(Parcel source) {
                return new Node(source);
            }

            @Override
            public Node[] newArray(int size) {
                return new Node[size];
            }
        };
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.nodeName);
            dest.writeString(this.url);
            dest.writeInt(this.isSelect);
            dest.writeInt(this.isConfigNode);
        }
    }
}
