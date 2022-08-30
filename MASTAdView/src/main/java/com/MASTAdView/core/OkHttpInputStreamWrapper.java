package com.MASTAdView.core;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

/**
 * A wrapper to close @{@link okhttp3.okhttp3.Response}
 *
 * @author: bedprakash on 17/11/16.
 */

public class OkHttpInputStreamWrapper extends InputStream {
  private final Response response;

  OkHttpInputStreamWrapper(Response response) {
    this.response = response;
  }

  @Override
  public int read() throws IOException {
    return response.body().byteStream().read();
  }

  @Override
  public void close() throws IOException {
    response.close();
  }
}
