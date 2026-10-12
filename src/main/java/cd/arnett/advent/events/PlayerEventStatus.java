package cd.arnett.advent.events;

public enum PlayerEventStatus {
    /**
     * Not registered for the event
     */
    OMITTED,

    /**
     * registered for the event and currently active
     */
    PLAYING,

    /**
     * registered for the event but not currently taking part
     */
    ABSENT;

}
