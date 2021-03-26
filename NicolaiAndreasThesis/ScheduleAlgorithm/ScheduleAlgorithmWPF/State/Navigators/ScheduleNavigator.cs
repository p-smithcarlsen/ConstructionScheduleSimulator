using ScheduleAlgorithmWPF.Commands;
using ScheduleAlgorithmWPF.Models;
using ScheduleAlgorithmWPF.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace ScheduleAlgorithmWPF.State.Navigators
{
    public class ScheduleNavigator : ObservableObject, INavigator
    {
        public ViewModelBase CurrentViewModel { get; set; }

        public ICommand UpdateCurrentViewModelCommand => new UpdateCurrentViewModelCommand(this);
    }
  
}
