using ScheduleAlgorithm.Domain.Entity;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;

namespace ScheduleAlgorithmLibrary.Algorithm
{
    /// <summary>
    /// Class Determine if there exist a DAG
    /// If a cycle exist, the class returns null
    /// <bref from="Algorithhms 4th Edition by Robert Sedgewick, Kevin Wayne, page 581"></bref>
    /// </summary>
    public class TopologicalOrder 
    {
        #region Fields
        private IEnumerable<ConstructionTask> _order;
        private DirectedCycles dc;        
        #endregion

        public TopologicalOrder(DirectedGraph g)
        {           
            dc = new DirectedCycles(g);
            if (!dc.HasCycle())
            {
                DepthFirstOrder dfs = new DepthFirstOrder(g);
                _order = dfs.reversePost;
            }
        }

        #region Properties
        /// <summary>
        /// Return the topological order of the vertices
        /// </summary>
        public IEnumerable<ConstructionTask> Order { get => _order; }
        public IEnumerable<ConstructionTask> Cycles { get => dc.Cycles(); }
        #endregion

        #region Methods
        public bool IsDAG() => dc.Cycles() == null;
        #endregion

        #region Helper Methods
        /// <summary>
        /// Prints the cycle detected by topsort
        /// </summary>
        /// <returns></returns>
        public string PrintCycle()
        {
            if (!IsDAG())
            {
                string msg = "A cycle is detected in the your dataset\n";
                foreach (var item in dc.Cycles())
                {
                    msg += item.TaskID + "\n";
                }
                return msg;
            }
            return null;
        }
        #endregion
    }   
}
