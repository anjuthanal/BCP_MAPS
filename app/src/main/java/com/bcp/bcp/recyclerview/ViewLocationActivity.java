package com.bcp.bcp.recyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.bcp.bcp.R;
import com.bcp.bcp.database.DatabaseHandler;
import com.bcp.bcp.database.FenceTiming;
import com.bcp.bcp.database.LocationData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by anjup on 4/14/16.
 */
public class ViewLocationActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    List<FenceTiming> fenceTimingList = new ArrayList<FenceTiming>();
    List<LocationData> locationDataList = new ArrayList<LocationData>();
    List<LocationFenceTrackDetails> locationFenceTrackDetailses = new ArrayList<LocationFenceTrackDetails>();
    List<LocationFenceTrackDetails> samplelocFenDetailses = new ArrayList<LocationFenceTrackDetails>();
    private LocationDetailsAdapter locationDetailsAdapter;
    DatabaseHandler databaseHandler;
    private SimpleDateFormat format;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_location);
        samplelocFenDetailses = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.loc_recycler_view);

        locationDetailsAdapter = new LocationDetailsAdapter(samplelocFenDetailses);//pass new list combination of location and fence
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(locationDetailsAdapter);
        format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        prepareFenceDetailsOBject();
    }

    class CallsComp implements Comparator<LocationFenceTrackDetails> {

        @Override
        public int compare(LocationFenceTrackDetails lhs, LocationFenceTrackDetails rhs) {
            try {
                Date lhsDate = format.parse(lhs.getTime());
                Date rhsDate = format.parse(rhs.getTime());
                return lhsDate.compareTo(rhsDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }


    private void prepareFenceDetailsOBject() {

        databaseHandler = new DatabaseHandler(this);
        fenceTimingList.addAll(databaseHandler.getAllFenceTiming());
        locationDataList.addAll(databaseHandler.getAllLocationData());

        if (fenceTimingList != null || locationDataList != null) {
            for (FenceTiming fenceTiming : fenceTimingList) {
                LocationFenceTrackDetails trackDetails = new LocationFenceTrackDetails();
                trackDetails.setAddress(fenceTiming.getFenceAddress());
                trackDetails.setStatus(fenceTiming.getStatus());
                trackDetails.setTime(fenceTiming.getDatetime());
                samplelocFenDetailses.add(trackDetails);
            }
            for (LocationData locationData : locationDataList) {
                LocationFenceTrackDetails trackDetails = new LocationFenceTrackDetails();
                trackDetails.setAddress(locationData.getLocAddress());
                trackDetails.setStatus("");
                trackDetails.setTime(locationData.getLocDatetime());
                samplelocFenDetailses.add(trackDetails);
            }
            Collections.sort(samplelocFenDetailses, new CallsComp());
        }


        locationDetailsAdapter.notifyDataSetChanged();
    }

    private void prepareListView() {

        databaseHandler = new DatabaseHandler(this);
        fenceTimingList.addAll(databaseHandler.getAllFenceTiming());
        locationDataList.addAll(databaseHandler.getAllLocationData());


        locationDetailsAdapter.notifyDataSetChanged();
    }
}
