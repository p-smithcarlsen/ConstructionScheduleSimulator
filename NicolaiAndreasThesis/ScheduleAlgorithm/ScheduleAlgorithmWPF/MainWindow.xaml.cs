using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using Database.EntityFrameworkCore;
using LiveCharts;
using LiveCharts.Defaults;
using LiveCharts.Wpf;
using ScheduleAlgorithmLibrary.Algorithm;
using ScheduleAlgorithmLibrary.Utilities;
using ScheduleAlgorithmWPF.Commands;
using ScheduleAlgorithmWPF.ViewModels;
using ScheduleAlgorithmWPF.Views;

namespace ScheduleAlgorithmWPF
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();
            nav.Content = new HomeViewModel();       
        }

       
    }
}
