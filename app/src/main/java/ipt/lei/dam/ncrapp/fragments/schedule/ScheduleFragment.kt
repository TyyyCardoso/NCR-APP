package ipt.lei.dam.ncrapp.fragments.schedule

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.adapters.ScheduleAdapter
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.models.schedule.ScheduleResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ScheduleFragment : BasicFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter
    private lateinit var docReference : String
    private lateinit var docName : String
    private val url = "" + RetrofitClient.BASE_URL + "files/docs/"


    companion object {
        var scheduleList: List<ScheduleResponse> = emptyList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        setupLoadingAnimation(view)

        val fabSchedule: FloatingActionButton = view.findViewById(R.id.fab_add_schedule)

        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.userInfo), AppCompatActivity.MODE_PRIVATE)
        val clientType = sharedPref.getString(getString(R.string.clientType), getString(R.string.member))

        if(!clientType.equals(getString(R.string.admin))){
            fabSchedule.visibility = View.GONE
        }else {
            fabSchedule.visibility = View.VISIBLE
            fabSchedule.setOnClickListener {
                findNavController().navigate(R.id.navigation_schedule_add)
            }
        }


        val backButton = requireActivity().findViewById<ImageView>(R.id.back_button)
        backButton.visibility = View.GONE

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = ScheduleAdapter(requireContext(), scheduleList).apply {
            // Definiçao do clickListener de abrir detalhes
            onItemClickListener = { schedule ->
                AlertDialog.Builder(requireContext())
                    .setTitle(requireContext().getString(R.string.dialogAlertTitle))
                    .setMessage(getString(R.string.dialogAlertMessage5, schedule.docName))
                    .setNeutralButton(getString(R.string.dialogNegativeButton), null)
                    .setPositiveButton(getString(R.string.addEventTextEventTranspBox)) { _, _ ->
                        deleteSchedule(schedule.id)
                    }
                    .show()
            }

            onItemClickTransferListener = { schedule ->

                docName = schedule.docName!!
                docReference = url + schedule.docReference!!

                AlertDialog.Builder(requireContext())
                    .setTitle(requireContext().getString(R.string.dialogAlertTitle))
                    .setMessage(getString(R.string.dialogAlertMessage6, docName))
                    .setNeutralButton(getString(R.string.dialogNegativeButton), null)
                    .setPositiveButton(getString(R.string.addEventTextEventTranspBox)) { _, _ ->
                        downloadFile(docReference, docName)
                    }
                    .show()
            }
        }
        recyclerView.adapter = adapter
        recyclerView.visibility = View.VISIBLE

        getSchedulesList()

        return view
    }



    private fun getSchedulesList() {
        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.getSchedules().execute()
            },
            onSuccess = { schedulesListResponse ->
                scheduleList = schedulesListResponse
                adapter.updateData(scheduleList)

            },
            onError = { errorMessage ->
                if (toast != null) {
                    toast!!.setText(errorMessage)
                } else {
                    toast = Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT)
                }
                toast!!.show()
            }
        )
    }

        private fun deleteSchedule(id: Int?) {
            makeRequestWithRetries(
                requestCall = {
                    RetrofitClient.apiService.deleteSchedule(id).execute()
                },
                onSuccess = {
                    getSchedulesList()
                },
                onError = { errorMessage ->
                    if (toast != null) {
                        toast!!.setText(errorMessage)
                    } else {
                        toast = Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT)
                    }
                    toast!!.show()
                }
            )
        }



    private fun downloadFile(url: String?, docName: String?) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(url)
                val connection = url.openConnection()
                val inputStream = connection.getInputStream()

                val dir = requireContext().getExternalFilesDir(null)

                val file = File(dir, docName)
                val output = FileOutputStream(file)

                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                output.close()
                inputStream.close()
                withContext(Dispatchers.Main) {
                    openFileInDocumentReader(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions or errors
            }
        }
    }

    private fun openFileInDocumentReader(file: File) {
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().applicationContext.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION

        try{
            requireContext().startActivity(intent)
        }catch (e: ActivityNotFoundException) {
        }

    }
}