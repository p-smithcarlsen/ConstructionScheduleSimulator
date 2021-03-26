using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using ScheduleAlgorithmLibrary.Algorithm;
using ScheduleAlgorithmLibrary.Utilities;
using WinRTXamlToolkit.Controls.DataVisualization.Charting;
using WinRTXamlToolkit.Tools;

// The Blank Page item template is documented at https://go.microsoft.com/fwlink/?LinkId=234238

namespace ScheduleAlgorithm.Views
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class FlowLine : Page
    {

        private DirectedAcyclicGraph DAG;
        private ConstructionTask startTast;
        private ConstructionTask endTask;

        private class FlowlineTask
        {
            public double Zone { get; set; }
            public double Day { get; set; }
        }

        public FlowLine()
        {
            this.InitializeComponent();
        }

        public void LoadChartContents()
        {
            FileParser fileParser = new FileParser();
            var taskList = fileParser.ReadCsvFile();

            ScheduleAlgorithmLibrary.Algorithm.DirectedAcyclicGraph.DirectedGraph dg = new DirectedAcyclicGraph.DirectedGraph(taskList);
            // this approach should be a dynamic option from the user
            ConstructionTask start = taskList.Find(t => t.TaskId.Equals("1.0"));
            // test start job
            if (start != null)
            {
                dg.StartTask = start;
                this.startTast = start;
            }     
                

            this.DAG = new DirectedAcyclicGraph();         

            var shortest = GraphUtil.ComputeDAG(dg.DAG, dg.StartTask, taskList, true);

            this.endTask = GraphUtil.Precedence[GraphUtil.Precedence.Length - 1];

            var data = GraphUtil.GetSchedule();

            //ConstraintModel constraintModel = new ConstraintModel();
            //var data = constraintModel.createCraftSchedule(taskList);

            double currentDay = 0;

            foreach (var key in data.Keys)
            {

                List<FlowlineTask> flowlineTasks =  new List<FlowlineTask>();

                //var test = data[key].GroupBy(a => a.Zone).ToDictionary(b => b.Key,c => c);

                foreach (var constructionTask in data[key])
                {
                    var zoneEnd = double.Parse(constructionTask.Zone.Substring(1));

                    var zoneStart = zoneEnd - 1;

                    flowlineTasks.Add(new FlowlineTask()
                    {
                        Day = currentDay + 0.01,
                        Zone = zoneStart
                    });

                    currentDay += constructionTask.Duration.Required;

                    flowlineTasks.Add(new FlowlineTask
                    {
                        Day = currentDay,
                        Zone = zoneEnd
                    });

                }
                
                SeriesDefinition taskSeriesDefinition = new SeriesDefinition();
                taskSeriesDefinition.DependentValuePath = "Zone";
                taskSeriesDefinition.IndependentValuePath = "Day";
                taskSeriesDefinition.Title = key;
                taskSeriesDefinition.ItemsSource = flowlineTasks;

                (FlowlineChart.Series[0] as StackedLineSeries).SeriesDefinitions.Add(taskSeriesDefinition);
                
            }
            // set CPM
            CPM.Text = GetCPMasString() + " total cost: "+shortest[endTask];
        }

        private void Page_Loaded(object sender, RoutedEventArgs e)
        {
            LoadChartContents();
        }

        private string GetCPMasString()
        {
            List<ConstructionTask> CPM = GraphUtil.GetPath(endTask);
            CPM.Reverse();
            string CMPResult = "";            
            foreach (ConstructionTask task in CPM)
            {
                if (task.Equals(endTask))                
                    CMPResult += $"[{task.TaskId}]";
                else
                    CMPResult += $"[{task.TaskId}] --> ";
            }
            return CMPResult;

        }
    }

    
}
