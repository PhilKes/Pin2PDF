package com.philkes.pin2pdf.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.philkes.pin2pdf.R;
import com.philkes.pin2pdf.model.Pin;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PinAdapter extends RecyclerView.Adapter<PinAdapter.ViewHolder> {
    private List<Pin> pins;

    public PinAdapter(List<Pin> pins) {
        this.pins=pins;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view=LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.pin_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.updateData(pins.get(i));
    }

    @Override
    public int getItemCount() {
        return pins.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final ImageView imgView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            titleView=view.findViewById(R.id.pin_title);
            imgView=view.findViewById(R.id.pin_img);
        }

        public void updateData(Pin pin){
            titleView.setText(pin.getTitle());
            Picasso pic=Picasso.get();
            pic.setLoggingEnabled(true);
            pic.load(pin.getImgUrl()).into(imgView);
        }
    }

}
