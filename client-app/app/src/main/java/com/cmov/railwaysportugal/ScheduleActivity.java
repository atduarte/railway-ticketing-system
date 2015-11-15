package com.cmov.railwaysportugal;

import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.transition.Scene;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    protected static Lines[] lines;

    RequestQueue queue;
    JsonArrayRequest jsObjRequest ;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    static Spinner linesname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        //get lines

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        queue = Volley.newRequestQueue(ScheduleActivity.this);
        String url ="http://54.186.113.106/timetable";

        jsObjRequest  = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray  response) {

                        Gson gson = new Gson();
                        lines = gson.fromJson(response.toString(), Lines[].class);


                        // Create the adapter that will return a fragment for each of the three
                        // primary sections of the activity.
                        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

                        // Set up the ViewPager with the sections adapter.
                        mViewPager = (ViewPager) findViewById(R.id.container);
                        mViewPager.setAdapter(mSectionsPagerAdapter);





                    }
                },  new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        } ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Authorization", Config.token);
                return params;
            }
        };

        jsObjRequest.setTag("TIMETABLE");

        queue.add(jsObjRequest);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return lines.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return "LINE Nº"+new Integer(position+1).toString();

        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

            TextView directions = (TextView) rootView.findViewById(R.id.directions);
            linesname = (Spinner) rootView.findViewById(R.id.linesspinner);
            ArrayAdapter<String> stations_adapter;
            ArrayList<String> stations = new ArrayList<>();
            final int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER)-1;
            directions.setText(lines[sectionNumber].from + "->"+lines[sectionNumber].to);
            for (int i = 0; i < lines[sectionNumber].timetables.length ; i++)
            {
                stations.add(String.format("%02d:%02d", lines[sectionNumber].timetables[i].departure / 60, lines[sectionNumber].timetables[i].departure % 60) + " "+" "+String.format("%02d:%02d", lines[sectionNumber].timetables[i].arrival / 60, lines[sectionNumber].timetables[i].arrival % 60) );
            }
            stations_adapter = new ArrayAdapter<String>(container.getContext(), R.layout.spinner_layout, stations);
            linesname.setAdapter(stations_adapter);

            linesname.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // create list
                    ArrayList<String> listtrips = new ArrayList<String>();
                    for (int i = 0; i < lines[sectionNumber].timetables[position].stations.length; i++) {
                        listtrips.add(lines[sectionNumber].timetables[position].stations[i].name + " " + String.format("%02d:%02d", lines[sectionNumber].timetables[position].stations[i].departure / 60, lines[sectionNumber].timetables[position].stations[i].departure % 60));
                    }
                    ListView directions = (ListView) rootView.findViewById(R.id.listView);
                    ArrayAdapter<String> adapter2;
                    adapter2 = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, listtrips);
                    directions.setAdapter(adapter2);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });


            return rootView;
        }
    }
}
