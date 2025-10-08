package com.example.flightapp.presentation.home

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.flightapp.R
import com.example.flightapp.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
internal class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private var cameraMoveJob: Job? = null
    private var countryAdapter: ArrayAdapter<String>? = null

    private val planeMarkers = mutableMapOf<String, Marker>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
        listeners()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val southwest =
            LatLng(
                viewModel.uiState.value.coordinates.laMin,
                viewModel.uiState.value.coordinates.loMin
            )

        val northeast =
            LatLng(
                viewModel.uiState.value.coordinates.laMax,
                viewModel.uiState.value.coordinates.loMax
            )

        val bounds = LatLngBounds(southwest, northeast)

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0))

        mMap.setOnCameraMoveListener {
            handleCameraMove()
        }
        mMap.setOnMarkerClickListener { marker ->

            viewModel.onAction(HomeContract.HomeAction.OnMarkerClick(marker.title))
            true // block default behavior
        }

    }

    private fun listeners() {
        binding.countryOfOriginSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                viewModel.onAction(
                    action = HomeContract.HomeAction.ChangeOriginCountry(
                        country = binding.countryOfOriginSpinner.selectedItem.toString()
                    )
                )
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //
            }

        }
    }

    private fun observe() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                binding.welcomeTextView.text = "Flight Count: ${uiState.filteredFlightData.states.size}"
                binding.countryOfOriginTextView.text = "Country of Origin: ${uiState.originCountry}"

                updateCountrySpinner(uiState)
                updateMap(uiState)
            }
        }

        lifecycleScope.launch {
            viewModel.effect.collectLatest { effect ->
                when (effect) {
                    is HomeContract.HomeEffect.ShowFlightInfoInToastMessage -> {
                        Toast.makeText(
                            requireContext(),
                            "Flight Info: ${effect.flight.callsign} - ${effect.flight.originCountry}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun updateCountrySpinner(uiState: HomeContract.HomeUiState) {
        val newCountryList = listOf(HomeContract.DEFAULT_ORIGIN_COUNTRY) +
                uiState.allFlightData.states.map { it.originCountry }.distinct()

        if (countryAdapter == null) {
            countryAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                newCountryList
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.countryOfOriginSpinner.adapter = countryAdapter
        } else {
            countryAdapter?.clear()
            countryAdapter?.addAll(newCountryList)
            countryAdapter?.notifyDataSetChanged()
        }


        val currentIndex = newCountryList.indexOf(uiState.originCountry)
        if (currentIndex >= 0 && binding.countryOfOriginSpinner.selectedItemPosition != currentIndex) {
            binding.countryOfOriginSpinner.setSelection(currentIndex)
        }
    }

    private fun updateMap(uiState: HomeContract.HomeUiState) {
        if (!::mMap.isInitialized) return

        val currentFlightKeys = uiState.filteredFlightData.states.map { it.callsign }.toSet()
        val existingKeys = planeMarkers.keys.toSet()

        val markersToRemove = existingKeys - currentFlightKeys
        markersToRemove.forEach { key ->
            planeMarkers[key]?.remove()
            planeMarkers.remove(key)
        }

        val icon = vectorToBitmap(requireContext(), R.drawable.ic_plane)

        uiState.filteredFlightData.states.forEach { flight ->
            val key = flight.callsign
            val position = LatLng(flight.latitude, flight.longitude)
            val rotation = flight.trueTrack.toFloat()

            if (planeMarkers.containsKey(key)) {
                planeMarkers[key]?.let { marker ->
                    marker.position = position
                    marker.rotation = rotation
                }
            } else {
                val newMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .rotation(rotation)
                        .flat(true)
                        .anchor(0.5f, 0.5f)
                        .icon(icon)
                        .title(flight.callsign)
                )
                planeMarkers[key] = newMarker!!
            }
        }
    }

    private fun handleCameraMove() {
        cameraMoveJob?.cancel()

        cameraMoveJob = lifecycleScope.launch {
            delay(HomeContract.WAIT_AFTER_CAMERA_MOVEMENT_STOPPED)

            if (::mMap.isInitialized) {
                val bounds = mMap.projection.visibleRegion.latLngBounds
                val southwest = bounds.southwest
                val northeast = bounds.northeast

                viewModel.onAction(
                    action = HomeContract.HomeAction.GetFlightData(
                        coordinates = HomeContract.Coordinates(
                            laMin = southwest.latitude,
                            loMin = southwest.longitude,
                            laMax = northeast.latitude,
                            loMax = northeast.longitude
                        )
                    )

                )
            }
        }
    }

    private fun vectorToBitmap(context: Context, vectorResId: Int): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return BitmapDescriptorFactory.defaultMarker()

        val bitmap = createBitmap(drawable.intrinsicWidth / 2, drawable.intrinsicHeight / 2)

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onResume() {
        viewModel.onAction(HomeContract.HomeAction.OnResume)
        super.onResume()
    }

    override fun onPause() {
        viewModel.onAction(HomeContract.HomeAction.OnPause)
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraMoveJob?.cancel()
        _binding = null
    }
}
