package com.example.flightapp.presentation.home

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@AndroidEntryPoint
internal class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private var cameraMoveJob: Job? = null

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

        // Initialize Google Maps
        val mapFragment = childFragmentManager.findFragmentById(com.example.flightapp.R.id.map) as SupportMapFragment?
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

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

        mMap.setOnCameraMoveListener {
            handleCameraMove()
        }

    }

    private fun observe() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                binding.descriptionTextView.text = "Flight Count: ${uiState.flightData.states.size}"

                if (::mMap.isInitialized) {
                    mMap.clear()

                    val icon = vectorToBitmap(requireContext(), R.drawable.ic_plane)

                    uiState.flightData.states.forEach { flight ->
                        mMap.addMarker(
                            MarkerOptions()
                                .position(
                                    LatLng(
                                        flight.latitude,
                                        flight.longitude
                                    )
                                )
                                .title(flight.callsign)
                                .icon(icon)
                                .anchor(0.5f, 0.5f)
                                .rotation(flight.trueTrack.toFloat())
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraMoveJob?.cancel()
        _binding = null
    }

    private fun handleCameraMove() {
        cameraMoveJob?.cancel()

        cameraMoveJob = lifecycleScope.launch {
            delay(1000)

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
}
