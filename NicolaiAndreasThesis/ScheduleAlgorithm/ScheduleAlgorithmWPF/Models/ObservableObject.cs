using PropertyChanged;
using System.ComponentModel;


namespace ScheduleAlgorithmWPF.Models
{
    /// <summary>
    /// 
    /// </summary>
    /// This annotation sets property fields nicely without calling OnPropertyChanged foreach property
    [AddINotifyPropertyChangedInterface]
    public class ObservableObject : INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged;

        protected void OnPropertyChanged(string propertyName)
        {
           if(propertyName != null)
               PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));            
        }
    }
}
