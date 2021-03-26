using LiveCharts;
using LiveCharts.Defaults;
using LiveCharts.Wpf;
using ScheduleAlgorithm.Domain.Entity;
using ScheduleAlgorithmLibrary.Algorithm;
using ScheduleAlgorithmLibrary.Utilities;
using ScheduleAlgorithmWPF.Controls;
using ScheduleAlgorithmWPF.Data;
using ScheduleAlgorithmWPF.State.Navigators;
using ScheduleAlgorithmWPF.ViewModels.Base;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace ScheduleAlgorithmWPF.ViewModels
{
    public abstract class ScheduleViewModel : ViewModelBase
    {
        #region Fields
        private ObservableCollection<ConstructionTask> _items;
        private bool _isGraphPresent = false;
        private double _deadline;
  
        // new content
        private DirectedGraph _graph;
        private TopologicalOrder _order;
        private LongestPath _lp;
       
        #endregion

        #region Properties
        /// <summary>
        /// Type of view
        /// </summary>
        public abstract ViewType Type { get; }       
        
        /// <summary>
        /// Store Critical Path
        /// </summary>
        public IEnumerable<ConstructionTask> CPTechnical { get; set; }
        public IEnumerable<ConstructionTask> CPContinuity { get; set; }
        public IEnumerable<ConstructionTask> CPLocation { get; set; }
        public IEnumerable<ConstructionTask> CPProduction { get; set; }

        public DirectedGraph Graph { get => _graph; set => _graph = value; }
        public ObservableCollection<ConstructionTask> Items
        {
            get
            {
                return _items;
            }
            set
            {
                _items = value;
                OnGraphChanged();
            }
        }
        public event Action GraphChanged;

        /// <summary>
        /// Exception message for the user 
        /// </summary>
        public string ExceptionMessage { get; set; } = "";
        #endregion

        #region LiveChart Properties
        /// <summary>
        /// Chart
        /// </summary>     
        public CartesianChart Chart { get; set; }              
        public static ToggleButtons TbTechnical { get; set; }
        public static ToggleButtons TbContinuity { get; set; }
        public static ToggleButtons TbLocation { get; set; }
        public static ToggleButtons TbProduction { get; set; }
        public static WrapPanel WpCraftsPanel { get; set; }

        /// <summary>
        /// Deadline by client
        /// </summary>
        public double Deadline
        {
            get
            {
                return _deadline;
            }
            set
            {
                if (value == _deadline)
                    return;
                _deadline = value;
                SetDeadLine(_deadline); 
            }
        }
        public List<LineSeries> dLine { get; set; }
        public SeriesCollection Series { get; set; }       
     
        public TaskToolTipControl ToolTip { get; set; }
      

        #endregion

        #region Methods       
       
        /// <summary>
        /// Load content fro all views
        /// </summary>
        /// <param name="type"></param>
        public CartesianChart LoadContent(ViewType type)
        {             
            try
            {                
                if (Graph == null)
                {
                    _isGraphPresent = InitSchedule();
                }

                if (_isGraphPresent == false)
                {
                    return Chart;
                }

                #region Chart Content
               

                Chart = new CartesianChart();             
                
                ChartLegend Legend = new ChartLegend();
                // remove chart animation
                Chart.DisableAnimations = true;         
                              
                #region Set up legend               
                // get craft types
                var crafts = Items.Select(x => x.Craft).Distinct();

                foreach (var craft in crafts)
                {
                    var brush = ViewModelUtils.GetRandomBrush();
                    // check for null pointers
                    if (craft == null)
                    {                       
                        // if craft is not defined, Unknowned is added as the text
                        string unknown = "Unknown";
                        if (!ViewModelUtils.LegendColors.Keys.Contains(unknown))
                            ViewModelUtils.LegendColors.Add(unknown, brush);                                                 
                    }
                    else if (!ViewModelUtils.LegendColors.Keys.Contains(craft))                                          
                        ViewModelUtils.LegendColors.Add(craft, brush);                    
                }

                // set capacity for brush if cp is turned on
                if (TbTechnical.Toggled || TbLocation.Toggled || TbContinuity.Toggled || TbProduction.Toggled)
                {
                    foreach (var legendColor in ViewModelUtils.LegendColors.Values)
                    {
                        legendColor.Opacity = 0.2;
                    }
                }

                // set legend for each craft
                Legend.Series.AddRange(
                    GraphUtil.DistinctBy(Items, t => t.Craft).Where(t => t.Craft != null)
                        .Select(t => new SeriesViewModel()
                        {
                            Title = t.Craft,
                            Stroke = ViewModelUtils.GetColorByCraft(t.Craft)
                        }));
                // add unknown to legend
                if(Items.Any(t => t.Craft == null))
                {
                    Legend.Series.Add(new SeriesViewModel
                    {
                        Title = "Unknown",
                        Stroke = ViewModelUtils.GetColorByCraft("Unknown")
                    });
                }               

                // critical brush
                if (TbTechnical.Toggled || TbLocation.Toggled || TbContinuity.Toggled || TbProduction.Toggled)
                {
                    Legend.Series.Add(new SeriesViewModel
                    {
                        Title = "Critical Tasks",
                        Stroke = ViewModelUtils.CRITICAL_BRUSH
                    });
                }

                // set up buttons
                ButtonGeneration();

                #endregion

                //LiveChart content
                Series = new SeriesCollection();

                foreach (ConstructionTask current in _order.Order.Where(t => !t.Equals(GraphUtil.Source) && !t.Equals(GraphUtil.Sink)))
                {
                    ChartValues<ObservablePoint> dataPoints;
                    // data points by viewmodel type
                    switch (type)
                    {
                        case ViewType.LatestStart:
                            dataPoints = GetDataPoints(current.Zone, current.GetLS(), current.LF); break;
                        default:
                            dataPoints = GetDataPoints(current.Zone, current.GetES(), current.EF); break;
                    }
                    var craftSeries = new LineSeries
                    {
                        Values = dataPoints,
                        LineSmoothness = 0,
                        Fill = Brushes.Transparent,
                        PointGeometry = null,
                        Title = current.Craft,
                        LabelPoint = chartPoint => $"TaskID: {current.TaskID}"
                    };

                    Brush standardBush;
                    // check for null pointers in crafts
                    if (string.IsNullOrEmpty(current.Craft))
                        standardBush = ViewModelUtils.GetColorByCraft("Unknown");
                    else standardBush = ViewModelUtils.GetColorByCraft(current.Craft);

                    #region CP Combinations
                    // if cp is turned on
                    if (TbTechnical.Toggled)
                    {
                        if (CPTechnical.Contains(current))
                        {
                            craftSeries.Stroke = ViewModelUtils.CRITICAL_BRUSH;
                        }
                        else
                        {
                            craftSeries.Stroke = standardBush;
                        }
                    }
                    else
                    {
                        if (TbProduction.Toggled || TbContinuity.Toggled || TbLocation.Toggled)
                        {
                            if (CPTechnical.Contains(current))
                            {
                                craftSeries.Stroke = ViewModelUtils.LOW_PRIORITY_CRITICAL_BRUSH;
                            }
                            else
                            {
                                craftSeries.Stroke = standardBush;
                            }
                        }
                        if (TbLocation.Toggled && !TbContinuity.Toggled && !TbProduction.Toggled)
                        {
                            if (CPLocation.Contains(current))
                            {
                                craftSeries.Stroke = ViewModelUtils.CRITICAL_BRUSH;
                            }
                        }
                        else if (TbContinuity.Toggled && !TbLocation.Toggled && !TbProduction.Toggled)
                        {
                            if (CPContinuity.Contains(current))
                            {
                                craftSeries.Stroke = ViewModelUtils.CRITICAL_BRUSH;
                            }
                        }
                        else if (TbProduction.Toggled && !TbLocation.Toggled && !TbContinuity.Toggled)
                        {
                            if (CPProduction.Contains(current))
                            {
                                craftSeries.Stroke = ViewModelUtils.CRITICAL_BRUSH;
                            }
                        }
                        else if (TbLocation.Toggled && TbContinuity.Toggled && !TbProduction.Toggled)
                        {
                            if (CPLocation.Contains(current) || CPContinuity.Contains(current))
                            {
                                craftSeries.Stroke = ViewModelUtils.CRITICAL_BRUSH;
                            }
                        }
                        else if (TbLocation.Toggled && TbProduction.Toggled && !TbContinuity.Toggled)
                        {
                            if (CPLocation.Contains(current) || CPProduction.Contains(current))
                            {
                                craftSeries.Stroke = ViewModelUtils.CRITICAL_BRUSH;
                            }
                        }
                        else if (TbProduction.Toggled && TbContinuity.Toggled && !TbLocation.Toggled)
                        {
                            if (CPProduction.Contains(current) || CPContinuity.Contains(current))
                            {
                                craftSeries.Stroke = ViewModelUtils.CRITICAL_BRUSH;
                            }
                        }
                        else if (TbLocation.Toggled && TbContinuity.Toggled && TbProduction.Toggled)
                        {
                            if (CPLocation.Contains(current) || CPContinuity.Contains(current) || CPProduction.Contains(current))
                            {
                                craftSeries.Stroke = ViewModelUtils.CRITICAL_BRUSH;
                            }
                        }
                        else
                        {
                            standardBush.Opacity = 1.0;
                            craftSeries.Stroke = standardBush;
                        }                  
                    }
                    #endregion

                    Series.Add(craftSeries);
                }
                // x-axis
                Chart.AxisX.Add(new Axis()
                {
                    MinValue=0,
                    FontSize = 12,
                    MaxValue =Graph.Graph.Keys.Max(t => t.EF),
                    Title = "Time in days",
                    Foreground = new SolidColorBrush(Colors.Black),
                    Separator = new LiveCharts.Wpf.Separator() { Step = 1 }
                });
                // y-axis
                Chart.AxisY.Add(new Axis
                {
                    MinValue = 0,
                    MaxValue = _items.Max(x => x.Zone),
                    Separator = new LiveCharts.Wpf.Separator { Step = 1 },
                    IsMerged = true,
                    FontSize = 20,
                    Title = "Location",
                    Foreground = new SolidColorBrush(Colors.Black)
                });
                // add line series
                Chart.Series.AddRange(Series);          
             
                // chart setups
                Chart.Hoverable = false;
                Chart.ToolTip = null;
                Chart.ChartLegend = Legend;
                Chart.LegendLocation = LegendLocation.Right;               
                
                #endregion

                return Chart;

            }
            catch (Exception e)
            {
                ViewModelUtils.ResponseUserError(/*"No graph is present"*/e.StackTrace);
                return null;
            }
        }

        /// <summary>
        /// Initialize the Schedule
        /// </summary>
        /// <returns></returns>
        public bool InitSchedule()
        {
            //Stopwatch sw = new Stopwatch();
            try
            {
                MsSqlDataService sql = new MsSqlDataService();
                Items = new ObservableCollection<ConstructionTask>(sql.LoadConstructionTasks());
                if (_items is null || _items.Count < 1)
                {
                    var noFileMessageBox = MessageBox.Show("No file has been selected. Would you like to use a preview file?",
                        "No file selected", MessageBoxButton.YesNo);

                    if (noFileMessageBox == MessageBoxResult.Yes)
                    {
                        CsvParser parser = new CsvParser();
                        _items = new ObservableCollection<ConstructionTask>(parser.LoadCsvFile());
                    }

                    if (noFileMessageBox == MessageBoxResult.No)
                    {
                        return false;
                    }

                }
                // init graph
                Graph = new DirectedGraph(Items.OrderBy(x => x.TaskID).ToList());

                _order = new TopologicalOrder(Graph);
                if (!_order.IsDAG())
                {
                    ViewModelUtils.ResponseUserError(_order.PrintCycle());
                    return false;
                }
                else
                {
                    _lp = new LongestPath(Graph, _order, null);


                    CPTechnical = _lp.GetCriticalTasks();

                    if (CPTechnical == null)
                    {
                        ViewModelUtils.ResponseUserError(_lp.CycleDetectedException.Message);
                        return false;
                    }
                    CPContinuity = GraphUtil.GetTasksAboveAverage(CPTechnical);

                    CPProduction = GraphUtil.GetSlowestTasks(CPTechnical);

                    CPLocation = GraphUtil.GetConflictingTasks(_items);

                    return true;
                }

            }
            catch (Exception e)
            {
                // return the error message to the user
                ExceptionMessage += e.StackTrace + "\n";
                return false;
            }
        }

        /// <summary>
        /// invokes a new graph
        /// </summary>
        public void OnGraphChanged()
        {
            if (GraphChanged != null)
            {            
                Graph = new DirectedGraph(Items.ToList());
                GraphChanged.Invoke();
            }
        }

        #endregion

        #region Helper Methods     
        /// <summary>
        /// Set craft button dynamically
        /// </summary>
        private void ButtonGeneration()
        {
            WpCraftsPanel.Children.Clear();

            foreach (var legend in ViewModelUtils.LegendColors.Keys)
            {
                RadioButton button = new RadioButton()
                {
                    Content = legend,
                    Style = (Style)Application.Current.Resources["NavButton"]
                };


                button.Click += (s, e) =>
                {
                    foreach (var legendColor in ViewModelUtils.LegendColors.Values)
                    {
                        legendColor.Opacity = 0.2;
                    }

                    ViewModelUtils.LegendColors[legend].Opacity = 1.0;
                };

                WpCraftsPanel.Children.Add(button);
            }
        }

        /// <summary>
        /// Displays the deadline
        /// </summary>
        /// <param name="makespan"></param>
        private void SetDeadLine(double makespan)
        {

            if (Items == null || Items.Count < 1)
                return;
            #region clear data
            if (dLine != null)
            {
                foreach (var item in dLine)
                {
                    Chart.Series.Remove(item);
                }
                //Chart.Series.Remove(dLine);
                Chart.VisualElements.Clear();
            }
            #endregion
            
            dLine = new List<LineSeries>();

            #region Deadline
            ChartValues<ObservablePoint> dataDeadline = new ChartValues<ObservablePoint>();

            var yMax = Items.Max(t => t.Zone);
            // add startpoint
            dataDeadline.Add(new ObservablePoint
            {
                X = makespan,
                Y = 0
            });
            // end point
            dataDeadline.Add(new ObservablePoint
            {
                X = makespan,
                Y = yMax
            });

            ///Create text label for deadline            
            CreateVisualElements(makespan, yMax * 0.90, "Deadline", 16, FontWeights.Bold, .6, HorizontalAlignment.Center, VerticalAlignment.Bottom, -90);

            // create vertical deadline
            dLine.Add(new VerticalLineSeries()
            {
                Values = dataDeadline,
                Fill = Brushes.Transparent,
                Stroke = Brushes.Red,
                StrokeThickness = 2,
                StrokeDashArray = new DoubleCollection(new[] { 3d }),
                PointGeometry = null,
            });
            #endregion

            // measurement lines to makespan
            var delayedDict = new Dictionary<int, double>();

            // horizontal line offset
            double yOffset = -0.1;

            // if values are above deadline
            foreach (var item in Items)
            {
                double finish;
                switch (Type)
                {
                    case ViewType.LatestStart: finish = item.LF; break;
                    default: finish = item.EF; break;
                }
             

                if (finish > Deadline)
                {
                    // static offset
                   
                    if (!delayedDict.ContainsKey(item.Zone))
                        delayedDict.Add(item.Zone, item.Zone + yOffset);
                    else
                    {
                        // start over with the offset
                        if (delayedDict[item.Zone] >= item.Zone + Math.Round(.8, 1))
                            delayedDict[item.Zone] = item.Zone + yOffset;
                        else
                            delayedDict[item.Zone] += yOffset;
                    }

                    #region chart values of delayed tasks
                    // delayed time
                    var delayedPoints = new ChartValues<ObservablePoint>();

                    // end= Ef
                    delayedPoints.Add(new ObservablePoint
                    {
                        X = finish,
                        Y = delayedDict[item.Zone]
                    });
                    // start from deadline
                    delayedPoints.Add(new ObservablePoint
                    {
                        X = makespan,
                        Y = delayedDict[item.Zone]
                    });

                    // crossing lines
                    var crossPoints = new ChartValues<ObservablePoint>();
                    // end point
                    crossPoints.Add(new ObservablePoint
                    {
                        X = finish,
                        Y = delayedDict[item.Zone] - 0.015
                    });
                    // start point
                    crossPoints.Add(new ObservablePoint
                    {
                        X = finish,
                        Y = item.Zone - 0.015
                    });
                   
                    #endregion

                    #region lines
                    // cross
                    dLine.Add(new LineSeries
                    {
                        Values = crossPoints,
                        LineSmoothness = 0,
                        Fill = Brushes.Transparent,
                        Stroke = Brushes.Red,
                        StrokeThickness = 1,
                        PointGeometry = null,
                        ToolTip = null,
                        Opacity = 0.5
                    });
                    
                    // delays
                    dLine.Add(new LineSeries
                    {
                        Values = delayedPoints,
                        Fill = Brushes.Transparent,
                        Stroke = Brushes.Red,
                        StrokeThickness = 1,
                        PointGeometry = null,
                        ToolTip = null,
                        Opacity = 0.5

                    });
                    #endregion

                    var xoffset = finish - item.EstimatedDuration / 2;
                    CreateVisualElements(
                        xoffset, //x offset
                        delayedDict[item.Zone], //y offset
                        $"{finish - makespan} days",//label 
                        10,
                        FontWeights.Normal,
                        1,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Top
                        );               
                
                }
            }

            #region Set Time to deadline
            var maxTask = Graph.Graph.Keys
                .Where(t => t.EF == Graph.Graph.Keys
                    .Max(ts => ts.EF) && !t.Equals(GraphUtil.Sink))
                .Select(t => t)
                .First();

            if (Deadline > maxTask.EF)
            {
                var excessTimePoints = new ChartValues<ObservablePoint>();
                // end= Ef
                excessTimePoints.Add(new ObservablePoint
                {
                    X = Deadline,
                    Y = yOffset + maxTask.Zone
                });
                // start from deadline
                excessTimePoints.Add(new ObservablePoint
                {
                    X = maxTask.EF,
                    Y = yOffset + maxTask.Zone
                });

                var crossPoints = new ChartValues<ObservablePoint>();

                crossPoints.Add(new ObservablePoint
                {
                    X = maxTask.EF,
                    Y = yOffset + maxTask.Zone - 0.015
                });
                // start point
                crossPoints.Add(new ObservablePoint
                {
                    X = maxTask.EF,
                    Y = maxTask.Zone - 0.015
                });

                // delays
                dLine.Add(new LineSeries
                {
                    Values = excessTimePoints,
                    Fill = Brushes.Transparent,
                    Stroke = Brushes.Green,
                    StrokeThickness = 1,
                    PointGeometry = null,
                    ToolTip = null,
                    Opacity = 0.75
                });
                // cross
                dLine.Add(new LineSeries
                {
                    Values = crossPoints,
                    LineSmoothness = 0,
                    Fill = Brushes.Transparent,
                    Stroke = Brushes.Green,
                    StrokeThickness = 1,
                    PointGeometry = null,
                    ToolTip = null,
                    Opacity = 0.75
                });

                var xoffset = ((Deadline - maxTask.EF) / 2) + maxTask.EF;

                CreateVisualElements(
                    xoffset, //x offset
                    maxTask.Zone + yOffset, //y offset
                    $"{Deadline - maxTask.EF} days",//label 
                    10,
                    FontWeights.Normal,
                    1,
                    HorizontalAlignment.Center,
                    VerticalAlignment.Top
                    );
            }
            #endregion

            Chart.AxisX.First().MaxValue = Math.Max(Graph.Graph.Keys.Max(t => t.EF), Deadline);
            Chart.Series.AddRange(dLine);
        }

        /// <summary>
        /// Creates visual text elements
        /// </summary>
        /// <param name="xOffset"></param>
        /// <param name="yOffset"></param>
        /// <param name="label"></param>
        /// <param name="fontSize"></param>
        /// <param name="fontWeights"></param>
        /// <param name="opacity"></param>
        /// <param name="horizontal"></param>
        /// <param name="vertical"></param>
        /// <param name="rotationAngle"></param>
        private void CreateVisualElements(double xOffset, double yOffset, string label, double fontSize, FontWeight fontWeights, double opacity, HorizontalAlignment horizontal, VerticalAlignment vertical, int rotationAngle = 0)
        {

            Chart.VisualElements.Add(new VisualElement
            {
                X = xOffset,
                Y = yOffset,
                HorizontalAlignment = horizontal,
                VerticalAlignment = vertical,
                UIElement = new TextBlock
                {
                    Text = label,
                    FontWeight = fontWeights,
                    FontSize = fontSize,
                    Opacity = opacity,
                    RenderTransform = new RotateTransform { CenterX = xOffset, CenterY = yOffset, Angle = rotationAngle }
                }
            });
        }

        /// <summary>
        /// Retrieve DataPoints
        /// </summary>
        /// <param name="zone"></param>
        /// <param name="start"></param>
        /// <param name="end"></param>
        /// <returns></returns>
        private ChartValues<ObservablePoint> GetDataPoints(int zone, double start, double end)
        {
            var dataPoints = new ChartValues<ObservablePoint>(); 
            
            var endZone = zone;
            var startZone = endZone - 1;

            dataPoints.Add(new ObservablePoint()
            {
                X = start,
                Y = startZone
            }) ;

            dataPoints.Add(new ObservablePoint()
            {
                X = end,
                Y = endZone
            });
            
            return dataPoints;
        }
        #endregion
    }
}
