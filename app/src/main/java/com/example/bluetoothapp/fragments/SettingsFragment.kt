package com.example.bluetoothapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bluetoothapp.R
import com.example.bluetoothapp.SampleApplication
import com.example.bluetoothapp.adapters.ScanResultsAdapter
import com.example.bluetoothapp.utils.isLocationPermissionGranted
import com.example.bluetoothapp.utils.requestLocationPermission
import com.example.bluetoothapp.utils.showError
import com.polidea.rxandroidble2.exceptions.BleScanException
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_settings.*


private const val EXTRA_MAC_ADDRESS = "extra_mac_address"
private const val TAG = "deviceFragment"

class SettingsFragment : Fragment() {

    private val rxBleClient = SampleApplication.rxBleClient

    private var scanDisposable: Disposable? = null

    private val resultsAdapter =
        ScanResultsAdapter {
            val deviceFragment = DeviceFragment()
            val args = Bundle()
            args.putString(EXTRA_MAC_ADDRESS, it.bleDevice.macAddress )
            deviceFragment.arguments = args
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.entry_container, deviceFragment, TAG)
                ?.addToBackStack(TAG)
                ?.commit()
        }

    private var hasClickedScan = false

    private val isScanning: Boolean
        get() = scanDisposable != null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureResultList()
        scan_toggle_btn.setOnClickListener { onScanToggleClick() }
    }

    private fun configureResultList() {
        with(scan_results) {
            setHasFixedSize(true)
            itemAnimator = null
            adapter = resultsAdapter
        }
    }

    private fun onScanToggleClick() {
        if (isScanning) {
            scanDisposable?.dispose()
        } else {
            if (this.context!!.isLocationPermissionGranted()) {
                scanBleDevices()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally { dispose() }
                    .subscribe({ resultsAdapter.addScanResult(it) }, { onScanFailure(it) })
                    .let { scanDisposable = it }
            } else {
                hasClickedScan = true
                requestLocationPermission()
            }
        }
        updateButtonUIState()
    }

    private fun scanBleDevices(): Observable<ScanResult> {
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()
        //Can add custom filter
        val scanFilter = ScanFilter.Builder()
            .build()

        return rxBleClient.scanBleDevices(scanSettings, scanFilter)
    }

    private fun dispose() {
        scanDisposable = null
        resultsAdapter.clearScanResults()
        updateButtonUIState()
    }

    private fun onScanFailure(throwable: Throwable) {
        if (throwable is BleScanException) showError(throwable)
    }

    private fun updateButtonUIState() =
        scan_toggle_btn.setText(if (isScanning) R.string.button_stop_scan else R.string.button_start_scan)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (isLocationPermissionGranted(requestCode, grantResults) && hasClickedScan) {
            hasClickedScan = false
            scanBleDevices()
        }
    }

    override fun onPause() {
        super.onPause()

        if (isScanning) scanDisposable?.dispose()
    }
}