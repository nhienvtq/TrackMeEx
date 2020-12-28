package com.example.trackmeex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trackmeex.ImageBitmapString.StringToBitMap
import com.example.trackmeex.data.Section
import kotlinx.android.synthetic.main.custome_section.view.*


class sectionRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var sectionList: List<Section> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): RecyclerView.ViewHolder {
        return sectionViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.custome_section,parent,false))
    }

    override fun getItemCount(): Int {
        return sectionList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is sectionViewHolder ->{
                holder.bind(sectionList.get(position))
            }
        }
    }

    fun setData(section: List<Section>){
        this.sectionList = section
        notifyDataSetChanged()
    }

    //construct and binding database and recycler views
    class sectionViewHolder constructor(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(section: Section) {
            itemView.distanceRecycler.text = section.distance
            itemView.speedRecycler.text = section.speed
            itemView.timeRecycler.text = section.time
            itemView.bitmapimageView.setImageBitmap(StringToBitMap(section.image))
        }
    }
}