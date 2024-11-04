package com.example.runapps.authentication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runapps.R
import com.example.runapps.activity.RecentActivity

class RecentActivityAdapter(private val activityList: List<RecentActivity>) :
    RecyclerView.Adapter<RecentActivityAdapter.RecentActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentActivityViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_activity, parent, false)
        return RecentActivityViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecentActivityViewHolder, position: Int) {
        val currentItem = activityList[position]
        holder.dateTextView.text = currentItem.date
        holder.distanceTextView.text = currentItem.distance
        holder.detailsTextView.text = "${currentItem.calories} kcal   ${currentItem.speed} km/hr"
    }

    override fun getItemCount() = activityList.size

    class RecentActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.activityDate)
        val distanceTextView: TextView = itemView.findViewById(R.id.activityDistance)
        val detailsTextView: TextView = itemView.findViewById(R.id.activityDetails)
        val arrowIcon: ImageView = itemView.findViewById(R.id.arrowIcon)
    }
}