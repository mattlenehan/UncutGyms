package com.example.uncutgyms.ui.main.home

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import com.example.uncutgyms.R
import com.example.uncutgyms.databinding.GymListViewItemBinding
import com.example.uncutgyms.ui.main.util.roundedCorners

internal sealed class GymViewHolder(bindings: ViewBinding) :
    RecyclerView.ViewHolder(bindings.root) {

    class GymListViewHolder(
        private val bindings: GymListViewItemBinding,
    ) : GymViewHolder(bindings) {
        fun bind(item: GymViewItem.GymListItem, onClick: (String) -> Unit) {
            val resources = bindings.root.resources
            bindings.name.text = item.business.name
            bindings.price.text = item.business.price ?: resources.getString(R.string.n_a)
            bindings.distance.text = resources.getString(
                R.string.x_miles_away,
                String.format(
                    "%.2f",
                    item.business.distance?.toDouble()?.div(1609.344)
                )
            )
            if (item.business.imageUrl.isNotEmpty()) {
                bindings.icon.load(item.business.imageUrl) {
                    roundedCorners()
                }
            } else {
                bindings.icon.load(R.drawable.ic_launcher_background) {
                    roundedCorners()
                }
            }
            bindings.root.setOnClickListener {
                onClick(item.business.id)
            }
        }
    }
}