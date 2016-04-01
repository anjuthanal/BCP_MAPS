package com.bcp.bcp.recyclerview;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.bcp.bcp.R;
import com.bcp.bcp.database.DatabaseHandler;
import com.bcp.bcp.database.FenceTiming;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by anjup on 4/1/16.
 */
public class FenceTimingActivity extends AppCompatActivity {

    private List<FenceDetailsOBject> fenceDetailsOBjectArrayList = new ArrayList<>();
    List<FenceTiming> fenceTimingList = new ArrayList<FenceTiming>();
    private RecyclerView recyclerView;
    private FenceAdapter mAdapter;
    DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fencedetails);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new FenceAdapter(fenceTimingList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        prepareFenceDetailsOBject();
    }

    private void prepareFenceDetailsOBject() {

        databaseHandler = new DatabaseHandler(this);
        fenceTimingList.addAll(databaseHandler.getAllFenceTiming());

        for(FenceTiming fenceTiming:fenceTimingList)
        {
            Log.e("fence",fenceTiming.getFenceAddress() +" : "+fenceTiming.getStatus()+" : "+fenceTiming.getDatetime());
        }


        mAdapter.notifyDataSetChanged();
    }
}
