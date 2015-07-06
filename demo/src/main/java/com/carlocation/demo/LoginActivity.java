package com.carlocation.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.carlocation.comm.NotificationListener;
import com.carlocation.comm.ResponseListener;
import com.carlocation.comm.UserService;
import com.carlocation.comm.messaging.Notification;

public class LoginActivity extends Activity {
	private static final String LOG_TAG = "LoginActivity";

	private static final String key_setting = "SETTING";

	private EditText field_usrName;
	private EditText field_pasWord;
	private Button button_logIn;

	private UserService mUserService;

	private LocalListener mListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		mListener = new LocalListener();

		field_usrName = (EditText) findViewById(R.id.userName);
		field_pasWord = (EditText) findViewById(R.id.passWord);
		button_logIn = (Button) findViewById(R.id.logIn);

		restoreLogInfo();

		button_logIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (field_usrName.getText().toString().length() == 0) {
					field_usrName.setError("Pls enter UserName!");
					field_usrName.requestFocus();
					return;
				}
				if (field_pasWord.getText().toString().length() == 0) {
					field_pasWord.setError("Pls enter Pwd!");
					field_pasWord.requestFocus();
					return;
				}

				if (mUserService == null) {
					mUserService = new UserService(
							((CarLocationApplication) getApplicationContext())
									.getService(), mListener);
				}
				mUserService.logIn(field_usrName.getText().toString(),
						field_pasWord.getText().toString());

			}

		});
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		saveLogInfo();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void restoreLogInfo(){
		SharedPreferences restore = getSharedPreferences(key_setting,MODE_PRIVATE);
		field_usrName.setText(restore.getString("mUserName",""));
		field_pasWord.setText(restore.getString("mPassWord",""));
	}

	public void saveLogInfo(){
		SharedPreferences save = getSharedPreferences(key_setting,MODE_PRIVATE);
		SharedPreferences.Editor editor = save.edit();
		editor.putString("mUserName", field_usrName.getText().toString());
		editor.putString("mPassWord",field_pasWord.getText().toString());
		editor.commit();
	}


	class LocalListener implements ResponseListener, NotificationListener {

		/**
		 * Unsolicited message notification.
		 * 
		 * @param noti
		 */
		@Override
		public void onNotify(Notification noti) {
			forward(noti);
		}

		@Override
		public void onResponse(Notification noti) {
			forward(noti);
		}

		private void forward(Notification notif) {
			if (notif.notiType == Notification.NotificationType.RESPONSE
					&& notif.result == Notification.Result.SUCCESS) {
				Intent i = new Intent(LoginActivity.this, MainActivity.class);
				startActivity(i);
				finish();
			}else {
                Toast.makeText(LoginActivity.this,"Authentication failed! Pls try again!",Toast.LENGTH_SHORT).show();
            }

		}
	}

}
