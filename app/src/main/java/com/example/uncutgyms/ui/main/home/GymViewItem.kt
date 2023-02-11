package com.example.uncutgyms.ui.main.home

import com.example.uncutgyms.R
import com.example.uncutgyms.models.YelpBusiness
import com.example.uncutgyms.ui.main.util.ViewItem

sealed class GymViewItem(
    open val id: String,
    open val type: GymViewItemType
) : ViewItem<GymViewItem> {

    override fun compareTo(other: GymViewItem): Int = this.id.compareTo(other.id)

    override fun areContentsTheSame(other: GymViewItem): Boolean = this == other

    override fun areItemsTheSame(other: GymViewItem): Boolean = type == other.type && id == other.id

    data class GymListItem(
        override val id: String,
        val business: YelpBusiness
    ) : GymViewItem(
        id = id,
        type = GymViewItemType.GYM_LIST_ITEM,
    )
}

enum class GymViewItemType(
    val layoutId: Int,
) {
    GYM_LIST_ITEM(R.layout.gym_list_view_item),
}