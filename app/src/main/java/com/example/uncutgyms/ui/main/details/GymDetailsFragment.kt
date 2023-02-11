package com.example.uncutgyms.ui.main.details

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.uncutgyms.BuildConfig.MAPS_API_KEY
import com.example.uncutgyms.MainActivity
import com.example.uncutgyms.R
import com.example.uncutgyms.databinding.FragmentGymDetailsBinding
import com.example.uncutgyms.ui.main.MainViewModel
import com.example.uncutgyms.ui.main.util.ApiResult
import com.example.uncutgyms.ui.main.util.requestLocationPermission
import com.example.uncutgyms.ui.main.util.showSnackbar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class GymDetailsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private val mainViewModel by activityViewModels<MainViewModel>()

    private val args: GymDetailsFragmentArgs by navArgs()

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

    private var _binding: FragmentGymDetailsBinding? = null
    private val binding get() = _binding!!

    var polylineFinal: Polyline? = null

    private lateinit var map: GoogleMap

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setHasOptionsMenu(true)

        _binding = FragmentGymDetailsBinding.inflate(inflater, container, false)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_share, menu)
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share_menu_item -> {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_SUBJECT, "Sharing Business")
                i.putExtra(Intent.EXTRA_TEXT, args.businessInfo.url)
                startActivity(Intent.createChooser(i, "Share Business"))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.map.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        binding.map.getMapAsync(this)
    }

    private fun subscribeUi() {
        val gym = args.businessInfo
        binding.heroImage.load(gym.imageUrl)
        binding.name.text = gym.name
        binding.callButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${gym.phone}")
            startActivity(intent)
        }

        mainViewModel.locationStatus.observe(
            viewLifecycleOwner
        ) { state ->
            when (state.status) {
                ApiResult.Status.ERROR -> {
                    state.message?.let {
                        binding.root.showSnackbar(it)
                    }
                }

                ApiResult.Status.LOADING -> {}

                ApiResult.Status.SUCCESS -> {
                    val lat = state?.data?.latitude ?: DEFAULT_LAT
                    val lng = state?.data?.longitude ?: DEFAULT_LONG
                    val currentLocation = LatLng(lat, lng)
                    val targetGym = LatLng(
                        gym.coordinates.latitude ?: DEFAULT_LAT,
                        gym.coordinates.longitude ?: DEFAULT_LONG
                    )
                    map.addMarker(
                        MarkerOptions()
                            .position(
                                currentLocation
                            )
                            .title("Current Location")
                    )
                    map.addMarker(
                        MarkerOptions()
                            .position(
                                targetGym
                            )
                            .title(gym.name)
                    )
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                currentLocation.latitude,
                                currentLocation.longitude
                            ),
                            15.0f
                        )
                    )
                    mainViewModel.getDirections(
                        mapOf(
                            "origin" to "${currentLocation.latitude},${currentLocation.longitude}",
                            "destination" to "${targetGym.latitude},${targetGym.longitude}",
                            "key" to MAPS_API_KEY,
                        )
                    )
                }
            }
        }

        mainViewModel.directions.observe(
            viewLifecycleOwner
        ) {
            when (it.status) {
                ApiResult.Status.SUCCESS -> {
                    polylineFinal?.remove()

                    val points = mutableListOf<LatLng>()
                    val routes = it.data?.routes
                    val legsList = routes?.map { route ->
                        route.legs
                    }
                    legsList?.forEach { list ->
                        list?.forEach { leg ->
                            for (step in leg.steps) {
                                val lat = step.startLocation?.lat
                                val lng = step.startLocation?.lng
                                if (lat != null && lng != null) {
                                    points.add(
                                        LatLng(lat, lng)
                                    )
                                    points.add(
                                        LatLng(step.endLocation?.lat!!, step.endLocation.lng!!)
                                    )
                                }
                            }
                        }
                    }

                    polylineFinal = map.addPolyline(PolylineOptions().addAll(points))
                }
                ApiResult.Status.ERROR -> {

                }
                ApiResult.Status.LOADING -> {

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

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        checkPermissions()
        subscribeUi()
        map.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        return false
    }

    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String? {

        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude

        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude

        // Sensor enabled
        val sensor = "sensor=false"
        val mode = "mode=driving"

        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor&$mode"

        // Output format
        val output = "json"

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
    }

    companion object {
        private const val DEFAULT_LAT = 33.524155
        private const val DEFAULT_LONG = -111.905792
    }
}
