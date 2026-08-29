package com.cryptium.coincurrently.ui.main.Extensions

/**
 * Unwraps Result when double-wrapped (e.g. from cache). Use when API returns Result<T>
 * but cache may have stored Result<Result<T>>.
 */
fun <T> Result<T>.unwrapOrNull(): T? {
    var value: Any? = getOrNull()
    while (value is Result<*>) {
        value = value.getOrNull()
    }
    @Suppress("UNCHECKED_CAST")
    return value as? T
}
