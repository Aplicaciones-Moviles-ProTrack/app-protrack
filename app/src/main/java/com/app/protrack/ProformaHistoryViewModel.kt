// Expone y actualiza el historial local de proformas.
package com.app.protrack

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.protrack.data.local.Proforma
import com.app.protrack.data.local.ProtrackDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Controlador entre la interfaz y el DAO de proformas. */
class ProformaHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = ProtrackDatabase.getInstance(application).proformaDao()

    val historial: StateFlow<List<Proforma>> = dao.observarHistorial()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Persiste la proforma sin bloquear la interfaz.
    fun guardar(proforma: Proforma) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertar(proforma)
        }
    }
}
