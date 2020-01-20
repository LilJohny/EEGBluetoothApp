package com.example.bluetoothapp.fragments

import androidx.fragment.app.Fragment
import com.example.bluetoothapp.R
import com.example.bluetoothapp.utils.isConnected
import com.example.bluetoothapp.utils.showSnackbarShort
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_connection.*

class ConnectionFragment :Fragment(){

    private lateinit var bleDevice: RxBleDevice

    private var connectionDisposable: Disposable? = null

    private var stateDisposable: Disposable? = null

    private val mtuDisposable = CompositeDisposable()



    private fun onConnectionFailure(throwable: Throwable) = showSnackbarShort("Connection error: $throwable")

    private fun onConnectionReceived() = showSnackbarShort("Connection received")

    private fun onConnectionStateChange(newState: RxBleConnection.RxBleConnectionState) {
        connection_state.text = newState.toString()
        updateUI()
    }

    private fun onMtuReceived(mtu: Int) = showSnackbarShort("MTU received: $mtu")

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