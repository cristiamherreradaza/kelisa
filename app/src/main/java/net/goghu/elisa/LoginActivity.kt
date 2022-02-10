package net.goghu.elisa

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.PermissionChecker.checkSelfPermission
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.google.gson.Gson
import net.goghu.elisa.databinding.ActivityLoginBinding
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val usuarioId = ""
    private val email = ""
    private val nombre = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Obtenemos el PreferenceManager
        val preferenciaGuardada  = getPreferences(MODE_PRIVATE);
        val nombreSession = preferenciaGuardada.getString("nombre", "NA").toString()
        if (nombreSession != "NA"){
            Log.i("Informacion session", "si tiene")
            
            // si tiene session saltamos a la siguiente actividad
            val intent = Intent(this@LoginActivity,PrincipalActivity::class.java);
            intent.putExtra("nombre", nombreSession)
            startActivity(intent);
        }else{
            // se adiciono el log
            Log.i("Informacion session", "ninguna session")
        }

//        Toast.makeText(this, isOnline(), Toast.LENGTH_LONG)
        if (isOnline(this)){
            Log.i("En linea", "Si esta")
        }else{
            Log.i("En linea", "No esta")
            Toast.makeText(this, "CONECTESE A INTERNET", Toast.LENGTH_LONG).show()
            binding.btnIngresar.visibility = View.INVISIBLE
        }

        binding.btnIngresar.setOnClickListener{

            val email = binding.tiEmail.text.toString()
            val password = binding.tiPassword.text.toString()

            if (email.isEmpty()){
                binding.tiEmail.error = getString(R.string.card_required)
            }

            if (password.isEmpty()){
                binding.tiPassword.error = getString(R.string.card_required)
            }

            envia(nombre, email, password)
        }
    }

    private fun envia(nombre:String, email: String, password: String){
//        Toast.makeText(this, nombre, Toast.LENGTH_SHORT).show()
        val url = Constants.BASE_URL + Constants.API_PATH + Constants.LOGIN_PATH

        val email = binding.tiEmail.text.toString().trim()
        val password = binding.tiPassword.text.toString().trim()

        val jsonParams = JSONObject()

        jsonParams.put(Constants.EMAIL_PARAM, email)
        jsonParams.put(Constants.PASSWORD_PARAM, password)

        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, url, jsonParams, { response ->
//            me retorna los datos de guardado
            Log.i("respuesta", response.toString())

            // manejamos la respuesta
            val jsonObject = JSONTokener(response.toString()).nextValue() as JSONObject
            // capturamos el id que nos devolvio el registro
            val mensaje = jsonObject.getString("mensaje")

                val usuario = jsonObject.getString("usuario")

                // Obtenemos el PreferenceManager
                val preferencias  = getPreferences(Context.MODE_PRIVATE);

                with(preferencias.edit()){
                    putString("usuario", usuario)
                    putString("email", email)
                        .apply()
                }

                val intent = Intent(this@LoginActivity,PrincipalActivity::class.java);
                startActivity(intent);



            // Log.i("nombre g ", preferencias.getString("nombre", "NA").toString())

//            preferencias.getString()

            //updateUI("Se registro correctamente")

        },{
            if (it.networkResponse.statusCode == 401){
                //updateUI("error en la peticion")
                Toast.makeText(this, "Usuario y Password invalidos", Toast.LENGTH_SHORT).show()
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

    private fun updateUI(result: String) {
        //binding.tvResult.visibility = View.VISIBLE
        //binding.tvResult.text = result
    }

    // verificamos si tiene conexion a internet
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    private fun session(){

    }
}