package com.example.checkcertificate.ui.home

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.checkcertificate.R

class AppListAdapter(var onClick: (ApplicationInfo) -> Unit) :
    ListAdapter<ApplicationInfo, AppListAdapter.ViewHolder>(AppDiffCallback) {
    class ViewHolder(view: View, private val context: Context) : RecyclerView.ViewHolder(view) {
        private val tvAppName=view.findViewById<TextView>(R.id.tv_app_name)
        private val ivAppImage=view.findViewById<ImageView>(R.id.iv_app_image)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(appInfo:ApplicationInfo, onClick: (ApplicationInfo) -> Unit){
            tvAppName.text=appInfo.packageName
            Glide.with(context)
                .load(appInfo.icon)
                .override(50, 50)
                .placeholder(R.drawable.setting)
                .centerCrop()
                .into(ivAppImage)

            itemView.setOnClickListener {
                onClick.invoke(appInfo)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.app_item, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    object AppDiffCallback : DiffUtil.ItemCallback<ApplicationInfo>() {
        override fun areItemsTheSame(oldItem: ApplicationInfo, newItem: ApplicationInfo): Boolean {
            return oldItem.packageName==newItem.packageName
        }

        override fun areContentsTheSame(
            oldItem: ApplicationInfo,
            newItem: ApplicationInfo
        ): Boolean {
            return oldItem.packageName==newItem.packageName
        }
    }
}