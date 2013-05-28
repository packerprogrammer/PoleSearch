package com.glps.polesearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class MainActivity extends FragmentActivity implements
        ActionBar.TabListener,
        LocationListener,
        GooglePlayServicesClient.OnConnectionFailedListener,
        GooglePlayServicesClient.ConnectionCallbacks
        {

    // Global constants (first 5 specifically for location services)

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;

    private static Handler hm;
    private static int GRID_SEARCH = 1;
    private static int POLE_SEARCH = 2;
    private int iFirstColIndex;
    private int iSecondColIndex;
    private TextView latitudeField;
    private TextView longitudeField;
    final PoleDatabase datasource = new PoleDatabase(this);
    private static final int TEXT_VIEW_CNT = 5;
    private TextView[] textViewArray = new TextView[TEXT_VIEW_CNT];

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);


            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        hm = new Handler()
        {
            public void handleMessage(Message m) {
                if (m.what == 0)
                {
                    long t = m.getData().getLong("time");
                    String s = "Load Done. " + t/1000 + " s";
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                }

            }
        };

        //datasource.open();

        latitudeField = (TextView) findViewById(R.id.TextView02);
        longitudeField = (TextView) findViewById(R.id.TextView04);

        textViewArray[0] = (TextView) findViewById(R.id.TextView05);
        textViewArray[1] = (TextView) findViewById(R.id.TextView06);
        textViewArray[2] = (TextView) findViewById(R.id.TextView07);
        textViewArray[3] = (TextView) findViewById(R.id.TextView08);
        textViewArray[4] = (TextView) findViewById(R.id.TextView09);
    }

    public static Handler returnHandler()
    {
        return hm;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onLocationChanged(Location location) {
      // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment; // = null;
            if (position == 0){
                fragment = new SearchFragment();
            }
            else {
                fragment = new NearbyFragment();
            }

            //Bundle args = new Bundle();
            //args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
            //fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);

            }
            return null;
        }
    }
    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public static class SearchFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public SearchFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_search, container, false);
            //TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            //dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public static class NearbyFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public NearbyFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);

            //TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            //dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    private boolean ValidData(EditText e){
        //Check the syntax of the data entered in the search box

        //make sure the user entered at least 3 chars
        if (e.length() >= 3) {
            return true;
        }//if
        else {
            return false;
        }//else
    }//ValidData

    private int SearchType(RadioGroup r){

        int idGridPole = r.getCheckedRadioButtonId();

        switch (idGridPole) {
            case -1:
                return -1;
            case R.id.radioGrid:
                iFirstColIndex = 2;
                iSecondColIndex = 3;
                return GRID_SEARCH;
            case R.id.radioPole:
                iFirstColIndex = 3;
                iSecondColIndex = 2;
                return POLE_SEARCH;
            default:
                return -1;
        }



    }//SearchType
    //Button Clicks
    public void SearchClick (View view){

        int iSearchType;
        String Temp;


        //create object editText and populate it with the text field edit_message
        EditText editText = (EditText) findViewById(R.id.edit_message);
        if (ValidData(editText) == false) {
            Toast.makeText(getApplicationContext(), "3 Character Minimum", Toast.LENGTH_LONG).show();
            return;
        }//if validdata

        RadioGroup RadioGroupSearchType = (RadioGroup) findViewById(R.id.radioSearchType);
        iSearchType = SearchType(RadioGroupSearchType);
        if (iSearchType == -1){
            Toast.makeText(getApplicationContext(), "Grid or Pole?", Toast.LENGTH_LONG).show();
            return;
        }//if SearchType


        String sGridLoc = editText.getText().toString();


        final Cursor PoleValue = datasource.getPole(sGridLoc, iSearchType);
        List<String> listItems = new ArrayList<String>();


        if (PoleValue == null) {
            final AlertDialog.Builder NullBuilder=new AlertDialog.Builder(this);
            long rows = datasource.getRows();
            NullBuilder
                    .setTitle("No Records")
                    .setMessage("No Records were Found. If you have recently updated the application it's possible all poles have yet to be loaded." +
                            "There are currently " + rows + " poles loaded.")
                    .setCancelable(false)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close
                            // current activity
                            dialog.cancel();
                        }
                    });
            AlertDialog NullAlert = NullBuilder.create();
            NullAlert.show();

            return;
        }
        if (PoleValue.moveToFirst()){
            do {
                Temp=PoleValue.getString(iFirstColIndex) + "-" + PoleValue.getString(iSecondColIndex);
                listItems.add(Temp);
            } while  (PoleValue.moveToNext());
        }//if PoleValue
        else{
            PoleValue.close();
            Toast.makeText(getApplicationContext(), "No Record Found", Toast.LENGTH_LONG).show();
            return;
        }//else


        //setContentView(R.layout.activity_main);
        final CharSequence[] PoleList = listItems.toArray(new CharSequence[listItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Pole");
        builder.setItems(PoleList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(getApplicationContext(), PoleList[item], Toast.LENGTH_SHORT).show();

                PoleValue.moveToPosition(item);
                if (!SendToGoogleMaps(PoleValue.getDouble(0),PoleValue.getDouble(1),PoleList[item].toString())){
                    Toast.makeText(getApplicationContext(), "Unable to Launch Map", Toast.LENGTH_LONG).show();
                    return;
                }

            }// onClick
        });
        AlertDialog alert = builder.create();
        alert.show();
    }//else if rowcount

    public boolean SendToGoogleMaps (double xpos, double ypos, String label){
        String query = ypos + "," + xpos + "(" + label + ")";
        String enquery = Uri.encode(query);
        String geoloc = "geo:" + ypos +", "+ xpos + "?q=" + enquery;
        Uri location = Uri.parse(geoloc); // z param is zoom level
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
        // Verify it resolves
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start an activity if it's safe
        if (isIntentSafe) {
            startActivity(mapIntent);
            return true;
        }//if isIntentSafe
        return false;
    }

}
