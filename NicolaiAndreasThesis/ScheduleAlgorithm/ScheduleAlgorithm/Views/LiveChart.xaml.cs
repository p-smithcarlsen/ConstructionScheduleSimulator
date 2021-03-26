using System;
using System.Collections.Generic;
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
using LiveCharts;
using LiveCharts.Defaults;
using LiveCharts.Uwp;
using ScheduleAlgorithmLibrary.Algorithm;
using ScheduleAlgorithmLibrary.Utilities;

// The Blank Page item template is documented at https://go.microsoft.com/fwlink/?LinkId=234238

namespace ScheduleAlgorithm.Views
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class LiveChart : Page
    {

        public LiveChart()
        {
            this.InitializeComponent();
        }


        //public void LoadChartContentsSerial()
        //{
        //    var data = GraphUtil.GetSchedulePointsList();

        //    //ConstraintModel constraintModel = new ConstraintModel();
        //    //var data = constraintModel.createCraftSchedule(taskList);

        //    double currentDay = 0;
        //    var SeriesCollection = new SeriesCollection();

        //    foreach (var ctTuple in data)
        //    {
        //        ChartValues<ObservablePoint> dataPoints = new ChartValues<ObservablePoint>();

        //        var constructionTask = ctTuple.Item1;
        //        var acc = ctTuple.Item2;

        //        var startTime = acc - constructionTask.Duration;
        //        var endTime = acc;

        //        var zoneEnd = double.Parse(ctTuple.Item1.Zone.Substring(1));
        //        var zoneStart = zoneEnd - 1;

        //        dataPoints.Add(new ObservablePoint
        //        {
        //            X = startTime,
        //            Y = zoneStart
        //        });

        //        dataPoints.Add(new ObservablePoint
        //        {
        //            X = endTime,
        //            Y = zoneEnd
        //        });

        //        var craftSeries = new LineSeries
        //        {
        //            Values = dataPoints,
        //            LineSmoothness = 0,
        //            Fill = new SolidColorBrush(Windows.UI.Colors.Transparent),
        //        };

        //        SeriesCollection.Add(craftSeries);
        //    }

        //    LCPlanningChart.AxisY.Clear();
        //    LCPlanningChart.AxisY.Add(new Axis
        //    {
        //        MinValue = 0,
        //        MaxValue = 5,
        //        Separator = new Separator { Step = 1 },
        //        IsMerged = true
        //    });


        //    LCPlanningChart.Series.AddRange(SeriesCollection);
        //}




        //private void Page_Loaded(object sender, RoutedEventArgs e)
        //{
        //    LoadChartContentsSerial();
        //}
    }
}
