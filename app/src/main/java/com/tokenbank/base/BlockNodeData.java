package com.tokenbank.base;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.tokenbank.config.AppConfig;
import com.tokenbank.config.Constant;
import com.tokenbank.utils.FileUtil;
import com.tokenbank.utils.GsonUtil;

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
    public static final int PUBLIC = 0;
    public static final int LOCAL = 1;
    private String desc ;
    private BlockNodeData() {
    }

    public static BlockNodeData getInstance() {
        if (instance == null) {
            instance = new BlockNodeData();
        }
        return instance;
    }

    public void init(){
        //初始化时根据当前钱包类型切换节点列表，每次切换钱包时重新初始化
        desc = BlockChainData.getInstance().getDescByHid(WalletInfoManager.getInstance().getWalletType());
        String customNodes = FileUtil.getStringFromSp(AppConfig.getContext(), Constant._node, Constant.NodeList);
        LocalNodeList = toNodeList(customNodes);
        // 无本地存储时从配置文件读取
        if(LocalNodeList == null || LocalNodeList.size() == 0){
            String publicNodes = FileUtil.getConfigFile(AppConfig.getContext(), "publicNode.json");
            mNodeList = toNodeList(publicNodes);
        } else {
            mNodeList.addAll(LocalNodeList);
        }
    }

    public List<Node> getNodeList(){
        if (mNodeList == null || mNodeList.size() == 0) {
            Log.e(TAG, "getPublicNodeList: 读取配置文件失败");
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

    private List<Node> toNodeList(String JsonStr) {
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
                            GsonUtil nodeLit = nodes.getObject(j, "{}");
                            Node node = new Node();
                            node.isSelect = nodeLit.getInt("isSelect",-1);
                            node.nodeName = nodeLit.getString("name","");
                            node.url = nodeLit.getString("url","");
                            node.isConfigNode = nodeLit.getInt("isConfigNode",PUBLIC);
                            NodeList.add(node);
                        }
                    }
                }
            }
        }
        return NodeList;
    }

    public void saveNodeToSp(){
        //保存的逻辑为覆盖保存
        GsonUtil json = new GsonUtil("{}");
        GsonUtil data = new GsonUtil("[]");
        GsonUtil item = new GsonUtil("{}");
        item.put(desc,toJson(mNodeList));
        data.put(item);
        json.put("data",data);
        Log.d(TAG, "saveNodeToSp: "+json);
        FileUtil.putStringToSp(AppConfig.getContext(), Constant._node, Constant.NodeList, json.toString());
    }

    public static class Node implements Parcelable{
        public String nodeName;
        public String url;
        public int isSelect; // 未初始化-1  初始化后   0 未选择    1 被选择
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
