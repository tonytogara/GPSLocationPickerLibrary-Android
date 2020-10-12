# GPSLocationPickerLibrary-Android
Android GPS Location Picker Library 

Retrieve device GPS Location using a few simple steps. 

Accessing Library
Gradle:
- implementation 'com.github.tonytogara:GPSLocationPickerLibrary-Android:0b7e1d2'

Permissions (Manifest)
- Uses Fine Location: <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>

Usage

private GPSLocationPicker mGPSLocationPicker;


private ProgressDialog pDialog;

@Override
protected void onCreate(Bundle savedInstanceState)
{
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create GPS location Picker Library reference
    mGPSLocationPicker = new GPSLocationPicker(MainActivity.this, MainActivity.this);

    // request for a location
    mGPSLocationPicker.requestLocation();

    // check Location by running a 10 seconds counter
    checkLocation();
}

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
{
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            if (mGPSLocationPicker.isPermissionFirstLoading)
            {
                mGPSLocationPicker.isPermissionFirstLoading = false;
                Toast.makeText(getBaseContext(), "Permissions Granted!", Toast.LENGTH_LONG).show();

                mGPSLocationPicker.retrieveLocation();

                // check Location by running a 10 seconds counter
                checkLocation();
            }else
            {
                mGPSLocationPicker.mLocationRequestCount = 0;
                mGPSLocationPicker.mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, mGPSLocationPicker.mLocationListener);
            }
        }else
        {
            Toast.makeText(getBaseContext(), "Location Permission is required for this app to run.", Toast.LENGTH_LONG).show();
        }
    }
}

// check if location has been picked up
private void checkLocation()
{
    // only start timer if permissions are available
    if (!mGPSLocationPicker.isPermissionFirstLoading && mGPSLocationPicker.isGPSEnabled)
    {
        // show loading dialog
        showLoadingDialog();

        // run a count down timer and check if location has been found.
        Thread startTimer = new Thread()
        {
            public void run ()
            {

                try
                {
                    int seconds = 10;
                    sleep(1000 * seconds);

                    if (GPSLocationPicker.isLocationFound)
                    {
                        Log.d("SYSTEM_STATUS", GPSLocationPicker.USER_LOCATION);

                        // show Toast Message using main thread
                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                Toast.makeText(getBaseContext(), "Location Found - " +
                                        GPSLocationPicker.USER_LOCATION + " ; " +
                                        GPSLocationPicker.LOCATION_STREET_NAME, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else
                    {
                        Log.d("SYSTEM_STATUS", "Location not found");

                        // show Toast Message using main thread
                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                Toast.makeText(getBaseContext(), "Location not found", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    hidepDialog();
                }catch (InterruptedException e){
//                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "Oops!, something went wrong, contact admin.", Toast.LENGTH_SHORT).show();
                }
            }

        };
        startTimer.start();
    }else
    {
        // the library will automatically prompt the user to allow permissions
    }
}

// show progress dialog
private void showLoadingDialog()
{
    pDialog = new ProgressDialog(MainActivity.this);
    pDialog.setIndeterminate(true);
    pDialog.setMessage("Please wait while loading....");
    pDialog.setCancelable(false);

    if (!pDialog.isShowing())
        pDialog.show();
}

// hide loading dialog
private void hidepDialog()
{
    if (pDialog.isShowing())
        pDialog.dismiss();
}



