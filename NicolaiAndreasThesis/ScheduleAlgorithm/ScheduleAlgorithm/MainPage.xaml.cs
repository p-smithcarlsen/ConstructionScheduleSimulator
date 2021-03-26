using ScheduleAlgorithmLibrary.Utilities;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.Storage;
using Windows.Storage.Pickers;
using Windows.UI.Popups;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using Microsoft.Toolkit.Uwp.UI.Controls;
using DocumentFormat.OpenXml.CustomProperties;
using ScheduleAlgorithm.Views;
using ScheduleAlgorithmLibrary.Utilities;
using ScheduleAlgorithmLibrary.Algorithm;

using System.Collections;

namespace ScheduleAlgorithm
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page, INotifyPropertyChanged
    {
        public MainPage()
        {
            this.InitializeComponent();
        }



        public event PropertyChangedEventHandler PropertyChanged;
        
        protected async override void OnNavigatedTo(NavigationEventArgs e)
        {

        }

        private void OnPage_Loaded(object sender, RoutedEventArgs e)
        {
        }

        #region NavigationView event handlers
        private void nvTopLevelNav_Loaded(object sender, RoutedEventArgs e)
        {
            // set the initial SelectedItem
            foreach (NavigationViewItemBase item in NavView.MenuItems)
            {
                if (item is NavigationViewItem && item.Tag.ToString() == "taskList")
                {
                    NavView.SelectedItem = item;
                    break;
                }
            }
            ContentFrame.Navigate(typeof(TaskList));
        }

        private void nvTopLevelNav_SelectionChanged(NavigationView sender, NavigationViewSelectionChangedEventArgs args)
        {
        }

        private void nvTopLevelNav_ItemInvoked(NavigationView sender, NavigationViewItemInvokedEventArgs args)
        {
            TextBlock ItemContent = args.InvokedItem as TextBlock;
            if (ItemContent != null)
            {
                switch (ItemContent.Tag)
                {
                    case "Nav_TaskList":
                        ContentFrame.Navigate(typeof(TaskList));
                        break;

                    case "Nav_Schedule":
                        ContentFrame.Navigate(typeof(Schedule));
                        break;
                    case "Nav_Flowline":
                        ContentFrame.Navigate(typeof(FlowLine));
                        break;
                    case "Nav_LiveChart":
                        ContentFrame.Navigate(typeof(LiveChart));
                        break;
                }
            }
        }
        #endregion
    }
}
