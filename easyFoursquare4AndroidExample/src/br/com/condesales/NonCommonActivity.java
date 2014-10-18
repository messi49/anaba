package br.com.condesales;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import br.com.condesales.listeners.AccessTokenRequestListener;
import br.com.condesales.listeners.FoursquareVenueDetailsRequestListener;
import br.com.condesales.listeners.VenuePhotosListener;
import br.com.condesales.listeners.VenuesHistoryListener;
import br.com.condesales.models.PhotoItem;
import br.com.condesales.models.PhotosGroup;
import br.com.condesales.models.Venue;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class NonCommonActivity extends Activity implements AccessTokenRequestListener{
	//	private TextView userName;
	//	private TextView placeLv;
	//	private ImageView userImage;
	//	private ViewSwitcher viewSwitcher;
	//	private TextView commonText;
	//	private TextView nonCommonText;
	//	private Button commonCategory;
	//	private Button nonCommonCategory;

	final String IMAGE_SIZE = "100x100";

	private EasyFoursquareAsync async;
	private ArrayList<String> venueId;
	private String[] venueName;
	private Bitmap image, default_image;
	private List<CustomData> objects;
	private int counter = 0, num = 0, size;
	private CustomData[] item;
	private  ArrayList<PhotoItem> photoArray;
	private String[] ImageURL;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_common);

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());

		//ask for access
		async = new EasyFoursquareAsync(this);
		async.requestAccess(this);

		Intent i = getIntent();
		venueId = i.getStringArrayListExtra("venues");

		// リソースに準備した画像ファイルからBitmapを作成しておく
		default_image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

		// データの作成
		objects = new ArrayList<CustomData>();
		size = venueId.size();
		item = new CustomData[size];
		venueName = new String[size];
		ImageURL = new String[size];

		for (int p = 0 ; p < venueId.size() ; p++){
			getVenueInfo(venueId.get(p));
		}


	}

	private void getVenueImage(String id) {
		VenuePhotosListener mVenuePhotosListener = new VenuePhotosListener(){
			@Override
			public void onError(String errorMsg) {
				Toast.makeText(NonCommonActivity.this, errorMsg, Toast.LENGTH_LONG)
				.show();
			}

			@Override
			public void onGotVenuePhotos(PhotosGroup photosGroup) {
				photoArray = photosGroup.getItems();
				ImageURL[num] = photoArray.get(0).getPrefix() + IMAGE_SIZE + photoArray.get(0).getSuffix();
				Log.v("ImageURL", ImageURL[num]);
				//item[num].setImagaData(image);
				num++;

				if(num == counter){
					setup();
				}

			}

			private Bitmap getBitmap(String imageUrl) {
				try {
					URL url = new URL(imageUrl);
					//インプットストリームで画像を読み込む
					InputStream istream = url.openStream();
					//読み込んだファイルをビットマップに変換
					image = BitmapFactory.decodeStream(istream);
				} catch (MalformedURLException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				return image;
			}
		};

		async.getVenuePhotos(id, mVenuePhotosListener);
	}

	private void setup() {
		for(int i=0; i<size; i++){
			CustomData item = new CustomData();
			item.setImagaData(default_image);
			item.setTextData(venueName[i]);
			objects.add(item);
		}
		CustomAdapter customAdapater = new CustomAdapter(getApplicationContext(), 0, objects);

		ListView listView = (ListView)findViewById(R.id.list);
		listView.setAdapter(customAdapater);
	}

	private void getVenueInfo(String id) {
		FoursquareVenueDetailsRequestListener mFoursquareVenueDetailsRequestListener = new FoursquareVenueDetailsRequestListener(){

			@Override
			public void onError(String errorMsg) {
				Toast.makeText(NonCommonActivity.this, errorMsg, Toast.LENGTH_LONG)
				.show();				
			}

			@Override
			public void onVenueDetailFetched(Venue venues) {
				venueName[counter] = venues.getName();
				Log.v("onVenueDetailFetched", "counter = "+ counter + ", " + venues.getName());
				counter++;
				if(counter == size){
					setup();
				}
			}

		};

		async.getVenueDetail(id, mFoursquareVenueDetailsRequestListener);
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



