package org.pet.pccontrol;

import java.io.IOException;
import java.net.UnknownHostException;

import org.pet.pccontrol.MouseSocketClient.ButtonAction;
import org.pet.pccontrol.util.SystemUiHider;

import android.app.Activity;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MouseActivity extends Activity {

	private static final String TAG = "MousePanel";
	
	private static final long CONNECTION_INDICATOR_DURATION = 5000;

	private ContextHolder holder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_mouse);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		holder = ContextHolder.getInstance(getApplicationContext());
		
		final Button leftButton = (Button) findViewById(R.id.leftButton);
		final Button rightButton = (Button) findViewById(R.id.rightButton);
		
		final TextView connectionIndicator = (TextView) findViewById(R.id.connectionLostIndicator);
		
		OnClickListener leftClickListener = new LeftClickListener();
		OnClickListener rightClickListener = new RightClickListener();
		
		leftButton.setOnClickListener(leftClickListener);
		rightButton.setOnClickListener(rightClickListener);
		
		final GestureOverlayView gestureOverlay = (GestureOverlayView) findViewById(R.id.gestureOverlay);
		gestureOverlay
				.addOnGestureListener(new GestureOverlayView.OnGestureListener() {
					
					private MouseSocketClient socketClient;

					private int existingX, existingY;
					
					private int delta, deltaPositive, deltaNegative;
					
					private boolean isAlreadyMove = false;
					
					private boolean isConnected = false;

					@Override
					public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
						try {
							delta = getDeltaValue(holder.getSensitivity());
							deltaPositive = delta;
							deltaNegative = -delta;
							existingX = (int) event.getX();
							existingY = (int) event.getY();
							socketClient = new MouseSocketClient(holder.getHostname(), holder.getPort(), holder.getTimeout());
							socketClient.connect();
							isAlreadyMove = false;
							isConnected = true;
							hideConnectionLostIndicator(connectionIndicator);
						} catch (UnknownHostException e) {
							Log.e(TAG, "Fail to connect to socket", e);
						} catch (IOException e) {
							Log.e(TAG, "Fail to connect to socket", e);
						}
					}

					@Override
					public void onGestureEnded(GestureOverlayView overlay,
							MotionEvent event) {
						try {
							int endX = (int) event.getX();
							int endY = (int) event.getY();
							if(isConnected && holder.isTappingEnabled() && !isAlreadyMove && endX==existingX && endY==existingY){
								socketClient.buttonClick(ButtonAction.LEFT_CLICK);
							}
							if(isConnected)
								socketClient.disconnect();
							else
								showConnectionLostIndicator(connectionIndicator, CONNECTION_INDICATOR_DURATION);
						} catch (IOException e) {
							Log.e(TAG, "Fail to disconnect to socket", e);
						}
					}

					@Override
					public void onGestureCancelled(GestureOverlayView overlay,
							MotionEvent event) {
						try {
							if(isConnected)
								socketClient.disconnect();
							else
								showConnectionLostIndicator(connectionIndicator, CONNECTION_INDICATOR_DURATION);
						} catch (IOException e) {
							Log.e(TAG, "Fail to disconnect to socket", e);
						}
					}
					
					@Override
					public void onGesture(GestureOverlayView overlay, MotionEvent event) {
						int x = (int) event.getX();
						int y = (int) event.getY();
						
						if(existingX == 0)
							existingX = x;
						if(existingY == 0)
							existingY = y;
						
						int actX = 0;
						int actY = 0;
						
						if(x > existingX){ // Movement from left to right
							//Log.v(TAG, "Movement from left to right");
							actX = deltaPositive;
						} else if (x == existingX) { // No movement
							
						} else if (x < existingX) { // Movement from right to left
							//Log.v(TAG, "Movement from right to left");
							actX = deltaNegative;
						}
						
						if(y > existingY){ // Movement from bottom to top
							//Log.v(TAG, "Movement from bottom to top");
							actY = deltaPositive;
						} else if (y == existingY){ // No movement
							
						} else if (y < existingY){ // Movement from top to bottom
							//Log.v(TAG, "Movement from top to bottom");
							actY = deltaNegative;
						}
						
						existingX = x;
						existingY = y;
						
						// Send mouse to socket
						MouseCoordinate mouseCoordinate = new MouseCoordinate(actX, actY);
						try {
							if(isConnected)
								socketClient.moveMouse(mouseCoordinate);
							else
								showConnectionLostIndicator(connectionIndicator, CONNECTION_INDICATOR_DURATION);
							isAlreadyMove = true;
						} catch (IOException e) {
							Log.e(TAG, "Fail to send data to socket", e);
						}

					}
				});

	}
	
	private void showConnectionLostIndicator(final TextView connectionIndicator, final long duration){
		if(connectionIndicator.getVisibility() != TextView.VISIBLE){
			connectionIndicator.setVisibility(TextView.VISIBLE);
			connectionIndicator.getHandler().post(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(duration);
						connectionIndicator.setVisibility(TextView.GONE);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			});
		}
	}
	
	private void hideConnectionLostIndicator(TextView connectionIndicator){
		connectionIndicator.setVisibility(TextView.GONE);
	}
	
	private int getDeltaValue(int sensitivity){
		// max = 20
		// 1) g(x) = -f(1+x)
		// 2) g(x) = -f+max
//		int delta = -sensitivity + 20;
//		if(delta == 0)
//			delta = 1;
		return sensitivity;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId){
		case R.id.menu_setting :
			Intent intent = new Intent(getApplicationContext(), SettingPopup.class);
			startActivity(intent);
			break;
		}

		
		return true;
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

	}

	class LeftClickListener implements OnClickListener {
		
		private MouseSocketClient socketClient;
		
		@Override
		public void onClick(View v) {
			//Log.v(TAG, "Execute left button click");
			try {
				socketClient = new MouseSocketClient(holder.getHostname(), holder.getPort(), holder.getTimeout());
				socketClient.connect();
				socketClient.buttonClick(ButtonAction.LEFT_CLICK);
			} catch (UnknownHostException e) {
				Log.e(TAG, "Fail to connect to socket", e);
			} catch (IOException e) {
				Log.e(TAG, "Fail to connect to socket", e);
			} finally {
				try {
					socketClient.disconnect();
				} catch (IOException e) {
					Log.e(TAG, "Fail to connect to socket", e);
				}
			}
		}
		
	}
	
	class RightClickListener implements OnClickListener {
		
		private MouseSocketClient socketClient;

		@Override
		public void onClick(View v) {
			//Log.v(TAG, "Execute right click button");
			try {
				socketClient = new MouseSocketClient(holder.getHostname(), holder.getPort(), holder.getTimeout());
				socketClient.connect();
				socketClient.buttonClick(ButtonAction.RIGHT_CLICK);
			} catch (UnknownHostException e) {
				Log.e(TAG, "Fail to connect to socket", e);
			} catch (IOException e) {
				Log.e(TAG, "Fail to connect to socket", e);
			} finally {
				try {
					socketClient.disconnect();
				} catch (IOException e) {
					Log.e(TAG, "Fail to connect to socket", e);
				}
			}
		}
		
	}

}
