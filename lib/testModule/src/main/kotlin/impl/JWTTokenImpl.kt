package impl

import dev.reprator.core.usecase.ACCESS_TOKEN
import dev.reprator.core.usecase.JWTToken
import dev.reprator.core.usecase.REFRESH_TOKEN

class JWTTokenImpl: JWTToken {
    override fun generateToken(): Pair<ACCESS_TOKEN, REFRESH_TOKEN> {
        return Pair("","")
    }
}