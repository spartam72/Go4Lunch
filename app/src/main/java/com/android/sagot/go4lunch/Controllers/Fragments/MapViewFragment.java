package com.android.sagot.go4lunch.Controllers.Fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.sagot.go4lunch.Controllers.Activities.RestaurantCardActivity;
import com.android.sagot.go4lunch.Controllers.Base.BaseFragment;
import com.android.sagot.go4lunch.Models.AdapterRestaurant;
import com.android.sagot.go4lunch.Models.Go4LunchViewModel;
import com.android.sagot.go4lunch.Models.firestore.Restaurant;
import com.android.sagot.go4lunch.R;
import com.android.sagot.go4lunch.Utils.Toolbox;
import com.android.sagot.go4lunch.api.RestaurantHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**************************************************************************************************
 *
 *  FRAGMENT that displays the Restaurant List
 *  ------------------------------------------
 *  IN = Last Know Location : Location
 *
 **************************************************************************************************/
public class MapViewFragment extends BaseFragment implements OnMapReadyCallback,  GoogleMap.OnInfoWindowClickListener {

    // For debug
    private static final String TAG = MapViewFragment.class.getSimpleName();

    // Parameter for the construction of the fragment
    private static final String KEY_LAST_KNOW_LOCATION = "KEY_LAST_KNOW_LOCATION";

    // For add Google Map in Fragment
    private SupportMapFragment mMapFragment;

    // ==> For use Api Google Play Service : map
    private GoogleMap mMap;

    // ==> For update UI Location
    private static final float DEFAULT_ZOOM = 13f;

    // Restaurants List
    Map<String,AdapterRestaurant> mListAdapterRestaurants;

    //==> For use Api Google Play Service : Location
    // _ The geographical location where the device is currently located.
    // _ That is, the last-known location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    public MapViewFragment() {
        // Required empty public constructor
    }
    // ---------------------------------------------------------------------------------------------
    //                               FRAGMENT INSTANTIATION
    // ---------------------------------------------------------------------------------------------
    public static MapViewFragment newInstance(Location lastKnownLocation) {

        // Create new fragment
        MapViewFragment mapViewFragment = new MapViewFragment();

        // Create bundle and add it some data
        Bundle args = new Bundle();
        final Gson gson = new GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .create();
        String json = gson.toJson(lastKnownLocation);
        args.putString(KEY_LAST_KNOW_LOCATION, json);

        mapViewFragment.setArguments(args);

        return mapViewFragment;
    }
    // ---------------------------------------------------------------------------------------------
    //                                    ENTRY POINT
    // ---------------------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");

        // Get data from Bundle (created in method newInstance)
        // Restoring the Date with a Gson Object
        Gson gson = new Gson();
        mLastKnownLocation = gson.fromJson(getArguments().getString(KEY_LAST_KNOW_LOCATION, ""),Location.class);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map_view, container, false);

        // Load Restaurant List of the ViewModel
        mListAdapterRestaurants = getCompleteMapAdapterRestaurantOfTheModel();

        // Configure the Maps Service of Google
        configurePlayServiceMaps();

        return rootView;
    }

    // ---------------------------------------------------------------------------------------------
    //                            GOOGLE PLAY SERVICE : MAPS
    // ---------------------------------------------------------------------------------------------
    public void configurePlayServiceMaps() {
        Log.d(TAG, "configurePlayServiceMaps: ");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (mMapFragment == null) {
            mMapFragment = SupportMapFragment.newInstance();
            mMapFragment.getMapAsync(this);
        }
        // Build the map
        getChildFragmentManager().beginTransaction().replace(R.id.fragment_map_view, mMapFragment).commit();
    }
    /**
     * Manipulates the map when it's available
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        mMap = googleMap;

        // Disable 3D Building
        mMap.setBuildingsEnabled(false);

        // Activate OnMarkerClickListener
        mMap.setOnInfoWindowClickListener(this);

        // Show current Location
        showCurrentLocation();

        // Display Restaurants Markers and activate Listen on the participants number
        displayAndListensMarkers();
    }
    // ---------------------------------------------------------------------------------------------
    //                                       ACTIONS
    // ---------------------------------------------------------------------------------------------
    // Click on Restaurants Map Marker
    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(TAG, "onMarkerClick: ");

        //Launch Restaurant Card Activity with restaurantIdentifier
        Toolbox.startActivity(getActivity(),RestaurantCardActivity.class,
                RestaurantCardActivity.KEY_DETAILS_RESTAURANT_CARD,
                marker.getTag().toString());
    }
    // ---------------------------------------------------------------------------------------------
    //                                       METHODS
    // ---------------------------------------------------------------------------------------------
    /**
     * Method that places the map on a current location
     */
    private void showCurrentLocation() {
        Log.d(TAG, "showCurrentLocation: ");
        Log.d(TAG, "showCurrentLocation: mLastKnownLocation.getLatitude()  = " + mLastKnownLocation.getLatitude());
        Log.d(TAG, "showCurrentLocation: mLastKnownLocation.getLongitude() = " + mLastKnownLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

        // Update Location UI
        updateLocationUI();
    }
    /**
     * Creating restaurant markers on the map and activating for each of them
     * a listener to change the number of participants in order to change their color in real time
     */
    public void displayAndListensMarkers() {
        Log.d(TAG, "fireStoreListener: ");
        mMap.clear();

        Log.d(TAG, "displayAndListensMarkers: mapAdapterRestaurant.size() = "+getCompleteMapAdapterRestaurantOfTheModel().size());
        Set<Map.Entry<String, AdapterRestaurant>> setListAdapterRestaurant = getCompleteMapAdapterRestaurantOfTheModel().entrySet();
        Iterator<Map.Entry<String, AdapterRestaurant>> it = setListAdapterRestaurant.iterator();
        while(it.hasNext()){
            Map.Entry<String, AdapterRestaurant> adapterRestaurant = it.next();

            Log.d(TAG, "fireStoreListener: identifier adapterRestaurant = "
                    +adapterRestaurant.getValue().getRestaurant().getIdentifier());

            // Declare a Marker for current Restaurant
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(adapterRestaurant.getValue().getRestaurant().getLat()),
                                    Double.parseDouble(adapterRestaurant.getValue().getRestaurant().getLng())
                            )
                    )
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant_marker_orange))
                    .title(adapterRestaurant.getValue().getRestaurant().getName())
            );
            marker.setTag(adapterRestaurant.getValue().getRestaurant().getIdentifier());

            // Save Marker reference in MapAdapterRestaurant in the Model
            //getMapAdapterRestaurantOfTheModel().get(adapterRestaurant.getKey()).setMarker(marker);
            adapterRestaurant.getValue().setMarker(marker);

            // Listen Nbr Participants
            listenNbrParticipantsForUpdateMarkers(adapterRestaurant.getValue().getRestaurant(),marker);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

        // Update Location UI
        MapViewFragment.this.updateLocationUI();
    }
    /**
     * Enables listening to the number of participants in each restaurant
     * to enable marker color change in real time
     */
    public void listenNbrParticipantsForUpdateMarkers(Restaurant restaurant,Marker marker) {
        Log.d(TAG, "listenNbrParticipantsForUpdateMarkers: ");

        RestaurantHelper
                .getRestaurantsCollection()
                .document(restaurant.getIdentifier())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot restaurant, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.d(TAG, "fireStoreListener.onEvent: Listen failed: " + e);
                            return;
                        }
                        if (restaurant != null) {
                            Log.d(TAG, "fireStoreListener.onEvent: identifier Restaurant = " + restaurant.get("identifier"));
                            Log.d(TAG, "fireStoreListener.onEvent: nbrParticipants = " + restaurant.get("nbrParticipants"));
                            if (Integer.parseInt(restaurant.get("nbrParticipants").toString()) == 0) {
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant_marker_orange));
                            } else {
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant_marker_green));
                            }
                        }
                    }
                });
    }
    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    public void updateLocationUI() {
        Log.d(TAG, "updateLocationUI: ");
        if (mMap != null) {
            try {
                Go4LunchViewModel model = ViewModelProviders.of(getActivity()).get(Go4LunchViewModel.class);
                Log.d(TAG, "updateLocationUI: isLocationPermissionGranted = "+model.isLocationPermissionGranted());
                if (model.isLocationPermissionGranted()) {
                    Log.d(TAG, "updateLocationUI: Permission Granted");
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                } else {
                    Log.d(TAG, "updateLocationUI: Permission not Granted");
                    mMap.setMyLocationEnabled(false);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            } catch (SecurityException e) {
                Log.e("updateLocationUI %s", e.getMessage());
            }
        }
    }
}
