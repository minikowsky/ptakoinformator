package com.example.ptakoinformator.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.ptakoinformator.R
import com.example.ptakoinformator.data.BirdDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btn_clear_DB).setOnClickListener {
            clearDB()
        }
    }

    private fun clearDB() {
        val builder = AlertDialog.Builder(activity).apply {
            setTitle("Potwierdź czyszczenie")
            setMessage("Czy na pewno chcesz wyczyścić bazę?")
            setPositiveButton("Yes") { dialog, id ->
                GlobalScope.launch {
                    BirdDatabase.getInstance(context).clearAllTables()
                }
                dialog.cancel()
                activity?.viewModelStore?.clear()
            }
            setNegativeButton("No") { dialog, id ->
                dialog.cancel()

            }
        }
        val alert = builder.create()
        alert.show()

    }
}