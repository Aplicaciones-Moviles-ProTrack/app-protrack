// Gestiona la pantalla de cálculo de área.
package com.app.protrack

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

class AreaFragment : Fragment() {

    private lateinit var viewModel: AreaViewModel

    // Infla y prepara la vista del fragmento.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val vista = inflater.inflate(
            R.layout.fragment_area,
            container,
            false
        )

        viewModel = AreaViewModel()

        val etLargo = vista.findViewById<EditText>(R.id.etLargo)
        val etAncho = vista.findViewById<EditText>(R.id.etAncho)
        val tvResultado = vista.findViewById<TextView>(R.id.tvResultadoArea)

        val watcher = object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                actualizarArea(
                    etLargo,
                    etAncho,
                    tvResultado
                )
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        etLargo.addTextChangedListener(watcher)
        etAncho.addTextChangedListener(watcher)

        return vista
    }

    // Recalcula y muestra el área introducida.
    private fun actualizarArea(
        etLargo: EditText,
        etAncho: EditText,
        tvResultado: TextView
    ) {

        val resultado = viewModel.calcularArea(
            etLargo.text.toString(),
            etAncho.text.toString()
        )

        tvResultado.text = "$resultado m²"
    }
}