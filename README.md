# ConstructionScheduleSimulator:
Our thesis project: A program that can simulate a construction project schedule.

# Description:
This program is a prototype, which is used to model the choices, 
project managers are faced with throughout a construction project. 

It is possible to simulate a single construction project, where the
project manager is able to affect the course of the project either
by running the project with the minimum required workers or by 
adding extra workers. The number of extra workers required is 
determined by the logs of the previously simulated projects. If this
option is chosen, additional logging is printed to the console. If
the database contains enough data (more than 50 previous project, 
approximately), the program will create a probability distribution,
showing the accumulated probability of finishing within a number
of days after the set project deadline. 

It is also possible to run an experiment, simulating a number of 
construction projects. If this option is chosen, whether or not extra
workers are supplied is determined by the program. 

# User guide:
## Use case 1:
Use case 1 is to run an experiment, simulating a number of construction 
projects. This option deletes the current database and builds a new one 
throughout the simulation. For each iteration, the previous projects
(starting at 0) will be analyzed, to find the success rate of providing
extra workers to the project. You choose the number of simulations to
be performed with parameter 2. 

  1) navigate to the root of the project to begin
  2) compile the project by running 'javac program.java'
  3) run the program by running 'java program.java experiment <param1> <param2>'
      where param1 determines the type (experiment)
      where param2 determines number of projects (1...n)
      for example: java program.java experiment 1000

## Use case 2:
Use case 2 is to simulate a single construction project as a project 
manager. This option will not delete the existing database, so adding
workers will be determined based on the success statistics of previous
projects. You can determine (with parameter 2) whether you want to 
provide manual user input (to choose whether to add workers or not, 
and to choose when contractors are able to provide more workers 
throughout the project).

  1) navigate to the root of the project to begin
  2) compile the project by running 'javac program.java'
  3) run the program by running 'java program.java <param1> <param2>'
      where param1 determines the type (schedule)
      where param2 determines manual input (true/false)
      for example: java program.java schedule true

