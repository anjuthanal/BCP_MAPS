package com.bcp.bcp.recyclerview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bcp.bcp.MainActivity;
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
    List<LocationFenceTrackDetails> samplelocFenDetailses = new ArrayList<LocationFenceTrackDetails>();
    List<LocationFenceTrackDetails> diplayList = new ArrayList<LocationFenceTrackDetails>();
    private LocationDetailsAdapter locationDetailsAdapter;
    DatabaseHandler databaseHandler;
    private SimpleDateFormat format;
    ImageView backarrow,infobtton;
    static final long DAY = 24 * 60 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_view_location);

        samplelocFenDetailses = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.loc_recycler_view);
        backarrow = (ImageView)findViewById(R.id.backarrow);
        infobtton = (ImageView)findViewById(R.id.info);

        locationDetailsAdapter = new LocationDetailsAdapter(diplayList);//pass new list combination of location and fence
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(locationDetailsAdapter);
        format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        prepareFenceDetailsOBject();


        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewLocationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


        infobtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(ViewLocationActivity.this);

                builder.setMessage("Label in Yellow colour shows Entry \n Lablel in blue shows Exits \n Lable with (B) indicates that data from beacons" +
                        "\n White colour label indicate out of office tracking details")
                        .setCancelable(false)
                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();
                            }
                        });

                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("Colour Scheme");
                alert.show();
            }
        });
    }


    class CallsComp implements Comparator<LocationFenceTrackDetails> {

        @Override
        public int compare(LocationFenceTrackDetails lhs, LocationFenceTrackDetails rhs) {
            try {
                Date lhsDate = format.parse(lhs.getTime());
                Date rhsDate = format.parse(rhs.getTime());
                return lhsDate.compareTo(rhsDate);
            } catch (Exception e) {
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
            getDataToDisplay(samplelocFenDetailses);
        }

        locationDetailsAdapter.notifyDataSetChanged();
    }



    public List<LocationFenceTrackDetails> getDataToDisplay(List<LocationFenceTrackDetails> samplelocFenDetailses){


        for(LocationFenceTrackDetails fenceTrackDetails :samplelocFenDetailses)
        {
            try {
                Date dateFromDb = format.parse(fenceTrackDetails.getTime());
                long dbmilli = dateFromDb.getTime();
                long cyurrDatemilli = new Date().getTime();
                if(dbmilli > cyurrDatemilli -DAY){

                    diplayList.add(fenceTrackDetails);
                }else{

                }

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.reverse(diplayList);
        return  diplayList;
    }
    private void prepareListView() {

        databaseHandler = new DatabaseHandler(this);
        fenceTimingList.addAll(databaseHandler.getAllFenceTiming());
        locationDataList.addAll(databaseHandler.getAllLocationData());


        locationDetailsAdapter.notifyDataSetChanged();
    }
}
