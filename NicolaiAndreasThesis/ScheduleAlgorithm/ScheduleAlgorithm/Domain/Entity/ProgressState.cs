namespace ScheduleAlgorithm.Domain.Entity
{
    /// <summary>
    /// Progress state
    /// Indicating what state a <code="ConstructionTask"> is in
    /// </summary>
    public enum ProgressState
    {
        Pending,
        InProgress,
        Finished,
        Delayed
    }
}