using LiveCharts;
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
    /// Interaction logic for TaskToolTipControl.xaml
    /// </summary>
    public partial class TaskToolTipControl : IChartTooltip
    {

        #region Fields
        private TooltipData _data;
        #endregion

        #region Properties
        public event PropertyChangedEventHandler PropertyChanged;

        public TooltipData Data
        {
            get
            {                
                return _data;
            }
            set
            {
                if (value == _data)
                    return;
                _data = value;
                OnPropertyChanged("Data");
            }
        }

        public TooltipSelectionMode? SelectionMode { get; set; }

        #endregion


        public TaskToolTipControl()
        {
            InitializeComponent();
            _data = new TooltipData();
            DataContext = this;
        }        

      
        /// <summary>
        /// Property change
        /// </summary>
        /// <param name="propertyName"></param>
        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {                        
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
