package com.wmwl.core.lib.http.finalokhttp;

import android.util.Log;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;


import okhttp3.ConnectionPool;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http2.Header;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

public class FinalOkHttp {
	
	private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8 * 1024; // 8KB
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String HEADER_USER_AGENT = "User-Agent" ;
	private static final String ENCODING_GZIP = "gzip";
	private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

	private static int maxConnections = 10; // http请求最大并发连接数
	private static int socketTimeout = 15; // 超时时间，默认10秒
	private static int connectTimeout = 15; // 连接超时时间，默认10秒
	private static int connectionPoolTime = 10000; // 超时时间，默认1秒,连接管理器配置超时
	private static int keepAliveDuration = 15;//连接存活时间
	private static int maxRetries = 0;// 错误尝试次数，错误异常表请在RetryHandler添加  suzb原本为0次
	
	
	protected final OkHttpClient.Builder clientBuilder;
//	protected final Request request;
	protected final Request.Builder requestBuilder;
	protected Request request;
	protected OkHttpClient client;
	protected String charset = "utf-8";
	
	public FinalOkHttp(){
		requestBuilder = new Request.Builder();
		
		clientBuilder = new OkHttpClient.Builder();
		ConnectionPool connectionPool = new ConnectionPool(maxConnections, keepAliveDuration, TimeUnit.SECONDS);
		clientBuilder.connectionPool(connectionPool);
		clientBuilder.connectTimeout(connectTimeout, TIMEOUT_UNIT);
		clientBuilder.readTimeout(socketTimeout, TIMEOUT_UNIT);
		
		SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		//默认所有证书都信任
		clientBuilder.sslSocketFactory(socketFactory,new X509TrustManager() {
			
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
			
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
				
			}
			
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
				
			}
		});
		clientBuilder.hostnameVerifier(new HostnameVerifier() {
			
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
		clientBuilder.addNetworkInterceptor(new Interceptor() {
			
			@Override
			public Response intercept(Chain chain) throws IOException {
				Request request = chain.request();
				if (!request.headers().names().contains(HEADER_ACCEPT_ENCODING)) {
						request = request.newBuilder()
			                .header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP)
			                .method(request.method(), new RequestBodyGzip(request.body()))
			                .build();
				}
//				// http请求头添加包名和版本号信息
				setHeader(request) ;
				if(!request.headers().names().contains(HEADER_USER_AGENT)){
//					request.newBuilder().addHeader(HEADER_USER_AGENT, AppConfiguration.USER_AGENT);
//					LoggerUtil.d("lhjtianji", "http增加user-agent") ;
				}
				
				return chain.proceed(request);
			}
		});
		client = clientBuilder.build();
	}
	
	/**
	 * 设置请求头
	 * */
	private void setHeader(Request request){
		String insertEncodedType = "application/x-www-form-urlencoded";
		String insertContentType = "application/x-www-form-urlencoded;charset=UTF-8";
		if(!request.headers().names().contains(insertEncodedType)){
			request = request.newBuilder().header(insertContentType, "").build();
		}else{
			request = request.newBuilder().addHeader(insertContentType, "").build();
		}
	}
	
	public void configCharset(String charSet) {
		if (charSet != null && charSet.trim().length() != 0)
			this.charset = charSet;
	}

	public void configCookieStore(CookieJar cookieJar) {
		this.clientBuilder.cookieJar(cookieJar);
		this.client = clientBuilder.build();
	}

	public void configUserAgent(String userAgent) {
		this.requestBuilder.removeHeader(HEADER_USER_AGENT).addHeader(HEADER_USER_AGENT, userAgent);
		this.request = requestBuilder.build();
	}
	
	/**
	 * 设置网络连接超时时间(单位：秒)，默认为10秒钟
	 * @param timeout
	 */
	public void configTimeout(int timeout){
		this.clientBuilder.connectTimeout(timeout, TimeUnit.SECONDS);
		this.clientBuilder.readTimeout(timeout, TimeUnit.SECONDS);
		this.client = clientBuilder.build();
	}
	
	/**
	 * 设置网络连接超时时间，默认为10秒钟
	 * 
	 * @param timeout
	 */
	public void configTimeout(int timeout,TimeUnit unit) {
		this.clientBuilder.connectTimeout(timeout, unit);
		this.clientBuilder.readTimeout(timeout, unit);
		this.client = clientBuilder.build();
	}
	
	/**
	 * 设置https请求时 的 SSLSocketFactory
	 * 
	 * @param sslSocketFactory
	 */
	public void configSSLSocketFactory(SSLSocketFactory sslSocketFactory,X509TrustManager trust){
		this.clientBuilder.sslSocketFactory(sslSocketFactory,trust);
		this.client = clientBuilder.build();
	}
	
	/**
	 * 设置https请求时 的 校验域名
	 * 
	 * @param sslSocketFactory
	 */
	public void configHostnameVerifier(HostnameVerifier verifier){
		this.clientBuilder.hostnameVerifier(verifier);
		this.client = clientBuilder.build();
	}
	
	/**
	 * 配置错误重试次数
	 * 
	 * @param retry
	 */
	public void configRequestExecutionRetryCount(int maxRetry) {
		if(maxRetry>1){
			this.clientBuilder.retryOnConnectionFailure(true);
			this.clientBuilder.addInterceptor(new RetryConnectionInterceptor(maxRetry));
		}else{
			this.clientBuilder.retryOnConnectionFailure(false);
		}
		this.client = clientBuilder.build();
	}

	/**
	 * 添加http请求头
	 * 
	 * @param header
	 * @param value
	 */
	public void addHeader(String header, String value) {
		this.requestBuilder.addHeader(header, value);
		this.request = requestBuilder.build();
	}
	
	public void get(String url,AjaxCallBack<? extends Object> callBack){
		this.get(url, null,callBack);
	}

	public void get(String url, AjaxParams params,AjaxCallBack<? extends Object> callBack){
		this.client = this.clientBuilder.build();
		this.requestBuilder.url(getUrlWithQueryString(url,params));
		this.request = this.requestBuilder.build();
		this.get(url, null,params,callBack);
	}

	public void get(String url,Headers headers, AjaxParams params,AjaxCallBack<? extends Object> callBack){
		this.client = this.clientBuilder.build();
		this.requestBuilder.url(getUrlWithQueryString(url,params));

		if(null!=headers){
			for(int i=0;i<headers.size();i++){
				this.requestBuilder.addHeader(headers.name(i), headers.value(i));
			}
		}
		this.request = this.requestBuilder.build();
		sendRequest(client,request,callBack);
	}

	public void post(String url,AjaxCallBack<? extends Object> callBack){
		this.post(url, null,callBack);
	}

	public void post(String url, AjaxParams params,AjaxCallBack<? extends Object> callBack){
		Log.d("WMWL","11111111111111111111111111");
		this.client = this.clientBuilder.build();
		this.requestBuilder.url(url);
		if(null!=params){
			this.requestBuilder.post(params.getBody());
		}
		this.request = this.requestBuilder.build();
		this.post(url, null,params,callBack);
	}

	public void post(String url,Headers headers, AjaxParams params,AjaxCallBack<? extends Object> callBack){
		Log.d("WMWL","2222222222222222222222222222");
		this.client = this.clientBuilder.build();
		this.requestBuilder.url(url);
		if(null!=params){
			this.requestBuilder.post(params.getBody());
		}
		if(null!=headers){
			for(int i=0;i<headers.size();i++){
				this.requestBuilder.addHeader(headers.name(i), headers.value(i));
			}
		}
		this.request = this.requestBuilder.build();
		sendRequest(client,request,callBack);
	}
	
	protected <T> void sendRequest(OkHttpClient client,Request request, AjaxCallBack<T> callBack){
		Log.d("WMWL","333333333333333333333333333");
		new HttpHandler<T>(client,request,charset,callBack).execute();
	}
	
	public String getUrlWithQueryString(String url, AjaxParams params) {
		if (params != null) {
			String paramString = params.getParamString();
			url += "?" + paramString;
		}
		return url;
	}
	protected RequestBody paramsToBody(AjaxParams params) {
		RequestBody entity = null;

		if (params != null) {
			entity = params.getBody();
		}

		return entity;
	}
	
	private static class RequestBodyGzip extends RequestBody {
		
		private RequestBody body;
		
		public RequestBodyGzip(){
			this(null);
		}
		
		public RequestBodyGzip(RequestBody body){
			this.body = body;
		}

		@Override
		public MediaType contentType() {
			if(null!=body){
				body.contentType();
			}
			return MediaType.parse("Content-Encoding:gzip");
		}

		@Override
		public void writeTo(BufferedSink sink) throws IOException {
			 BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
             body.writeTo(gzipSink);
             gzipSink.close();
		}

		@Override
		public long contentLength() throws IOException {
			return -1;
		}
		
	}

}
