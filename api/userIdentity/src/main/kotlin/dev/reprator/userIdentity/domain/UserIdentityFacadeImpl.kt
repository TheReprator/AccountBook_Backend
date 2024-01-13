package dev.reprator.userIdentity.domain

import dev.reprator.base.action.JwtTokenService
import dev.reprator.base.beans.LENGTH_OTP
import dev.reprator.modals.user.UserIdentityId
import dev.reprator.modals.user.UserIdentityOTPModal
import dev.reprator.userIdentity.data.UserIdentityRepository
import dev.reprator.userIdentity.modal.*
import dev.reprator.userIdentity.socialVerifier.SMScodeGenerator
import org.joda.time.DateTime
import kotlin.random.Random

class UserIdentityFacadeImpl(
    private val repository: UserIdentityRepository,
    private val smsUseCase: SMScodeGenerator,
    private val tokenService: JwtTokenService
) : UserIdentityFacade {

    override suspend fun addNewUserIdentity(userInfo: UserIdentityRegisterEntity): UserIdentityRegisterModal {
        return repository.addNewUserIdentity(userInfo)
    }

    private fun generateCode(): Int {
        val random = Random.nextInt(900000) + 100000
        return random
    }

    override suspend fun generateAndSendOTP(userId: UserIdentityId): Boolean {
        val userModal = repository.getUserById(userId)
        val otpCode = generateCode()
        return smsUseCase.sendOtpToMobileNumber(userModal.country.callingCode, userModal.phoneNumber, otpCode).also {
            if (!it)
                return@also
            val updateModal = userModal.copy(
                phoneOtp = otpCode,
                otpCount = (userModal.otpCount.plus(1)), updateTime = DateTime.now().toDateTimeISO()
            )
            repository.updateUserById(updateModal)
        }
    }

    override suspend fun getUserById(userId: UserIdentityId): UserIdentityFullModal = repository.getUserById(userId)

    override suspend fun verifyOTP(otpInfo: UserIdentityOtpEntity): UserIdentityOTPModal {
        val fullUserInfo = repository.getUserById(otpInfo.userId)
        val updatedModal = fullUserInfo.copy(
            phoneOtp = otpInfo.phoneOtp,
            refreshToken = tokenService.generateRefreshToken(fullUserInfo.userId.toString()),
            accessToken = tokenService.generateAccessToken(fullUserInfo.userId.toString()),
            isPhoneVerified = true
        )
        return repository.verifyOtp(updatedModal)
    }

    override suspend fun refreshToken(accessToken: String): UserIdentityOTPModal.DTO {
        val (isTokenValid, userId) = tokenService.isTokenValid(accessToken)
        if (!isTokenValid)
            throw InvalidTokenException()

        val fullModal = repository.getUserById(userId).copy(refreshToken = tokenService.generateAccessToken(userId.toString()))
        return repository.refreshToken(accessToken, fullModal).copy(accessToken = tokenService.generateAccessToken(userId.toString()))
    }

    override suspend fun logout(userId: UserIdentityId): Boolean = repository.logout(userId)

}