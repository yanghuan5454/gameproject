package com.wmwl.core.lib.http.finalokhttp;

import java.io.IOException;
import java.util.List;


import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.wmwl.core.lib.http.finalokhttp.handler.EntityCallBack;
import com.wmwl.core.lib.http.finalokhttp.handler.FileEntityHandler;
import com.wmwl.core.lib.http.finalokhttp.handler.StringEntityHandler;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class HttpHandler<T> extends AsyncTask<Object, Object, Object> implements EntityCallBack {

    private final AjaxCallBack<T> callback;
    private final OkHttpClient client;
    private final Request request;
    private boolean isResume = false;

    protected String charset;
    protected String targetUrl = null;
    protected long time;
    protected final StringEntityHandler mStrEntityHandler = new StringEntityHandler();
    protected final FileEntityHandler mFileEntityHandler = new FileEntityHandler();

    public HttpHandler(OkHttpClient client, Request request, String charset, AjaxCallBack<T> callback) {
        this(client, request, null, charset, callback);
    }

    public HttpHandler(OkHttpClient client, Request request, String targetUrl, String charset, AjaxCallBack<T> callback) {
        this.callback = callback;
        this.client = client;
        this.request = request;
        this.targetUrl = targetUrl;
        this.charset = charset;
    }

    @Override
    protected Object doInBackground(Object... objects) {
        Log.d("WMWL", "444444444444444444444444444444");
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("WMWL", "onResponse");
                try {
                    publishProgress(UPDATE_START); // 开始
                    makeCookies(response);
                    handleResponse(response);

                } catch (Exception e) {
                    publishProgress(UPDATE_FAILURE, e, UPDATE_ERROR, e.getMessage()); // 结束
                }

            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("WMWL", "httpError>>" + e.getMessage());
            }
        });
        return null;
    }

//	public void execute(){
//
//	}

    private void makeCookies(Response response) {

        try {
            HttpUrl url = response.request().url();
            CookieJar mCookieJar = client.cookieJar();
            List<Cookie> cookies = Cookie.parseAll(url, response.headers());

            mCookieJar.saveFromResponse(response.request().url(), cookies);
            for (int i = 0; i < cookies.size(); i++) {
                // 这里是读取Cookie['PHPSESSID']的值存在静态变量中，保证每次都是同一个值
                Log.e("lhjtianji", "name:>>>" + cookies.get(i).name() + " ," + cookies.get(i).value());
//			if ("PHPSESSID".equals(cookies.get(i).getName())) {
//				if(TextUtils.isEmpty(AppConfig.SLTH_PHPSESSID)){
//					AppConfig.SLTH_PHPSESSID = cookies.get(i).getValue();
//				}
//				continue;
//			}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected final static int UPDATE_START = 1;
    protected final static int UPDATE_LOADING = 2;
    protected final static int UPDATE_FAILURE = 3;
    protected final static int UPDATE_SUCCESS = 4;
    protected final static int UPDATE_ERROR = -101;


    @SuppressWarnings("unchecked")
    protected void onProgressUpdate(Object... values) {
        int update = Integer.valueOf(String.valueOf(values[0]));
        try {
            switch (update) {
                case UPDATE_START:
                    if (callback != null)
                        callback.onStart();
                    break;
                case UPDATE_LOADING:
                    if (callback != null)
                        callback.onLoading(Long.valueOf(String.valueOf(values[1])),
                                Long.valueOf(String.valueOf(values[2])));
                    break;
                case UPDATE_FAILURE:
                    if (callback != null)
                        callback.onFailure((Throwable) values[1], (Integer) values[2],
                                (String) values[3]);
                    break;
                case UPDATE_SUCCESS:
                    if (callback != null)
                        callback.onSuccess((T) values[1]);

                    break;
                default:
                    break;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public boolean isStop() {
        return mFileEntityHandler.isStop();
    }

    /**
     * @param stop 停止下载任务
     */
    public void stop() {
        mFileEntityHandler.setStop(true);
    }


    private void handleResponse(Response response) {
        try {
            int status = response.code();
            if (status >= 300) {
                String errorMsg = "response status error code:"
                        + status;
                if (status == 416 && isResume) {
                    errorMsg += " \n maybe you have download complete.";
                }
                publishProgress(
                        UPDATE_FAILURE,
                        new Exception(response.message()), UPDATE_ERROR,
                        errorMsg);
            } else {
                try {
                    ResponseBody entity = response.body();
                    Object responseBody = null;
                    if (entity != null) {
                        time = SystemClock.uptimeMillis();
                        if (targetUrl != null) {
                            responseBody = mFileEntityHandler.handleEntity(entity,
                                    this, targetUrl, isResume);
                        } else {
                            responseBody = mStrEntityHandler.handleEntity(entity,
                                    this, charset);
                        }

                    }
                    publishProgress(UPDATE_SUCCESS, responseBody);

                } catch (IOException e) {
                    publishProgress(UPDATE_FAILURE, e, UPDATE_ERROR, e.getMessage());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            publishProgress(UPDATE_FAILURE, e, UPDATE_ERROR, e.getMessage());
        }
    }

    @Override
    public void callBack(long count, long current, boolean mustNoticeUI) {
        if (callback != null && callback.isProgress()) {
            if (mustNoticeUI) {
                onProgressUpdate(UPDATE_LOADING, count, current);
            } else {
                long thisTime = SystemClock.uptimeMillis();
                if (thisTime - time >= callback.getRate()) {
                    time = thisTime;
                    onProgressUpdate(UPDATE_LOADING, count, current);
                }
            }
        }
    }

}
