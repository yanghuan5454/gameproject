package com.wmwl.core.lib.http.finalokhttp;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryConnectionInterceptor implements Interceptor {
	
	public int maxRetry;
	public int retryNum;
	
	public RetryConnectionInterceptor(int maxRetry){
		this.maxRetry = maxRetry;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		
		Request request = chain.request();
        Response response;
        do {
            response = chain.proceed(request);
            retryNum++;
        }while(!response.isSuccessful() && retryNum < maxRetry);
        return response;
	}

}
