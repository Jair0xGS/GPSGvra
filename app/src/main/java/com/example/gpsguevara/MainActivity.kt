package com.example.gpsguevara

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.lang.Exception
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_FINE_REQUEST: Int = 99
    val DEFAULT_UPDATE_INTERVAL = 5
    val FAST_UPDATE_INTERVAL= 5
    lateinit var tv_lat : TextView
    lateinit var tv_lon : TextView
    lateinit var tv_altitude: TextView
    lateinit var tv_accuracy : TextView
    lateinit var tv_speed: TextView
    lateinit var tv_sensor: TextView
    lateinit var tv_updates: TextView
    lateinit var tv_address: TextView
    lateinit var sw_location_updates :Switch
    lateinit var sw_gps :Switch
    //varialbe to remember if we are tracking or not
    var updateOn = false
    //this is a config file for all settings related to focused...
    lateinit var locationRequest: LocationRequest
    //location service of google
    lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_lat = findViewById(R.id.tv_lat)
        tv_lon= findViewById(R.id.tv_lon)
        tv_altitude= findViewById(R.id.tv_altitude)
        tv_accuracy= findViewById(R.id.tv_accuracy)
        tv_speed= findViewById(R.id.tv_speed)
        tv_sensor= findViewById(R.id.tv_sensor)
        tv_updates= findViewById(R.id.tv_updates)
        tv_address= findViewById(R.id.tv_address)
        sw_gps= findViewById(R.id.sw_gps)
        sw_location_updates= findViewById(R.id.sw_locationsupdates)


        //init location request
        locationRequest = LocationRequest.create().apply {
            interval=               (1000 * DEFAULT_UPDATE_INTERVAL).toLong()
            fastestInterval=        (1000*FAST_UPDATE_INTERVAL).toLong()
            priority=               LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?:return
                super.onLocationResult(locationResult)
                actualizarValoresEnUI(location = locationResult.lastLocation)
            }
        }

        sw_gps.setOnClickListener {
            if (sw_gps.isChecked) {
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                tv_sensor.text= "USANDO SENSORES GPS"
            }else{
                locationRequest.priority=LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                tv_sensor.text="USANDO ANTENAS DE TELEFONO Y WIFI"
            }
        }
        sw_location_updates.setOnClickListener{
            if(sw_location_updates.isChecked){
                //iniciar rastreo de ubicacion
                empezarActualizacionDeUbicacion()

            }else{
                //detener rastreo de ubicacion

                detenetActualizacionesDeUbicacion()
            }
        }
        actualizarGps()
    }

    private fun detenetActualizacionesDeUbicacion() {

        val messge = "Tu ubicacion no se esta rastreando"
        tv_lat.text=messge
        tv_lon.text=messge
        tv_altitude.text=messge
        tv_accuracy.text=messge
        tv_speed.text=messge
        tv_sensor.text=messge
        tv_updates.text=messge
        tv_address.text=messge
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun empezarActualizacionDeUbicacion() {
        tv_updates.text="Tu ubicacion esta siendo rastreada"
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(this,"No se tienen los permisos para accceder a su ubicacion",Toast.LENGTH_SHORT).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),PERMISSIONS_FINE_REQUEST)
            }
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        Toast.makeText(this,"Si se tienen los permisos para accceder a su ubicacion",Toast.LENGTH_SHORT).show()
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSIONS_FINE_REQUEST->{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    actualizarGps()
                }else{
                    Toast.makeText(this,"No se tienen los permisos para accceder a su ubicacion",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun actualizarGps(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
            ==
                    PackageManager.PERMISSION_GRANTED
        ){
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { p0 ->
                if (p0 != null){
                    actualizarValoresEnUI(
                        p0!!
                    )
                }
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),PERMISSIONS_FINE_REQUEST)
            }
        }

    }

    private fun actualizarValoresEnUI(location: Location) {

        Toast.makeText(this,"actualizando ui",Toast.LENGTH_SHORT).show()
        //actualizar todos los valores de la ui con la nueva posicion
        tv_lat.text= location.latitude.toString()
        tv_lon.text= location.longitude.toString()
        tv_accuracy.text= location.accuracy.toString()
        if(location.hasAltitude()){
            tv_altitude.text= location.altitude.toString()
        }else{
            tv_altitude.text="NO DISPONIBLE"
        }
        if(location.hasSpeed()){
            tv_speed.text= location.speed.toString()
        }else{
            tv_speed.text="NO DISPONIBLE"
        }
        val geocoder : Geocoder= Geocoder(this)
        try {
            val address : List<Address> = geocoder.getFromLocation(location.latitude,location.longitude,1)
            tv_address.text=address[0].getAddressLine(0)
        }catch (e :Exception){
            tv_address.text="Direccion no encontrada"
        }


    }
}