package com.example.bookwormconnect;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.net.URLEncoder;

public class mapPicker extends AppCompatActivity {
    MapView map;
    Button confirmBtn;
    double selectedLat, selectedLon;
    String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE)
        );
        EdgeToEdge.enable(this);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map_picker);
        EditText detailsEt;
        detailsEt = findViewById(R.id.detailsEt);

        EditText searchEt;
        Button searchBtn;

        searchEt = findViewById(R.id.searchEt);
        searchBtn = findViewById(R.id.searchBtn);

        searchBtn.setOnClickListener(v -> {
            String query = searchEt.getText().toString().trim();

            if (!query.isEmpty()) {
                searchLocation(query);
            }
        });
        map = findViewById(R.id.map);
        confirmBtn = findViewById(R.id.confirmBtn);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        IMapController controller = map.getController();
        controller.setZoom(15.0);
        controller.setCenter(new GeoPoint(31.5204, 74.3587)); // Lahore default

        map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                selectedLat = p.getLatitude();
                selectedLon = p.getLongitude();

                map.getOverlays().clear();

                Marker marker = new Marker(map);
                marker.setPosition(p);
                marker.setTitle("Selected Location");
                map.getOverlays().add(marker);

                map.invalidate();

                getAddressFromNominatim(selectedLat, selectedLon);

                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        }));

        confirmBtn.setOnClickListener(v -> {

            String extraDetails = detailsEt.getText().toString().trim();

            String finalAddress;

            if (!extraDetails.isEmpty()) {
                finalAddress = selectedAddress + ", " + extraDetails;
            } else {
                finalAddress = selectedAddress;
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("address", finalAddress);

            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void searchLocation(String query) {

        new Thread(() -> {
            try {
                String url = "https://nominatim.openstreetmap.org/search?q="
                        + URLEncoder.encode(query, "UTF-8")
                        + "&format=json&limit=1";

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", "YourAppName")
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String res = response.body().string();
                    JSONArray array = new JSONArray(res);

                    if (array.length() > 0) {
                        JSONObject obj = array.getJSONObject(0);

                        double lat = obj.getDouble("lat");
                        double lon = obj.getDouble("lon");
                        String displayName = obj.getString("display_name");

                        runOnUiThread(() -> {
                            GeoPoint point = new GeoPoint(lat, lon);

                            map.getController().setZoom(16.0);
                            map.getController().setCenter(point);

                            map.getOverlays().clear();

                            Marker marker = new Marker(map);
                            marker.setPosition(point);
                            marker.setTitle(displayName);
                            map.getOverlays().add(marker);

                            map.invalidate();
                            TextView addressTv;
                            addressTv=findViewById(R.id.addressTv);
                            addressTv.setText(displayName);
                            selectedAddress = displayName;
                        });
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getAddressFromNominatim(double lat, double lon) {
        new Thread(() -> {
            try {
                String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat="
                        + lat + "&lon=" + lon;

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", "YourAppName")
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String res = response.body().string();
                    JSONObject obj = new JSONObject(res);

                    String displayName = obj.getString("display_name");

                    runOnUiThread(() -> {
                        TextView addressTv;
                        addressTv=findViewById(R.id.addressTv);
                        addressTv.setText(displayName);
                        selectedAddress = displayName;
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}