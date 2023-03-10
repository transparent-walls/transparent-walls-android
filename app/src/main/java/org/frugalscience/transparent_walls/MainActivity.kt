package org.frugalscience.transparent_walls

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.frugalscience.transparent_walls.ui.theme.TransparentwallsTheme
import java.util.*


class MainActivity : ComponentActivity() {
    private lateinit var wifiSignalReceiver: BroadcastReceiver
    private val PERMISSIONS_REQUEST_CODE: Int = 123
    private var signalStrengthText: MutableState<String> = mutableStateOf("0")

    @RequiresApi(Build.VERSION_CODES.M)
    fun createPermissions(){
        println("ASking for permission")
        var permission = Manifest.permission.ACCESS_NETWORK_STATE
        println(ContextCompat.checkSelfPermission(this, permission))
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                requestPermissions(arrayOf(permission), PERMISSIONS_REQUEST_CODE)
            }
        }
        permission = Manifest.permission.CHANGE_NETWORK_STATE
        println(ContextCompat.checkSelfPermission(this, permission))
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                requestPermissions(arrayOf(permission), PERMISSIONS_REQUEST_CODE)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            createPermissions();
        }

        // Request the necessary permissions
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()


//        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
            }

            // Network capabilities have changed for the network
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val signalStrength = networkCapabilities.signalStrength
                signalStrengthText.value = "$signalStrength"
            }

            // lost network connection
            override fun onLost(network: Network) {
                super.onLost(network)
            }
        }

        val connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(request, networkCallback)


        class WifiSignalReceiver : BroadcastReceiver() {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == WifiManager.RSSI_CHANGED_ACTION) {
                   val caps = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                    val signalStrength = caps?.signalStrength
                    print(signalStrength)
                    signalStrengthText.value = "$signalStrength"
                }
            }
        }

        wifiSignalReceiver = WifiSignalReceiver()
        val intentFilter = IntentFilter(WifiManager.RSSI_CHANGED_ACTION)

        registerReceiver(wifiSignalReceiver, intentFilter)

//        val handler = Handler(Looper.getMainLooper())
//        val intervalMillis = 1000L // 1 second

//        handler.postDelayed(object : Runnable {
//            @RequiresApi(Build.VERSION_CODES.Q)
//            override fun run() {
//               val caps = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//                val signalStrength = caps?.signalStrength
//                print(signalStrength)
//                signalStrengthText.value = "$signalStrength"
//                handler.postDelayed(this, intervalMillis)
//            }
//        }, intervalMillis)

        setContent {
            TransparentwallsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    var signalStrength by remember { this.signalStrengthText }
                    Column (modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        SignalStrengthText(signalStrength = signalStrength)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiSignalReceiver)
    }
}

@Composable
fun SignalStrengthText(signalStrength: String) {
    Text(text = "$signalStrength", fontSize = 80.sp)
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TransparentwallsTheme {
        Greeting("Android")
    }
}