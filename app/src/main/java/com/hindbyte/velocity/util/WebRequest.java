package com.hindbyte.velocity.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class WebRequest {

    public final int GET = 1;
    public final int POST = 2;
    public final String Encoding = "UTF-8";

    public WebRequest() {
    }

    public String makeWebServiceCall(String url, int requestMethod) {
        return this.makeWebServiceCall(url, requestMethod, null);
    }

    private String makeWebServiceCall(String urlAddress, int requestMethod, HashMap<String, String> params) {
        URL url;
        StringBuilder response = new StringBuilder();
        try {
            url = new URL(urlAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setDoInput(true);
            if (requestMethod == POST) {
                conn.setRequestMethod("POST");
            } else if (requestMethod == GET) {
                conn.setRequestMethod("GET");
            }
            if (params != null) {
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, Encoding));
                StringBuilder result = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        result.append("&");
                    }
                    result.append(URLEncoder.encode(entry.getKey(), Encoding));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), Encoding));
                }

                writer.write(result.toString());
                writer.flush();
                writer.close();
                os.close();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            } else {
                response = new StringBuilder();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }

}