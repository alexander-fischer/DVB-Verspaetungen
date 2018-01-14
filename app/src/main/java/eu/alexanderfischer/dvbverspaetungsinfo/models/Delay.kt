package eu.alexanderfischer.dvbverspaetungsinfo.models

import com.google.gson.annotations.SerializedName
import io.realm.*
import io.realm.annotations.PrimaryKey

open class Delay(
        @PrimaryKey var id: String = "0",
        var createdAt: String = "",
        var text: String = "",
        var state: String = "",
        @SerializedName("dayOfWeek")
        var dayOfWeek: String = "",
        var linien: RealmList<String> = RealmList()) : RealmObject() {

    fun save() {
        val realm = Realm.getDefaultInstance()

        realm.executeTransaction {
            realm.copyToRealmOrUpdate(this)
        }
    }

    companion object {
        private fun allRealmDelays(): RealmResults<Delay> {
            val realm = Realm.getDefaultInstance()
            val query = realm.where(Delay::class.java).sort("id", Sort.DESCENDING)
            return query.findAll()
        }

        fun allDelays(): ArrayList<Delay> {
            val realm = Realm.getDefaultInstance()
            val list = realm.copyFromRealm(allRealmDelays())
            return ArrayList(list)
        }

        fun getAllFromLiveResults(results: RealmResults<Delay>): List<Delay> {
            val realm = Realm.getDefaultInstance()

            val list = realm.copyFromRealm(results)
            list.sortByDescending { it.id }
            return list
        }

        fun liveResults(): LiveRealmData<Delay> {
            val delays = allRealmDelays()
            return LiveRealmData(delays)
        }
    }
}