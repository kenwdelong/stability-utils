package com.kendelong.util.http;

import org.apache.http.impl.client.CloseableHttpClient;

public interface IHttpClientStrategy
{
   public CloseableHttpClient getHttpClient();
}
