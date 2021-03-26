using ScheduleAlgorithmWPF.Models;
using ScheduleAlgorithmWPF.ViewModels;
using ScheduleAlgorithmWPF.ViewModels.Base;
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
    /// Interaction logic for ToggleButtons.xaml
    /// </summary>
    public partial class ToggleButtons : UserControl, INotifyPropertyChanged
    {
        Thickness LeftSideAlignment = new Thickness(-39, 0, 0, 0);
        Thickness RightSideAlignment = new Thickness(0, 0, -39, 0);

        SolidColorBrush Off = new SolidColorBrush(Color.FromRgb(160, 160, 160));
        SolidColorBrush On = new SolidColorBrush(Color.FromRgb(0, 123, 207));
 
        private bool _Toggled = false;

        public event PropertyChangedEventHandler PropertyChanged;

        public bool Toggled
        {
            get
            {
                return _Toggled;
            }
            set
            {
                if (value == _Toggled)
                    return;
                _Toggled = value;
                OnPropertyChanged();
            }
        }

        protected void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        public ToggleButtons()
        {
            InitializeComponent();
            Back.Fill = Off;
            _Toggled = false;
            Dot.Margin = LeftSideAlignment;
            OnOff.Text = "Off";          
        }       

        private void Dot_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
           
            if (!_Toggled)
            {
                // Button is now on
                Back.Fill = On;
                _Toggled = true;
                Dot.Margin = RightSideAlignment;
                OnOff.Text = "On";
                
            }
            else
            {
                // Button is now off
                Back.Fill = Off;
                _Toggled = false;
                Dot.Margin = LeftSideAlignment;
                OnOff.Text = "Off";               
            }
        }
    }
}
