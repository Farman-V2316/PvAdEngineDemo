package com.newshunt.dataentity.onboarding;

/**
 * Enum saying the possible Registration States
 * <p/>
 * NOT_REGISTERED -- Device is in NOT_REGISTERED STATE (No server Ping Pong is in progress)
 * <p/>
 * IN_PROGRESS -- Registration is in Progress
 * <p/>
 * REGISTERED -- Device is REGISTERED successfully once (We can proceed in language Selection)
 */
public enum RegistrationState {
  NOT_REGISTERED, IN_PROGRESS, REGISTERED
}
