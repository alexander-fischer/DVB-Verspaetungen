package eu.alexanderfischer.dvbverspaetungsinfo.helper

import org.greenrobot.eventbus.EventBus

object DvbEventBus {

    /**
     * Broadcasts any kind of object.
     */
    fun broadcast(any: Any) {
        EventBus.getDefault().post(any)
    }
}