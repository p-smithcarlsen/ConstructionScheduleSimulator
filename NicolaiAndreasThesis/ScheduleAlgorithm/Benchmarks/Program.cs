using CsvHelper;
using ScheduleAlgorithm.Domain.Entity;
using ScheduleAlgorithmLibrary.Algorithm;
using ScheduleAlgorithmLibrary.Utilities;
using System;
using SCG = System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using C5;
using System.Collections.Generic;

namespace Benchmarks
{
    public class Program
    {        
        static void Main(string[] args)
        {

            //var parser = new ScheduleAlgorithmLibrary.Utilities.CsvParser();
            //var tasks = parser.LoadCsvFile();

            //#region DAG old
            ////DirectedAcyclicGraph dg = new DirectedAcyclicGraph(tasks);           

            //// test purpose
            ////Queue<string> priority = new Queue<string>();
            ////priority.Enqueue("z4");
            ////priority.Enqueue("z2");
            ////priority.Enqueue("z1");
            ////priority.Enqueue("z3");
            ////priority.Enqueue("z5");
            //#endregion

            //#region New DAG
            //var digraph = new DirectedGraph(tasks);
            ////digraph.ConstructPrecedence();
            ////digraph.Reverse();
            ////var t1 = new ConstructionTask { TaskID = "6.0", Precedence = "", EstimatedDuration = 2, Progress = ProgressState.Pending };
            ////var t2 = new ConstructionTask { TaskID = "6.1", Precedence = "", EstimatedDuration = 2, Progress = ProgressState.Pending };
            ////digraph.AddTask(t1);
            ////digraph.AddTask(t2);
            ////digraph.AddPrecedence(t1, t2);
            ////var task = digraph.Graph.Keys.First(f => f.TaskID == "1.0");
            ////digraph.AddPrecedence(task, t1);
            ////digraph.AddPrecedence(t2, t1);
            //#endregion
            ////digraph.ConstructPrecedence();
            //var dag =new TopologicalOrder(digraph);

            //var start = digraph.Graph.Keys.First(y => y.TaskID.Equals("1.0"));

            //if (dag.Order is null)
            //{
            //    dag.Cycles.ToList().ForEach(q => Console.WriteLine(q.TaskID));
            //}     
            
            ////var a = dag.GetEndDate(start, digraph.Graph.Keys.First(y => y.TaskID.Equals("4.9")));
            //var lp = new LongestPath(digraph, dag,null);
            //var i = lp.GetCriticalTasks();
            ////lp.ComputeLatestStart(start);
            

            ////var test = digraph.Reverse();

            ////var time = _DAG.GetLongestPath();
            ////_DAG.PrioritizeTask(priority);

             var t = 0;


            var parser = new ScheduleAlgorithmLibrary.Utilities.CsvParser();
            var tasks = parser.LoadCsvFile();

            var digraph = new DirectedGraph(tasks);


            int dataset = 2000;
            int counter = 11;
            double[] runningtimes = new double[counter];

            for (int i = 0; i < counter; i++)
            {
                runningtimes[i] = BenchmarkMethod(() => new TopologicalOrder(digraph));
                parser.ClearContent();
            }

            var total = runningtimes.Sum();

            // removed the first element
            var newArr = runningtimes.Skip(1).ToArray();
            var avg = total / newArr.Length;


            string filename = $"dataset{dataset}.csv";
            string path =
                $@"C:\Users\DressesPc\Documents\Development\Thesis\schedulingalgorithm\ScheduleAlgorithm\Benchmarks\Results\{filename}";

            using (var writer = new StreamWriter(path))
            using (var csv = new CsvWriter(writer, CultureInfo.InvariantCulture))
            {
                csv.Configuration.Delimiter = ";";
                csv.Configuration.HasHeaderRecord = false;
                csv.WriteRecords(newArr);
            }


        }

        public static double BenchmarkMethod(Action method)
        {
            Stopwatch sw = new Stopwatch();
            sw.Start();
            method();
            sw.Stop();
            return sw.Elapsed.TotalSeconds;
        }


        //public static Queue<System.Collections.Generic.KeyValuePair<string, IEnumerable<ConstructionTask>>> OrderByPriority(System.Collections.Generic.ICollection<ConstructionTask> entity, string[] priority)
        //{               
        //    var queue = new Queue<System.Collections.Generic.KeyValuePair<string, IEnumerable<ConstructionTask>>>();

        //    for (int i = 0; i < priority.Count(); i++)
        //    {
        //        var key = priority[i];
        //        var value = entity.Where(t => t.Zone.Equals(key)).Select(t => t);
        //        queue.Enqueue(new System.Collections.Generic.KeyValuePair<string, IEnumerable<ConstructionTask>>(key, value));
        //    }                 
        //    return queue;
        //}
        
    
    }
}
