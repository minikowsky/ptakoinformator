package com.example.ptakoinformator.fragments

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ptakoinformator.R
import com.example.ptakoinformator.data.Bird
import com.example.ptakoinformator.viewmodels.HistoryViewModel
import com.example.ptakoinformator.viewmodels.HistoryViewModelFactory
import java.io.File
import java.util.*
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import java.io.IOException
import java.io.OutputStream


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

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val historyListAdapter = HistoryListAdapter(viewModel.birds, viewModel)

        val historyLayoutManager = LinearLayoutManager(context)

        viewModel.birds.observe(viewLifecycleOwner,
        Observer {
            historyListAdapter.notifyDataSetChanged()
        })

        view.findViewById<RecyclerView>(R.id.rv_history)
            .apply {
                adapter = historyListAdapter
                layoutManager = historyLayoutManager
            }

        view.findViewById<Button>(R.id.button_export_html).setOnClickListener {
            //TODO: Generate html file

            val htmlText: String = getHtmlText(viewModel.birds)
            try {
                val values = ContentValues()

                values.put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    "report"
                )

                values.put(
                    MediaStore.MediaColumns.MIME_TYPE,
                    "text/html"
                )

                values.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS + "/Reports/"
                )


                val uri: Uri? = requireActivity().contentResolver.insert(
                    MediaStore.Files.getContentUri("external"),
                    values
                )


                val outputStream: OutputStream? =
                    requireActivity().contentResolver.openOutputStream(uri!!)

                outputStream?.write(htmlText.toByteArray())

                outputStream?.close()
            } catch ( e:IOException) {
                Toast.makeText(view.getContext(), "Fail to create file", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private fun getHtmlText(birds: LiveData<List<Bird>>): String {
        var s: String = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><html>\n" +
                "<head>\n" +
                "<title>Hello world</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<header>Raport z dnia ${Date()}</header>" +
                "<table>\n" +
                "<td>"
        birds.value?.forEach {
            s+="<tr><p>${it.date}<img src=\"${File(it.photoUri).toUri()}\" alt=\"${File(it.photoUri).toUri()}\" width=\"500\" height=\"500\">"
            s+="<p>Klasyfikacja:</p>"
            s+="<p>Największe prawdopodobieństwo: ${it.classification.mainClassification}-${it.classification.mainProbability}</p>"
            s+="<p>Drugie najwyższe: ${it.classification.secondClassification}-${it.classification.secondProbability}</p>"
            s+="<p>Trzecie najwyższe: ${it.classification.thirdClassification}-${it.classification.thirdProbability}</p></p></tr>"
        }

        s += "</td>\n</table>\n</body>\n</html>"
        return s
    }

    private fun saveHtmlFileToStorage(htmlText: String){
        val fileCollection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
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


