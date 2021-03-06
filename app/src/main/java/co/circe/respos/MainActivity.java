package co.circe.respos;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import co.circe.respos.adapter.PlaceAutocompleteAdapter;
import co.circe.respos.adapter.RestaurantInfo;
import co.circe.respos.fragment.locationListViewFragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import co.circe.respos.adapter.DrawerAdapter;
import co.circe.respos.library.GMapV2Direction;
import co.circe.respos.library.UserFunctions;
import co.circe.respos.model.DrawerItem;
import co.circe.respos.services.location_service;
import co.circe.respos.util.ImageUtil;
import co.circe.respos.util.basic_utils;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks, LocationListener {

    //tag
    public static String TAG = "MainActivity";

    //Screen Slide
    private static final int NUM_PAGES = 2;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    // Map variables
    private GoogleMap map;
    private static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(new LatLng(8.06890, 68.03215), new LatLng(35.674520, 97.40238));
    JSONObject restaurantsJson;
    JSONObject restaurantJson;
    private HashMap<Marker, RestaurantInfo> eventMarkerMap;

    //.Location variables
    protected GoogleApiClient googleapiclientlocation;
    protected Location currentLocation;
    private String latitude;
    private String longitude;
    protected LocationRequest locationrequest;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private LatLng center_map;
    LatLng destination;

    //Fragment variables
    protected boolean fragmentback = false;

    //Action Bar variables
    private boolean searchexpanded = false;

    // UI variables
    private static View view;
    private AutoCompleteTextView autocompletetextview;
    ProgressDialog normal_dialog;

    //Adapter variables
    private PlaceAutocompleteAdapter adapter;

    //Places Autocomplete variables
    protected GoogleApiClient googleapiclientplaces;

    public static final String LEFT_MENU_OPTION = "co.froogal.froogal.MainActivity";
    public static final String LEFT_MENU_OPTION_1 = "Left Menu Option 1";
    public static final String LEFT_MENU_OPTION_2 = "Left Menu Option 2";
    public int height;
    public int width;
    private ListView mDrawerList;
    private List<DrawerItem> mDrawerItems;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ImageView imageview_left;
    private ImageView imageview_center;
    private ImageView imageview_right;
    private TextView textview_left;
    private TextView textview_center;
    private TextView textview_right;
    private String button_clicked = "near_by";
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    SharedPreferences sharedpreferences;
    String username = "user";

    private Handler mHandler;
    locationListViewFragment listFragment;
    basic_utils bu;

    CardFrontFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get height and width
        Display display = this.getWindowManager().getDefaultDisplay();
        width  = display.getWidth();
        height = display.getHeight();

        // Starting location intent service
        startService(new Intent(getBaseContext(), location_service.class));

        // Basic utils object
        bu = new basic_utils(getApplicationContext());
        eventMarkerMap = new HashMap<Marker, RestaurantInfo>();

        // Updating values from shared preferences
        if(bu.location_check()) {

            latitude = bu.get_defaults("latitude");
            longitude = bu.get_defaults("longitude");
            Log.d(TAG, "Location Updated from shared preference");

        }

        imageview_left  =  (ImageView) findViewById(R.id.image_left);
        imageview_center = (ImageView) findViewById(R.id.image_center);
        imageview_right = (ImageView) findViewById(R.id.image_right);
        textview_left = (TextView) findViewById(R.id.text_left);
        textview_center = (TextView) findViewById(R.id.text_center);
        textview_right = (TextView) findViewById(R.id.text_right);

        JSONObject a = null;
        listFragment = locationListViewFragment.newInstance(a);
        //image_center(imageview_center.getRootView());
        //new ProcessRestaurants().execute();
        mapFragment = new CardFrontFragment();

        // Card flip animation
        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_frame,new CardFrontFragment() )
                    .commit();
        }

        // location
        buildGoogleApiClient();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Changing action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM |  ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_maps, null);
        actionBar.setCustomView(v);

        // Places AutoComplete
        googleapiclientplaces = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        googleapiclientplaces.connect();
        adapter = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1,googleapiclientplaces, BOUNDS_INDIA, null);

        //UI
        autocompletetextview = (AutoCompleteTextView) findViewById(R.id.autocomplete_places);
        autocompletetextview.setAdapter(adapter);
        autocompletetextview.setOnItemClickListener(autocompleteclicklistener);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_view);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        prepareNavigationDrawerItems();
        setAdapter();
        //mDrawerList.setAdapter(new DrawerAdapter(this, mDrawerItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    // Onclick buttons
    public void image_left(View v) {

        button_clicked = "favourite";

        textview_left.setTextColor(getResources().getColor(R.color.cpb_red_dark));
        textview_center.setTextColor(getResources().getColor(R.color.cpb_white));
        textview_right.setTextColor(getResources().getColor(R.color.cpb_white));

        imageview_left.setBackground(getResources().getDrawable(R.drawable.round_border_main_selected));
        imageview_center.setBackground(getResources().getDrawable(R.drawable.round_border_main_unselected));
        imageview_right.setBackground(getResources().getDrawable(R.drawable.round_border_main_unselected));

        imageview_left.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_red_24dp));
        imageview_center.setImageDrawable(getResources().getDrawable(R.drawable.ic_around_white));
        imageview_right.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_white_24dp));

        new ProcessRestaurants_fav().execute();
    }

    public void image_center(View v) {

        button_clicked = "near_by";



        textview_left.setTextColor(getResources().getColor(R.color.cpb_white));
        textview_center.setTextColor(getResources().getColor(R.color.cpb_blue_dark));
        textview_right.setTextColor(getResources().getColor(R.color.cpb_white));


        imageview_left.setBackground(getResources().getDrawable(R.drawable.round_border_main_unselected));
        imageview_center.setBackground(getResources().getDrawable(R.drawable.round_border_main_selected));
        imageview_right.setBackground(getResources().getDrawable(R.drawable.round_border_main_unselected));

        imageview_left.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
        imageview_center.setImageDrawable(getResources().getDrawable(R.drawable.ic_around_blue));
        imageview_right.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_white_24dp));

        new ProcessRestaurants().execute();
    }

    public void image_right(View v) {

        button_clicked = "popular";

        textview_left.setTextColor(getResources().getColor(R.color.cpb_white));
        textview_center.setTextColor(getResources().getColor(R.color.cpb_white));
        textview_right.setTextColor(getResources().getColor(R.color.material_yellow_500));

        imageview_left.setBackground(getResources().getDrawable(R.drawable.round_border_main_unselected));
        imageview_center.setBackground(getResources().getDrawable(R.drawable.round_border_main_unselected));
        imageview_right.setBackground(getResources().getDrawable(R.drawable.round_border_main_selected));

        imageview_left.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
        imageview_center.setImageDrawable(getResources().getDrawable(R.drawable.ic_around_white));
        imageview_right.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_yellow_24dp));

        new ProcessRestaurants_pop().execute();;

    }

    private void setAdapter() {

        boolean isFirstType = true;

        View headerView = null;

        headerView = prepareHeaderView(R.layout.header_navigation_drawer,
                bu.get_defaults("image_url"));


        BaseAdapter adapter = new DrawerAdapter(this, mDrawerItems, isFirstType);

        mDrawerList.addHeaderView(headerView);
        mDrawerList.setAdapter(adapter);
    }

    private View prepareHeaderView(int layoutRes, String url) {
        View headerView = getLayoutInflater().inflate(layoutRes, mDrawerList, false);
        ImageView iv = (ImageView) headerView.findViewById(R.id.userImage);
        TextView tv = (TextView) headerView.findViewById(R.id.name);
        ImageUtil.displayRoundImage(iv, url, null);

        String name = bu.get_defaults("fname") + " " + bu.get_defaults("lname");
        tv.setText(name);

        return headerView;
    }

    private void prepareNavigationDrawerItems() {
        mDrawerItems = new ArrayList<>();
        mDrawerItems.add(
                new DrawerItem(
                       getResources().getDrawable(R.drawable.ic_redeem_black_24dp),
                        R.string.drawer_title_orders,
                        DrawerItem.DRAWER_ITEM_TAG_ORDERS));
        mDrawerItems.add(
                new DrawerItem(
                        getResources().getDrawable(R.drawable.ic_redeem_black_24dp),
                        R.string.drawer_title_rewardpoints,
                        DrawerItem.DRAWER_ITEM_TAG_REWARDPOINTS));
        mDrawerItems.add(
                new DrawerItem(
                        getResources().getDrawable(R.drawable.ic_redeem_black_24dp),
                        R.string.drawer_title_redeem,
                        DrawerItem.DRAWER_ITEM_TAG_REDEEM));
        mDrawerItems.add(
                new DrawerItem(
                        getResources().getDrawable(R.drawable.ic_redeem_black_24dp),
                        R.string.drawer_title_invite,
                        DrawerItem.DRAWER_ITEM_TAG_INVITE));

        mDrawerItems.add(
                new DrawerItem(
                        getResources().getDrawable(R.drawable.ic_redeem_black_24dp),
                        R.string.drawer_title_editprofile,
                        DrawerItem.DRAWER_ITEM_TAG_EDITPROFILE));
        mDrawerItems.add(
                new DrawerItem(
                        getResources().getDrawable(R.drawable.ic_redeem_black_24dp),
                        R.string.drawer_title_aboutus,
                        DrawerItem.DRAWER_ITEM_TAG_ABOUTUS));
        mDrawerItems.add(
                new DrawerItem(
                        getResources().getDrawable(R.drawable.ic_redeem_black_24dp),
                        R.string.drawer_title_logout,
                        DrawerItem.DRAWER_ITEM_TAG_LOGOUT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;

        }
        // Handle action buttons
        int id = item.getItemId();

        if (id == R.id.search) {
            if (!searchexpanded) {

                searchexpanded = true;
                item.setIcon(R.drawable.ic_clear_white_36dp);

                // Action Bar
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                findViewById(R.id.autocomplete_places).setVisibility(View.VISIBLE);
                autocompletetextview.requestFocus();

                //Showing keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(autocompletetextview, InputMethodManager.SHOW_IMPLICIT);

                return true;

            }
            else
            {
                searchexpanded = false;
                item.setIcon(R.drawable.ic_action_search);

                //Hiding keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(autocompletetextview.getWindowToken(), InputMethodManager.SHOW_IMPLICIT);

                //Action Bar
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                findViewById(R.id.autocomplete_places).setVisibility(View.GONE);

                return true;

            }
        }
        if(id == R.id.maplist)
        {
            if(fragmentback)
            {
                //Animation
                flipCard();

                item.setIcon(R.drawable.ic_action_view_as_list);
                fragmentback = false;

            }
            else {

                //Animation
                flipCard();
                item.setIcon(R.drawable.ic_action_locate);
                fragmentback = true;

            }
        }

        return super.onOptionsItemSelected(item);

    }

    private void flipCard() {

        if (fragmentback) {
            getFragmentManager().popBackStack();
            return;
        }
        fragmentback = true;
        getFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in, R.anim.slide_in,
                            R.anim.slide_out, R.anim.slide_out)
                    .replace(R.id.content_frame, listFragment)
                    .addToBackStack(null)
                    .commit();
    }

    protected synchronized void buildGoogleApiClient() {

        googleapiclientlocation = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();

    }

    protected void createLocationRequest() {

        locationrequest = new LocationRequest();
        locationrequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationrequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(googleapiclientlocation, locationrequest, this);

    }

    protected void stopLocationUpdates() {

        LocationServices.FusedLocationApi.removeLocationUpdates(googleapiclientlocation, this);

    }


    @Override
    protected void onStart() {

        super.onStart();
        googleapiclientlocation.connect();
        googleapiclientplaces.connect();

    }

    @Override
    protected void onResume() {

        super.onResume();
        googleapiclientlocation.connect();
        googleapiclientplaces.connect();
        if (googleapiclientlocation.isConnected()) {
            startLocationUpdates();
        }

    }


    @Override
    protected void onStop() {

        super.onStop();
        if (googleapiclientlocation.isConnected()) {
            googleapiclientlocation.disconnect();
        }
        if(googleapiclientplaces.isConnected()) {
            googleapiclientlocation.disconnect();
        }

    }


    @Override
    public void onConnected(Bundle connectionHint) {

        if (currentLocation == null) {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleapiclientlocation);
        }
        if(currentLocation != null) {

            //Updating shared prefecerences
            bu.set_defaults("latitude", String.valueOf(currentLocation.getLatitude()));
            bu.set_defaults("longitude", String.valueOf(currentLocation.getLongitude()));

            latitude = String.valueOf(currentLocation.getLatitude());
            longitude = String.valueOf(currentLocation.getLongitude());

            // Calling map
            setUpMapIfNeeded();
         }
        else
        {
            buildGoogleApiClient();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        currentLocation = location;
        latitude = String.valueOf(currentLocation.getLatitude());
        longitude = String.valueOf(currentLocation.getLongitude());
        Log.d(TAG, "Location changed to " + latitude + " " + longitude);
        //Updating shared preferences
        bu.set_defaults("latitude", String.valueOf(currentLocation.getLatitude()));
        bu.set_defaults("longitude", String.valueOf(currentLocation.getLongitude()));

    }



    // For default
    private class ProcessRestaurants extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            UserFunctions userFunction = new UserFunctions();
            JSONObject json = userFunction.getRestaurants(latitude, longitude, bu.get_defaults("uid"));
            return json;

        }

        @Override
        protected void onPostExecute(JSONObject json) {

            List<RestaurantInfo> result = new ArrayList<RestaurantInfo>();
            try {

                if(fragmentback) {
                    listFragment.updateList(json.getJSONObject("restaurants"));
                }
                else{
                    listFragment = locationListViewFragment.newInstance(json.getJSONObject("restaurants"));
                }
                // Remove all markers before
                map.clear();
                eventMarkerMap.clear();
                // Creating Markers for Restaurants
                restaurantsJson = json.getJSONObject("restaurants");
                int noOfRestaurants = restaurantsJson.length();
                int i = 0;
                for (i = 0 ; i < noOfRestaurants ; i++) {
                    restaurantJson = null;
                    try {
                        restaurantJson = restaurantsJson.getJSONObject("'" + i + "'");
                        RestaurantInfo ci = new RestaurantInfo();
                        ci.resName = restaurantJson.getString("name");
                        ci.resAddress = restaurantJson.getString("address");
                        ci.phoneNumber = restaurantJson.getString("phone_number");
                        ci.reward = restaurantJson.getString("reward");
                        ci.coupon = restaurantJson.getString("coupon");
                        ci.latitude = restaurantJson.getString("latitude");
                        ci.longitude = restaurantJson.getString("longitude");
                        ci.resID = restaurantJson.getString("restaurant_id");
                        result.add(ci);

                        //Adding each restaurant marker
                        eventMarkerMap.put(map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant_marker))
                                .position(new LatLng(Double.valueOf(ci.latitude), Double.valueOf(ci.longitude)))
                                .title(ci.resName)), ci);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(normal_dialog != null)
                {
                    normal_dialog.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    // For favourite
    private class ProcessRestaurants_fav extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();
            JSONObject json = userFunction.getFavRestaurants(bu.get_defaults("uid"));
            return json;

        }

        @Override
        protected void onPostExecute(JSONObject json) {

            List<RestaurantInfo> result = new ArrayList<RestaurantInfo>();
            try {

                if(fragmentback) {
                    listFragment.updateList(json.getJSONObject("restaurants"));//
                }
                else{
                        listFragment = locationListViewFragment.newInstance(json.getJSONObject("restaurants"));
                }
                // Remove all markers before
                map.clear();
                eventMarkerMap.clear();
                // Creating Markers for Restaurants
                restaurantsJson = json.getJSONObject("restaurants");
                int noOfRestaurants = restaurantsJson.length();
                int i = 0;
                for (i = 0 ; i < noOfRestaurants ; i++) {
                    restaurantJson = null;
                    try {
                        restaurantJson = restaurantsJson.getJSONObject("'" + i + "'");
                        RestaurantInfo ci = new RestaurantInfo();
                        ci.resName = restaurantJson.getString("name");
                        ci.resAddress = restaurantJson.getString("address");
                        ci.phoneNumber = restaurantJson.getString("phone_number");
                        ci.reward = restaurantJson.getString("reward");
                        ci.coupon = restaurantJson.getString("coupon");
                        ci.latitude = restaurantJson.getString("latitude");
                        ci.longitude = restaurantJson.getString("longitude");
                        ci.resID = restaurantJson.getString("restaurant_id");
                        ci.isFav = "1";
                        result.add(ci);

                        //Adding each restaurant marker
                        eventMarkerMap.put(map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant_marker))
                                .position(new LatLng(Double.valueOf(ci.latitude), Double.valueOf(ci.longitude)))
                                .title(ci.resName)),ci);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // For popular
    private class ProcessRestaurants_pop extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();
            JSONObject json = userFunction.getPopRestaurants(latitude, longitude, bu.get_defaults("uid"));
            return json;

        }

        @Override
        protected void onPostExecute(JSONObject json) {

            List<RestaurantInfo> result = new ArrayList<RestaurantInfo>();
            try {

                if(fragmentback) {
                    listFragment.updateList(json.getJSONObject("restaurants"));//
                }
                else{
                    listFragment = locationListViewFragment.newInstance(json.getJSONObject("restaurants"));
                }
                // Remove all markers before
                map.clear();
                // Creating Markers for Restaurants
                restaurantsJson = json.getJSONObject("restaurants");
                int noOfRestaurants = restaurantsJson.length();
                int i = 0;
                for (i = 0 ; i < noOfRestaurants ; i++) {
                    restaurantJson = null;
                    try {
                        restaurantJson = restaurantsJson.getJSONObject("'" + i + "'");
                        RestaurantInfo ci = new RestaurantInfo();
                        ci.resName = restaurantJson.getString("name");
                        ci.resAddress = restaurantJson.getString("address");
                        ci.phoneNumber = restaurantJson.getString("phone_number");
                        ci.reward = restaurantJson.getString("reward");
                        ci.coupon = restaurantJson.getString("coupon");
                        ci.latitude = restaurantJson.getString("latitude");
                        ci.longitude = restaurantJson.getString("longitude");
                        ci.resID = restaurantJson.getString("restaurant_id");
                        ci.isFav = restaurantJson.getString("isFav");
                        result.add(ci);

                        //Adding each restaurant marker
                        map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant_marker))
                                .position(new LatLng(Double.valueOf(ci.latitude), Double.valueOf(ci.longitude)))
                                .title(ci.resName));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onConnectionSuspended(int cause) {

        showToast("Connection suspended");
        googleapiclientlocation.connect();

    }


    protected void showToast(String text) {

        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

    }

    private AdapterView.OnItemClickListener autocompleteclicklistener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = adapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("Place Autocomplete", "Autocomplete item selected: " + item.description);
            Places.GeoDataApi.getPlaceById(googleapiclientplaces, item.placeId.toString())
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(PlaceBuffer places) {
                            if (places.getStatus().isSuccess()) {
                                final Place myPlace = places.get(0);
                                destination = myPlace.getLatLng();
                                Log.d(TAG, "Place found: " + myPlace.getLatLng());
                                latitude = String.valueOf(destination.latitude);
                                longitude = String.valueOf(destination.longitude);
                                if(button_clicked.equals("favourite"))
                                {
                                    new ProcessRestaurants_fav().execute();
                                }
                                else if(button_clicked.equals("popular"))
                                {
                                    new ProcessRestaurants_pop().execute();
                                }
                                else {
                                    new ProcessRestaurants().execute();
                                }
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)))
                                        .zoom(13)
                                        .build();
                                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                          //      To be put in another activity afterwards
                          //      findDirections(Double.parseDouble(latitude), Double.parseDouble(longitude), destination.latitude, destination.longitude, GMapV2Direction.MODE_DRIVING);
                            }
                            places.release();
                        }
                    });
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(googleapiclientplaces, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {

        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();
                return;
            }
            places.release();
        }
    };

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        showToast("Could not connect to Google API Client: Error ");
    }



    public class CardFrontFragment extends android.app.Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Log.d("Fragment", "Showing front map fragment");
            if (view != null) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null)
                    parent.removeView(view);
            }
            try {
                view = inflater.inflate(R.layout.activity_maps_front, container, false);
                view.findViewById(R.id.marker_button).setPadding(0,0,0, (int) Math.round(height/21.3));
            }
            catch (InflateException e) {
            }
            fragmentback = false;
            return view;
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        if (googleapiclientlocation.isConnected()) {
            stopLocationUpdates();
        }

    }

    private void setUpMapIfNeeded() {
        if (map == null) {
            map = ((SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.maps)).getMap();
            if (map != null) {
                setUpMap();
            }
        }
    }


    private void setUpMap() {

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)))
                .zoom(13)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        // Call preocess
        normal_dialog = new ProgressDialog(MainActivity.this);
        normal_dialog.setTitle("Contacting Servers");
        normal_dialog.setMessage("Loading data ...");
        normal_dialog.setIndeterminate(false);
        normal_dialog.setCancelable(false);
        normal_dialog.show();
        normal_dialog.dismiss();
        new ProcessRestaurants().execute();
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition arg0) {
                map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        center_map = map.getCameraPosition().target;
                        latitude = String.valueOf(center_map.latitude);
                        longitude = String.valueOf(center_map.longitude);
                        if(button_clicked.equals("favourite"))
                        {
                            new ProcessRestaurants_fav().execute();
                        }
                        else if(button_clicked.equals("popular"))
                        {
                            new ProcessRestaurants_pop().execute();
                        }
                        else {
                            new ProcessRestaurants().execute();
                        }
                    }
                });
            }
        });
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                View v = getLayoutInflater().inflate(R.layout.marker_info, null);
                TextView title = (TextView) v.findViewById(R.id.title_name);
                title.setText(marker.getTitle());
                return v;
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                RestaurantInfo eventInfo = eventMarkerMap.get(marker);
                Intent i = new Intent(MainActivity.this, ResDetailsActivity.class);
                i.putExtra("res_latitude", String.valueOf(marker.getPosition().latitude));
                i.putExtra("res_longitude",String.valueOf(marker.getPosition().longitude));
                i.putExtra("res_ID",eventInfo.resID);
                i.putExtra("isFav", eventInfo.isFav);
                startActivity(i);
                finish();
            }
        });
    }

    public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);
        GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask();
        asyncTask.execute(map);
    }

    public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints)
    {
        Polyline newPolyline;
        GoogleMap mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.maps)).getMap();
        PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.BLUE);
        for(int i = 0 ; i < directionPoints.size() ; i++)
        {
            rectLine.add(directionPoints.get(i));
        }
        newPolyline = mMap.addPolyline(rectLine);
    }

    public class GetDirectionsAsyncTask extends AsyncTask<Map<String, String>, Object, ArrayList>
    {
        public static final String USER_CURRENT_LAT = "user_current_lat";
        public static final String USER_CURRENT_LONG = "user_current_long";
        public static final String DESTINATION_LAT = "destination_lat";
        public static final String DESTINATION_LONG = "destination_long";
        public static final String DIRECTIONS_MODE = "directions_mode";
        private Exception exception;

        public GetDirectionsAsyncTask()
        {

        }

        public void onPreExecute()
        {
        }

        @Override
        public void onPostExecute(ArrayList result)
        {
            if (exception == null)
            {
                handleGetDirectionsResult(result);
            }
            else
            {
                showToast("Please try after some time !");
            }
        }

        @Override
        protected ArrayList doInBackground(Map<String, String>... params)
        {
            Map<String, String> paramMap = params[0];
            try
            {
                LatLng fromPosition = new LatLng(Double.valueOf(paramMap.get(USER_CURRENT_LAT)) , Double.valueOf(paramMap.get(USER_CURRENT_LONG)));
                LatLng toPosition = new LatLng(Double.valueOf(paramMap.get(DESTINATION_LAT)) , Double.valueOf(paramMap.get(DESTINATION_LONG)));
                GMapV2Direction md = new GMapV2Direction();
                Document doc = md.getDocument(fromPosition, toPosition, paramMap.get(DIRECTIONS_MODE));
                ArrayList directionPoints = md.getDirection(doc);
                return directionPoints;
            }
            catch (Exception e)
            {
                exception = e;
                return null;
            }
        }
    }

    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            selectItem(position,  mDrawerItems.get(position - 1).getTag());
        }
    }

    private void selectItem(int position, int drawerTag) {
        if (position < 1) {
            return;
        }

        if(drawerTag == DrawerItem.DRAWER_ITEM_TAG_ORDERS){

            Intent login = new Intent(getApplicationContext(), OrdersActivity.class);
            startActivity(login);
        }
        else if(drawerTag == DrawerItem.DRAWER_ITEM_TAG_REWARDPOINTS){
            Intent login = new Intent(getApplicationContext(), RewardsActivity.class);
            startActivity(login);
        }
        else if(drawerTag == DrawerItem.DRAWER_ITEM_TAG_REDEEM){
            Intent login = new Intent(getApplicationContext(), RedeemActivity.class);
            startActivity(login);
        }
        else if(drawerTag == DrawerItem.DRAWER_ITEM_TAG_INVITE){
            Intent login = new Intent(getApplicationContext(), InviteActivity.class);
            startActivity(login);
        }
        else if(drawerTag == DrawerItem.DRAWER_ITEM_TAG_EDITPROFILE){
            Intent login = new Intent(getApplicationContext(), EditProfileActivity.class);
            startActivity(login);
        }
        else if(drawerTag == DrawerItem.DRAWER_ITEM_TAG_ABOUTUS){
            Intent login = new Intent(getApplicationContext(), AboutUSActivity.class);
            startActivity(login);
        }
        else if(drawerTag == DrawerItem.DRAWER_ITEM_TAG_LOGOUT){

            bu.clear_defaults();
            LoginManager.getInstance().logOut();
            Intent login = new Intent(getApplicationContext(), SplashScreensActivity.class);
            login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(login);
            finish();

        }

        String drawerTitle = getString(mDrawerItems.get(position - 1).getTitle());
        Toast.makeText(this, "You selected " + drawerTitle + " at position: " + position, Toast.LENGTH_SHORT).show();

        mDrawerList.setItemChecked(position, true);
        //setTitle(mDrawerItems.get(position - 1).getTitle());
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private class CommitFragmentRunnable implements Runnable {

        private Fragment fragment;

        public CommitFragmentRunnable(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void run() {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }
    }

    public void commitFragment(Fragment fragment) {
        mHandler.post(new CommitFragmentRunnable(fragment));
    }
    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}
