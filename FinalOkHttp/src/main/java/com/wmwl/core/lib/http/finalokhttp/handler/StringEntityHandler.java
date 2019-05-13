package com.wmwl.core.lib.http.finalokhttp.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;

public class StringEntityHandler {

	public Object handleEntity(ResponseBody response, EntityCallBack callback,
			String charset) throws IOException {
		if (response == null)
			return null;

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];

		long count = response.contentLength();
		long curCount = 0;
		int len = -1;
		InputStream is = response.byteStream();
		while ((len = is.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
			curCount += len;
			if (callback != null)
				callback.callBack(count, curCount, false);
		}
		if (callback != null)
			callback.callBack(count, curCount, true);
		byte[] data = outStream.toByteArray();
		outStream.close();
		is.close();
		return new String(data, charset);
	}

}
