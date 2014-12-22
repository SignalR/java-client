/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.http;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Represents an HTTP Response
 */
public interface Response {

    /**
     * Returns the response headers
     */
    public Map<String, List<String>> getHeaders();

    /**
     * Return the header value
     * 
     * @param headerName
     *            Header to retrieve
     * @return The header Values
     */
    public List<String> getHeader(String headerName);

    /**
     * Reads the response stream to the end and returns its value as a String
     * 
     * @return The response content as a String
     * @throws java.io.IOException
     */
    public String readToEnd() throws IOException;

    /**
     * Reads one line from the response stream
     * 
     * @return A line from the response stream
     * @throws java.io.IOException
     */
    public String readLine() throws IOException;

    /**
     * Returns the response HTTP Status code
     */
    public int getStatus();

    /**
     * Reads the response stream to the end and returns its value as a Byte[]
     * 
     * @return The response content as a Byte[]
     * @throws java.io.IOException
     */
    public byte[] readAllBytes() throws IOException;
}
