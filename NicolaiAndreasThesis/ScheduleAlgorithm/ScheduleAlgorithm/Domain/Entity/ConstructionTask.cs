using System;
using System.Collections.Generic;

namespace ScheduleAlgorithm.Domain.Entity
{
    public class ConstructionTask 
    {
        #region Properties
        // identifications
        public string TaskID { get; set; }
        public int Zone { get; set; }
        public string Craft { get; set; }
        public string Operation { get; set; }
        // constraints
        public int EstimatedResources { get; set; }
        public double ActualResources { get; set; }
        public ProgressState Progress { get; set; } 
        public string Precedence { get; set; }
        // time units
        public double EstimatedDuration { get; set; }
        public bool IsCritical { get => GetLS()-GetES()==0;}



        /// <summary>
        /// get the earliest start 
        /// </summary>
        /// <returns>Earliest start</returns>
        public double GetES()
        {
            if (EF == 0) return EF;
            return EF - EstimatedDuration;

        }
        /// <summary>
        /// Get the latest start
        /// </summary>
        /// <returns>Latest start</returns>
        public double GetLS()
        {
            if (LF == double.PositiveInfinity) return LF;
            return LF - EstimatedDuration;

        }        
  
        public double EF { get; set; }
        public double LF { get; set; } = double.PositiveInfinity;

        #endregion

        #region Methods
        /// <summary>
        /// Set Resources by Takt
        /// </summary>
        /// <param name="takt"></param>
        public void SetResourcesByTakt(double takt)
        {
            ActualResources = (EstimatedDuration * EstimatedResources) / takt;
            var actualDuration = GetActualDuration();

            if (actualDuration == -1)
            {
                return;
            }

            EstimatedDuration = GetActualDuration();
        }
      
        /// <summary>
        /// Print string presentation of task id
        /// </summary>
        /// <returns></returns>
        public override string ToString()
        {
            return $"{TaskID}";
        }
        
        /// <summary>
        /// Id compare
        /// </summary>
        public static IEqualityComparer<ConstructionTask> TaskIdComparer { get; } = new TaskIdEqualityComparer();
        #endregion

        #region Helper Methods
        /// <summary>
        /// Get actual duration from allocated resources
        /// </summary>
        /// <returns>-1, if no resources are allocated, otherwise actual duration</returns>
        private double GetActualDuration(int constant = 0)
        {

            return ActualResources >= 0 ? EstimatedDuration * EstimatedResources / ActualResources + constant : -1;
        }
        private sealed class TaskIdEqualityComparer : IEqualityComparer<ConstructionTask>
        {
            public bool Equals(ConstructionTask x, ConstructionTask y)
            {
                if (ReferenceEquals(x, y)) return true;
                if (ReferenceEquals(x, null)) return false;
                if (ReferenceEquals(y, null)) return false;
                if (x.GetType() != y.GetType()) return false;
                return x.TaskID == y.TaskID;
            }

            public int GetHashCode(ConstructionTask obj)
            {                               
                return obj.TaskID.GetHashCode();
            }
        }
        #endregion


    }
}
