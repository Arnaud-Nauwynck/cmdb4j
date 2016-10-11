package org.cmdb4j.overthere.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ByteArrayOutputStreamConsumer implements Runnable {

	private InputStream stream;
	private ByteArrayOutputStream buffer = new ByteArrayOutputStream(); 
	
	// ------------------------------------------------------------------------

	public ByteArrayOutputStreamConsumer(InputStream stream) {
		this.stream = stream;
	}

	// ------------------------------------------------------------------------

	public Future<?> submitConsumeStream(ExecutorService executorService) {
		return executorService.submit(this);
	}
	
	public void waitFinishConsumeStream(Future<?> future) {
		try {
			future.get(10, TimeUnit.SECONDS);
		} catch (Exception e) {
			throw new RuntimeException("Failed to consume");
		}
	}
	
	
	public void runConsumeStream(ExecutorService executorService) {
		Future<?> future = executorService.submit(this);
		try {
			future.get();
		} catch (InterruptedException e) {
			throw new RuntimeException("interrupted");
		} catch (ExecutionException e) {
			throw new RuntimeException("Failed", e);
		}
	}
	
	@Override
	public void run() {
		if (stream == null) {
			return;
		}
		
		try {
			for(;;) {
				// int available = stream.available();
				int b = stream.read();
				if (b == -1) {
					break;
				}
				buffer.write(b);
			}
		} catch (IOException ex) {
			throw new RuntimeException("Failed to read stream", ex);
		} finally {
			try {
				stream.close();
			} catch(IOException ex) {
				// best effort, no rethrow!
			}
			stream = null;
		}
	}

	public byte[] getBuffer() {
		return buffer.toByteArray();
	}

	public String getBufferAsText() {
		return buffer.toString();
	}

	public String getBufferAsText(String charsetName) throws UnsupportedEncodingException {
		return buffer.toString(charsetName);
	}
	
}
