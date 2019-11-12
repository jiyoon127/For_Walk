package com.example.forwalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GpsAdapter extends RecyclerView.Adapter<GpsAdapter.ViewHolder> {

    static private List<GpsItem> mGpsTempArray;

    public GpsAdapter(List<GpsItem> GpsList){
        mGpsTempArray = GpsList;
    }

    OnItemClickListener listener;
    public static interface OnItemClickListener{
        public void onItemClick(ViewHolder holder, View view, int position);
    }

    static public GpsItem getItem(int position){
        return mGpsTempArray.get(position);
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
        holder.setOnItemClickListener(listener);
    }



    public int getItemCount(){
        return mGpsTempArray.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tCard1, tCard2;
        public CardView cv;

        OnItemClickListener listener;

        public ViewHolder(final View itemView){
            super(itemView);
            tCard1 = (TextView) itemView.findViewById(R.id.tCard1);
            tCard2 = (TextView) itemView.findViewById(R.id.tCard2);
            cv = (CardView) itemView.findViewById(R.id.cardView);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(listener!=null){
                        listener.onItemClick(ViewHolder.this, itemView, position);
                    }
                }
            });
        }

        public void setOnItemClickListener(OnItemClickListener listener){
            this.listener = listener;
        }
    }
}
