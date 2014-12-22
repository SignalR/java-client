/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import microsoft.aspnet.signalr.client.Constants;
import microsoft.aspnet.signalr.client.Credentials;

/**
 * Represents credentials based on cookie values
 */
public class CookieCredentials implements Credentials {

    private Map<String, String> mCookieValues;

    /**
     * Creates a new instance
     */
    public CookieCredentials() {
        mCookieValues = new HashMap<String, String>();
    }

    public CookieCredentials(String cookie) {
        mCookieValues = new HashMap<String, String>();

        if (cookie != null) {
            cookie = cookie.trim();

            if (!cookie.trim().equals("")) {
                String[] keyValues = cookie.split(";");
                for (int i = 0; i < keyValues.length; i++) {
                    String[] parts = keyValues[i].split("=");
                    try {
                        addCookie(URLDecoder.decode(parts[0], Constants.UTF8_NAME), URLDecoder.decode(parts[1], Constants.UTF8_NAME));
                    } catch (UnsupportedEncodingException e) {
                    }
                }
            }
        }
    }

    /**
     * Adds a cookie to the credential
     * 
     * @param name
     *            The cookie name
     * @param value
     *            The cookie value
     */
    public void addCookie(String name, String value) {
        mCookieValues.put(name, value);
    }

    /**
     * Removes a cookie from the credential
     * 
     * @param name
     *            The cookie name
     */
    public void removeCookie(String name) {
        mCookieValues.remove(name);
    }

    @Override
    public void prepareRequest(Request request) {
        if (mCookieValues.size() > 0) {
            StringBuilder currentCookies = new StringBuilder();
            if (request.getHeaders().containsKey("Cookie")) {
                currentCookies.append(request.getHeaders().get("Cookie"));

                currentCookies.append("; ");
            }

            currentCookies.append(this.toString());

            request.removeHeader("Cookie");
            request.addHeader("Cookie", currentCookies.toString());
        }
    }

    @Override
    public String toString() {
        if (mCookieValues.size() > 0) {
            StringBuilder sb = new StringBuilder();

            for (String key : mCookieValues.keySet()) {
                try {
                    sb.append(URLEncoder.encode(key, Constants.UTF8_NAME));
                    sb.append("=");
                    sb.append(URLEncoder.encode(mCookieValues.get(key), Constants.UTF8_NAME));
                    sb.append(";");
                } catch (UnsupportedEncodingException e) {
                }
            }

            return sb.toString();
        } else {
            return "";
        }
    }
}
