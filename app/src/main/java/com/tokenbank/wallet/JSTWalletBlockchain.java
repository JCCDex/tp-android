package com.tokenbank.wallet;

import android.content.Context;
import android.text.TextUtils;

import com.android.jccdex.app.base.JCallback;
import com.android.jccdex.app.fst.FstWallet;
import com.android.jccdex.app.util.JCCJson;
import com.tokenbank.base.BaseWalletUtil;
import com.tokenbank.base.BlockNodeData;
import com.tokenbank.base.TBController;
import com.tokenbank.base.WCallback;
import com.tokenbank.base.WalletInfoManager;
import com.tokenbank.config.AppConfig;
import com.tokenbank.config.Constant;
import com.tokenbank.dialog.EthGasSettignDialog;
import com.tokenbank.utils.FileUtil;
import com.tokenbank.utils.GsonUtil;
import com.tokenbank.utils.Util;

/**
 * @ClassName JSTWalletBlockchain
 * @Authur name
 * @Date 21-3-24
 * Description
 */
public class JSTWalletBlockchain implements BaseWalletUtil {


    @Override
    public void init() {

         }

    @Override
    public void createWallet(WCallback callback) {
        FstWallet.getInstance().createWallet(new JCallback() {
            @Override
            public void completion(JCCJson json) {
                String address = json.getString("address");
                String secret = json.getString("secret");
                String words = json.getString("words");
                if (address != null && secret != null && words != null) {
                    GsonUtil gsonUtil = new GsonUtil(json.toString());
                    callback.onGetWResult(0, gsonUtil);
                } else {
                    callback.onGetWResult(-1, null);
                }
            }
        });
    }

    @Override
    public void importWallet(String privateKey, int type, WCallback callback) {
        FstWallet.getInstance().importSecret(privateKey,"",new JCallback() {
            @Override
            public void completion(JCCJson json) {
                String address = json.getString("address");
                String secret = json.getString("secret");
                if (address != null && secret != null) {
                    GsonUtil gsonUtil = new GsonUtil(json.toString());
                    callback.onGetWResult(0, gsonUtil);
                } else {
                    callback.onGetWResult(-1, null);
                }
            }
        });
    }

    @Override
    public void toIban(String address, WCallback callback) {
        FstWallet.getInstance().toIban(address, new JCallback() {
            @Override
            public void completion(JCCJson json) {
                String iban = json.getString("Iban");
                if (iban == null) {
                    callback.onGetWResult(-1, null);
                } else {
                    GsonUtil gsonUtil = new GsonUtil(json.toString());
                    callback.onGetWResult(0, gsonUtil);
                }
            }
        });
    }

    @Override
    public void fromIban(String ibanAddress, WCallback callback) {
        FstWallet.getInstance().fromIban(ibanAddress, new JCallback() {
            @Override
            public void completion(JCCJson json) {
                String address = json.getString("address");
                if (address == null) {
                    callback.onGetWResult(-1, null);
                } else {
                    GsonUtil gsonUtil = new GsonUtil(json.toString());
                    callback.onGetWResult(0, gsonUtil);
                }
            }
        });
    }

    @Override
    public void gasPrice(WCallback callback) {
        FstWallet.getInstance().getGasPrice(new JCallback() {
            @Override
            public void completion(JCCJson json) {
                String gas = json.getString("gasPrice");
                if (gas == null) {
                    gas = "8000000000";
                }
                double gasPrice = 8.0f;
                double wei = Util.parseDouble(gas);
                if (wei > 0) {
                    gasPrice = wei / 1000000000.0f;
                }
                GsonUtil gasPriceJson = new GsonUtil("{}");
                gasPriceJson.putDouble("gasPrice", gasPrice);
                callback.onGetWResult(0, gasPriceJson);
            }
        });
    }

    @Override
    public void signedTransaction(GsonUtil data, WCallback callback) {
        //TODO make JSON
        JCCJson json = new JCCJson();
        FstWallet.getInstance().SignTransaction(json, new JCallback() {
            @Override
            public void completion(JCCJson json) {

            }
        });
    }

    @Override
    public void sendSignedTransaction(String rawTransaction, WCallback callback) {
        FstWallet.getInstance().sendSignedTransaction(rawTransaction, new JCallback() {
            @Override
            public void completion(JCCJson json) {
                String hash = json.getString("hash");
                if (hash == null) {
                    callback.onGetWResult(-1, null);
                } else {
                    callback.onGetWResult(0, null);
                }
            }
        });
    }

    @Override
    public boolean isWalletLegal(String pk, String address) {
        if (!TextUtils.isEmpty(pk) && !TextUtils.isEmpty(address) && pk.startsWith("0x") && pk.length() == 66) {
            return true;
        }
        return false;
    }

    @Override
    public void generateReceiveAddress(String walletAddress, double amount, String token, WCallback callback) {
        if (TextUtils.isEmpty(walletAddress) || TextUtils.isEmpty(token)) {
            callback.onGetWResult(-1, new GsonUtil("{}"));
            return;
        }
        final double tmpAmount = amount < 0 ? 0.0f : amount;
        final GsonUtil address = new GsonUtil("{}");
        toIban(walletAddress, new WCallback() {
            @Override
            public void onGetWResult(int ret, GsonUtil extra) {
                if (ret == 0) {
                    String ibanAddress = extra.getString("iban", "");
                    if (TextUtils.isEmpty(ibanAddress)) {
                        callback.onGetWResult(-1, address);
                    } else {
                        String receiveStr = String.format("iban:%s?amount=%f&token=%s", ibanAddress, tmpAmount, token);
                        address.putString("receiveAddress", receiveStr);
                        callback.onGetWResult(0, address);
                    }
                } else {
                    callback.onGetWResult(-1, address);
                }
            }
        });
    }

    @Override
    public void calculateGasInToken(double gas, double gasPrice, boolean defaultToken, WCallback callback) {
        if (gasPrice <= 0.0) {
            gasPrice(new WCallback() {
                @Override
                public void onGetWResult(int ret, GsonUtil extra) {
                    double gasPrice = 8.0f;
                    if (ret == 0) {
                        double gasPriceByEth = extra.getDouble("gasPrice", 8.0f);
                        if (gasPriceByEth > 0.0f) {
                            gasPrice = gasPriceByEth;
                        }
                    }
                    double totalGasInWei = gasPrice * 1000000000.0f * getRecommendGas(gas, defaultToken);
                    GsonUtil gas = new GsonUtil("{}");
                    gas.putString("gas", Util.formatDoubleToStr(5, Util.fromWei(TBController.ETH_INDEX, totalGasInWei)) + " " + "ETH");
                    callback.onGetWResult(0, gas);
                }
            });
        } else {
            double totalGasInWei = gasPrice * 1000000000.0f * getRecommendGas(gas, defaultToken);
            GsonUtil gasJson = new GsonUtil("{}");
            gasJson.putString("gas", Util.formatDoubleToStr(5, Util.fromWei(TBController.ETH_INDEX, totalGasInWei)) + " " + "ETH");
            callback.onGetWResult(0, gasJson);
        }
    }

    @Override
    public void gasSetting(Context context, double gasPrice, boolean defaultToken, WCallback callback) {
        EthGasSettignDialog gasSettignDialog = new EthGasSettignDialog(context, new EthGasSettignDialog.OnSettingGasListener() {
            @Override
            public void onSettingGas(double gasPrice, double gasInToken) {
                GsonUtil gas = new GsonUtil("{}");
                gas.putString("gas", Util.formatDoubleToStr(5, gasInToken) + " " + "mfc");
                gas.putDouble("gasPrice", gasPrice);
                callback.onGetWResult(0, gas);
            }
        }, gasPrice, defaultToken, TBController.ETH_INDEX);
        gasSettignDialog.show();
    }

    @Override
    public double getRecommendGas(double gas, boolean defaultToken) {
        if (gas <= 0.0f) {
            if (defaultToken) {
                return 22000;
            } else {
                return 70000;
            }
        }
        return 0;
    }

    @Override
    public String getDefaultTokenSymbol() {
        return "mfc";
    }

    @Override
    public int getDefaultDecimal() {
        return 18;
    }

    @Override
    public void getTokenInfo(String token, long blockChainId, WCallback callback) {

    }

    @Override
    public void translateAddress(String sourceAddress, WCallback callback) {
        if (TextUtils.isEmpty(sourceAddress)) {
            GsonUtil addressJson = new GsonUtil("{}");
            addressJson.putString("receive_address", "");
            callback.onGetWResult(0, addressJson);
            return;
        }
        fromIban(sourceAddress, new WCallback() {
            @Override
            public void onGetWResult(int ret, GsonUtil extra) {
                GsonUtil addressJson = new GsonUtil("{}");
                if (ret == 0) {
                    addressJson.putString("receive_address", extra.getString("address", ""));
                } else {
                    addressJson.putString("receive_address", "");
                }
                callback.onGetWResult(0, addressJson);
            }
        });
    }

    @Override
    public boolean checkWalletAddress(String receiveAddress) {
        if (!receiveAddress.startsWith("0x") || receiveAddress.length() != 42) {
            return false;
        }
        return true;
    }

    @Override
    public boolean checkWalletPk(String privateKey) {
        if (!privateKey.startsWith("0x") || privateKey.length() != 66) {
            return false;
        }
        return true;
    }

    @Override
    public void queryTransactionDetails(String hash, WCallback callback) {
        if (TextUtils.isEmpty(hash)) {
            callback.onGetWResult(-1, new GsonUtil("{}"));
            return;
        }
        FstWallet.getInstance().getTransactionDetail(hash, new JCallback() {
            @Override
            public void completion(JCCJson json) {

            }
        });
    }

    @Override
    public void queryBalance(String address, int type, WCallback callback) {
        FstWallet.getInstance().getBalance(address, new JCallback() {
            @Override
            public void completion(JCCJson json) {
                String balance = json.getString("balance");
                if (balance == null) {
                    balance = "0";
                }
                GsonUtil formatData = new GsonUtil("{}");
                GsonUtil arrays = new GsonUtil("[]");
                GsonUtil data = new GsonUtil("{}");
                data.putLong("blockchain_id", Long.parseLong("" + TBController.FST_INDEX));
                data.putString("icon_url", Constant.MOAC_ICON);
                data.putString("bl_symbol", "mfc");
                data.putInt("decimal", 18);
                data.putString("balance", balance);
                data.putString("asset", "0");
                arrays.put(data);
                formatData.put("data", arrays);
                callback.onGetWResult(0, formatData);
            }
        });
    }

    @Override
    public void queryTransactionList(GsonUtil params, WCallback callback) {
    }

    @Override
    public double getValue(int decimal, double originValue) {
        if (decimal <= 0) {
            decimal = getDefaultDecimal();
        }
        return Util.formatDouble(5, Util.translateValue(decimal, originValue));
    }

    @Override
    public GsonUtil loadTransferTokens(Context context) {
        String data = FileUtil.getConfigFile(context, "fstTokens.json");
        return new GsonUtil(data);
    }

    @Override
    public String getTransactionSearchUrl(String hash) {
        return Constant.fst_transaction_search_url + hash;
    }
}
