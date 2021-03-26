using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Media;

namespace ScheduleAlgorithmWPF.ViewModels.Base
{
    public class ViewModelUtils
    {
        private static Dictionary<string, Brush> _legendColors;
        public static readonly Brush CRITICAL_BRUSH = new SolidColorBrush(Colors.Red);
        public static readonly Brush LOW_PRIORITY_CRITICAL_BRUSH = new SolidColorBrush()
        {
            Color = Colors.Red,
            Opacity = 0.2
        };
        public static readonly Brush BLUE_BRUSH = new SolidColorBrush(new Color() { R = 0, G = 123, B = 207 });

        public static Dictionary<string, Brush> LegendColors
        {
            get
            {
                if (_legendColors == null)
                {
                    SetColors();
                    return _legendColors;
                }                   
                else
                    return _legendColors;                
            }
            set
            {
                if (_legendColors == value)
                    return;
                else
                    _legendColors = value;
                
            }
        }

        /// <summary>
        /// Init colors
        /// </summary>
        private static void SetColors()
        {            
            LegendColors = new Dictionary<string, Brush>();
            LegendColors.Add("Concrete", new SolidColorBrush() { Color = Colors.Green });
            LegendColors.Add("VVS", new SolidColorBrush() { Color = Colors.Blue });
            LegendColors.Add("Carpenter", new SolidColorBrush() { Color = Colors.Sienna });
            LegendColors.Add("Electrician", new SolidColorBrush() { Color = Colors.Orange });
            LegendColors.Add("Painter", new SolidColorBrush() { Color = Colors.Purple });
            _legendColors = LegendColors;
        }

        public static Brush GetColorByCraft(string craft)
        {
            return LegendColors.FirstOrDefault(c => c.Key.Equals(craft)).Value;           
        }  

        public static void ResponseUserError(params string[] args)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("The following errors occured:\n");
            for (int i = 0; i < args.Length; i++)
            {                
                sb.Append(args[0] + "\n");
            }
            MessageBoxResult result = MessageBox.Show(sb.ToString(), "Warning", MessageBoxButton.OK, MessageBoxImage.Warning);
        }

        public static Brush GetRandomBrush()
        {
            Random rnd = new Random();
            var r = Convert.ToByte(rnd.Next(0, 100));
            var g = Convert.ToByte(rnd.Next(0, 256));
            var b = Convert.ToByte(rnd.Next(0, 256));

            Color color = Color.FromRgb(r,g,b);

            return new SolidColorBrush(color);
        }
    }
}
