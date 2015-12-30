package com.kendelong.util.http;

import java.util.Map;

public interface IHttpConnectionService
{
	
	/**
	 * This method accepts an HTTP connection URL, input data, and a content type
	 * to be set in the header of the Post.
	 * It returns any response back to the client
	 * 
	 * @param connectionURL is the HTTP URL to connect to.
	 * @param data in a form matching the content type 
	 * @param contentType is the String to be set as the Content-Type HTTP header 
	 * @return the response
	 * @throws Exception currently logs a warn on an exception and throws it back to the client 
	 */
	public HttpResponseObject postStringAsRequestEntity(String connectionURL, String data, String contentType) throws Exception;
	public HttpResponseObject postStringAsRequestEntity(String connectionURL, String data, String contentType, Map<String, String> headers) throws Exception;
	
	/**
	 * This method accepts an HTTP connection URL and input data.
	 * It will label the content type as "text/xml" and send in a Post. 
	 * It returns any response back to the client
	 * 
	 * @param connectionURL is the HTTP URL to connect to.
	 * @param data in the form of XML specifying the request
	 * @return the response
	 * @throws Exception currently logs a warn on an exception and throws it back to the client 
	 */
	public HttpResponseObject postStringAsXml(String connectionURL, String data) throws Exception;

	public HttpResponseObject postStringAsJson(String connectionURL, String data) throws Exception;

	/**
	 * This method accepts an HTTP connection URL and input data as a Map
	 * of parameter names and values.
	 * It will send the data in the body of a Post as a series of name/value pairs. 
	 * It returns any response back to the client
	 * 
	 * @param connectionURL is the HTTP URL to connect to.
	 * @param parameters data used to form the query string
	 * @return the response
	 * @throws Exception currently logs a warn on an exception and throws it back to the client 
	 */
	public HttpResponseObject postData(String connectionURL, Map<String, String> parameters) throws Exception;
	
	/**
	 * Does an HTTP GET and returns the result
	 * @param connectionUrl The URL to POST the data to
	 * @param parameters data used to form the query string
	 * @return the response body
	 * @throws Exception currently logs a warn on an exception and throws it back to the client 
	 */
	public HttpResponseObject getResult(String connectionUrl, Map<String, String> parameters) throws Exception;
	public HttpResponseObject getResult(String connectionUrl, Map<String, String> parameters, Map<String, String> headers) throws Exception;

	public HttpResponseObject simpleGet(String url) throws Exception;

	
}
