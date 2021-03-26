using LiveCharts.Defaults;
using LiveCharts;
using LiveCharts.Wpf;
using ScheduleAlgorithmWPF.State.Navigators;
using ScheduleAlgorithmWPF.ViewModels;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Controls;
using System.Windows.Media;
using ScheduleAlgorithmLibrary.Algorithm;
using Separator = LiveCharts.Wpf.Separator;
using ScheduleAlgorithmWPF.ViewModels.Base;

namespace ScheduleAlgorithmWPF.Views.Schedules
{
    /// <summary>
    /// Interaction logic for EarliestStart.xaml
    /// </summary>
    public partial class EarliestStart : UserControl
    {     
        public EarliestStart()
        {
            InitializeComponent();            
        }        
    }
}