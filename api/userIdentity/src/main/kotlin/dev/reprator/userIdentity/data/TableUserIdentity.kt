package dev.reprator.userIdentity.data

import dev.reprator.country.data.TableCountryEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime

object TableUserIdentity : Table(name="user_login_data") {

    val id = integer("userid").autoIncrement()
    val phoneNumber = text("phonenumber")
    val phoneCountryId = (integer("phonecountryid").references(TableCountryEntity.table.id))
    val phoneOtp = integer("phoneotp").nullable()
    val isPhoneVerified = bool("isphoneverified").default(false)
    val userType = enumerationByName<USER_CATEGORY>("usertype", 20, USER_CATEGORY::class).default(USER_CATEGORY.OWNER)
    val refreshToken = text("refreshtoken").nullable()
    val creationTime = datetime("creationtime").default(DateTime.now().toDateTimeISO())
    val updateTime = datetime("updatetime").default(DateTime.now().toDateTimeISO())

    override val primaryKey = PrimaryKey(id)
}
