package br.com.condesales;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.com.condesales.criterias.CheckInCriteria;
import br.com.condesales.criterias.TipsCriteria;
import br.com.condesales.listeners.AccessTokenRequestListener;
import br.com.condesales.listeners.CheckInListener;
import br.com.condesales.listeners.ImageRequestListener;
import br.com.condesales.listeners.TipsRequestListener;
import br.com.condesales.listeners.UserInfoRequestListener;
import br.com.condesales.listeners.VenuesHistoryListener;
import br.com.condesales.models.Checkin;
import br.com.condesales.models.Tip;
import br.com.condesales.models.User;
import br.com.condesales.models.Venue;
import br.com.condesales.models.Venues;
import br.com.condesales.tasks.users.GetUserVenuesHistoryRequest;
import br.com.condesales.tasks.users.UserImageRequest;

public class MainActivity extends Activity implements
AccessTokenRequestListener, ImageRequestListener, android.view.View.OnClickListener{
	private final int MP = TableLayout.LayoutParams.MATCH_PARENT;
	private final int WC = TableLayout.LayoutParams.WRAP_CONTENT;

	private EasyFoursquareAsync async;
	private ImageView userImage;
	private ViewSwitcher viewSwitcher;
	private TextView userName;
	private TextView placeLv;
	private VenuesHistoryListener mVenuesHistoryListener; 
	EasyFoursquare mEasyFoursquare;
	private String[] categoryType;
	private String[] categoryNum;
	private ArrayList<String> venueId;
	private TableLayout tableLayout;
	private ImageButton shareButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//		try {
		//			// ここで2秒間スリープし、スプラッシュを表示させたままにする。
		//			Thread.sleep(2000);
		//		} catch (InterruptedException e) {
		//		}

		// 通常時のテーマをセットする。
		setTheme(R.style.NormalTheme);

		setContentView(R.layout.activity_main);
		userImage = (ImageView) findViewById(R.id.userImage);
		viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher1);
		userName = (TextView) findViewById(R.id.userName);
		placeLv = (TextView) findViewById(R.id.placeLv);
		//ask for access
		async = new EasyFoursquareAsync(this);
		async.requestAccess(this);

		mEasyFoursquare = new EasyFoursquare(this);

		tableLayout = (TableLayout)findViewById(R.id.tableLayout);

		shareButton = (ImageButton)findViewById(R.id.shareButton);
		shareButton.setOnClickListener(this);

		venueId = new ArrayList<String>();
	}


	@Override
	public void onError(String errorMsg) {
		// Do something with the error message
		Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onAccessGrant(String accessToken) {
		// with the access token you can perform any request to foursquare.
		// example:
		async.getUserInfo(new UserInfoRequestListener() {

			@Override
			public void onError(String errorMsg) {
				// Some error getting user info
				Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG)
				.show();
			}

			@Override
			public void onUserInfoFetched(User user) {
				// OWww. did i already got user!?
				if (user.getBitmapPhoto() == null) {
					UserImageRequest request = new UserImageRequest(
							MainActivity.this, MainActivity.this);
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

		//for another examples uncomment lines below:
		//requestTipsNearby();
		//checkin();

		getHistory();
	}

	private void getHistory() {
		VenuesHistoryListener mVenuesHistoryListener = new VenuesHistoryListener(){

			@Override
			public void onError(String errorMsg) {
				Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG)
				.show();
			}

			@Override
			public void onGotVenuesHistory(ArrayList<Venues> list) {
				HashMap<String, Integer> categoryMap = new HashMap<String, Integer>();

				int size = list.size();
				int counter = 0;

				String[] type = new String[size];

				for(int i=0; i < size; i++){
					int l=0;

					if(i == 0){
						categoryMap.put(list.get(i).getVenue().getCategories().get(0).getName(), 1);
						venueId.add(list.get(i).getVenue().getId());
						counter++;
						continue;
					}
					for(l = 0; l<counter; l++){
						if(categoryMap.get(list.get(i).getVenue().getCategories().get(0).getName()) != null){
							int k = categoryMap.get(list.get(i).getVenue().getCategories().get(0).getName());
							categoryMap.put(list.get(i).getVenue().getCategories().get(0).getName(), (k+1));
							break;
						}
					}
					if(l == counter && l != 0){
						categoryMap.put(list.get(i).getVenue().getCategories().get(0).getName(), 1);
						venueId.add(list.get(i).getVenue().getId());
						counter++;
					}
				}
				sortCategory(categoryMap);

			}

			private List<Entry<String, Integer>> sortCategory(HashMap<String, Integer> categoryMap) {
				List<Map.Entry<String,Integer>> entries = 
						new ArrayList<Map.Entry<String,Integer>>(categoryMap.entrySet());
				Collections.sort(entries, new Comparator<Map.Entry<String,Integer>>() {
					@Override
					public int compare(
							Entry<String,Integer> entry1, Entry<String,Integer> entry2) {
						return ((Integer)entry2.getValue()).compareTo((Integer)entry1.getValue());
					}
				});

				// 内容を表示
				int i = 0;
				categoryType = new String[3];
				categoryNum = new String[3];

				for (Entry<String,Integer> s : entries) {
					if(i < 3){
						categoryType[i] = s.getKey();
						categoryNum[i] = s.getValue().toString();
					}
					System.out.println("s.getKey() : " + s.getKey());
					System.out.println("s.getValue() : " + s.getValue());
					i++;
				}

				int lvPoint = Integer.parseInt(categoryNum[1]);
				if(lvPoint < 20){
					placeLv.setText(categoryType[1] + " Lv.1");
				}
				else if(20 < lvPoint && lvPoint < 40){
					placeLv.setText(categoryType[1] + " Lv.2");
				}
				else{
					placeLv.setText(categoryType[1] + " Lv.3");
				}

				for(i=0; i<3; i++){
					TableRow tableRow = (TableRow)getLayoutInflater().inflate(R.layout.tablerow_layout, null);
					TextView name = (TextView)tableRow.findViewById(R.id.rowtext1);
					name.setText(categoryType[i]);
					TextView point = (TextView)tableRow.findViewById(R.id.rowtext2);
					point.setText(categoryNum[i]);

					if((i+1)%2 == 0){
						int color = getResources().getColor(R.color.chart_dg_gray);
						name.setBackgroundColor(color);
						point.setBackgroundColor(color);
					}

					tableLayout.addView(tableRow, new TableLayout.LayoutParams(MP, WC));
				}

				return entries;
			}
		};

		async.getVenuesHistory(mVenuesHistoryListener);		
	}


	@Override
	public void onImageFetched(Bitmap bmp) {
		userImage.setImageBitmap(bmp);
	}

	private void requestTipsNearby() {
		Location loc = new Location("");
		loc.setLatitude(40.4363483);
		loc.setLongitude(-3.6815703);

		TipsCriteria criteria = new TipsCriteria();
		criteria.setLocation(loc);
		async.getTipsNearby(new TipsRequestListener() {

			@Override
			public void onError(String errorMsg) {
				Toast.makeText(MainActivity.this, "error", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onTipsFetched(ArrayList<Tip> tips) {
				Toast.makeText(MainActivity.this, tips.toString(), Toast.LENGTH_LONG).show();
			}
		}, criteria);
	}

	private void checkin() {
		CheckInCriteria criteria = new CheckInCriteria();
		criteria.setBroadcast(CheckInCriteria.BroadCastType.PUBLIC);
		criteria.setVenueId("4c7063da9c6d6dcb9798d27a");

		async.checkIn(new CheckInListener() {
			@Override
			public void onCheckInDone(Checkin checkin) {
				Toast.makeText(MainActivity.this, checkin.getId(), Toast.LENGTH_LONG).show();
			}

			@Override
			public void onError(String errorMsg) {
				Toast.makeText(MainActivity.this, "error", Toast.LENGTH_LONG).show();
			}
		}, criteria);
	}

	@Override
	public void onClick(View v) {
		if(v == shareButton){
			Intent intent = new Intent(getApplicationContext(), BluetoothShareActivity.class);
			//Intent intent = new Intent(getApplicationContext(),ExpressActivity.class);
			//Intent intent = new Intent(getApplicationContext(), CommonActivity.class);

			intent.putExtra("venues", venueId);
			startActivity(intent);
		}
	}
}
