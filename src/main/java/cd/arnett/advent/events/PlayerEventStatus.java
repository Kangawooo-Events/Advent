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
    OMITTED,

    /**
     * Finished taking part in the event
     * however you want to interpret that, like they won, died, either way they're done
     */
    FINISHED;

    public boolean isPlaying()
    {
        return this == PLAYING || this == FINISHED;
    }
}
