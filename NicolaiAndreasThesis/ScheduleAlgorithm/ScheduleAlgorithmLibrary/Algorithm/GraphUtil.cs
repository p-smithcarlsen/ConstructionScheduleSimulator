using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ScheduleAlgorithm.Domain.Entity;
using ScheduleAlgorithmLibrary.Algorithm;
using ScheduleAlgorithmLibrary.Utilities;
using ScheduleAlgorithmLibrary.Algorithm.Components;

namespace ScheduleAlgorithmLibrary.Algorithm
{
    public class GraphUtil
    {                        
        #region Graph Utils   
        /// <summary>
        /// Source and Sink Nodes
        /// </summary>
        public static readonly ConstructionTask Source = new ConstructionTask
        {
            TaskID = "0",            
        };
        public static readonly ConstructionTask Sink = new ConstructionTask
        {
            TaskID = "-1",             
        };
        #endregion

        #region Methods
        /// <summary>
        /// distinct by key
        /// </summary>
        /// <typeparam name="TSource"></typeparam>
        /// <typeparam name="TKey"></typeparam>
        /// <param name="source"></param>
        /// <param name="keySelector"></param>
        /// <returns></returns>
        public static IEnumerable<TSource> DistinctBy<TSource, TKey>(IEnumerable<TSource> source, Func<TSource, TKey> keySelector)
        {
            HashSet<TKey> seenKeys = new HashSet<TKey>();            
            return source
                .Where(element => seenKeys.Add(keySelector(element)))
                .Select(element => element);
        }

        /// <summary>        
        /// </summary>
        /// <param name="tasks"></param>
        /// <returns>a list task above average</returns>
        public static IEnumerable<ConstructionTask> GetTasksAboveAverage(IEnumerable<ConstructionTask> tasks)
        {            
            var avg = tasks.Average(CT => CT.EstimatedDuration);
            return tasks.Where(CT => CT.EstimatedDuration > avg);
        }

        /// <summary>
        /// </summary>
        /// <param name="tasks"></param>
        /// <returns>return conflicting tasks that impact the makespan</returns>
        public static HashSet<ConstructionTask> GetConflictingTasks(IEnumerable<ConstructionTask> tasks)
        {
            HashSet<ConstructionTask> cpTasks = new HashSet<ConstructionTask>();


            var zones = tasks.GroupBy(x => x.Zone).ToDictionary(x => x.Key, x => x.ToList());

            // store orignal makespan
            var makespan = tasks.Max(t => t.EF);

            foreach (var zone in zones.Values)
            {
                HashSet<ConstructionTask> conflictingTasks = new HashSet<ConstructionTask>();
                for (int i = 0; i < zone.Count - 1; i++)
                {
                    for (int j = i + 1; j < zone.Count; j++)
                    {
                        // check the tasks against each other
                        var task1 = zone[j];
                        var task2 = zone[i];

                        var isInConflict1 = task1.GetES() <= task2.GetES() && task1.EF >= task2.EF;
                        var isInConflict2 = task2.GetES() <= task1.GetES() && task2.EF >= task1.EF;
                        var isInConflict3 = task1.EF > task2.GetES() && task1.GetES() < task2.GetES();

                        if (isInConflict1 || isInConflict2 || isInConflict3)
                        {
                            conflictingTasks.Add(task1);
                            conflictingTasks.Add(task2);
                        }

                    }
                }

                #region Check location
                if (conflictingTasks.Count > 0)
                {
                    // take the insertion point and store the EF value of that task
                    double conflictstart = conflictingTasks.Min(t => t.GetES());
                    // sum the the rest of the location 
                    double costOfInsertion = zone.Where(t => t.GetES() >= conflictstart).Sum(t => t.EstimatedDuration);
                    // store the cost of the conflict
                    double CostOfConflict = conflictstart + costOfInsertion;
                    // only add the cp tasks if cost increases the makespan
                    if (CostOfConflict > makespan)
                    {
                        cpTasks.UnionWith(conflictingTasks);
                    }
                }
                #endregion
            }
            return cpTasks;
        }

        /// <summary>
        /// </summary>
        /// <param name="constructionTasks"></param>
        /// <returns>´returns a list containing the tasks with the longest duration</returns>
        public static IEnumerable<ConstructionTask> GetSlowestTasks(IEnumerable<ConstructionTask> constructionTasks)
        {
            var highestDuration = constructionTasks.Max(CT => CT.EstimatedDuration);
            return constructionTasks.Where(CT => CT.EstimatedDuration == highestDuration);
        }

        #endregion

        #region Custom Exception for DAG
        /// <summary>
        /// Custom Exception for graph argument.
        /// Will be refactored later on
        /// </summary>
        [Serializable]
        public class CycleDetectedException : Exception
        {
            public CycleDetectedException(string message) : base(message)
            {                
            }
        }
        #endregion
    }

}

