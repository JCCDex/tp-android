package com.tokenbank.base;

import android.os.Parcel;
import android.os.Parcelable;

//TODO
/**
 * @ClassName Transaction
 * @Authur name
 * @Date 21-4-23
 * 用来 封装各个链的交易 和 交易状态返回
 */
public class Transaction {

    interface TransactionResult{
        //void
    }

    private static Transaction instance = new Transaction();

    private Transaction() {
    }

    public static Transaction getInstance() {
        return instance;
    }


    private void send(){


    }


    /**
     * Tx 封装的是
     */
    public static class TX implements Parcelable {

        private String from;
        private String to;

        private String value;
        private String gas;
        private String gasPrice;

        private String data;


        protected TX(Parcel in) {
        }

        public static final Creator<TX> CREATOR = new Creator<TX>() {
            @Override
            public TX createFromParcel(Parcel in) {
                return new TX(in);
            }

            @Override
            public TX[] newArray(int size) {
                return new TX[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }
    }


}
