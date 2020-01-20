package com.example.bluetoothapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bluetoothapp.R
import com.example.bluetoothapp.SampleApplication
import com.example.bluetoothapp.activities.AdvancedCharacteristicOperationActivity
import com.example.bluetoothapp.adapters.DiscoveryResultsAdapter
import com.example.bluetoothapp.utils.isConnected
import com.example.bluetoothapp.utils.showSnackbarShort
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_services.*


private const val EXTRA_MAC_ADDRESS = "extra_mac_address"
private const val EXTRA_CHARACTERISTIC_UUID = "extra_uuid"
private const val TAG = "characteristicOperationFragment"
class ServiceDiscoveryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connect.setOnClickListener { onConnectToggleClick() }
        macAddress = arguments?.getString(EXTRA_MAC_ADDRESS).toString()
        bleDevice = SampleApplication.rxBleClient.getBleDevice(macAddress)

        scan_results.apply {
            setHasFixedSize(true)
            adapter = resultsAdapter
        }
    }

    private lateinit var bleDevice: RxBleDevice

    private lateinit var macAddress: String

    private val resultsAdapter = DiscoveryResultsAdapter { onAdapterItemClick(it) }

    private val discoveryDisposable = CompositeDisposable()

    private fun onConnectToggleClick() {
        bleDevice.establishConnection(false)
            .flatMapSingle { it.discoverServices() }
            .take(1) // Disconnect automatically after discovery
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { updateUI() }
            .doFinally { updateUI() }
            .subscribe(
                { resultsAdapter.swapScanResult(it) },
                { showSnackbarShort("Connection error: $it") })
            .let { discoveryDisposable.add(it) }
    }

    private fun onAdapterItemClick(item: DiscoveryResultsAdapter.AdapterItem) {
        when (item.type) {
            DiscoveryResultsAdapter.AdapterItem.CHARACTERISTIC -> {
                var characteristicOperationFragment = AdvancedCharacteristicOperationFragment()
                var fragmentArgs = Bundle()
                fragmentArgs.putString(EXTRA_MAC_ADDRESS, bleDevice.macAddress)
                fragmentArgs.putString(EXTRA_CHARACTERISTIC_UUID, item.uuid.toString())
                characteristicOperationFragment.arguments = fragmentArgs
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.entry_container, characteristicOperationFragment, TAG)
                    ?.addToBackStack(TAG)
                    ?.commit()
            }
            else -> showSnackbarShort(R.string.not_clickable)
        }
    }

    private fun updateUI() {
        connect.isEnabled = !bleDevice.isConnected
    }

    override fun onPause() {
        super.onPause()
        discoveryDisposable.clear()
    }
}