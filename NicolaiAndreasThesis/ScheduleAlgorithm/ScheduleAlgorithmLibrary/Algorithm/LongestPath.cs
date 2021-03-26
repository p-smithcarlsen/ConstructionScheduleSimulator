using ScheduleAlgorithm.Domain.Entity;
using ScheduleAlgorithmLibrary.Algorithm.Components;
using System;
using System.Collections.Generic;
using System.Linq;

namespace ScheduleAlgorithmLibrary.Algorithm
{
    public class LongestPath
    {        
        private readonly DirectedGraph G;
        private readonly TopologicalOrder topOrder;    

        /// <summary>
        /// </summary>
        /// <param name="G"></param>
        /// <param name="Source">compute from a given source, if null then the whole grapg is computed </param>
        /// <param name="topOrder"></param>
        /// 
        public LongestPath(DirectedGraph G, TopologicalOrder topOrder, ConstructionTask Source = null)
        {         
            if (!topOrder.IsDAG())
                throw new ArgumentException("The Graph cannot be generated");               
            this.G = G;
            this.topOrder = topOrder;
            // compute longest path in the dag
            ComputeLongestPath(G,topOrder);
        }

        #region Properties   
        public GraphUtil.CycleDetectedException CycleDetectedException { get; set; }
        public IEnumerable<ConstructionTask> Order { get => this.topOrder.Order; }
        #endregion

        #region Methods         
        /// <summary>
        /// return CP     
        /// </summary>
        /// <returns>The critical path</returns>
        public IEnumerable<ConstructionTask> GetCriticalTasks()
        {          
            ComputeLatestStart();
            if (CycleDetectedException != null)
                return null;
            return topOrder.Order
                .Where(t => t.GetLS() == t.GetES() && // task with no slack
                            !t.Equals(GraphUtil.Source) && !t.Equals(GraphUtil.Sink)); // skip source & sink       
        }
        #endregion

        #region Helper Methods
        /// <summary>
        /// Compute longest path
         /// </summary>
        private void ComputeLongestPath(DirectedGraph G,TopologicalOrder topOrder)
        {           
            foreach (var t in topOrder.Order)
                UpdateEarliestFinish(G, t);      
        }

        /// <summary>
        /// Update ES in toporder, in proportional to |V| + E
        /// </summary>
        /// <param name="g"> digraph</param>
        /// <param name="t"> task to visit</param>
        private void UpdateEarliestFinish(DirectedGraph g, ConstructionTask t)
        {
            foreach (Edge edge in g.Graph[t])
                if (edge.To.GetES() < t.EF + edge.Weight)
                    edge.To.EF = t.EF + edge.Weight;
        }

        /// <summary>
        /// Compute LS 
        /// </summary>
        private void ComputeLatestStart()
        {                   
            // reverse the graph
            var g = G.Reverse();
            // toporder the reversed dag
            TopologicalOrder order = new TopologicalOrder(g);
            if (order.Order == null)
                CycleDetectedException = new GraphUtil.CycleDetectedException(order.PrintCycle());
            else
            {
                // set sink lf to makespan
                GraphUtil.Sink.LF = GraphUtil.Sink.EF;
                // update in toporder 
                foreach (var t in order.Order)
                    UpdateLatestStart(g, t);
            }          
        }                 

            /// <summary>
            /// Update lastest start
            /// </summary>
            /// <param name="g">digraph</param>
            /// <param name="t"></param>
        private void UpdateLatestStart(DirectedGraph g, ConstructionTask t)
        {                 
            foreach (Edge edge in g.Graph[t])                     
                if (edge.To.LF == double.PositiveInfinity || edge.To.GetLS() > t.GetLS() - edge.To.EstimatedDuration)             
                    edge.To.LF = t.LF - edge.Weight; // update LF                                                                          
        }
        #endregion
    }
   

}
