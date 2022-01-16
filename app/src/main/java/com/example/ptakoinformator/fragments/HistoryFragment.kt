package com.example.ptakoinformator.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ptakoinformator.R
import com.example.ptakoinformator.viewmodels.HistoryViewModel
import com.example.ptakoinformator.viewmodels.HistoryViewModelFactory

class HistoryFragment: Fragment() {

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((requireNotNull(this.activity).application)) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.history_fragment,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val historyListAdapter = HistoryListAdapter(viewModel.birds)

        val historyLayoutManager = GridLayoutManager(context,2)
        view.findViewById<RecyclerView>(R.id.rv_history)
            .apply {
                adapter = historyListAdapter
                layoutManager = historyLayoutManager
            }

        view.findViewById<Button>(R.id.button_export_html).setOnClickListener {
            //TODO: Generate html file
        }
    }

    /*fun createFile(pickerInitialUri: Uri){
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        document.textContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><html>\n" +
                "<head>\n" +
                "    <title>Hello world</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1 class=\"h1Class\">My header1</h1>\n" +
                "    <p class=\"pClass\">paragraph1</p>\n" +
                "</body>\n" +
                "</html>"
        with(TransformerFactory.newInstance().newTransformer()){
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"no")
            setOutputProperty(OutputKeys.METHOD, "xml")
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        }
    }*/

}