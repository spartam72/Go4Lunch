package com.android.sagot.go4lunch.Controllers.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;

import com.android.sagot.go4lunch.Controllers.Base.BaseActivity;
import com.android.sagot.go4lunch.Models.sharedPreferences.Preferences_SettingsActivity;
import com.android.sagot.go4lunch.R;
import com.android.sagot.go4lunch.Utils.Toolbox;
import com.android.sagot.go4lunch.api.UserHelper;
import com.android.sagot.go4lunch.notifications.NotificationsAlarmReceiver;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnTouch;

public class SettingsActivity extends BaseActivity {

    // For Debug
    private static final String TAG = "SettingsActivity";

    // For Retrieve SharedPreferences of the Activity
    // -----------------------------------------------
    // Create the key data for retrieve preferences of the Activity
    public static final int SETTINGS_ACTIVITY_RC = 300;
    public static final String SHARED_PREF_SETTINGS_ACTIVITY = "SHARED_PREF_SETTINGS_ACTIVITY";
    private Preferences_SettingsActivity mPreferences_SettingsActivity;

    // Adding @BindView in order to indicate to ButterKnife to get & serialise it
    @BindView(R.id.activity_settings_coordinatorLayout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.activity_settings_notification) Switch mNotificationSwitch;
    @BindView(R.id.activity_settings_radius_search) EditText mRadiusSearchEditText;
    @BindView(R.id.activity_settings_button) Button mButton;
    @BindView(R.id.activity_settings_radio_button_sort_by_name) RadioButton mNameRadioButton;
    @BindView(R.id.activity_settings_radio_button_sort_by_distance) RadioButton mDistanceRadioButton;
    @BindView(R.id.activity_settings_radio_button_sort_by_nbr_likes) RadioButton mNbLikesRadioButton;
    @BindView(R.id.activity_settings_radio_button_sort_by_nbr_participants) RadioButton mNbParticipantsRadioButton;

    // Identify each Http Request
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;

    // Creating an intent to execute our broadcast
    private PendingIntent mPendingIntent;
    // Creating alarmManager
    private AlarmManager mAlarmManager;

    // For Switch manage
    private boolean mNotificationStatus;

    // ---------------------------------------------------------------------------------------------
    //                                DECLARATION BASE METHODS
    // ---------------------------------------------------------------------------------------------
    // BASE METHOD Implementation
    // Get the activity layout
    // CALLED BY BASE METHOD 'onCreate(...)'
    @Override
    protected int getActivityLayout() {
        return R.layout.activity_settings;
    }

    // BASE METHOD Implementation
    // Get the coordinator layout
    // CALLED BY BASE METHOD
    @Override
    protected View getCoordinatorLayout() {
        return mCoordinatorLayout;
    }
    // ---------------------------------------------------------------------------------------------
    //                                      ENTRY POINT
    // ---------------------------------------------------------------------------------------------
    // ----------------------
    // OVERRIDE BASE METHODS
    // ----------------------
    // OVERRIDE BASE METHOD : onCreate(Bundle savedInstanceState)
    // To add the call to configureAlarmManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve SharedPreferences
        retrieveSharedPreferences();

        // Configure Toolbar
        this.configureToolBar();

        //Configuring The AlarmManager
        this.configureAlarmManager();

        //Manage Distance Edit Text
        manageDistanceEditText();
    }
    // ---------------------------------------------------------------------------------------------
    //                                   SHARED PREFERENCES
    // ---------------------------------------------------------------------------------------------
    // >> SHARED PREFERENCES RETRIEVE <-------
    private void retrieveSharedPreferences() {
        Log.d(TAG, "retrievesPreferences: ");

        // Get back Intent send to parameter by the MainActivity
        Intent intent = getIntent();
        String sharedPreferences = intent.getStringExtra(SHARED_PREF_SETTINGS_ACTIVITY);
        Log.d(TAG, "retrieveSharedPreferences: sharedPreferences = "+sharedPreferences);
        // Restoring the preferences with a Gson Object
        Gson gson = new Gson();
        mPreferences_SettingsActivity = gson.fromJson(  sharedPreferences,
                                                        Preferences_SettingsActivity.class);
        Log.d(TAG, "retrieveSharedPreferences: searchRadius = "+mPreferences_SettingsActivity.getSearchRadius());
        Log.d(TAG, "retrieveSharedPreferences: notification = "+mPreferences_SettingsActivity.isNotificationStatus());
        Log.d(TAG, "retrieveSharedPreferences: sortChoice   = "+mPreferences_SettingsActivity.getSortChoice());
        // Retrieve Notification Status
        getAndCheckNotificationStatus();

        // Retrieve Radius search
        getAndCheckRadiusSearch();

        // Retrieve SortChoice
        getAndCheckSortChoice();
    }
    // Retrieve the notification status of the SharedPreferences
    // And check it in UI
    private void getAndCheckNotificationStatus(){
        Log.d(TAG, "getAndCheckNotificationStatus: ");

        // Retrieve Notification Status
        mNotificationStatus = mPreferences_SettingsActivity.isNotificationStatus();
        Log.d(TAG, "getAndCheckNotificationStatus: notification = "+mNotificationStatus);
        // Check it in UI
        mNotificationSwitch.setChecked(mNotificationStatus);
    }
    // Retrieve the Radius Search of the SharedPreferences
    // And check it in UI
    private void getAndCheckRadiusSearch(){
        Log.d(TAG, "getAndCheckRadiusSearch: ");

        // Retrieve Radius Search
        String radiusSearch = mPreferences_SettingsActivity.getSearchRadius();
        // Check it in UI
        mRadiusSearchEditText.setText(radiusSearch);
    }
    // Retrieve the Sort Choice of the SharedPreferences
    // And check it in UI
    private void getAndCheckSortChoice(){
        Log.d(TAG, "getAndCheckSortChoice: ");

        // Retrieve Radius Search
        String sortChoice = mPreferences_SettingsActivity.getSortChoice();
        // Check it in UI
        switch (sortChoice){
            case "name"             : mNameRadioButton.setChecked(true);break;
            case "distance"         : mDistanceRadioButton.setChecked(true);break;
            case "nbLikes"          : mNbLikesRadioButton.setChecked(true);break;
            case "nbParticipants"   : mNbParticipantsRadioButton.setChecked(true);break;
        }
    }
    // ---------------------------------------------------------------------------------------------
    //                                     TOOLBAR
    // ---------------------------------------------------------------------------------------------
    protected void configureToolBar() {
        Log.d(TAG, "configureToolBar: ");

        // Change the toolbar Title
        setTitle(R.string.activity_welcome_title);
        // Sets the Toolbar
        setSupportActionBar(mToolbar);
    }
    // ---------------------------------------------------------------------------------------------
    //                                       ACTIONS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------
    // ACTION Switch Notification
    // ---------------------------
    @OnCheckedChanged(R.id.activity_settings_notification)
    public void OnCheckedChanged(CompoundButton cb, boolean isChecked){
        Log.d(TAG, "OnCheckedChanged: ");
        Log.d(TAG, "OnCheckedChanged: isChecked        = "+isChecked);
        Log.d(TAG, "OnCheckedChanged: isChecked Before = "+mNotificationStatus);

        // If check switch and old state is not checked
        if (isChecked && !mNotificationStatus)
            Snackbar.make(mCoordinatorLayout,"Notifications set !",Snackbar.LENGTH_LONG).show();
        if (!isChecked && mNotificationStatus)
            Snackbar.make(mCoordinatorLayout,"Notifications canceled !",Snackbar.LENGTH_LONG).show();

        mNotificationStatus = isChecked;

        Log.d(TAG, "OnCheckedChanged: isChecked After  = " +mNotificationStatus);
    }
    // --------------------------
    // ACTION Distance Edit Text
    // --------------------------
    private void manageDistanceEditText() {
        Log.d(TAG, "manageDistanceEditText: ");

        //The distance entered must be between 1 and 1500 meters
        mRadiusSearchEditText.setFilters(new InputFilter[]{new Toolbox.MinMaxFilter("1", "1500")});
    }
    // --------------------------
    // ACTION Sort Radio Buttons
    // --------------------------
    @OnCheckedChanged({ R.id.activity_settings_radio_button_sort_by_name,
                        R.id.activity_settings_radio_button_sort_by_distance,
                        R.id.activity_settings_radio_button_sort_by_nbr_likes,
                        R.id.activity_settings_radio_button_sort_by_nbr_participants})
    public void onRadioButtonSortCheckChanged(CompoundButton button, boolean checked) {
        Log.d(TAG, "onRadioButtonSortCheckChanged: ");

        Log.d(TAG, "onRadioButtonSortCheckChanged: button  = "+button.getId());
        Log.d(TAG, "onRadioButtonSortCheckChanged: checked = "+checked);
        switch (button.getId()){
            case R.id.activity_settings_radio_button_sort_by_name:
                Log.d(TAG, "onRadioButtonSortCheckChanged: NAME");
                if (checked) mPreferences_SettingsActivity.setSortChoice("name");
                    break;
            case R.id.activity_settings_radio_button_sort_by_distance:
                Log.d(TAG, "onRadioButtonSortCheckChanged: DISTANCE");
                if (checked) mPreferences_SettingsActivity.setSortChoice("distance");
                    break;
            case R.id.activity_settings_radio_button_sort_by_nbr_likes:
                Log.d(TAG, "onRadioButtonSortCheckChanged: LIKES");
                if (checked) mPreferences_SettingsActivity.setSortChoice("nbLikes");
                    break;
            case R.id.activity_settings_radio_button_sort_by_nbr_participants:
                Log.d(TAG, "onRadioButtonSortCheckChanged: PARTICIPANTS");
                if (checked) mPreferences_SettingsActivity.setSortChoice("nbParticipants");
                    break;
        }
    }
    // -------------------------
    // ACTION Validation Button
    // -------------------------
    @OnTouch(R.id.activity_settings_button)
    public boolean onTouchValidateButton(View v, MotionEvent event) {

        // If check or not checked Switch Notification
        if (mPreferences_SettingsActivity.isNotificationStatus()) this.startAlarm();
        if (!mPreferences_SettingsActivity.isNotificationStatus()) this.stopAlarm();

        // Manages the result back to the caller
        builtReturnResult();

        // Finish the Activity
        finish();
        return false;
    }
    // -----------------------------
    // ACTION Delete Account Button
    // -----------------------------
    @OnTouch(R.id.activity_settings_delete_account_button)
    public boolean onTouchDeleteAccountButton(View v, MotionEvent event) {
        // Manages the result back to the caller
        builtReturnResult();

        // Delete User in FireBase
        deleteUserFromFireBase();
        return false;
    }
    // ---------------------------------------------------------------------------------------------
    //                                   RETURN RESULTS
    // ---------------------------------------------------------------------------------------------
    // Manages the result back to the caller
    private void builtReturnResult(){
        // Return to the Welcome Activity
        Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
        Intent intent = new Intent();
        mPreferences_SettingsActivity.setNotificationStatus(mNotificationStatus);
        mPreferences_SettingsActivity.setSearchRadius(mRadiusSearchEditText.getText().toString());
        Log.d(TAG, "onTouchValidateButton: notification = "+mPreferences_SettingsActivity.isNotificationStatus());
        Log.d(TAG, "onTouchValidateButton: searchRadius = "+mPreferences_SettingsActivity.getSearchRadius());
        intent.putExtra(SHARED_PREF_SETTINGS_ACTIVITY, gson.toJson(mPreferences_SettingsActivity));
        setResult(RESULT_OK, intent);
    }
    // ---------------------------------------------------------------------------------------------
    //                                    REST REQUESTS
    // ---------------------------------------------------------------------------------------------
    // Create http requests (SignOut & Delete)
    private void deleteUserFromFireBase(){
        if (this.getCurrentUser() != null) {

            // Delete User in FireBase Database
            UserHelper.deleteUser(getCurrentUser().getUid());

            // Delete User in Authentication
            AuthUI.getInstance()
                    .delete(this)
                    .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));
        }
    }
    // Create OnCompleteListener called after tasks ended
    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return aVoid -> finish();
    }
    // ---------------------------------------------------------------------------------------------
    //                                     NOTIFICATION
    // ---------------------------------------------------------------------------------------------
    // ----------------------------
    // CONFIGURATION ALARM MANAGER
    // ----------------------------
    private void configureAlarmManager(){
        Log.d(TAG, "configureAlarmManager: ");
        // Create the Intent to destination of the BroadcastReceiver
        Intent alarmIntent = new Intent(SettingsActivity.this,
                NotificationsAlarmReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(SettingsActivity.this, 0,
                alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    // ------------------------------
    // SCHEDULE TASK  : AlarmManager
    // ------------------------------
    // @Return The hour it milliseconds of release of the first alarm
    private long nextNotification() {
        Log.d(TAG, "nextNotification: ");

        Calendar cal = Calendar.getInstance();
        // If it is after noon then we add one day to the meter of release of the alarm
        if (cal.get(Calendar.HOUR_OF_DAY) > 12 ) cal.add(Calendar.DATE, 1);
        // The alarm next one will thus be at 12:00 am tomorrow
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        cal.set(Calendar.MILLISECOND, 0);
        //The hour it milliseconds of release of the first alarm
        return cal.getTimeInMillis();
    }
    // Start Alarm
    private void startAlarm() {
        Log.d(TAG, "startAlarm: ");

        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.setInexactRepeating( AlarmManager.RTC_WAKEUP,     // which will wake up the device when it goes off
                nextNotification(),          // First start at 12:00
                mAlarmManager.INTERVAL_DAY,  // Will trigger every day
                mPendingIntent);
    }
    // Stop Alarm
    private void stopAlarm() {
        Log.d(TAG, "stopAlarm: ");

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(mPendingIntent);
    }
}
