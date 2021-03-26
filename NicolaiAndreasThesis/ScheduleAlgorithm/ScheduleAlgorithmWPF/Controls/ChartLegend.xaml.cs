using LiveCharts.Wpf;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace ScheduleAlgorithmWPF.Controls
{
    /// <summary>
    /// Interaction logic for ChartLegend.xaml
    /// </summary>
    public partial class ChartLegend : UserControl, IChartLegend
    {
        private List<SeriesViewModel> _series;

        public ChartLegend()
        {
            _series = new List<SeriesViewModel>();
            InitializeComponent();
            DataContext = this;
        }

        public List<SeriesViewModel> Series
        {
            get
            {              
                return _series;
            }
            set
            {
                if (value == _series)
                    return;
                OnPropertyChanged("Series"); 
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {           
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
