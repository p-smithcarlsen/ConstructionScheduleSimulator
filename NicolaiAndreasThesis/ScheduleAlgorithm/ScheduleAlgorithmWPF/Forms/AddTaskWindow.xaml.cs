using System;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using ScheduleAlgorithm.Domain.Entity;
using ScheduleAlgorithmWPF.Data;
using ScheduleAlgorithmWPF.Views;

namespace ScheduleAlgorithmWPF.Forms
{
    /// <summary>
    /// Interaction logic for AddTaskWindow.xaml
    /// </summary>
    public partial class AddTaskWindow : Window
    {
        private readonly MsSqlDataService _msSqlDataService = new MsSqlDataService();
        private readonly DataView _dataView;
        private string _taskId = string.Empty;

        public AddTaskWindow()
        {
            InitializeComponent();
        }

        public AddTaskWindow(DataView dataView)
        {
            _dataView = dataView;
            InitializeComponent();
        }

        private void Window_Loaded(object sender, RoutedEventArgs e)
        {
            CbProgress.ItemsSource = Enum.GetValues(typeof(ProgressState)).Cast<ProgressState>();
            CbProgress.SelectedValue = ProgressState.Pending;
        }

        private void BtnCreateTask_Click(object sender, RoutedEventArgs e)
        {
            var taskOperation = TxtTaskOperation.Text;
            var taskCraft = TxtCraft.Text;
            var taskProgress = (ProgressState)CbProgress.SelectionBoxItem;
            var taskPrecedence = TxtPrecedence.Text;

            if (string.IsNullOrWhiteSpace(TxtZone.Text))
            {
                MessageBox.Show("Please enter a valid integer as zone", "Zone Error", MessageBoxButton.OK,
                    MessageBoxImage.Warning);
                return;
            }

            Double.TryParse(TxtEstimatedDuration.Text, out var taskEstimatedDuration);
            Int32.TryParse(TxtZone.Text, out var taskZone);

            ConstructionTask constructionTask = new ConstructionTask()
            {
                Craft = taskCraft,
                EstimatedDuration = taskEstimatedDuration,
                Operation = taskOperation,
                TaskID = _taskId,
                Zone = taskZone,
                Progress = taskProgress,
                Precedence = taskPrecedence
            };

            _msSqlDataService.AddNewConstructionTask(constructionTask);

            _dataView.FillDataGridFromDatabase();
            this.Close();
        }

        public string GenerateTaskID(string zone)
        {
            MsSqlDataService msSqlDataService = new MsSqlDataService();

            var dbConstructionTasks = msSqlDataService.LoadConstructionTasks();
            var zoneCompare = int.Parse(zone);
            var nextIdNumber = dbConstructionTasks.Count(x => x.Zone == zoneCompare);

            return zone + "." + nextIdNumber;
        }


        private void TxtZone_OnPreviewTextInput(object sender, TextCompositionEventArgs e)
        {
            e.Handled = !IsTextAllowed(e.Text);
        }

        private static readonly Regex _regex = new Regex("[^0-9.-]+"); //regex that matches disallowed text
        private static bool IsTextAllowed(string text)
        {
            return !_regex.IsMatch(text);
        }

        private void TxtZone_LostFocus(object sender, RoutedEventArgs e)
        {
            if (TxtZone.Text.Length > 0)
            {
                var TaskID = GenerateTaskID(TxtZone.Text);
                _taskId = TaskID;
                TxtTaskID.Text = _taskId;
            }
        }

    }
}