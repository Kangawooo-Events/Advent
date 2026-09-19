package cd.arnett.advent.events;

public enum PlayerEventStatus {
    /**
     * registered for the event and currently active
     */
    PLAYING,

    /**
     * registered for the event but not currently taking part
     */
    ABSENT,

    /**
     * Not registered for the event
     */
    OMITTED

    public PlayerEventStatus disconnected()
    {
        return this == PLAYING ? ABSENT : this;
    }

}
