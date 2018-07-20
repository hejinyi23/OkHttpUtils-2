package com.xwdz.okhttpgson;

import android.os.Handler;
import android.os.Looper;

import com.xwdz.okhttpgson.callback.ICallBack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author huangxingwei(xwdz9989 @ gmail.com)
 * @since 2018/3/27
 */
public class OkRunManager {

    private static final int CONNECT_TIMEOUT_SECONDS = 30;
    private static final int READ_TIMEOUT_SECONDS = 30;
    private static final int WRITE_TIMEOUT_SECONDS = 30;

    private final ArrayList<Call> mCalls = new ArrayList<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private static OkRunManager sOkRunManager;

    private OkHttpClient mClient;
    private OkHttpClient.Builder mBuilder;
    private String mTag;


    public static OkRunManager getInstance() {
        if (sOkRunManager == null) {
            synchronized (OkRunManager.class) {
                if (sOkRunManager == null) {
                    sOkRunManager = new OkRunManager();
                }
            }
        }
        return sOkRunManager;
    }

    private OkRunManager() {
        mBuilder = newBuilder(CONNECT_TIMEOUT_SECONDS, READ_TIMEOUT_SECONDS, WRITE_TIMEOUT_SECONDS);
    }


    public void build() {
        //默认log拦截器
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLog(mTag));
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        mBuilder.addInterceptor(logInterceptor);
        mClient = mBuilder.build();
    }


    public OkRunManager newBuilder() {
        mBuilder = newBuilder(CONNECT_TIMEOUT_SECONDS, READ_TIMEOUT_SECONDS, WRITE_TIMEOUT_SECONDS);
        return this;
    }


    private OkHttpClient.Builder newBuilder(long connectTimeout, long readTimeout, long writeTimeout) {
        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS);
    }


    public OkRunManager addInterceptor(Interceptor interceptor) {
        mBuilder.addInterceptor(interceptor);
        return this;
    }

    public OkRunManager attachTag(String tag) {
        this.mTag = tag;
        return this;
    }

    public OkRunManager addNetworkInterceptor(Interceptor interceptor) {
        mBuilder.addNetworkInterceptor(interceptor);
        return this;
    }

    public OkRunManager setOkHttpClient(OkHttpClient client) {
        this.mClient = client;
        return this;
    }

    public OkHttpClient getDefaultClient() {
        return mClient;
    }

    public Response execute(final Request request) throws IOException {
        Call call = mClient.newCall(request);
        mCalls.add(call);
        return call.execute();
    }


    public void execute(final Request request, final ICallBack iCallBack, final boolean isMainUIThread) {
        Call call = mClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (iCallBack != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iCallBack.onFailure(call, e);
                        }
                    });
                }
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                if (iCallBack != null) {
                    try {
                        iCallBack.onNativeResponse(call, response, isMainUIThread);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mCalls.add(call);
    }

    public void cancel() {
        if (!mCalls.isEmpty()) {
            for (Call call : mCalls) {
                if (call.isExecuted()) {
                    call.cancel();
                }
            }
        }
    }
}