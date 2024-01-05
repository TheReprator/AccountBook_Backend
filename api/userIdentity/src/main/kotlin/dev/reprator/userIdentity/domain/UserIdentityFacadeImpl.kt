package dev.reprator.userIdentity.domain

import dev.reprator.core.util.constants.LENGTH_OTP
import dev.reprator.userIdentity.data.UserIdentityRepository
import dev.reprator.userIdentity.modal.UserIdentityFullModal
import dev.reprator.userIdentity.modal.UserIdentityId
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal
import dev.reprator.userIdentity.socialVerifier.SMScodeGenerator
import org.joda.time.DateTime
import kotlin.random.Random

class UserIdentityFacadeImpl(
    private val repository: UserIdentityRepository,
    private val smsUseCase: SMScodeGenerator
) : UserIdentityFacade {

    override suspend fun addNewUserIdentity(userInfo: UserIdentityRegisterEntity): UserIdentityRegisterModal {
        return repository.addNewUserIdentity(userInfo)
    }

    private suspend fun generateCode(): Int {
        val random = Random.nextInt(999999)
        return String.format("%0${LENGTH_OTP}d", random).toInt()
    }

    override suspend fun generateAndSendOTP(userId: UserIdentityId): Boolean {
        val userModal = repository.getUserById(userId)
        val otpCode = generateCode()
        return smsUseCase.sendOtpToMobileNumber(userModal.country.callingCode, userModal.phoneNumber, otpCode).also {
            if(!it)
                return@also
            val updateModal = userModal.copy(phoneOtp= otpCode,
                otpCount = (userModal.otpCount + 1), updateTime = DateTime.now().toDateTimeISO())
            repository.updateUserById(updateModal)
        }
    }

    override suspend fun getUserById(userId: UserIdentityId): UserIdentityFullModal = repository.getUserById(userId)

}