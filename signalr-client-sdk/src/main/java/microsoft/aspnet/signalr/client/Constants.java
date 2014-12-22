/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

import java.nio.charset.Charset;

/**
 * Constants used through the framework
 */
public class Constants {

    /**
     * HTTP GET Verb
     */
    public static final String HTTP_GET = "GET";

    /**
     * HTTP POST Verb
     */
    public static final String HTTP_POST = "POST";

    /**
     * UTF-8 Encoding name
     */
    public static final String UTF8_NAME = "UTF-8";

    /**
     * UTF-8 Charset instance
     */
    public static final Charset UTF8 = Charset.forName(UTF8_NAME);
}
