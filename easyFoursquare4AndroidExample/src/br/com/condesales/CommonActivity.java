package br.com.condesales;

import java.util.ArrayList;
import java.util.List;

import br.com.condesales.listeners.AccessTokenRequestListener;
import br.com.condesales.listeners.FoursquareVenueDetailsRequestListener;
import br.com.condesales.listeners.VenuesHistoryListener;
import br.com.condesales.models.Venue;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class CommonActivity extends Activity implements AccessTokenRequestListener{
	//	private TextView userName;
	//	private TextView placeLv;
	//	private ImageView userImage;
	//	private ViewSwitcher viewSwitcher;
	//	private TextView commonText;
	//	private TextView nonCommonText;
	//	private Button commonCategory;
	//	private Button nonCommonCategory;

	private EasyFoursquareAsync async;
	private ArrayList<String> venueId;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_common);
		
		//ask for access
		async = new EasyFoursquareAsync(this);
		async.requestAccess(this);

		Intent i = getIntent();
		venueId = i.getStringArrayListExtra("venues");

		// リソースに準備した画像ファイルからBitmapを作成しておく
		Bitmap image;
		image = BitmapFactory.decodeResource(getResources(), R.drawable.search_icon);

		// データの作成
		List<CustomData> objects = new ArrayList<CustomData>();
		
		for (int p = 0 ; p < venueId.size() ; p++){
			String venue = venueId.get(p);
			getVenueInfo(venue);
			//Log.v("Blue", "venue " + p +" = " + venue);
		}
		
		CustomData item1 = new CustomData();
		item1.setImagaData(image);
		item1.setTextData("１つ目～");

		CustomData item2 = new CustomData();
		item2.setImagaData(image);
		item2.setTextData("The second");

		CustomData item3 = new CustomData();
		item3.setImagaData(image);
		item3.setTextData("Il terzo");

		objects.add(item1);
		objects.add(item2);
		objects.add(item3);

		CustomAdapter customAdapater = new CustomAdapter(this, 0, objects);

		ListView listView = (ListView)findViewById(R.id.list);
		listView.setAdapter(customAdapater);

	}

	private void getVenueInfo(String id) {
		FoursquareVenueDetailsRequestListener mFoursquareVenueDetailsRequestListener = new FoursquareVenueDetailsRequestListener(){

			@Override
			public void onError(String errorMsg) {
				Toast.makeText(CommonActivity.this, errorMsg, Toast.LENGTH_LONG)
				.show();				
			}

			@Override
			public void onVenueDetailFetched(Venue venues) {
				Log.v("onVenueDetailFetched", venues.getName());
			}

		};

		async.getVenueDetail(id, mFoursquareVenueDetailsRequestListener);;		

	}

	@Override
	public void onError(String errorMsg) {
		Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();		
	}

	@Override
	public void onAccessGrant(String accessToken) {
		// TODO 自動生成されたメソッド・スタブ
		
	}
}



