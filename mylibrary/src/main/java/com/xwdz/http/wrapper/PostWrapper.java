package com.xwdz.http.wrapper;

import com.xwdz.http.utils.Assert;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author xingwei.huang (xwdz9989@gamil.com)
 * @since 2019/3/21
 */
public class PostWrapper extends BaseWrapper<PostWrapper> {


    private LinkedHashMap<String, String> mHeaders = new LinkedHashMap<>();
    private LinkedHashMap<String, String> mParams  = new LinkedHashMap<>();

    private boolean mCallbackToMainUIThread = true;
    private String      mUrl;
    private String      mTag;


    public PostWrapper(OkHttpClient okHttpClient, String url) {
        super(okHttpClient);
        mHeaders.clear();
        mParams.clear();

        mUrl = url;
        mTag = url;
    }


    @Override
    protected Request buildRequest() {
        Assert.checkNull(mUrl, "POST 请求链接不能为空!");

        final Request.Builder requestBuilder = new Request.Builder();
        FormBody.Builder params = new FormBody.Builder();

        for (Map.Entry<String, String> map : mHeaders.entrySet()) {
            requestBuilder.addHeader(map.getKey(), map.getValue());
        }

        requestBuilder.url(mUrl);

        for (Map.Entry<String, String> map : mParams.entrySet()) {
            params.add(map.getKey(), map.getValue());
        }
        requestBuilder.post(params.build());

        requestBuilder
                .tag(mTag);
        return requestBuilder.build();
    }

    @Override
    public PostWrapper tag(Object object) {
        Assert.checkNull(object, "tag not null!");
        mTag = String.valueOf(object);
        return this;
    }

    @Override
    public PostWrapper addHeader(String key, String value) {
        mHeaders.put(key, value);
        return this;
    }

    @Override
    public PostWrapper addParams(String key, String value) {
        mParams.put(key, value);
        return this;
    }

    @Override
    public PostWrapper params(LinkedHashMap<String, String> params) {
        mParams.putAll(params);
        return this;
    }

    @Override
    public PostWrapper headers(LinkedHashMap<String, String> header) {
        mHeaders.putAll(header);
        return this;
    }

    @Override
    protected void ready() {

    }

    @Override
    protected boolean isCallbackMainUIThread() {
        return mCallbackToMainUIThread;
    }

    @Override
    public PostWrapper setCallbackMainUIThread(boolean isCallbackToMainUIThread) {
        mCallbackToMainUIThread = isCallbackToMainUIThread;
        return this;
    }
}
