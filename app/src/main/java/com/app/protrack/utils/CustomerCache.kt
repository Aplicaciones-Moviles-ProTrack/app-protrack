// Guarda clientes recientes para reutilizarlos en nuevas proformas.
package com.app.protrack.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class CustomerInfo(val name: String, val email: String)

class CustomerCache(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("customer_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Guarda un cliente reciente evitando duplicados.
    fun saveCustomer(name: String, email: String) {
        val customers = getCustomers().toMutableList()
        // Evitar duplicados por email
        customers.removeAll { it.email == email }
        customers.add(0, CustomerInfo(name, email))
        
        // Limitar a los últimos 20 clientes
        val limitedList = customers.take(20)
        
        prefs.edit().putString("customers_json", gson.toJson(limitedList)).apply()
    }

    // Recupera la lista de clientes almacenada localmente.
    fun getCustomers(): List<CustomerInfo> {
        val json = prefs.getString("customers_json", null) ?: return emptyList()
        val type = object : TypeToken<List<CustomerInfo>>() {}.type
        return gson.fromJson(json, type)
    }
}