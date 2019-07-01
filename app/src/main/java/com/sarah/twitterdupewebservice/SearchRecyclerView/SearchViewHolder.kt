package com.sarah.twitterdupewebservice.SearchRecyclerView

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sarah.twitterdupewebservice.R
import kotlinx.android.synthetic.main.item_search_rvlist.view.*

class SearchViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var searchImage: ImageView
    var searchName:TextView


    init {
        this.searchImage =itemView.findViewById(R.id.ivSearchProfile)
        this.searchName = itemView.findViewById(R.id.tvSearchName)


    }
}