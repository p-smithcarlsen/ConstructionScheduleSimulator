using ScheduleAlgorithmWPF.State.Navigators;


namespace ScheduleAlgorithmWPF.ViewModels
{
    public class HomeViewModel : ViewModelBase
    {       
        public INavigator Navigator { get; set; } = new ScheduleNavigator();
    }
}
