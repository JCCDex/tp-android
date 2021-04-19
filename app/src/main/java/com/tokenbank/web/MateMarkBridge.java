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
import com.tokenbank.base.BaseWalletUtil;
import com.tokenbank.base.BlockNodeData;
import com.tokenbank.base.TBController;
import com.tokenbank.base.WCallback;
import com.tokenbank.base.WalletInfoManager;
import com.tokenbank.config.AppConfig;
import com.tokenbank.dialog.DappMessageDialog;
import com.tokenbank.dialog.PwdDialog;
import com.tokenbank.utils.GsonUtil;
import com.tokenbank.utils.ToastUtil;
import com.tokenbank.utils.ViewUtil;
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
        Log.e(TAG, "callHandler: "+methodName + " params = "+params);
        GsonUtil param = new GsonUtil(params);
        GsonUtil msg = new GsonUtil(param.getString("param",""));
        mCurrentWallet = WalletInfoManager.getInstance().getCurrentWallet();
        switch (methodName){
            case "secret":
                notifySuccessResult(mCurrentWallet.wpk,callbackId);
                break;
            case "eth_accounts":
                notifySuccessResult(mCurrentWallet.waddress,callbackId);
                break;
            case "getNode":
                notifySuccessResult(BlockNodeData.getInstance().getCurrentNode().url,callbackId);
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
            case "wallet_requestPermissions":
                notifySuccessResult(mCurrentWallet.waddress,callbackId);
                break;
            case "eth_getEncryptionPublicKey":
                AppConfig.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewUtil.showSysAlertDialog(mContext, mContext.getString(R.string.enter_title_prompt), mContext.getString(R.string.toast_allow_web3_Permissions),
                            mContext.getString(R.string.dialog_btn_confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EthereumWallet.getInstance().getEncryptionPublicKey(mCurrentWallet.wpk, new JCallback() {
                                        @Override
                                        public void completion(JCCJson json) {
                                            String publicKey = json.getString("publicKey");
                                            if(!publicKey.equals("")){
                                                notifySuccessResult(publicKey,callbackId);
                                            }
                                        }
                                    });
                                    dialog.dismiss();
                                }
                            }, mContext.getString(R.string.button_reject), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    notifySuccessResult("false",callbackId);
                                    dialog.dismiss();
                                }
                            });
                    }
                });
                break;
            case "eth_signTypedData":
                AppConfig.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new DappMessageDialog(mContext, new DappMessageDialog.OnOrderListener() {
                          @Override
                          public void onConfirmOrder() {
                              EthereumWallet.getInstance().signTypedData(msg.getObj(),mCurrentWallet.wpk, new JCallback() {
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
                          }
                          @Override
                          public void onRejectOrder() {
                              notifyFailedResult("false",callbackId);
                          }
                        },mCurrentWallet.waddress, msg.toString()).show();
                    }
                });
                break;
            case "eth_signTypedData_v3":
                AppConfig.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new DappMessageDialog(mContext, new DappMessageDialog.OnOrderListener() {
                            @Override
                            public void onConfirmOrder() {
                                EthereumWallet.getInstance().signTypedData_v3(msg.getObj(),mCurrentWallet.wpk, new JCallback() {
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
                            }
                            @Override
                            public void onRejectOrder() {
                                notifyFailedResult("false",callbackId);
                            }
                        },mCurrentWallet.waddress, msg.toString()).show();
                    }
                });
                break;
            case "personal_sign":
                AppConfig.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new DappMessageDialog(mContext, new DappMessageDialog.OnOrderListener() {
                            @Override
                            public void onConfirmOrder() {
                                EthereumWallet.getInstance().personalSign(msg.toString(),mCurrentWallet.wpk,param.getString("password",""),new JCallback() {
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
                            }
                            @Override
                            public void onRejectOrder() {
                                notifyFailedResult("false",callbackId);
                            }
                        },mCurrentWallet.waddress, msg.toString()).show();
                    }
                });
                break;
            case "eth_signTypedData_v4":
                AppConfig.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new DappMessageDialog(mContext, new DappMessageDialog.OnOrderListener() {
                            @Override
                            public void onConfirmOrder() {
                                EthereumWallet.getInstance().signTypedData_v4(msg.getObj(),mCurrentWallet.wpk, new JCallback() {
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
                            }
                            @Override
                            public void onRejectOrder() {
                                notifyFailedResult("false",callbackId);
                            }
                        },mCurrentWallet.waddress, msg.toString()).show();
                    }
                });
                break;
            case "wallet_getPermissions":
                AppConfig.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewUtil.showSysAlertDialog(mContext, mContext.getString(R.string.enter_title_prompt), mContext.getString(R.string.toast_allow_web3_Permissions),
                                mContext.getString(R.string.dialog_btn_confirm), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        notifySuccessResult("true",callbackId);
                                        dialog.dismiss();
                                    }
                                }, mContext.getString(R.string.dialog_btn_cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        notifySuccessResult("false",callbackId);
                                        dialog.dismiss();
                                    }
                        });
                    }
                });
                break;

            case  "eth_decrypt":
                AppConfig.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new DappMessageDialog(mContext, new DappMessageDialog.OnOrderListener() {
                            @Override
                            public void onConfirmOrder() {
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
                            }
                            @Override
                            public void onRejectOrder() {
                                notifyFailedResult("false",callbackId);
                            }
                        },mCurrentWallet.waddress, msg.toString()).show();
                    }
                });
                break;
            case "eth_sendTransaction":
                final GsonUtil TransactionParam = new GsonUtil(params);
                TransactionParam.putString("secret",mCurrentWallet.wpk);
                TransactionParam.putString("abi","MetaMask");
                //这个丑东西怎么处理掉
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
                                                    String rawTransaction  = extra.getString("rawTransaction", "");
                                                    Log.d(TAG, "onGetWResult: 签名为 "+rawTransaction);
                                                    mWalletUtil.sendSignedTransaction(rawTransaction, new WCallback() {
                                                        @Override
                                                        public void onGetWResult(int ret, GsonUtil extra) {
                                                            if(ret == 0){
                                                                Log.d(TAG, "onGetWResult: "+extra.toString());
                                                                String hash = extra.getString("hash","");
                                                                notifySuccessResult(hash,callbackId);
                                                            } else {
                                                                Log.d(TAG, "onGetWResult: 交易上链失败");
                                                                notifyFailedResult("sendTransaction failed",callbackId);
                                                            }
                                                        }
                                                    });
                                                } else{
                                                    Log.d(TAG, "onGetWResult: 签名失败");
                                                    notifyFailedResult("sign failed",callbackId);
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

}
