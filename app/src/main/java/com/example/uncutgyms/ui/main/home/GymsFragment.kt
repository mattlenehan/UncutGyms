package com.example.uncutgyms.ui.main.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uncutgyms.databinding.FragmentGymsBinding
import com.example.uncutgyms.databinding.GymListViewItemBinding
import com.example.uncutgyms.ui.main.MainViewModel
import com.example.uncutgyms.ui.main.util.ApiResult
import com.example.uncutgyms.ui.main.util.requestLocationPermission
import com.example.uncutgyms.ui.main.util.showSnackbar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GymsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    internal val mainViewModel by activityViewModels<MainViewModel>()

    private var userPermanentlyDeniedLocPermissions = false

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher = this.requestLocationPermission(
        userPermanentlyDeniedLocPermissions = userPermanentlyDeniedLocPermissions,
        onGrantedCallback = {
            map.isMyLocationEnabled = true
            mainViewModel.findLocation()
        },
        askConfirmationToSkip = {},
        onUserPermanentlyDenied = { userPermanentlyDeniedLocPermissions = true }
    )

    private val gymsAdapter by lazy {
        ListAdapter(listener = adapterListener)
    }

    private var _binding: FragmentGymsBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGymsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.map.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.map.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.map.onCreate(savedInstanceState)
        init()
        subscribeUi()
    }

    private fun init() {
        binding.map.getMapAsync(this)

        binding.recycler.apply {
            adapter = gymsAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }

        binding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab?.position) {
                        0 -> {
                            handleFilter(GymsTabOption.MAP)
                            binding.map.visibility = View.VISIBLE
                            binding.recycler.visibility = View.GONE
                        }
                        1 -> {
                            handleFilter(GymsTabOption.LIST)
                            binding.map.visibility = View.GONE
                            binding.recycler.visibility = View.VISIBLE
                        }
                        else -> {
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
    }

    private fun subscribeUi() {
        mainViewModel.tabFlow.asLiveData().observe(
            viewLifecycleOwner
        ) { filter ->
            binding.tabLayout.apply {
                val viewModelIndex = filter.ordinal
                val currentIndex = selectedTabPosition
                if (viewModelIndex != currentIndex) {
                    selectTab(getTabAt(viewModelIndex))
                }
            }
        }

        mainViewModel.gyms.observe(
            viewLifecycleOwner
        ) { result ->
            when (result.status) {
                ApiResult.Status.SUCCESS -> {

                    // populate list
                    result.data?.let {
                        gymsAdapter.accept(it)
                    }

                    // populate map
                    result.data?.let {
                        it.forEach { item ->
                            if (item is GymViewItem.GymListItem) {
                                val gym = item.business
                                map.addMarker(
                                    MarkerOptions()
                                        .position(
                                            LatLng(
                                                gym.coordinates.latitude ?: DEFAULT_LAT,
                                                gym.coordinates.longitude ?: DEFAULT_LONG
                                            )
                                        )
                                        .title(gym.name)

                                )
                            }
                        }
                    }
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                mainViewModel.locationStatus.value?.data?.latitude
                                    ?: DEFAULT_LAT,
                                mainViewModel.locationStatus.value?.data?.longitude
                                    ?: DEFAULT_LONG
                            ),
                            15.0f
                        )
                    )

                    binding.loading.visibility = View.GONE
                }
                ApiResult.Status.ERROR -> {
                    result.message?.let {
                        binding.root.showSnackbar(it)
                    }
                    binding.loading.visibility = View.GONE
                }
                ApiResult.Status.LOADING -> {
                    binding.loading.visibility = View.VISIBLE
                }
            }
        }

        mainViewModel.locationStatus.observe(
            viewLifecycleOwner
        ) { state ->
            when (state.status) {
                ApiResult.Status.ERROR -> {
                    binding.loading.visibility = View.GONE
                    if (binding.tabLayout.selectedTabPosition == 0) {
                        binding.map.visibility = View.VISIBLE
                    } else {
                        binding.recycler.visibility = View.VISIBLE
                    }
                    binding.tabLayout.visibility = View.VISIBLE
                    state.message?.let {
                        binding.root.showSnackbar(it)
                    }
                }

                ApiResult.Status.LOADING -> {
                    binding.recycler.visibility = View.GONE
                    binding.map.visibility = View.GONE
                    binding.tabLayout.visibility = View.GONE
                    binding.loading.visibility = View.VISIBLE
                }

                ApiResult.Status.SUCCESS -> {
                    binding.loading.visibility = View.GONE
                    if (binding.tabLayout.selectedTabPosition == 0) {
                        binding.map.visibility = View.VISIBLE
                    } else {
                        binding.recycler.visibility = View.VISIBLE
                    }
                    binding.tabLayout.visibility = View.VISIBLE
                    val lat = state?.data?.latitude ?: DEFAULT_LAT
                    val lng = state?.data?.longitude ?: DEFAULT_LONG
                    mainViewModel.getGyms(
                        latitude = lat,
                        longitude = lng,
                    )
                }
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            mainViewModel.findLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    private val adapterListener = object : AdapterListener {
        override fun onGymClick(id: String) {
            val business = (mainViewModel.gyms.value?.data?.firstOrNull {
                it.id == id
            } as? GymViewItem.GymListItem)?.business
            business?.let {
                val direction = GymsFragmentDirections.openGymDetails(it)
                findNavController().navigate(direction)
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        checkPermissions()
        map.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        return false
    }

    companion object {
        private const val DEFAULT_LAT = 33.524155
        private const val DEFAULT_LONG = -111.905792
    }
}

private interface AdapterListener {
    fun onGymClick(id: String)
}

private class ListAdapter(private val listener: AdapterListener) :
    RecyclerView.Adapter<GymViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<GymViewItem>() {
        override fun areItemsTheSame(oldItem: GymViewItem, newItem: GymViewItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GymViewItem, newItem: GymViewItem): Boolean {
            return oldItem.id == newItem.id
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun accept(newItems: List<GymViewItem>) {
        differ.submitList(newItems)
    }

    override fun getItemViewType(position: Int) =
        differ.currentList[position].type.ordinal

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GymViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (val itemType = GymViewItemType.values()[viewType]) {
            GymViewItemType.GYM_LIST_ITEM -> {
                val bindings: GymListViewItemBinding =
                    DataBindingUtil.inflate(
                        inflater, itemType.layoutId, parent, false
                    )
                GymViewHolder.GymListViewHolder(bindings)
            }

        }
    }

    override fun onBindViewHolder(holder: GymViewHolder, position: Int) {
        val item = differ.currentList[position]
        when (holder) {
            is GymViewHolder.GymListViewHolder -> holder.bind(
                item as GymViewItem.GymListItem,
                onClick = { id -> listener.onGymClick(id) }
            )
        }
    }

    override fun getItemCount() = differ.currentList.size
}

private fun GymsFragment.handleFilter(option: GymsTabOption) {
    mainViewModel.onTabSelected(option)
}

enum class GymsTabOption {
    MAP,
    LIST
}
