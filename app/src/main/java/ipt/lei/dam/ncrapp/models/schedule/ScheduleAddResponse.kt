package ipt.lei.dam.ncrapp.models.schedule

import android.net.Uri

data class ScheduleAddResponse(val docName : String, val docDescription : String, val docType : String, val pdf: Uri?)