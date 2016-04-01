package com.bcp.bcp.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bcp.bcp.R;

import java.util.List;

/**
 * Created by anjup on 3/31/16.
 */
public class FenceAdapter extends RecyclerView.Adapter<FenceAdapter.MyViewHolder>  {

    private List<FenceDetailsOBject> fenceDetailsOBjectList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView address, status, time;

        public MyViewHolder(View view) {
            super(view);
            address = (TextView) view.findViewById(R.id.address);
            status = (TextView) view.findViewById(R.id.status);
            time = (TextView) view.findViewById(R.id.dtime);
        }
    }
    public FenceAdapter(List<FenceDetailsOBject> fenceDetailsOBjectList) {
        this.fenceDetailsOBjectList = fenceDetailsOBjectList;
    }

    @Override
    public FenceAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_fence, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FenceAdapter.MyViewHolder holder, int position) {
        FenceDetailsOBject fenceDetailsOBject = fenceDetailsOBjectList.get(position);
        holder.address.setText(fenceDetailsOBject.getfAddress());
        holder.status.setText(fenceDetailsOBject.getfStatus());
        holder.time.setText(fenceDetailsOBject.getfTime());

    }

    @Override
    public int getItemCount() {
        return fenceDetailsOBjectList.size();
    }
}
