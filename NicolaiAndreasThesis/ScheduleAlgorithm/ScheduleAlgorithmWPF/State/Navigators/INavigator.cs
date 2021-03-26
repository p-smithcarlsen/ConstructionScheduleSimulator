using ScheduleAlgorithmWPF.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace ScheduleAlgorithmWPF.State.Navigators
{
    public enum ViewType
    {
        /// <summary>
        /// Schedule view
        /// </summary>
        Home,        
        /// <summary>
        /// Weather data 
        /// </summary>
        Data,
        /// <summary>
        /// Computes CP
        /// </summary>
        CriticalPath,
        /// <summary>
        /// Get earliest start date for every node
        /// </summary>
        EarliestStart,
        /// <summary>
        /// Get latest start date for every node
        /// </summary>
        LatestStart,
        /// <summary>
        /// Get earliest finish date for every node
        /// </summary>
        EarliestFinish,
        /// <summary>
        /// Get latest finish date for every node
        /// </summary>
        LatestFinish,
        /// <summary>
        /// TreeView 
        /// </summary>
        TreeView
    }

    public interface INavigator
    {
        ViewModelBase CurrentViewModel { get; set; }
        ICommand UpdateCurrentViewModelCommand { get; }
    }
}
