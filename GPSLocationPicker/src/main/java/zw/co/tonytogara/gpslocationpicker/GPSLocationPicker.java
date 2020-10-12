package zw.co.tonytogara.gpslocationpicker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import zw.co.tonytogara.gpslocationpicker.constants.LibConstants;

import static android.content.Context.LOCATION_SERVICE;

public class GPSLocationPicker
{

    // location
    public LocationManager mLocationManager;
    public LocationListener mLocationListener;
    public int mLocationRequestCount;

    // location variables
    public static String USER_LOCATION, LOCATION_ADDRESS, LOCATION_STREET_NAME, CITY, STATE, COUNTRY;
    public static boolean isLocationFound = false;

    // permission fix
    public boolean isPermissionFirstLoading = false, isGPSEnabled = false;

    // context & activity
    private Context context;
    private Activity activity;

    public GPSLocationPicker(Context context, Activity activity)
    {
        this.context = context;
        this.activity = activity;
    }

    // request for location
    public void requestLocation()
    {
        LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        assert locationManager != null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            isGPSEnabled = true;
        }else{
            showGPSDisabledAlertToUser();
        }

        // check if GPS is enabled
        if (isGPSEnabled)
        {
            // check if GPS permissions have been granted (RunTime Permission checking)
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // permission first time
                isPermissionFirstLoading = true;

                Toast.makeText(activity, "Location Permission is required for this app to run.", Toast.LENGTH_LONG).show();

                // trigger run time request permission dialog (If dialog does not appear, make sure the permission has also been added in your manifest)
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else
                {
                    // retrieve user location
                    retrieveLocation();
            }
        }else
        {
            // GPS not activated
            Toast.makeText(activity, LibConstants.GPS_REQUIRED, Toast.LENGTH_LONG).show();
        }
    }

    // SHOW GPS ALERT TO USER
    private void showGPSDisabledAlertToUser()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage("GPS is disabled on this device. Enable it to proceed.")
                .setCancelable(false)
                .setPositiveButton("Goto Settings To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                activity.startActivity(callGPSSettingIntent);

                                // show load page
                                Snackbar.make(
                                        activity.findViewById(android.R.id.content),
                                        "GPS not enabled.",
                                        Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Reload App", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
//                            Toast.makeText(getBaseContext(), "Trigger retrieve", Toast.LENGTH_LONG).show();
                                                activity.startActivity(new Intent(context, activity.getClass()));
                                                activity.finish();
                                            }
                                        }).show();
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    // trigger retrieve location
    public void retrieveLocation()
    {
        // reset location found
        if (isLocationFound)
        {
            isLocationFound = false;
        }

        mLocationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                Log.d(LibConstants.LOG_SYSTEM_STATUS, "USER_LOCATION - " + location.toString() + "@ " + mLocationRequestCount);

                // check request type 0 = First Time request, 1 = another type of request
                if (mLocationRequestCount == 4)
                {
                    // set user location
                    USER_LOCATION = location.getLatitude() + "," + location.getLongitude();

                    // set isLocationFound
                    isLocationFound = true;
                }

                if(mLocationRequestCount < 5)
                {
                    mLocationRequestCount++;

                    if (mLocationRequestCount == 4)
                    {
                        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                        try
                        {
                            List<Address> listAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                            if (listAddress != null && listAddress.size() > 0)
                            {

                                String address = listAddress.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                String city = listAddress.get(0).getLocality();
                                String state = listAddress.get(0).getAdminArea();
                                String country = listAddress.get(0).getCountryName();
                                String streetName = listAddress.get(0).getThoroughfare();

                                Log.d(LibConstants.LOG_SYSTEM_STATUS, "LOCATION_ADDRESS - " +
                                        listAddress.get(0).toString() + " ; Address - " + address +
                                        "; City - " + city + "; state - " + state + "; Country - " +
                                        country + "; Street - " + streetName);

                                LOCATION_ADDRESS = address;
                                LOCATION_STREET_NAME = streetName;
                                CITY = city;
                                STATE = state;
                                COUNTRY = country;
                            }
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                else
                {
                    mLocationManager.removeUpdates(mLocationListener);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else
        {
            try
            {
                mLocationRequestCount = 0;
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            }catch (Exception e)
            {
                Toast.makeText(activity, "Unable to access setup device, please contact admin!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
