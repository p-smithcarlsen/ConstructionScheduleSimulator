using ScheduleAlgorithm.Domain.Entity;
using ScheduleAlgorithmLibrary.Algorithm.Components;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ScheduleAlgorithmLibrary.Algorithm
{
    /// <summary>
    /// Initiate a directed graph 
    /// <bref from="Algorithhms 4th Edition by Robert Sedgewick, Kevin Wayne, page 569"></bref>
    /// </summary>
    public class DirectedGraph 
    {
        #region Fields
        private Dictionary<ConstructionTask, List<Edge>> _digraph;        
        #endregion

        #region Constructors     
        /// <summary>
        /// Empty graph
        /// </summary>
        public DirectedGraph() 
        {
            _digraph = new Dictionary<ConstructionTask, List<Edge>>();
            //add source and sink
            AddTaskRange(new List<ConstructionTask>() { GraphUtil.Source, GraphUtil.Sink });
        }
        /// <summary>
        /// Construct a digraph with pre-defined activity list
        /// </summary>
        /// <param name="tasks"></param>
        public DirectedGraph(List<ConstructionTask> tasks)
        {
            if (tasks.Count < 1)
                throw new ArgumentException("No tasks in the list");      
            _digraph = new Dictionary<ConstructionTask, List<Edge>>();
            //add source and sink
            AddTaskRange(new List<ConstructionTask>() { GraphUtil.Source, GraphUtil.Sink });          
            for (int i = 0; i < tasks.Count; i++)
                _digraph.Add(tasks[i], new List<Edge>());
            ConstructPrecedence(tasks);
        }
        #endregion

        #region Properties     
        public int Count { get => _digraph.Count; }
        public Dictionary<ConstructionTask, List<Edge>> Graph { get => _digraph; }           
        #endregion 

        /// <summary>
        /// Construct precedence
        /// </summary>
        /// <param name="tasks"></param>
        private void ConstructPrecedence(List<ConstructionTask> tasks)
        {            
            if (_digraph is null || _digraph.Count < 1)
                throw new ArgumentException("No activities present");        
            for (int i = 0; i < tasks.Count; i++)
            {                
                // add source to vertices with no incoming edges
                if (string.IsNullOrEmpty(tasks[i].Precedence) ||string.IsNullOrWhiteSpace(tasks[i].Precedence) )                                    
                    AddPrecedence(GraphUtil.Source, tasks[i], tasks[i].EstimatedDuration);
                // add precedence
                for (int j = 0; j < tasks.Count; j++)
                {
                    if (tasks[j].Precedence is null) continue;
                    if (tasks[j].Precedence.Contains(tasks[i].TaskID))
                        AddPrecedence(tasks[i], tasks[j], tasks[j].EstimatedDuration);
                }                
                // add sink to vertices with no outgoing edges
                if (_digraph[tasks[i]].Count < 1)
                    AddPrecedence(tasks[i], GraphUtil.Sink,0);
            }
        }
        #region Methods
        public List<Edge> GetPrecedence(ConstructionTask task)
        {
            if (!_digraph.ContainsKey(task))
                throw new ArgumentException("The Task is not present in the schedule");
            return _digraph[task];            
        }

        public void AddPrecedence(ConstructionTask from, ConstructionTask to, double weight)
        {
            if (!_digraph.ContainsKey(from))
                throw new ArgumentException($"{from.TaskID} is not present in the schedule");
            else if (!_digraph.ContainsKey(to))
                throw new ArgumentException($"{to.TaskID} is not present in the schedule");
            else            
                _digraph[from].Add(new Edge { 
                    From = from, 
                    To = to, 
                    Weight = weight});                                 
        }

        /// <summary>
        /// Add node to the graph
        /// duplicated it is not allowed
        /// </summary>
        /// <param name="task"></param>
        public void AddTask(ConstructionTask task) 
        {
            if (!_digraph.Any(d => d.Key.TaskID.Equals(task.TaskID)))
                _digraph.Add(task, new List<Edge>());
            else return;
        }

        /// <summary>
        /// Add range
        /// </summary>
        /// <param name="tasks"></param>
        public void AddTaskRange(List<ConstructionTask> tasks) => tasks.ForEach(t => AddTask(t));
      

        /// <summary>
        /// reverse the graph
        /// </summary>
        /// <returns></returns>
        public DirectedGraph Reverse()
        {
            DirectedGraph reversed = new DirectedGraph();  
            // add the entries
            reversed.AddTaskRange(_digraph.Keys.ToList());     
            // reverse the graph 
            foreach (var task in _digraph)                        
                foreach (var p in task.Value)
                    reversed.AddPrecedence(p.To, p.From, p.To.EstimatedDuration);                                                                     
            return reversed;
        }       
        #endregion
       
    }

    /// <summary>
    /// Depth First search
    /// </summary>
    public interface IDepthFirstSearch
    {
        void DFS(DirectedGraph G, ConstructionTask Source);
    }

    /// <summary>
    /// Cycle detection
    /// <bref from="Algorithhms 4th Edition by Robert Sedgewick, Kevin Wayne, page 569"></bref>
    /// </summary>
    internal class DirectedCycles : IDepthFirstSearch
    {
        #region Fields
        private Dictionary<ConstructionTask, bool> _marked;
        private Stack<ConstructionTask> _cycles;
        private List<ConstructionTask> _edgeTo;
        private Dictionary<ConstructionTask, bool> _onStack;
        #endregion

        internal DirectedCycles(DirectedGraph g)
        {
            _onStack = g.Graph.Keys.ToDictionary(k => k, k => false);
            _edgeTo = new List<ConstructionTask>(g.Count);
            _marked = g.Graph.Keys.ToDictionary(k => k, k => false); 
            foreach (var kv in g.Graph)
                if (!_marked[kv.Key]) DFS(g, kv.Key);
            
        }
        #region Methods
        /// <summary>
        /// Depth-First-Search
        /// </summary>
        /// <param name="G"></param>
        /// <param name="Source"></param>
        public void DFS(DirectedGraph G, ConstructionTask Source)
        {
            _onStack[Source] = true;
            _marked[Source] = true;

            var precedence = G.GetPrecedence(Source);
            foreach (var p in precedence)
            {
                var successor = p.To;
                if (this.HasCycle()) return;
                else if (!_marked[successor])
                {
                    _edgeTo.Add(Source);
                    DFS(G, successor);
                }
                else if (_onStack[successor])
                {
                    _cycles = new Stack<ConstructionTask>();
                    ConstructionTask x = Source;
                    while (x != successor && x != null)
                    {
                        _cycles.Push(x);
                        x = _edgeTo.Find(t => t.Equals(x));
                    }                    
                    _cycles.Push(successor);
                    _cycles.Push(Source);
                }
            }

            _onStack[Source] = false;
            return;
        }
        public bool HasCycle() => _cycles != null;
        public IEnumerable<ConstructionTask> Cycles() => _cycles;
        #endregion
    }

    /// <summary>
    /// Order the vertices, the reversed-post order is used in the TopologicalOrder class
    /// <bref from="Algorithhms 4th Edition by Robert Sedgewick, Kevin Wayne, page 571"></bref>
    /// </summary>
    internal class DepthFirstOrder : IDepthFirstSearch
    {
        #region Fields
        private Dictionary<ConstructionTask, bool> _marked;       
        private Stack<ConstructionTask> _revpost;
        #endregion

        internal DepthFirstOrder(DirectedGraph g)
        {            
            _revpost = new Stack<ConstructionTask>();
            _marked = g.Graph.Keys.ToDictionary(k => k, k => false);
            foreach (var kv in g.Graph)
                if (!_marked[kv.Key]) DFS(g, kv.Key);
        }

        public void DFS(DirectedGraph G, ConstructionTask Source)
        {           
            _marked[Source] = true;
            var precedence = G.GetPrecedence(Source);
            foreach (var p in precedence)
                if (!_marked[p.To]) DFS(G, p.To);           
            _revpost.Push(Source);
        }

        #region Properties
        /// <summary>
        /// Get the reversed post order
        /// </summary>
        public Stack<ConstructionTask> reversePost { get => _revpost; }
        #endregion
    }

}
