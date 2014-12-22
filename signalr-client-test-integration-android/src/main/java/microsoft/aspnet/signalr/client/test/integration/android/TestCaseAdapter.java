/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration.android;

import microsoft.aspnet.signalr.client.android.test.integration.R;
import microsoft.aspnet.signalr.client.test.integration.framework.TestCase;
import microsoft.aspnet.signalr.client.test.integration.framework.TestStatus;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

/**
 * Adapter to bind a ToDoItem List to a view
 */
public class TestCaseAdapter extends ArrayAdapter<TestCase> {

	/**
	 * Adapter context
	 */
	Context mContext;

	/**
	 * Adapter View layout
	 */
	int mLayoutResourceId;

	public TestCaseAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);

		mContext = context;
		mLayoutResourceId = layoutResourceId;
	}

	/**
	 * Returns the view for a specific item on the list
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;

		final TestCase testCase = getItem(position);

		if (row == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(mLayoutResourceId, parent, false);
		}

		final CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkTestCase);

		String text = String.format("%s - %s", testCase.getName(), testCase.getStatus().toString());

		if (testCase.getStatus() == TestStatus.Failed) {
			checkBox.setTextColor(Color.RED);
		} else if (testCase.getStatus() == TestStatus.Passed) {
			checkBox.setTextColor(Color.GREEN);
		} else {
			checkBox.setTextColor(Color.BLACK);
		}

		checkBox.setText(text);
		checkBox.setChecked(testCase.isEnabled());

		checkBox.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				testCase.setEnabled(checkBox.isChecked());
			}
		});

		return row;
	}

}
