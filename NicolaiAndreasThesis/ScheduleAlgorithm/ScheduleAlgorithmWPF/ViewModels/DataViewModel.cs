using ScheduleAlgorithmWPF.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ScheduleAlgorithm.Domain.Entity;

namespace ScheduleAlgorithmWPF.ViewModels
{
    public class DataViewModel : ViewModelBase
    {
        public TaskCollection<ConstructionTask> Collection { get; set; }

        public DataViewModel()
        {
            // set collection from db
            Collection = new TaskCollection<ConstructionTask>();
        }

        /// <summary>
        /// Get all tasks to usein the view model
        /// </summary>
        /// <returns></returns>
        private IEnumerable<ConstructionTask> GetAllTask()
        {            
            return Collection.GetAllTasks();
        }

    }
}
