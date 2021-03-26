using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Database.EntityFrameworkCore.Model
{
    public class ConstructionTask
    {
        [Key]
        public string TaskID { get; set; }
        public ICollection<Resource> Resources { get; set; }

        private sealed class TaskIdEqualityComparer : IEqualityComparer<ConstructionTask>
        {
            public bool Equals(ConstructionTask x, ConstructionTask y)
            {
                if (ReferenceEquals(x, y)) return true;
                if (ReferenceEquals(x, null)) return false;
                if (ReferenceEquals(y, null)) return false;
                if (x.GetType() != y.GetType()) return false;
                return x.TaskID == y.TaskID;
            }

            public int GetHashCode(ConstructionTask obj)
            {
                return obj.TaskID.GetHashCode();
            }
        }

        public static IEqualityComparer<ConstructionTask> TaskIdComparer { get; } = new TaskIdEqualityComparer();

        public string Zone { get; set; }
        public string Craft { get; set; }
        public string Operation { get; set; }
        public double? ActualDuration { get; set; }
        public double EstimatedDuration { get; set; }
        public ProgressState Progress { get; set; }
        public ICollection<ConstructionTask> Precedence { get; set; }
    }

    public enum ProgressState
    {
        Pending,
        InProgress,
        Finished,
    }
}
