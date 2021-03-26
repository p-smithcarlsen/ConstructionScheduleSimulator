using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using ScheduleAlgorithm.Domain.Entity;

namespace Database.EntityFrameworkCore
{
    public class ScheduleDbContext : DbContext
    {
        public DbSet<ConstructionTask> Tasks { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            modelBuilder.Entity<ConstructionTask>()
                        .HasKey(ct => ct.TaskID);
        }

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            //optionsBuilder.UseSqlServer("Server=(localdb)\\MSSQLLocalDB;Database=ScheduleAlgorithm;Trusted_Connection=True;");
            optionsBuilder.UseInMemoryDatabase("Serd");
            base.OnConfiguring(optionsBuilder);
        }
    }
}