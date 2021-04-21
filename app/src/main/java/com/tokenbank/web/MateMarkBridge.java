package com.tokenbank.web;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.android.jccdex.app.base.JCallback;
import com.android.jccdex.app.ethereum.EthereumWallet;
import com.android.jccdex.app.util.JCCJson;
import com.just.agentweb.AgentWeb;
import com.tokenbank.R;
import com.tokenbank.activity.TokenTransferActivity;
import com.tokenbank.base.BaseWalletUtil;
import com.tokenbank.base.BlockNodeData;
import com.tokenbank.base.TBController;
import com.tokenbank.base.WCallback;
import com.tokenbank.base.WalletInfoManager;
import com.tokenbank.config.AppConfig;
import com.tokenbank.dialog.DappMessageDialog;
import com.tokenbank.dialog.DappTransactionDialog;
import com.tokenbank.dialog.EosOrderDetailDialog;
import com.tokenbank.dialog.PwdDialog;
import com.tokenbank.utils.GsonUtil;
import com.tokenbank.utils.ToastUtil;
import com.tokenbank.utils.ViewUtil;
import com.tokenbank.wallet.ETHWalletBlockchain;
import com.zxing.activity.CaptureActivity;

import org.json.JSONObject;

/**
 * @ClassName MateMarkBridge
 * @Authur name
 * @Date 21-2-25
 * Description MateMask DAPP 支持
 */

public class MateMarkBridge {

    private static final String TAG = "MateMarkBridge";
    private WalletInfoManager.WData mCurrentWallet; //当前使用哪个钱包转账
    private AgentWeb mAgentWeb;
    private Context mContext;
    private BaseWalletUtil mWalletUtil;

    public MateMarkBridge(AgentWeb agent, Context context, IWebCallBack callback) {
        this.mAgentWeb = agent;
        this.mContext = context;
        this.mWalletUtil = TBController.getInstance().getWalletUtil(WalletInfoManager.getInstance().getWalletType());
    }

    @JavascriptInterface
    public void callHandler(String methodName, String params, final String callbackId) {
        Log.e(TAG, "callHandler: "+methodName + " params = "+params);
        GsonUtil param = new GsonUtil(params);
        String msg = param.getString("param","");
        GsonUtil msgJson = new GsonUtil(msg);
        mCurrentWallet = WalletInfoManager.getInstance().getCurrentWallet();
        switch (methodName){
            case "eth_requestAccounts":
            case "eth_accounts":
                notifySuccessResult(mCurrentWallet.waddress,callbackId);
                break;
            case "getNode":
                notifySuccessResult(BlockNodeData.getInstance().getCurrentNode().url,callbackId);
                break;
            case "wallet_getPermissions":
                // 默认钱包权限开启
                notifySuccessResult("true",callbackId);
                break;
            case "eth_getEncryptionPublicKey":
                requestPermissionsDialog("",mContext.getString(R.string.toast_allow_dapp_Permissions), new PwdDialog.PwdResult() {
                    @Override
                    public void authPwd(String tag, boolean result) {
                        if(result){
                            EthereumWallet.getInstance().getEncryptionPublicKey(mCurrentWallet.wpk, new JCallback() {
                                @Override
                                public void completion(JCCJson json) {
                                    String publicKey = json.getString("publicKey");
                                    if(!publicKey.equals("")){
                                        notifySuccessResult(publicKey,callbackId);
                                    }
                                }
                            });
                        } else {
                            notifyFailedResult("false",callbackId);
                        }
                    }
                });
                break;
            case "eth_signTypedData":
                requestPermissionsDialog(msg,mContext.getString(R.string.toast_allow_dapp_Sign), new PwdDialog.PwdResult() {
                    @Override
                    public void authPwd(String tag, boolean result) {
                        if(result){
                            EthereumWallet.getInstance().signTypedData(msg,mCurrentWallet.wpk, new JCallback() {
                                @Override
                                public void completion(JCCJson json) {
                                    String result = json.getString("result");
                                    if(!result.equals("")){
                                        notifySuccessResult(result,callbackId);
                                    } else {
                                        notifyFailedResult("false",callbackId);
                                    }
                                }
                            });
                        } else {
                            notifyFailedResult("false",callbackId);
                        }
                    }
                });
                break;
            case "eth_signTypedData_v3":
                requestPermissionsDialog(msg, mContext.getString(R.string.toast_allow_dapp_Sign),new PwdDialog.PwdResult() {
                    @Override
                    public void authPwd(String tag, boolean result) {
                        if(result){
                            EthereumWallet.getInstance().signTypedData_v3(msgJson.getObj(),mCurrentWallet.wpk, new JCallback() {
                                @Override
                                public void completion(JCCJson json) {
                                    String result = json.getString("result");
                                    if(!result.equals("")){
                                        notifySuccessResult(result,callbackId);
                                    } else {
                                        notifyFailedResult("false",callbackId);
                                    }
                                }
                            });
                        } else {
                            notifyFailedResult("false",callbackId);
                        }
                    }
                });
                break;
            case "personal_sign":
                requestPermissionsDialog(msg, mContext.getString(R.string.toast_allow_dapp_Sign),new PwdDialog.PwdResult() {
                    @Override
                    public void authPwd(String tag, boolean result) {
                        if(result){
                            EthereumWallet.getInstance().personalSign(msg,mCurrentWallet.wpk,param.getString("password",""),new JCallback() {
                                @Override
                                public void completion(JCCJson json) {
                                    String result = json.getString("result");
                                    Log.d(TAG, "completion: result = "+result);
                                    if(!result.equals("")){
                                        notifySuccessResult(result,callbackId);
                                    } else {
                                        notifyFailedResult("false",callbackId);
                                    }
                                }
                            });
                        } else {
                            notifyFailedResult("false",callbackId);
                        }
                    }
                });
                break;
            case "eth_signTypedData_v4":
                requestPermissionsDialog(msg, mContext.getString(R.string.toast_allow_dapp_Sign),new PwdDialog.PwdResult() {
                    @Override
                    public void authPwd(String tag, boolean result) {
                        if(result){
                            EthereumWallet.getInstance().signTypedData_v4(msgJson.getObj(),mCurrentWallet.wpk, new JCallback() {
                                @Override
                                public void completion(JCCJson json) {
                                    String result = json.getString("result");
                                    if(!result.equals("")){
                                        notifySuccessResult(result,callbackId);
                                    } else {
                                        notifyFailedResult("false",callbackId);
                                    }
                                }
                            });
                        } else {
                            notifyFailedResult("false",callbackId);
                        }
                    }
                });
                break;
            case  "eth_decrypt":
                requestPermissionsDialog(msg, mContext.getString(R.string.toast_allow_dapp_Sign),new PwdDialog.PwdResult() {
                    @Override
                    public void authPwd(String tag, boolean result) {
                        if(result){
                            EthereumWallet.getInstance().decrypt(msg.toString(),mCurrentWallet.wpk, new JCallback() {
                                @Override
                                public void completion(JCCJson json) {
                                    String result = json.getString("result");
                                    if(!result.equals("")){
                                        notifySuccessResult(result,callbackId);
                                    } else {
                                        notifyFailedResult("false",callbackId);
                                    }
                                }
                            });
                        } else {
                            notifyFailedResult("false",callbackId);
                        }
                    }
                });
                break;
            case "eth_sendTransaction":
                requestTransactionDialog(msgJson, new PwdDialog.PwdResult() {
                    @Override
                    public void authPwd(String tag, boolean result) {
                        if(result){
                            mWalletUtil.signedTransaction(msgJson, new WCallback() {
                                @Override
                                public void onGetWResult(int ret, GsonUtil extra) {
                                    if(ret == 0){
                                        mWalletUtil.sendSignedTransaction(extra.getString("hash",""), new WCallback() {
                                            @Override
                                            public void onGetWResult(int ret, GsonUtil extra) {
                                                if (ret == 0) {
                                                    notifyFailedResult(extra.getString("hash",""),callbackId);
                                                }
                                                else {
                                                    notifyFailedResult("false",callbackId);
                                                }
                                            }
                                        });
                                    }else {
                                        notifyFailedResult("false",callbackId);
                                    }
                                }
                            });
                        }else {
                            notifyFailedResult("false",callbackId);
                        }
                    }
                });
                break;
            case "eth_signTransaction":
                requestTransactionDialog(msgJson, new PwdDialog.PwdResult() {
                    @Override
                    public void authPwd(String tag, boolean result) {
                        if(result){
                            mWalletUtil.signedTransaction(msgJson, new WCallback() {
                                @Override
                                public void onGetWResult(int ret, GsonUtil extra) {
                                    if(ret == 0){
                                        notifySuccessResult(extra.getString("rawTransaction",""),callbackId);
                                    }else {
                                        notifyFailedResult("false",callbackId);
                                    }
                                }
                            });
                        }else {
                            notifyFailedResult("false",callbackId);
                        }
                    }
                });
                break;
            case "wallet_scanQRCode":
                CaptureActivity.startCaptureActivity(mContext, callbackId);
                break;
            case "wallet_watchAsset":
                //待研究是否允许提供
                //TODO 添加 ERC20合约
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
            case "wallet_addEthereumChain":
                //待研究是否允许提供
                break;
            default:
                Log.e(TAG, "callHandler:  不存在该函数 : "+methodName +" params =  "+params);
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

    private void requestPermissionsDialog(String msg,String title,PwdDialog.PwdResult pwdResult){
        AppConfig.postOnUiThread(new Runnable() {
            @Override
            public void run() {
                new DappMessageDialog(mContext, new DappMessageDialog.OnOrderListener() {
                    @Override
                    public void onConfirmOrder() {
                        new PwdDialog(mContext,pwdResult, mCurrentWallet.whash, "sign").show();
                    }
                    @Override
                    public void onRejectOrder() {
                        pwdResult.authPwd("sign",false);
                    }
                },mCurrentWallet.waddress, msg,title).show();
            }
        });
    }

    private void requestTransactionDialog(GsonUtil params,PwdDialog.PwdResult pwdResult){
        AppConfig.postOnUiThread(new Runnable() {
            @Override
            public void run() {
                new DappTransactionDialog(mContext,params.getString("from",""),params.getString("to",""),params.getDouble("value",0.0),"",
                new DappTransactionDialog.OnOrderListener() {
                    @Override
                    public void onConfirmOrder() {
                        new PwdDialog(mContext, pwdResult, mCurrentWallet.whash, "transaction").show();
                    }

                    @Override
                    public void onRejectOrder() {
                        pwdResult.authPwd("transaction",false);
                    }
                }).show();
            }
        });
    }

}
