using ScheduleAlgorithmLibrary.Algorithm;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ScheduleAlgorithm.Domain.Entity;
using ScheduleAlgorithmWPF.Models;
using ScheduleAlgorithmLibrary.Utilities;

namespace ScheduleAlgorithmWPF.Models
{
    /// <summary>
    /// This class take care of the communication to the database
    /// </summary>
    /// <typeparam name="T"></typeparam>
    public class TaskCollection<T> : ObservableObject, IDataService<T> where T : ConstructionTask
    {
        #region Properties
        /// <summary>
        /// Collection of tasks from fileparser
        /// </summary>
        public IList<ConstructionTask> Collection;

        /// <summary>
        /// Create an instance of the communication to the database
        /// </summary>
        public static TaskCollection<ConstructionTask> TaskCollections = new TaskCollection<ConstructionTask>();
        #endregion

        #region EndPoints
        /// <summary>
        /// Add an entity to the database
        /// </summary>
        /// <param name="entity"></param>
        /// <returns></returns>
        public Task<T> Create(T entity)
        {
            if (entity is ConstructionTask)
            {
                Collection?.Add(entity as ConstructionTask);
            }
            throw new ArgumentException($"Cannot insert type: {entity.GetType()} into db");           
        }

        /// <summary>
        /// Delete an object if present
        /// </summary>
        /// <param name="id">The Task that needs to be deleted </param>
        /// <returns>Returns true if the object exist, false if not</returns>
        public async Task<bool> Delete(string id)
        {           
            foreach (T item in Collection)
            {
                if (item.TaskID == id)
                    Collection.Remove(item);
            }
            return false;            
        }       
        
        /// <summary>
        /// Get Alle constructoin tasks from the database
        /// </summary>
        /// <returns></returns>
        public IEnumerable<T> GetAllTasks()
        {         
            // when the db is not initialized 
            if (Collection?.Count() > 0)            
                return Collection as IEnumerable<T>;            
            else
            {
                 CsvParser parser = new CsvParser();
                 Collection = parser.LoadCsvFile();                
            }
            return Collection as IEnumerable<T>;
        }

        /// <summary>
        /// Get a specific task from the database
        /// </summary>
        /// <param name="id"></param>
        /// <returns></returns>
        public async Task<T> GetTask(string id)
        {
            foreach (T item in Collection)
            {
                if (item.TaskID.Equals(id))
                    return item;
                else                
                    throw new ArgumentException($"The database does not contain task {id}");                
            }            
            return null;
        }

        /// <summary>
        /// Update properties to a task
        /// </summary>
        /// <param name="id">Task Id</param>
        /// <param name="entity">Object</param>
        /// <returns></returns>
        public Task<T> Update(string id, T entity)
        {
            throw new NotImplementedException();
        }
        #endregion
    }

    /// <summary>
    /// I want to remove this to domain logic project!     
    /// </summary>
    /// <typeparam name="T">Generic type</typeparam>
    public interface IDataService<T>
    {
        IEnumerable<T> GetAllTasks();

        Task<T> GetTask(string id);

        Task<T> Update(string id, T entity);

        Task<T> Create(T entity);

        Task<bool> Delete(string id);
    }
}

