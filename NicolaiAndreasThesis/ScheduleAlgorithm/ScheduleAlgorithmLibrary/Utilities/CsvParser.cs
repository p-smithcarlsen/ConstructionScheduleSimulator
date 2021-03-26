using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ScheduleAlgorithm.Domain.Entity;
using ScheduleAlgorithmLibrary.Algorithm;

namespace ScheduleAlgorithmLibrary.Utilities
{
    /// <summary>
    /// Custom CSV parser 
    /// </summary>
    public class CsvParser
    {
        #region Fields
        readonly List<ConstructionTask> _taskList = new List<ConstructionTask>();
        // static test file
        private readonly string _staticFile = $"{Properties.Resources.tinydataset}";
        #endregion
        
        #region Properties
        public string ExceptionMessage { get; set; }
        #endregion

        public List<ConstructionTask> LoadCsvFile(string file = null)
        {               
            bool header = true;           
            try
            {                
                if (file is null)
                {
                    foreach (string lines in _staticFile.Split('\n'))
                    {
                        if (header)
                        {
                            header = false;
                            continue;
                        }
                        if (!lines.Contains('\r'))
                            continue;                        
                        AddTask(lines );
                    }                    
                }
                else
                {
                    foreach (var readLine in File.ReadLines(file))
                    {
                        if (string.IsNullOrWhiteSpace(readLine))
                            continue;
                        if (header)
                        {
                            header = false;
                            continue;
                        }
                        AddTask(readLine);
                    }
                }      
            }
            #region Error Handling
            catch (FileNotFoundException f)
            {
                ExceptionMessage += f.Message + "\n";
            }     
            catch (FormatException fe)
            {
                ExceptionMessage += fe.Message + "\n";          
            }
            catch (IndexOutOfRangeException ie)
            {
                ExceptionMessage += ie.Message + "\n";
            }
            catch (IOException ioe)
            {
                ExceptionMessage += ioe.Message + "\n";
            }
            #endregion           
           
            return _taskList;
        }

        /// <summary>
        /// Read Lines from file
        /// </summary>
        /// <param name="readLine">Lines</param>
        private void AddTask(string readLine)
        {
            string[] taskRow = FormatPrecedenceString(readLine);
            if (taskRow[0] != "")
            {
                ConstructionTask task = new ConstructionTask
                {
                    TaskID = taskRow[0],
                    Zone = ConvertToInt(taskRow[1].Substring(1)),
                    Operation = taskRow[2],
                    EstimatedDuration = ConvertToDouble(taskRow[3]),
                    EstimatedResources = ConvertToInt(taskRow[4]),
                    Craft = taskRow[5],                  
                    Precedence = taskRow[6],
                    Progress = ProgressState.Pending
                };           
                _taskList.Add(task);
            }
        }

        /// <summary>
        /// String formatting
        /// </summary>
        /// <param name="s"></param>
        /// <returns></returns>
        private string[] FormatPrecedenceString(string s)
        {
            string specials = @"[\r\n\t]";
            string[] taskRow = System.Text.RegularExpressions.Regex.Replace(s, specials, "").Split(',');
            // remove ';' from tasks with no dependecies          
            if (taskRow[6].EndsWith(";"))                
                taskRow[6] = taskRow[6].Replace(";","");                     
            return taskRow;
        }
      
        /// <summary>
        /// Convert to double
        /// </summary>
        /// <param name="inputString"></param>
        /// <returns></returns>
        private static Double ConvertToDouble(string inputString)
        {
            bool success = Double.TryParse(inputString, out var outputDouble);
            if (success)
            {
                return outputDouble;
            }
            else
            {
                Console.WriteLine("Attempted conversion of '{0}' failed.",
                    inputString ?? "<null>");
                return 0;
            }
        }

        /// <summary>
        /// Convert to int
        /// </summary>
        /// <param name="inputString"></param>
        /// <returns></returns>
        private static int ConvertToInt(string inputString)
        {
            bool success = int.TryParse(inputString, out var outputInt);
            if (success)
            {
                return outputInt;
            }
            else
            {
                Console.WriteLine("Attempted conversion of '{0}' failed.",
                    inputString ?? "<null>");
                return 0;
            }
        }

        /// <summary>
        /// For benchmark purpose
        /// </summary>
        public void ClearContent()
        {
            _taskList.Clear();
        }
    }
}
