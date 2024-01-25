package dev.reprator.userIdentity.data

import dev.reprator.country.data.TableCountryEntity
import dev.reprator.modals.user.USER_CATEGORY
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import org.postgresql.util.PGobject

object TableUserIdentity : Table(name="user_login_data") {

    val id = integer("userid").autoIncrement()
    val phoneNumber = text("phonenumber")
    val phoneCountryId = (integer("phonecountryid").references(TableCountryEntity.table.id))
    val phoneOtp = integer("phoneotp").nullable()
    val otpCount = integer("otp_count").default(0)
    val isPhoneVerified = bool("isphoneverified").default(false)
    /*Enable this for testing and comment other*/
   //val userType = enumerationByName<USER_CATEGORY>("usertype", 20, USER_CATEGORY::class).default(USER_CATEGORY.owner)
    val userType = customEnumeration("usertype", "USERCATEGORY", { value ->
        USER_CATEGORY.valueOf(value as String) }, { PGEnum("usercategory", it) }).default(USER_CATEGORY.owner)
    val refreshToken = text("refreshtoken").nullable()
    val creationTime = datetime("creationtime").default(DateTime.now().toDateTimeISO())
    val updateTime = datetime("updatetime").default(DateTime.now().toDateTimeISO())

    override val primaryKey = PrimaryKey(id)
}

class PGEnum<T : Enum<T>>(enumTypeName: String, enumValue: T?) : PGobject() {
    init {
        value = enumValue?.name
        type = enumTypeName
    }
}
