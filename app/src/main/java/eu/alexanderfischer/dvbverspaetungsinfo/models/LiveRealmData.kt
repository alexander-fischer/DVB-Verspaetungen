package eu.alexanderfischer.dvbverspaetungsinfo.models

import android.arch.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmResults


class LiveRealmData<T : RealmModel>(realmResults: RealmResults<T>) : LiveData<RealmResults<T>>() {

    private val results: RealmResults<T> = realmResults
    private val listener = RealmChangeListener<RealmResults<T>> { results -> value = results }

    override fun onActive() {
        results.addChangeListener(listener)
    }

    override fun onInactive() {
        results.removeChangeListener(listener)
    }
}