package com.android.sagot.go4lunch.Controllers.Activities;

import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.android.sagot.go4lunch.Controllers.Base.BaseActivity;
import com.android.sagot.go4lunch.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Michaël SAGOT on 15/08/2018.
 */

public class MainActivity extends BaseActivity {

    // For debugging Mode
    private static final String TAG = MainActivity.class.getSimpleName();

    // Adding @BindView in order to indicate to ButterKnife to get & serialise it
    // - Get Coordinator Layout
    @BindView(R.id.activity_main_coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    //@BindView(R.id.main_activity_facebook_login_button) Button mGoogleLoginButton;
    //@BindView(R.id.main_activity_google_login_button) Button mFaceBookLoginButton;

    // For Authentication
    // Identifier for Sign-In Activity
    private static final int RC_SIGN_IN = 100;

    // -------------------------
    // DECLARATION BASE METHODS
    // -------------------------
    // BASE METHOD Implementation
    // Get the activity layout
    // CALLED BY BASE METHOD 'onCreate(...)'
    @Override
    protected int getActivityLayout() {
        return R.layout.activity_main;
    }

    // BASE METHOD Implementation
    // Get the coordinator layout
    // CALLED BY BASE METHOD
    @Override
    protected int getCoordinatorLayout() {
        return R.id.activity_main_coordinator_layout;
    }

    // Change Color of the Status Toolbar
   /* public void changeStatusBarColor(){
        changeStatusBarColor(this,ContextCompat.getColor(this,R.color.colorPrimaryDark));
    }*/

    // --------------------
    // ACTIONS
    // --------------------

    @OnClick(R.id.main_activity_facebook_login_button)
    public void onClickFacebookLoginButton() {
        Log.d(TAG, "onClickFacebookLoginButton: ²");
        this.startSignInActivity();
    }

    @OnClick(R.id.main_activity_google_login_button)
    public void onClickGoogleLoginButton() {
        Log.d(TAG, "onClickGoogleLoginButton: ");
        this.startSignInActivity();
    }

    // --------------------
    //   AUTHENTICATION
    // --------------------

    // Launch Sign-In FireBase Activity for Result
    private void startSignInActivity(){
        Log.d(TAG, "startSignInActivity: ");
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(  new AuthUI.IdpConfig.GoogleBuilder().build()))    //GOOGLE
                         //                    new AuthUI.IdpConfig.FacebookBuilder().build())) // FACEBOOK
                        .setIsSmartLockEnabled(false, true) // Email list possible
                        .setLogo(R.drawable.pic_logo_restaurant_400x400)
                        .build(),
                RC_SIGN_IN);
    }

    // Method that retrieves the result of the authentication
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        super.onActivityResult(requestCode, resultCode, data);
        // 4 - Handle SignIn Activity response on activity result
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    // Method that handles response after SignIn Activity close
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){
        Log.d(TAG, "handleResponseAfterSignIn: ");

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {   // = 100
            if (resultCode == RESULT_OK) { // SUCCESS = -1
                showSnackBar(this.mCoordinatorLayout, getString(R.string.connection_succeed));
                // Call Welcome Activity
                startWelcomeActivity();
            } else { // ERRORS
                if (response == null) {
                    showSnackBar(this.mCoordinatorLayout, getString(R.string.error_authentication_canceled));
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {     // = 1
                    showSnackBar(this.mCoordinatorLayout, getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.PROVIDER_ERROR) { // = 4
                    showSnackBar(this.mCoordinatorLayout, getString(R.string.error_provider));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {   // = 0
                    showSnackBar(this.mCoordinatorLayout, getString(R.string.error_unknown_error));
                }
            }
        }
    }

    // --------------------
    //   CALL ACTIVITY
    // -------------------
    private void startWelcomeActivity(){
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }

    // --------------------
    // UI
    // --------------------

    // Show Snack Bar with a message
    private void showSnackBar(CoordinatorLayout coordinatorLayout, String message){
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }
}
