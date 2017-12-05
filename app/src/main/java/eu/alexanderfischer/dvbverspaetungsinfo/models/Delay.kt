package eu.alexanderfischer.dvbverspaetungsinfo.models

import com.google.gson.annotations.SerializedName

data class Delay(val id: String,
                 @SerializedName("created_at")
                 val createdAt: String,
                 val text: String,
                 val state: String,
                 val dayOfWeek: String,
                 val linien: List<String>)