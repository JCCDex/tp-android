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
    private String desc ;
    private static BlockNodeData instance;
    private List<Node> PublicNode = new ArrayList<>(); //系统默认列点列表    通过配置文件获取
    private List<Node> CustomNode = new ArrayList<>(); //用户自定义节点列表    通过sp存储获取 根据节点的不同也要分别存储。
    public static final int PUBLIC = 0;
    public static final int CUSTOM = 1;
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
        String publicNodes = FileUtil.getConfigFile(AppConfig.getContext(), "publicNode.json");
        PublicNode = toNodeList(publicNodes,PUBLIC);

        String customNodes = FileUtil.getStringFromSp(AppConfig.getContext(), Constant.custom_node, Constant.customNodeList);
        CustomNode = toNodeList(customNodes,CUSTOM);
    }

    public List<Node> getPublicNodeList(){
        if (PublicNode == null || PublicNode.size() == 0) {
            Log.e(TAG, "getPublicNodeList: 读取配置文件失败");
            init();
            return null;
        }
        return PublicNode;
    }

    public List<Node> getCustomNodeList(){
        if (CustomNode == null || CustomNode.size() == 0) {
            init();
            return null;
        }
        return CustomNode;
    }

    public boolean addCustomNode(Node node){
        for (Node item : CustomNode) {
            if(node.url.equals(item.url)){
                return false;
            }
        }
        CustomNode.add(node);
        saveCustomNodeToSp();
        return true;
    }

    public void deleteCustomNode(Node node){
        CustomNode.remove(node);
        saveCustomNodeToSp();
    }

    private GsonUtil toJson(List<Node> nodes){
        GsonUtil data = new GsonUtil("[]");
        for (int i = 0; i < nodes.size(); i++) {
            Node item = nodes.get(i);
            GsonUtil node = new GsonUtil("{}");
            node.putString("name",item.nodeName);
            node.putString("url",item.url);
            node.putInt("position",item.position);
            node.putInt("isSelect",item.isSelect);
            node.putInt("isConfigNode",item.isConfigNode);
            data.put(desc,node);
        }
        return data;
    }

    private List<Node> toNodeList(String JsonStr,int flag) {
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
                            node.position = nodeLit.getInt("isSelect",1);
                            node.isSelect = nodeLit.getInt("position",-1);
                            node.nodeName = nodeLit.getString("name","");
                            node.url = nodeLit.getString("url","");
                            node.isConfigNode = flag;
                            NodeList.add(node);
                        }
                    }
                }
            }
        }
        return NodeList;
    }

    public void saveCustomNodeToSp(){
        //保存的逻辑为全部删除后重新保存
        FileUtil.putStringToSp(AppConfig.getContext(), Constant.custom_node, Constant.customNodeList, toJson(CustomNode).toString());
    }

    public static class Node implements Parcelable{
        public String nodeName;
        public String url;
        public int position; // 默认-1    初始化后 0,1,2,3,4,5 被选择的item序列
        public int isSelect; // 默认 0 未选择    1 被选择
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
                    node.isSelect == this.isSelect && node.position == this.position && node.isConfigNode == this.isConfigNode;
        }

        protected Node(Parcel in) {
            this.position = in.readInt();
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
            dest.writeInt(this.position);
            dest.writeString(this.nodeName);
            dest.writeString(this.url);
            dest.writeInt(this.isSelect);
            dest.writeInt(this.isConfigNode);
        }
    }
}
