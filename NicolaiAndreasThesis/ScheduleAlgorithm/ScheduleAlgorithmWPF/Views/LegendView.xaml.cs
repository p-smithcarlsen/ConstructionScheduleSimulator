using ScheduleAlgorithmWPF.ViewModels.Base;
using System;
using System.Collections.Generic;
using System.Linq;
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

namespace ScheduleAlgorithmWPF.Views
{
    /// <summary>
    /// Interaction logic for LegendView.xaml
    /// </summary>
    public partial class LegendView : UserControl
    {

        public List<Rectangle> Rectangles { get; set; } = new List<Rectangle>();
        public LegendView()
        {
            InitializeComponent();            
        }
       
    }
}
