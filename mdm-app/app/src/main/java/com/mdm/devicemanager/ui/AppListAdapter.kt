package com.mdm.devicemanager.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mdm.devicemanager.R
import com.mdm.devicemanager.data.model.AppDetail

class AppListAdapter : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    private var apps: List<AppDetail> = emptyList()

    fun submitList(newApps: List<AppDetail>) {
        // Sort by category, then by app name
        apps = newApps.sortedWith(compareBy({ it.category }, { it.appName }))
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount(): Int = apps.size

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appName: TextView = itemView.findViewById(R.id.appName)
        private val packageName: TextView = itemView.findViewById(R.id.packageName)
        private val versionText: TextView = itemView.findViewById(R.id.versionText)
        private val appTypeBadge: TextView = itemView.findViewById(R.id.appTypeBadge)
        private val categoryBadge: TextView = itemView.findViewById(R.id.categoryBadge)

        fun bind(app: AppDetail) {
            appName.text = app.appName
            packageName.text = app.packageName
            versionText.text = "${app.versionName} (${app.versionCode})"

            // System/User badge
            if (app.isSystemApp) {
                appTypeBadge.text = itemView.context.getString(R.string.system_app)
                appTypeBadge.setBackgroundColor(Color.parseColor("#FFF3E0"))
                appTypeBadge.setTextColor(Color.parseColor("#E65100"))
            } else {
                appTypeBadge.text = itemView.context.getString(R.string.user_app)
                appTypeBadge.setBackgroundColor(Color.parseColor("#E3F2FD"))
                appTypeBadge.setTextColor(Color.parseColor("#1565C0"))
            }

            // Category badge
            val (bgColor, textColor) = getCategoryColors(app.category)
            categoryBadge.text = app.category
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor(bgColor))
                cornerRadius = 12f
            }
            categoryBadge.background = bg
            categoryBadge.setTextColor(Color.parseColor(textColor))
        }

        private fun getCategoryColors(category: String): Pair<String, String> {
            return when (category) {
                "Social Media"  -> "#E8EAF6" to "#283593"
                "Communication" -> "#E0F2F1" to "#00695C"
                "Entertainment" -> "#FCE4EC" to "#AD1457"
                "Games"         -> "#FFF3E0" to "#E65100"
                "Productivity"  -> "#E8F5E9" to "#2E7D32"
                "Shopping"      -> "#FFF8E1" to "#F57F17"
                "Finance"       -> "#E3F2FD" to "#1565C0"
                "Travel"        -> "#F3E5F5" to "#6A1B9A"
                "Health"        -> "#E8F5E9" to "#1B5E20"
                "Education"     -> "#E0F7FA" to "#00838F"
                "News"          -> "#ECEFF1" to "#37474F"
                "Photography"   -> "#FBE9E7" to "#BF360C"
                "Utilities"     -> "#F5F5F5" to "#424242"
                "Browser"       -> "#E1F5FE" to "#01579B"
                "System"        -> "#EFEBE9" to "#4E342E"
                else            -> "#F5F5F5" to "#616161"
            }
        }
    }
}
