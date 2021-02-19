package com.tokenbank.web;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.just.agentweb.AgentWeb;
import com.tokenbank.R;
import com.tokenbank.activity.ImportWalletActivity;
import com.tokenbank.base.BaseWalletUtil;
import com.tokenbank.base.BlockChainData;
import com.tokenbank.base.TBController;
import com.tokenbank.base.WCallback;
import com.tokenbank.base.WalletInfoManager;
import com.tokenbank.config.AppConfig;
import com.tokenbank.dialog.MsgDialog;
import com.tokenbank.dialog.PwdDialog;
import com.tokenbank.utils.DeviceUtil;
import com.tokenbank.utils.FileUtil;
import com.tokenbank.utils.GsonUtil;
import com.tokenbank.utils.ToastUtil;
import com.zxing.activity.CaptureActivity;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import cn.sharesdk.onekeyshare.OnekeyShare;

import static com.tokenbank.activity.CreateWalletActivity.TAG;


/**
 * JS调用原生接口类
 */
public class JsNativeBridge {

    private final static String MSG_SUCCESS = "success";
    private final static long FIFTEEN = 15 * 60 * 1000L;

    private AgentWeb mAgentWeb;
    private Context mContext;
    private WalletInfoManager mWalletManager;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private String version,name,address;
    private String mFrom, mTo, mValue, mToken, mIssuer, mGas, mMemo,mGasPrice;
    private IWebCallBack mWebCallBack;
    private BaseWalletUtil mWalletUtil;
    private WalletInfoManager.WData mCurrentWallet; //当前使用哪个钱包转账
    private BlockChainData.Block block;
    public JsNativeBridge(AgentWeb agent, Context context, IWebCallBack callback) {
        this.mAgentWeb = agent;
        this.mContext = context;
        this.mWebCallBack = callback;
        this.mWalletManager = WalletInfoManager.getInstance();
        this.mWalletUtil = TBController.getInstance().getWalletUtil(WalletInfoManager.getInstance().getWalletType());
    }

    @JavascriptInterface
    public void callMessage(String methodName, String params, final String callbackId) {
        mCurrentWallet = WalletInfoManager.getInstance().getCurrentWallet();
        block = BlockChainData.getInstance().getBolckByHid(WalletInfoManager.getInstance().getWalletType());
        final GsonUtil result = new GsonUtil("{}");
        Log.d(TAG, "callMessage: +++++++++++++++++++++   1"+methodName);
        switch (methodName) {
            case "getAppInfo":
                PackageManager packageManager = mContext.getPackageManager();
                PackageInfo packageInfo = null;
                try {
                    packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
                    if (packageInfo != null) {
                        version = packageInfo.versionName;
                        name = mContext.getResources().getString(packageInfo.applicationInfo.labelRes);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                GsonUtil infoData = new GsonUtil("{}");
                infoData.putString("name", name);
                infoData.putString("system", "android");
                infoData.putString("version", version);
                infoData.putString("sys_version", Build.VERSION.SDK_INT + "");
                result.putBoolean("result", true);
                result.put("data", infoData);
                result.putString("msg", MSG_SUCCESS);
                this.mAgentWeb.getJsAccessEntrace().callJs("javascript:" + callbackId + "('" + result.toString() + "')");
                break;

            case "getWallets":
                List<WalletInfoManager.WData> wallets = mWalletManager.getAllWallet();
                GsonUtil data1 = new GsonUtil("[]");
                for (int i = 0; i < wallets.size(); i++) {
                    GsonUtil wallet = new GsonUtil("{}");
                    address = wallets.get(i).waddress;
                    name = wallets.get(i).wname;
                    wallet.putString("name", name);
                    wallet.putString("address", address);
                    data1.put(wallet);
                }
                result.putBoolean("result", true);
                result.put("data", data1);
                result.putString("msg", MSG_SUCCESS);
                this.mAgentWeb.getJsAccessEntrace().callJs("javascript:" + callbackId + "('" + result.toString() + "')");
                break;

            case "getDeviceId":
                String deviceId = DeviceUtil.generateDeviceUniqueId();
                result.putString("deviceId", deviceId);
                result.putString("msg", MSG_SUCCESS);
                this.mAgentWeb.getJsAccessEntrace().callJs("javascript:" + callbackId + "('" + result.toString() + "')");
                break;

            case "shareNewsToSNS":
                GsonUtil tx = new GsonUtil(params);
                String mTitle = tx.getString("title", "");
                String mUrl = tx.getString("url", "").toUpperCase();
                String mText = tx.getString("text", "");
                String mImgUrl = tx.getString("imgUrl", "");
                OnekeyShare oks = new OnekeyShare();
                // title标题，微信、QQ和QQ空间等平台使用
                oks.setTitle(mTitle);
                // titleUrl QQ和QQ空间跳转链接
                oks.setTitleUrl(mUrl);
                // text是分享文本，所有平台都需要这个字段
                oks.setText(mText);
                // imagePath是图片的本地路径，确保SDcard下面存在此张图片
//                oks.setImagePath(mImgUrl);
                oks.setImageUrl(mImgUrl);
                // url在微信、Facebook等平台中使用
                oks.setUrl(mUrl);
                // 启动分享GUI
                oks.show(mContext);
                break;

            case "invokeQRScanner":
                CaptureActivity.startCaptureActivity(mContext, callbackId);
                break;

            case "getCurrentWallet":
                String walletName = mCurrentWallet.wname;
                GsonUtil data = new GsonUtil("{}");
                data.putString("address", mCurrentWallet.waddress);
                data.putString("name",walletName);
                data.putString("blockchain","jingtum");
                result.putBoolean("result", true);
                result.put("data", data);
                result.putString("msg", MSG_SUCCESS);
                this.mAgentWeb.getJsAccessEntrace().callJs("javascript:" + callbackId + "('" + result.toString() + "')");
                break;

            case "sign":
                final GsonUtil SignParam = new GsonUtil(params);
                Log.d(TAG, "callHandler: "+mCurrentWallet.waddress.toLowerCase() );
                Log.d(TAG, "callHandler: "+SignParam.getString("address","").toLowerCase() );
                if(!mCurrentWallet.waddress.toLowerCase().equals(SignParam.getString("address",""))){
                    result.putString("err","has no this wallet");
                    notifySignResult(result,callbackId);
                    return;
                }
                SignParam.putString("secret",mCurrentWallet.wpk);
                AppConfig.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    new PwdDialog(mContext, new PwdDialog.PwdResult() {
                        @Override
                        public void authPwd(String tag, boolean flag) {
                        if (TextUtils.equals(tag, "transaction")) {
                            if (flag) {
                                //执行
                                mWalletUtil.signedTransaction(SignParam, new WCallback() {
                                    @Override
                                    public void onGetWResult(int ret, GsonUtil extra) {
                                        String raw = extra.getString("raw","");
                                        if(raw.equals("")){
                                            result.putString("err",extra.getString("err",""));
                                        } else {
                                            result.putString("raw",raw);
                                        }
                                        notifySignResult(result,callbackId);
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
            case "moacTokenTransfer":

                break;
            case "back":
                if (mWebCallBack != null) {
                    mWebCallBack.onBack();
                }
                break;

            case "close":
                if (mWebCallBack != null) {
                    mWebCallBack.onClose();
                }
                break;

            case "fullScreen":
                if (mWebCallBack != null) {
                    mWebCallBack.switchFullScreen(params);
                }
                break;

            case "importWallet":
                ImportWalletActivity.startImportWalletActivity(mContext,0);
                break;

            case "setMenubar":
                //未开发导航栏
                //导航栏隐藏与否
                break;
            case "signJingtumTransaction":
                /*
                    "Account":"j47J1UriYXXXXXXXXXXXX",
                    "Fee": 0.00001,
                    "Flags": 524288,
                    "TakerGets":"111",
                    "TakerPays":{
                        "currency":"CNY",
                        "issuer":"jGa9J9TkqtBcUoHe2zqhVFFbgUVED6o9or",
                        "value":"1.2321"
                        },
                    "TransactionType":"OfferCreate",
                    "Sequence":4368

                {
                "Account":"jL2773nCXm3W6EjQu5H9TAt1iitJaftNVb",
                "Destination":"jZ3Upe4Be53xVVoRqyiXqCkrXBMfegDP9",
                "Fee":0.001,
                "Amount":{"currency":"SWT","issuer":"","value":"1"},
                "TransactionType":"Payment",
                "Sequence":1,
                "Memos":[{"Memo":{"MemoData":"恭喜发财，大吉大利"}}]
                }

                JSONObject transaction = new JSONObject();
                transaction.put("Account", "jpgWGpfHz8GxqUjz5nb6ej8eZJQtiF6KhH");
                transaction.put("Fee", 0.00001);
                transaction.put("Flags", 0);
                transaction.put("Destination", "j4JJb3c17HuwRoKycjtrd9adpmbrneEE6w");
                transaction.put("Amount", 1);
                transaction.put("TransactionType", "Payment");
                transaction.put("Sequence", 1);
                 */
                final GsonUtil trans = new GsonUtil(params);
                trans.putInt("Flags", 0);
                GsonUtil SwtcTx = new GsonUtil("{}");
                SwtcTx.put("transaction", trans);
                SwtcTx.putString("secret", mCurrentWallet.wpk);
                AppConfig.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new PwdDialog(mContext, new PwdDialog.PwdResult() {
                            @Override
                            public void authPwd(String tag, boolean flag) {
                                if (TextUtils.equals(tag, "transaction")) {
                                    if (flag) {

                                        mWalletUtil.signedTransaction(SwtcTx, new WCallback() {
                                            @Override
                                            public void onGetWResult(int ret, GsonUtil extra) {
                                                if( ret == 0 ){
                                                    String signature = extra.getString("signature","");
                                                    result.putBoolean("result", true);
                                                    result.putString("data", signature);
                                                    result.putString("msg", MSG_SUCCESS);
                                                    Log.d(TAG, "onGetWResult: success !!!!!!"+signature);
                                                    notifySignResult(result,callbackId);
                                                } else {
                                                    Log.d(TAG, "onGetWResult: err !!!!!!");
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
            case "saveImage":
                Log.d(TAG, "callHandler: 开始保存图片 url = "+params);
                String picUrl = params;
                if(!picUrl.equals("")){
                    try {
                        //通过url获取图片
                        URL iconUrl=new URL(picUrl);
                        URLConnection connection=iconUrl.openConnection();
                        HttpURLConnection httpURLConnection= (HttpURLConnection) connection;
                        int length = httpURLConnection.getContentLength();
                        connection.connect();
                        InputStream inputStream=connection.getInputStream();
                        BufferedInputStream bufferedInputStream=new BufferedInputStream(inputStream,length);
                        Bitmap mBitmap= BitmapFactory.decodeStream(bufferedInputStream);
                        bufferedInputStream.close();
                        inputStream.close();
                        //保存图片
                        FileUtil.saveBitmap(AppConfig.getContext(),mBitmap);
                        new MsgDialog(AppConfig.getContext(), mContext.getString(R.string.picture_save_success)).show();
                    } catch (Exception e) {
                        Log.d(TAG, "callHandler: 保存失败");
                        AppConfig.postOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new MsgDialog(AppConfig.getContext(), mContext.getString(R.string.picture_save_false)).show();
                            }
                        });
                        e.printStackTrace();
                    }
                }
                break;

            case "rollHorizontal":
                //横屏
                if (mWebCallBack != null) {
                    mWebCallBack.rollHorizontal();
                }
                break;

            case "popGestureRecognizerEnable":

                //苹果接口 安卓无效
                break;

            case "forwardNavigationGesturesEnable":

                break;
            case "signMoacTransaction":
                /*
                    tp 接口参数：

                    from: '0xaaaaaaa',
                    to: '0xaaaaaab',
                    gasPrice: 100000000,
                    gasLimit: 60000,
                    data: '0xaawefwefwefwefwefef',
                    value: 1000000000,
                    chainId: 99,
                    via: '',
                    shardingFlag: 0,
                 */
                final GsonUtil MoacTx = new GsonUtil(params);
                MoacTx.putString("gas",MoacTx.getString("gasPrice",""));
                MoacTx.putString("privateKey", mCurrentWallet.wpk);
                MoacTx.putString("senderAddress", MoacTx.getString("from",""));
                MoacTx.putString("receiverAddress", MoacTx.getString("to",""));
                MoacTx.putDouble("tokencount", MoacTx.getDouble("value",0.0f));
                MoacTx.putDouble("gas", MoacTx.getDouble("gasLimit",0.0f));
                MoacTx.putDouble("gasPrice", MoacTx.getDouble("gasPrice",0.0f));
                AppConfig.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new PwdDialog(mContext, new PwdDialog.PwdResult() {
                            @Override
                            public void authPwd(String tag, boolean flag) {
                                if (TextUtils.equals(tag, "transaction")) {
                                    if (flag) {
                                        mWalletUtil.signedTransaction(MoacTx, new WCallback() {
                                            @Override
                                            public void onGetWResult(int ret, GsonUtil extra) {
                                                String signature = extra.getString("signature","");
                                                if(signature.equals("")){
                                                    result.putString("err",extra.getString("err",""));
                                                } else {
                                                    result.putString("signature",signature);
                                                }
                                                notifySignResult(result,callbackId);
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
            case "getNodeUrl":
                String node = "";
                result.putString("node", node);
                result.putString("msg", MSG_SUCCESS);
                this.mAgentWeb.getJsAccessEntrace().callJs("javascript:" + callbackId + "('" + result.toString() + "')");
                break;
            case "sendMoacTransaction":
                /*
                tp 接口参数：
                from: '0xaaaaaaa',
                to: '0xaaaaaab',
                gasPrice: 100000000,
                gasLimit: 60000,
                data: '0xaawefwefwefwefwefef',
                value: 1000000000,
                chainId: 99,
                via: '',
                shardingFlag: 0,
                 */
                final GsonUtil TransactionParam = new GsonUtil(params);
                if(!mCurrentWallet.waddress.toLowerCase().equals(TransactionParam.getString("address",""))){
                    result.putString("err","has no this wallet");
                    notifySignResult(result,callbackId);
                    return;
                }
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
                                                String hash = extra.getString("hash","");
                                                if(hash.equals("")){
                                                    result.putString("err",extra.getString("err",""));
                                                } else {
                                                    result.putString("hash",hash);
                                                }
                                                notifySignResult(result,callbackId);
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
            default:
                Log.e(TAG, "callHandler: no such method : "+methodName);
                break;
        }
    }

    private void notifySignResult(GsonUtil result, String callbackId) {
        this.mAgentWeb.getJsAccessEntrace().callJs("javascript:" + callbackId + "('" + result.toString() + "')");
    }


    private Spanned formatHtml() {
        String paysH = "<font color=\"#3B6CA6\">" + mValue + " </font>";
        String paysCurH = "<font color=\"#021E38\">" + mToken + " </font>";
        return Html.fromHtml(paysH.concat(paysCurH));
    }
}
