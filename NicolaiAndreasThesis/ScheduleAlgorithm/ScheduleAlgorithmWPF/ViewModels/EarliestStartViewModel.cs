using LiveCharts.Wpf;
using LiveCharts.Wpf.Charts.Base;
using ScheduleAlgorithmWPF.State.Navigators;
using ScheduleAlgorithmWPF.ViewModels.Base;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ScheduleAlgorithmWPF.ViewModels
{
    public class EarliestStartViewModel : ScheduleViewModel
    {
        #region Properties  
        /// <summary>
        /// Get type of view
        /// </summary>
        public override ViewType Type => ViewType.EarliestStart;
      
        #endregion

        public EarliestStartViewModel()
        {
            Chart = LoadContent(Type);          
            // error handling
            if (!string.IsNullOrEmpty(ExceptionMessage))
            {
                ViewModelUtils.ResponseUserError(ExceptionMessage);
            }
        }       
    }
}
