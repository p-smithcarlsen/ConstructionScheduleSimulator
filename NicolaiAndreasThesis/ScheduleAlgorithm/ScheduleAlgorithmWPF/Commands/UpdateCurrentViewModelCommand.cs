using ScheduleAlgorithmWPF.State.Navigators;
using ScheduleAlgorithmWPF.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;

namespace ScheduleAlgorithmWPF.Commands
{
    public class UpdateCurrentViewModelCommand : ICommand
    {
        public event EventHandler CanExecuteChanged;

        private INavigator _navigator;     

        public UpdateCurrentViewModelCommand(INavigator navigator)
        {
            _navigator = navigator;            
        }

        public bool CanExecute(object parameter)
        {
            return true;
        }
        private void NotImplementedPopUp(ViewType type)
        {
            string msg = $"The following command: {type.ToString()} is not implemented yet." + 
                "\nDo you want to close this window?";
            MessageBoxResult result = MessageBox.Show(msg, "Functionality not implemented", MessageBoxButton.OK, MessageBoxImage.Information);
            
        }

        public void Execute(object parameter)
        {
            if (parameter is ViewType)
            {
                ViewType viewtype = (ViewType)parameter;
                switch (viewtype)
                {
                    case ViewType.Home:
                        _navigator.CurrentViewModel = new HomeViewModel();
                        break;
                    case ViewType.Data:
                        _navigator.CurrentViewModel = new DataViewModel();
                        break;
                    case ViewType.EarliestStart:
                        _navigator.CurrentViewModel = new EarliestStartViewModel();
                        break;
                    case ViewType.LatestStart:
                        _navigator.CurrentViewModel = new LatestStartViewModel();
                        break;
                    case ViewType.EarliestFinish:
                        NotImplementedPopUp(ViewType.EarliestFinish);
                        break;
                    case ViewType.LatestFinish:
                        NotImplementedPopUp(ViewType.LatestFinish);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
