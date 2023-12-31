package dev.reprator.userIdentity.data

import dev.reprator.country.data.TableCountryEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime

enum class USER_CATEGORY(userType: String) {
    ADMIN("admin"),
    OWNER("owner"),
    EMPLOYEE("employee")
}

object TableUserIdentity : Table(name="user_login_data") {

    val id = integer("userid").autoIncrement()
    val phoneNumber = text("phonenumber")
    val phoneCountryId = (integer("phonecountryid").references(TableCountryEntity.table.id))
    val phoneOtp = integer("phoneotp").nullable()
    val isPhoneVerified = bool("isphoneverified").default(false)

    override val primaryKey = PrimaryKey(id)
}
