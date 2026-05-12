package com.fit5046.wattwise


// Password validation regex: min 8 chars, 1 uppercase, 1 digit, 1 special char
// Design Guideline 1: Complexity enforcement with real-time keystroke validation
private val PASSWORD_REGEX = Regex("^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#!$%^&*]).{8,}$")

fun validatePassword(password: String): String? {
    return if (password.isEmpty()) null
    else if (!PASSWORD_REGEX.matches(password))
        "Password must be 8+ characters with an uppercase letter, number, and special character"
    else null
}