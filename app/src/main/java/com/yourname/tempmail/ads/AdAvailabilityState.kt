package com.yourname.tempmail.ads

/** Observability state for each ad slot. */
enum class AdAvailabilityState {
    NOT_CONFIGURED,
    LOADING,
    AVAILABLE,
    UNAVAILABLE, // SDK reported no fill
    FAILED,
    DISABLED,     // user opted out
}

data class AdState(
    val available: AdAvailabilityState = AdAvailabilityState.NOT_CONFIGURED,
) {
    val canShow: Boolean get() = available == AdAvailabilityState.AVAILABLE
}