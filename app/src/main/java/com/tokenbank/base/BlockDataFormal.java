package com.tokenbank.base;

import com.tokenbank.utils.GsonUtil;

import java.math.BigDecimal;

//TODO
/**
 * @ClassName BlockDataFormal
 * @Authur name
 * @Date 21-4-23
 *
 * 用来规定各个链 各个币种 余额 显示精度
 * 对 BaseWalletUtil 的进一步封装 sendTransaction signTransaction 和 getBalance 都将从这里发出 最终目的是覆盖整个 BaseWalletUtil需要规格化的地方
 *
 * 用户输入 -->  规格化 --> 交易
 * 获取余额 -->  规格化 --> 显示
 * gas gasPrice decimal --> 约束显示
 *
 * count 用于 显示   < balance(BigDecimal) --> 规格化  --> count(String)>
 * value 用于 交易   < balance(BigDecimal) --> 规格化  --> value(String)>
 * balance 用于计算(转化为 最小单位进行计算)
 */
public class BlockDataFormal {

    private static BlockDataFormal instance = new BlockDataFormal();
    private BaseWalletUtil mWalletUtil;
    private WalletInfoManager.WData mCurrentWallet;

    private BlockDataFormal() {
    }

    public static BlockDataFormal getInstance() {
        return instance;
    }

    private void init(){
        mCurrentWallet = WalletInfoManager.getInstance().getCurrentWallet();
        mWalletUtil = TBController.getInstance().getWalletUtil(mCurrentWallet.type);
    }

    private void getBalance(String address ){

        mWalletUtil.queryBalance(address, mCurrentWallet.type, new WCallback() {
            @Override
            public void onGetWResult(int ret, GsonUtil extra) {
            }
        });
    }


    // count -> balance
    private BigDecimal countFormal(String count){
        return null;
    }

    private void valueFormal(){

    }

    private BigDecimal getBalance(){
        return null;
    }

}
