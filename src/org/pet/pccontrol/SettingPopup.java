package org.pet.pccontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

public class SettingPopup extends Activity {

	private static final String TAG = "Setting";
	
	private ProgressDialog myPd_ring;
	
	private String strCharacters;
	
	private String toastMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.setting_view);

		final ContextHolder holder = ContextHolder.getInstance(getApplicationContext());
		final EditText hostnameEditText = (EditText) findViewById(R.id.serverHost);
		final EditText serverPortEditText = (EditText) findViewById(R.id.serverPort);
		final EditText socketTimeout = (EditText) findViewById(R.id.socketTimeout);
		final SeekBar mouseSensitivitySeekBar = (SeekBar) findViewById(R.id.mouseSensitivity);
		final CheckBox tappingEnableCheckbox = (CheckBox) findViewById(R.id.touchpadTapping);
		final Button saveButton = (Button) findViewById(R.id.saveButton);
		final Button cancelButton = (Button) findViewById(R.id.closeButton);
		final Button testConnectionButton = (Button) findViewById(R.id.testConnection);

		String existingHostname = holder.getHostname();
		int existingPort = holder.getPort();
		int existingTimeout = holder.getTimeout();
		int existingSensitivity = holder.getSensitivity();
		if(existingHostname != null && existingHostname != "")
			hostnameEditText.setText(existingHostname);
		if(existingPort > 0)
			serverPortEditText.setText(String.valueOf(existingPort));
		socketTimeout.setText(String.valueOf(existingTimeout));
		mouseSensitivitySeekBar.setProgress(existingSensitivity);
		tappingEnableCheckbox.setChecked(holder.isTappingEnabled());

		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		testConnectionButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Editable hostnameEditable = hostnameEditText.getText();
				Editable serverPortEditable = serverPortEditText.getText();
				Editable socketTimeoutEditable = socketTimeout.getText();
				final String hostname = hostnameEditable!=null?hostnameEditable.toString():null;
				String serverPortStr = serverPortEditable!=null?serverPortEditable.toString():null;
				int tmpSocketTimeout = 5000;
				int tmpServerPort = 0;
				boolean isValidData = true;
				if(hostname == null || hostname.equals("")){
					isValidData = false;
					Toast.makeText(getApplicationContext(), getText(R.string.empty_hostname_entered), Toast.LENGTH_SHORT).show();
				}
				try {
					tmpSocketTimeout = Integer.parseInt(socketTimeoutEditable.toString());
				} catch (Exception e1) {
					Log.e(TAG, "Fail to parse socket timeout. Will use default value 5 seconds.", e1);
				}
				try {
					if(serverPortStr == null || serverPortStr.equals(""))
						throw new Exception("Server port is required.");
					tmpServerPort = Integer.parseInt(serverPortStr);
					if(tmpServerPort <= 0)
						throw new Exception("Invalid server port entered.");
				} catch (Exception e) {
					isValidData = false;
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
				
				final int socketTimeout = tmpSocketTimeout;
				final int serverPort = tmpServerPort;
				if(isValidData){
					myPd_ring = ProgressDialog.show(SettingPopup.this, "Please wait", "Testing connection, please wait..", true);
                    myPd_ring.setCancelable(false);
                    new Thread(new Runnable() {  
                          @Override
                          public void run() {
							try {
								Thread.sleep(1000);
								strCharacters = "Connecting to " + hostname + " on port " + serverPort + "..";
								runOnUiThread(changeMessage);
								MouseSocketClient client = new MouseSocketClient(hostname, serverPort, socketTimeout);
								boolean isSuccess = false;
								try{
									client.connect();
									strCharacters = "Connection established, waiting to close connection..";
									runOnUiThread(changeMessage);
									client.disconnect();
									isSuccess = true;
								} catch (Exception e){
								}
								Thread.sleep(1000);
								strCharacters = "Done!!";
								runOnUiThread(changeMessage);
								Thread.sleep(1000);
								if(!isSuccess){
									toastMessage = "Connection error!!";
									//Toast.makeText(getApplicationContext(), "Connection error!!", Toast.LENGTH_LONG).show();
								} else {
									toastMessage = "Connection success!!";
									//Toast.makeText(getApplicationContext(), "Connection success!!", Toast.LENGTH_LONG).show();
								}
								runOnUiThread(toastIt);
							} catch (Exception e) {
							}
							myPd_ring.dismiss();
                          }
                    }).start();
				} 
			}
		});

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				Editable hostnameEditable = hostnameEditText.getText();
				Editable serverPortEditable = serverPortEditText.getText();
				Editable socketTimeoutEditable = socketTimeout.getText();
				final int mouseSensitivity = mouseSensitivitySeekBar.getProgress();
				final String hostname = hostnameEditable!=null?hostnameEditable.toString():null;
				String serverPortStr = serverPortEditable!=null?serverPortEditable.toString():null;
				int tmpSocketTimeout = 5000;
				int tmpServerPort = 0;
				final boolean isTouchpadTappingEnabled = tappingEnableCheckbox.isChecked();
				boolean isValidData = true;
				if(hostname == null || hostname.equals("")){
					isValidData = false;
					Toast.makeText(getApplicationContext(), getText(R.string.empty_hostname_entered), Toast.LENGTH_SHORT).show();
				}
				try {
					tmpSocketTimeout = Integer.parseInt(socketTimeoutEditable.toString());
				} catch (Exception e1) {
					Log.e(TAG, "Fail to parse socket timeout. Will use default value 5 seconds.", e1);
				}
				try {
					if(serverPortStr == null || serverPortStr.equals(""))
						throw new Exception("Server port is required.");
					tmpServerPort = Integer.parseInt(serverPortStr);
					if(tmpServerPort <= 0)
						throw new Exception("Invalid server port entered.");
				} catch (Exception e) {
					isValidData = false;
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
				
				final int socketTimeout = tmpSocketTimeout;
				final int serverPort = tmpServerPort;

				if(isValidData){
					if(socketTimeout <= 0){
						alertDialog.setMessage(getText(R.string.timeout_warning));
						alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.ok_button), new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								holder.setHostname(hostname);
								holder.setPort(serverPort);
								holder.setTimeout(socketTimeout);
								holder.setSensitivity(mouseSensitivity);
								holder.setTappingEnabled(isTouchpadTappingEnabled);
								Toast.makeText(getApplicationContext(), R.string.succesfully_update_config, Toast.LENGTH_LONG).show();
								finish();
							}
						});
						
						alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getText(R.string.cancel_button), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								alertDialog.dismiss();
							}
						});
						
						alertDialog.show();
					} else if (socketTimeout > 10000){
						alertDialog.setMessage(getText(R.string.timeout_too_long));
						alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.ok_button), new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								holder.setHostname(hostname);
								holder.setPort(serverPort);
								holder.setTimeout(socketTimeout);
								holder.setSensitivity(mouseSensitivity);
								holder.setTappingEnabled(isTouchpadTappingEnabled);
								Toast.makeText(getApplicationContext(), R.string.succesfully_update_config, Toast.LENGTH_LONG).show();
								finish();
							}
						});
						
						alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getText(R.string.cancel_button), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								alertDialog.dismiss();
							}
						});
						
						alertDialog.show();
					} else {
						holder.setHostname(hostname);
						holder.setPort(tmpServerPort);
						holder.setTimeout(socketTimeout);
						holder.setSensitivity(mouseSensitivity);
						holder.setTappingEnabled(isTouchpadTappingEnabled);
						Toast.makeText(getApplicationContext(), R.string.succesfully_update_config, Toast.LENGTH_LONG).show();
						finish();
				}
			}

		}
	});

}
	
	
	
	private Runnable changeMessage = new Runnable() {
	    @Override
	    public void run() {
	        //Log.v(TAG, strCharacters);
	    	myPd_ring.setMessage(strCharacters);
	    }
	};

	private Runnable toastIt = new Runnable() {
	    @Override
	    public void run() {
	    	Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
	    }
	};
	
@Override
protected void onPostCreate(Bundle savedInstanceState) {
	super.onPostCreate(savedInstanceState);
}

}
