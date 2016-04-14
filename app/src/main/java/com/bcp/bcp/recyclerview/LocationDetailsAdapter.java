package com.bcp.bcp.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bcp.bcp.R;
import com.bcp.bcp.database.FenceTiming;

import java.util.List;

/**
 * Created by anjup on 4/14/16.
 */
public class LocationDetailsAdapter extends RecyclerView.Adapter<LocationDetailsAdapter.MyViewHolder>  {

    private List<LocationFenceTrackDetails> locationFenceTrackDetails;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView address,  time;

        public MyViewHolder(View view) {
            super(view);
            address = (TextView) view.findViewById(R.id.textView);
            time = (TextView) view.findViewById(R.id.textView2);
        }
    }

    public LocationDetailsAdapter(List<LocationFenceTrackDetails> locationFenceTrackDetails) {
        this.locationFenceTrackDetails = locationFenceTrackDetails;
    }

    @Override
    public LocationDetailsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_details, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LocationDetailsAdapter.MyViewHolder holder, int position) {
        LocationFenceTrackDetails locationFenceTrackDetailsObj = locationFenceTrackDetails.get(position);
        holder.address.setText(locationFenceTrackDetailsObj.getAddress());
        holder.time.setText(locationFenceTrackDetailsObj.getTime());

    }

    @Override
    public int getItemCount() {
        return locationFenceTrackDetails.size();
    }
}
