using System.Collections.Generic;
using System.Threading.Tasks;

namespace Database.EntityFrameworkCore.Services
{
    public interface IDataService<T>
    {
        Task<IEnumerable<T>> GetAllTasks();

        Task<T> GetTask(string id);

        Task<T> Update(string id, T entity);
       
        Task<T> Create(T entity);

        Task<bool> Delete(string id);

    }
}
