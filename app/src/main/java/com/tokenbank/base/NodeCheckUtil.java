package com.tokenbank.base;

import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.PortScan;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @ClassName NodeCheckUtil
 * @Authur name
 * @Date 21-3-11
 * Description
 */
public class NodeCheckUtil {

    public static void checkNode(String url,Observer<String> observer){
        String[] ws = url.replace("http://", "").replace("https://", "").split(":");
        if (ws.length != 2) {
            return;
        }
        String host = ws[0];
        String port = ws[1];
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            ArrayList<Integer> prots = PortScan.onAddress(host).setMethodTCP().setPort(Integer.valueOf(port)).doScan();
            if (prots != null && prots.size() == 1) {
                Ping ping = Ping.onAddress(host);
                ping.setTimeOutMillis(1000);
                ping.setTimes(5);
                ping.doPing(new Ping.PingListener() {
                    @Override
                    public void onResult(PingResult pingResult) {
                    }

                    @Override
                    public void onFinished(PingStats pingStats) {
                        String ping = String.format("%.2f", pingStats.getAverageTimeTaken());
                        emitter.onNext(ping);
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
            } else {
                if (!emitter.isDisposed()) {
                    emitter.onError(new Throwable());
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }
}
