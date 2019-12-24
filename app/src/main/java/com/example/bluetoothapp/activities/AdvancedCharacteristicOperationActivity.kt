package com.example.bluetoothapp.activities


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.jakewharton.rxbinding3.view.clicks
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.example.bluetoothapp.R
import com.example.bluetoothapp.SampleApplication
import com.example.bluetoothapp.presenters.*
import com.example.bluetoothapp.presenters.CompatibilityModeEvent
import com.example.bluetoothapp.presenters.InfoEvent
import com.example.bluetoothapp.presenters.PresenterEvent
import com.example.bluetoothapp.presenters.ResultEvent
import com.example.bluetoothapp.presenters.prepareActivityLogic
import com.example.bluetoothapp.utils.showSnackbarShort
import com.example.bluetoothapp.utils.toHex
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_characteristic_operations_advanced.*
import java.util.*


private val TAG = AdvancedCharacteristicOperationActivity::class.java.simpleName

private const val EXTRA_MAC_ADDRESS = "extra_mac_address"

private const val EXTRA_CHARACTERISTIC_UUID = "extra_uuid"

/**
 * This activity allows for connecting to a device and interact with a given characteristic.
 *
 * It may be used as a direct replacement for
 * [CharacteristicOperationExampleActivity][com.polidea.rxandroidble2.samplekotlin.example4_characteristic.CharacteristicOperationExampleActivity]
 *
 * When the connection is not established only the "CONNECT" button is active.
 * When the user clicks on the "CONNECT" button the connection is established and other buttons are enabled according to the properties
 * of the characteristic.
 * Subsequent clicks on the "CONNECT" button (whose name will change according to the connection state) will close the connection.
 *
 * When the connection is open other buttons are activated in regards of the characteristic's properties.
 * If the user clicks on "READ" a characteristic read is performed and the output is set on the TextView and EditText or a Snackbar is shown
 * in case of an error. "WRITE" clicks work the same as read but a write command is performed with data from the EditText.
 * If the characteristic has both of PROPERTY_NOTIFY and PROPERTY_INDICATE then only one of them is possible to be set at any given time.
 * Texts on notification and indication buttons will change accordingly to the current state of the notifications / indications.
 */
class AdvancedCharacteristicOperationActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context, macAddress: String, uuid: UUID) =
            Intent(context, AdvancedCharacteristicOperationActivity::class.java).apply {
                putExtra(EXTRA_MAC_ADDRESS, macAddress)
                putExtra(EXTRA_CHARACTERISTIC_UUID, uuid)
            }
    }

    private var activityFlowDisposable: Disposable? = null

    private lateinit var presenterEventObservable: Observable<PresenterEvent>

    private val inputBytes: ByteArray
        get() = write_input.text.toString().toByteArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_characteristic_operations_advanced)
        plot.setOnClickListener{startActivity(PlotActivity.newInstance(this))}
        val macAddress = intent.getStringExtra(EXTRA_MAC_ADDRESS)
        val characteristicUuid = intent.getSerializableExtra(EXTRA_CHARACTERISTIC_UUID) as UUID
        val bleDevice = SampleApplication.rxBleClient.getBleDevice(macAddress)

        supportActionBar!!.subtitle = getString(R.string.mac_address, macAddress)

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