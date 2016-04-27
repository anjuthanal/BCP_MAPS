package com.bcp.bcp.recyclerview;

import android.graphics.Color;
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
        String address = locationFenceTrackDetailsObj.getAddress();
        String addrTodisplay = address.replace(",","\n");
        if(locationFenceTrackDetailsObj.getStatus().contains("Exited"))
        {
            holder.address.setTextColor(Color.parseColor("#6699ff"));
            holder.address.setText(locationFenceTrackDetailsObj.getAddress());

        }else if(locationFenceTrackDetailsObj.getStatus().contains("Entered")){
            holder.address.setTextColor(Color.parseColor("#ffff00"));
            holder.address.setText(locationFenceTrackDetailsObj.getAddress());
        }else{
            holder.address.setTextColor(Color.parseColor("#ffffff"));
            holder.address.setTextSize(10);
            holder.address.setText(addrTodisplay);
        }
        holder.time.setText(locationFenceTrackDetailsObj.getTime());
        holder.time.setTextColor(Color.parseColor("#ffffff"));
        holder.time.setTextSize(10);

    }

    @Override
    public int getItemCount() {
        return locationFenceTrackDetails.size();
    }
}
