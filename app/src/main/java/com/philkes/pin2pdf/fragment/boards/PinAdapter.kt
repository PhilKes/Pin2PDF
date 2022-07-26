package com.philkes.pin2pdf.fragment.boards

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.philkes.pin2pdf.R
import com.squareup.picasso.Picasso

/**
 * [androidx.recyclerview.widget.RecyclerView.Adapter] for ListView of [PinModel] with custom [ViewGroup]
 */
class PinAdapter(private val pins: List<PinModel>, private val onPinUpdated: (PinModel) -> Unit) :
    RecyclerView.Adapter<PinAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.pin_item, viewGroup, false)
        return ViewHolder(view, onPinUpdated)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.updateData(pins[i])
    }

    override fun getItemCount(): Int {
        return pins.size
    }

    class ViewHolder(view: View, private val onPinUpdated: (PinModel) -> Unit) : RecyclerView.ViewHolder(view) {
        private val titleView: TextView
        private val imgView: ImageView
        private val btnOpen: Button
        private val btnNotes: ImageButton
        private val txtNotes: EditText
        private val context: Context
        private val imm: InputMethodManager
        private val isImageFitToScreen = false
        private var notesEditable = false

        fun updateUI() {
            with(txtNotes) {
                if (notesEditable) {
                    // Make txtNotes editable, focused and show Soft Keyboard
                    inputType = InputType.TYPE_CLASS_TEXT
                    btnNotes.setImageDrawable(context.resources.getDrawable(drawable_save))
                    requestFocus()
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                } else {
                    // Make txtNotes read only + hide Soft Keyboard
                    inputType = InputType.TYPE_NULL
                    btnNotes.setImageDrawable(context.resources.getDrawable(drawable_edit))
                    imm.hideSoftInputFromWindow(windowToken, 0)
                }
            }
        }

        fun updateData(pin: PinModel) {
            with(pin){
                titleView.text = title
                txtNotes.setText(note)
                titleView.setOnClickListener { openInBrowser(this) }
                btnOpen.setOnClickListener { openInBrowser(this) }
                btnNotes.setOnClickListener {
                    if (notesEditable) {
                        note = txtNotes.text.toString()
                        onPinUpdated(this);
                    }
                    notesEditable = !notesEditable
                    updateUI()
                }
                Picasso.get().load(imgUrl).into(imgView)
            }

        }

        private fun openInBrowser(pin: PinModel?) {
            val link = if (pin!!.pdfLink != null) pin.pdfLink else pin.link
            Log.d(TAG, String.format("Open in Chrome: %s", link))
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            context.startActivity(browserIntent)
        }

        companion object {
            private const val TAG = "PinViewHolder"
            private const val IMG_SIZE = 50
            private const val drawable_save = android.R.drawable.ic_menu_save
            private const val drawable_edit = android.R.drawable.ic_menu_edit
        }

        init {
            context = view.context
            imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // Define click listener for the ViewHolder's View
            titleView = view.findViewById(R.id.pin_title)
            imgView = view.findViewById(R.id.pin_img)
            btnOpen = view.findViewById(R.id.open_recipe)
            btnNotes = view.findViewById(R.id.edit_notes)
            txtNotes = view.findViewById(R.id.txt_notes)
            txtNotes.inputType = InputType.TYPE_NULL


/*            imgView.setOnClickListener((v)->{
                    if(isImageFitToScreen) {
                        isImageFitToScreen=false;
                        imgView.setLayoutParams(new ConstraintLayout.LayoutParams(IMG_SIZE, IMG_SIZE));
                        imgView.setAdjustViewBounds(true);
                    }else{
                        isImageFitToScreen=true;
                        imgView.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
                        imgView.setScaleType(ImageView.ScaleType.FIT_XY);
                    }

            });*/
        }
    }
}