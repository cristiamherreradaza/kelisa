package net.goghu.elisa

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import net.goghu.elisa.MyFirebaseMessagingService
import com.google.android.gms.location.*
import net.goghu.elisa.databinding.ActivityPrincipalBinding
import org.json.JSONObject
import org.json.JSONTokener

class PrincipalActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityPrincipalBinding

    private val LOCATION_PERMISSION_REQ_CODE = 1000
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    /*public lateinit var latitud: String
    public lateinit var longitud: String

    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarPrincipal.toolbar)

        binding.appBarPrincipal.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_principal)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // leemos las preferencias guardadas
        val preferencias = getSharedPreferences("preferencias", Context.MODE_PRIVATE)
        val nombre = preferencias.getString("nombre", "").toString()

        // cambiamos el nombre del usuario al nav
        val navigationView : NavigationView  = findViewById(R.id.nav_view)
        val headerView : View = navigationView.getHeaderView(0)
        val navUsername : TextView = headerView.findViewById(R.id.txtVNombreUsuario)

        navUsername.text = nombre

        // llamamos al servicio fused de ubicacion
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // capturamos la ubicacion actual
        getCurrentLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.principal, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_principal)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    //nuevas funciones de localizacion
    private fun getCurrentLocation() {
        // verificamos los permisos para la ubicacion
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQ_CODE);
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // getting the last known or current location
                latitude = location.latitude
                longitude = location.longitude

//                Log.i("latitud ", latitude.toString())
//                Log.i("longitud ", longitude.toString())
                /*tvLatitude.text = "Latitude: ${location.latitude}"
                tvLongitude.text = "Longitude: ${location.longitude}"
                tvProvider.text = "Provider: ${location.provider}"*/
//                btOpenMap.visibility = View.VISIBLE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed on getting current location",
                    Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                } else {
                    // permission denied
                    Toast.makeText(this, "You need to grant permission to access location",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun openMap() {
        val uri = Uri.parse("geo:${latitude},${longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    fun panico(view: android.view.View) {

        getCurrentLocation()

        Log.i("latitud ", latitude.toString())
        Log.i("longitud ", longitude.toString())

        val latitud = latitude.toString()
        val longitud = longitude.toString()

        enviaLocalizacion(latitud, longitud)

        Toast.makeText(this, "Se mando la alerta", Toast.LENGTH_LONG).show()
    }

    // notificaciones push

    /*fun salirTema(){
        MyFirebaseMessagingService.unsubscribeTopic(this, "Encargados")
    }

    fun suscribirTema(view: android.view.View) {
        MyFirebaseMessagingService.subscribeTopic(this@PrincipalActivity,"Encargados")
        Toast.makeText(this, "Suscrito a Encargados", Toast.LENGTH_LONG).show()
    }

    fun enviarMensaje(view: android.view.View) {
        MyFirebaseMessagingService.sendMessage("Elisa: Alerta", "Cristiam en problemas", "Encargados")
    }*/

    // fin notificaciones push

    private fun enviaLocalizacion(latitud :String, longitud: String){
//        Toast.makeText(this, nombre, Toast.LENGTH_SHORT).show()
        val url = Constants.BASE_URL + Constants.API_PATH + Constants.LOCALIZACION_PATH

        // leemos las preferencias guardadas
        val preferencias = getSharedPreferences("preferencias", Context.MODE_PRIVATE)
        val usuarioId = preferencias.getString("usuario_id", "").toString()

        Log.i("el usuarioId", usuarioId)

        val jsonParams = JSONObject()

        jsonParams.put(Constants.ID_PARAM, usuarioId)
        jsonParams.put(Constants.LATITUD_PARAM, latitud)
        jsonParams.put(Constants.LONGITUD_PARAM, longitud)

        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, url, jsonParams, { response ->
//            me retorna los datos de guardado
            Log.i("respuesta", response.toString())

            // manejamos la respuesta
            val jsonObject = JSONTokener(response.toString()).nextValue() as JSONObject
            // capturamos el id que nos devolvio el registro
            //val usuario = jsonObject.getString("usuario")
//            Log.i("Usuario: ", usuario)

        },{
            if (it.networkResponse.statusCode == 400){
                Log.i("Error", "error en el envio")
            }
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json"
                return params
            }
        }
        LoginApplication.reqResApi.addToRequestQueue(jsonObjectRequest)
    }
}