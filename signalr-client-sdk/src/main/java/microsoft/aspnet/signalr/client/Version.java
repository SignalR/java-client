/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

/**
 * Represents a Version of a Product or Library
 */
public class Version {

    int[] mParts;

    /**
     * Initializes the Version
     * 
     * @param version
     *            A string representing a version
     */
    public Version(String version) {
        try {
            String[] parts = version.split("\\.");
            mParts = new int[parts.length];

            for (int i = 0; i < parts.length; i++) {
                mParts[i] = Integer.parseInt(parts[i]);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(version);
        }
    }

    /**
     * Returns a part of the version
     * 
     * @param index
     *            Zero-based index for the version parts
     */
    public int getPart(int index) {
        return mParts[index];
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof Version)) {
            return false;
        }

        Version v2 = (Version) o;

        if (v2.mParts.length != mParts.length) {
            return false;
        }

        for (int i = 0; i < mParts.length; i++) {
            if (mParts[i] != v2.mParts[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < mParts.length; i++) {
            if (i != 0) {
                sb.append(".");
            }

            int part = mParts[i];
            sb.append(part);
        }

        return sb.toString().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < mParts.length; i++) {
            if (i != 0) {
                sb.append(".");
            }

            sb.append(mParts[i]);
        }

        return sb.toString();
    }
}
