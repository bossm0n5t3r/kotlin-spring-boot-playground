package me.bossm0n5t3r.mfatotp.user

class User(
    val username: String,
    val password: String,
) {
    var totpSecret: String? = null
    var mfaEnabled = false
}
