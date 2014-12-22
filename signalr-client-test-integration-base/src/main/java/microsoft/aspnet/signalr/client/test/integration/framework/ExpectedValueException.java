/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration.framework;

public class ExpectedValueException extends Exception {

	private static final long serialVersionUID = -1566510176488795332L;

	public ExpectedValueException(Object expected, Object actual) {
		super(String.format("Expected %s - Actual %s", expected.toString(), actual.toString()));

	}
}
