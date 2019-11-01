package com.example.forwalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GpsAdapter extends RecyclerView.Adapter<GpsAdapter.ViewHolder>{

    private List<GpsItem> mGpsTempArray;

    public GpsAdapter(List<GpsItem> GpsList){
        mGpsTempArray = GpsList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tCard1.setText(Integer.toString(mGpsTempArray.get(position).getNum()));
        holder.tCard2.setText(mGpsTempArray.get(position).getTime());
    }

    public int getItemCount(){
        return mGpsTempArray.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tCard1, tCard2;

        public ViewHolder(View itemView){
            super(itemView);
            tCard1 = (TextView) itemView.findViewById(R.id.tCard1);
            tCard2 = (TextView) itemView.findViewById(R.id.tCard2);
        }
    }
}
