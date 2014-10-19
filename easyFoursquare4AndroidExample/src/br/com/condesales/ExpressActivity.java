package br.com.condesales;

import java.util.ArrayList;

import br.com.condesales.listeners.AccessTokenRequestListener;
import br.com.condesales.listeners.FriendsListener;
import br.com.condesales.listeners.ImageRequestListener;
import br.com.condesales.listeners.UserInfoRequestListener;
import br.com.condesales.listeners.VenuesHistoryListener;
import br.com.condesales.models.User;
import br.com.condesales.tasks.users.UserImageRequest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class ExpressActivity extends Activity implements android.view.View.OnClickListener, ImageRequestListener, AccessTokenRequestListener{
	private ArrayList<String> receiveVenueId = new ArrayList<String>();
	private ArrayList<String> myVenueId = new ArrayList<String>();


	private ArrayList<String> commonVenue = new ArrayList<String>();
	private ArrayList<String> nonCommonVenue = new ArrayList<String>();

	private EasyFoursquareAsync async;

	private TextView userName;
	private TextView placeLv;
	private ImageView userImage;
	private ViewSwitcher viewSwitcher;
	private TextView commonText;
	private TextView nonCommonText;
	private Button commonCategory;
	private Button nonCommonCategory;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_express);

		userImage = (ImageView) findViewById(R.id.userImage);
		viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher1);
		userName = (TextView) findViewById(R.id.userName);
		placeLv = (TextView) findViewById(R.id.placeLv);
		commonText = (TextView) findViewById(R.id.commonText);
		commonText.setText("↓お互いの共通点");
		nonCommonText = (TextView) findViewById(R.id.nonCommonText);
		nonCommonText.setText("↓知らないスポットをお薦めしよう！");
		commonCategory = (Button) findViewById(R.id.commonCategory);
		commonCategory.setOnClickListener(this);
		nonCommonCategory = (Button) findViewById(R.id.nonCommonCategory);
		nonCommonCategory.setOnClickListener(this);

		//ask for access
		async = new EasyFoursquareAsync(this);
		async.requestAccess(this);

		Intent i = getIntent();
		myVenueId = i.getStringArrayListExtra("sendVenues");
		receiveVenueId = i.getStringArrayListExtra("recieveVenues");

		for(int p=0; p<receiveVenueId.size(); p++){
			Log.v("receiveVenueId", receiveVenueId.get(p));
		}

		for(int p=0; p<myVenueId.size(); p++){
			Log.v("myVenueId", myVenueId.get(p));
		}


		for(int p=0; p<myVenueId.size(); p++){
			if(receiveVenueId.contains(myVenueId.get(p))){
				commonVenue.add(myVenueId.get(p));
			}
		}
		
		for(int p=0; p<receiveVenueId.size(); p++){
			if(!commonVenue.contains(receiveVenueId.get(p))){
				nonCommonVenue.add(receiveVenueId.get(p));
			}
		}
		
		




		//		for(int p=0; p<myVenueId.size(); p++){
		//			if(receiveVenueId.contains(myVenueId.get(p))){
		//				commonVenue.add(myVenueId.get(p));
		//			}
		//			else{
		//				nonCommonVenue.add(myVenueId.get(p));
		//			}
		//		}
		//
		//
		//		for(int p=0; p<receiveVenueId.size(); p++){
		//			if(myVenueId.contains(receiveVenueId.get(p))){
		//				commonVenue.add(receiveVenueId.get(p));
		//				Log.v("venueueueueue",receiveVenueId.get(p));
		//			}
		//			else{
		//				nonCommonVenue.add(receiveVenueId.get(p));
		//				Log.v("venueueueueue","---------" + receiveVenueId.get(p));
		//
		//			}
		//		}


		commonCategory.setText("共通: " + commonVenue.size() + "件");
		nonCommonCategory.setText("非共通:" + nonCommonVenue.size() + "件");

	}

	private void getFriends(String string) {
		UserInfoRequestListener mUserInfoRequestListener = new UserInfoRequestListener(){
			@Override
			public void onError(String errorMsg) {
				Toast.makeText(ExpressActivity.this, errorMsg, Toast.LENGTH_LONG)
				.show();
			}

			public void onUserInfoFetched(User user) {
				Log.v("ExpressActivity", "User = " + user.getFirstName());
			}
		};

		async.getUserInfo(mUserInfoRequestListener);

	}

	@Override
	public void onClick(View v) {
		if(v == commonCategory){
			//Intent intent = new Intent(getApplicationContext(),ShareActivity.class);
			Intent intent = new Intent(getApplicationContext(),CommonActivity.class);
			intent.putExtra("venues", commonVenue);

			//intent.putExtra("nabe_type", nabe_type);
			startActivity(intent);
		}
		else if (v == nonCommonCategory){
			//Intent intent = new Intent(getApplicationContext(),ShareActivity.class);
			Intent intent = new Intent(getApplicationContext(),NonCommonActivity.class);
			intent.putExtra("venues", nonCommonVenue);

			//intent.putExtra("nabe_type", nabe_type);
			startActivity(intent);
		}
	}

	@Override
	public void onImageFetched(Bitmap bmp) {
		userImage.setImageBitmap(bmp);
	}

	@Override
	public void onError(String errorMsg) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void onAccessGrant(String accessToken) {
		async.getUserInfo(new UserInfoRequestListener() {

			@Override
			public void onError(String errorMsg) {
				// Some error getting user info
				Toast.makeText(ExpressActivity.this, errorMsg, Toast.LENGTH_LONG)
				.show();
			}

			@Override
			public void onUserInfoFetched(User user) {
				// OWww. did i already got user!?
				if (user.getBitmapPhoto() == null) {
					UserImageRequest request = new UserImageRequest(
							ExpressActivity.this);
					request.execute(user.getPhoto());
				} else {
					userImage.setImageBitmap(user.getBitmapPhoto());
				}
				userName.setText(user.getFirstName() + " " + user.getLastName());
				viewSwitcher.showNext();
				//				Toast.makeText(MainActivity.this, "Got it!", Toast.LENGTH_LONG)
				//				.show();
			}
		});
	}
}


