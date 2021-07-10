package com.philkes.pin2pdf.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.philkes.pin2pdf.R;
import com.philkes.pin2pdf.model.PinModel;
import com.philkes.pin2pdf.storage.local.service.DBService;
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
        private final Button btnOpen;
        private final ImageButton btnNotes;
        private final EditText txtNotes;

        private final Context context;
        private final InputMethodManager imm;

        private boolean isImageFitToScreen=false;
        private boolean notesEditable=false;

        private static final int drawable_save=android.R.drawable.ic_menu_save;
        private static final int drawable_edit=android.R.drawable.ic_menu_edit;

        private final DBService dbService;


        public ViewHolder(View view) {
            super(view);
            context=view.getContext();
            imm=(InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            dbService=DBService.getInstance(context);

            // Define click listener for the ViewHolder's View
            titleView=view.findViewById(R.id.pin_title);
            imgView=view.findViewById(R.id.pin_img);
            btnOpen=view.findViewById(R.id.open_recipe);
            btnNotes=view.findViewById(R.id.edit_notes);
            txtNotes=view.findViewById(R.id.txt_notes);

            txtNotes.setInputType(InputType.TYPE_NULL);


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

        public void updateUI() {
            if(notesEditable) {
                // Make txtNotes editable, focused and show Soft Keyboard
                txtNotes.setInputType(InputType.TYPE_CLASS_TEXT);
                btnNotes.setImageDrawable(context.getResources().getDrawable(drawable_save));
                txtNotes.requestFocus();
                imm.showSoftInput(txtNotes, InputMethodManager.SHOW_IMPLICIT);
            }
            else {
                // Make txtNotes read only + hide Soft Keyboard
                txtNotes.setInputType(InputType.TYPE_NULL);
                btnNotes.setImageDrawable(context.getResources().getDrawable(drawable_edit));
                imm.hideSoftInputFromWindow(txtNotes.getWindowToken(), 0);
            }
        }

        public void updateData(PinModel pin) {
            titleView.setText(pin.getTitle());
            txtNotes.setText(pin.getNote());
            btnOpen.setOnClickListener(view1 -> {
                String link=pin.getPdfLink()!=null ? pin.getPdfLink() : pin.getLink();
                System.out.println("Open in Chrome: " + link);
                Intent browserIntent=new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                context.startActivity(browserIntent);
            });
            btnNotes.setOnClickListener((v) -> {
                if(notesEditable) {
                    pin.setNote(txtNotes.getText().toString());
                    dbService.updatePin(pin, null);
                }
                notesEditable=!notesEditable;
                updateUI();
            });
            Picasso.get().load(pin.getImgUrl()).into(imgView);

        }
    }

}
