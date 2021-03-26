using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using Database.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;
using ScheduleAlgorithm.Domain.Entity;

namespace ScheduleAlgorithmWPF.Data
{
    public class MsSqlDataService
    {
        public void AddNewConstructionTasks(List<ConstructionTask> newConstructionTasks)
        {
            try
            {
                using (ScheduleDbContext db = new ScheduleDbContext())
                {
                    db.Tasks.AddRange(newConstructionTasks);
                    db.SaveChanges();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("When adding multiple construction tasks to DB, a handled exception just occurred: " + ex.Message, "Exception", MessageBoxButton.OK, MessageBoxImage.Warning);
            }
        }

        public void UpdateConstructionTasks(List<ConstructionTask> constructionTasks)
        {
            try
            {
                using (ScheduleDbContext db = new ScheduleDbContext())
                {
                    db.Tasks.UpdateRange(constructionTasks);
                    db.SaveChanges();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("When updating multiple construction tasks to DB, a handled exception just occurred: " + ex.Message, "Exception", MessageBoxButton.OK, MessageBoxImage.Warning);
            }
        }

        public void UpdateConstructionTask(ConstructionTask constructionTask)
        {
            try
            {
                using (ScheduleDbContext db = new ScheduleDbContext())
                {
                    db.Tasks.Update(constructionTask);
                    db.SaveChanges();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("When updating a construction tasks to DB, a handled exception just occurred: " + ex.Message, "Exception", MessageBoxButton.OK, MessageBoxImage.Warning);
                throw;
            }
        }

        public void AddNewConstructionTask(ConstructionTask newDbConstructionTask)
        {
            try
            {
                using (ScheduleDbContext db = new ScheduleDbContext())
                {
                    db.Tasks.Add(newDbConstructionTask);
                    db.SaveChanges();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("When adding the a single construction task to DB, a handled exception just occurred: " + ex.Message, "Exception", MessageBoxButton.OK, MessageBoxImage.Warning);
            }
        }

        public int ClearConstructionTaskDatabase()
        {
            var numberOfRemovedRows = 0;

            try
            {
                using (ScheduleDbContext db = new ScheduleDbContext())
                {
                    var allTasks = LoadConstructionTasks();
                    db.RemoveRange(allTasks);
                    db.SaveChanges();
                    numberOfRemovedRows = allTasks.Count;
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("When clearing construction tasks from DB, a handled exception just occurred: " + ex.Message, "Exception", MessageBoxButton.OK, MessageBoxImage.Warning);
            }

            return numberOfRemovedRows;
        }

        public List<ConstructionTask> LoadConstructionTasks()
        {
            var tasks = new List<ConstructionTask>();
            try
            {
                using (ScheduleDbContext db = new ScheduleDbContext())
                {
                    tasks = db.Tasks.ToList();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("When loading construction tasks from DB, a handled exception just occurred: " + ex.Message, "Exception", MessageBoxButton.OK, MessageBoxImage.Warning);
            }

            return tasks;
        }
    }
}