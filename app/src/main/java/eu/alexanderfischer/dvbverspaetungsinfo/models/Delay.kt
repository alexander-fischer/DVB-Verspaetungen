package eu.alexanderfischer.dvbverspaetungsinfo.models

import com.google.gson.annotations.SerializedName
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.PrimaryKey

open class Delay(
        @PrimaryKey var id: String = "0",
        @SerializedName("created_at") var createdAt: String = "",
        var text: String = "",
        var state: String = "",
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
            return realm.where(Delay::class.java).findAll()
        }

        fun allDelays(): ArrayList<Delay> {
            val realm = Realm.getDefaultInstance()
            val list = realm.copyFromRealm(allRealmDelays())
            return ArrayList(list)
        }

        fun liveResults(): LiveRealmData<Delay> {
            val delays = allRealmDelays()
            return LiveRealmData(delays)
        }
    }
}