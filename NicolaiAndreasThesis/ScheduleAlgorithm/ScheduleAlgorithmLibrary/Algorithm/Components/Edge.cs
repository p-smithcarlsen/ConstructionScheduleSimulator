using ScheduleAlgorithm.Domain.Entity;
using System;
using System.Collections.Generic;

namespace ScheduleAlgorithmLibrary.Algorithm.Components
{        
    public class Edge
    {
        #region Default Constructor
        public Edge() { }
        #endregion

        #region Properties
        // tail
        public ConstructionTask From { get; set; }
        // head
        public ConstructionTask To { get; set; }
        // weight
        public double Weight { get; set; }
        #endregion
    }
}
