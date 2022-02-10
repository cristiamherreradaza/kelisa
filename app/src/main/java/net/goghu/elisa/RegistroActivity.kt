package net.goghu.elisa

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import net.goghu.elisa.databinding.ActivityRegistroBinding
import org.json.JSONObject
import org.json.JSONTokener

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_registro)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnRegistrar.setOnClickListener{

            val nombre = binding.tiNombre.text.toString()
            val email = binding.tiEmail.text.toString()
            val password = binding.tiPassword.text.toString()

            if (nombre.isEmpty()){
                binding.tiNombre.error = getString(R.string.card_required)
            }

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
        val url = Constants.BASE_URL + Constants.API_PATH + Constants.REGISTER_PATH

        val name = binding.tiNombre.text.toString().trim()
        val email = binding.tiEmail.text.toString().trim()
        val password = binding.tiPassword.text.toString().trim()

        val jsonParams = JSONObject()

        jsonParams.put(Constants.EMAIL_PARAM, email)
        jsonParams.put(Constants.NOMBRE_PARAM, name)
        jsonParams.put(Constants.PASSWORD_PARAM, password)

        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, url, jsonParams, { response ->
//            me retorna los datos de guardado
            Log.i("respuesta", response.toString())

            // manejamos la respuesta
            val jsonObject = JSONTokener(response.toString()).nextValue() as JSONObject
            // capturamos el id que nos devolvio el registro
            val usuario_id = jsonObject.getString("usuario")
            val nombre = jsonObject.getString("nombre")
            val email = jsonObject.getString("email")
            val token = jsonObject.getString("token")

            Log.i("Volley Usuario: ", usuario_id)
            Log.i("Volley nombre: ", nombre)

            // Obtenemos el PreferenceManager
//            val preferencias  = getPreferences(Context.MODE_PRIVATE)
            val preferencias = getSharedPreferences("preferencias", Context.MODE_PRIVATE)

            with(preferencias.edit()){
                putString("usuario_id", usuario_id)
                putString("nombre", nombre)
                putString("email", email)
                putString("token", token)
                    .apply()
            }

            // Log.i("nombre g ", preferencias.getString("nombre", "NA").toString())

            Toast.makeText(this, "Registro exitoso!!!", Toast.LENGTH_SHORT).show()
//            preferencias.getString()
            val intent = Intent(this@RegistroActivity,PrincipalActivity::class.java)
            startActivity(intent)

        },{
            if (it.networkResponse.statusCode == 400){
//                updateUI("error en la peticion")
                Toast.makeText(this, "Error al registrarse", Toast.LENGTH_SHORT).show()
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