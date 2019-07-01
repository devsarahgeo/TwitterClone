package com.sarah.twitterdupewebservice.SearchRecyclerView

import android.content.Context
import android.content.Intent
import android.support.annotation.NonNull
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Filter
import android.widget.Filterable
import com.sarah.twitterdupewebservice.Model.SearchDataModel
import com.sarah.twitterdupewebservice.OtherUsersProfile
import com.sarah.twitterdupewebservice.Profile
import com.sarah.twitterdupewebservice.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.tweets_ticket.view.*

class SearchAdapter(val context:Context,val searchList:ArrayList<SearchDataModel>):RecyclerView.Adapter<SearchViewHolder>(),Filterable {
    internal var filterListResult:ArrayList<SearchDataModel>
    init {
        this.filterListResult=searchList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val layoutView: View = LayoutInflater.from(parent.context).inflate(R.layout.item_search_rvlist, parent, false)
        val lp: RecyclerView.LayoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutView.layoutParams = lp
        return SearchViewHolder(layoutView)
    }

    override fun getItemCount(): Int {
        return  filterListResult.count()
    }
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(charString: CharSequence?): FilterResults {
                val charSearch=charString.toString()
                if(charSearch.isEmpty()){
                    filterListResult= searchList
                }else{
                    val resultList = ArrayList<SearchDataModel>()
                    for(row in searchList){
                        if(row.searchName.toLowerCase().contains(charSearch.toLowerCase()))
                            resultList.add(row)
                    }
                    filterListResult=resultList
                }
                val filterResults= Filter.FilterResults()
                filterResults.values=filterListResult
                return filterResults
            }
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                filterListResult=filterResults!!.values as ArrayList<SearchDataModel>
                notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
       val searchListPosn = filterListResult[position]
        holder.searchName.text = searchListPosn.searchName

        Picasso.with(context).load(searchListPosn.searchImage).into(holder.searchImage)
        holder.itemView.setOnClickListener{
            val intent = Intent(context,OtherUsersProfile::class.java)
            intent.putExtra("name",searchListPosn.searchName)
            intent.putExtra("image",searchListPosn.searchImage)
            intent.putExtra("users_user_id",searchListPosn.search_user_id)

            context.startActivity(intent)
        }
    }


}