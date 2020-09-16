package org.celllife.idart.rest;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 
 * @author Victor Aravena victor.aravena@ciochile.cl
 *
 */

public class ApiAuthRest {

	static String username = null;
	static String password = null;
	static String URLBase = null;
	static String URLReportingBase = null;

    public final static Logger log = Logger.getLogger(ApiAuthRest.class);
	/**
	 * HTTP POST
	 * @param URLPath
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static Boolean getRequestPost(String URLPath, StringEntity input) throws Exception {
        String URL = URLBase + URLPath;
        Boolean response =  false;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
        	HttpPost httpPost = new HttpPost(URL);
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            BasicScheme scheme = new BasicScheme();
            Header authorizationHeader = scheme.authenticate(credentials, httpPost);
            httpPost.setHeader(authorizationHeader);
            httpPost.setEntity(input);
            HttpResponse responseRequest = httpclient.execute(httpPost);
            
    		if (responseRequest.getStatusLine().getStatusCode() != 204 && responseRequest.getStatusLine().getStatusCode() != 201) {
    			throw new RuntimeException("POST Failed : HTTP error code : "
    				+ responseRequest.getStatusLine().getStatusCode());
    		}
    		httpclient.getConnectionManager().shutdown();
    		response = true;
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return response;
    }

    /**
	 * HTTP GET
	 * @param URLPath
	 * @return
	 * @throws Exception
	 */
	public static String getRequestGet(String URLPath) throws Exception {
        String URL = URLBase + URLPath;
        String response =  "";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            HttpGet httpGet = new HttpGet(URL);
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            BasicScheme scheme = new BasicScheme();
            Header authorizationHeader = scheme.authenticate(credentials, httpGet);
            httpGet.setHeader(authorizationHeader);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            
           log.trace("GET Executing request: " + httpGet.getRequestLine());
           log.trace(response);
            response = httpclient.execute(httpGet,responseHandler);
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return response;
    }

	/**
	 * HTTP GET
	 * @param URLPath
	 * @return
	 * @throws Exception
	 */
	public static String getReportingRequestGet(String URLPath) throws Exception {
        String URL = URLReportingBase + URLPath;
        String response =  "";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            HttpGet httpGet = new HttpGet(URL);

            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            BasicScheme scheme = new BasicScheme();
            Header authorizationHeader = scheme.authenticate(credentials, httpGet);
            httpGet.setHeader(authorizationHeader);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            
           log.trace("Executing request: " + httpGet.getRequestLine());
           log.trace(response);
            response = httpclient.execute(httpGet,responseHandler);
            
           
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return response;
    }

    /**
     * POSTGREST HTTP GET
     * @param URLPath
     * @return
     * @throws Exception
     */
    public static HttpResponse postgrestRequestGetAll(String URLPath, String jwtoken,PoolingHttpClientConnectionManager pool) {
        String URL = URLPath;
        CloseableHttpResponse response =  null;
pool = new PoolingHttpClientConnectionManager();
        pool.setDefaultMaxPerRoute(1);
        pool.setMaxTotal(1);

        final CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(pool).build();

        try {
            HttpGet httpGet = new HttpGet(URL);
            httpGet.setHeader("Authorization"," Bearer "+jwtoken);

           log.trace("GET Executing request: " + httpGet.getRequestLine());
            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
//            EntityUtils.consume(entity);
//            response.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static StringBuilder postgrestRequestGetBuffer(String URLPath, String jwtoken, PoolingHttpClientConnectionManager pool) throws Exception {
        String URL = URLPath;
        CloseableHttpResponse response =  null;
        BufferedReader reader = null;
        String line = null;
        StringBuilder str = null;
        pool = new PoolingHttpClientConnectionManager();
        pool.setDefaultMaxPerRoute(1);
        pool.setMaxTotal(1);

        final CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(pool).build();

        try {
            HttpGet httpGet = new HttpGet(URL);

            httpGet.setHeader("Authorization"," Bearer "+jwtoken);

           log.trace("GET Executing request: " + httpGet.getRequestLine());
            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
//            EntityUtils.consume(entity);

            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));

            str = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                str.append(line + "\n");
            }
//           response.close();
    }catch (Exception e) {
        e.printStackTrace();
    }
        return str;
    }


    public static StringBuilder postgrestRequestPostBuffer(String URLPath, StringEntity input, PoolingHttpClientConnectionManager pool) throws Exception {
        String URL = URLPath;
        CloseableHttpResponse response =  null;
        BufferedReader reader = null;
        String line = null;
        StringBuilder str = null;
        pool = new PoolingHttpClientConnectionManager();
        pool.setDefaultMaxPerRoute(1);
        pool.setMaxTotal(1);

        final CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(pool).build();

        try {
            HttpPost httpPost = new HttpPost(URL);
            httpPost.setEntity(input);
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();

            log.trace("GET Executing request: " + httpPost.getRequestLine());
            reader = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));

            str = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                str.append(line + "\n");
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * POSTGREST HTTP GET
     * @param URLPath
     * @return
     * @throws Exception
     */
    public static HttpResponse postgrestRequestGet(String URLPath,String jwtoken, PoolingHttpClientConnectionManager pool) throws Exception {
        String URL = URLPath;
        CloseableHttpResponse response = null;
        pool.setDefaultMaxPerRoute(1);
        pool.setMaxTotal(1);
        final CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(pool).build();

        try {
            HttpGet httpGet = new HttpGet(URL);
            httpGet.setHeader("Authorization"," Bearer "+jwtoken);
            response = httpclient.execute(httpGet);
//            HttpEntity entity = response.getEntity();
//            EntityUtils.consume(entity);
           log.trace("GET Executing request: " + httpGet.getRequestLine());
           log.trace(response);

//            response.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * POSTGREST HTTP POST
     * @param URLPath
     * @param input
     * @return
     * @throws Exception
     */
    public static HttpResponse postgrestRequestPost(String URLPath, StringEntity input, String jwtoken, PoolingHttpClientConnectionManager pool) throws Exception {
        String URL = URLPath;
        CloseableHttpResponse response = null;
        pool.setDefaultMaxPerRoute(1);
        pool.setMaxTotal(1);

        final CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(pool).build();

        try {
            HttpPost httpPost = new HttpPost(URL);
            httpPost.setHeader("Authorization"," Bearer "+jwtoken);
            httpPost.setEntity(input);
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
            if (response.getStatusLine().getStatusCode() != 204 && response.getStatusLine().getStatusCode() != 201) {
               log.trace(("POST Failed : HTTP error code : "+ response.getStatusLine().getStatusCode()));
            }
            response.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * POSTGREST HTTP PATCH
     * @param URLPath
     * @param input
     * @return
     * @throws Exception
     */
    public static HttpResponse postgrestRequestPatch(String URLPath, StringEntity input, String jwtoken, PoolingHttpClientConnectionManager pool) throws Exception {
        String URL = URLPath;
        CloseableHttpResponse response = null;
        pool.setDefaultMaxPerRoute(1);
        pool.setMaxTotal(1);

        final CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(pool).build();

        try {
            HttpPatch httpPatch = new HttpPatch(URL);
            httpPatch.setHeader("Authorization"," Bearer "+jwtoken);
            httpPatch.setEntity(input);
            response = httpclient.execute(httpPatch);
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);

            if (response.getStatusLine().getStatusCode() != 204 && response.getStatusLine().getStatusCode() != 201) {
               log.trace(("PATCH Failed : HTTP error code : "+ response.getStatusLine().getStatusCode()));
            }
//            httpclient.getConnectionManager().shutdown();
            response.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * POSTGREST HTTP PUT
     * @param URLPath
     * @param input
     * @return
     * @throws Exception
     */
    public static HttpResponse postgrestRequestPut(String URLPath, StringEntity input, String jwtoken,PoolingHttpClientConnectionManager pool) throws Exception {
        String URL = URLPath;
        CloseableHttpResponse response = null;
        pool.setDefaultMaxPerRoute(1);
        pool.setMaxTotal(1);

        final CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(pool).build();

        try {
            HttpPut httpPut = new HttpPut(URL);
            httpPut.setHeader("Authorization"," Bearer "+jwtoken);
            httpPut.setEntity(input);
            response = httpclient.execute(httpPut);

            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);

            if (response.getStatusLine().getStatusCode() != 204 && response.getStatusLine().getStatusCode() != 201) {
               log.trace(("PUT Failed : HTTP error code : "+ response.getStatusLine().getStatusCode()));
            }
            response.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * POSTGREST HTTP DELETE
     * @param URLPath
     * @return
     * @throws Exception
     */
    public static HttpResponse postgrestRequestDelete(String URLPath, String jwtoken,PoolingHttpClientConnectionManager pool) throws Exception {
        String URL = URLPath;
        CloseableHttpResponse response = null;
        pool.setDefaultMaxPerRoute(1);
        pool.setMaxTotal(1);

        final CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(pool).build();

        try {
            HttpDelete httpDelete = new HttpDelete(URL);
            httpDelete.setHeader("Authorization"," Bearer "+jwtoken);
            response = httpclient.execute(httpDelete);

            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);

           log.trace("DELETE Executing request: " + httpDelete.getRequestLine());
           log.trace(response);

            response.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String getServerStatus(String url) throws IOException {

        String result = "";
        int code = 200;
        try {
            URL siteURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.connect();

            code = connection.getResponseCode();
            connection.disconnect();
            if (code == 200) {
                result = "-> Green <-\t" + "Code: " + code;
            } else {
                result = "-> Yellow <-\t" + "Code: " + code;
            }
        } catch (Exception e) {
            result = "-> Red <-\t" + "Wrong domain - Exception: " + e.getMessage();
        }

        return result;
    }

	public static void setUsername(String username) {
		ApiAuthRest.username = username;
	}

	public static void setPassword(String password) {
		ApiAuthRest.password = password;
	}

	public static void setURLBase(String uRLBase) {
		URLBase = uRLBase;
	}
	
	public static void setURLReportingBase(String uRLReportingBase) {
		URLReportingBase = uRLReportingBase;
	}
  
}
