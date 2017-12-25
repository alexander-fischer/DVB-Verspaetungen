package eu.alexanderfischer.dvbverspaetungsinfo.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import eu.alexanderfischer.dvbverspaetungsinfo.R
import eu.alexanderfischer.dvbverspaetungsinfo.helper.TextHelper
import eu.alexanderfischer.dvbverspaetungsinfo.models.Delay
import java.util.*

/**
 * Implementation of an ArrayAdapter.
 */
class DelayAdapter(ctx: Context,
                   private val delays: ArrayList<Delay>) :
        ArrayAdapter<Delay>(ctx, R.layout.list_layout, delays) {

    private val layout = R.layout.list_layout

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var holder = ViewHolder()
        var view = convertView
        val delay = delays[position]

        if (view == null) {
            val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = vi.inflate(layout, null)

            holder.text = view.findViewById<View>(R.id.list_layout_textview) as TextView
            holder.infoText = view.findViewById<View>(R.id.list_layout_infos) as TextView
            holder.dateText = view.findViewById<View>(R.id.list_layout_date) as TextView
            view.tag = holder

        } else {
            holder = view.tag as ViewHolder
        }

        if (delay.text != "") {
            holder.text.text = delay.text
        } else {
            holder.text.text = ""
            holder.text.visibility = View.GONE
        }

        val infoText = TextHelper.makeInfoText(delay)
        if (infoText != "") {
            holder.infoText.visibility = View.VISIBLE
            holder.infoText.text = infoText
        } else {
            holder.infoText.visibility = View.GONE
        }

        return view!!
    }

    /**
     * For applying the ViewHolder pattern for adapters.
     */
    private class ViewHolder {
        lateinit var text: TextView
        lateinit var infoText: TextView
        lateinit var dateText: TextView
    }
}
