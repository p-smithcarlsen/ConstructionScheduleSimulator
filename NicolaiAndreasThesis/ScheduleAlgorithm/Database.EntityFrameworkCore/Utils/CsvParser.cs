using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Database.EntityFrameworkCore.Model;

namespace Database.EntityFrameworkCore.Utils
{
    public class CsvParser
    {
        List<ConstructionTask> _taskList = new List<ConstructionTask>();
        Dictionary<string,List<string>> _dependencyDictionary = new Dictionary<string, List<string>>();

        public static readonly ConstructionTask Source = new ConstructionTask() { TaskID = "0" };

        public List<ConstructionTask> LoadCsvFile(string fileName)
        {
            bool header = true;
            foreach (var readLine in File.ReadLines(fileName))
            {
                if (string.IsNullOrWhiteSpace(readLine))
                {
                    continue;
                }
                if (header)
                {
                    header = false;
                    continue;
                }

                var taskRow = readLine.Split(',');

                if (taskRow[0] != "")
                {
                    try
                    {
                        ConstructionTask task = new ConstructionTask()
                        {
                            TaskID = taskRow[0],
                            Zone = taskRow[1],
                            Operation = taskRow[2],
                            EstimatedDuration = ConvertToDouble(taskRow[3]),
                            Progress = ProgressState.Pending,
                            Craft = taskRow[5],
                            Precedence = new List<ConstructionTask>(),
                            Resources = new List<Resource>()
                        };

                        var dependencies = taskRow[6];
                        _taskList.Add(task);
                        AddToDependencyDictionary(task.TaskID, dependencies);
                    }
                    catch (FormatException fe)
                    {
                        Console.WriteLine(fe.Message);
                        throw;
                    }
                    catch (IndexOutOfRangeException ie)
                    {
                        Console.WriteLine(ie.Message);
                        break;
                    }
                    catch (IOException ioe)
                    {
                        Console.WriteLine(ioe.Message);
                        break;
                    }
                }
            }
            foreach (var constructionTask in _taskList)
            {
                var precedenceList = _dependencyDictionary[constructionTask.TaskID];

                foreach (var taskId in precedenceList)
                {
                    var dependentTask = _taskList.Find(x => x.TaskID.Equals(taskId));
                    constructionTask.Precedence.Add(dependentTask);
                }


                if (constructionTask.Precedence.Count < 1)
                {
                    constructionTask.Precedence.Add(Source);
                }

            }
            return _taskList;
        }

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

        private void AddToDependencyDictionary(string currentTaskID, string dependencyString)
        {
            var dependencyList = new List<string>();
            string[] dependentTaskIDs = dependencyString.Split(';');

            foreach (var taskID in dependentTaskIDs)
            {
                if (string.IsNullOrWhiteSpace(taskID))
                {
                    continue;
                }
                try
                {
                    dependencyList.Add(taskID);
                }
                catch (Exception e)
                {
                    Console.WriteLine(e);
                    throw;
                }
            }
            _dependencyDictionary.Add(currentTaskID,dependencyList);
        }
    }
}
