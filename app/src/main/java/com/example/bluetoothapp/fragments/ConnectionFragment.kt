package com.example.bluetoothapp.fragments

import android.annotation.TargetApi
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bluetoothapp.R
import com.example.bluetoothapp.SampleApplication
import com.example.bluetoothapp.utils.isConnected
import com.example.bluetoothapp.utils.showToastShort
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_connection.*

private const val EXTRA_MAC_ADDRESS = "extra_mac_address"

class ConnectionFragment :Fragment(){

    private lateinit var bleDevice: RxBleDevice

    private var connectionDisposable: Disposable? = null

    private var stateDisposable: Disposable? = null

    private val mtuDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connect_toggle.setOnClickListener { onConnectToggleClick() }
        set_mtu.setOnClickListener { onSetMtu() }

        val macAddress = arguments?.getString(EXTRA_MAC_ADDRESS)

        bleDevice = SampleApplication.rxBleClient.getBleDevice(macAddress!!)


        bleDevice.observeConnectionStateChanges()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onConnectionStateChange(it) }
            .let { stateDisposable = it }
    }

    private fun onConnectionFailure(throwable: Throwable) = showToastShort("Connection error: $throwable")

    private fun onConnectionReceived() = showToastShort("Connection received")

    private fun onConnectionStateChange(newState: RxBleConnection.RxBleConnectionState) {
        connection_state.text = newState.toString()
        updateUI()
    }

    private fun onConnectToggleClick() {
        if (bleDevice.isConnected) {
            triggerDisconnect()
        } else {
            bleDevice.establishConnection(autoconnect.isChecked)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { dispose() }
                .subscribe({ onConnectionReceived() }, { onConnectionFailure(it) })
                .let { connectionDisposable = it }
        }
    }
    @TargetApi(21 )
    private fun onSetMtu() {
        newMtu.text.toString().toIntOrNull()?.let { mtu ->
            bleDevice.establishConnection(false)
                .flatMapSingle { rxBleConnection -> rxBleConnection.requestMtu(mtu) }
                .take(1) // Disconnect automatically after discovery
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { updateUI() }
                .subscribe({ onMtuReceived(it) }, { onConnectionFailure(it) })
                .let { mtuDisposable.add(it) }
        }
    }
    private fun onMtuReceived(mtu: Int) = showToastShort("MTU received: $mtu")

    private fun dispose() {
        connectionDisposable = null
        updateUI()
    }

    private fun triggerDisconnect() = connectionDisposable?.dispose()

    private fun updateUI() {
        connect_toggle.setText(if (bleDevice.isConnected) R.string.button_disconnect else R.string.button_connect)
        autoconnect.isEnabled = !bleDevice.isConnected
    }

    override fun onPause() {
        super.onPause()
        triggerDisconnect()
        mtuDisposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        stateDisposable?.dispose()
    }

}