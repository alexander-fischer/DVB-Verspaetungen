package eu.alexanderfischer.dvbverspaetungsinfo.ui;

/**
 * Created by Alexander Fischer.
 *
 * Adapter class that represents the all DelayInformation objects.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import eu.alexanderfischer.dvbverspaetungsinfo.R;
import eu.alexanderfischer.dvbverspaetungsinfo.helpers.DateHelper;
import eu.alexanderfischer.dvbverspaetungsinfo.helpers.TextHelper;
import eu.alexanderfischer.dvbverspaetungsinfo.models.DelayInformation;

/**
 * Implementation of an ArrayAdapter.
 */
public class DelayInformationAdapter extends ArrayAdapter<DelayInformation> {
    ArrayList<DelayInformation> tweetObjects;
    int layout;
    boolean isFilterActivated;
    String mDayOfWeek = "";

    public DelayInformationAdapter(Context context, int layout, int resource, ArrayList<DelayInformation> pTweetObjects, boolean isFilterActivated) {
        super(context, layout, resource, pTweetObjects);
        this.tweetObjects = pTweetObjects;
        this.layout = layout;
        this.isFilterActivated = isFilterActivated;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        View view = convertView;

        Date currentDate = DateHelper.getCurrentTimeAndDate();
        String currentDayOfWeek = new SimpleDateFormat("EE", Locale.ENGLISH)
                .format(currentDate.getTime());

        if (view == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(layout, null);

            holder.text = (TextView) view.findViewById(R.id.list_layout_textview);
            holder.infoText = (TextView) view.findViewById(R.id.list_layout_infos);
            holder.dateText = (TextView) view.findViewById(R.id.list_layout_date);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        final DelayInformation delayInformation = tweetObjects.get(position);

        if (delayInformation.getText() != null) {
            holder.text.setText(delayInformation.getText());
        } else {
            holder.text.setText("");
            holder.text.setVisibility(View.GONE);
        }

        if (!delayInformation.getInfoText().equals("")) {
            holder.infoText.setVisibility(View.VISIBLE);
            if (isFilterActivated) {
                holder.infoText.setText(TextHelper.makeInfoText(getContext(), delayInformation,
                        isFilterActivated));
            } else {
                holder.infoText.setText(delayInformation.getInfoText());
            }
        } else {
            holder.infoText.setVisibility(View.GONE);
        }

        if (!delayInformation.getDayOfWeek().equals("")) {
            String dayOfWeek = delayInformation.getDayOfWeek();

            if (dayOfWeek.equals(mDayOfWeek)) {
                holder.dateText.setVisibility(View.GONE);

            } else {
                if (currentDayOfWeek.equals(dayOfWeek)) {
                    holder.dateText.setVisibility(View.VISIBLE);
                    holder.dateText.setText("Heute");

                } else {
                    holder.dateText.setVisibility(View.VISIBLE);
                    holder.dateText.setText(delayInformation.getDayOfWeek());
                }
            }

            mDayOfWeek = dayOfWeek;
        } else {
            holder.dateText.setVisibility(View.GONE);
        }

        return view;
    }

    /**
     * For applying the ViewHolder pattern for adapters.
     */
    static class ViewHolder {
        TextView text;
        TextView infoText;
        TextView dateText;
    }
}
