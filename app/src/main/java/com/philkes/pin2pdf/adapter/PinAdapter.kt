package com.philkes.pin2pdf.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.philkes.pin2pdf.R
import com.philkes.pin2pdf.model.PinModel
import com.squareup.picasso.Picasso

class PinAdapter(private val pins: List<PinModel>) : RecyclerView.Adapter<PinAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.pin_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.updateData(pins[i])
    }

    override fun getItemCount(): Int {
        return pins.size
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleView: TextView
        private val imgView: ImageView
        private val context: Context
        var isImageFitToScreen = false
        fun updateData(pin: PinModel) {
            titleView.text = pin.title
            titleView.setOnClickListener { view1: View? ->
                val link = if (pin.pdfLink != null) pin.pdfLink else pin.link
                println("Open in Chrome: $link")
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                context.startActivity(browserIntent)
            }
            Picasso.get().load(pin.imgUrl).into(imgView)
        }

        companion object {
            private const val IMG_SIZE = 50
        }

        init {
            context = view.context
            // Define click listener for the ViewHolder's View
            titleView = view.findViewById(R.id.pin_title)
            imgView = view.findViewById(R.id.pin_img)
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
    }
}