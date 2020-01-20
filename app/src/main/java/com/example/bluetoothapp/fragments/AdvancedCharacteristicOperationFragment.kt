package com.example.bluetoothapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.example.bluetoothapp.R
import com.example.bluetoothapp.SampleApplication

import com.example.bluetoothapp.presenters.*
import com.example.bluetoothapp.presenters.CompatibilityModeEvent
import com.example.bluetoothapp.presenters.ErrorEvent
import com.example.bluetoothapp.presenters.InfoEvent
import com.example.bluetoothapp.presenters.PresenterEvent
import com.example.bluetoothapp.presenters.ResultEvent
import com.example.bluetoothapp.presenters.Type
import com.example.bluetoothapp.utils.showSnackbarShort
import com.example.bluetoothapp.utils.toHex
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_characteristics_operations.*
import java.util.*

private const val EXTRA_MAC_ADDRESS = "extra_mac_address"
private val TAG = "CharacteristicOperation"
private const val EXTRA_CHARACTERISTIC_UUID = "extra_uuid"

class AdvancedCharacteristicOperationFragment : Fragment() {
    private var activityFlowDisposable: Disposable? = null

    private lateinit var presenterEventObservable: Observable<PresenterEvent>

    private val inputBytes: ByteArray
        get() = write_input.text.toString().toByteArray()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_characteristics_operations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val macAddress = arguments?.getString(EXTRA_MAC_ADDRESS)
        val characteristicUuid = UUID.fromString(arguments?.getString(EXTRA_CHARACTERISTIC_UUID))
        val bleDevice = SampleApplication.rxBleClient.getBleDevice(macAddress!!)


        val sharedConnectButtonClicks = connect_button.activatedClicksObservable().share()

        val sharedNotifyButtonClicks = notify_button.activatedClicksObservable().share()
        val sharedIndicateButtonClicks = indicate_button.activatedClicksObservable().share()
        val (connect, connecting, disconnect) =
            sharedConnectButtonClicks.setupButtonTexts(
                connect_button,
                R.string.button_connect,
                R.string.connecting,
                R.string.button_disconnect
            )

        val (setupNotification, settingNotification, teardownNotification) =
            sharedNotifyButtonClicks.setupButtonTexts(
                notify_button,
                R.string.button_setup_notification,
                R.string.setting_notification,
                R.string.teardown_notification
            )

        val (setupIndication, settingIndication, teardownIndication) =
            sharedIndicateButtonClicks.setupButtonTexts(
                indicate_button,
                R.string.button_setup_indication,
                R.string.setting_indication,
                R.string.teardown_indication
            )

        val readObservable = read_button.activatedClicksObservable()


        val writeObservable =
            write_button.activatedClicksObservable()
                .map { inputBytes }
                .doOnError { throwable -> showSnackbarShort("Could not parse input: $throwable") }
                .retryWhen { it }

        presenterEventObservable = prepareActivityLogic(
            bleDevice,
            characteristicUuid,
            connect,
            connecting,
            disconnect,
            readObservable,
            writeObservable,
            setupNotification,
            settingNotification,
            teardownNotification,
            setupIndication,
            settingIndication,
            teardownIndication
        )
    }


    override fun onResume() {
        super.onResume()
        presenterEventObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { it.handleEvent() }
            .let { activityFlowDisposable = it }
    }

    override fun onPause() {
        super.onPause()
        activityFlowDisposable?.dispose()
        activityFlowDisposable = null
    }


    private fun PresenterEvent.handleEvent() {
        Log.i(TAG, toString())
        when (this) {
            is InfoEvent -> showSnackbarShort(infoText)
            is CompatibilityModeEvent -> handleCompatibility()
            is ResultEvent -> handleResult()
            is ErrorEvent -> handleError()
        }
    }


    private fun CompatibilityModeEvent.handleCompatibility() {
        compat_only_warning.visibility = if (isCompatibility) View.VISIBLE else View.INVISIBLE
        if (isCompatibility) {
            /*
            All characteristics that have PROPERTY_NOTIFY or PROPERTY_INDICATE should contain
            a Client Characteristic Config Descriptor. The RxAndroidBle supports compatibility mode
            for setting the notifications / indications because it is not possible to fix the firmware
            in some third party peripherals. If you have possibility - inform the developer
            of the firmware that it is an error so they can fix.
            */
            Log.e(
                TAG, """
                            |THIS PERIPHERAL CHARACTERISTIC HAS PROPERTY_NOTIFY OR PROPERTY_INDICATE
                            |BUT DOES NOT HAVE CLIENT CHARACTERISTIC CONFIG DESCRIPTOR WHICH VIOLATES
                            |BLUETOOTH SPECIFICATION - CONTACT THE FIRMWARE DEVELOPER TO FIX IF POSSIBLE
                            """.trimMargin()
            )
        }
    }


    private fun ResultEvent.handleResult() {
        when (type) {
            Type.READ -> {
                read_output.text = String(result.toByteArray())
                read_hex_output.text = result.toByteArray().toHex()
                write_input.setText(result.toByteArray().toHex())
            }
            Type.WRITE -> showSnackbarShort("Write success")
            Type.NOTIFY -> showSnackbarShort("Notification: ${result.toByteArray().toHex()}")
            Type.INDICATE -> showSnackbarShort("Indication: ${result.toByteArray().toHex()}")
        }
    }


    private fun ErrorEvent.handleError() {
        @Suppress("ReplaceSingleLineLet")
        when (type) {
            Type.READ -> "Read error: $error"
            Type.WRITE -> "Write error: $error"
            Type.NOTIFY -> "Notifications error: $error"
            Type.INDICATE -> "Indications error: $error"
        }.let { showSnackbarShort(it) }
    }
}

/**
 * Function that returns an observable that emits `true` every time the button is being clicked. It enables the button
 * whenever the returned Observable is being subscribed and disables it when un-subscribed. Takes care of making interactions with
 * the button on the proper thread.
 *
 * @param button the button to wrap into an Observable
 * @return the observable
 */
private fun Button.activatedClicksObservable(): Observable<Boolean> =
    Observable.using(
        { apply { isEnabled = true } },
        { it.clicks().map { true } },
        { it.isEnabled = false }
    )
        .subscribeOn(AndroidSchedulers.mainThread()) // RxView expects to be subscribed on the Main Thread
        .unsubscribeOn(AndroidSchedulers.mainThread())

/**
 * Set up button texts reflecting current button state.
 *
 * @param start button text to trigger the process when it's not yet started
 * @param progress button text while in progress
 * @param end button text to end the process
 */
private fun Observable<Boolean>.setupButtonTexts(
    button: Button,
    @StringRes start: Int,
    @StringRes progress: Int,
    @StringRes end: Int
): Triple<Observable<Boolean>, Observable<Boolean>, Observable<Boolean>> =
    Triple(
        compose(button.onSubscribeSetText(start)),
        compose(button.onSubscribeSetText(progress)),
        compose(button.onSubscribeSetText(end))
    )

/**
 * Function that returns an [ObservableTransformer] which will on subscribe
 * set a text on a button using a proper thread
 *
 * @param button the button to set text on
 * @param textResId the text resource id
 * @return the transformer
 */
private fun Button.onSubscribeSetText(@StringRes textResId: Int): ObservableTransformer<Boolean, Boolean> =
    ObservableTransformer {
        it.doOnSubscribe { setText(textResId) }
            .subscribeOn(AndroidSchedulers.mainThread())
    }