package com.philkes.pin2pdf.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.philkes.pin2pdf.R;
import com.philkes.pin2pdf.model.PinModel;
import com.squareup.picasso.Picasso;

import java.util.List;


public class PinAdapter extends RecyclerView.Adapter<PinAdapter.ViewHolder> {
    private List<PinModel> pins;

    public PinAdapter(List<PinModel> pins) {
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
        private static final int IMG_SIZE=50;
        private final TextView titleView;
        private final ImageView imgView;

        private final Context context;


        boolean isImageFitToScreen=false;

        public ViewHolder(View view) {
            super(view);
            this.context=view.getContext();
            // Define click listener for the ViewHolder's View
            titleView=view.findViewById(R.id.pin_title);
            imgView=view.findViewById(R.id.pin_img);
            // TODO On image click show Fullscreen Image
           /* imgView.setOnClickListener((v)->{
                    if(isImageFitToScreen) {
                        isImageFitToScreen=false;
                        imgView.setLayoutParams(new LinearLayout.LayoutParams(IMG_SIZE, IMG_SIZE));
                        imgView.setAdjustViewBounds(true);
                    }else{
                        isImageFitToScreen=true;
                        imgView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                        imgView.setScaleType(ImageView.ScaleType.FIT_XY);
                    }

            });*/
        }

        public void updateData(PinModel pin) {
            titleView.setText(pin.getTitle());
            titleView.setOnClickListener(view1 -> {
                String link=pin.getPdfLink()!=null ? pin.getPdfLink() : pin.getLink();
                System.out.println("Open in Chrome: "+link);
                Intent browserIntent=new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                context.startActivity(browserIntent);
            });
            Picasso.get().load(pin.getImgUrl()).into(imgView);

        }
    }

}
