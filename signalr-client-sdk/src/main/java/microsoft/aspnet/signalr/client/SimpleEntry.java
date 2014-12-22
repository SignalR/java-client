/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

import java.util.Map.Entry;

/**
 * Simple Entry&lt;K,V&gt; implementation
 * 
 * @param <K>
 *            Key
 * @param <V>
 *            Value
 */
public class SimpleEntry<K, V> implements Entry<K, V> {
    K mKey;
    V mValue;

    /**
     * Initializes the SimpleEntry
     * 
     * @param key
     *            Entry key
     * @param value
     *            Entry value
     */
    public SimpleEntry(K key, V value) {
        mKey = key;
        mValue = value;
    }

    @Override
    public K getKey() {
        return mKey;
    }

    @Override
    public V getValue() {
        return mValue;
    }

    @Override
    public V setValue(V value) {
        mValue = value;
        return mValue;
    }
}