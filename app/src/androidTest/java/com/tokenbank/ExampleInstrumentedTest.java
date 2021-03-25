package com.tokenbank;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tokenbank.base.BaseWalletUtil;
import com.tokenbank.wallet.SWTWalletBlockchain;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private BaseWalletUtil mSwtWalletUtil;

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.tokenbank", appContext.getPackageName());
    }

    @Test
    public void testSendRaw(){
        mSwtWalletUtil = new SWTWalletBlockchain();
    }
}
