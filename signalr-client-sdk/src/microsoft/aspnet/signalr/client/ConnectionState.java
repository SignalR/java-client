/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

/***
 * Represents the state of a connection
 */
public enum ConnectionState {
    Connecting, Connected, Reconnecting, Disconnected
}