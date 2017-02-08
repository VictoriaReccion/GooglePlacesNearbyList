package com.example.android.googleplacesnearbylist.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.googleplacesnearbylist.R;
import com.example.android.googleplacesnearbylist.model.MyResults;

import java.util.List;

import static android.R.id.list;

/**
 * Created by victo on 2/7/2017.
 */

public class PlacesAdapter extends RecyclerView.Adapter {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private List<MyResults> placeList;
    private LayoutInflater inflater;

    public PlacesAdapter(List<MyResults> placeList, Context context) {
        this.placeList = placeList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == VIEW_ITEM) {
            View view = inflater.inflate(R.layout.place_item, parent, false);
            return new PlaceHolder(view);
        } else if(viewType == VIEW_PROG){
            View view = inflater.inflate(R.layout.progress_item, parent, false);
            return new ProgressHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof PlaceHolder) {
            MyResults myResults = placeList.get(position);
            ((PlaceHolder)holder).lbl_place_name.setText(myResults.getName());
            ((PlaceHolder)holder).lbl_place_desc.setText(myResults.getVicinity());
            ((PlaceHolder)holder).lbl_place_dist.setText(myResults.getDistance());
        } else {
            ((ProgressHolder)holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return placeList.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public void setItems(List<MyResults> placeList) {
        this.placeList = placeList;
    }

    private class PlaceHolder extends RecyclerView.ViewHolder {

        public TextView lbl_place_name;
        public TextView lbl_place_desc;
        public TextView lbl_place_dist;

        public PlaceHolder(View itemView) {
            super(itemView);

            lbl_place_name = (TextView) itemView.findViewById(R.id.lbl_place_name);
            lbl_place_desc = (TextView) itemView.findViewById(R.id.lbl_place_snippet);
            lbl_place_dist = (TextView) itemView.findViewById(R.id.lbl_place_distance);
        }
    }

    private class ProgressHolder extends RecyclerView.ViewHolder {

        public ProgressBar progressBar;

        public ProgressHolder(View itemView) {
            super(itemView);

            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }

}
