package com.example.billingkiosk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.billingkiosk.model.AppItem

class AppAdapter(
    private val appList: List<AppItem>,
    private val onToggle: (AppItem) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    inner class AppViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvAppName)
        val cbAllowed: CheckBox = view.findViewById(R.id.cbAllow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(v)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = appList[position]
        holder.tvName.text = app.label
        holder.cbAllowed.setOnCheckedChangeListener(null) // penting: cegah re-binding bug
        holder.cbAllowed.isChecked = app.isAllowed
        holder.cbAllowed.setOnCheckedChangeListener { _, isChecked ->
            app.isAllowed = isChecked
            onToggle(app)
        }
    }

    override fun getItemCount(): Int = appList.size
}
