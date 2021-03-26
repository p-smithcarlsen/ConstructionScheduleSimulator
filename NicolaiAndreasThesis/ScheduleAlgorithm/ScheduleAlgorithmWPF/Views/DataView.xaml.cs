using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using Database.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;
using ScheduleAlgorithm.Domain.Entity;
using ScheduleAlgorithmLibrary.Utilities;
using ScheduleAlgorithmWPF.Data;
using ScheduleAlgorithmWPF.Forms;
using ScheduleAlgorithmWPF.ViewModels;
using MessageBox = System.Windows.MessageBox;
using OpenFileDialog = Microsoft.Win32.OpenFileDialog;
using UserControl = System.Windows.Controls.UserControl;

namespace ScheduleAlgorithmWPF.Views
{
    /// <summary>
    /// Interaction logic for DataView.xaml
    /// </summary>
    public partial class DataView : UserControl
    {
        readonly MsSqlDataService _msSqlDataService = new MsSqlDataService();

        public DataView()
        {
            InitializeComponent();
        }
        /// <summary>
        /// 
        /// </summary>
        public void FillDataGridFromDatabase()
        {
            var constructionTasks = _msSqlDataService.LoadConstructionTasks();
            TasksDataGrid.ItemsSource = constructionTasks;

        }
        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void UserControl_Loaded(object sender, RoutedEventArgs e)
        {
            FillDataGridFromDatabase();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void BtnUpdateTasks_Click(object sender, RoutedEventArgs e)
        {

        }
        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private async void BtnImportFromCSV_Click(object sender, RoutedEventArgs e)
        {
            OpenFileDialog openFileDialog = new OpenFileDialog { Filter = "CSV File|*.csv" };

            if (openFileDialog.ShowDialog() == false)
            {
                return;
            }
            CsvParser csvParser = new CsvParser();
            var fileName = openFileDialog.FileName;

            List<ConstructionTask> tasks = null;
            await Task.Run(() =>
            {
                tasks = csvParser.LoadCsvFile(fileName);
                var existingTasks = _msSqlDataService.LoadConstructionTasks();

                var newTasks = tasks.Except(existingTasks, ConstructionTask.TaskIdComparer).ToList();
                _msSqlDataService.AddNewConstructionTasks(newTasks);
                MessageBox.Show(newTasks.Count + " constructions tasks has been added to the database");

            });

            FillDataGridFromDatabase();

            //CsvImportWindow csvImportWindow = new CsvImportWindow(tasks, this);
            //csvImportWindow.Show();

        }

        private void TasksDataGrid_OnAutoGeneratingColumn(object sender, DataGridAutoGeneratingColumnEventArgs e)
        {
            string headerName = e.Column.Header.ToString();

            switch (headerName)
            {
                case "Progress":
                    e.Column.IsReadOnly = false;
                    break;
                case "TaskID":
                    e.Column.IsReadOnly = true;
                    break;
                case "EstimatedResources":
                    e.Column.IsReadOnly = true;
                    e.Column.Header = "Estimated Resources";
                    break;
                case "ActualResources":
                    e.Column.IsReadOnly = true;
                    e.Column.Header = "Actual Resources";
                    break;
                case "Zone":
                    e.Column.IsReadOnly = true;
                    break;
                case "Craft":
                    e.Column.IsReadOnly = true;
                    break;
                case "Operation":
                    e.Column.IsReadOnly = true;
                    break;
                case "EstimatedDuration":
                    e.Column.Header = "Estimated Duration";
                    e.Column.IsReadOnly = true;
                    break;
                case "Precedence":
                    e.Column.IsReadOnly = true;
                    break;
            }
        }

        private void BtnAddTask_Click(object sender, RoutedEventArgs e)
        {
            AddTaskWindow addTaskWindow = new AddTaskWindow(this);
            addTaskWindow.Show();
        }

        private void BtnClearData_OnClick(object sender, RoutedEventArgs e)
        {
            var removedRows = _msSqlDataService.ClearConstructionTaskDatabase();
            MessageBox.Show(removedRows + " construction tasks were removed from the database");

            FillDataGridFromDatabase();
        }
    }
}
