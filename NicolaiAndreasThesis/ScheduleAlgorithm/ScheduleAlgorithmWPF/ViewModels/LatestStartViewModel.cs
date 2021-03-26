using ScheduleAlgorithmWPF.State.Navigators;
using ScheduleAlgorithmWPF.ViewModels.Base;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ScheduleAlgorithmWPF.ViewModels
{
    public class LatestStartViewModel : ScheduleViewModel
    {
        public override ViewType Type => ViewType.LatestStart;    

        public LatestStartViewModel()
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
