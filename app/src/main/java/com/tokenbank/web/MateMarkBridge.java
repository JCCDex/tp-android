package com.tokenbank.web;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.just.agentweb.AgentWeb;
import com.tokenbank.R;
import com.tokenbank.base.BaseWalletUtil;
import com.tokenbank.base.BlockChainData;
import com.tokenbank.base.TBController;
import com.tokenbank.base.WCallback;
import com.tokenbank.base.WalletInfoManager;
import com.tokenbank.config.AppConfig;
import com.tokenbank.dialog.PwdDialog;
import com.tokenbank.utils.GsonUtil;
import com.tokenbank.utils.ToastUtil;
import com.zxing.activity.CaptureActivity;

/**
 * @ClassName MateMarkBridge
 * @Authur name
 * @Date 21-2-25
 * Description MateMask DAPP 支持
 */

public class MateMarkBridge {

    private static final String TAG = "MateMarkBridge";
    private BlockChainData.Block block;
    private WalletInfoManager.WData mCurrentWallet; //当前使用哪个钱包转账
    private String version,name,address;
    private AgentWeb mAgentWeb;
    private Context mContext;
    private WalletInfoManager mWalletManager;
    private BaseWalletUtil mWalletUtil;

    public MateMarkBridge(AgentWeb agent, Context context, IWebCallBack callback) {
        this.mAgentWeb = agent;
        this.mContext = context;
        this.mWalletManager = WalletInfoManager.getInstance();
        this.mWalletUtil = TBController.getInstance().getWalletUtil(WalletInfoManager.getInstance().getWalletType());
    }

    @JavascriptInterface
    public void callHandler(String methodName, String params, final String callbackId) {
        mCurrentWallet = WalletInfoManager.getInstance().getCurrentWallet();
        block = BlockChainData.getInstance().getBolckByHid(WalletInfoManager.getInstance().getWalletType());
        GsonUtil result = new GsonUtil("{}");
        GsonUtil ArrayFormResult = new GsonUtil("[]");
        Log.e(TAG, "原生的这个方法被调用 ： "+methodName);
        switch (methodName){
            case "net_version":
                notifySuccessResult("1.2",callbackId);
                break;
            case "eth_chainId":
                notifySuccessResult("1",callbackId);
                break;
            case "eth_accounts":
                result.putString("result",mCurrentWallet.waddress);
                notifySuccessResult(result,callbackId);
                break;
            case "eth_sign":
                final GsonUtil EthTx = new GsonUtil(params);
                // 密钥和字符串，签名
                Log.d(TAG, "callHandler: "+params);
                EthTx.putString("secret",mCurrentWallet.wpk);
                EthTx.putString("api","api");
                AppConfig.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new PwdDialog(mContext, new PwdDialog.PwdResult() {
                            @Override
                            public void authPwd(String tag, boolean flag) {
                                if (TextUtils.equals(tag, "transaction")) {
                                    if (flag) {
                                        //执行
                                        mWalletUtil.signedTransaction(EthTx, new WCallback() {
                                            @Override
                                            public void onGetWResult(int ret, GsonUtil extra) {
                                                if(ret == 0){
                                                    String signature = extra.getString("rawTransaction","");
                                                    if(signature.equals("")){
                                                        notifyFailedResult(extra.getString("err",""),callbackId);
                                                    } else {
                                                        notifySuccessResult(signature,callbackId);
                                                    }
                                                } else {
                                                    ToastUtil.toast(AppConfig.getContext(), AppConfig.getContext().getString(R.string.sign_failed));
                                                }
                                            }
                                        });
                                    } else {
                                        ToastUtil.toast(AppConfig.getContext(), AppConfig.getContext().getString(R.string.toast_order_password_incorrect));
                                    }
                                }
                            }
                        }, mCurrentWallet.whash, "transaction").show();
                    }
                });
                break;
            case "eth_requestAccounts":
                notifySuccessResult(mCurrentWallet.waddress,callbackId);
                break;
            case "eth_getEncryptionPublicKey":break;
            case "eth_signTypedData":break;
            case "eth_signTypedData_v3":break;
            case "eth_signTypedData_v4":break;
            case "wallet_getPermissions":break;
            case "wallet_requestPermissions":break;
            case "eth_call":break;
            case "eth_getBalance":
                AppConfig.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWalletUtil.queryBalance(address, mCurrentWallet.type, new WCallback() {
                            @Override
                            public void onGetWResult(int ret, GsonUtil extra) {
                                if (ret == 0) {
                                    notifySuccessResult(extra.getString("balance",""),callbackId);
                                }
                            }
                        });
                    }
                });
                break;
            case "eth_estimateGas":break;
            case "eth_sendTransaction":
                Log.d(TAG, "callHandler: "+params);
                final GsonUtil TransactionParam = new GsonUtil(params);
                TransactionParam.putString("secret",mCurrentWallet.wpk);
                AppConfig.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new PwdDialog(mContext, new PwdDialog.PwdResult() {
                            @Override
                            public void authPwd(String tag, boolean flag) {
                                if (TextUtils.equals(tag, "transaction")) {
                                    if (flag) {
                                        mWalletUtil.signedTransaction(TransactionParam, new WCallback() {
                                            @Override
                                            public void onGetWResult(int ret, GsonUtil extra) {
                                                if(ret == 0){
                                                    mWalletUtil.sendSignedTransaction(extra.getString("r", ""), new WCallback() {
                                                        @Override
                                                        public void onGetWResult(int ret, GsonUtil extra) {
                                                            if(ret == 0){
                                                                Log.d(TAG, "onGetWResult: "+extra.toString());
                                                                String hash = extra.getString("hash","");
                                                                notifySuccessResult(hash,callbackId);
                                                            } else {
                                                                notifyFailedResult(extra.getString("err",""),callbackId);
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    } else {
                                        ToastUtil.toast(AppConfig.getContext(), AppConfig.getContext().getString(R.string.toast_order_password_incorrect));
                                    }
                                }
                            }
                        }, mCurrentWallet.whash, "transaction").show();
                    }
                });
                break;
            case "wallet_scanQRCode":
                CaptureActivity.startCaptureActivity(mContext, callbackId);
                break;
            case "wallet_watchAsset":
                //TODO 添加合约
                /*
                type: 'ERC20',
                options: {
                  address: '0xb60e8dd61c5d32be8058bb8eb970870f07233155',
                  symbol: 'FOO',
                  decimals: 18,
                  image: 'https://foo.io/token-image.svg',
                },
                 */
                //TODO 添加成功后返回 boolean
                notifySuccessResult("true",callbackId);
                break;
            case "wallet_addEthereumChain":break;
            case "personal_sign":break;
            default:
                Log.e(TAG, "callHandler: no such method : "+methodName);
                break;

        }
    }

    private void notifySuccessResult(GsonUtil data, String callbackId){
        this.mAgentWeb.getJsAccessEntrace().callJs("javascript:" + callbackId + "('" + data.toString() + "')");
    }

    private void notifyFailedResult(String data,String callbackId){
        GsonUtil result = new GsonUtil("{}");
        result.putString("result", data);
        this.mAgentWeb.getJsAccessEntrace().callJs("javascript:" + callbackId + "('" + result.toString() + "')");
    }

    private void notifySuccessResult(String data,String callbackId){
        GsonUtil result = new GsonUtil("{}");
        result.putString("result", data);
        this.mAgentWeb.getJsAccessEntrace().callJs("javascript:" + callbackId + "('" + result.toString() + "')");
    }

}
