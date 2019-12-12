package com.example.bluetoothapp.activities


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bluetoothapp.R
import com.example.bluetoothapp.SampleApplication
import com.example.bluetoothapp.adapters.ScanResultsAdapter
import com.polidea.rxandroidble2.exceptions.BleScanException

import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable



import android.content.Context
import android.content.Intent
import com.example.bluetoothapp.adapters.DiscoveryResultsAdapter
import com.example.bluetoothapp.utils.*
import com.example.bluetoothapp.utils.showSnackbarShort
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_example3.*


private const val EXTRA_MAC_ADDRESS = "extra_mac_address"

class ServiceDiscoveryActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context, macAddress: String) =
            Intent(context, ServiceDiscoveryActivity::class.java).apply {
                putExtra(EXTRA_MAC_ADDRESS, macAddress)
            }
    }

    private lateinit var bleDevice: RxBleDevice

    private lateinit var macAddress: String

    private val resultsAdapter = DiscoveryResultsAdapter { onAdapterItemClick(it) }

    private val discoveryDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example3)
        connect.setOnClickListener { onConnectToggleClick() }

        macAddress = intent.getStringExtra(EXTRA_MAC_ADDRESS)
        supportActionBar!!.subtitle = getString(R.string.mac_address, macAddress)
        bleDevice = SampleApplication.rxBleClient.getBleDevice(macAddress)

        scan_results.apply {
            setHasFixedSize(true)
            adapter = resultsAdapter
        }
    }

    private fun onConnectToggleClick() {
        bleDevice.establishConnection(false)
            .flatMapSingle { it.discoverServices() }
            .take(1) // Disconnect automatically after discovery
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { updateUI() }
            .doFinally { updateUI() }
            .subscribe({ resultsAdapter.swapScanResult(it) }, { showSnackbarShort("Connection error: $it") })
            .let { discoveryDisposable.add(it) }
    }

    private fun onAdapterItemClick(item: DiscoveryResultsAdapter.AdapterItem) {
        when (item.type) {
            DiscoveryResultsAdapter.AdapterItem.CHARACTERISTIC -> {
                startActivity(CharacteristicOperationActivity.newInstance(this, macAddress, item.uuid))
                // If you want to check the alternative advanced implementation comment out the line above and uncomment one below
//            startActivity(AdvancedCharacteristicOperationExampleActivity.newInstance(this, macAddress, item.uuid))
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