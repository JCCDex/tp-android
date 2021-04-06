package com.tokenbank.base;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.tokenbank.config.AppConfig;
import com.tokenbank.config.Constant;
import com.tokenbank.utils.FileUtil;
import com.tokenbank.utils.GsonUtil;
import com.tokenbank.web.ChainChangeEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来维护节点列表和检测节点的连接性
 */
public class BlockNodeData {
    private static final String TAG = "BlockNodeData";
    private List<Node> mNodeList = new ArrayList<>(); //系统默认列点列表    通过配置文件获取
    private List<Node> LocalNodeList = new ArrayList<>(); //本地节点列表 包含用户节点    通过sp存储获取
    private static BlockNodeData instance;
    public static final int PUBLIC = 0; //从配置读取的公共节点  不可删除
    public static final int PRIVATE = 1; //用户添加的的个人节点  可以删除
    private String desc;
    private Node mCurrentNode;
    private BlockNodeData() {
    }

    public static BlockNodeData getInstance() {
        if (instance == null) {
            instance = new BlockNodeData();
        }
        return instance;
    }

    public void init(){
        if(WalletInfoManager.getInstance().getWalletType() != 0){
            desc = BlockChainData.getInstance().getDescByHid(WalletInfoManager.getInstance().getWalletType());
            String customNodes = FileUtil.getStringFromSp(AppConfig.getContext(), Constant._node, Constant.NodeList);
            LocalNodeList = toNodeList(customNodes,PRIVATE);
            // 无本地存储时从配置文件读取
            if(LocalNodeList == null || LocalNodeList.size() == 0){
                String publicNodes = FileUtil.getConfigFile(AppConfig.getContext(), "publicNode.json");
                mNodeList = toNodeList(publicNodes,PUBLIC);
            } else {
                mNodeList.addAll(LocalNodeList);
            }
        }
    }

    public List<Node> getNodeList(){
        if (mNodeList == null || mNodeList.size() == 0) {
            init();
            return null;
        }
        return mNodeList;
    }

    public Node getNode(){
        for (Node node : mNodeList) {
            if(node.isSelect == 1){
                return node;
            }
        }
        return null;
    }

    public Node getCurrentNode(){
        return this.mCurrentNode;
    }

    public void setCurrentNode(Node node){
        if (!(mCurrentNode.url.equals(node.url))){
            ChainChangeEvent event = new ChainChangeEvent();
            event.setEventName("chainChanged");
            EventBus.getDefault().post(event);
        }
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

    private List<Node> toNodeList(String JsonStr, int type) {
        List<Node> NodeList = new ArrayList<>();
        GsonUtil json = new GsonUtil(JsonStr);
        GsonUtil data = json.getArray("data","");
        if(data.isValid()){
            for (int i= 0;i<data.getLength();i++){
                GsonUtil item = data.getObject(i, "{}");
                if(item.isValid()){
                    GsonUtil nodes = item.getArray(desc, "");
                    if(nodes.isValid()){
                        for (int j = 0; j < nodes.getLength(); j++) {
                            GsonUtil nodeList = nodes.getObject(j, "{}");
                            Node node = new Node();
                            if(type == PUBLIC && i == 0 && j == 0){
                                //从配置读取时默认第一位可选
                                node.isSelect = 1;
                                node.nodeName = nodeList.getString("name","");
                                node.url = nodeList.getString("url","");
                                node.isConfigNode = nodeList.getInt("isConfigNode",PUBLIC);
                                mCurrentNode = node;
                            } else {
                                //从配置列表读取的节点需初始化, 逻辑和从本地读取的节点相同
                                node.isSelect = nodeList.getInt("isSelect",0);
                                if(node.isSelect == 1){
                                    mCurrentNode = node;
                                }
                                node.nodeName = nodeList.getString("name","");
                                node.url = nodeList.getString("url","");
                                node.isConfigNode = nodeList.getInt("isConfigNode",PUBLIC);
                            }
                            NodeList.add(node);
                        }
                    }
                }
            }
        }
        return NodeList;
    }

    public void saveNodeToSp(){
        //保存的逻辑为覆盖保存 存储时所有节点一并存储到 sp
        GsonUtil json = new GsonUtil("{}");
        GsonUtil data = new GsonUtil("[]");
        GsonUtil item = new GsonUtil("{}");
        item.put(desc,toJson(mNodeList));
        data.put(item);
        json.put("data",data);
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
