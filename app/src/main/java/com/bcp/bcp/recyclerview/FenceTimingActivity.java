package com.bcp.bcp.recyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.bcp.bcp.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by anjup on 4/1/16.
 */
public class FenceTimingActivity extends AppCompatActivity {

    private List<FenceDetailsOBject> fenceDetailsOBjectArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FenceAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fencedetails);
       /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new FenceAdapter(fenceDetailsOBjectArrayList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        prepareFenceDetailsOBject();
    }

    private void prepareFenceDetailsOBject() {
        FenceDetailsOBject fenceDetailsOBject = new FenceDetailsOBject("Google Office", "Entry", "2015");
        fenceDetailsOBjectArrayList.add(fenceDetailsOBject);

        fenceDetailsOBject = new FenceDetailsOBject("Google Office", "Exit", (new Date()).toString());
        fenceDetailsOBjectArrayList.add(fenceDetailsOBject);

        fenceDetailsOBject = new FenceDetailsOBject("DLF HYD", "Entry", "2015");
        fenceDetailsOBjectArrayList.add(fenceDetailsOBject);

        fenceDetailsOBject = new FenceDetailsOBject("DLF HYD", "Exit", "2015");
        fenceDetailsOBjectArrayList.add(fenceDetailsOBject);

        fenceDetailsOBject = new FenceDetailsOBject("CHN HYD", "Entry", "2015");
        fenceDetailsOBjectArrayList.add(fenceDetailsOBject);

        fenceDetailsOBject = new FenceDetailsOBject("CHN HYD", "Exit", "2015");
        fenceDetailsOBjectArrayList.add(fenceDetailsOBject);

        mAdapter.notifyDataSetChanged();
    }
}
