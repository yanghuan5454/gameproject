package com.wmwl.core.lib.http.finalokhttp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;


/**
 * <p>
 * 使用方法:
 * <p>
 * 
 * <pre>
 * AjaxParams params = new AjaxParams();
 * params.put(&quot;username&quot;, &quot;michael&quot;);
 * params.put(&quot;password&quot;, &quot;123456&quot;);
 * params.put(&quot;email&quot;, &quot;test@tsz.net&quot;);
 * params.put(&quot;profile_picture&quot;, new File(&quot;/mnt/sdcard/pic.jpg&quot;)); // 上传文件
 * params.put(&quot;profile_picture2&quot;, inputStream); // 上传数据流
 * params.put(&quot;profile_picture3&quot;, new ByteArrayInputStream(bytes)); // 提交字节流
 * 
 * FinalHttp fh = new FinalHttp();
 * fh.post(&quot;http://www.yangfuhai.com&quot;, params, new AjaxCallBack&lt;String&gt;() {
 * 	&#064;Override
 * 	public void onLoading(long count, long current) {
 * 		textView.setText(current + &quot;/&quot; + count);
 * 	}
 * 
 * 	&#064;Override
 * 	public void onSuccess(String t) {
 * 		textView.setText(t == null ? &quot;null&quot; : t);
 * 	}
 * });
 * </pre>
 */
public class AjaxParams {
	private static String ENCODING = "UTF-8";

	protected ConcurrentHashMap<String, String> urlParams;
	protected ConcurrentHashMap<String, FileWrapper> fileParams;

	public AjaxParams() {
		init();
	}

	public AjaxParams(Map<String, String> source) {
		init();

		for (Map.Entry<String, String> entry : source.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	public AjaxParams(String key, String value) {
		init();
		put(key, value);
	}

	public AjaxParams(Object... keysAndValues) {
		init();
		int len = keysAndValues.length;
		if (len % 2 != 0)
			throw new IllegalArgumentException(
					"Supplied arguments must be even");
		for (int i = 0; i < len; i += 2) {
			String key = String.valueOf(keysAndValues[i]);
			String val = String.valueOf(keysAndValues[i + 1]);
			put(key, val);
		}
	}

	public void put(String key, String value) {
		if (key != null && value != null) {
			urlParams.put(key, value);
		}
	}

	public void put(String key, File file) throws FileNotFoundException {
		put(key, file, file.getName());
	}
	
	public void put(String key, File file, String fileName) {
		put(key, file, fileName, null);
	}

	public void put(String key, byte[] contentByte) {
		put(key, contentByte, null);
	}

	public void put(String key, byte[] contentByte, String fileName) {
		put(key, contentByte, fileName, null);
	}

	/**
	 * 添加 contentByte 到请求中.
	 * 
	 * @param key
	 *            the key name for the new param.
	 * @param contentByte
	 *            the content bytes to add.
	 * @param fileName
	 *            the name of the file.
	 * @param contentType
	 *            the content type of the file, eg. application/json
	 */
	public void put(String key, byte[] contentByte, String fileName,
			String contentType) {
		if (key != null && contentByte != null) {
			fileParams.put(key, new FileWrapper(contentByte, fileName, contentType));
		}
	}
	
	/**
	 * 添加 file 到请求中
	 * @param key
	 * @param file
	 * @param fileName
	 * @param contentType
	 */
	public void put(String key, File file, String fileName,
			String contentType) {
		if (key != null && file != null) {
			fileParams.put(key, new FileWrapper(file, fileName, contentType));
		}
	}

	public void remove(String key) {
		urlParams.remove(key);
		fileParams.remove(key);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (ConcurrentHashMap.Entry<String, String> entry : urlParams
				.entrySet()) {
			if (result.length() > 0)
				result.append("&");

			result.append(entry.getKey());
			result.append("=");
			result.append(entry.getValue());
		}

		for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams
				.entrySet()) {
			if (result.length() > 0)
				result.append("&");

			result.append(entry.getKey());
			result.append("=");
			result.append("FILE");
		}

		return result.toString();
	}

	/**
	 * Returns an HttpEntity containing all request parameters
	 */
	public RequestBody getBody() {
		RequestBody requestBody = null;

		try {
			if (!fileParams.isEmpty()) {
				RequestBody fileBody = null;
				MultipartBody.Builder builder = new MultipartBody.Builder();

				// Add string params
				for (ConcurrentHashMap.Entry<String, String> entry : urlParams
						.entrySet()) {
					builder.addFormDataPart(entry.getKey(), entry.getValue());
				}

				// Add file params
				for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams
						.entrySet()) {
					FileWrapper file = entry.getValue();
					if (file.contentByte != null) {
						fileBody = MultipartBody.create(MediaType.parse(file.contentType), file.contentByte);
						builder.addFormDataPart(entry.getKey(), file.getFileName(), fileBody);
					}else if(file.file != null){
						fileBody = MultipartBody.create(MediaType.parse(file.contentType), file.file);
						builder.addFormDataPart(entry.getKey(), file.getFileName(), fileBody);
					}
				}

				requestBody = builder.build();
			} else {
				requestBody = paramsToFormBody();
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return requestBody;
	}
	
	private FormBody paramsToFormBody(){
		FormBody.Builder builder = new FormBody.Builder(Charset.forName(ENCODING));
		for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
			builder.add(entry.getKey(), entry.getValue());
		}
		return builder.build();
	}

	private void init() {
		urlParams = new ConcurrentHashMap<String, String>();
		fileParams = new ConcurrentHashMap<String, FileWrapper>();
	}


	public String getParamString() {
		StringBuilder str = new StringBuilder();
		FormBody body = paramsToFormBody();
		for(int i=0;i<body.size();i++){
			str.append(body.encodedName(i));
			str.append("=");
			str.append(body.encodedValue(i));
			
			if(i!=body.size()-1) {
				str.append("&");
			}
		}
		return str.toString();
	}

	private static class FileWrapper {
		public byte[] contentByte;
		public String fileName;
		public String contentType;
		public File file;

		public FileWrapper(byte[] contentByte, String fileName,
				String contentType) {
			this.contentByte = contentByte;
			this.fileName = fileName;
			this.contentType = contentType;
		}
		
		public FileWrapper(File file, String fileName,
				String contentType) {
			this.file = file;
			this.fileName = fileName;
			this.contentType = contentType;
		}

		public String getFileName() {
			if (fileName != null) {
				return fileName;
			} else {
				return "nofilename";
			}
		}
	}
}