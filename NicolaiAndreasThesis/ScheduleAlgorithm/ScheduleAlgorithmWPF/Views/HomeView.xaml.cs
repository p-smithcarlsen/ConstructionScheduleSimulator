using LiveCharts;
using LiveCharts.Defaults;
using LiveCharts.Wpf;
using ScheduleAlgorithmLibrary.Algorithm;
using ScheduleAlgorithmWPF.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Collections.ObjectModel;
using System.Text.RegularExpressions;
using System.Windows.Controls.Primitives;
using Microsoft.Win32;
using ScheduleAlgorithm.Domain.Entity;
using ScheduleAlgorithmLibrary.Utilities;
using ScheduleAlgorithmWPF.Data;
using ScheduleAlgorithmWPF.State.Navigators;

namespace ScheduleAlgorithmWPF.Views
{
    /// <summary>
    /// Interaction logic for HomeView.xaml
    /// </summary>
    public partial class HomeView : UserControl
    {
        readonly MsSqlDataService _msSqlDataService = new MsSqlDataService();
        private List<ConstructionTask> _newConstructionTasks = new List<ConstructionTask>();
        private ConstructionTask _newConstructionTask;

        public ScheduleViewModel CurrentVM { get; set; }

        public bool IsCpToggled { get => ScheduleViewModel.TbTechnical.Toggled; }

        public HomeView()
        {
            InitializeComponent();
            ScheduleViewModel.TbTechnical = TbTechnical;
            ScheduleViewModel.TbContinuity = TbContinuity;
            ScheduleViewModel.TbLocation = TbLocation;
            ScheduleViewModel.TbProduction = TbProduction;

            ScheduleViewModel.WpCraftsPanel = BtnWrapPanel;

            #region Set default text
            txtTaktInput.Text = "Takt";
            txtDeadlineInput.Text = "Deadline";

            txtTaktInput.Foreground = new SolidColorBrush(Colors.Gray);
            txtDeadlineInput.Foreground = new SolidColorBrush(Colors.Gray);

            #endregion

            CurrentVM = DataContext as ScheduleViewModel;
        }

        private void UpdateView()
        {
            //// get the view model
            CurrentVM = ((HomeViewModel)DataContext).Navigator.CurrentViewModel as ScheduleViewModel;

            if (CurrentVM != null && CurrentVM is ScheduleViewModel)
            {
                CurrentVM.InitSchedule();
                CurrentVM.LoadContent(CurrentVM.Type);

            }
        }

        #region UI Component logic
        private void btnImport_Click(object sender, RoutedEventArgs e)
        {
            _msSqlDataService.ClearConstructionTaskDatabase();
            OpenFileDialog openFileDialog = new OpenFileDialog { Filter = "CSV File|*.csv" };

            if (openFileDialog.ShowDialog() == false)
            {
                return;
            }
            CsvParser csvParser = new CsvParser();
            var fileName = openFileDialog.FileName;

            List<ConstructionTask> tasks = null;
            Task.Run(() =>
            {
                tasks = csvParser.LoadCsvFile(fileName);
                _msSqlDataService.AddNewConstructionTasks(tasks);
            });

        }

        private void btnUpdate_Click(object sender, RoutedEventArgs e)
        {

                var existingTasks = _msSqlDataService.LoadConstructionTasks();

                var updatingTasks = _newConstructionTasks.Intersect(existingTasks, ConstructionTask.TaskIdComparer).ToList();
                if (updatingTasks.Count > 0)
                {
                    _msSqlDataService.UpdateConstructionTasks(updatingTasks);
                }

                var newTasks = _newConstructionTasks.Except(existingTasks, ConstructionTask.TaskIdComparer).ToList();
                if (newTasks.Count > 0)
                {
                    _msSqlDataService.AddNewConstructionTasks(newTasks);
                }

                _newConstructionTasks.Clear();

                UpdateView();
            }


        private void lstView_CellEditEnding(object sender, DataGridCellEditEndingEventArgs e)
        {
            try
            {

                FrameworkElement elementTaskId = lstView.Columns[0].GetCellContent(e.Row);
                if (elementTaskId?.GetType() == typeof(TextBox))
                {
                    var taskID = ((TextBox) elementTaskId).Text;
                    _newConstructionTask.TaskID = taskID;
                }

                FrameworkElement elementZone = lstView.Columns[1].GetCellContent(e.Row);
                if (elementZone?.GetType() == typeof(TextBox))
                {
                    var zone = ((TextBox) elementZone).Text;
                    _newConstructionTask.Zone = Convert.ToInt32(zone);
                }

                FrameworkElement elementCraft = lstView.Columns[2].GetCellContent(e.Row);
                if (elementCraft?.GetType() == typeof(TextBox))
                {
                    var craft = ((TextBox)elementCraft).Text;
                    _newConstructionTask.Craft = craft;
                }

                FrameworkElement elementEstimatedDuration = lstView.Columns[3].GetCellContent(e.Row);
                if (elementEstimatedDuration?.GetType() == typeof(TextBox))
                {
                    var estimatedDuration = ((TextBox) elementEstimatedDuration).Text;
                    _newConstructionTask.EstimatedDuration = Convert.ToDouble(estimatedDuration);
                }

                FrameworkElement elementOperation = lstView.Columns[4].GetCellContent(e.Row);
                if (elementOperation?.GetType() == typeof(TextBox))
                {
                    var operation = ((TextBox) elementOperation).Text;
                    _newConstructionTask.Operation = operation;
                }

                FrameworkElement elementEstimatedResources = lstView.Columns[5].GetCellContent(e.Row);
                if (elementEstimatedResources?.GetType() == typeof(TextBox))
                {
                    var estimatedResources = ((TextBox) elementEstimatedResources).Text;
                    _newConstructionTask.EstimatedResources = Convert.ToInt32(estimatedResources);
                }

                FrameworkElement elementActualResources = lstView.Columns[6].GetCellContent(e.Row);
                if (elementActualResources?.GetType() == typeof(TextBox))
                {
                    var actualResources = ((TextBox) elementActualResources)?.Text;
                    _newConstructionTask.ActualResources = Convert.ToDouble(actualResources);
                }

                FrameworkElement elementPrecedence = lstView.Columns[7].GetCellContent(e.Row);
                if (elementPrecedence?.GetType() == typeof(TextBox))
                {
                    var precedence = ((TextBox) elementPrecedence).Text;
                    _newConstructionTask.Precedence = precedence;
                }
            }
            catch (Exception exception)
            {
                MessageBox.Show(exception.Message);
            }
        }

        private void lstView_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            _newConstructionTask = lstView.SelectedItem as ConstructionTask;
        }

        private void lstView_RowEditEnding(object sender, DataGridRowEditEndingEventArgs e)
        {
            try
            {
                _newConstructionTasks.Add(_newConstructionTask);
            }
            catch (Exception exception)
            {
                MessageBox.Show(exception.Message);
            }
        }

        private void btnSetTakt_Click(object sender, RoutedEventArgs e)
        {
            // error handling
            if (txtTaktInput.Text == "Takt")
                return;

            var takt = Convert.ToDouble(txtTaktInput.Text);
            List<ConstructionTask> notUpdatedConstructionTasks = new List<ConstructionTask>();

            if (takt <= 0)
            {
                MessageBox.Show("Please select a takt larger than 0");
                return;
            }

            var constructionTasks = _msSqlDataService.LoadConstructionTasks();

            GraphUtil.Sink.EF = 0;
            GraphUtil.Sink.LF = Double.PositiveInfinity;

            GraphUtil.Source.EF = 0;
            GraphUtil.Source.LF = Double.PositiveInfinity;

            foreach (var constructionTask in constructionTasks)
            {
                constructionTask.SetResourcesByTakt(takt);
                if (constructionTask.ActualResources == 0)
                {
                    notUpdatedConstructionTasks.Add(constructionTask);
                }
                _msSqlDataService.UpdateConstructionTask(constructionTask);
            }

            UpdateView();

            if (notUpdatedConstructionTasks.Count > 0)
            {
                string msg = "The following tasks did not change, because their estimated resources " +
                             "were less than or equal to zero:\n";

                foreach (var notUpdatedConstructionTask in notUpdatedConstructionTasks)
                {
                    msg += "Task id: " + notUpdatedConstructionTask.TaskID + "\n";
                }
                MessageBox.Show(msg,"Takt Warning", MessageBoxButton.OK, MessageBoxImage.Warning);
            }

        }

        private static readonly Regex _regex = new Regex("[^0-9.-]+"); //regex that matches disallowed text

        private static bool IsTextAllowed(string text)
        {
            return !_regex.IsMatch(text);
        }

        private void txtTaktInput_PreviewTextInput(object sender, TextCompositionEventArgs e)
        {
            e.Handled = !IsTextAllowed(e.Text);
        }

        private void txtTaktInput_LostFocus(object sender, RoutedEventArgs e)
        {
            if (string.IsNullOrWhiteSpace(txtTaktInput.Text))
            {
                txtTaktInput.Text = "Takt";
                txtTaktInput.Foreground = new SolidColorBrush(Colors.Gray);
            }
        }

        private void txtTaktInput_GotFocus(object sender, RoutedEventArgs e)
        {
            if (txtTaktInput.Text == "Takt")
            {
                txtTaktInput.Text = "";
                txtTaktInput.Foreground = new SolidColorBrush(Colors.Black);
            }
        }

        private void TbTechnical_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            UpdateView();
        }

        private void TbLocation_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            UpdateView();
        }

        private void TbContinuity_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            UpdateView();
        }

        private void TbProduction_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            UpdateView();
        }

        private void btnSetDeadline_Click(object sender, RoutedEventArgs e)
        {
            // error handling
            if (txtDeadlineInput.Text == "Deadline")
                return;

            double makespan = Convert.ToDouble(txtDeadlineInput.Text);

            CurrentVM = ((HomeViewModel)DataContext).Navigator.CurrentViewModel as ScheduleViewModel;
            if (CurrentVM != null && CurrentVM is ScheduleViewModel)
            {
                CurrentVM.Deadline = makespan;
            }

        }

        private void txtDeadlineInput_PreviewTextInput(object sender, TextCompositionEventArgs e)
        {
            e.Handled = !IsTextAllowed(e.Text);
        }

        private void txtDeadlineInput_LostFocus(object sender, RoutedEventArgs e)
        {
            if (string.IsNullOrWhiteSpace(txtDeadlineInput.Text))
            {
                txtDeadlineInput.Text = "Deadline";
                txtDeadlineInput.Foreground = new SolidColorBrush(Colors.Gray);
            }
        }

        private void txtDeadlineInput_GotFocus(object sender, RoutedEventArgs e)
        {
            if (txtDeadlineInput.Text == "Deadline")
            {
                txtDeadlineInput.Text = "";
                txtDeadlineInput.Foreground = new SolidColorBrush(Colors.Black);
            }
        }
        #endregion


    }
}
