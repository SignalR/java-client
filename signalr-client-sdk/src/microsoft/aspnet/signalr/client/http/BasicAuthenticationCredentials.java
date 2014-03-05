/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.http;

import microsoft.aspnet.signalr.client.Credentials;

/**
 * Credentials implementation for HTTP Basic Authentication
 */
public class BasicAuthenticationCredentials implements Credentials {
    private String mUsername;
    private String mPassword;
    private Base64Encoder mEncoder;

    /**
     * Creates a BasicAuthenticationCredentials instance with a username,
     * password and an encoder
     * 
     * @param username
     *            The username for the credentials
     * @param password
     *            The password for the credentials
     * @param encoder
     *            The Base64 encoder to use
     */
    public BasicAuthenticationCredentials(String username, String password, Base64Encoder encoder) {
        initialize(username, password, encoder);
    }

    /**
     * Initializes a BasicAuthenticationCredentials instance with a username and
     * a password
     * 
     * @param username
     *            The username for the credentials
     * @param password
     *            The password for the credentials
     * @param encoder
     *            The Base64 encoder to use
     */
    private void initialize(String username, String password, Base64Encoder encoder) {
        mUsername = username;
        mPassword = password;
        mEncoder = encoder;

        if (encoder == null) {
            throw new IllegalArgumentException("encoder");
        }
    }

    /**
     * Returns the username for the credentials
     */
    public String getUsername() {
        return mUsername;
    }

    /**
     * Sets the username for the credentials
     * 
     * @param username
     *            username to set
     */
    public void setUsername(String username) {
        mUsername = username;
    }

    /**
     * Returns the password for the credentials
     */
    public String getPassword() {
        return mPassword;
    }

    /**
     * Sets the password for the credentials
     * 
     * @param password
     *            password for the credentials
     */
    public void setPassword(String password) {
        mPassword = password;
    }

    @Override
    public void prepareRequest(Request request) {
        String headerValue = mUsername + ":" + mPassword;

        headerValue = mEncoder.encodeBytes(headerValue.getBytes()).trim();

        request.addHeader("Authorization", "Basic " + headerValue);
    }

    /**
     * Represents a Base64Encoder
     */
    public interface Base64Encoder {
        /**
         * Encodes a byte array
         * 
         * @param bytes
         *            Bytes to encode
         * @return The encoded bytes
         */
        public String encodeBytes(byte[] bytes);
    }

    public class InvalidPlatformException extends Exception {
        private static final long serialVersionUID = 1975952258601813204L;
    }
}
